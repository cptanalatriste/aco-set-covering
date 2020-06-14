package setcov.isula.sample;

import isula.aco.setcov.SetCoveringPreProcessor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AcoSetCoveringWithIsulaTest {

    @Test
    public void testReadingSmallProblemFile() throws IOException {
        String fileName = "AC_10_cover.txt";
        String dataDirectory = "/Users/cgavidia/Documents/coverData/normalProblems/";

        SetCoveringPreProcessor preProcessor = FileUtils.initialisePreProcessorFromFile(
                dataDirectory + fileName);

        int numberOfSamples = 605;
        int numberOfCandidates = 2904;
        assertEquals(numberOfSamples, preProcessor.getNumberOfSamples());
        assertEquals(numberOfCandidates, preProcessor.getNumberOfCandidates());

        int sampleIndex = 6;
        int coveringCandidateIndex = 16;
        int nonCoveringCandidateIndex = 15;

        Set<Integer> candidatesForSample = preProcessor.getCandidatesPerSample().get(sampleIndex);


        assertFalse(candidatesForSample.contains(nonCoveringCandidateIndex));
        assertTrue(candidatesForSample.contains(coveringCandidateIndex));

    }

}