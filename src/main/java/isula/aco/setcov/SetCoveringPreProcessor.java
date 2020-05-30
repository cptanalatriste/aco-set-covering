package isula.aco.setcov;

import org.apache.commons.math3.util.Combinations;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static isula.aco.setcov.SetCoveringEnvironment.COVERED;

public class SetCoveringPreProcessor {

    private final SetCoveringEnvironment environment;
    private final HashMap<Integer, Set<Integer>> samplesPerCandidate;
    private final Set<Integer> dominatedCandidates;

    public SetCoveringPreProcessor(SetCoveringEnvironment environment) {
        this.environment = environment;
        this.samplesPerCandidate = calculateSamplesPerCandidate();
        this.dominatedCandidates = findDominatedCandidates();
    }

    private HashMap<Integer, Set<Integer>> calculateSamplesPerCandidate() {

        HashMap<Integer, Set<Integer>> samplesPerCandidate = new HashMap<>();

        environment.getAllCandidatesStream().forEach((candidateIndex) -> samplesPerCandidate.put(candidateIndex, this.getSamplesCovered(candidateIndex)));

        return samplesPerCandidate;
    }

    private Set<Integer> findDominatedCandidates() {
        Set<Integer> dominatedCandidates = new HashSet<>();
        for (int[] candidates : new Combinations(environment.getNumberOfCandidates(), 2)) {
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


    public Set<Integer> getDominatedCandidates() {
        return dominatedCandidates;
    }

    public HashMap<Integer, Set<Integer>> getSamplesPerCandidate() {
        return this.samplesPerCandidate;
    }

    public Set<Integer> getSamplesCovered(int candidateIndex) {

        return IntStream.range(0, environment.getNumberOfSamples())
                .filter(sampleIndex -> environment.getProblemRepresentation()[sampleIndex][candidateIndex] == COVERED)
                .boxed()
                .collect(Collectors.toUnmodifiableSet());


    }
}
