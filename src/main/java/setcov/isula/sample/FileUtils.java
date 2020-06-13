package setcov.isula.sample;

import isula.aco.setcov.SetCoveringPreProcessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static setcov.isula.sample.AcoSetCoveringWithIsula.PREPROCESING_TIME_LIMIT;

public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());

    private static final int UNASSIGNED = -1;
    private static final String TEAM_NAME = "Isula";
    private static final String ACADEMIC_PREFIX = "AC";
    private static final String SOLUTION_DIRECTORY = "/Users/cgavidia/Documents/GitHub/gecco2020-ocp-competition/solutions/";
    private static final String DIRECTORY_MODE = "-d";
    private static final String FILE_MODE = "-f";


    public static List<String> getFilesToProcess(String mode, String path) throws IOException {
        if (DIRECTORY_MODE.equals(mode)) {
            return Files.list(Paths.get(path))
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(p -> p.toFile().length(), Comparator.naturalOrder()))
                    .map(Object::toString)
                    .filter(FileUtils::shouldProcessFile)
                    .collect(Collectors.toList());
        } else if (FILE_MODE.equals(mode)) {
            return Collections.singletonList(path);
        }

        return Collections.emptyList();
    }


    public static boolean shouldProcessFile(String fileName) {
        return true;
    }

    static void writeSolutionToFile(String instanceName, String algorithmName, List<Integer> solutionFound) throws FileNotFoundException {
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

        logger.fine("Solution size " + solutionSize + " Solution: " + solutionAsString);

        printWriter.println(solutionSize);
        printWriter.println(solutionAsString.substring(0, solutionAsString.length() - 1));

        printWriter.close();

        logger.info("Solution written to " + outputFile);
    }

    static void writeObjectToFile(String fileName, Object anObject) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(fileName);
        printWriter.println(anObject.toString());
        printWriter.close();
        logger.info("Object written to: " + fileName);

    }

    public static String getInstanceName(String fileName) {
        String instanceName = fileName.substring(fileName.length() - 15);
        return instanceName.substring(0, 5);
    }

    public static boolean requiresDominationAnalysis(String fileName) {
        return getInstanceName(fileName).contains(ACADEMIC_PREFIX);
    }

    private static String getOutputFile(String instanceName, String algorithmName) {
        return TEAM_NAME + "_" + algorithmName + "_Track1_" + instanceName + ".txt";
    }

    public static boolean isValidSolution(List<Integer> solutionFound, String fileName) throws IOException {
        SetCoveringPreProcessor preProcessor = initialisePreProcessorFromFile(fileName);
        return isValidSolution(solutionFound, preProcessor.getSamplesPerCandidate(), preProcessor.getNumberOfSamples());
    }


    public static boolean isValidSolution(List<Integer> solutionFound,
                                          Map<Integer, Set<Integer>> samplesPerCandidate,
                                          int numberOfSamples) {
        logger.info("Validating generated solution.");

        boolean[] samplesCovered = new boolean[numberOfSamples];
        int pendingSamples = numberOfSamples;

        for (Integer candidateIndex : solutionFound) {
            if (candidateIndex != null) {

                for (Integer sampleIndex : samplesPerCandidate.get(candidateIndex)) {
                    if (!samplesCovered[sampleIndex]) {
                        samplesCovered[sampleIndex] = true;
                        pendingSamples -= 1;
                    }
                }
            }
        }
        if (pendingSamples > 0) {
            List<Integer> uncoveredSamples = IntStream.range(0, numberOfSamples)
                    .filter((candidateIndex) -> !samplesCovered[candidateIndex])
                    .boxed()
                    .collect(Collectors.toList());
            logger.warning("Solution does not cover " + pendingSamples + " samples");
            logger.warning("Pending samples " + uncoveredSamples);
        }

        return pendingSamples == 0;
    }

    public static List<Integer> readSolutionFromFile(String fileName) throws IOException {

        int numberOfCandidates = 0;
        int lineCounter = 0;
        List<Integer> storedSolution = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");

                if (lineCounter == 0) {
                    numberOfCandidates = Integer.parseInt(tokens[0]);

                } else {

                    if (tokens.length != numberOfCandidates) {
                        throw new RuntimeException("Expecting " + numberOfCandidates + " candidates for solution " + fileName
                                + " .Currently in file: " + tokens.length);
                    }

                    storedSolution = Arrays.stream(tokens).map(Integer::parseInt).collect(Collectors.toList());
                }
                lineCounter += 1;
            }
        }

        return storedSolution;
    }

    public static SetCoveringPreProcessor initialisePreProcessorFromFile(String fileName) throws IOException {

        int numberOfSamples;
        int numberOfCandidates;

        SetCoveringPreProcessor dataPreProcessor = new SetCoveringPreProcessor(PREPROCESING_TIME_LIMIT);

        int lineCounter = 0;
        int sampleIndex = UNASSIGNED;
        int candidatesForSample = UNASSIGNED;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");

                if (lineCounter == 0) {
                    numberOfSamples = Integer.parseInt(tokens[0]);
                    numberOfCandidates = Integer.parseInt(tokens[1]);

                    dataPreProcessor.setNumberOfCandidates(numberOfCandidates);
                    dataPreProcessor.setNumberOfSamples(numberOfSamples);

                } else if (sampleIndex == UNASSIGNED && tokens.length == 1) {
                    sampleIndex = Integer.parseInt(tokens[0]);
                } else if (sampleIndex != UNASSIGNED && candidatesForSample == UNASSIGNED && tokens.length == 1) {
                    candidatesForSample = Integer.parseInt(tokens[0]);
                } else if (sampleIndex != UNASSIGNED && candidatesForSample != UNASSIGNED) {

                    if (tokens.length != candidatesForSample) {
                        throw new RuntimeException("Expecting " + candidatesForSample + " candidates for sample " + sampleIndex
                                + " .Currently in file: " + tokens.length);
                    }

                    dataPreProcessor.addCandidatesForSample(sampleIndex, tokens);

                    sampleIndex = UNASSIGNED;
                    candidatesForSample = UNASSIGNED;
                }
                lineCounter += 1;
            }
        }

        logger.info("Problem information gathered from: " + fileName);
        return dataPreProcessor;
    }

    public static List<Integer> getStoredSolution(String inputFileName) throws IOException {
        String problemInstance = getInstanceName(inputFileName);
        String solutionFile = SOLUTION_DIRECTORY + "Isula_AntSystemConfiguration_Track1_" + problemInstance + ".txt";

        List<Integer> storedSolution = readSolutionFromFile(solutionFile);
        logger.info("Solution loaded from file: " + solutionFile);
        return storedSolution;
    }
}
