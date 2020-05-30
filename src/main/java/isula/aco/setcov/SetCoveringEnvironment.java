package isula.aco.setcov;

import isula.aco.Environment;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetCoveringEnvironment extends Environment {

    private static final Logger logger = Logger.getLogger(SetCoveringEnvironment.class.getName());

    private final long preprocessTimeInSeconds;

    public final Set<Integer> dominatedCandidates;
    public final Set<Integer> mandatoryCandidates;
    private final Map<Integer, Set<Integer>> samplesPerCandidate;
    private final Map<Integer, Set<Integer>> candidatesPerSample;

    private int numberOfCandidates;
    private int numberOfSamples;


    /**
     * Creates an Environment for the Ants to traverse.
     */
    public SetCoveringEnvironment(SetCoveringPreProcessor preProcessor) {
        super();

        Instant preprocessStart = Instant.now();

        this.numberOfCandidates = preProcessor.getNumberOfCandidates();
        this.numberOfSamples = preProcessor.getNumberOfSamples();

        this.candidatesPerSample = preProcessor.getCandidatesPerSample();
        this.dominatedCandidates = preProcessor.findDominatedCandidates();
        this.samplesPerCandidate = Collections.unmodifiableMap(preProcessor.getSamplesPerCandidate());

        logger.info(dominatedCandidates.size() + " dominated candidates from " + this.getNumberOfCandidates());
        this.mandatoryCandidates = this.findMandatoryCandidates();

        Instant preprocessEnd = Instant.now();
        this.preprocessTimeInSeconds = Duration.between(preprocessStart, preprocessEnd).getSeconds();
        logger.info("Pre-process finished in " + preprocessTimeInSeconds + " seconds.");

        this.setPheromoneMatrix(this.createPheromoneMatrix());
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

        if (this.numberOfCandidates != 0) {
            return new double[this.numberOfCandidates][1];
        }

        return null;
    }

    public int getNumberOfCandidates() {
        return numberOfCandidates;
    }

    public int getNumberOfSamples() {
        return this.numberOfSamples;
    }

    public Set<Integer> getCoveringCandidates(int sampleIndex) {
        return this.candidatesPerSample.get(sampleIndex);
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

    public boolean isValidSolution(List<Integer> solutionFound) {
        boolean[] samplesCovered = new boolean[this.getNumberOfSamples()];
        int pendingSamples = this.getNumberOfSamples();

        for (Integer candidateIndex : solutionFound) {
            if (candidateIndex != null) {

                for (Integer sampleIndex : this.samplesPerCandidate.get(candidateIndex)) {
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
