package setcov.isula.sample;

import isula.aco.*;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static setcov.isula.sample.FileUtils.*;

public class AcoSetCoveringWithIsula implements ParameterOptimisationTarget {

    private static final Logger logger = Logger.getLogger(AcoSetCoveringWithIsula.class.getName());

    private static final int PARALLEL_RUNS = 3;

    private static final int NUMBER_OF_ITERATIONS = 150;
    private static final int NUMBER_OF_ANTS = 20;
    private static final boolean PERFORM_PARAMETER_TUNNING = true;
    private static final Duration CONSTRUCTION_TIME_LIMIT = Duration.ofHours(1);
    public static final Duration PREPROCESING_TIME_LIMIT = Duration.ofHours(1);

    private final SetCoveringEnvironment setCoveringEnvironment;
    private String currentProcessingFile;

    public AcoSetCoveringWithIsula(SetCoveringEnvironment setCoveringEnvironment) {
        this.setCoveringEnvironment = setCoveringEnvironment;
    }


    public static void main(String... args) throws IOException {
        logger.info("ANT SYSTEM FOR THE SET COVERING PROBLEM");

        String mode = args[0];
        String path = args[1];
        List<String> fileNames = getFilesToProcess(mode, path);

        fileNames.forEach(fileName -> {
            try {
                SetCoveringEnvironment setCoveringEnvironment = getSetCoveringEnvironment(fileName);
                AcoSetCoveringWithIsula acoSetCoveringWithIsula = getCoordinatorInstance(fileName, setCoveringEnvironment);
                processProblemFile(fileName, setCoveringEnvironment, acoSetCoveringWithIsula);
            } catch (Exception e) {
                logger.warning("Error processing: " + fileName);
                e.printStackTrace();
            }
        });
    }


    protected static void processProblemFile(String fileName, SetCoveringEnvironment setCoveringEnvironment,
                                             AcoSetCoveringWithIsula acoSetCoveringWithIsula) throws IOException,
        ConfigurationException {
        String instanceName = getInstanceName(fileName);

        logger.info("Current instance: " + instanceName);


        BaseAntSystemConfiguration configurationProvider;
        if (PERFORM_PARAMETER_TUNNING) {
            configurationProvider = acoSetCoveringWithIsula.getOptimisedConfiguration(instanceName);
        } else {
            configurationProvider = getDefaultAntSystemConfiguration();
        }

        configurationProvider.setNumberOfAnts(NUMBER_OF_ANTS);
        configurationProvider.setNumberOfIterations(NUMBER_OF_ITERATIONS);

        logger.info("Algorithm configuration: " + configurationProvider);

        AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = acoSetCoveringWithIsula.solveProblem(
            setCoveringEnvironment, configurationProvider);
        writeObjectToFile(instanceName + "_solver.txt", problemSolver);

        List<Integer> solutionFound = problemSolver.getBestSolution();
        FileUtils.writeSolutionToFile(instanceName, configurationProvider.getConfigurationName(), solutionFound);
    }

    protected static SetCoveringEnvironment getSetCoveringEnvironment(String fileName) throws IOException {
        SetCoveringPreProcessor dataPreProcessor = FileUtils.initialisePreProcessorFromFile(fileName);
        return new SetCoveringEnvironment(dataPreProcessor,
            requiresDominationAnalysis(fileName));
    }

    protected static AcoSetCoveringWithIsula getCoordinatorInstance(String fileName, SetCoveringEnvironment setCoveringEnvironment) {
        AcoSetCoveringWithIsula acoSetCoveringWithIsula = new AcoSetCoveringWithIsula(setCoveringEnvironment);
        acoSetCoveringWithIsula.setCurrentProcessingFile(fileName);
        return acoSetCoveringWithIsula;
    }

    public static BaseAntSystemConfiguration getDefaultAntSystemConfiguration() {
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
            problemSolver = solveProblem(this.setCoveringEnvironment, baseAntSystemConfiguration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert problemSolver != null;
        return problemSolver.getBestSolutionCost();
    }

    private AcoProblemSolver<Integer, SetCoveringEnvironment> solveProblem(SetCoveringEnvironment environment,
                                                                           ConfigurationProvider configurationProvider)
        throws ConfigurationException {

        ParallelAcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = new ParallelAcoProblemSolver<>();
        problemSolver.initialize(() -> new SetCoveringEnvironment(environment),
            this::createAntColony,
            configurationProvider,
            CONSTRUCTION_TIME_LIMIT, PARALLEL_RUNS);

        configureAntSystem(problemSolver);

        problemSolver.solveProblem();
        List<Integer> solutionFound = problemSolver.getBestSolution();
        logger.fine("Best solution found: " + solutionFound);
        if (!isValidSolution(solutionFound, environment.getSamplesPerCandidate(), environment.getNumberOfSamples())) {
            throw new RuntimeException("The solution found is not valid :(");
        }

        return problemSolver;

    }

    public AntColony<Integer, SetCoveringEnvironment> createAntColony(ConfigurationProvider configurationProvider) {
        return new AntColony<>(configurationProvider.getNumberOfAnts()) {
            @Override
            protected Ant<Integer, SetCoveringEnvironment> createAnt(SetCoveringEnvironment environment) {
                return new AntForSetCovering(environment);
            }
        };
    }


    public void configureAntSystem(ParallelAcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver) {

        problemSolver.addDaemonAction(StartPheromoneMatrix::new);
        problemSolver.addDaemonAction(PerformEvaporation::new);
        problemSolver.addDaemonAction(OfflinePheromoneUpdate::new);

        problemSolver.getAntColonies()
            .forEach((colony) -> colony.addAntPolicies(
                new RandomNodeSelection<>(), new ApplyLocalSearch()));

    }

    public String getCurrentProcessingFile() {
        return currentProcessingFile;
    }

    public void setCurrentProcessingFile(String currentProcessingFile) {
        this.currentProcessingFile = currentProcessingFile;
    }
}
