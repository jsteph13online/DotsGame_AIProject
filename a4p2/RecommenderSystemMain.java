/*
    480: Fall 2022
    Assignment 4: Part 2 Recommender System using K-Nearest Neighbor
    Code By: Joscelyn Stephens
 */



import java.awt.*;
import java.io.File;
import java.util.*;

public class RecommenderSystemMain {

    final String RATING_FILE = "hw4-book-ratings-40.csv";
    final String BOOK_NAME_FILE = "hw4-book-names.csv";
    final int MAX_RATING = 5;
    final int DEFAULT_K = 5;
    int numUsers=0;
    int numItems=0;

    //Integer[user_id, book_id, rating[1-5]]
    Map<Integer, Integer[]> importMatrix; // .csv file imported into program
    Map<Integer, String> bookNames; // csv file of book names imported into program
    Map<Integer, Double> users; // user ids, avg
    Map<Integer, Integer> items; // item ids
    Map<Integer, Integer[]> ratingsMatrix; // rows: user ids | columns: item ids
    Pearsons pearsons;


    ///* Turn off if testing testing part 1 in same directory
    public static void main(String[] args){
        RecommenderSystemMain run = new RecommenderSystemMain();
        run.start();

    } // */

    public void start(){
        importFile();
        System.out.println("Entries in database: " + importMatrix.size());
        prepwork();
        hw4();
        //moreTests(); // uncomment to see additional tests
    }

    public void prepwork(){
        countUsersItems();
        boolean complete = createRatingsMatrix();
        pearsons = new Pearsons(numUsers, this);
        pearsons.fillPearsonMatrix();
        //printRatingsMatrix(); // FOR TESTING ONLY - see user x item rating matrix
        //printUserMatrix(); // FOR TESTING ONLY - see the user-is and user rating avg
        //pearsons.printPearsonMatrix(); // FOR TESTING ONLY - see pearson ratings user/user matrix
    }

    // additional tests outside of those assignmed for hw
    public void moreTests(){
        //GUESSES
        performGuesses(36, 0, 5);
        performGuesses(31, 6, 10);
        performGuesses(1, 0, 5);
        performGuesses(1, 1, 5);
        performGuesses(2, 1, 10);

        //RECS
        performRecs(38, 20, 3);
        performRecs(39, 10, 3);
        performRecs(1, 5, 3);
        performRecs(2, 5, 3);
    }

    // run the tests assigned for hw4
    public void hw4(){
        //QUESTION 1
        findNearestNeighbors(35, 10);
        findNearestNeighbors(39, 10);

        //QUESTION 2
        performGuesses(35, 0, 5);
        performGuesses(39, 6, 10);

        //QUESTION 3
        // top 3 recs for user 35, k=20
        performRecs(35, 20, 3);

        //QUESTION 4
        // top 3 recs for user 36, k=10
        performRecs(36, 10, 3);

        //QUESTION 5
        // MAE for test data k=5, users 30-39
        performMae(5, true);

        //QUESTION 6
        // repeat evaluation of #5, with range of k values from 1 - 30, users 30-39
        for(int k = 1; k<31; k+=5){
            performMae(k, true);
        }
        //QUESTION 6 - But MAE on ALL users
        // repeat evaluation of #5, with range of k values from 1 - 30
        for(int k = 1; k<31; k+=5){
            performMae(k, false);
        }


    }

    // trigger the logic that would detect the N nearest neighbors of X
    public void findNearestNeighbors(int userId, int k){
        Neighbor[] n = pearsons.getKNN(userId, k);
        printNeighbors(userId, n);
    }

    // trigger logic to guess the rating user X would give item Y
    public void performGuesses(int userId, int itemId, int k){
        Double[] guess = pearsons.guessRating(userId, itemId, k);
        printGuesses(userId, itemId, guess, k);
    }

    // trigger the logic to calculate X top recs of items for user Y
    public void performRecs(int userId, int k, int recSize){
        Neighbor[] recs = pearsons.getRecs(userId, k, recSize);
        printRecs(recs, userId);
    }

    // trigger the logic to calculate the MAE and print the results
    public void performMae(int k, boolean justTestUsers){
        double mae = pearsons.getMAEs(k, justTestUsers);
        printMAE(mae, k);
    }


    //print guess results
    public void printGuesses(int userId, int itemId, Double[] results, int k){
        System.out.println("\nUser: " + userId);
        System.out.println("Predicted Rating of Item "+ itemId +" '"+ bookNames.get(itemId)+"': " + results[0]);
        System.out.println("Confidence:  " + results[1] );
        int realRating = accuracyCheck(userId, itemId);
        if(realRating>0)System.out.println("ACCURACY CHECK!\nDocumented Rating: " + realRating);
    }

    //print nearestNeighborResults
    public void printNeighbors(int userId, Neighbor[] n){
        System.out.println("\n"+ n.length+" Nearest Neighbors of " + userId+ ":");
        for(int i = (n.length-1); i>-1; i--) {
            System.out.println("\tUser:" + n[i].getId() + " Similarity: " + n[i].getPvalue());
        }
    }

