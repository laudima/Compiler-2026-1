package com.compiler.lexer.nfa;

import com.compiler.lexer.LexicalRule;
import com.compiler.lexer.regex.RegexParser;
import java.util.List;

/**
 * NfaCombiner
 * ---
 * Utility to combine multiple lexical rules into a single NFA.
 */
public class NfaCombiner {
    /**
     * Combines multiple lexical rules into a single NFA.
     *
     * @param rules List of lexical rules (regex, tokenType)
     * @return Combined NFA
     */
    public static NFA combine(List<LexicalRule> rules) {
        RegexParser parser = new RegexParser();
        State newStart = new State();
        for (LexicalRule rule : rules) {
            NFA nfa = parser.parse(rule.regex);
            nfa.endState.setFinal(true);
            nfa.endState.tokenType = rule.tokenType; 
            newStart.addTransition(null, nfa.startState);
        }
        // The combined NFA has the new state as start and does not have a single final state
        return new NFA(newStart, null);
    }
}
