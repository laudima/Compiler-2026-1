package com.compiler.lexer;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;

/**
 * NfaToDfaConverter
 * -----------------
 * This class provides a static method to convert a Non-deterministic Finite Automaton (NFA)
 * into a Deterministic Finite Automaton (DFA) using the standard subset construction algorithm.
 */
/**
 * Utility class for converting NFAs to DFAs using the subset construction algorithm.
 */
public class NfaToDfaConverter {
	/**
	 * Default constructor for NfaToDfaConverter.
	 */
	public NfaToDfaConverter() {
		// No initialization required
	}

	/**
	 * Converts an NFA to a DFA using the subset construction algorithm.
	 * Each DFA state represents a set of NFA states. Final states are marked if any NFA state in the set is final.
	 *
	 * @param nfa The input NFA
	 * @param alphabet The input alphabet (set of characters)
	 * @return The resulting DFA
	 */
	public static DFA convertNfaToDfa(NFA nfa, Set<Character> alphabet) {
		/*
		 Pseudocode:
		 1. Create initial DFA state from epsilon-closure of NFA start state
		 2. While there are unmarked DFA states:
			  - For each symbol in alphabet:
				  - Compute move and epsilon-closure for current DFA state
				  - If target set is new, create new DFA state and add to list/queue
				  - Add transition from current to target DFA state
		 3. Mark DFA states as final if any NFA state in their set is final
		 4. Return DFA with start state and all DFA states
		*/
		Set<State> startClosure = epsilonClosure(Set.of(nfa.startState));
		DfaState startDfaState = new DfaState(startClosure);
		List<DfaState> dfaStates = new java.util.ArrayList<>();
		dfaStates.add(startDfaState);
		Set<DfaState> unmarkedStates = new HashSet<>();
		unmarkedStates.add(startDfaState);
		while (!unmarkedStates.isEmpty()) {
			DfaState currentDfaState = unmarkedStates.iterator().next();
			unmarkedStates.remove(currentDfaState);
			for (char symbol : alphabet) {
				Set<State> moveResult = move(currentDfaState.getNfaStates(), symbol);
				Set<State> targetNfaStates = epsilonClosure(moveResult);
				if (targetNfaStates.isEmpty()) {
					continue; // No epsilon transition for this symbol
				}
				DfaState targetDfaState = findDfaState(dfaStates, targetNfaStates);
				if (targetDfaState == null) {
					targetDfaState = new DfaState(targetNfaStates);
					dfaStates.add(targetDfaState);
					unmarkedStates.add(targetDfaState);
				}
				currentDfaState.addTransition(symbol, targetDfaState);
			}
		}
		return new DFA(startDfaState, dfaStates);
	}

	/**
	 * Computes the epsilon-closure of a set of NFA states.
	 * The epsilon-closure is the set of states reachable by epsilon (null) transitions.
	 *
	 * @param states The set of NFA states.
	 * @return The epsilon-closure of the input states.
	 */
	private static Set<State> epsilonClosure(Set<State> states) {
		/*
		Pseudocode:
		1. Initialize closure with input states
		2. Use stack to process states
		3. For each state, add all reachable states via epsilon transitions
		4. Return closure set
		*/
		Set<State> closure = new HashSet<>(states);
		Set<State> stack = new HashSet<>(states);
		while (!stack.isEmpty()) {
			Set<State> newStates = new HashSet<>();
			for (State state : stack) {
				newStates.addAll(state.getEpsilonTransitions());
			}
			newStates.removeAll(closure);
			closure.addAll(newStates);
			stack = newStates;
		}
		return closure;
	}

	/**
	 * Returns the set of states reachable from a set of NFA states by a given symbol.
	 *
	 * @param states The set of NFA states.
	 * @param symbol The input symbol.
	 * @return The set of reachable states.
	 */
	private static Set<State> move(Set<State> states, char symbol) {
		/*
		 Pseudocode:
		 1. For each state in input set:
			  - For each transition with given symbol:
				  - Add destination state to result set
		 2. Return result set
		*/
		Set<State> result = new HashSet<>();
		for (State state : states) {
			Set<State> transitions = new HashSet<>();
			transitions.addAll(state.getTransitions(symbol));
			for (State transition : transitions) {
				result.add(transition);
			}
		}
		return result;
	}

	/**
	 * Finds an existing DFA state representing a given set of NFA states.
	 *
	 * @param dfaStates The list of DFA states.
	 * @param targetNfaStates The set of NFA states to search for.
	 * @return The matching DFA state, or null if not found.
	 */
	private static DfaState findDfaState(List<DfaState> dfaStates, Set<State> targetNfaStates) {
	   /*
	    Pseudocode:
	    1. For each DFA state in list:
		    - If its NFA state set equals target set, return DFA state
	    2. If not found, return null
	   */
	   for (DfaState dfaState : dfaStates) {
		   if (dfaState.getNfaStates().equals(targetNfaStates)) {
			   return dfaState;
		   }
	   }
	   return null;
	}
}
