package setcov.isula.sample;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class AcoSetCoveringWithIsulaTest {

    @Test
    public void testReadingSmallProblemFile() throws IOException {
        String fileName = "AC_10_cover.txt";
        double[][] problemRepresentation = AcoSetCoveringWithIsula.getRepresentationFromFile(fileName);

        int numberOfSamples = 605;
        int numberOfCandidates = 2904;
        assertEquals(numberOfSamples, problemRepresentation.length);
        assertEquals(numberOfCandidates, problemRepresentation[0].length);

        int sampleIndex = 6;
        int coveringCandidateIndex = 16;
        int nonCoveringCandidateIndex = 15;
        assertEquals(0, problemRepresentation[sampleIndex][nonCoveringCandidateIndex]);
        assertEquals(1, problemRepresentation[sampleIndex][coveringCandidateIndex]);

    }

}