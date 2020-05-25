package isula.aco.setcov;

import isula.aco.Environment;
import isula.aco.exception.InvalidInputException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SetCoveringEnvironment extends Environment {

    public static final int COVERED = 1;
    private final int numberOfSamples;

    public int getNumberOfSamples() {
        return numberOfSamples;
    }

    private int numberOfCandidates;

    /**
     * Creates an Environment for the Ants to traverse.
     *
     * @param problemRepresentation Graph representation of the problem to be solved.
     * @throws InvalidInputException When the problem graph is incorrectly formed.
     */
    public SetCoveringEnvironment(double[][] problemRepresentation) throws InvalidInputException {
        super(problemRepresentation);
        this.numberOfCandidates = problemRepresentation[0].length;
        this.numberOfSamples = problemRepresentation.length;
    }

    protected double[][] createPheromoneMatrix() {
        return new double[this.numberOfCandidates][];
    }

    public int getNumberOfCandidates() {
        return this.numberOfCandidates;
    }

    public List<Integer> getSamplesCovered(int candidateIndex) {
        ArrayList<Integer> coveredSamples = new ArrayList<>();

        IntStream.range(0, this.numberOfSamples).forEachOrdered(sampleIndex -> {

            if (this.getProblemRepresentation()[sampleIndex][candidateIndex] == COVERED) {
                coveredSamples.add(sampleIndex);
            }
        });

        return coveredSamples;
    }
}
