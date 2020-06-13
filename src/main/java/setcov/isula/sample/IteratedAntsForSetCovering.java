package setcov.isula.sample;

import isula.aco.Ant;
import isula.aco.AntColony;
import isula.aco.ConfigurationProvider;
import isula.aco.ParallelAcoProblemSolver;
import isula.aco.setcov.AntForSetCovering;
import isula.aco.setcov.ConstructPartialSolutionsForSetCovering;
import isula.aco.setcov.SetCoveringEnvironment;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static setcov.isula.sample.FileUtils.*;

public class IteratedAntsForSetCovering extends AcoSetCoveringWithIsula {

    private static final Logger logger = Logger.getLogger(IteratedAntsForSetCovering.class.getName());

    public static final double REMOVAL_FACTOR = 0.5;

    public IteratedAntsForSetCovering(SetCoveringEnvironment setCoveringEnvironment) {
        super(setCoveringEnvironment);
    }

    public static void main(String... args) throws IOException {
        logger.info("ITERATED ANTS FOR THE SET COVERING PROBLEM");

        String mode = args[0];
        String path = args[1];
        List<String> fileNames = getFilesToProcess(mode, path);

        fileNames.forEach(fileName -> {
            try {
                SetCoveringEnvironment setCoveringEnvironment = getSetCoveringEnvironment(fileName);
                IteratedAntsForSetCovering acoSetCoveringWithIsula = getCoordinatorInstance(fileName, setCoveringEnvironment);
                processProblemFile(fileName, setCoveringEnvironment, acoSetCoveringWithIsula);
            } catch (Exception e) {
                logger.warning("Error processing: " + fileName);
                e.printStackTrace();
            }
        });
    }


    protected static IteratedAntsForSetCovering getCoordinatorInstance(String fileName,
                                                                       SetCoveringEnvironment setCoveringEnvironment) {
        IteratedAntsForSetCovering acoSetCoveringWithIsula = new IteratedAntsForSetCovering(setCoveringEnvironment);
        acoSetCoveringWithIsula.setCurrentProcessingFile(fileName);
        return acoSetCoveringWithIsula;
    }

    @Override
    public AntColony<Integer, SetCoveringEnvironment> createAntColony(ConfigurationProvider configurationProvider) {
        return new AntColony<>(configurationProvider.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, SetCoveringEnvironment> createAnt(SetCoveringEnvironment environment) {

                Set<Integer> partialSolution = getPartialSolutionFromFile();
                return new AntForSetCovering(environment, partialSolution);
            }
        };
    }

    private Set<Integer> getPartialSolutionFromFile() {
        logger.info("Loading partial solution for: " + this.getCurrentProcessingFile());
        Set<Integer> partialSolution = new HashSet<>();
        try {
            List<Integer> solutionFile = FileUtils.getStoredSolution(this.getCurrentProcessingFile());
            partialSolution.addAll(solutionFile.subList(0, (int) (solutionFile.size() * REMOVAL_FACTOR)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return partialSolution;
    }

    @Override
    public void configureAntSystem(ParallelAcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver) {

        super.configureAntSystem(problemSolver);

        logger.info("Adding a daemon action for partial solution generation.");
        problemSolver.getAntColonies()
                .forEach((colony) -> colony.addAntPolicies(new ConstructPartialSolutionsForSetCovering()));


    }
}
