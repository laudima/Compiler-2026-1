package com.compiler.lexer;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tokenizer {
    private final DFA dfa;


    public static class Token {
        public final String type;
        public final String lexeme;
        public Token(String type, String lexeme) {
            this.type = type;
            this.lexeme = lexeme;
        }
        @Override
        public String toString() {
            return "<" + type + ", '" + lexeme + "'>";
        }
    }

    public Tokenizer(DFA dfa) {
        this.dfa = dfa;
    }

    /**
     * Removes comments from the input string.
     * Supports line comments (//...) and block comments (/* ... *​/).
     * @param input Input string
     * @return String without comments
     */
    private String removeComments(String input) {
        input = input.replaceAll("/\\*.*?\\*/", "");
        input = input.replaceAll("//.*", "");
        return input;
    }

    /**
     * Splits the input by whitespace.
     * @param input Input string
     * @return List of lexemes separated by whitespace
     */
    public static List<String> separateByWhitespace(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Arrays.asList();
        }
        
        return Arrays.asList(input.trim().split("\\s+"));
    }

    /**
     * Tokenizes the input using the maximal munch rule.
     * Whitespace separates lexemes, but is not removed.
     * @param input Input string
     * @return List of recognized tokens
     */
    public List<Token> tokenize(String input) {
        input = removeComments(input);
        List<String> lexemes = separateByWhitespace(input);
        List<Token> tokens = new ArrayList<>();

    // Tokenize each lexeme
        for (String lexeme : lexemes) {
            int pos = 0;
            int lexLen = lexeme.length();
            while (pos < lexLen) {
                DfaState state = dfa.startState;
                int k = pos;
                DfaState lastFinal = null;
                int lastFinalPos = pos;
                // TODO: use the already implemented DfaSimulator here
                while (k < lexLen && state != null) {
                    state = state.getTransition(lexeme.charAt(k));
                    if (state != null && state.isFinal && state.tokenType != null) {
                        lastFinal = state;
                        lastFinalPos = k + 1;
                    }
                    k++;
                }
                if (lastFinal != null) {
                    tokens.add(new Token(lastFinal.tokenType, lexeme.substring(pos, lastFinalPos)));
                    pos = lastFinalPos;
                } else {
                    // No match: skip one character or throw error
                    pos++;
                }
            }
        }
        return tokens;
    }

}
