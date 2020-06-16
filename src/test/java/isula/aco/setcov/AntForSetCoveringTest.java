package isula.aco.setcov;

import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import setcov.isula.sample.AcoSetCoveringWithIsula;
import setcov.isula.sample.BaseAntSystemConfiguration;
import setcov.isula.sample.FileUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static setcov.isula.sample.AcoSetCoveringWithIsula.PREPROCESING_TIME_LIMIT;

public class AntForSetCoveringTest {

    private final SetCoveringEnvironment environment;
    private final SetCoveringEnvironment smallEnvironment;
    private BaseAntSystemConfiguration algorithmConfiguration;


    private final SetCoveringPreProcessor preProcessor;

    public AntForSetCoveringTest() throws IOException {
        String fileName = "AC_10_cover.txt";
        String dataDirectory = "/Users/cgavidia/Documents/coverData/well-explored-problems/";

        this.preProcessor = FileUtils.initialisePreProcessorFromFile(
                dataDirectory + fileName);
        this.environment = new SetCoveringEnvironment(preProcessor);
        this.algorithmConfiguration = AcoSetCoveringWithIsula.getDefaultAntSystemConfiguration();


        SetCoveringPreProcessor preProcessor = new SetCoveringPreProcessor(PREPROCESING_TIME_LIMIT);
        preProcessor.setNumberOfSamples(4);
        preProcessor.setNumberOfCandidates(4);
        preProcessor.addCandidatesForSample(0, new String[]{"1"});
        preProcessor.addCandidatesForSample(1, new String[]{"0", "2"});
        preProcessor.addCandidatesForSample(2, new String[]{"2", "3"});
        preProcessor.addCandidatesForSample(3, new String[]{"0", "3"});

        smallEnvironment = new SetCoveringEnvironment(preProcessor);
    }

    @Test
    public void testGetHeuristicValue() {
        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);
        double expectedHeuristicValueForC1 = 1.0 / 4.0;
        double expectedHeuristicValueForC3 = 2.0 / 4.0;

        assertEquals(expectedHeuristicValueForC1, ant.getHeuristicValue(1, 0,
                smallEnvironment), 0.001);

