package setcov.isula.sample;

import isula.aco.ConfigurationProvider;

public class BaseAntSystemConfiguration implements ConfigurationProvider {

    public int getNumberOfAnts() {
//        return 150;
        return 50;
    }

    public double getEvaporationRatio() {
        return 0.2;
    }

    public int getNumberOfIterations() {
        return 5;
    }

    public double getInitialPheromoneValue() {
        return this.getBasePheromoneValue();
    }

    public double getHeuristicImportance() {
        return 1.5;
    }

    public double getPheromoneImportance() {
        return 4;
    }

    public double getBasePheromoneValue() {
        return 90.0;
    }

    public String getConfigurationName() {
        return "AntSystemConf1";
    }
}
