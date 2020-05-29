package isula.aco.setcov;

import isula.aco.Ant;
import isula.aco.exception.SolutionConstructionException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AntForSetCovering extends Ant<Integer, SetCoveringEnvironment> {

    private final Set<Integer> mandatoryCandidates;
    private boolean[] samplesCovered;
    private Map<Integer, Set<Integer>> samplesPerCandidate;
    private int numberOfSamples;


    public AntForSetCovering(SetCoveringEnvironment environment) {
        super();

        this.numberOfSamples = environment.getNumberOfSamples();
        this.samplesCovered = new boolean[numberOfSamples];
        this.samplesPerCandidate = environment.getSamplesPerCandidate();
        this.mandatoryCandidates = environment.getMandatoryCandidates();

        this.setSolution(new Integer[environment.getNumberOfCandidates()]);
    }

    @Override
    public void clear() {
        super.clear();
        this.samplesCovered = new boolean[numberOfSamples];
        this.mandatoryCandidates.forEach(this::visitNode);
    }

    @Override
    public void visitNode(Integer candidateIndex) {
        super.visitNode(candidateIndex);

        Set<Integer> candidateSamples = samplesPerCandidate.get(candidateIndex);

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

        return commonElements.size() / (double) this.numberOfSamples;
    }

    private Set<Integer> getUncoveredSamples() {

        return IntStream.range(0, this.numberOfSamples)
                .filter(sampleIndex -> !this.samplesCovered[sampleIndex])
                .boxed()
                .collect(Collectors.toSet());
    }

    public double getSolutionCost(SetCoveringEnvironment environment) {
        if (!isSolutionReady(environment)) {
            throw new RuntimeException("Cannot calculate cost of an incomplete solution");
        }

        return Math.toIntExact(Arrays.stream(getSolution()).filter(Objects::nonNull).count());
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
