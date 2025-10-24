package com.compiler.lexer.regex;

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
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        // TODO: Implement insertConcatenationOperator
        /*
            Pseudocode:
            For each character in regex:
                - Append current character to output
                - If not at end of string:
                        - Check if current and next character form an implicit concatenation
                        - If so, append '·' to output
            Return output as string
         */
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        // TODO: Implement isOperand
        /*
        Pseudocode:
        Return true if c is not one of: '|', '*', '?', '+', '(', ')', '·'
         */
        throw new UnsupportedOperationException("Not implemented");
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

}
