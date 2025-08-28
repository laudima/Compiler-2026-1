package com.compiler.lexer.nfa;

import java.util.Set;
/**
 * Represents a Non-deterministic Finite Automaton (NFA) with a start and end state.
 * <p>
 * An NFA is used in lexical analysis to model regular expressions and pattern matching.
 * This class encapsulates the start and end states of the automaton.
 */

public class NFA {
    /**
     * The initial (start) state of the NFA.
     */
    public final State startState;

    /**
     * The final (accepting) state of the NFA.
     */
    public final State endState;

    /**
     * The current state of the NFA during processing.
     */
    public final State currentState;

    /**
     * The set of states in the NFA.
     */
    public final Set<State> states;

    /**
     * The alphabet of the NFA (set of input symbols).
     */
    public final Set<Character> alphabet;

    /**
     * The transitions of the NFA.
     * Each transition is represented as a pair: (current state, transition).
     */
    public final Set<TransitionTuple> transitions;

    /**
     * Helper class to represent a transition tuple (current state, transition).
     */
    public static class TransitionTuple {
        public final State fromState;
        public final Transition transition;

        public TransitionTuple(State fromState, Transition transition) {
            this.fromState = fromState;
            this.transition = transition;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TransitionTuple other = (TransitionTuple) obj;
            return fromState.equals(other.fromState) && transition.equals(other.transition);
        }

        @Override
        public int hashCode() {
            return 31 * fromState.hashCode() + transition.hashCode();
        }
    }

    /**
     * Constructs a new NFA with the given start and end states.
     * @param start The initial state.
     * @param end The final (accepting) state.
     */
    public NFA(State start, State end) {
        this.startState = start;
        this.endState = end;
        end.setFinal(true); // Mark end state as final
        this.currentState = start; // Initialize current state to start state
        this.states = Set.of(start, end); // TODO: this are not all of the states
        this.alphabet = Set.of(); // TODO: these are not all of the symbols 
        this.transitions = Set.of(); // TODO: this are not all the transitions
    }

    /**
     * Returns the initial (start) state of the NFA.
     * @return the start state
     */
    public State getStartState() {
        return startState;
    }

    /**
     * Returns the final (accepting) state of the NFA.
     * @return the end state
     */
    public State getEndState() {
        return endState;
    }

    /**
     * Adds a transition to the NFA.
     * @param symbol the symbol for the transition
     * @param toState the state to transition to
     */
    public void addTransition(char symbol, State toState) {
        Transition transition = new Transition(symbol, toState);
        transitions.add(new TransitionTuple(currentState, transition));
    }


}