        assertEquals(expectedHeuristicValueForC3, ant.getHeuristicValue(3, 0,
                smallEnvironment), 0.001);
    }

    @Test
    public void testNodeSelection() {
        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);

        StartPheromoneMatrix<Integer, SetCoveringEnvironment> startPheromoneAction = new StartPheromoneMatrix<>();
        startPheromoneAction.setEnvironment(smallEnvironment);
        startPheromoneAction.applyDaemonAction(algorithmConfiguration);

        RandomNodeSelection<Integer, SetCoveringEnvironment> nodeSelectionPolicy = new RandomNodeSelection<>();
        nodeSelectionPolicy.setAnt(ant);

        List<Integer> sampleNeighbourhood = Arrays.asList(1, 3);
        Map<Integer, Double> probabilities = nodeSelectionPolicy.getProbabilitiesForNeighbourhood(smallEnvironment,
                algorithmConfiguration, sampleNeighbourhood);
        assertTrue(probabilities.get(1) < probabilities.get(3));
        double delta = 0.001;
        assertEquals(probabilities.get(1) + probabilities.get(3), 1.0, delta);

        double pheromoneComponent = Math.pow(algorithmConfiguration.getInitialPheromoneValue(),
                algorithmConfiguration.getPheromoneImportance());
        double expectedProbabilityForC1 = Math.pow(0.25, algorithmConfiguration.getHeuristicImportance()) *
                pheromoneComponent;
        double expectedProbabilityForC3 = Math.pow(0.5, algorithmConfiguration.getHeuristicImportance()) *
                pheromoneComponent;
        double denominator = expectedProbabilityForC1 + expectedProbabilityForC3;
        expectedProbabilityForC1 = expectedProbabilityForC1 / denominator;
        expectedProbabilityForC3 = expectedProbabilityForC3 / denominator;

        assertEquals(expectedProbabilityForC1, probabilities.get(1), delta);
        assertEquals(expectedProbabilityForC3, probabilities.get(3), delta);

        Integer nextComponent = nodeSelectionPolicy.getNextComponent(probabilities);
        assertTrue(nextComponent == 1 || nextComponent == 3);

    }

    @Test
    public void testVisitNode() {

        AntForSetCovering ant = new AntForSetCovering(environment);

        int firstSampleCovered = 0;
        int secondSampleCovered = 1;

        assertFalse(ant.isSampleCovered(firstSampleCovered));
        assertFalse(ant.isSampleCovered(secondSampleCovered));

        int candidateToVisit = 22;
        ant.visitNode(candidateToVisit, this.environment);
        assertTrue(ant.isSampleCovered(firstSampleCovered));
        assertTrue(ant.isSampleCovered(secondSampleCovered));

    }

    @Test
    public void testGetSolutionCost() {
        AntForSetCovering ant = new AntForSetCovering(environment);

        Assertions.assertThrows(RuntimeException.class, () -> ant.getSolutionCost(environment));

        int bound = environment.getNumberOfCandidates();
        for (int candidateIndex = 0; candidateIndex < bound; candidateIndex++) {
            if (!environment.getDominatedCandidates().contains(candidateIndex)) {
                ant.visitNode(candidateIndex, this.environment);
            }
        }

        double expectedCost = environment.getNumberOfCandidates() - environment.getDominatedCandidates().size();
        assertEquals(expectedCost, ant.getSolutionCost(environment));


    }

    @Test
    public void testGetNeighbourhood() {
        AntForSetCovering ant = new AntForSetCovering(environment);

        int bound = environment.getNumberOfCandidates();

        for (int candidateIndex = 0; candidateIndex < bound; candidateIndex++) {
            if (!environment.getDominatedCandidates().contains(candidateIndex)) {
                ant.visitNode(candidateIndex, this.environment);

                List<Integer> neighbourhood = ant.getNeighbourhood(environment);
                assertFalse(neighbourhood.contains(candidateIndex));
            }
        }

        assertEquals(0, ant.getNeighbourhood(environment).size());
        assertTrue(FileUtils.isValidSolution(ant.getSolution(), preProcessor.getSamplesPerCandidate(),
                preProcessor.getNumberOfSamples()));


    }

    @Test
    public void testSetPheromoneTrailValue() {

        AntForSetCovering ant = new AntForSetCovering(environment);
        double initialPheromoneValue = 0;
        int componentIndex = 16;

        assertEquals(initialPheromoneValue, ant.getPheromoneTrailValue(componentIndex, 0,
                environment).intValue());

        double pheromoneDeposit = 4.0;
        ant.setPheromoneTrailValue(componentIndex, 0, environment, pheromoneDeposit);

        assertEquals(pheromoneDeposit, ant.getPheromoneTrailValue(componentIndex, 0,
                environment));


    }


    @Test
    public void testClear() {

        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);
        ant.clear();

        assertEquals(1, ant.getSolution().get(0));
        assertEquals(1, ant.getCurrentIndex());

        Set<Integer> neighbourhood = Set.of(0, 3);
        assertEquals(neighbourhood, ant.getNeighbourhoodForSample(smallEnvironment, 3));

        BaseAntSystemConfiguration algorithmConfiguration = AcoSetCoveringWithIsula.getDefaultAntSystemConfiguration();
        StartPheromoneMatrix<Integer, SetCoveringEnvironment> startPheromoneAction = new StartPheromoneMatrix<>();
        startPheromoneAction.setEnvironment(smallEnvironment);
        startPheromoneAction.applyDaemonAction(algorithmConfiguration);

        RandomNodeSelection<Integer, SetCoveringEnvironment> nodeSelectionPolicy = new RandomNodeSelection<>();
        nodeSelectionPolicy.setAnt(ant);

        nodeSelectionPolicy.applyPolicy(smallEnvironment, algorithmConfiguration);
    }
}