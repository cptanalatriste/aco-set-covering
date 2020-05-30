package setcov.isula.sample;

import isula.aco.ConfigurationProvider;
import isula.aco.algorithms.antsystem.AntSystemConfigurationProvider;
import isula.aco.tuning.BasicConfigurationProvider;

public class BaseAntSystemConfiguration extends BasicConfigurationProvider implements AntSystemConfigurationProvider {


    private double pheromoneDepositFactor = 0.0;

    public BaseAntSystemConfiguration() {

    }

    public BaseAntSystemConfiguration(ConfigurationProvider configurationProvider) {
        super(configurationProvider);
        this.pheromoneDepositFactor = configurationProvider.getInitialPheromoneValue();
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
                ", pheromoneDepositFactor=" + pheromoneDepositFactor +
                '}';
    }

    public void setPheromoneDepositFactor(double pheromoneDepositFactor) {
        this.pheromoneDepositFactor = pheromoneDepositFactor;
    }

    @Override
    public double getPheromoneDepositFactor() {
        return pheromoneDepositFactor;
    }
}
