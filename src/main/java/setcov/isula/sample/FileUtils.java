package setcov.isula.sample;

import isula.aco.setcov.SetCoveringPreProcessor;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

public class FileUtils {

    private static final Logger logger = Logger.getLogger(FileUtils.class.getName());
    private static final String DATA_DIRECTORY = "/Users/cgavidia/Documents/data/";

    private static final int UNASSIGNED = -1;
    private static final double COVERED = 1.0;
    private static final String TEAM_NAME = "Isula";

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

    static String getInputFile(String instanceName) {
        return instanceName + "_cover.txt";
    }

    private static String getOutputFile(String instanceName, String algorithmName) {
        return TEAM_NAME + "_" + algorithmName + "_Track1_" + instanceName + ".txt";
    }

    public static SetCoveringPreProcessor initialisePreProcessorFromFile(String fileName) throws IOException {

        int numberOfSamples;
        int numberOfCandidates;

        SetCoveringPreProcessor dataPreProcessor = new SetCoveringPreProcessor();

        int lineCounter = 0;
        int sampleIndex = UNASSIGNED;
        int candidatesForSample = UNASSIGNED;
        try (BufferedReader reader = new BufferedReader(new FileReader(DATA_DIRECTORY + fileName))) {
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
}
