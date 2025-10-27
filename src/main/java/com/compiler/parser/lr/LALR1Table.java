package com.compiler.parser.lr;

import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;


/**
 * Builds the LALR(1) parsing table (ACTION/GOTO).
 * Main task for Practice 9.
 */
public class LALR1Table {
    private final LR1Automaton automaton;

    // merged LALR states and transitions
    private java.util.List<java.util.Set<LR1Item>> lalrStates = new java.util.ArrayList<>();
    private java.util.Map<Integer, java.util.Map<Symbol, Integer>> lalrTransitions = new java.util.HashMap<>();
    
    // ACTION table: state -> terminal -> Action
    public static class Action {
        public enum Type { SHIFT, REDUCE, ACCEPT }
        public final Type type;
        public final Integer state; // for SHIFT
        public final com.compiler.parser.grammar.Production reduceProd; // for REDUCE

        private Action(Type type, Integer state, com.compiler.parser.grammar.Production prod) {
            this.type = type; this.state = state; this.reduceProd = prod;
        }

        public static Action shift(int s) { return new Action(Type.SHIFT, s, null); }
        public static Action reduce(com.compiler.parser.grammar.Production p) { return new Action(Type.REDUCE, null, p); }
        public static Action accept() { return new Action(Type.ACCEPT, null, null); }
    }

    private final java.util.Map<Integer, java.util.Map<Symbol, Action>> action = new java.util.HashMap<>();
    private final java.util.Map<Integer, java.util.Map<Symbol, Integer>> gotoTable = new java.util.HashMap<>();
    private final java.util.List<String> conflicts = new java.util.ArrayList<>();
    private int initialState = 0;

    public LALR1Table(LR1Automaton automaton) {
        this.automaton = automaton;
    }

    /**
     * Prints the LALR(1) transitions in a readable format.
     */
    public void printLALRTransitions() {
        System.out.println("\n=== LALR(1) TRANSITIONS ===");
        
        if (lalrTransitions.isEmpty()) {
            System.out.println("No transitions found.");
            return;
        }
        
        for (java.util.Map.Entry<Integer, java.util.Map<Symbol, Integer>> entry : lalrTransitions.entrySet()) {
            int sourceState = entry.getKey();
            java.util.Map<Symbol, Integer> transitions = entry.getValue();
            
            System.out.println("State " + sourceState + ":");
            for (java.util.Map.Entry<Symbol, Integer> transition : transitions.entrySet()) {
                Symbol symbol = transition.getKey();
                int targetState = transition.getValue();
                System.out.println("  " + symbol.name + " -> " + targetState);
            }
        }
        System.out.println();
    }

