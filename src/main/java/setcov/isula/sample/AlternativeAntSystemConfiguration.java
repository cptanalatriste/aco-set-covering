package setcov.isula.sample;


public class AlternativeAntSystemConfiguration extends BaseAntSystemConfiguration {


    public double getEvaporationRatio() {
        return 0.8;
    }

    public int getNumberOfIterations() {
        return 5;
    }

    public double getHeuristicImportance() {
        return 5.0;
    }

    public double getPheromoneImportance() {
        return 0.25;
    }

    public double getBasePheromoneValue() {
        return 1.0;
    }

    public String getConfigurationName() {
        return "AntSystemConf2";
    }
}
