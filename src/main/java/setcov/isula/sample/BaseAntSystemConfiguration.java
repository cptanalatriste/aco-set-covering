package setcov.isula.sample;

import isula.aco.ConfigurationProvider;

public class BaseAntSystemConfiguration implements ConfigurationProvider {

    private ConfigurationProvider configurationProvider;

    public void setNumberOfAnts(int numberOfAnts) {
        this.numberOfAnts = numberOfAnts;
    }

    public void setNumberOfIterations(int numberOfIterations) {
        this.numberOfIterations = numberOfIterations;
    }

    private int numberOfAnts;
    private int numberOfIterations;


    public BaseAntSystemConfiguration(ConfigurationProvider configurationProvider) {
        this.configurationProvider = configurationProvider;
        this.numberOfAnts = configurationProvider.getNumberOfAnts();
        this.numberOfIterations = configurationProvider.getNumberOfIterations();
    }

    public int getNumberOfAnts() {
        return numberOfAnts;
    }

    public double getEvaporationRatio() {
        return configurationProvider.getEvaporationRatio();
    }

    public int getNumberOfIterations() {
        return numberOfIterations;
    }

    public double getInitialPheromoneValue() {
        return configurationProvider.getInitialPheromoneValue();
    }

    public double getHeuristicImportance() {
        return configurationProvider.getHeuristicImportance();
    }

    public double getPheromoneImportance() {
        return configurationProvider.getPheromoneImportance();
    }

    public double getBasePheromoneValue() {
        return configurationProvider.getInitialPheromoneValue();
    }

    public String getConfigurationName() {
        return "AntSystemConf1";
    }


}
