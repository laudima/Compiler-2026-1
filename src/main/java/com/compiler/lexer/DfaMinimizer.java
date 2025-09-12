
/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.State;

/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    /**
     * Default constructor for DfaMinimizer.
     */
        public DfaMinimizer() {
        }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
        /*
        Pseudocode:
        1. Collect and sort all DFA states
        2. Initialize table of state pairs; mark pairs as distinguishable if one is final and the other is not
        3. Iteratively mark pairs as distinguishable if their transitions lead to distinguishable states or only one has a transition
        4. Partition states into equivalence classes (using union-find)
        5. Create new minimized states for each partition
        6. Reconstruct transitions for minimized states
        7. Set start state and return minimized DFA
        */
        List<DfaState> allStates = new ArrayList<>(originalDfa.getAllStates());
        allStates.sort((s1, s2) -> Integer.compare(s1.id, s2.id)); // Sort states by id for consistent ordering
        Map<Pair, Boolean> table = new HashMap<>(); // Table to track distinguishable pairs

        int n = allStates.size(); // Size of the state list

        //Initialize table and mark pairs
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                DfaState s1 = allStates.get(i);
                DfaState s2 = allStates.get(j);
                boolean distinguishable = (s1.isFinal() != s2.isFinal());
                table.put(new Pair(s1, s2), distinguishable);
            }
        }

        // Iteratively mark pairs as distinguishable if their transitions lead to distinguishable states or only one has a transition
        boolean changed; // Necessary to do again the cycle if a new pair is marked something else changed
        do {
            changed = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    DfaState s1 = allStates.get(i);
                    DfaState s2 = allStates.get(j);
                    Pair pair = new Pair(s1, s2);
                    if (table.get(pair)) {
                        continue; // Already marked as distinguishable
                    }
                    for (char symbol : alphabet) {
                        DfaState t1 = s1.getTransitions().get(symbol);
                        DfaState t2 = s2.getTransitions().get(symbol);
                        if (t1 == null && t2 == null) {
                            continue; // Both states lack transition on this symbol
                        }
                        if (t1 == null || t2 == null) {
                            table.put(pair, true); // One state has a transition, the other does not
                            changed = true;
                            break;
                        }
                        Pair targetPair = new Pair(t1, t2);
                        if (table.getOrDefault(targetPair, false)) {
                            table.put(pair, true); // Transitions lead to distinguishable states
                            changed = true;
                            break;
                        }
                    }
                }
            }
        } while (changed);

        List<Set<DfaState>> partitions = createPartitions(allStates, table); // Group equivalent states

        // new minimized states for each partition
        List<DfaState> newStates = new ArrayList<>();
        for (int i = 0; i < partitions.size(); i++) {
            Set<DfaState> partition = partitions.get(i);
            
            Set<State> combinedNfaStates = new HashSet<>();
            for (DfaState state : partition) {
                Set<State> nfaStates = state.getNfaStates();
                combinedNfaStates.addAll(nfaStates);
            }
            DfaState newState = new DfaState(combinedNfaStates);
            newStates.add(newState);
        }

        // Map old states to new minimized states
        Map<DfaState, DfaState> stateMapping = new HashMap<>();
        for (int i = 0; i < partitions.size(); i++) {
            Set<DfaState> partition = partitions.get(i);
            DfaState newState = newStates.get(i);
            for (DfaState oldState : partition) {
                stateMapping.put(oldState, newState);
            }
        }
        // Reconstruct transitions for minimized states
        for (Set<DfaState> partition : partitions) {
            DfaState representative = partition.iterator().next();
            DfaState newState = stateMapping.get(representative);
            for (char symbol : alphabet) {
                DfaState target = representative.getTransitions().get(symbol);
                if (target != null) {
                    DfaState newTarget = stateMapping.get(target);
                    newState.addTransition(symbol, newTarget);
                }
            }
        }
        // Determine the new start state
        DfaState newStartState = stateMapping.get(originalDfa.startState);
        return new DFA(newStartState, newStates);

    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
        /*
        Pseudocode:
        1. Initialize each state as its own parent
        2. For each pair not marked as distinguishable, union the states
        3. Group states by their root parent
        4. Return list of partitions
        */
        Map<DfaState, DfaState> parent = new HashMap<>();
        for (DfaState state : allStates) {
            parent.put(state, state); // Each state is its own parent initially
        }

        // Union states based on the distinguishability table
        for (Map.Entry<Pair, Boolean> entry : table.entrySet()) {
            if (!entry.getValue()) { // Not marked as distinguishable
                Pair pair = entry.getKey(); // Get the pair of states
                union(parent, pair.s1, pair.s2); // Union the two states
            }
        }

        // Group states by their root parent
        Map<DfaState, Set<DfaState>> partitions = new HashMap<>();
        for (DfaState state : allStates) {
            DfaState root = find(parent, state);
            if (!partitions.containsKey(root)) {
                partitions.put(root, new HashSet<DfaState>());
            }
            partitions.get(root).add(state);
        }

        return new ArrayList<>(partitions.values());
    }

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
        /*
        Pseudocode:
        If parent[state] == state, return state
        Else, recursively find parent and apply path compression
        Return parent[state]
        */
        // Base case: if state is its own parent, return it
        if (parent.get(state) == state) {
            return state;
        }
        // Recursive case: find the root parent and apply path compression
        DfaState root = find(parent, parent.get(state)); 
        parent.put(state, root);
        return root; // Path compression
    }

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
        /*
        Pseudocode:
        Find roots of s1 and s2
        If roots are different, set parent of one to the other
        */
        DfaState root1 = find(parent, s1);
        DfaState root2 = find(parent, s2);
        if (root1 != root2) {
            parent.put(root2, root1);
        }
    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            /*
             Pseudocode:
             Assign s1 and s2 so that s1.id <= s2.id
            */
            DfaState first = s1.id <= s2.id ? s1 : s2;
            DfaState second = s1.id <= s2.id ? s2 : s1;
            this.s1 = first;
            this.s2 = second;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return s1.id == pair.s1.id && s2.id == pair.s2.id;
        }

        @Override
        public int hashCode() {
            return 31 * s1.id + s2.id;
        }
    }
}
