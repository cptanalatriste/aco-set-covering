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

public class IteratedAntsForSetCovering extends AcoSetCoveringWithIsula {

    private static final Logger logger = Logger.getLogger(IteratedAntsForSetCovering.class.getName());

    public static final double REMOVAL_FACTOR = 0.5;

    public IteratedAntsForSetCovering(SetCoveringEnvironment setCoveringEnvironment) {
        super(setCoveringEnvironment);
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
