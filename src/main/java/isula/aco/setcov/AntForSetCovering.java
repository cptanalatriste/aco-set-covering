package isula.aco.setcov;

import isula.aco.Ant;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AntForSetCovering extends Ant<Integer, SetCoveringEnvironment> {

    private boolean[] samplesCovered;
    private final double[][] problemRepresentation;
    private int numberOfSamples;


    public AntForSetCovering(SetCoveringEnvironment environment) {
        super();

        this.numberOfSamples = environment.getNumberOfSamples();
        this.samplesCovered = new boolean[numberOfSamples];
        this.problemRepresentation = environment.getProblemRepresentation();

        this.setSolution(new Integer[environment.getNumberOfCandidates()]);
    }

    @Override
    public void clear() {
        super.clear();
        this.samplesCovered = new boolean[numberOfSamples];
    }

    @Override
    public void visitNode(Integer candidateIndex) {
        super.visitNode(candidateIndex);

        IntStream.range(0, this.numberOfSamples).forEachOrdered(sampleIndex -> {
            if ((int) this.problemRepresentation[sampleIndex][candidateIndex] == SetCoveringEnvironment.COVERED) {
                this.samplesCovered[sampleIndex] = true;
            }
        });
    }

    public boolean isSampleCovered(int sampleIndex) {
        return this.samplesCovered[sampleIndex];
    }

    public Double getHeuristicValue(Integer candidateIndex, Integer positionInSolution,
                                    SetCoveringEnvironment environment) {
        List<Integer> uncoveredSamples = this.getUncoveredSamples();
        List<Integer> coveredByCandidate = environment.getSamplesCovered(candidateIndex);

        Set<Integer> commonElements = uncoveredSamples.stream()
                .distinct()
                .filter(coveredByCandidate::contains)
                .collect(Collectors.toSet());

        return commonElements.size() / (double) this.numberOfSamples;
    }

    private List<Integer> getUncoveredSamples() {

        return IntStream.range(0, this.numberOfSamples)
                .filter(sampleIndex -> !this.samplesCovered[sampleIndex])
                .boxed()
                .collect(Collectors.toList());
    }

    public double getSolutionCost(SetCoveringEnvironment environment) {
        if (!isSolutionReady(environment)) {
            throw new RuntimeException("Cannot calculate cost of an incomplete solution");
        }


        return this.getCurrentIndex();
    }

    public boolean isSolutionReady(SetCoveringEnvironment environment) {
        List<Integer> uncoveredSamples = this.getUncoveredSamples();
        return uncoveredSamples.size() == 0;
    }


    public List<Integer> getNeighbourhood(SetCoveringEnvironment environment) {

        return IntStream.range(0, environment.getNumberOfCandidates())
                .filter(candidateIndex -> !this.isNodeVisited(candidateIndex))
                .boxed()
                .collect(Collectors.toList());

    }

    public Double getPheromoneTrailValue(Integer solutionComponent, Integer positionInSolution,
                                         SetCoveringEnvironment environment) {


        double[][] pheromoneMatrix = environment.getPheromoneMatrix();
        return pheromoneMatrix[solutionComponent][0];
    }

    public void setPheromoneTrailValue(Integer solutionComponent, Integer positionInSolution,
                                       SetCoveringEnvironment environment, Double value) {

        double[][] pheromoneMatrix = environment.getPheromoneMatrix();
        pheromoneMatrix[solutionComponent][0] = value;

    }
}
