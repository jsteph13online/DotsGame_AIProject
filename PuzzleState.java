/*
    CSC 480 AI: Fall 2022
    Assignment 1: 8-Puzzle (Modified)
    Joscelyn Stephens
 */

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Math.abs;

public class PuzzleState {
    private final int PUZZLE_SIZE = 9;
    private final int ROW_SIZE = 3;

    private int[] GOAL;
    private int[] currentState;

    private int hashState;

    private int hCost;

    private int stepCost;
    private Action act;

    PuzzleState(){}
    // standard cstrs:
        // cstr for starting state
    PuzzleState(int[] s, int[] G){currentState = s; GOAL = G;
    hCost=-1; stepCost=-1; hashState = Arrays.hashCode(currentState);}
        // cstr for subsequent states
    public PuzzleState(int[] s, int[] G, int i, Action a) {
        currentState = s; GOAL = G;
        stepCost=i; // cost of state = value of tiles moved to achieve it from the explored node that discovered this state
        act=a; // Action taken to achieve this state from the explored node that discovered this state
        hCost=-1; hashState = Arrays.hashCode(currentState);}

    //cstrs for algorithms using h(n):
        // starting state
    PuzzleState(int[] s, int[] G, boolean b){currentState = s; GOAL = G;
        hCost=setHN(b); stepCost=-1; hashState = Arrays.hashCode(currentState);}
        // subsequent states
    public PuzzleState(int[] s, int[] G, int i, Action a, boolean b) {
        currentState = s; GOAL = G;
        stepCost=i; // cost of state = value of tiles moved to achieve it from the explored node that discovered this state
        act=a; // Action taken to achieve this state from the explored node that discovered this state
        hCost=setHN(b); hashState = Arrays.hashCode(currentState);}

    //==GETTERS==
    public int[] getArray(){return currentState;}
    public int getHash(){return hashState;}
    public Action getAct(){return act;}
    public int getStepCost(){return stepCost;}
    public int getHN(){return hCost;} //Only for certain search methods

    //Set the h(n) value (h1 if one is true, h2 if one is false)
    //Called from cstr if boolean value is added in the cstr call
    public int setHN(boolean one){
        int h = 0;
        if(one){
            // use formula 1 for hcost
            // h(n) = # of tiles out of place in relation to goal state
            for(int i = 0; i<PUZZLE_SIZE; i++){
                if(currentState[i] !=GOAL[i])
                    h += 1;
            }
         }
        else {
            // use formula 2 for hcost (Manhatten Distance)
            // h(n) = # of steps from tile to correct position
            int rowIndex = 0; // used for finding the actual row's place int the 1d array (increases by 3 every loop)
            // row 1 = 0-2, row 2 = 3-5. row3 = 6-8
            for (int row = 0; row < ROW_SIZE; row++) {
                // col 1 = 0,3,6  col 2 = 1,4,7  col 3 = 2,5,8
                for (int col = 0; col < ROW_SIZE; col++) {
                    if (currentState[col + rowIndex] == 0)
                        continue; // do nothing if we found the blank
                    else {
                        for (int i = 0; i < PUZZLE_SIZE; i++) {
                            int goalIndex = findGoalIndex(currentState[col+rowIndex]);
                            int grow;
                            // find the actual 0,1,2 value of the row from the 0-8 index returned
                            switch (goalIndex) {
                                case 0:
                                case 1:
                                case 2:
                                    grow = 0;
                                    break;
                                case 3:
                                case 4:
                                case 5:
                                    grow = 1;
                                    break;
                                default:
                                    grow = 2;
                                    break;
                            }
                            int gcol;
                            // find the acrual 0,1,2 value of the col from the 0-8 index returned
                            switch (goalIndex) {
                                case 0:
                                case 3:
                                case 6:
                                    gcol = 0;
                                    break;
                                case 1:
                                case 4:
                                case 7:
                                    gcol = 1;
                                    break;
                                default:
                                    gcol = 2;
                                    break;
                            }
                            h += abs(row - grow);
                            h += abs(col - gcol);
                        }
                    }
                }
                rowIndex += 3; // increase rowIndex since we're using a 1D array not a 2D one for our grid
            }
        }
        return h;
    }

    // HELPER METHOD-> finds location of a value 1-8 on the GOAL_STATE grid, and returns the index of the number on the goal
    // (used to calculate MANHATTAN DISTANCE for h2 value calculations
    private int findGoalIndex(int num){
        for(int i = 0; i<PUZZLE_SIZE; i++){
            if(GOAL[i] == num) return i;
        }
        return 0; // If there is some error, this should not happen though
    }

    // HELPER -> Checked if the stored int (hashed array) matched the int passed into the method)
    public boolean matchesStateHash(int i){
        if (this.hashState == i) return true;
        return false;
    }

    // Find all possible Children nodes from this state
    // Returns an array with those child state's within them
    public ArrayList<PuzzleState> findChildren(Alg a) {
        int useCstr = 0;
        switch(a){
            case A1: case GREEDY:
                useCstr = 1;
                break;
            case A2:
                useCstr = 2;
                break;
            default:
                useCstr = 0;
                break;
        }

        ArrayList<PuzzleState> babies = new ArrayList<PuzzleState>();
        int blankIndex = findBlank();

        // If blank is not on left, try to make state by action: LEFT
        if (blankIndex != 0 && blankIndex != 3 && blankIndex != 6) {
            swapTiles(blankIndex - 1, blankIndex, babies, Action.LEFT, useCstr);
        }
        // If blank not on far right, try to make state by action RIGHT
        if (blankIndex != 2 && blankIndex != 5 && blankIndex != 8) {
            swapTiles(blankIndex + 1, blankIndex, babies, Action.RIGHT, useCstr);
        }
        // If blank not on top, try to make state by action UP
        if (blankIndex != 0 && blankIndex != 1 && blankIndex != 2) {
            swapTiles(blankIndex - 3, blankIndex, babies, Action.UP, useCstr);
        }
        // If blank not on bottom, try to make state by action DOWN
        if (blankIndex != 6 && blankIndex != 7 && blankIndex != 8) {
            swapTiles(blankIndex + 3, blankIndex, babies, Action.DOWN, useCstr);
        }

        return babies;
    }

    //HELPER: Swap two tiles and return a new state in the provided array
    private void swapTiles(int d1, int d2, ArrayList<PuzzleState> s, Action a, int useCstr) {

        int[] cpy = CopyPuzzle(currentState);
        int temp = cpy[d1];
        cpy[d1] = currentState[d2];
        cpy[d2] = temp;
        if ( useCstr == 1){ // cst for h1
            s.add((new PuzzleState(cpy, GOAL, cpy[d1]+cpy[d2], a, true)));
        }
        else if (useCstr == 2){ // cst for h2
            s.add((new PuzzleState(cpy, GOAL, cpy[d1]+cpy[d2], a, false)));
        }
        else{ // cstr that doesn't care about heuristics in states
        s.add((new PuzzleState(cpy, GOAL, cpy[d1]+cpy[d2], a)));}
    }

    //HELPER: COPY puzzle board and RETURN the copy
    private int[] CopyPuzzle(int[] state) {
        int[] c = new int[PUZZLE_SIZE];
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            c[i] = state[i];
        }
        return c;
    }

    //HELPER: FIND the BLANK TILE (aka the "0")
    private int findBlank() {
        // -1 on return = SOMETHING IS SUPER WRONG
        int index = -1;
        for (int i = 0; i < PUZZLE_SIZE; i++) {
            if (currentState[i] == 0)
                index = i;
        }
        return index;
    }


}
