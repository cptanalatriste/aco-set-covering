package isula.aco.setcov;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplyLocalSearchTest {

    @Test
    void testApplyPolicy()  {

        double[][] testRepresentation = {{0., 1., 0., 0., 1.},
                {1., 0., 1., 0., 1.},
                {0., 0., 1., 1., 0.},
                {1., 0., 0., 1., 0.}};
        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(testRepresentation);
        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);
        IntStream.range(0, smallEnvironment.getNumberOfCandidates())
                .filter((index) -> index != 1)
                .forEach((candidateIndex) -> ant.visitNode(candidateIndex, smallEnvironment));
        double originalCost = ant.getSolutionCost(smallEnvironment);

        ApplyLocalSearch localSearchPolicy = new ApplyLocalSearch();
        localSearchPolicy.setAnt(ant);
        localSearchPolicy.applyPolicy(smallEnvironment, null);

        List<Integer> improvedSolution = ant.getSolution();
        assertTrue(smallEnvironment.isValidSolution(improvedSolution));
        assertTrue(ant.getSolutionCost(smallEnvironment) < originalCost);
        assertTrue(smallEnvironment.isValidSolution(improvedSolution));

    }
}