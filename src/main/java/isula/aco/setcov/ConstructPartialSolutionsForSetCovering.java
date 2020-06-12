package isula.aco.setcov;

import isula.aco.algorithms.iteratedants.ConstructPartialSolution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static setcov.isula.sample.IteratedAntsForSetCovering.REMOVAL_FACTOR;


public class ConstructPartialSolutionsForSetCovering extends ConstructPartialSolution<Integer, SetCoveringEnvironment> {


    @Override
    public int getNumberOfComponentsToRemove() {
        return (int) (this.getAnt().getSolution().size() * REMOVAL_FACTOR);
    }

    @Override
    public List<Integer> getNewPartialSolution(List<Integer> indexesForRemoval) {
        List<Integer> currentSolution = getAnt().getSolution();
        List<Integer> candidatesToRemove = indexesForRemoval
                .stream()
                .mapToInt(currentSolution::get)
                .boxed()
                .collect(Collectors.toList());

        List<Integer> newPartialSolution = new ArrayList<>(currentSolution);
        newPartialSolution.removeAll(candidatesToRemove);
        return newPartialSolution;
    }
}
