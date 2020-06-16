package isula.aco.setcov;

import org.junit.jupiter.api.Test;
import setcov.isula.sample.FileUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static setcov.isula.sample.AcoSetCoveringWithIsula.PREPROCESING_TIME_LIMIT;
import static setcov.isula.sample.FileUtils.isValidSolution;

public class SetCoveringEnvironmentTest {

    private SetCoveringEnvironment environment;

    public SetCoveringEnvironmentTest() throws IOException {
        String fileName = "AC_10_cover.txt";
        String dataDirectory = "/Users/cgavidia/Documents/coverData/well-explored-problems/";
        SetCoveringPreProcessor preProcessor = FileUtils.initialisePreProcessorFromFile(dataDirectory + fileName);
        this.environment = new SetCoveringEnvironment(preProcessor);
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
    public void testFindDominatedCandidates() {

        assertTrue(this.environment.getDominatedCandidates().size() <= this.environment.getNumberOfCandidates());

        SetCoveringPreProcessor preProcessor = new SetCoveringPreProcessor(PREPROCESING_TIME_LIMIT);
        preProcessor.setNumberOfSamples(4);
        preProcessor.setNumberOfCandidates(4);
        preProcessor.addCandidatesForSample(0, new String[]{"0"});
        preProcessor.addCandidatesForSample(1, new String[]{"0", "2", "3"});
        preProcessor.addCandidatesForSample(2, new String[]{"0", "1", "2", "3"});
        preProcessor.addCandidatesForSample(3, new String[]{"1", "3"});
        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(preProcessor);

        Set<Integer> dominatedCandidates = smallEnvironment.getDominatedCandidates();

        assertEquals(2, dominatedCandidates.size());
        assertFalse(smallEnvironment.isDominatedCandidate(0));
        assertTrue(smallEnvironment.isDominatedCandidate(1));
        assertTrue(smallEnvironment.isDominatedCandidate(2));
        assertFalse(smallEnvironment.isDominatedCandidate(3));


    }

    @Test
    public void testGetMandatoryCandidates() {

        SetCoveringPreProcessor preProcessor = new SetCoveringPreProcessor(PREPROCESING_TIME_LIMIT);
        preProcessor.setNumberOfSamples(4);
        preProcessor.setNumberOfCandidates(5);
        preProcessor.addCandidatesForSample(0, new String[]{"1", "4"});
        preProcessor.addCandidatesForSample(1, new String[]{"0", "1", "2"});
        preProcessor.addCandidatesForSample(2, new String[]{"2", "3"});
        preProcessor.addCandidatesForSample(3, new String[]{"0", "3"});

        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(preProcessor);
        Set<Integer> mandatoryCandidates = smallEnvironment.getMandatoryCandidates();

        assertEquals(1, mandatoryCandidates.size());
        assertTrue(mandatoryCandidates.contains(1));

    }

    @Test
    public void testValidateSolution() throws IOException {

        SetCoveringPreProcessor preProcessor = new SetCoveringPreProcessor(PREPROCESING_TIME_LIMIT);
        preProcessor.setNumberOfSamples(4);
        preProcessor.setNumberOfCandidates(4);
        preProcessor.addCandidatesForSample(0, new String[]{"1"});
        preProcessor.addCandidatesForSample(1, new String[]{"0", "2"});
        preProcessor.addCandidatesForSample(2, new String[]{"2", "3"});
        preProcessor.addCandidatesForSample(3, new String[]{"0", "3"});

        List<Integer> invalidSolution = Arrays.asList(3, 1, null, null);
        assertFalse(isValidSolution(invalidSolution, preProcessor.getSamplesPerCandidate(),
                preProcessor.getNumberOfSamples()));

        List<Integer> validSolution = Arrays.asList(0, 1, 2, null);
        assertTrue(isValidSolution(validSolution, preProcessor.getSamplesPerCandidate(),
                preProcessor.getNumberOfSamples()));

        String fileName = "AC_01_cover.txt";
        String dataDirectory = "/Users/cgavidia/Documents/coverData/well-explored-problems/";
        List<Integer> validSolutionForBigEnvironment = Arrays.asList(1114, 1999, 236, 2483, 817, 1366, 423, 49, 1188,
                1007, 1980, 849, 775, 1494, 2069, 2596, 2739, 2466, 2221, 2852, 2583);
        assertTrue(isValidSolution(validSolutionForBigEnvironment, dataDirectory + fileName));

    }
}