package isula.aco.setcov;

import isula.aco.exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import setcov.isula.sample.AcoSetCoveringWithIsula;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SetCoveringEnvironmentTest {

    private SetCoveringEnvironment environment;

    public SetCoveringEnvironmentTest() throws IOException, InvalidInputException {
        String fileName = "AC_10_cover.txt";
        double[][] problemRepresentation = AcoSetCoveringWithIsula.getRepresentationFromFile(fileName);
        this.environment = new SetCoveringEnvironment(problemRepresentation);
    }

    @Test
    public void testCreatePheromoneMatrix() {

        double[][] pheromoneMatrix = environment.createPheromoneMatrix();
        int numberOfCandidates = 2904;

        assertEquals(numberOfCandidates, pheromoneMatrix.length);
    }

    @Test
    public void testGetSamplesCovered() {

        int candidateIndex = 16;
        List<Integer> coveredByCandidate = environment.getSamplesCovered(candidateIndex);
        assertTrue(coveredByCandidate.contains(0));
        assertTrue(coveredByCandidate.contains(3));
        assertTrue(coveredByCandidate.contains(4));

        assertFalse(coveredByCandidate.contains(9));

    }
}