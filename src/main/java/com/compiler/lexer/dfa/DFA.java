package com.compiler.lexer.dfa;

import java.util.List;
import java.util.Set;

/**
 * DFA
 * ---
 * Represents a complete Deterministic Finite Automaton (DFA).
 * Contains the start state and a list of all states in the automaton.
 */
public class DFA {
    /**
     * The starting state of the DFA.
     */
    public final DfaState startState;

    /**
     * A list of all states in the DFA.
     */
    public final List<DfaState> allStates;

    public final Set<Character> alphabet;

    /**
     * Constructs a new DFA.
     * @param startState The starting state of the DFA.
     * @param allStates  A list of all states in the DFA.
     */
    public DFA(DfaState startState, List<DfaState> allStates) {
        this.startState = startState;
        this.allStates = allStates;
        this.alphabet = new java.util.HashSet<>();
        for (DfaState state : allStates) {
            // Collect all unique symbols in the DFA's alphabet
            // With the function keySet(), we get all the keys (input symbols) from the transitions map
            this.alphabet.addAll(state.getTransitions().keySet());
        }
    }
}