    //Print results for finding Top Predicted recommendations
    public void printRecs(Neighbor[] recs, int userId){
        System.out.println("\nTop "+ recs.length+" Recommendations for User " + userId+ ":");
        for(int i = 0; i< recs.length; i++) {
            System.out.println("\tItem Id: "+ recs[i].getId() +" Title: " + bookNames.get(recs[i].getId()) +
                    "\t\tExpected Rating: " + recs[i].getRating() + "   Confidence: " + recs[i].getPvalue());
        }
    }

    public void printMAE(double mae, int k){
        System.out.println("\nMAE for k = " +k);
        System.out.println(mae);
    }

    //get existing rating (if exists) to compare guess against
    public int accuracyCheck(int userId, int itemId){
        for(Map.Entry<Integer, Integer[]> entry: ratingsMatrix.entrySet()){
            Integer[] row = entry.getValue();
            for(int i = 0; i< row.length; i++){
                if(userId == entry.getKey()){
                    if(itemId==i && row[i]!=null){
                        return row[i]; // return rating if not null
                    }
                }
            }
        }
        return -1; // if rating doesn't exist, return -1
    }
    //Import .csv file into matrix (hashmap)
    public void importFile(){
        try
        {
            // Import Vocabulary
            importMatrix = new HashMap<>();
            bookNames = new HashMap<>();
            File f = new File(RATING_FILE);
            File f1 = new File(BOOK_NAME_FILE);
            Scanner reader = new Scanner(f);
            String data = reader.nextLine(); // skip first line (just headers
            int i = 0;
            while (reader.hasNextLine()) {
                data = reader.nextLine();
                String[] splitData = data.split(",");
                int len = splitData.length;
                Integer[] splitDataInt = new Integer[len];
                for (int j = 0; j < len; j++) {
                    splitDataInt[j] = Integer.parseInt(splitData[j]);
                }
                importMatrix.put(i, splitDataInt);
                i++;
            }
            reader.close();
            System.out.println(RATING_FILE + " read into program");
            reader = new Scanner(f1);
            int id = 0;
            while (reader.hasNextLine()) {
                data = reader.nextLine();
                if(data.equals("")) break;
                String[] splitData = data.split(",");
                String name = splitData[1];
                //int id = 0;
                //id = Integer.parseInt(splitData[0]);
                bookNames.put(id, name);
                id++;
            }
            reader.close();
            System.out.println(BOOK_NAME_FILE + " read into program");
            /* FOR TESTING ONLY
            for(Map.Entry<Integer, String>element: bookNames.entrySet()){
                System.out.println(element.getKey() + " " + element.getValue());
            } */
        }
        catch(Exception e){
            System.out.println("ERROR: issue importing files");
        }
    }
    //Helper - count number of users and items
    public void countUsersItems(){
        users = new HashMap<>();
        items = new HashMap<>();
        for(Map.Entry<Integer, Integer[]> element:importMatrix.entrySet()){
           Integer[] temp = element.getValue();
           if(!users.containsKey(temp[0])){users.put(temp[0], 0.0);}
           if(!items.containsKey(temp[1])){items.put(temp[1], temp[1]);}
        }
        numUsers = users.size();
        numItems = items.size();
        System.out.println("Number of Users: " + numUsers);
        System.out.println("Number of Items: " + numItems);
    }
    //USing the importMatrix, create a userID x itemID formatted ratingsMatrix
    public boolean createRatingsMatrix(){
        ratingsMatrix = new HashMap<>();
        //Put a row for each user into the ratingsMap
        for(Map.Entry<Integer, Double> element: users.entrySet()){
            Integer[] ratingArray = new Integer[numItems];
            ratingsMatrix.put(element.getKey(), ratingArray);
        }
        // populate map with any ratings which may exist
        for(Map.Entry<Integer, Integer[]>element: importMatrix.entrySet()){
            Integer[] temp = element.getValue();
            // put the rating of the item into the matrix at the correct place
            Integer[] temp2 = ratingsMatrix.get(temp[0]);
            temp2[temp[1]] = temp[2];
            ratingsMatrix.put(temp[0], temp2);
        }

        return true;

    }
    //FOR TESTING - print the ratingsMatrix (userid X itemID (ratings))
    public void printRatingsMatrix(){
        //FOR TESTING ONLY
        System.out.println("\nRATINGS MATRIX:");
        for(Map.Entry<Integer, Integer[]>element: ratingsMatrix.entrySet()) {
            System.out.print("\n" + element.getKey());
            Integer[] temp2 = element.getValue();
            for(int i = 0; i< temp2.length; i++){
                System.out.print(" "+i +"="+ temp2[i] +" ");
            }
        }
    }
    //FOR TESTING - print the ratingsMatrix (userid X itemID (ratings))
    public void printUserMatrix(){
        //FOR TESTING ONLY
        System.out.println("\nUSERS MATRIX:");
        for(Map.Entry<Integer, Double>element: users.entrySet()) {
            System.out.print("\n" + element.getKey() + " " + element.getValue());

        }
    }



}



