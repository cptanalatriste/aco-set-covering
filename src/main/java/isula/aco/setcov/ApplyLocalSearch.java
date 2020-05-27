package isula.aco.setcov;

import isula.aco.AntPolicy;
import isula.aco.AntPolicyType;
import isula.aco.ConfigurationProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class ApplyLocalSearch extends AntPolicy<Integer, SetCoveringEnvironment> {

    private static Logger logger = Logger.getLogger(ApplyLocalSearch.class.getName());

    public ApplyLocalSearch() {
        super(AntPolicyType.AFTER_SOLUTION_IS_READY);
    }

    @Override
    public boolean applyPolicy(SetCoveringEnvironment environment, ConfigurationProvider configurationProvider) {

        AntForSetCovering ant = (AntForSetCovering) this.getAnt();
        Integer[] currentSolution = ant.getSolution();

        Set<Integer> componentsToRemove = new HashSet<>();
        for (Integer candidateIndex : currentSolution) {

            if (candidateIndex != null) {
                Set<Integer> exclusivelyCovered = environment.getSamplesCovered(candidateIndex);

                for (Integer opponentIndex : currentSolution) {
                    if (opponentIndex != null && !opponentIndex.equals(candidateIndex) && !componentsToRemove.contains(opponentIndex)) {
                        Set<Integer> coveredByOpponent = environment.getSamplesCovered(opponentIndex);
                        exclusivelyCovered.removeAll(coveredByOpponent);

                        if (exclusivelyCovered.size() == 0) {
                            componentsToRemove.add(candidateIndex);
                            break;
                        }
                    }
                }
            }
        }

        if (componentsToRemove.size() > 0) {
            logger.fine("Removing " + componentsToRemove.size() + " from a solution of size " + ant.getSolutionCost(environment));
            List<Integer> newSolution = new ArrayList<>();

            for (Integer candidateIndex : currentSolution) {
                if (candidateIndex != null && !componentsToRemove.contains(candidateIndex)) {
                    newSolution.add(candidateIndex);
                }
            }

            ant.setSolution(newSolution.toArray(new Integer[environment.getNumberOfCandidates()]));
        }


        return true;
    }
}
