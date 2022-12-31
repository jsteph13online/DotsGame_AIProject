/*
    CSC 480 AI: Fall 2022
    Assignment 1: 8-Puzzle (Modified)
    Joscelyn Stephens
 */

import java.util.*;

public class Solver {
    public final int[] EASY_START = {1,3,4,8,6,2,7,0,5};
    public final int[] MED_START = {2,8,1,0,4,3,7,6,5};
    public final int[] HARD_START = {5,6,7,4,0,8,3,2,1};
    public final int[] GOAL_STATE = {1,2,3,8,0,4,7,6,5};
    public int GOAL_HASH;

    private int problem; // selected problem
    private Alg algorithm; // algorithm we are using
    ResultsFrame display; // to display results

    private final boolean SHOW_REPORT_INFO = true;
    private final boolean SHOW_DEBUG = false;

    // cstr called at start of program
    Solver(ResultsFrame d){
        display = d; problem = 1; algorithm = Alg.BREADTH;
        GOAL_HASH = Arrays.hashCode(GOAL_STATE);
    }

    // returns problem init state as int[]
    //1=easy, 2=med, 3=hard, used for GUI return
    public int[] getProblem(int i){
        switch (i){
            case 1:
                return EASY_START;
            case 2:
                return MED_START;
            case 3:
                return HARD_START;
        }
        return EASY_START;
    }

    // start process of solving puzzle with given problem and algorithm
    public void StartSolving(int p, Alg a){
       problem = p; algorithm = a;
        // init the starting state based on the chosed problem
        PuzzleState reg = new PuzzleState(getProblem(problem), GOAL_STATE); // state w/out heuristics
        PuzzleState h1 = new PuzzleState(getProblem(problem), GOAL_STATE, true); // state including h1 heuristic
        PuzzleState h2 = new PuzzleState(getProblem(problem), GOAL_STATE, false); // state including h2 heuristic
        // Init a ROOT NODE for starting state
        Node ROOT = new Node(reg);
        Node ROOTH1 = new Node(h1);
        Node ROOTH2 = new Node(h2);
        //CALL SEARCH ALG with the root node
        switch(algorithm){
            case BREADTH -> BreadthSearch(ROOT);
            case GREEDY -> GreedySearch(ROOTH1);
            case UNIFORM -> UniformSearch(ROOT);
            case A1 -> A1Search(ROOTH1);
            case A2 -> A2Search(ROOTH2);
            default -> DepthSearch(ROOT);
        }
    }

    // Setup for Breadth Search, Called the PerformBreadthSearch(), then the ShowResults() once Solution is found
    private void BreadthSearch(Node ROOT){
        Queue<Node> frontier = new LinkedList<>(); // creation of search queue
        frontier.add(ROOT); // Add root to frontier
        List<Integer> explored = new ArrayList<>(); // creation of list to hold explored states
        //RUN SEARCH vv
        Stack<Node> solutionStack = PerformBreadthSearch(frontier, explored);
        // Call function to display Results
        ShowResults(solutionStack, "Breadth Search Algorithm");
    }

    // Meat of Breadth Search Algorithm (FIFO Queue Search)
    private Stack<Node> PerformBreadthSearch( Queue<Node> frontier, List<Integer> explored){
        System.out.println("Search Beginning");
        int maxQueue = 0; // for written report analysis
        int nodesOffQueue = 1; // for written report
        Stack<Node> solutionPath = new Stack<>();
        //TBD
        while(!frontier.isEmpty()) // While there are nodes in the frontier queue
        {
            Node n = frontier.poll();
            explored.add(n.getState().getHash());//add node state's hash code to explored list
            nodesOffQueue += 1;
            if(!n.getState().matchesStateHash(GOAL_HASH)) {
                //Create an array of states that are possible successors from our current state
                ArrayList<PuzzleState> possibleChildren = n.getState().findChildren(Alg.BREADTH);
                ArrayList<Node> front = new ArrayList<>(frontier);
                // iterate through the list of possible generated children
                for(int i=0; i<possibleChildren.size(); i++){
                    //1. Create a node holding the possible child
                    Node temp = new Node(n, possibleChildren.get(i).getAct(), possibleChildren.get(i),
                            n.getPathCost() + possibleChildren.get(i).getStepCost());
                    // if this node's state doesn't exist in frontier or explored lists,
                    // add it to the frontier
                    if(!CheckExplored(explored, front, temp)){
                        frontier.add(temp);
                        // if queue is a new MAX SIZE, save new MAX SIZE (for written report
                        if(maxQueue < frontier.size() && SHOW_REPORT_INFO)
                        { maxQueue = frontier.size();}
                    }
                }
            }
            else{
                System.out.println("Goal State Reached!");
                // Last in, First out stack creation to track steps from goal node back to root ancestor
                // This is the stack of Nodes that is returned by this function
                while(n.getParent() != null){
                    solutionPath.push(n);
                    n=n.getParent();
                }
                solutionPath.push(n); // final push adds ROOT node to solution stack
                if(SHOW_REPORT_INFO)System.out.println("Max Queue Size: " + maxQueue); // for written report analysis
                if(SHOW_REPORT_INFO)System.out.println("Nodes From Queue: " + nodesOffQueue); // for written report analysis
                return solutionPath;
            }
        }
        // This part of function is reached only if NO SOLUTION is found by the algorithm
        System.out.println("ERROR: NO SOLUTION FOUND!");
        if(SHOW_REPORT_INFO)System.out.println("Max Queue Size: " + maxQueue); // for written report analysis
        if(SHOW_REPORT_INFO)System.out.println("Nodes From Queue: " + nodesOffQueue); // for written report analysis
        display.AppendText("ERROR: NO SOLUTION FOUND\n");
        return solutionPath; // will return null stack
    }

