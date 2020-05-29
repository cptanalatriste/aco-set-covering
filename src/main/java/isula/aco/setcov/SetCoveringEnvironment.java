package isula.aco.setcov;

import isula.aco.Environment;
import isula.aco.exception.InvalidInputException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetCoveringEnvironment extends Environment {

    private static Logger logger = Logger.getLogger(SetCoveringEnvironment.class.getName());
    public static final int COVERED = 1;

    private final long preprocessTimeInSeconds;
    private final SetCoveringPreProcessor preProcessor;

    public Set<Integer> dominatedCandidates;
    public Set<Integer> mandatoryCandidates;
    private Map<Integer, Set<Integer>> samplesPerCandidate;


    /**
     * Creates an Environment for the Ants to traverse.
     *
     * @param problemRepresentation Graph representation of the problem to be solved.
     * @throws InvalidInputException When the problem graph is incorrectly formed.
     */
    public SetCoveringEnvironment(double[][] problemRepresentation) throws InvalidInputException {
        super(problemRepresentation);

        Instant preprocessStart = Instant.now();

        this.preProcessor = new SetCoveringPreProcessor(this);

        this.samplesPerCandidate = Collections.unmodifiableMap(preProcessor.getSamplesPerCandidate());
        this.dominatedCandidates = preProcessor.getDominatedCandidates();
        logger.info(dominatedCandidates.size() + " dominated candidates from " + this.getNumberOfCandidates());

        this.mandatoryCandidates = this.findMandatoryCandidates();

        Instant preprocessEnd = Instant.now();
        this.preprocessTimeInSeconds = Duration.between(preprocessStart, preprocessEnd).getSeconds();
        logger.info("Pre-process finished in " + preprocessTimeInSeconds + " seconds.");
    }

    public Set<Integer> getMandatoryCandidates() {
        return this.mandatoryCandidates;
    }

    private Set<Integer> findMandatoryCandidates() {


        Set<Integer> mandatoryCandidates = new HashSet<>();

        for (int sampleIndex = 0; sampleIndex < this.getNumberOfSamples(); sampleIndex += 1) {
            Set<Integer> coveringCandidates = this.getCoveringCandidates(sampleIndex);

            if (coveringCandidates.size() == 1) {
                mandatoryCandidates.add(coveringCandidates.iterator().next());
            }
        }

        logger.info("Mandatory candidates found: " + mandatoryCandidates.size());
        return mandatoryCandidates;
    }

    public Set<Integer> getDominatedCandidates() {
        return this.dominatedCandidates;
    }

    protected double[][] createPheromoneMatrix() {
        return new double[this.getNumberOfCandidates()][1];
    }

    public int getNumberOfCandidates() {
        return this.getProblemRepresentation()[0].length;
    }

    public int getNumberOfSamples() {
        return this.getProblemRepresentation().length;
    }

    public Set<Integer> getCoveringCandidates(int sampleIndex) {
        return getCoveringCandidates(sampleIndex, getAllCandidatesStream());
    }

    private Set<Integer> getCoveringCandidates(int sampleIndex, IntStream candidateStream) {
        return candidateStream
                .filter(candidateIndex -> !this.isDominatedCandidate(candidateIndex) &&
                        this.getProblemRepresentation()[sampleIndex][candidateIndex] == COVERED)
                .boxed()
                .collect(Collectors.toSet());
    }

    public IntStream getAllCandidatesStream() {
        return IntStream.range(0, this.getNumberOfCandidates());
    }


    public Set<Integer> getSamplesForNonDominatedCandidate(int candidateIndex) {
        return this.samplesPerCandidate.get(candidateIndex);
    }

    public boolean isDominatedCandidate(int candidateIndex) {
        return this.dominatedCandidates.contains(candidateIndex);
    }

    public Map<Integer, Set<Integer>> getSamplesPerCandidate() {
        return samplesPerCandidate;
    }

    public boolean isValidSolution(Integer[] solutionFound) {
        boolean[] samplesCovered = new boolean[this.getNumberOfSamples()];
        int pendingSamples = this.getNumberOfSamples();

        for (Integer candidateIndex : solutionFound) {
            if (candidateIndex != null) {

                for (Integer sampleIndex : this.preProcessor.getSamplesCovered(candidateIndex)) {
                    if (!samplesCovered[sampleIndex]) {
                        samplesCovered[sampleIndex] = true;
                        pendingSamples -= 1;
                    }
                }
            }
        }
        if (pendingSamples > 0) {
            List<Integer> uncoveredSamples = IntStream.range(0, this.getNumberOfSamples())
                    .filter((candidateIndex) -> !samplesCovered[candidateIndex])
                    .boxed()
                    .collect(Collectors.toList());
            logger.warning("Solution does not cover " + pendingSamples + " samples");
            logger.warning("Pending samples " + uncoveredSamples);
        }

        return pendingSamples == 0;
    }

    @Override
    public String toString() {
        return "SetCoveringEnvironment{" +
                "preprocessTimeInSeconds=" + preprocessTimeInSeconds +
                ", dominatedCandidates.size()=" + dominatedCandidates.size() +
                ", mandatoryCandidates.size()=" + mandatoryCandidates.size() +
                "} " + super.toString();
    }
}
