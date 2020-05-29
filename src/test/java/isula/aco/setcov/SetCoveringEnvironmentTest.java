package isula.aco.setcov;

import isula.aco.exception.InvalidInputException;
import org.junit.jupiter.api.Test;
import setcov.isula.sample.FileUtils;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class SetCoveringEnvironmentTest {

    private SetCoveringEnvironment environment;

    public SetCoveringEnvironmentTest() throws IOException, InvalidInputException {
        String fileName = "AC_10_cover.txt";
        double[][] problemRepresentation = FileUtils.getRepresentationFromFile(fileName);
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

        int candidateIndex = 22;
        Set<Integer> coveredByCandidate = environment.getSamplesForNonDominatedCandidate(candidateIndex);
        assertTrue(coveredByCandidate.contains(2));
        assertTrue(coveredByCandidate.contains(3));
        assertTrue(coveredByCandidate.contains(4));

        assertFalse(coveredByCandidate.contains(8));

    }

    @Test
    void testFindDominatedCandidates() throws InvalidInputException {

        assertTrue(this.environment.getDominatedCandidates().size() <= this.environment.getNumberOfCandidates());


        double[][] testRepresentation = {{1., 0., 0., 0.},
                {1., 0., 1., 1.},
                {1., 1., 1., 1.},
                {0., 1., 0., 1.}};
        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(testRepresentation);

        Set<Integer> dominatedCandidates = smallEnvironment.getDominatedCandidates();

        assertEquals(2, dominatedCandidates.size());
        assertFalse(smallEnvironment.isDominatedCandidate(0));
        assertTrue(smallEnvironment.isDominatedCandidate(1));
        assertTrue(smallEnvironment.isDominatedCandidate(2));
        assertFalse(smallEnvironment.isDominatedCandidate(3));


    }

    @Test
    public void getMandatoryCandidates() throws InvalidInputException {

        double[][] testRepresentation = {{0., 1., 0., 0.},
                {1., 0., 1., 0.},
                {0., 0., 1., 1.},
                {1., 0., 0., 1.}};
        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(testRepresentation);
        Set<Integer> mandatoryCandidates = smallEnvironment.getMandatoryCandidates();

        assertEquals(1, mandatoryCandidates.size());
        assertTrue(mandatoryCandidates.contains(1));

    }

    @Test
    void testValidateSolution() throws InvalidInputException, IOException {
        double[][] testRepresentation = {{0., 1., 0., 0.},
                {1., 0., 1., 0.},
                {0., 0., 1., 1.},
                {1., 0., 0., 1.}};
        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(testRepresentation);

        Integer[] invalidSolution = {3, 1, null, null};
        assertFalse(smallEnvironment.isValidSolution(invalidSolution));

        Integer[] validSolution = {0, 1, 2, null};
        assertTrue(smallEnvironment.isValidSolution(validSolution));

        String fileName = "AC_01_cover.txt";
        double[][] problemRepresentation = FileUtils.getRepresentationFromFile(fileName);
        SetCoveringEnvironment bigEnvironment = new SetCoveringEnvironment(problemRepresentation);
        Integer[] validSolutionForBigEnvironment = {1114, 1999, 236, 2483, 817, 1366, 423, 49, 1188, 1007, 1980, 849, 775, 1494, 2069, 2596, 2739, 2466, 2221, 2852, 2583};
        assertTrue(bigEnvironment.isValidSolution(validSolutionForBigEnvironment));

    }
}