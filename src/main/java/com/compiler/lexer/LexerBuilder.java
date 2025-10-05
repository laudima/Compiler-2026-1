package com.compiler.lexer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.compiler.lexer.nfa.NFA;

public class LexerBuilder {
    /**
     * Construye un NFA a partir de un regex usando RegexParser.
     * @param regex Expresión regular en notación infija
     * @return NFA construido
     */
    public static NFA buildNfaFromRegex(String regex) {
        com.compiler.lexer.regex.RegexParser parser = new com.compiler.lexer.regex.RegexParser();
        return parser.parse(regex);
    }

    /**
     * Lee un archivo de definiciones de tokens y construye una lista de NFAs.
     * Cada línea del archivo debe tener el formato: regex;TokenType
     * Las líneas vacías o que comienzan con '#' son ignoradas.
     * @param filePath Ruta al archivo de definiciones de tokens
     * @return Lista de NFAs construidos
     * @throws Exception Si ocurre un error al leer el archivo
     */
    public static List<NFA> buildNfasFromFile(String filePath) throws Exception {
        List<NFA> nfas = new ArrayList<>();
        List<String> allLines = Files.readAllLines(Paths.get(filePath));
        List<String> tokenLines = new ArrayList<>();
        for (String line : allLines) {
            if (line == null) continue;
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;
            tokenLines.add(t);
        }

        // Assign priorities so that later lines have higher precedence (lower numeric priority)
        int total = tokenLines.size();
        for (int idx = 0; idx < total; idx++) {
            String line = tokenLines.get(idx);
            String[] parts = line.split(";", 2);
            if (parts.length != 2) continue;
            String regex = parts[0].trim();
            String tokenTypeName = parts[1].trim();
            NFA nfa = buildNfaFromRegex(regex);
            int priority = total - idx - 1; // last line gets 0
            nfa.endState.setFinal(tokenTypeName, priority);
            nfas.add(nfa);
        }
        return nfas;
    }
}