package isula.aco.setcov;

import isula.aco.exception.ConfigurationException;
import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class SetCoveringPreProcessor {

    private static final Logger logger = Logger.getLogger(SetCoveringPreProcessor.class.getName());


    private Map<Integer, Set<Integer>> samplesPerCandidate;
    private final HashMap<Integer, Set<Integer>> candidatesPerSample;

    private int numberOfCandidates;
    private int numberOfSamples;

    public SetCoveringPreProcessor() {
        this.samplesPerCandidate = new HashMap<>();
        this.candidatesPerSample = new HashMap<>();
    }

    public void addCandidatesForSample(int sampleIndex, String[] candidatesAsTokens) {
        this.candidatesPerSample.put(sampleIndex, Arrays.stream(candidatesAsTokens)
                .map(Integer::parseInt)
                .collect(Collectors.toUnmodifiableSet()));
    }

    public Map<Integer, Set<Integer>> calculateSamplesPerCandidate() {

        Map<Integer, Set<Integer>> samplesPerCandidate = new ConcurrentHashMap<>();

        IntStream.range(0, this.getNumberOfCandidates())
                .parallel()
                .forEach((candidateIndex) -> samplesPerCandidate.put(candidateIndex,
                        this.getSamplesCovered(candidateIndex)));

        return samplesPerCandidate;
    }

    public Set<Integer> findDominatedCandidates() {

        logger.fine("Starting calculateSamplesPerCandidate()");
        this.samplesPerCandidate = calculateSamplesPerCandidate();

        logger.fine("Before inspecting combinations");

        if (this.getNumberOfCandidates() == 0 || this.getNumberOfSamples() == 0) {
            throw new ConfigurationException("You need to set the number of candidates and samples before " +
                    "starting pre-processing");
        }

        Set<Integer> dominatedCandidates = Collections.synchronizedSet(new HashSet<>());

        Combinations combinations = new Combinations(this.getNumberOfCandidates(), 2);
        StreamSupport.stream(combinations.spliterator(), true).forEach((candidates) -> {
            int candidateIndex = candidates[0];
            Set<Integer> candidateSamples = this.samplesPerCandidate.get(candidateIndex);
            int opponentIndex = candidates[1];
            Set<Integer> opponentSamples = this.samplesPerCandidate.get(opponentIndex);

            if (candidateSamples != null && opponentSamples != null) {
                if (opponentSamples.containsAll(candidateSamples)) {
                    dominatedCandidates.add(candidateIndex);
                } else if (candidateSamples.containsAll(opponentSamples)) {
                    dominatedCandidates.add(opponentIndex);
                }
            }
        });

        dominatedCandidates.forEach((candidateIndex) -> this.samplesPerCandidate.remove(candidateIndex));

        return Collections.unmodifiableSet(dominatedCandidates);
    }


    public Set<Integer> getSamplesCovered(int candidateIndex) {

        return this.candidatesPerSample.entrySet().stream()
                .filter((entry) -> entry.getValue().contains(candidateIndex))
                .map(Map.Entry::getKey)
                .collect(Collectors.toUnmodifiableSet());

    }

    public int getNumberOfCandidates() {
        return this.numberOfCandidates;
    }

    public int getNumberOfSamples() {
        return this.numberOfSamples;
    }

    public void setNumberOfCandidates(int numberOfCandidates) {
        this.numberOfCandidates = numberOfCandidates;
    }

    public void setNumberOfSamples(int numberOfSamples) {
        this.numberOfSamples = numberOfSamples;
    }

    public Map<Integer, Set<Integer>> getCandidatesPerSample() {
        return this.candidatesPerSample;
    }

    public Map<Integer, Set<Integer>> getSamplesPerCandidate() {
        return this.samplesPerCandidate;
    }


}
