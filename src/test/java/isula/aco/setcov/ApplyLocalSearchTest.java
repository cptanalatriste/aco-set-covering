package isula.aco.setcov;

import isula.aco.exception.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class ApplyLocalSearchTest {

    @Test
    void testApplyPolicy() throws InvalidInputException {

        double[][] testRepresentation = {{0., 1., 0., 0., 1.},
                {1., 0., 1., 0., 1.},
                {0., 0., 1., 1., 0.},
                {1., 0., 0., 1., 0.}};
        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(testRepresentation);
        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);
        IntStream.range(0, smallEnvironment.getNumberOfCandidates())
                .filter((index) -> index != 1)
                .forEach(ant::visitNode);
        double originalCost = ant.getSolutionCost(smallEnvironment);

        ApplyLocalSearch localSearchPolicy = new ApplyLocalSearch();
        localSearchPolicy.setAnt(ant);
        localSearchPolicy.applyPolicy(smallEnvironment, null);

        Integer[] improvedSolution = ant.getSolution();
        assertTrue(smallEnvironment.isValidSolution(improvedSolution));
        assertTrue(ant.getSolutionCost(smallEnvironment) < originalCost);
        assertTrue(smallEnvironment.isValidSolution(improvedSolution));

    }
}