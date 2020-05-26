package setcov.isula.sample;

import isula.aco.*;
import isula.aco.algorithms.antsystem.OfflinePheromoneUpdate;
import isula.aco.algorithms.antsystem.PerformEvaporation;
import isula.aco.algorithms.antsystem.RandomNodeSelection;
import isula.aco.algorithms.antsystem.StartPheromoneMatrix;
import isula.aco.exception.InvalidInputException;
import isula.aco.setcov.AntForSetCovering;
import isula.aco.setcov.SetCoveringEnvironment;

import javax.naming.ConfigurationException;
import java.io.*;
import java.util.Objects;
import java.util.logging.Logger;

public class AcoSetCoveringWithIsula {

    private static Logger logger = Logger.getLogger(AcoSetCoveringWithIsula.class.getName());

    private static final int UNASSIGNED = -1;
    private static final double COVERED = 1.0;
    private static final String TEAM_NAME = "Isula";


    public static void main(String... args) throws InvalidInputException, ConfigurationException, IOException {
        logger.info("ANT COLONY FOR THE SET COVERING PROBLEM");

        String instanceName = "AC_01";
        String fileName = getInputFile(instanceName);
        double[][] problemRepresentation = getRepresentationFromFile(fileName);
        BaseAntSystemConfiguration configurationProvider = new AlternativeAntSystemConfiguration();

        Integer[] solutionFound = solveProblem(problemRepresentation, configurationProvider);
        writeSolutionToFile(instanceName, configurationProvider.getConfigurationName(), solutionFound);
    }

    private static void writeSolutionToFile(String instanceName, String algorithmName, Integer[] solutionFound) throws FileNotFoundException {
        String outputFile = getOutputFile(instanceName, algorithmName);
        PrintWriter printWriter = new PrintWriter(outputFile);

        int solutionSize = 0;
        StringBuilder solutionAsString = new StringBuilder();
        for (Integer solutionComponent : solutionFound) {
            if (solutionComponent != null) {
                solutionSize += 1;
                solutionAsString.append(solutionComponent).append(" ");
            }
        }

        printWriter.println(solutionSize);
        printWriter.println(solutionAsString.substring(0, solutionAsString.length() - 1));

        printWriter.close();

        logger.info("Solution written to " + outputFile);
    }

    private static String getInputFile(String instanceName) {
        return instanceName + "_cover.txt";
    }

    private static String getOutputFile(String instanceName, String algorithmName) {
        return TEAM_NAME + "_" + algorithmName + "_Track1_" + instanceName + ".txt";
    }

    private static Integer[] solveProblem(double[][] problemRepresentation, BaseAntSystemConfiguration
            configurationProvider) throws InvalidInputException, ConfigurationException {

        AntColony<Integer, SetCoveringEnvironment> antColony = createAntColony(configurationProvider);
        SetCoveringEnvironment environment = new SetCoveringEnvironment(problemRepresentation);

        AcoProblemSolver<Integer, SetCoveringEnvironment> problemSolver = new AcoProblemSolver<>();
        problemSolver.initialize(environment, antColony, configurationProvider);
        problemSolver.addDaemonActions(new StartPheromoneMatrix<>(),
                new PerformEvaporation<>());

        problemSolver.addDaemonActions(getPheromoneUpdatePolicy());
        problemSolver.getAntColony().addAntPolicies(new RandomNodeSelection<>());

        problemSolver.solveProblem();
        Integer[] solutionFound = problemSolver.getBestSolution();
        if (!environment.validateSolution(solutionFound)) {
            throw new RuntimeException("The solution found is not valid :(");
        }

        return solutionFound;

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

    public static double[][] getRepresentationFromFile(String fileName) throws IOException {

        File file = new File(Objects.requireNonNull(AcoSetCoveringWithIsula.class.getClassLoader().getResource(fileName)).getFile());
        int numberOfSamples;
        int numberOfCandidates;

        double[][] problemRepresentation = null;

        int lineCounter = 0;
        int sampleIndex = UNASSIGNED;
        int candidatesForSample = UNASSIGNED;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");

                if (lineCounter == 0) {
                    numberOfSamples = Integer.parseInt(tokens[0]);
                    numberOfCandidates = Integer.parseInt(tokens[1]);
                    problemRepresentation = new double[numberOfSamples][numberOfCandidates];
                } else if (sampleIndex == UNASSIGNED && tokens.length == 1) {
                    sampleIndex = Integer.parseInt(tokens[0]);
                } else if (sampleIndex != UNASSIGNED && candidatesForSample == UNASSIGNED && tokens.length == 1) {
                    candidatesForSample = Integer.parseInt(tokens[0]);
                } else if (sampleIndex != UNASSIGNED && candidatesForSample != UNASSIGNED) {

                    if (tokens.length != candidatesForSample) {
                        throw new RuntimeException("Expecting " + candidatesForSample + " candidates for sample " + sampleIndex
                                + " .Currently in file: " + tokens.length);
                    }

                    for (String currentToken : tokens) {
                        int candidateIndex = Integer.parseInt(currentToken);
                        problemRepresentation[sampleIndex][candidateIndex] = COVERED;
                    }

                    sampleIndex = UNASSIGNED;
                    candidatesForSample = UNASSIGNED;
                }
                lineCounter += 1;
            }
        }

        logger.info("Problem information gathered from: " + fileName);
        return problemRepresentation;
    }
}
