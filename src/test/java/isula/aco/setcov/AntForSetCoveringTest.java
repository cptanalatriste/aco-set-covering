package isula.aco.setcov;

import isula.aco.exception.InvalidInputException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import setcov.isula.sample.AcoSetCoveringWithIsula;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class AntForSetCoveringTest {

    private final SetCoveringEnvironment environment;

    public AntForSetCoveringTest() throws IOException, InvalidInputException {
        String fileName = "AC_10_cover.txt";
        double[][] problemRepresentation = AcoSetCoveringWithIsula.getRepresentationFromFile(fileName);
        this.environment = new SetCoveringEnvironment(problemRepresentation);
    }

    @Test
    public void testVisitNode() {

        AntForSetCovering ant = new AntForSetCovering(environment);

        int firstSampleCovered = 0;
        int secondSampleCovered = 1;

        assertFalse(ant.isSampleCovered(firstSampleCovered));
        assertFalse(ant.isSampleCovered(secondSampleCovered));

        int candidateToVisit = 16;
        ant.visitNode(candidateToVisit);
        assertTrue(ant.isSampleCovered(firstSampleCovered));
        assertTrue(ant.isSampleCovered(secondSampleCovered));

    }

    @Test
    public void testGetSolutionCost() {
        AntForSetCovering ant = new AntForSetCovering(environment);

        Assertions.assertThrows(RuntimeException.class, () -> ant.getSolutionCost(environment));

        IntStream.range(0, environment.getNumberOfCandidates()).forEachOrdered(ant::visitNode);

        assertEquals(environment.getNumberOfCandidates(), ant.getSolutionCost(environment));

    }

    @Test
    public void testGetNeighbourhood() {
        AntForSetCovering ant = new AntForSetCovering(environment);
        List<Integer> initialNeighbourHood = ant.getNeighbourhood(environment);

        assertEquals(environment.getNumberOfCandidates(), initialNeighbourHood.size());

        IntStream.range(0, environment.getNumberOfCandidates()).forEachOrdered(ant::visitNode);

        assertEquals(0, ant.getNeighbourhood(environment).size());
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
}