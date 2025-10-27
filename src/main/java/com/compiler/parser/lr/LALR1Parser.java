package com.compiler.parser.lr;

import java.util.List;

import com.compiler.lexer.Token;

/**
 * Implements the LALR(1) parsing engine.
 * Uses a stack and the LALR(1) table to process a sequence of tokens.
 * Complementary task for Practice 9.
 */
public class LALR1Parser {
    private final LALR1Table table;

    public LALR1Parser(LALR1Table table) {
        this.table = table;
    }

   // package-private accessor for tests
   LALR1Table getTable() {
       return table;
   }

   /**
    * Parses a sequence of tokens using the LALR(1) parsing algorithm.
    * @param tokens The list of tokens from the lexer.
    * @return true if the sequence is accepted, false if a syntax error is found.
    */
   public boolean parse(List<Token> tokens) {
        // 1. Initialize a stack for states and push the initial state (from table.getInitialState()).
        // 2. Create a mutable list of input tokens from the parameter and add the end-of-input token ("$").
        // 3. Initialize an instruction pointer `ip` to 0, pointing to the first token.
        // 4. Start a loop that runs until an ACCEPT or ERROR condition is met.
        //    a. Get the current state from the top of the stack.
        //    b. Get the current token `a` from the input list at index `ip`.
        //    c. Look up the action in the ACTION table: action = table.getActionTable()[state][a.type].
        //    d. If no action is found (it's null), it's a syntax error. Return false.
        //    e. If the action is SHIFT(s'):
        //       i. Push the new state s' onto the stack.
        //       ii. Advance the input pointer: ip++.
        //    f. If the action is REDUCE(A -> β):
        //       i. Pop |β| symbols (and states) from the stack. Handle epsilon productions (where |β|=0).
        //       ii. Get the new state `s` from the top of the stack.
        //       iii. Look up the GOTO state: goto_state = table.getGotoTable()[s][A].
        //       iv. If no GOTO state is found, it's an error. Return false.
        //       v. Push the goto_state onto the stack.
        //    g. If the action is ACCEPT:
        //       i. The input has been parsed successfully. Return true.
        //    h. If the action is none of the above, it's an unhandled case or error. Return false.

        // 1. Initialize a stack for states and push the initial state (from table.getInitialState()).
        java.util.Stack<Integer> stateStack = new java.util.Stack<>();
        stateStack.push(table.getInitialState());

        // 2. Create a mutable list of input tokens from the parameter and add the end-of-input token ("$").
        java.util.List<Token> inputTokens = new java.util.ArrayList<>(tokens);
        inputTokens.add(new Token("$", "$"));

        // 3. Initialize an instruction pointer `ip` to 0, pointing to the first token.
        int ip = 0;

        // 4. Start a loop that runs until an ACCEPT or ERROR condition is met.
        while (true) {
            // 4a. Get the current state from the top of the stack.
            int state = stateStack.peek();

            // 4b. Get the current token `a` from the input list at index `ip`.
            Token a = inputTokens.get(ip);

            // 4c. Look up the action in the ACTION table: action = table.getActionTable()[state][a.type].
            LALR1Table.Action action = table.getActionTable().get(state) != null ? table.getActionTable().get(state).get(new com.compiler.parser.grammar.Symbol(a.type, com.compiler.parser.grammar.SymbolType.TERMINAL)) : null;
            com.compiler.parser.grammar.Symbol terminalSymbol = new com.compiler.parser.grammar.Symbol(a.type, com.compiler.parser.grammar.SymbolType.TERMINAL);
            
            if (table.getActionTable().containsKey(state)) {
                action = table.getActionTable().get(state).get(terminalSymbol);
            }

            // 4d. If no action is found (it's null), it's a syntax error. Return false.
            if (action == null) {
                return false;
            }

            // 4e. If the action is SHIFT(s'):
            if (action.type == LALR1Table.Action.Type.SHIFT) {
                // 4e-i. Push the new state s' onto the stack.
                stateStack.push(action.state);
                
                // 4e-ii. Advance the input pointer: ip++.
                ip++;
            }
            // 4f. If the action is REDUCE(A -> β):
            else if (action.type == LALR1Table.Action.Type.REDUCE) {
                com.compiler.parser.grammar.Production production = action.reduceProd;
                
                // 4f-i. Pop |β| symbols (and states) from the stack. Handle epsilon productions (where |β|=0).
                int betaLength = production.right.size();
                
                // Handle epsilon productions - check if the right side contains only epsilon
                if (betaLength == 1 && production.right.get(0).name.equals("ε")) {
                    betaLength = 0;
                }
                
                for (int i = 0; i < betaLength; i++) {
                    stateStack.pop();
                }

                // 4f-ii. Get the new state `s` from the top of the stack.
                int s = stateStack.peek();

                // 4f-iii. Look up the GOTO state: goto_state = table.getGotoTable()[s][A].
                com.compiler.parser.grammar.Symbol A = production.left;
                Integer gotoState = null;
                
                if (table.getGotoTable().containsKey(s)) {
                    gotoState = table.getGotoTable().get(s).get(A);
                }

                // 4f-iv. If no GOTO state is found, it's an error. Return false.
                if (gotoState == null) {
                    return false;
                }

                // 4f-v. Push the goto_state onto the stack.
                stateStack.push(gotoState);
            }
            // 4g. If the action is ACCEPT:
            else if (action.type == LALR1Table.Action.Type.ACCEPT) {
                // 4g-i. The input has been parsed successfully. Return true.
                return true;
            }
            // 4h. If the action is none of the above, it's an unhandled case or error. Return false.
            else {
                return false;
            }
        }
   }
}
