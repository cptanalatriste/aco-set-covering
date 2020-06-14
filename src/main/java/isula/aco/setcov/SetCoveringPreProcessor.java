package isula.aco.setcov;

import isula.aco.exception.ConfigurationException;
import org.apache.commons.math3.util.Combinations;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class SetCoveringPreProcessor {

    private static final Logger logger = Logger.getLogger(SetCoveringPreProcessor.class.getName());

    private Map<Integer, Set<Integer>> samplesPerCandidate;
    private final HashMap<Integer, Set<Integer>> candidatesPerSample;
    public final Duration preprocessingTimeLimit;


    private int numberOfCandidates;
    private int numberOfSamples;

    public SetCoveringPreProcessor(Duration preprocessingTimeLimit) {
        this.samplesPerCandidate = new HashMap<>();
        this.candidatesPerSample = new HashMap<>();
        this.preprocessingTimeLimit = preprocessingTimeLimit;
    }

    public void setNumberOfCandidates(int numberOfCandidates) {
        this.numberOfCandidates = numberOfCandidates;

        IntStream.range(0, numberOfCandidates)
                .forEachOrdered((candidateIndex) -> samplesPerCandidate.put(candidateIndex, new HashSet<>()));
    }


    public void addCandidatesForSample(int sampleIndex, String[] candidatesAsTokens) {
        Set<Integer> candidateList = Arrays.stream(candidatesAsTokens)
                .map(Integer::parseInt)
                .collect(Collectors.toUnmodifiableSet());
        this.candidatesPerSample.put(sampleIndex, candidateList);

        candidateList.stream()
                .unordered()
                .forEach((candidateIndex) -> samplesPerCandidate.get(candidateIndex).add(sampleIndex));
    }

    public Set<Integer> findDominatedCandidates() {

        logger.info("Starting dominance analysis");

        if (this.getNumberOfCandidates() == 0 || this.getNumberOfSamples() == 0) {
            throw new ConfigurationException("You need to set the number of candidates and samples before " +
                    "starting pre-processing");
        }

        Set<Integer> dominatedCandidates = Collections.synchronizedSet(new HashSet<>());

        Combinations combinations = new Combinations(this.getNumberOfCandidates(), 2);
        Temporal executionStartTime = Instant.now();
        StreamSupport.stream(combinations.spliterator(), true)
                .unordered()
                .takeWhile((combination) -> Duration.between(executionStartTime, Instant.now()).compareTo(preprocessingTimeLimit) < 0)
                .forEach((candidates) -> {
                    int candidateIndex = candidates[0];
                    int opponentIndex = candidates[1];

                    Set<Integer> candidateSamples = this.samplesPerCandidate.get(candidateIndex);
                    Set<Integer> opponentSamples = this.samplesPerCandidate.get(opponentIndex);

                    if (candidateSamples != null && opponentSamples != null) {

                        if (opponentSamples.size() >= candidateSamples.size() &&
                                opponentSamples.containsAll(candidateSamples)) {
                            dominatedCandidates.add(candidateIndex);
                        } else if (candidateSamples.size() > opponentSamples.size() &&
                                candidateSamples.containsAll(opponentSamples)) {
                            dominatedCandidates.add(opponentIndex);
                        }
                    }
                });

        return Collections.unmodifiableSet(dominatedCandidates);
    }


    public int getNumberOfCandidates() {
        return this.numberOfCandidates;
    }

    public int getNumberOfSamples() {
        return this.numberOfSamples;
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