    // Setup for Depth Search, Called the PerformDepthSearch(), then the ShowResults() once Solution is found
    private void DepthSearch(Node ROOT){
        Stack<Node> frontier = new Stack<Node>(); // creation of search Stack (LIFO for depth search)
        frontier.push(ROOT); // Add root to frontier
        List<Integer> explored = new ArrayList<Integer>(); // creation of list to hold explored states
        //RUN SEARCH vv
        Stack<Node> solutionStack = PerformDepthSearch(frontier, explored);
        // Call function to display Results
        ShowResults(solutionStack, "Depth Search Algorithm");
    }

    // Meat of the Depth-Search Function (FILO Stack)
    private Stack<Node> PerformDepthSearch(Stack<Node> frontier, List<Integer> explored){
        System.out.println("Search Beginning");
        int maxQueue = 0; // for written report analysis
        int nodesOffQueue = 1; // for written report
        Stack<Node> solutionPath = new Stack<Node>();
        //LOOP until the frontier stack is empty (or solution is found)
        while(!frontier.isEmpty()) // While there are nodes in the frontier queue
        {
            Node n = frontier.pop();
            explored.add(n.getState().getHash());//add node state's hash code to explored list
            nodesOffQueue += 1;
            if(!n.getState().matchesStateHash(GOAL_HASH)) {
                //Create an array of states that are possible successors from our current state
                ArrayList<PuzzleState> possibleChildren = n.getState().findChildren(Alg.DEPTH);
                ////if(SHOW_DEBUG)System.out.println("Possible Children Found: " + possibleChildren.size());
                // Only do the following if there were children nodes found
                if(possibleChildren.size()>0){
                    ArrayList<Node> front = new ArrayList<Node>(frontier);
                    ////if(SHOW_DEBUG)System.out.println("Frontier Size: " + frontier.size());
                    // iterate through the list of possible generated children
                    for(int i=0; i<possibleChildren.size(); i++){
                        //1. Create a node holding the possible child
                        Node temp = new Node(n, possibleChildren.get(i).getAct(), possibleChildren.get(i),
                                n.getPathCost() + possibleChildren.get(i).getStepCost());
                        // if this node's state doesn't exist in frontier or explored lists,
                        // add it to the frontier
                        if(!CheckExplored(explored, front, temp)){
                            frontier.push(temp);
                            //if(SHOW_DEBUG)System.out.println("Child Added to Frontier");
                            // if queue is a new MAX SIZE, save new MAX SIZE (for written report
                            if(SHOW_REPORT_INFO && maxQueue < frontier.size())
                            { maxQueue = frontier.size();}
                        }
                }}
            }
            else{ // GOAL STATE IS REACHED
                System.out.println("Goal State Reached!");
                // Last in, First out stack creation to track steps from goal node back to root ancestor
                // This is the stack of Nodes that is returned by this function
                while(n.getParent() != null){
                    solutionPath.push(n);
                    n=n.getParent();
                }
                solutionPath.push(n); // final push adds ROOT node to solution stack
                if(SHOW_REPORT_INFO)System.out.println("Max Queue Size: " + maxQueue); // for written report analysis
                if(SHOW_REPORT_INFO)System.out.println("Nodes From Queue: " + nodesOffQueue); // for written report analysis
                return solutionPath;
            }
            // LOOP BACK TO NEXT NODE IN FRONTIER ^
        }
        // This part of function is reached only if NO SOLUTION is found by the algorithm
        System.out.println("ERROR: NO SOLUTION FOUND!");
        if(SHOW_REPORT_INFO)System.out.println("Max Queue Size: " + maxQueue); // for written report analysis
        if(SHOW_REPORT_INFO)System.out.println("Nodes From Queue: " + nodesOffQueue); // for written report analysis
        display.AppendText("ERROR: NO SOLUTION FOUND\n");
        return solutionPath; // will return null stack
    }

