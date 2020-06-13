package isula.aco.setcov;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static setcov.isula.sample.AcoSetCoveringWithIsula.PREPROCESING_TIME_LIMIT;
import static setcov.isula.sample.FileUtils.isValidSolution;

class ApplyLocalSearchTest {

    @Test
    void testApplyPolicy() {

        SetCoveringPreProcessor preProcessor = new SetCoveringPreProcessor(PREPROCESING_TIME_LIMIT);
        preProcessor.setNumberOfSamples(4);
        preProcessor.setNumberOfCandidates(5);
        preProcessor.addCandidatesForSample(0, new String[]{"1", "4"});
        preProcessor.addCandidatesForSample(1, new String[]{"0", "2", "4"});
        preProcessor.addCandidatesForSample(2, new String[]{"2", "3"});
        preProcessor.addCandidatesForSample(3, new String[]{"0", "3"});


        SetCoveringEnvironment smallEnvironment = new SetCoveringEnvironment(preProcessor);
        AntForSetCovering ant = new AntForSetCovering(smallEnvironment);
        IntStream.range(0, smallEnvironment.getNumberOfCandidates())
                .filter((index) -> index != 1)
                .forEach((candidateIndex) -> ant.visitNode(candidateIndex, smallEnvironment));
        double originalCost = ant.getSolutionCost(smallEnvironment);

        ApplyLocalSearch localSearchPolicy = new ApplyLocalSearch();
        localSearchPolicy.setAnt(ant);
        localSearchPolicy.applyPolicy(smallEnvironment, null);

        List<Integer> improvedSolution = ant.getSolution();
        assertTrue(isValidSolution(improvedSolution, preProcessor.getSamplesPerCandidate(),
                preProcessor.getNumberOfSamples()));
        assertTrue(ant.getSolutionCost(smallEnvironment) < originalCost);
        assertTrue(isValidSolution(improvedSolution, preProcessor.getSamplesPerCandidate(),
                preProcessor.getNumberOfSamples()));

    }
}