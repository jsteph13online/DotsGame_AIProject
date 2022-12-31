/*
    CSC 480 AI: Fall 2022
    Assignment 1: 8-Puzzle (Modified)
    Joscelyn Stephens
 */

import java.util.Arrays;
import java.util.Comparator;

public class Node {
    private PuzzleState state;
    private Node parent;
    private Action pAction;
    private int pathCost; // total path cost to get to this node
    private int fCost;

    //maybe don't need?
    private int stepCost; // step cost from parent to this node

    Node(){}

    // cstr for Root Node
    Node(PuzzleState s){
        parent = null; pAction = null;
        pathCost = 0; stepCost = 0;
        state = s; fCost = 0;
    }

    // cstr for Standard Nodes
    Node(Node papa, Action past, PuzzleState s, int cost){
        parent = papa;
        pAction = past;
        state = s; // passing in a state? (or calculating after node exists?
        pathCost = cost; // default? Or do we already know cost on create node? (or step + parent cost????)
        stepCost = pathCost - parent.getPathCost();
        fCost = cost;
    }

    // ==== zgetters
    public PuzzleState getState(){return state;}
    public Node getParent(){return parent;}
    public Action getPastAction(){return pAction;}
    public int getPathCost(){return pathCost;}
    public int getFN(){return fCost;}


    // ==== setters
    public void setPathCost(int step){
        stepCost = step;
        pathCost = parent.getPathCost() + stepCost;
    }
    public void setFN(){fCost = this.getState().getHN() + this.pathCost; }

    // check if node is Root node (return true if ROOT)
    public boolean isRoot(){
        if (parent == null) return true;
        else return false;
    }

    // CUSTOM COMPARATOR: H VALUE (order of PQ based on h(n) (so lowest h(n) will pop first))
    static class NodeComparator implements Comparator<Node> {
        public int compare(Node n1, Node n2) {
        if (n1.getState().getHN() > n2.getState().getHN())
            return 1;
        else if (n1.getState().getHN() < n2.getState().getHN())
            return -1;
        return 0;
    }
    }

    // CUSTOM COMPARATOR: F VALUE (order of PQ based on f(n) = cost + h(n) (so lowest will pop first))
    static class NodeFNComparator implements Comparator<Node> {
        public int compare(Node n1, Node n2) {
            if (n1.getFN() > n2.getFN())
                return 1;
            else if (n1.getFN() < n2.getFN())
                return -1;
            return 0;
        }
    }

    // CUSTOM COMPARATOR: COST (order of PQ based on cost (so lowest will pop first))
    static class NodeCostComparator implements Comparator<Node> {
        public int compare(Node n1, Node n2) {
            if (n1.getPathCost() > n2.getPathCost())
                return 1;
            else if (n1.getPathCost() < n2.getPathCost())
                return -1;
            return 0;
        }
    }


}

