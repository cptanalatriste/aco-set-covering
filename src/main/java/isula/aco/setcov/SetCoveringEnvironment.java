package isula.aco.setcov;

import isula.aco.Environment;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SetCoveringEnvironment extends Environment {

    private static final Logger logger = Logger.getLogger(SetCoveringEnvironment.class.getName());

    private long preprocessTimeInSeconds;

    public final Set<Integer> dominatedCandidates;
    public final Set<Integer> mandatoryCandidates;
    private final Map<Integer, Set<Integer>> samplesPerCandidate;
    private final Map<Integer, Set<Integer>> candidatesPerSample;

    private int numberOfCandidates;
    private int numberOfSamples;

    public SetCoveringEnvironment(SetCoveringEnvironment environmentToClone) {
        this.preprocessTimeInSeconds = environmentToClone.getPreprocessTimeInSeconds();
        this.dominatedCandidates = environmentToClone.getDominatedCandidates();
        this.mandatoryCandidates = environmentToClone.getMandatoryCandidates();
        this.samplesPerCandidate = environmentToClone.getSamplesPerCandidate();
        this.candidatesPerSample = environmentToClone.getCandidatesPerSample();
        this.numberOfCandidates = environmentToClone.getNumberOfCandidates();
        this.numberOfSamples = environmentToClone.getNumberOfSamples();
        this.preprocessTimeInSeconds = environmentToClone.preprocessTimeInSeconds;

        this.setPheromoneMatrix(this.createPheromoneMatrix());
    }

    public SetCoveringEnvironment(SetCoveringPreProcessor preProcessor) {
        this(preProcessor, true);
    }


    /**
     * Creates an Environment for the Ants to traverse.
     */
    public SetCoveringEnvironment(SetCoveringPreProcessor preProcessor, boolean performDominanceAnalysis) {
        super();

        Instant preprocessStart = Instant.now();

        this.numberOfCandidates = preProcessor.getNumberOfCandidates();
        this.numberOfSamples = preProcessor.getNumberOfSamples();

        this.candidatesPerSample = preProcessor.getCandidatesPerSample();

        if (performDominanceAnalysis) {
            this.dominatedCandidates = preProcessor.findDominatedCandidates();

        } else {
            logger.warning("Skipping dominance analysis");
            this.dominatedCandidates = Collections.emptySet();
        }

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
            Set<Integer> coveringCandidates = this.getCoveringCandidates(sampleIndex).stream()
                    .filter((candidateIndex) -> !this.dominatedCandidates.contains(candidateIndex))
                    .collect(Collectors.toUnmodifiableSet());

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


    private Map<Integer, Set<Integer>> getCandidatesPerSample() {
        return this.candidatesPerSample;
    }

    public boolean isDominatedCandidate(int candidateIndex) {
        return this.dominatedCandidates.contains(candidateIndex);
    }


    private long getPreprocessTimeInSeconds() {
        return this.preprocessTimeInSeconds;
    }

    public Map<Integer, Set<Integer>> getSamplesPerCandidate() {
        return samplesPerCandidate;
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
