package setcov.isula.sample;

import isula.aco.AcoProblemSolver;
import isula.aco.Ant;
import isula.aco.AntColony;
import isula.aco.ConfigurationProvider;
import isula.aco.algorithms.antsystem.OfflinePheromoneUpdate;
import isula.aco.algorithms.antsystem.PerformEvaporation;
import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import isula.aco.setcov.AntForSetCovering;
import isula.aco.setcov.ApplyLocalSearch;
import isula.aco.setcov.SetCoveringEnvironment;
import isula.aco.setcov.SetCoveringPreProcessor;
import isula.aco.tuning.AcoParameterTuner;
import isula.aco.tuning.ParameterOptimisationTarget;

import javax.naming.ConfigurationException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static setcov.isula.sample.FileUtils.*;

public class AcoSetCoveringWithIsula implements ParameterOptimisationTarget {

    private static final Logger logger = Logger.getLogger(AcoSetCoveringWithIsula.class.getName());


    private static final int NUMBER_OF_ANTS = 5;
    private static final int NUMBER_OF_ITERATIONS = 10;
    private static final Duration TIME_LIMIT = Duration.ofHours(1);
    private static final List<String> processedFiles = Arrays.asList("AC_01",
            "AC_10", "AC_11", "AC_02");

    private final SetCoveringEnvironment setCoveringEnvironment;
    private String currentProcessingFile;

    public AcoSetCoveringWithIsula(SetCoveringEnvironment setCoveringEnvironment) {
        this.setCoveringEnvironment = setCoveringEnvironment;
    }


    public static void main(String... args) throws ConfigurationException, IOException {
        logger.info("ANT COLONY FOR THE SET COVERING PROBLEM");

        List<String> fileNames = Files.list(Paths.get(DATA_DIRECTORY))
                .filter(Files::isRegularFile)
                .sorted(Comparator.comparing(p -> p.toFile().length(), Comparator.naturalOrder()))
                .map(Object::toString)
                .collect(Collectors.toList());

        for (String fileName : fileNames) {

            String instanceName = fileName.substring(fileName.length() - 15);
            instanceName = instanceName.substring(0, 5);

            if (processedFiles.contains(instanceName)) {
                logger.info("Skipping file " + fileName);
                continue;
            }

            logger.info("Current instance: " + instanceName);

            SetCoveringPreProcessor dataPreProcessor = FileUtils.initialisePreProcessorFromFile(fileName);

            SetCoveringEnvironment setCoveringEnvironment = new SetCoveringEnvironment(dataPreProcessor);
            AcoSetCoveringWithIsula acoSetCoveringWithIsula = new AcoSetCoveringWithIsula(setCoveringEnvironment);
            acoSetCoveringWithIsula.currentProcessingFile = fileName;

            BaseAntSystemConfiguration configurationProvider = getDefaultAntSystemConfiguration();
            configurationProvider.setNumberOfAnts(NUMBER_OF_ANTS);
            configurationProvider.setNumberOfIterations(NUMBER_OF_ITERATIONS);

            AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = solveProblem(setCoveringEnvironment,
                    configurationProvider, fileName);
            writeObjectToFile(instanceName + "_solver.txt", problemSolver);

            List<Integer> solutionFound = problemSolver.getBestSolution();
            FileUtils.writeSolutionToFile(instanceName, configurationProvider.getConfigurationName(), solutionFound);

        }
    }

    private static BaseAntSystemConfiguration getDefaultAntSystemConfiguration() {
        BaseAntSystemConfiguration configurationProvider = new BaseAntSystemConfiguration();
        configurationProvider.setInitialPheromoneValue(1.0);
        configurationProvider.setPheromoneImportance(1.0);
        configurationProvider.setEvaporationRatio(0.8);
        configurationProvider.setHeuristicImportance(5.0);
        configurationProvider.setPheromoneDepositFactor(1.0);

        return configurationProvider;
    }


    private BaseAntSystemConfiguration getOptimisedConfiguration(String instanceName) throws FileNotFoundException {
        List<Integer> numberOfAntsValues = Collections.singletonList(3);
        List<Integer> numberOfIterationValues = Collections.singletonList(3);
        List<Double> initialPheromoneValues = Collections.singletonList(1.0);
        List<Double> pheromoneImportanceValues = Collections.singletonList(1.0);

        List<Double> evaporationRatioValues = Arrays.asList(.1, .5, .9);
        List<Double> heuristicImportanceValues = Arrays.asList(1., 3., 5.);

        AcoParameterTuner parameterTuner = new AcoParameterTuner(numberOfAntsValues, evaporationRatioValues,
                numberOfIterationValues, initialPheromoneValues, heuristicImportanceValues, pheromoneImportanceValues);

        logger.info("Starting parameter tuning");
        ConfigurationProvider configurationProvider = parameterTuner.getOptimalConfiguration(this);
        writeObjectToFile(instanceName + "_tuning.txt", parameterTuner);

        return new BaseAntSystemConfiguration(configurationProvider);
    }

    @Override
    public double getSolutionCost(ConfigurationProvider configurationProvider) {
        BaseAntSystemConfiguration baseAntSystemConfiguration = new BaseAntSystemConfiguration(configurationProvider);
        AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = null;
        try {
            problemSolver = solveProblem(this.setCoveringEnvironment, baseAntSystemConfiguration, currentProcessingFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert problemSolver != null;
        return problemSolver.getBestSolutionCost();
    }

    private static AcoProblemSolver<Integer, SetCoveringEnvironment> solveProblem(SetCoveringEnvironment environment,
                                                                                  BaseAntSystemConfiguration
                                                                                          configurationProvider,
                                                                                  String fileName) throws ConfigurationException, IOException {

        AntColony<Integer, SetCoveringEnvironment> antColony = createAntColony(configurationProvider);

        AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = new AcoProblemSolver<>();
        problemSolver.initialize(environment, antColony, configurationProvider, TIME_LIMIT);
        configureAntSystem(problemSolver);

        problemSolver.solveProblem();
        List<Integer> solutionFound = problemSolver.getBestSolution();
        if (!isValidSolution(solutionFound, fileName)) {
            throw new RuntimeException("The solution found is not valid :(");
        }

        return problemSolver;

    }

    private static void configureAntSystem(AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver) {

        problemSolver.addDaemonActions(new StartPheromoneMatrix<>(),
                new PerformEvaporation<>());
        problemSolver.addDaemonActions(new OfflinePheromoneUpdate<>());
        problemSolver.getAntColony().addAntPolicies(new RandomNodeSelection<>(), new ApplyLocalSearch());
    }

    private static AntColony<Integer, SetCoveringEnvironment> createAntColony(BaseAntSystemConfiguration configurationProvide) {
        return new AntColony<>(configurationProvide.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, SetCoveringEnvironment> createAnt(SetCoveringEnvironment environment) {
                return new AntForSetCovering(environment);
            }
        };
    }


}
