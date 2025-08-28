package com.compiler.lexer.nfa;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a state in a Non-deterministic Finite Automaton (NFA).
 * Each state has a unique identifier, a list of transitions to other states,
 * and a flag indicating whether it is a final (accepting) state.
 *
 * <p>
 * Fields:
 * <ul>
 *   <li>{@code id} - Unique identifier for the state.</li>
 *   <li>{@code transitions} - List of transitions from this state to others.</li>
 *   <li>{@code isFinal} - Indicates if this state is an accepting state.</li>
 * </ul>
 *
 *
 * <p>
 * The {@code nextId} static field is used to assign unique IDs to each state.
 * </p>
 */
public class State {
    private static int nextId = 0;
    /**
     * Unique identifier for this state.
     */
    public final int id;

    /**
     * List of transitions from this state to other states.
     */
    public List<Transition> transitions;

    /**
     * Indicates if this state is a final (accepting) state.
     */
    public boolean isFinal;

    /**
     * Constructs a new state with a unique identifier and no transitions.
     * The state is not final by default.
     */
    public State() {
        this.id = nextId++;
        this.isFinal = false;
    }

    /**
     * Checks if this state is a final (accepting) state.
     * @return true if this state is final, false otherwise
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Returns the states reachable from this state via epsilon transitions (symbol == null).
     * @return a list of states reachable by epsilon transitions
     */
    public List<State> getEpsilonTransitions() {
        // Pseudocode: Iterate over transitions, if symbol is null, add to result list
        List<State> result = new ArrayList<>();
        if (transitions == null) return result;
        for (Transition t : transitions) {
            if (t.symbol == null) {
                result.add(t.toState);
            }
        }
        return result;
    }

    /**
     * Returns the states reachable from this state via a transition with the given symbol.
     * @param symbol the symbol for the transition
     * @return a list of states reachable by the given symbol
     */
    public List<State> getTransitions(char symbol) {
    // Pseudocode: Iterate over transitions, if symbol matches, add to result list
    // do i have to add the epsilon transitions as well?
        List<State> result = new ArrayList<>();
        if (transitions == null) return result; // If there is no transitions 
        for (Transition t : transitions) {
            if (t.symbol == null) {
                continue;
            } else if (t.symbol == symbol) {
                result.add(t.toState);
            }
        }
        return result;
    }

    /**
     * Adds a transition to this state.
     * Use {@code null} as the symbol for epsilon transitions.
     * @param symbol the symbol for the transition, or {@code null} for epsilon
     * @param toState the state to transition to
     */
    public void addTransition(Character symbol, State toState) {
        if (transitions == null) {
            transitions = new ArrayList<>();
        }
        transitions.add(new Transition(symbol, toState));
    }

    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
}