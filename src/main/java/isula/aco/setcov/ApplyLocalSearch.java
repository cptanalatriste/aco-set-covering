package isula.aco.setcov;

import isula.aco.AntPolicy;
import isula.aco.AntPolicyType;
import isula.aco.ConfigurationProvider;

import java.util.*;
import java.util.logging.Logger;

public class ApplyLocalSearch extends AntPolicy<Integer, SetCoveringEnvironment> {

    private static final Logger logger = Logger.getLogger(ApplyLocalSearch.class.getName());

    public ApplyLocalSearch() {
        super(AntPolicyType.AFTER_SOLUTION_IS_READY);
    }

    @Override
    public boolean applyPolicy(SetCoveringEnvironment environment, ConfigurationProvider configurationProvider) {

        logger.fine("Staring local search");

        AntForSetCovering ant = (AntForSetCovering) this.getAnt();
        List<Integer> currentSolution = ant.getSolution();

        Set<Integer> componentsToRemove = new HashSet<>();
        for (Integer candidateIndex : currentSolution) {

            if (candidateIndex != null) {
                Set<Integer> exclusivelyCovered = new HashSet<>(
                        environment.getSamplesForNonDominatedCandidate(candidateIndex));

                for (Integer opponentIndex : currentSolution) {
                    if (opponentIndex != null && !opponentIndex.equals(candidateIndex) &&
                            !componentsToRemove.contains(opponentIndex)) {
                        Set<Integer> coveredByOpponent = environment.getSamplesForNonDominatedCandidate(opponentIndex);
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
            logger.fine("Removing " + componentsToRemove.size() +
                    " from a solution of size " + ant.getSolutionCost(environment));
            ant.getSolution().removeAll(componentsToRemove);
        }

        logger.fine("Ending local search");


        return true;
    }

    @Override
    public String toString() {
        return "ApplyLocalSearch{}";
    }
}
