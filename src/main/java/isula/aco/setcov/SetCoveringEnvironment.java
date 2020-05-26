package isula.aco.setcov;

import isula.aco.Environment;
import isula.aco.exception.InvalidInputException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SetCoveringEnvironment extends Environment {

    private static Logger logger = Logger.getLogger(SetCoveringEnvironment.class.getName());
    public static final int COVERED = 1;

    public Set<Integer> dominatedCandidates;
    public Set<Integer> mandatoryCandidates;


    /**
     * Creates an Environment for the Ants to traverse.
     *
     * @param problemRepresentation Graph representation of the problem to be solved.
     * @throws InvalidInputException When the problem graph is incorrectly formed.
     */
    public SetCoveringEnvironment(double[][] problemRepresentation) throws InvalidInputException {
        super(problemRepresentation);
        this.dominatedCandidates = this.findDominatedCandidates();
        this.mandatoryCandidates = this.findMandatoryCandidates();
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

    private Set<Integer> findDominatedCandidates() {

        boolean[] dominatedChecklist = new boolean[this.getNumberOfCandidates()];

        for (int candidateIndex = 0; candidateIndex < this.getNumberOfCandidates(); candidateIndex++) {
            Set<Integer> candidateSamples = this.getSamplesCovered(candidateIndex);

            for (int opponentIndex = candidateIndex + 1; opponentIndex < this.getNumberOfCandidates(); opponentIndex++) {

                if (!dominatedChecklist[opponentIndex]) {
                    Set<Integer> opponentSamples = this.getSamplesCovered(opponentIndex);

                    if (opponentSamples.containsAll(candidateSamples)) {
                        dominatedChecklist[candidateIndex] = true;
                        break;
                    } else if (candidateSamples.containsAll(opponentSamples)) {
                        dominatedChecklist[opponentIndex] = true;
                    }
                }
            }
        }

        Set<Integer> dominatedAsSet = IntStream.range(0, this.getNumberOfCandidates())
                .filter(candidateIndex -> dominatedChecklist[candidateIndex])
                .boxed()
                .collect(Collectors.toSet());
        logger.info(dominatedAsSet.size() + " dominated candidates from " + this.getNumberOfCandidates());

        return dominatedAsSet;
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
        return IntStream.range(0, this.getNumberOfCandidates())
                .filter(candidateIndex -> !this.isDominatedCandidate(candidateIndex) &&
                        this.getProblemRepresentation()[sampleIndex][candidateIndex] == COVERED)
                .boxed()
                .collect(Collectors.toSet());
    }


    public Set<Integer> getSamplesCovered(int candidateIndex) {

        return IntStream.range(0, this.getNumberOfSamples())
                .filter(sampleIndex -> this.getProblemRepresentation()[sampleIndex][candidateIndex] == COVERED)
                .boxed()
                .collect(Collectors.toSet());


    }

    public boolean isDominatedCandidate(int candidateIndex) {
        return this.dominatedCandidates.contains(candidateIndex);
    }
}