    // Setup for Uniform Search, Called the PerformPQSearch(), then the ShowResults() once Solution is found
    private void UniformSearch(Node ROOT){
        PriorityQueue<Node> frontier = new PriorityQueue<Node>(1000, new Node.NodeCostComparator()); // creation of search queue
        frontier.add(ROOT); // Add root to frontier
        List<Integer> explored = new ArrayList<Integer>(); // creation of list to hold explored states
        //RUN SEARCH vv
        Stack<Node> solutionStack = PerformPQSearch(frontier, explored, Alg.UNIFORM);
        // Call function to display Results
        ShowResults(solutionStack, "Uniform Search Algorithm");
    }

    // Setup for Best-First Search, Called the PerformPQSearch(), then the ShowResults() once Solution is found
    private void GreedySearch(Node ROOT){
        PriorityQueue<Node> frontier = new PriorityQueue<Node>(1000, new Node.NodeComparator()); // creation of search queue
        frontier.add(ROOT); // Add root to frontier
        List<Integer> explored = new ArrayList<Integer>(); // creation of list to hold explored states
        //RUN SEARCH vv
        Stack<Node> solutionStack = PerformPQSearch(frontier, explored, Alg.GREEDY);
        // Call function to display Results
        ShowResults(solutionStack, "Greedy Best-First Search Algorithm");
    }

    // Setup for A1 Search, Called the PerformPQSearch(), then the ShowResults() once Solution is found
    private void A1Search(Node ROOT){
        if(SHOW_DEBUG)System.out.println("heuristic h1(n) = no of misplaced tiles relative to goal");
        PriorityQueue<Node> frontier = new PriorityQueue<Node>(1000, new Node.NodeFNComparator()); // creation of search queue
        frontier.add(ROOT); // Add root to frontier
        List<Integer> explored = new ArrayList<Integer>(); // creation of list to hold explored states
        //RUN SEARCH vv
        Stack<Node> solutionStack = PerformPQSearch(frontier, explored, Alg.A1);
        // Call function to display Results
        ShowResults(solutionStack, "A*1 Search Algorithm");
    }

    // Setup for A2 Search, Called the PerformPQSearch(), then the ShowResults() once Solution is found
    private void A2Search(Node ROOT){
        if(SHOW_DEBUG)System.out.println("heuristic h2(n) = Sum of Manhattan distances of tiles from correct position");
        PriorityQueue<Node> frontier = new PriorityQueue<Node>(1000, new Node.NodeFNComparator()); // creation of search queue
        frontier.add(ROOT); // Add root to frontier
        List<Integer> explored = new ArrayList<Integer>(); // creation of list to hold explored states
        //RUN SEARCH vv
        Stack<Node> solutionStack = PerformPQSearch(frontier, explored, Alg.A2);
        // Call function to display Results
        ShowResults(solutionStack, "A*2 Search Algorithm");
    }