    /**
     * Prints all LALR(1) states in a readable format.
     */
    public void printLALRStates() {
        System.out.println("\n=== LALR(1) STATES ===");
        
        if (lalrStates.isEmpty()) {
            System.out.println("No LALR states found. Build the table first.");
            return;
        }
        
        for (int i = 0; i < lalrStates.size(); i++) {
            System.out.println("State " + i + ":");
            java.util.Set<LR1Item> state = lalrStates.get(i);
            
            for (LR1Item item : state) {
                System.out.println("  " + item.toString());
            }
            System.out.println();
        }
    }
    /**
     * Builds the LALR(1) parsing table.
     */
    public void build() {

        // This is a multi-step process.
        
        // Step 1: Ensure the underlying LR(1) automaton is built.
        automaton.build();
        automaton.printStatesAndTransitions();

        // Step 2: Merge LR(1) states to create LALR(1) states.
        //  a. Group LR(1) states that have the same "kernel" (the set of LR(0) items).
        //     - A kernel item is an LR(1) item without its lookahead.
        //     - Create a map from a kernel (Set<KernelEntry>) to a list of state IDs that share that kernel.
        //  b. For each group of states with the same kernel:
        //     - Create a single new LALR(1) state.
        //     - This new state is formed by merging the LR(1) items from all states in the group.
        //     - Merging means for each kernel item, the new lookahead set is the union of all lookaheads for that item across the group.
        //     - Store these new LALR states in `lalrStates`.
        //  c. Create a mapping from old LR(1) state IDs to new LALR(1) state IDs.

        // Step 3: Build the transitions for the new LALR(1) automaton.
        //  - For each transition in the original LR(1) automaton `s -X-> t`:
        //  - Add a new transition for the LALR automaton: `merged(s) -X-> merged(t)`.
        //  - Use the mapping from step 2c to find the merged state IDs.
        //  - Store these new transitions in `lalrTransitions`.

        // Step 4: Fill the ACTION and GOTO tables based on the LALR automaton.
        //  - Call a helper method, e.g., `fillActionGoto()`.

        // Step 2a: Create a map from a kernel (Set<KernelEntry>) to a list of state IDs that share that kernel.
        java.util.Map<java.util.Set<KernelEntry>, java.util.List<Integer>> kernelToStates = new java.util.HashMap<>(); // kernel -> id (integer)
        java.util.List<java.util.Set<LR1Item>> lr1States = automaton.getStates(); // get the LR(1) states
        
        for (int i = 0; i < lr1States.size(); i++) {
            java.util.Set<LR1Item> state = lr1States.get(i);
            java.util.Set<KernelEntry> kernel = new java.util.HashSet<>();
            for (LR1Item item : state) {
                kernel.add(new KernelEntry(item.production, item.dotPosition));
            }
            kernelToStates.computeIfAbsent(kernel, k -> new java.util.ArrayList<>()).add(i);
        }

        // Step 2b: For each group of states with the same kernel, create a single new LALR(1) state.
        // Step 2c: Create a mapping from old LR(1) state IDs to new LALR(1) state IDs.
        java.util.Map<Integer, Integer> lr1ToLalr = new java.util.HashMap<>();
        
        // First, find which kernel contains the initial state (LR(1) state 0)
        java.util.Set<KernelEntry> initialKernel = null;
        for (java.util.Map.Entry<java.util.Set<KernelEntry>, java.util.List<Integer>> entry : kernelToStates.entrySet()) {
            if (entry.getValue().contains(0)) {
                initialKernel = entry.getKey();
                break;
            }
        }
        
        // Process the initial kernel first to ensure it becomes LALR state 0
        int lalrStateId = 0;
        if (initialKernel != null) {
            java.util.List<Integer> stateGroup = kernelToStates.get(initialKernel);
            
            // Merge items: union lookaheads for each kernel item
            java.util.Map<KernelEntry, java.util.Set<Symbol>> kernelToLookaheads = new java.util.HashMap<>();
            
            for (Integer stateId : stateGroup) {
                for (LR1Item item : lr1States.get(stateId)) {
                    KernelEntry ke = new KernelEntry(item.production, item.dotPosition);
                    kernelToLookaheads.computeIfAbsent(ke, k -> new java.util.HashSet<>()).add(item.lookahead);
                }
            }

            // Create merged LALR state
            java.util.Set<LR1Item> lalrState = new java.util.HashSet<>();
            for (java.util.Map.Entry<KernelEntry, java.util.Set<Symbol>> entry : kernelToLookaheads.entrySet()) {
                KernelEntry ke = entry.getKey();
                for (Symbol lookahead : entry.getValue()) {
                    lalrState.add(new LR1Item(ke.production, ke.dotPosition, lookahead));
                }
            }

            lalrStates.add(lalrState);

            // Map all old LR(1) states in this group to the new LALR state
            for (Integer oldStateId : stateGroup) {
                lr1ToLalr.put(oldStateId, lalrStateId);
            }

            lalrStateId++;
        }
        
        // Now process the remaining kernels
        for (java.util.Map.Entry<java.util.Set<KernelEntry>, java.util.List<Integer>> kernelEntry : kernelToStates.entrySet()) {
            java.util.Set<KernelEntry> kernel = kernelEntry.getKey();
            
            // Skip the initial kernel since we already processed it
            if (kernel.equals(initialKernel)) {
                continue;
            }
            
            java.util.List<Integer> stateGroup = kernelEntry.getValue();
            
            // Merge items: union lookaheads for each kernel item
            java.util.Map<KernelEntry, java.util.Set<Symbol>> kernelToLookaheads = new java.util.HashMap<>();
            
            for (Integer stateId : stateGroup) {
                for (LR1Item item : lr1States.get(stateId)) {
                    KernelEntry ke = new KernelEntry(item.production, item.dotPosition);
                    kernelToLookaheads.computeIfAbsent(ke, k -> new java.util.HashSet<>()).add(item.lookahead);
                }
            }

            // Create merged LALR state
            java.util.Set<LR1Item> lalrState = new java.util.HashSet<>();
            for (java.util.Map.Entry<KernelEntry, java.util.Set<Symbol>> entry : kernelToLookaheads.entrySet()) {
                KernelEntry ke = entry.getKey();
                for (Symbol lookahead : entry.getValue()) {
                    lalrState.add(new LR1Item(ke.production, ke.dotPosition, lookahead));
                }
            }

            lalrStates.add(lalrState);

            // Map all old LR(1) states in this group to the new LALR state
            for (Integer oldStateId : stateGroup) {
                lr1ToLalr.put(oldStateId, lalrStateId);
            }

            lalrStateId++;
        }

        // Step 3: Build the transitions for the new LALR(1) automaton.
        java.util.Map<Integer, java.util.Map<Symbol, Integer>> lr1Transitions = automaton.getTransitions();
        
        for (java.util.Map.Entry<Integer, java.util.Map<Symbol, Integer>> entry : lr1Transitions.entrySet()) {
            int lr1Source = entry.getKey();
            int lalrSource = lr1ToLalr.get(lr1Source);

            for (java.util.Map.Entry<Symbol, Integer> trans : entry.getValue().entrySet()) {
                Symbol symbol = trans.getKey();
                int lr1Target = trans.getValue();
                int lalrTarget = lr1ToLalr.get(lr1Target);

                lalrTransitions.computeIfAbsent(lalrSource, k -> new java.util.HashMap<>()).put(symbol, lalrTarget);
            }
        }

        printLALRStates();
        printLALRTransitions();

        // Step 4: Fill the ACTION and GOTO tables based on the LALR automaton.
        fillActionGoto();
    }

