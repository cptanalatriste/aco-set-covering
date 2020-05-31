package isula.aco.setcov;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import setcov.isula.sample.FileUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AntForSetCoveringTest {

    private final SetCoveringEnvironment environment;
    private final SetCoveringPreProcessor preProcessor;

    public AntForSetCoveringTest() throws IOException {
        String fileName = "AC_10_cover.txt";
        preProcessor = FileUtils.initialisePreProcessorFromFile(
                FileUtils.DATA_DIRECTORY + fileName);
        this.environment = new SetCoveringEnvironment(preProcessor);
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
        assertTrue(FileUtils.isValidSolution(ant.getSolution(), preProcessor));
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
    void testClear() {

        SetCoveringPreProcessor preProcessor = new SetCoveringPreProcessor();
        preProcessor.setNumberOfSamples(4);
        preProcessor.setNumberOfCandidates(4);
        preProcessor.addCandidatesForSample(0, new String[]{"1"});
        preProcessor.addCandidatesForSample(1, new String[]{"0", "2"});
        preProcessor.addCandidatesForSample(2, new String[]{"2", "3"});
        preProcessor.addCandidatesForSample(3, new String[]{"0", "3"});

        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(preProcessor);
        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);
        ant.clear();

        assertEquals(1, ant.getSolution().get(0));
        assertEquals(1, ant.getCurrentIndex());

        List<Integer> neighbourhood = Arrays.asList(0, 2, 3);
        assertEquals(neighbourhood, ant.getNeighbourhood(smallEnvironment));
    }
}