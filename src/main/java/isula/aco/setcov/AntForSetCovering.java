package isula.aco.setcov;

import isula.aco.Ant;
import isula.aco.exception.SolutionConstructionException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AntForSetCovering extends Ant<Integer, SetCoveringEnvironment> {

    private final SetCoveringEnvironment environment;
    private boolean[] samplesCovered;


    public AntForSetCovering(SetCoveringEnvironment environment) {
        super();

        this.environment = environment;
        this.samplesCovered = new boolean[environment.getNumberOfSamples()];

        this.setSolution(new ArrayList<>());
    }

    @Override
    public void clear() {
        super.clear();
        this.samplesCovered = new boolean[environment.getNumberOfSamples()];
        this.environment.getMandatoryCandidates().forEach((candidateIndex) -> this.visitNode(candidateIndex, this.environment));
    }

    @Override
    public void visitNode(Integer candidateIndex, SetCoveringEnvironment environment) {
        super.visitNode(candidateIndex, environment);

        Set<Integer> candidateSamples = environment.getSamplesPerCandidate().get(candidateIndex);

        if (candidateSamples == null) {
            throw new SolutionConstructionException("Candidate " + candidateIndex + " is dominated.");
        }

        candidateSamples.forEach((sampleIndex) -> this.samplesCovered[sampleIndex] = true);
    }

    public boolean isSampleCovered(int sampleIndex) {
        return this.samplesCovered[sampleIndex];
    }

    public Double getHeuristicValue(Integer candidateIndex, Integer positionInSolution,
                                    SetCoveringEnvironment environment) {
        Set<Integer> uncoveredSamples = this.getUncoveredSamples();
        Set<Integer> coveredByCandidate = environment.getSamplesForNonDominatedCandidate(candidateIndex);

        Set<Integer> commonElements = uncoveredSamples.stream()
                .distinct()
                .filter(coveredByCandidate::contains)
                .collect(Collectors.toSet());

        return commonElements.size() / (double) this.environment.getNumberOfSamples();
    }

    private Set<Integer> getUncoveredSamples() {

        return IntStream.range(0, this.environment.getNumberOfSamples())
                .filter(sampleIndex -> !this.samplesCovered[sampleIndex])
                .boxed()
                .collect(Collectors.toSet());
    }

    public double getSolutionCost(SetCoveringEnvironment environment) {
        if (!isSolutionReady(environment)) {
            throw new RuntimeException("Cannot calculate cost of an incomplete solution");
        }

        return Math.toIntExact(getSolution().stream().filter(Objects::nonNull).count());
    }

    public boolean isSolutionReady(SetCoveringEnvironment environment) {
        Set<Integer> uncoveredSamples = this.getUncoveredSamples();
        return uncoveredSamples.size() == 0;
    }


    public List<Integer> getNeighbourhood(SetCoveringEnvironment environment) {

        return IntStream.range(0, environment.getNumberOfCandidates())
                .filter(candidateIndex -> !this.isNodeVisited(candidateIndex) && !environment.isDominatedCandidate(candidateIndex))
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
