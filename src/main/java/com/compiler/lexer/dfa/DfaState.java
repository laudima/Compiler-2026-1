package com.compiler.lexer.dfa;

import java.util.Map;
import java.util.Set;

import com.compiler.lexer.nfa.State;

/**
 * DfaState
 * --------
 * Represents a single state in a Deterministic Finite Automaton (DFA).
 * Each DFA state corresponds to a set of states from the original NFA.
 * Provides methods for managing transitions, checking finality, and equality based on NFA state sets.
 */
public class DfaState {
    /**
     * Returns all transitions from this state.
     * @return Map of input symbols to destination DFA states.
     */
    public Map<Character, DfaState> getTransitions() {
        return transitions;
    }

    private static int nextId = 0;
    /**
     * Unique identifier for this DFA state.
     */
    public final int id;

    /**
     * The set of NFA states this DFA state represents.
     */
    public final Set<State> nfaStates;
    /**
     * Indicates whether this DFA state is a final (accepting) state.
     */
    public boolean isFinal;

    /**
     * Token type reconocido si es estado final (null si no es final o no tiene token asociado).
     */
    public String tokenType;
    /**
     * Map of input symbols to destination DFA states (transitions).
     */
    public final Map<Character, DfaState> transitions;

    /**
     * Constructs a new DFA state.
     * @param nfaStates The set of NFA states that this DFA state represents.
     */

    public DfaState(Set<State> nfaStates) {
        this.id = nextId++;
        this.nfaStates = nfaStates;
        this.isFinal = nfaStates.stream().anyMatch(State::isFinal);
        this.tokenType = null;
        // If it is final, assign the tokenType of highest priority (lowest index)
        int minPriority = Integer.MAX_VALUE;
        for (State s : nfaStates) {
            if (s.isFinal && s.tokenType != null) {
                // It is assumed that the tokenType has the form "TOKENNAME#PRIORITY"
                int priority = extractPriority(s.tokenType);
                if (priority < minPriority) {
                    minPriority = priority;
                    this.tokenType = s.tokenType;
                }
            }
        }
        this.transitions = new java.util.HashMap<>();
    }

    /**
     * Extracts the priority number from a token type string.
     * For example, from "TOKEN#2" it extracts 2.
     * If the format is not as expected, it returns Integer.MAX_VALUE.
     * @param tokenType The token type string.
     * @return The extracted priority number.
     */
    private int extractPriority(String tokenType) {
        if (tokenType.contains("#")) {
            try {
                return Integer.parseInt(tokenType.substring(tokenType.indexOf('#')+1));
            } catch (Exception e) {
                return Integer.MAX_VALUE;
            }
        }
        return 0;
    }

    public DfaState(int id, boolean isFinal, Set<State> nfaStates) {
        this.id = id;
        this.isFinal = isFinal;
        this.nfaStates = nfaStates;
        this.transitions = new java.util.HashMap<>();
    }

    /**
     * Adds a transition from this state to another on a given symbol.
     * @param symbol The input symbol for the transition.
     * @param toState The destination DFA state.
     */
    public void addTransition(Character symbol, DfaState toState) {
        transitions.put(symbol, toState);
    }

    /**
     * Two DfaStates are considered equal if they represent the same set of NFA states.
     * @param obj The object to compare.
     * @return True if the states are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DfaState)) return false;
        DfaState other = (DfaState) obj;
        return this.nfaStates.equals(other.nfaStates);
    }

    /**
     * The hash code is based on the set of NFA states.
     * @return The hash code for this DFA state.
     */
    @Override
    public int hashCode() {
        return nfaStates.hashCode();
    }
    
    /**
     * Returns a string representation of the DFA state, including its id and finality.
     * @return String representation of the state.
     */
    @Override
    public String toString() {
        return "DFA State " + id + (isFinal ? " (Final)" : "");
    }

    /**
     * Sets the finality of the DFA state.
     * @param isFinal True if this state is a final state, false otherwise.
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Checks if the DFA state is final.
     * @return True if this state is a final state, false otherwise.
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Gets the transition for a given input symbol.
     * @param symbol The input symbol for the transition.
     * @return The destination DFA state for the transition, or null if there is no transition for the given symbol.
     */
    public DfaState getTransition(char symbol) {
        return transitions.get(symbol);
    }

    /**
     * Returns the set of NFA states this DFA state represents.
     * @return The set of NFA states.
     */
    public Set<State> getNfaStates() {
        return nfaStates;
    }
}