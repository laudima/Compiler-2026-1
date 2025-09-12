package com.compiler.lexer;

public class LexicalRule {
    public final String regex;
    public final String tokenType;

    public LexicalRule(String regex, String tokenType) {
        this.regex = regex;
        this.tokenType = tokenType;
    }
}
