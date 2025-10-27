package com.compiler.parser.lr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Queue;
import java.util.LinkedList;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;
import com.compiler.parser.syntax.StaticAnalyzer;

/**
 * Builds the canonical collection of LR(1) items (the DFA automaton).
 * Items contain a lookahead symbol.
 */
public class LR1Automaton {
    private final Grammar grammar;
    private final List<Set<LR1Item>> states = new ArrayList<>();
    private final Map<Integer, Map<Symbol, Integer>> transitions = new HashMap<>();
    private String augmentedLeftName = null;

    public LR1Automaton(Grammar grammar) {
        this.grammar = Objects.requireNonNull(grammar);
    }

    public List<Set<LR1Item>> getStates() { return states; }
    public Map<Integer, Map<Symbol, Integer>> getTransitions() { return transitions; }

    /**
     * CLOSURE for LR(1): standard algorithm using FIRST sets to compute lookaheads for new items.
     */
    private Set<LR1Item> closure(Set<LR1Item> items) {
        // 1. Initialize a new set `closure` with the given `items`.
        // 2. Create a worklist (like a Queue or List) and add all items from `items` to it.
        // 3. Pre-calculate the FIRST sets for all symbols in the grammar.
        // 4. While the worklist is not empty:
        //    a. Dequeue an item `[A -> α • B β, a]`.
        //    b. If `B` is a non-terminal:
        //       i. For each production of `B` (e.g., `B -> γ`):
        //          - Calculate the FIRST set of the sequence `βa`. This will be the lookahead for the new item.
        //          - For each terminal `b` in FIRST(βa):
        //             - Create a new item `[B -> • γ, b]`.
        //             - If this new item is not already in the `closure` set:
        //               - Add it to `closure`.
        //               - Enqueue it to the worklist.
        // 5. Return the `closure` set.
        Set<LR1Item> closure = new HashSet<>(items);
        Queue<LR1Item> worklist = new LinkedList<>(items);
        // Pre-calculate the FIRST sets for all symbols in the grammar.
        Map<Symbol, Set<Symbol>> firstSets = new StaticAnalyzer(grammar).getFirstSets();
        // Add the first of $
        firstSets.putIfAbsent(new Symbol("$", SymbolType.TERMINAL), new HashSet<>());
        firstSets.get(new Symbol("$", SymbolType.TERMINAL)).add(new Symbol("$", SymbolType.TERMINAL));

        while (!worklist.isEmpty()) {
            //Dequeue an item `[A -> α • B β, a]`.
            LR1Item item = worklist.poll();

            Symbol symbolAfterDot = item.getSymbolAfterDot();

            // If `B` is a non-terminal:
            if (symbolAfterDot != null && symbolAfterDot.type == SymbolType.NON_TERMINAL) {
                // B is a non-terminal
                Symbol B = symbolAfterDot;
                // Get β
                List<Symbol> beta = new ArrayList<>();
                List<Symbol> productionRight = item.production.right;
                for (int i = item.dotPosition + 1; i < productionRight.size(); i++) {
                    beta.add(productionRight.get(i));
                }
                // Append lookahead 'a' to β
                beta.add(item.lookahead);
                // Compute FIRST(βa)
                Set<Symbol> firstBetaA = computeFirstOfSequence(beta, firstSets, new Symbol("\u03b5", SymbolType.TERMINAL));
                // For each production of B
                for (Production p : grammar.getProductions()) {
                    if (p.left.equals(B)) {
                        for (Symbol b : firstBetaA) {
                            LR1Item newItem = new LR1Item(p, 0, b);
                            if (!closure.contains(newItem)) {
                                closure.add(newItem);
                                worklist.add(newItem);
                            }
                        }
                    }
                }
            }
        }
        return closure;
    }
    /**
     * Compute FIRST of a sequence of symbols.
     */
    private Set<Symbol> computeFirstOfSequence(List<Symbol> seq, Map<Symbol, Set<Symbol>> firstSets, Symbol epsilon) {
        // 1. Initialize an empty result set.
        // 2. If the sequence is empty, add epsilon to the result and return.
        // 3. Iterate through the symbols `X` in the sequence:
        //    a. Get `FIRST(X)` from the pre-calculated `firstSets`.
        //    b. Add all symbols from `FIRST(X)` to the result, except for epsilon.
        //    c. If `FIRST(X)` does not contain epsilon, stop and break the loop.
        //    d. If it does contain epsilon and this is the last symbol in the sequence, add epsilon to the result set.
        // 4. Return the result set.

        // Initialize an empty result set.
        Set<Symbol> result = new HashSet<>();
        // If the sequence is empty, add epsilon to the result and return.
        if (seq.isEmpty()) {
            result.add(epsilon);
            return result;
        }
        for (int i = 0; i < seq.size(); i++) {
            Symbol X = seq.get(i);
            //Get `FIRST(X)` from the pre-calculated `firstSets`.
            Set<Symbol> firstX = firstSets.get(X);
            if (firstX != null) {
                // Add all symbols from `FIRST(X)` to the result, except for epsilon.
                for (Symbol sym : firstX) {
                    if (!sym.equals(epsilon)) {
                        result.add(sym);
                    }
                }
                
                // If FIRST(X) does not contain epsilon, stop
                if (!firstX.contains(epsilon)) {
                    break;
                }
                
                // If this is the last symbol and it contains epsilon, add epsilon
                if (i == seq.size() - 1 && firstX.contains(epsilon)) {
                    result.add(epsilon);
                }
            } else {
                // If firstX is null, we can't continue
                break;
            }
        }
        return result;
    }

