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
        try (java.util.stream.Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                 .map(line -> line.split(";", 2))
                 .filter(parts -> parts.length == 2)
                 .forEach(parts -> {
                     String regex = parts[0].trim();
                     String tokenTypeName = parts[1].trim();
                     NFA nfa = buildNfaFromRegex(regex);
                     nfa.endState.setFinal(tokenTypeName);
                     nfas.add(nfa);
                 });
        }
        return nfas;
    }
}