package isula.aco.setcov;

import isula.aco.algorithms.iteratedants.AntWithPartialSolution;
import isula.aco.exception.SolutionConstructionException;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AntForSetCovering extends AntWithPartialSolution<Integer, SetCoveringEnvironment> {

    private static final Logger logger = Logger.getLogger(AntForSetCovering.class.getName());

    private final SetCoveringEnvironment environment;
    private boolean[] samplesCovered;


    public AntForSetCovering(SetCoveringEnvironment environment) {
        this(environment, environment.getMandatoryCandidates());
    }

    public AntForSetCovering(SetCoveringEnvironment environment, Set<Integer> initialSolution) {
        super();

        this.environment = environment;
        this.samplesCovered = new boolean[environment.getNumberOfSamples()];

        this.setSolution(new ArrayList<>());
        this.setPartialSolution(initialSolution);
    }

    @Override
    public void clear() {
        this.samplesCovered = new boolean[environment.getNumberOfSamples()];
        super.clear();
        logger.fine("Initial solution size: " + this.getSolution().size());
    }

    @Override
    public SetCoveringEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public void visitNode(Integer candidateIndex, SetCoveringEnvironment environment) {
        logger.fine("Visiting node");
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
        logger.fine("Getting heuristic value");

        Set<Integer> uncoveredSamples = this.getUncoveredSamples();
        Set<Integer> coveredByCandidate = environment.getSamplesForNonDominatedCandidate(candidateIndex);

        Set<Integer> commonElements = uncoveredSamples.stream()
                .distinct()
                .filter(coveredByCandidate::contains)
                .collect(Collectors.toSet());

        return commonElements.size() / (double) this.environment.getNumberOfSamples();
    }

    public Set<Integer> getUncoveredSamples() {

        return IntStream.range(0, this.environment.getNumberOfSamples())
                .filter(sampleIndex -> !this.samplesCovered[sampleIndex])
                .boxed()
                .collect(Collectors.toSet());
    }

    public double getSolutionCost(SetCoveringEnvironment environment) {
        if (!isSolutionReady(environment)) {
            throw new RuntimeException("Cannot calculate cost of an incomplete solution");
        }

        return this.getSolutionCost(environment, this.getSolution());
    }

    @Override
    public double getSolutionCost(SetCoveringEnvironment environment, List<Integer> solution) {
        return solution.size();
    }

    public boolean isSolutionReady(SetCoveringEnvironment environment) {
        Set<Integer> uncoveredSamples = this.getUncoveredSamples();
        return uncoveredSamples.size() == 0;
    }


    public List<Integer> getNeighbourhood(SetCoveringEnvironment environment) {
        Set<Integer> uncoveredSamples = getUncoveredSamples();
        Optional<Integer> selectedSample = uncoveredSamples.stream()
                .skip((int) (uncoveredSamples.size() * Math.random()))
                .findFirst();

        if (selectedSample.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> coveringCandidates = environment.getCoveringCandidates(selectedSample.get());

        return coveringCandidates.stream()
                .filter(candidateIndex -> !this.isNodeVisited(candidateIndex) && !environment.isDominatedCandidate(candidateIndex))
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
