package isula.aco.setcov;

import isula.aco.exception.ConfigurationException;
import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetCoveringPreProcessor {

    private HashMap<Integer, Set<Integer>> samplesPerCandidate;
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

    private HashMap<Integer, Set<Integer>> calculateSamplesPerCandidate() {

        HashMap<Integer, Set<Integer>> samplesPerCandidate = new HashMap<>();

        IntStream.range(0, this.getNumberOfCandidates())
                .forEach((candidateIndex) -> samplesPerCandidate.put(candidateIndex,
                        this.getSamplesCovered(candidateIndex)));

        return samplesPerCandidate;
    }

    public Set<Integer> findDominatedCandidates() {
        this.samplesPerCandidate = calculateSamplesPerCandidate();

        if (this.getNumberOfCandidates() == 0 || this.getNumberOfSamples() == 0) {
            throw new ConfigurationException("You need to set the number of candidates and samples before " +
                    "starting pre-processing");
        }

        Set<Integer> dominatedCandidates = new HashSet<>();
        for (int[] candidates : new Combinations(this.getNumberOfCandidates(), 2)) {
            int candidateIndex = candidates[0];
            Set<Integer> candidateSamples = this.samplesPerCandidate.get(candidateIndex);
            int opponentIndex = candidates[1];
            Set<Integer> opponentSamples = this.samplesPerCandidate.get(opponentIndex);

            if (candidateSamples != null && opponentSamples != null) {
                if (opponentSamples.containsAll(candidateSamples)) {
                    dominatedCandidates.add(candidateIndex);
                    this.samplesPerCandidate.remove(candidateIndex);
                } else if (candidateSamples.containsAll(opponentSamples)) {
                    dominatedCandidates.add(opponentIndex);
                    this.samplesPerCandidate.remove(opponentIndex);
                }
            }
        }

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

    public HashMap<Integer, Set<Integer>> getSamplesPerCandidate() {
        return this.samplesPerCandidate;
    }


}