    // FUNCTION: Meat of searches that use a PriorityQueue (A1, A2, Best-First and Uniform)
    private Stack<Node> PerformPQSearch(PriorityQueue<Node> frontier, List<Integer> explored, Alg a){
        System.out.println("Search Beginning");
        int maxQueue = 0; // for written report analysis
        int nodesOffQueue = 1; // for written report
        Stack<Node> solutionPath = new Stack<Node>();

        while(!frontier.isEmpty()) // While there are nodes in the frontier queue
        {
            Node n = frontier.poll();
            explored.add(n.getState().getHash());//add node state's hash code to explored list
            nodesOffQueue += 1;
            if(!n.getState().matchesStateHash(GOAL_HASH)) {
                //Create an array of states that are possible successors from our current state
                ArrayList<PuzzleState> possibleChildren = n.getState().findChildren(a);
                ArrayList<Node> front = new ArrayList<Node>(frontier);
                // iterate through the list of possible generated children
                for(int i=0; i<possibleChildren.size(); i++){
                    //1. Create a node holding the possible child
                    Node temp = new Node(n, possibleChildren.get(i).getAct(), possibleChildren.get(i),
                            n.getPathCost() + possibleChildren.get(i).getStepCost());
                    // if this node's state doesn't exist in frontier or explored lists,
                    // add it to the frontier
                    if(!CheckExplored(explored, front, temp)){
                        temp.setFN(); // Calculate FN() if the node needs to be added to frontier
                        frontier.add(temp);
                        ///*
                         if(SHOW_DEBUG){
                            ArrayList<Node> debugList = new ArrayList<Node>(frontier);
                            for(int j = 0; j< debugList.size(); j++){
                                System.out.println("Node "+ j +": " + debugList.get(j).getFN());
                            }
                        } // */
                        // if queue is a new MAX SIZE, save new MAX SIZE (for written report
                        if(maxQueue < frontier.size() && SHOW_REPORT_INFO)
                        { maxQueue = frontier.size();}
                    }
                }

            }
            else{
                System.out.println("Goal State Reached!");
                // Last in, First out stack creation to track steps from goal node back to root ancestor
                // This is the stack of Nodes that is returned by this function
                while(n.getParent() != null){
                    solutionPath.push(n);
                    n=n.getParent();
                }
                solutionPath.push(n); // final push adds ROOT node to solution stack
                if(SHOW_REPORT_INFO)System.out.println("Max Queue Size: " + maxQueue); // for written report analysis
                if(SHOW_REPORT_INFO)System.out.println("Nodes From Queue: " + nodesOffQueue); // for written report analysis
                return solutionPath;
            }
        }
        // This part of function is reached only if NO SOLUTION is found by the algorithm
        System.out.println("ERROR: NO SOLUTION FOUND!");
        if(SHOW_REPORT_INFO)System.out.println("Max Queue Size: " + maxQueue); // for written report analysis
        if(SHOW_REPORT_INFO)System.out.println("Nodes From Queue: " + nodesOffQueue); // for written report analysis
        display.AppendText("ERROR: NO SOLUTION FOUND\n");
        return solutionPath; // will return null stack
    }

    // Show Results of Search When Complete
    // By popping Nodes from a stack and taking info from the Nodes/States
    private void ShowResults(Stack<Node> solutionPath, String algorithmName){
        int size = solutionPath.size();
        display.AppendText("\nResults using: " + algorithmName + "\n\n");
        System.out.println("\nResults using: " + algorithmName + "\n\n");
        for (int i = 0; i < size; i++) {
            Node n = solutionPath.pop();
            int[] s = n.getState().getArray();
            display.AppendText("Step " + i + ":\n");
            display.AppendText(s[0] + " " + s[1] + " " + s[2] + "\n");
            display.AppendText(s[3] + " " + s[4] + " " + s[5] + "\n");
            display.AppendText(s[6] + " " + s[7] + " " + s[8] + "\n");
            display.AppendText("Action Taken: " + n.getPastAction() + "\n");
            display.AppendText("Total Cost: " + n.getPathCost() + "\n\n");
            //* // Remove initial comment out lines to turn out console results printing
            System.out.println("Step " + i + ":");
            System.out.println(s[0] + " " + s[1] + " " + s[2]);
            System.out.println(s[3] + " " + s[4] + " " + s[5]);
            System.out.println(s[6] + " " + s[7] + " " + s[8]);
            System.out.println("Action Taken: " + n.getPastAction());
            System.out.println("Total Cost: " + n.getPathCost() + "\n"); // */
        }
        display.Show();
    }

    /* HELPER: check a node's current state against an
     arraylist<PS> of explored states and
     arraylist<NODE> of frontier unexplored but discovered states //*/
    private boolean CheckExplored(List<Integer> ex, ArrayList<Node> front, Node n){
        boolean exploredBool = false;
        //Compare node state against explored ArrayList<int[]>
        for(int i = 0; i<ex.size(); i++){
            if(!exploredBool){
                if(n.getState().matchesStateHash(ex.get(i)))
                {
                    exploredBool = true;
                }
            }
        }
        //Compare node state against frontier ArrayList<Node>
        for(int i = 0; i<front.size(); i++){
            if(!exploredBool){
                if(n.getState().matchesStateHash(front.get(i).getState().getHash()))
                {
                    exploredBool = true;
                }
            }
        }
        return exploredBool;
    }




}