    private void fillActionGoto() {
        // 1. Clear the action, gotoTable, and conflicts lists.
        // 2. Iterate through each LALR state `s` from 0 to lalrStates.size() - 1.
        // 3. For each state `s`, iterate through its LR1Item `it`.
        //    a. Get the symbol after the dot, `X = it.getSymbolAfterDot()`.
        //    b. If `X` is a terminal (SHIFT action):
        //       - Find the destination state `t` from `lalrTransitions.get(s).get(X)`.
        //       - Check for conflicts: if action table already has an entry for `[s, X]`, it's a conflict.
        //       - Otherwise, set `action[s][X] = SHIFT(t)`.
        //    c. If the dot is at the end of the production (`X` is null) (REDUCE or ACCEPT action):
        //       - This is an item like `[A -> α •, a]`.
        //       - If it's the augmented start production (`S' -> S •`) and lookahead is `$`, this is an ACCEPT action.
        //         - Set `action[s][$] = ACCEPT`.
        //       - Otherwise, it's a REDUCE action.
        //         - For the lookahead symbol `a` in the item:
        //         - Check for conflicts: if `action[s][a]` is already filled, report a Shift/Reduce or Reduce/Reduce conflict.
        //         - Otherwise, set `action[s][a] = REDUCE(A -> α)`.
        // 4. Populate the GOTO table.
        //    - For each state `s`, look at its transitions in `lalrTransitions`.
        //    - For each transition on a NON-TERMINAL symbol `B` to state `t`:
        //    - Set `gotoTable[s][B] = t`.

        // 1. Clear the action, gotoTable, and conflicts lists.
        action.clear();
        gotoTable.clear();
        conflicts.clear();

        // Get augmented start symbol name
        String augmentedStart = automaton.getAugmentedLeftName();
        Symbol dollarSymbol = new Symbol("$", SymbolType.TERMINAL);

        // 2. Iterate through each LALR state `s` from 0 to lalrStates.size() - 1.
        for (int s = 0; s < lalrStates.size(); s++) {
            java.util.Set<LR1Item> state = lalrStates.get(s);

            // 3. For each state `s`, iterate through its LR1Item `it`.
            for (LR1Item it : state) {
                // 3a. Get the symbol after the dot, `X = it.getSymbolAfterDot()`.
                Symbol X = it.getSymbolAfterDot();

                // 3b. If `X` is a terminal (SHIFT action):
                if (X != null && X.type == SymbolType.TERMINAL) {
                    // Find the destination state `t` from `lalrTransitions.get(s).get(X)`.
                    Integer t = lalrTransitions.get(s) != null ? lalrTransitions.get(s).get(X) : null;
                    
                    if (t != null) {
                        // Check for conflicts: if action table already has an entry for `[s, X]`, it's a conflict.
                        Action existing = action.computeIfAbsent(s, k -> new java.util.HashMap<>()).get(X);
                        if (existing != null) {
                            conflicts.add("Shift/Reduce conflict at state " + s + " on terminal " + X.name);
                        } else {
                            // Otherwise, set `action[s][X] = SHIFT(t)`.
                            action.get(s).put(X, Action.shift(t));
                        }
                    }
                }
                // 3c. If the dot is at the end of the production (`X` is null) (REDUCE or ACCEPT action):
                else if (X == null) {
                    // This is an item like `[A -> α •, a]`.
                    // If it's the augmented start production (`S' -> S •`) and lookahead is `$`, this is an ACCEPT action.
                    if (augmentedStart != null && it.production.left.name.equals(augmentedStart) && it.lookahead.equals(dollarSymbol)) {
                        // Set `action[s][$] = ACCEPT`.
                        action.computeIfAbsent(s, k -> new java.util.HashMap<>()).put(dollarSymbol, Action.accept());
                    } else {
                        // Otherwise, it's a REDUCE action.
                        // For the lookahead symbol `a` in the item:
                        Symbol a = it.lookahead;
                        
                        // Check for conflicts: if `action[s][a]` is already filled, report a Shift/Reduce or Reduce/Reduce conflict.
                        Action existing = action.computeIfAbsent(s, k -> new java.util.HashMap<>()).get(a);
                        if (existing != null) {
                            if (existing.type == Action.Type.SHIFT) {
                                conflicts.add("Shift/Reduce conflict at state " + s + " on terminal " + a.name);
                            } else {
                                conflicts.add("Reduce/Reduce conflict at state " + s + " on terminal " + a.name);
                            }
                        } else {
                            // Otherwise, set `action[s][a] = REDUCE(A -> α)`.
                            action.get(s).put(a, Action.reduce(it.production));
                        }
                    }
                }
            }
        }

        // 4. Populate the GOTO table.
        // For each state `s`, look at its transitions in `lalrTransitions`.
        for (java.util.Map.Entry<Integer, java.util.Map<Symbol, Integer>> entry : lalrTransitions.entrySet()) {
            int s = entry.getKey();
            
            // For each transition on a NON-TERMINAL symbol `B` to state `t`:
            for (java.util.Map.Entry<Symbol, Integer> trans : entry.getValue().entrySet()) {
                Symbol B = trans.getKey();
                int t = trans.getValue();
                
                if (B.type == SymbolType.NON_TERMINAL) {
                    // Set `gotoTable[s][B] = t`.
                    gotoTable.computeIfAbsent(s, k -> new java.util.HashMap<>()).put(B, t);
                }
            }
        }
    }
    
