package com.compiler.lexer.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // TODO: Implement constructor if needed
        
    }

    /**
     * Inserts the explicit concatenation operator ('.') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        /*
            Pseudocode:
            For each character in regex:
                - Append current character to output
                - If not at end of string:
                    - Check if current and next character form an implicit concatenation
                    - If so, append '·' to output
            Return output as string
        */
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char current = regex.charAt(i);
            output.append(current); // Append current character
            
            if (i < regex.length() - 1) { // If not at end of string
                char next = regex.charAt(i + 1);
                
                // Check if we need to insert concatenation operator
                if (needsConcatenation(current, next)) {
                    output.append('.');
                }
            }
        }
        return output.toString();
    }

    private static boolean needsConcatenation(char current, char next) {
        // Cases where we need concatenation:
        // 1. operand followed by operand (e.g., "ab")
        // 2. operand followed by opening parenthesis (e.g., "a(")
        // 3. closing parenthesis followed by operand (e.g., ")a")
        // 4. closing parenthesis followed by opening parenthesis (e.g., ")(")
        // 5. postfix operator (*,+,?) followed by operand (e.g., "a*b")
        // 6. postfix operator followed by opening parenthesis (e.g., "a*(")
        
        return (isOperand(current) && isOperand(next)) ||
            (isOperand(current) && next == '(') ||
            (current == ')' && isOperand(next)) ||
            (current == ')' && next == '(') ||
            (isPostfixOperator(current) && isOperand(next)) ||
            (isPostfixOperator(current) && next == '(');
    }

    private static boolean isPostfixOperator(char c) {
        return c == '*' || c == '+' || c == '?';
    }
    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        /*
        Pseudocode:
        Return true if c is not one of: '|', '*', '?', '+', '(', ')', '.'
         */
        return ".|*+?()".indexOf(c) == -1; // Looks if the char is in the string of operators 
    }

    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        // TODO: Implement toPostfix
        /*
        Pseudocode:
        1. Define operator precedence map

        +---+----------------------------------------------------------+
        |   |             ERE Precedence (from high to low)            |
        +---+----------------------------------------------------------+
        | 1 | Collation-related bracket symbols | [==] [::] [..]       |
        | 2 | Escaped characters                | \<special character> |
        | 3 | Bracket expression                | []                   |
        | 4 | Grouping                          | ()                   |
        | 5 | Single-character-ERE duplication  | * + ? {m,n}          |
        | 6 | Concatenation                     |                      |
        | 7 | Anchoring                         | ^ $                  |
        | 8 | Alternation                       | |                    |
        +---+-----------------------------------+----------------------+

        In this case our regex only uses the following operators: '|', '*', '?', '+', '(', ')', '·'.

        
        2. Preprocess regex to insert explicit concatenation operators
        3. For each character in regex:
            - If operand: append to output
            - If '(': push to stack
            - If ')': pop operators to output until '(' is found
            - If operator: pop operators with higher/equal precedence, then push current operator
        4. After loop, pop remaining operators to output
        5. Return output as string
         */



        Map<Character, Integer> precedence = new HashMap<>();
        precedence.put('|', 1); // Alternation
        precedence.put('.', 2); // Concatenation
        precedence.put('*', 3); // Kleene Star
        precedence.put('?', 3); // Optional
        precedence.put('+', 3); // One or more
        precedence.put('(', 0); // Parentheses have lowest precedence
        precedence.put(')', 0); // Parentheses have lowest precedence

        String regex; 
        regex = insertConcatenationOperator(infixRegex);

        // Initialize the output and operator stacks
        StringBuilder output = new StringBuilder();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < regex.length(); i++) {
            char current = regex.charAt(i);

            if (isOperand(current)) {
                // If operand add to output
                output.append(current);
            } else if (current == '(') {
                // If '(', push to stack
                operators.push(current);
            } else if (current == ')') {
                // If ')', pop until '(' is found
                while (!operators.isEmpty() && operators.peek() != '(') {
                    output.append(operators.pop());
                }
                if (!operators.isEmpty()) {
                    operators.pop(); // Pop '('
                }
            } else {
                // If operator, pop higher/equal precedence operators
                while (!operators.isEmpty() && operators.peek() != '(' && 
                      precedence.get(current) <= precedence.get(operators.peek())) {
                    output.append(operators.pop());
                }
                operators.push(current);
            }
        }

        // Pop remaining operators
        while (!operators.isEmpty()) {
            output.append(operators.pop());
        }

        return output.toString();
    }

    // test
    public static void main(String[] args) {
        String concat = "ab*c";
        System.out.println("Original: " + concat);
        concat = insertConcatenationOperator(concat);
        System.out.println("After concatenation: " + concat);
        String postfix = toPostfix(concat);
        System.out.println("Postfix: " + postfix);

    }
}
