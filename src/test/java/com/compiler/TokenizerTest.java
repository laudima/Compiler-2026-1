
package com.compiler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.compiler.lexer.LexicalRule;
import com.compiler.lexer.Tokenizer;
import com.compiler.lexer.nfa.NfaCombiner;
import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.NfaToDfaConverter;
import com.compiler.lexer.DfaMinimizer;
import java.util.*;

public class TokenizerTest {

    private static Tokenizer buildTokenizer() {
        List<LexicalRule> rules = Arrays.asList(
            new LexicalRule("abc(abc)*", "IDENTIFIER#0"),
            new LexicalRule("(012)+", "NUMBER#1")
        );
        NFA nfa = NfaCombiner.combine(rules);
        Set<Character> alphabet = new HashSet<>(Arrays.asList('a','b','c','0','1','2'));
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(nfa, alphabet);
        dfa = DfaMinimizer.minimizeDfa(dfa, alphabet);
        return new Tokenizer(dfa);
    }

    @ParameterizedTest
    @CsvSource({
        // input, expectedTokens (type:lexeme|type:lexeme|...)
        "abc,IDENTIFIER#0:abc",
        "abcabc,IDENTIFIER#0:abcabc",
        "012,NUMBER#1:012",
        "012012,NUMBER#1:012012",
        "abc012,IDENTIFIER#0:abc|NUMBER#1:012",
        "abcabc012,IDENTIFIER#0:abcabc|NUMBER#1:012"
    })
    void testTokenizer(String input, String expectedTokens) {
        Tokenizer tokenizer = buildTokenizer();
        List<Tokenizer.Token> tokens = tokenizer.tokenize(input);
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Tokenizer.Token t = tokens.get(i);
            if (i > 0) actual.append("|");
            actual.append(t.type).append(":").append(t.lexeme);
        }
        assertEquals(expectedTokens, actual.toString());
    }

    @ParameterizedTest
    @CsvSource({
        // input with whitespaces, expected tokens
        "abc abcabc 012,IDENTIFIER#0:abc|IDENTIFIER#0:abcabc|NUMBER#1:012",
        "  abc   012012   ,IDENTIFIER#0:abc|NUMBER#1:012012"
    })
    void testTokenizerIgnoresWhitespaces(String input, String expectedTokens) {
        Tokenizer tokenizer = buildTokenizer();
        // Elimina espacios antes de tokenizar
        List<Tokenizer.Token> tokens = tokenizer.tokenize(input);
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Tokenizer.Token t = tokens.get(i);
            if (i > 0) actual.append("|");
            actual.append(t.type).append(":").append(t.lexeme);
        }
        assertEquals(expectedTokens, actual.toString());
    }

    @ParameterizedTest
    @CsvSource({
        // input with comments, expected tokens
        "abc abcabc /*block*/ 012,IDENTIFIER#0:abc|IDENTIFIER#0:abcabc|NUMBER#1:012",
        "012 abc //abcabc,NUMBER#1:012|IDENTIFIER#0:abc"
    })
    void testTokenizerIgnoresComments(String input, String expectedTokens) {
        Tokenizer tokenizer = buildTokenizer();
        List<Tokenizer.Token> tokens = tokenizer.tokenize(input);
        StringBuilder actual = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            Tokenizer.Token t = tokens.get(i);
            if (i > 0) actual.append("|");
            actual.append(t.type).append(":").append(t.lexeme);
        }
        assertEquals(expectedTokens, actual.toString());
    }
}