    // ... (Getters and KernelEntry class can remain as is)
    public java.util.Map<Integer, java.util.Map<Symbol, Action>> getActionTable() { return action; }
    public java.util.Map<Integer, java.util.Map<Symbol, Integer>> getGotoTable() { return gotoTable; }
    
    // Print the action table
    public void printActionTable() {
        System.out.println("\n=== ACTION TABLE ===");
        
        // Get all terminals from the action table
        java.util.Set<Symbol> terminals = new java.util.TreeSet<>((a, b) -> a.name.compareTo(b.name));
        for (java.util.Map<Symbol, Action> stateActions : action.values()) {
            terminals.addAll(stateActions.keySet());
        }
        
        // Print header
        System.out.printf("%-8s", "State");
        for (Symbol terminal : terminals) {
            System.out.printf("%-12s", terminal.name);
        }
        System.out.println();
        
        // Print separator
        System.out.printf("%-8s", "--------");
        for (Symbol terminal : terminals) {
            System.out.printf("%-12s", "------------");
        }
        System.out.println();
        
        // Print each state's actions
        for (int state = 0; state < lalrStates.size(); state++) {
            System.out.printf("%-8d", state);
            
            for (Symbol terminal : terminals) {
                Action act = action.getOrDefault(state, new java.util.HashMap<>()).get(terminal);
                String actionStr = "";
                
                if (act != null) {
                    switch (act.type) {
                        case SHIFT:
                            actionStr = "shift " + act.state;
                            break;
                        case REDUCE:
                            actionStr = "reduce " + act.reduceProd.toString();
                            break;
                        case ACCEPT:
                            actionStr = "accept";
                            break;
                    }
                }
                
                System.out.printf("%-12s", actionStr);
            }
            System.out.println();
        }
        System.out.println();
    }

