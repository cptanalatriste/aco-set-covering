# aco-set-covering
A Java Program to solve the Unicost Set Covering Problem (USCP) using Ant Colony Algorithms. 


The Ant-Colony Algorithm
------------------------

The following figure shows an overview of our problem solving approach.
The process starts with an initial problem, containing list of _samples_
to cover.
Each sample can be covered by multiple _candidates_, and each candidate
covers several samples.
For a given problem instance, the algorithm produces a final solution containing
a list candidates, covering _all_ the samples of the initial problem.
Our goal is for this list to be as small as possible.

![Ant Colony ALgorithm for Set Covering](https://github.com/cptanalatriste/aco-set-covering/blob/master/img/aco_set_covering.png?raw=true)


**Preprocessing:** In set covering problems, execution time depends
largely on the number of candidates in the problem instance.
As is common in set covering solvers, we attempt to reduce this number by
removing _dominated candidates_.
A candidate dominates another when it covers _all_ of its covered samples.
Identifying dominated candidates can take significant time for large problem
instances.
To reduce time, our dominance analysis implementation uses multiple
concurrent threads.

Another common preprocessing step is the inclusion of _mandatory candidates_
in any solution to build.
After the dominance analysis phase, some samples might be covered by _only one_
candidate, becoming a mandatory part of every solution.
We group all mandatory candidates in a partial solution, that we
expand in the solution construction phase.

**Solution Construction:** There are a plethora of algorithms in the ACO
framework.
For the solution construction phase, we selected Any System (AS).
AS is the very first ACO algorithm, originally proposed to address the Travelling
Salesman problem.
We are basing our implementation on its adaptation to the set covering
problem [proposed by Silva and Ramalho](https://ieeexplore.ieee.org/abstract/document/971999).

In AS, `n` ants in a colony build solutions by randomly selecting candidates
during `t` iterations.
The probability of selecting a candidate is a function of their
associated pheromone value and **heuristic information**.
Heuristic information is a problem-dependent metric.
For set covering problems, is related to the proportion of uncovered samples a
potential candidate can cover.
The influence of pheromone and heuristic information in the probability values
is regulated by the parameters `alpha` and `beta`.
Once all ants have a solution ready, they deposit pheromone in the candidates
conforming their solution.
The amount of pheromone depends on the number of candidates (the shorter,
the better) and a constant `Q`.
To favour exploration, at the end of an iteration pheromone "evaporates"
on all candidates, by a factor `rho`.

Our original AS implementation had two major problems.
First, it did not incorporate local search after solution construction,
being local search a key element of successful ACO algorithms.
Second, solution construction was slow in large problem instances: in each
solution construction step, ants evaluate a large number of candidates and
their probabilities.
We address these issues by incorporating [ideas from Ren _et al._](https://www.sciencedirect.com/science/article/abs/pii/S0360835210000471).
They propose a local search procedure to remove _redundant candidates_
from every ant's solution, where redundancy requires a candidate to _not_
uniquely cover a sample.
We also incorporated their _single-row oriented method (SROM)_ as our
candidate selection policy.
In SROM, ants at each construction step focus on covering a single sample
instead of considering the whole uncovered space.
Local search and SROM impacted positively in solution quality
and execution time.

One of our design goals was to incorporate parallel processing in our ACO
approach, to maximise the number of solutions generated per time unit.
We adopted [a simple strategy](https://link.springer.com/chapter/10.1007/BFb0056914) that have shown promising results in other
optimisation problems: we have `r` independent ant colonies running in
parallel.
Each colony produces one solution, and we select the best among them as the
output of the solution construction phase.

**Solution Improvement:** Our extended AS algorithm still struggled
with very large problem instances.
The solutions produced after exhausting the time budget were selected among
very few candidate solutions.
This did not guarantee a proper exploration of the solution space.
We needed a way to reduce solution construction time, ideally taking advantage
of the candidate solutions generated after the solution construction phase.

We found a solution to this problem in _Iterated Ants_, an [ACO-based version
of the Iterated Greedy algorithm](https://link.springer.com/chapter/10.1007/11839088_16).
After generating an initial candidate solution, it performs the following on
every iteration:

1) Builds a partial solution by removing `k` elements from the
candidate solution 
2) Completes the partial solution using an ACO algorithm and
3) The complete solution then becomes the new solution to prune.
Construction time is reduced since ants have less components to add to an
already partially complete solution.

In our adaptation of Iterated Ants, the first solution triggering
the algorithm is the one produced by the solution construction phase.
For completing partial solutions, we rely on the extended AS algorithm we
described before.
Candidate removal for transforming a complete solution into a partial one is
driven by pheromone: the less pheromone associated with a candidate, the less
likely to be part of the new partial solution.

How to use this code
--------------------
The code uploaded to this GitHub Repository corresponds to a Maven Java Project. As such, it is strongly recommended that 
you have Maven installed before working with it.
**This project depends on the Isula Framework**. Follow the instructions available in
 https://github.com/cptanalatriste/isula

You can launch this project by executing ` mvn exec:java -Dexec.mainClass="setcov.isula.sample.AcoSetCoveringWithIsula"  -D exec.args="-f /pathToFolder/problem_data/AC_10_cover.txt" ` 
from the project root folder.

More about Isula
----------------
Visit the Isula Framework site: http://cptanalatriste.github.io/isula/

Review the Isula JavaDoc: http://cptanalatriste.github.io/isula/doc/

Questions, issues or support?
----------------------------
Feel free to contact me at carlos.gavidia@pucp.edu.pe.
