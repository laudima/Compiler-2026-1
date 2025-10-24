package com.compiler.parser.syntax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        /*
         * Pseudocode for FIRST set calculation:
         *
         * 1. For each symbol S in grammar:
         *      - If S is a terminal, FIRST(S) = {S}
         *      - If S is a non-terminal, FIRST(S) = {}
         *
         * 2. Repeat until no changes:
         *      For each production A -> X1 X2 ... Xn:
         *          - For each symbol Xi in the right-hand side:
         *              a. Add FIRST(Xi) - {ε} to FIRST(A)
         *              b. If ε is in FIRST(Xi), continue to next Xi
         *                 Otherwise, break
         *          - If ε is in FIRST(Xi) for all i, add ε to FIRST(A)
         *
         * 3. Return the map of FIRST sets for all symbols.
         */

        for (Symbol symbol : grammar.getTerminals()) {
            firstSets.put(symbol, Set.of(symbol));
        }

        for (Symbol symbol : grammar.getNonTerminals()) {
            firstSets.put(symbol, new HashSet<>());
        }

        // Add ε to FIRST(ε) if not already present
        if (!firstSets.containsKey(Symbol.EPSILON)) {
            firstSets.put(Symbol.EPSILON, Set.of(Symbol.EPSILON));
        }

        boolean changed;
        do {
            changed = false;
            for (var production : grammar.getProductions()) {
                Symbol A = production.getLeft();
                var rhs = production.getRight();
                var firstA = firstSets.get(A);
                
                if (firstA == null) {
                    System.err.println("Warning: FIRST set for " + A + " is null. Skipping production.");
                    continue;
                }
                
                int initialSize = firstA.size();

                // Special case: empty production
                if (rhs.isEmpty()) {
                    firstA.add(Symbol.EPSILON);
                    if (firstA.size() > initialSize) {
                        changed = true;
                    }
                    continue;
                }

                boolean allNullable = true;
                for (Symbol Xi : rhs) {
                    // Special case: epsilon symbol
                    if (Xi.equals(Symbol.EPSILON)) {
                        firstA.add(Symbol.EPSILON);
                        continue;
                    }
                    
                    var firstXi = firstSets.get(Xi);
                    if (firstXi == null) {
                        // Symbol not recognized - could be a grammar error
                        System.err.println("Warning: Symbol " + Xi + " not found in grammar. Treating as non-nullable terminal.");
                        firstSets.put(Xi, Set.of(Xi)); 
                        firstXi = firstSets.get(Xi);
                    }

                    // Add FIRST(Xi) - {ε} to FIRST(A)
                    for (Symbol sym : firstXi) {
                        if (!sym.equals(Symbol.EPSILON)) {
                            firstA.add(sym);
                        }
                    }

                    // If ε is not in FIRST(Xi), we cannot continue
                    if (!firstXi.contains(Symbol.EPSILON)) {
                        allNullable = false;
                        break;
                    }
                }
                if (allNullable) {
                    firstA.add(Symbol.EPSILON);
                }

                if (firstA.size() > initialSize) {
                    changed = true;
                }
            }
        } while (changed);

        return firstSets;
    }        

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        /*
         * Pseudocode for FOLLOW set calculation:
         *
         * 1. For each non-terminal A, FOLLOW(A) = {}
         * 2. Add $ (end of input) to FOLLOW(S), where S is the start symbol
         *
         * 3. Repeat until no changes:
         *      For each production B -> X1 X2 ... Xn:
         *          For each Xi (where Xi is a non-terminal):
         *              a. For each symbol Xj after Xi (i < j <= n):
         *                  - Add FIRST(Xj) - {ε} to FOLLOW(Xi)
         *                  - If ε is in FIRST(Xj), continue to next Xj
         *                    Otherwise, break
         *              b. If ε is in FIRST(Xj) for all j > i, add FOLLOW(B) to FOLLOW(Xi)
         *
         * 4. Return the map of FOLLOW sets for all non-terminals.
         *
         * Note: This method should call getFirstSets() first to obtain FIRST sets.
         */

        getFirstSets(); // Ensure FIRST sets are calculated
        for (Symbol symbol : grammar.getNonTerminals()) {
            followSets.put(symbol, new java.util.HashSet<>()); // empty set for non-terminals
        }
        // Add $ to FOLLOW of start symbol
        Symbol startSymbol = grammar.getProductions().get(0).getLeft();
        followSets.get(startSymbol).add(new Symbol("$", SymbolType.TERMINAL));


        boolean changed;
        do {
            changed = false;
            for (var production : grammar.getProductions()) {
                Symbol B = production.getLeft();
                var rhs = production.getRight();
                for (int i = 0; i < rhs.size(); i++) {
                    Symbol Xi = rhs.get(i);
                    if (Xi.type == SymbolType.NON_TERMINAL) {
                        var followXi = followSets.get(Xi);
                        int initialSize = followXi.size();

                        boolean allNullable = true;
                        for (int j = i + 1; j < rhs.size(); j++) {
                            Symbol Xj = rhs.get(j);
                            var firstXj = firstSets.get(Xj);
                            // Add FIRST(Xj) - {ε} to FOLLOW(Xi)
                            for (Symbol sym : firstXj) {
                                if (!sym.equals(Symbol.EPSILON)) {
                                    followXi.add(sym);
                                }
                            }
                            if (!firstXj.contains(Symbol.EPSILON)) {
                                allNullable = false;
                                break;
                            }
                        }
                        if (allNullable) {
                            // Add FOLLOW(B) to FOLLOW(Xi)
                            followXi.addAll(followSets.get(B));
                        }

                        if (followXi.size() > initialSize) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);
        return followSets;
    }
}