    // Print the GOTO table
    public void printGotoTable() {
        System.out.println("\n=== GOTO TABLE ===");
        
        // Get all non-terminals from the goto table
        java.util.Set<Symbol> nonTerminals = new java.util.TreeSet<>((a, b) -> a.name.compareTo(b.name));
        for (java.util.Map<Symbol, Integer> stateGotos : gotoTable.values()) {
            nonTerminals.addAll(stateGotos.keySet());
        }
        
        // Print header
        System.out.printf("%-8s", "State");
        for (Symbol nonTerminal : nonTerminals) {
            System.out.printf("%-12s", nonTerminal.name);
        }
        System.out.println();
        
        // Print separator
        System.out.printf("%-8s", "--------");
        for (Symbol nonTerminal : nonTerminals) {
            System.out.printf("%-12s", "------------");
        }
        System.out.println();
        
        // Print each state's goto entries
        for (int state = 0; state < lalrStates.size(); state++) {
            System.out.printf("%-8d", state);
            
            for (Symbol nonTerminal : nonTerminals) {
                Integer gotoState = gotoTable.getOrDefault(state, new java.util.HashMap<>()).get(nonTerminal);
                String gotoStr = gotoState != null ? gotoState.toString() : "";
                System.out.printf("%-12s", gotoStr);
            }
            System.out.println();
        }
        System.out.println();
    }

    public java.util.List<String> getConflicts() { return conflicts; }
    private static class KernelEntry {
        public final com.compiler.parser.grammar.Production production;
        public final int dotPosition;
        KernelEntry(com.compiler.parser.grammar.Production production, int dotPosition) {
            this.production = production;
            this.dotPosition = dotPosition;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof KernelEntry)) return false;
            KernelEntry o = (KernelEntry) obj;
            return dotPosition == o.dotPosition && production.equals(o.production);
        }
        @Override
        public int hashCode() {
            int r = production.hashCode();
            r = 31 * r + dotPosition;
            return r;
        }
    }
    public java.util.List<java.util.Set<LR1Item>> getLALRStates() { return lalrStates; }
    public java.util.Map<Integer, java.util.Map<Symbol, Integer>> getLALRTransitions() { return lalrTransitions; }
    public int getInitialState() { return initialState; }
}
