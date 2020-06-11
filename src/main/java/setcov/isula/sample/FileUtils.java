package setcov.isula.sample;

import isula.aco.setcov.SetCoveringPreProcessor;

import java.io.*;
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

    public static final int TOTAL_PROBLEM_INSTANCES = 69;

    public static final List<String> processedFiles = Arrays.asList(
            "AC_01", "AC_02", "AC_03", "AC_04", "AC_05",
            "AC_10", "AC_11", "AC_12", "AC_13", "AC_14", "AC_15", "AC_16", "AC_17",
            "RW_01", "RW_02", "RW_03", "RW_04", "RW_05", "RW_06", "RW_07", "RW_08", "RW_09",
            "RW_10", "RW_11", "RW_12", "RW_13", "RW_14", "RW_15", "RW_16", "RW_17", "RW_18", "RW_19",
            "RW_20", "RW_22", "RW_23", "RW_24", "RW_25", "RW_26", "RW_27", "RW_28", "RW_29",
            "RW_30", "RW_32", "RW_33", "RW_34", "RW_35", "RW_36", "RW_37"
    );


    public static boolean shouldProcessFile(String fileName) {
        String instanceName = getInstanceName(fileName);
        return !processedFiles.contains(instanceName);
//        return true;
//        return instanceName.equals("AC_13");

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
        return isValidSolution(solutionFound, preProcessor);
    }


    public static boolean isValidSolution(List<Integer> solutionFound, SetCoveringPreProcessor preProcessor) {
        Map<Integer, Set<Integer>> samplesPerCandidate = preProcessor.calculateSamplesPerCandidate();

        boolean[] samplesCovered = new boolean[preProcessor.getNumberOfSamples()];
        int pendingSamples = preProcessor.getNumberOfSamples();

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
            List<Integer> uncoveredSamples = IntStream.range(0, preProcessor.getNumberOfSamples())
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
