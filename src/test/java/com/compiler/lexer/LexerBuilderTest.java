package com.compiler.lexer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.nfa.NFA;

public class LexerBuilderTest {
    @Test
    public void testBuildNfasFromFileAndDfaRecognition() throws Exception {
        // Prepara archivo de tokens temporal
        String tokensFile = "tokens_test.txt";
        List<String> lines = Arrays.asList(
            "(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|_)(a|b|c|d|e|f|g|h|i|j|k|l|m|n|o|p|q|r|s|t|u|v|w|x|y|z|A|B|C|D|E|F|G|H|I|J|K|L|M|N|O|P|Q|R|S|T|U|V|W|X|Y|Z|_|0|1|2|3|4|5|6|7|8|9)*;IDENTIFIER",
            "(0|1|2|3|4|5|6|7|8|9)+;NUMBER",
            "if|else|while;KEYWORD"
        );
        Files.write(Paths.get(tokensFile), lines);

        // Construye NFAs
        List<NFA> nfas = LexerBuilder.buildNfasFromFile(tokensFile);
        assertEquals(3, nfas.size());

        // Une NFAs y construye DFA
        NFA combinedNfa = NFA.union(nfas);
        Set<Character> alphabet = new HashSet<>();
        for (char c = '0'; c <= '9'; c++) alphabet.add(c);
        for (char c = 'a'; c <= 'z'; c++) alphabet.add(c);
        for (char c = 'A'; c <= 'Z'; c++) alphabet.add(c);
        alphabet.add('_');
        DFA dfa = NfaToDfaConverter.convertNfaToDfa(combinedNfa, alphabet);
        assertNotNull(dfa);

        // Simula reconocimiento simple: busca el tipo de token para "if" y "123"
        String input1 = "if";
        String input2 = "123";
        String input3 = "variable";
        assertEquals("KEYWORD", simulateDfa(dfa, input1));
        assertEquals("NUMBER", simulateDfa(dfa, input2));
        assertEquals("IDENTIFIER", simulateDfa(dfa, input3));

        // Limpia archivo temporal
        Files.deleteIfExists(Paths.get(tokensFile));
    }

    // Simula el DFA sobre la entrada y retorna el tokenTypeName del estado final alcanzado
    private String simulateDfa(DFA dfa, String input) {
        com.compiler.lexer.dfa.DfaState state = dfa.startState;
        for (char c : input.toCharArray()) {
            com.compiler.lexer.dfa.DfaState next = state.getTransition(c);
            if (next == null) return null;
            state = next;
        }
        return state.isFinal ? state.tokenTypeName : null;
    }
}
