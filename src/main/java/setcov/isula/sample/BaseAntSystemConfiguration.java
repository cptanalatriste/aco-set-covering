package setcov.isula.sample;

import isula.aco.ConfigurationProvider;
import isula.aco.tuning.BasicConfigurationProvider;

public class BaseAntSystemConfiguration extends BasicConfigurationProvider {


    private double basePheromoneValue;

    public BaseAntSystemConfiguration(ConfigurationProvider configurationProvider) {
        super(configurationProvider);
        this.basePheromoneValue = configurationProvider.getInitialPheromoneValue();
    }

    public double getBasePheromoneValue() {
        return basePheromoneValue;
    }

    public String getConfigurationName() {
        return "AntSystemConfiguration";
    }


    @Override
    public String toString() {
        return "BaseAntSystemConfiguration{" +
                "numberOfAnts=" + getNumberOfAnts() +
                ", evaporationRatio=" + getEvaporationRatio() +
                ", numberOfIterations=" + getNumberOfIterations() +
                ", initialPheromoneValue=" + getInitialPheromoneValue() +
                ", heuristicImportance=" + getHeuristicImportance() +
                ", pheromoneImportance=" + getPheromoneImportance() +
                ", basePheromoneValue=" + basePheromoneValue +
                '}';
    }

}
