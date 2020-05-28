package setcov.isula.sample;

import isula.aco.*;
import isula.aco.algorithms.antsystem.OfflinePheromoneUpdate;
import isula.aco.algorithms.antsystem.PerformEvaporation;
import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import isula.aco.exception.InvalidInputException;
import isula.aco.setcov.AntForSetCovering;
import isula.aco.setcov.ApplyLocalSearch;
import isula.aco.setcov.SetCoveringEnvironment;
import isula.aco.tuning.AcoParameterTuner;
import isula.aco.tuning.ParameterOptimisationTarget;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static setcov.isula.sample.FileUtils.writeObjectToFile;

public class AcoSetCoveringWithIsula implements ParameterOptimisationTarget {

    private static Logger logger = Logger.getLogger(AcoSetCoveringWithIsula.class.getName());


    private static final int NUMBER_OF_ANTS = 5;
    private static final int NUMBER_OF_ITERATIONS = 10;

    private SetCoveringEnvironment setCoveringEnvironment;

    public AcoSetCoveringWithIsula(SetCoveringEnvironment setCoveringEnvironment) {
        this.setCoveringEnvironment = setCoveringEnvironment;
    }


    public static void main(String... args) throws InvalidInputException, ConfigurationException, IOException {
        logger.info("ANT COLONY FOR THE SET COVERING PROBLEM");


        for (int instanceNumber = 1; instanceNumber < 33; instanceNumber += 1) {
            String instanceName = "AC_" + String.format("%02d", instanceNumber);
            String fileName = FileUtils.getInputFile(instanceName);
            double[][] problemRepresentation = FileUtils.getRepresentationFromFile(fileName);

            SetCoveringEnvironment setCoveringEnvironment = new SetCoveringEnvironment(problemRepresentation);
            AcoSetCoveringWithIsula acoSetCoveringWithIsula = new AcoSetCoveringWithIsula(setCoveringEnvironment);
            BaseAntSystemConfiguration configurationProvider = acoSetCoveringWithIsula.getOptimisedConfiguration(instanceName);

            configurationProvider.setNumberOfAnts(NUMBER_OF_ANTS);
            configurationProvider.setNumberOfIterations(NUMBER_OF_ITERATIONS);

            AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = solveProblem(setCoveringEnvironment, configurationProvider);
            writeObjectToFile(instanceName + "_solver.txt", problemSolver);

            Integer[] solutionFound = problemSolver.getBestSolution();
            FileUtils.writeSolutionToFile(instanceName, configurationProvider.getConfigurationName(), solutionFound);

        }

    }


    private BaseAntSystemConfiguration getOptimisedConfiguration(String instanceName) throws FileNotFoundException {
        List<Integer> numberOfAntsValues = Collections.singletonList(3);
        List<Integer> numberOfIterationValues = Collections.singletonList(5);

        List<Double> evaporationRatioValues = Arrays.asList(.1, .5, .9);
        List<Double> initialPheromoneValues = Arrays.asList(1., 50., 100.);
        List<Double> heuristicImportanceValues = Arrays.asList(0., 2., 5.);
        List<Double> pheromoneImportanceValues = Arrays.asList(0.25, 2., 5.);

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

    private static AcoProblemSolver<Integer, SetCoveringEnvironment> solveProblem(SetCoveringEnvironment environment,
                                                                                  BaseAntSystemConfiguration
                                                                                          configurationProvider) throws ConfigurationException {

        AntColony<Integer, SetCoveringEnvironment> antColony = createAntColony(configurationProvider);

        AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = new AcoProblemSolver<>();
        problemSolver.initialize(environment, antColony, configurationProvider);
        problemSolver.addDaemonActions(new StartPheromoneMatrix<>(),
                new PerformEvaporation<>());

        problemSolver.addDaemonActions(getPheromoneUpdatePolicy());
        problemSolver.getAntColony().addAntPolicies(new RandomNodeSelection<>(), new ApplyLocalSearch());

        problemSolver.solveProblem();
        Integer[] solutionFound = problemSolver.getBestSolution();
        if (!environment.isValidSolution(solutionFound)) {
            throw new RuntimeException("The solution found is not valid :(");
        }

        return problemSolver;

    }


    private static DaemonAction<Integer, SetCoveringEnvironment> getPheromoneUpdatePolicy() {
        return new OfflinePheromoneUpdate<>() {
            @Override
            protected double getPheromoneDeposit(Ant<Integer, SetCoveringEnvironment> ant, Integer positionInSolution,
                                                 Integer solutionComponent, SetCoveringEnvironment environment,
                                                 ConfigurationProvider configurationProvider) {

                BaseAntSystemConfiguration setCoveringParameters = (BaseAntSystemConfiguration) configurationProvider;
                return setCoveringParameters.getBasePheromoneValue() / ant.getSolutionCost(environment);
            }
        };
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