    /**
     * GOTO for LR(1): moves dot over symbol and takes closure.
     */
    private Set<LR1Item> goTo(Set<LR1Item> state, Symbol symbol) {
        // 1. Initialize an empty set `movedItems`.
        // 2. For each item `[A -> α • X β, a]` in the input `state`:
        //    a. If `X` is equal to the input `symbol`:
        //       - Add the new item `[A -> α X • β, a]` to `movedItems`.
        // 3. Return the `closure` of `movedItems`.
        Set<LR1Item> movedItems = new HashSet<>();
        for (LR1Item item : state) {
            Symbol symbolAfterDot = item.getSymbolAfterDot();
            if (symbolAfterDot != null && symbolAfterDot.equals(symbol)) {
                LR1Item movedItem = new LR1Item(item.production, item.dotPosition + 1, item.lookahead);
                movedItems.add(movedItem);
            }
        }
        return closure(movedItems);
    }

    /**
     * Build the LR(1) canonical collection: states and transitions.
     */
    public void build() {
        // 1. Clear any existing states and transitions.
        // 2. Create the augmented grammar: Add a new start symbol S' and production S' -> S.
        // 3. Create the initial item: `[S' -> • S, $]`.
        // 4. The first state, `I0`, is the `closure` of this initial item set. Add `I0` to the list of states.
        // 5. Create a worklist (queue) and add `I0` to it.
        // 6. While the worklist is not empty:
        //    a. Dequeue a state `I`.
        //    b. For each grammar symbol `X`:
        //       i. Calculate `J = goTo(I, X)`.
        //       ii. If `J` is not empty and not already in the list of states:
        //          - Add `J` to the list of states.
        //          - Enqueue `J` to the worklist.
        //       iii. Create a transition from the index of state `I` to the index of state `J` on symbol `X`.

        states.clear();
        transitions.clear();
        // Add a new start symbol S' and production S' -> S.
        this.augmentedLeftName = grammar.getStartSymbol().name + "'";
        Symbol newStartSymbol = new Symbol(augmentedLeftName, SymbolType.NON_TERMINAL);
        // create the production S' -> S
        Production newStartProduction = new Production(newStartSymbol, Collections.singletonList(grammar.getStartSymbol()));
        // create the initial item [S' -> • S, $]
        LR1Item initialItem = new LR1Item(newStartProduction, 0, new Symbol("$", SymbolType.TERMINAL));
        // add the initial item to the first state
        Set<LR1Item> initialState = new HashSet<>();
        initialState.add(initialItem);
         // The first state, `I0`, is the `closure` of this initial item set. Add `I0` to the list of states.
        initialState = closure(initialState);
        // add the initial state to the list of states
        states.add(initialState);
        // Create a worklist (queue) and add `I0` to it.
        Queue<Set<LR1Item>> worklist = new LinkedList<>();
        worklist.add(initialState);
        // While the worklist is not empty:
        while (!worklist.isEmpty()) {
            // a. Dequeue a state `I`.
            Set<LR1Item> stateI = worklist.poll();
            int indexI = states.indexOf(stateI);
            // b. For each grammar symbol `X`:
            Set<Symbol> grammarSymbols = new HashSet<>(grammar.getNonTerminals());
            grammarSymbols.addAll(grammar.getTerminals());
            
            for (Symbol X : grammarSymbols) {
                // i. Calculate `J = goTo(I, X)`.
                Set<LR1Item> stateJ = goTo(stateI, X);
                // ii. If `J` is not empty and not already in the list of states:
                if (!stateJ.isEmpty()) {
                    int indexJ = states.indexOf(stateJ);
                    if (indexJ == -1) {
                        // - Add `J` to the list of states.
                        states.add(stateJ);
                        indexJ = states.size() - 1;
                        // - Enqueue `J` to the worklist.
                        worklist.add(stateJ);
                    }
                    // iii. Create a transition from the index of state `I` to the index of state `J` on symbol `X`.
                    transitions.computeIfAbsent(indexI, k -> new HashMap<>()).put(X, indexJ);
                }
            }
        }



    }

    public String getAugmentedLeftName() { return augmentedLeftName; }

    /**
     * Prints all states and transitions in a readable format.
     */
    public void printStatesAndTransitions() {
        System.out.println("=== LR(1) AUTOMATON STATES ===");
        for (int i = 0; i < states.size(); i++) {
            System.out.println("State " + i + ":");
            for (LR1Item item : states.get(i)) {
                System.out.println("  " + item);
            }
            System.out.println();
        }

        System.out.println("=== TRANSITIONS ===");
        for (Map.Entry<Integer, Map<Symbol, Integer>> entry : transitions.entrySet()) {
            int fromState = entry.getKey();
            for (Map.Entry<Symbol, Integer> transition : entry.getValue().entrySet()) {
                Symbol symbol = transition.getKey();
                int toState = transition.getValue();
                System.out.println("State " + fromState + " --" + symbol.toString() + "--> State " + toState);
            }
        }
    }


}
