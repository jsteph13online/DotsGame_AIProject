/*
    480: Fall 2022
    Assignment 4: Part 2 Recommender System using K-Nearest Neighbor
    Code By: Joscelyn Stephens
 */


import java.util.*;

//Calculate AVG per USer
//Calculate Matrix of pearson rating user x user
public class Pearsons {
    Double[][] pearsonMatrix;
    RecommenderSystemMain run;

    //cstr
    public Pearsons (int userNum, RecommenderSystemMain r){
        pearsonMatrix = new Double[userNum][userNum];
        run = r;
    }

    //SETUP Parent Calculation: Calculate the pearson matrix user/user
    public void fillPearsonMatrix(){
        Averages(); // create averages needed for calculation
        for(int i = 0; i< pearsonMatrix.length; i++){
            for(int j = 0; j< pearsonMatrix.length; j++){
                pearsonMatrix[i][j] = getPearson(i,j);
            }
        }
    }
    // SETUP: get avg rating per user
    public void Averages(){
        for(Map.Entry<Integer, Double>element: run.users.entrySet()){
            Double avg = getAverage(element.getKey()); // get avg for the user based on given data
            run.users.put(element.getKey(), avg); // put the avg in the user map
        }
    }
    //SETUP helper: get individual user's average
    private Double getAverage(int userId){
        double avg = 0.0;
        int count = 0;
        Integer[] value = run.ratingsMatrix.get(userId);
        //rating only counts to avg if value is not null
        for(int i = 0; i < value.length; i++) {
            if (value[i] == null) { }
            else { avg += value[i]; count++;}
        }
        avg = avg/count;
        return avg;
    }
    // FOR TESTING
    public void printPearsonMatrix(){
        for(int i = 0; i< pearsonMatrix.length; i++){
            System.out.print("\n" + i);
            for(int j = 0; j< pearsonMatrix.length; j++){
                System.out.print(" " + pearsonMatrix[i][j] + " ");
            }
        }

    }
    // SETUP helper:
    // (sum(rating(user a, item i) - usera_avg) * rating(user b, item i) - user b_avg)) /
    // SQRT(sum((rating(user a, item i) - usera_avg)^2)) * SQRT(sum((rating(user b, item i) - user b_avg))^2))
    private Double getPearson(int userA, int userB){
        double rating = 0.0;
        // get the values valid for the two users
        Integer[] valueA = run.ratingsMatrix.get(userA);
        Integer[] valueB = run.ratingsMatrix.get(userB);
        ArrayList<Integer> vectorA = new ArrayList();
        ArrayList<Integer> vectorB = new ArrayList();
        //rating only counts to avg BOTH users have non-null values
        for(int i = 0; i < valueA.length; i++) {
            if (valueA[i] == null || valueB[i] == null) { }
            else {
                vectorA.add(valueA[i]);
                vectorB.add(valueB[i]);
            }
        }
        //put the valid vector values into the equation
        double numerator = 0.0;
        double denomenatorA = 0.0;
        double denomenatorB = 0.0;
        for(int i = 0; i< vectorA.size(); i++){
            double userAval = vectorA.get(i) - run.users.get(userA);
            double userBval = vectorB.get(i) - run.users.get(userB);
            //GET NUMERATOR
            // (sum(rating(user a, item i) - usera_avg) * rating(user b, item i) - user b_avg))
            numerator += (userAval * userBval);
            //GET DENOMENATOR
            // SQRT(sum((rating(user a, item i) - usera_avg)^2)) * SQRT(sum((rating(user b, item i) - user b_avg))^2))
            denomenatorA += (userAval * userAval);
            denomenatorB += (userBval * userBval);
        }
        denomenatorA = Math.sqrt(denomenatorA);
        denomenatorB = Math.sqrt(denomenatorB);
        if(denomenatorA ==0 || denomenatorB ==0)
            return 0.0; //return 0 to avoid division by 0 creating a null value in our table

        rating = numerator / (denomenatorA * denomenatorB);
        return rating;
    }

    // PARENT FUNCTION (and helper function): get KNN
    public Neighbor[] getKNN(int userId, int k_size){
        int count = k_size;
        Neighbor[] knn= new Neighbor[k_size];
        Double[] prow = pearsonMatrix[userId];
        //get K nearest Neighbors
        for(int i = 0; i<prow.length; i++){
            double val = Math.abs(prow[i]);
            if(i != userId){
                if(count > 0) {
                    for (int j = 0; j < knn.length; j++) {
                        if (knn[j] == null) {
                            knn[j] = new Neighbor(i, prow[i]); // put it in if null
                            count--;
                            break;
                        }
                    }
                }
                else {
                    // sort by pearsons value (lowest to highest)
                    Arrays.sort(knn, new KnnSorter());
                    //if the new value is greater than one of the stored knns, then replace it
                    // will encounter knns from lowest to highest (thanks to the sort)
                    for(int j = 0; j< knn.length; j++){
                        double compVal = Math.abs(knn[j].getPvalue());
                        if ((val) > (compVal)) {
                            knn[j] = new Neighbor(i, prow[i]); // this is a nearer neighbor
                            break;
                        }
                    }
                }
            }
        }
        // if no nearest neightbors have been found
        if(count == k_size)
        {
            return new Neighbor[1];
        }
        Arrays.sort(knn, new KnnSorter());
        return knn;
    }
    //BACKUP to knn - this is triggered if USER has no nearest neighbors
    private Double[] getMeanTraining(int itemId){
        Double[] res = new Double[]{0.0, 0.0};
        double mean = 0.0;
        int count = 0;
        for(Map.Entry<Integer, Integer[]> element: run.ratingsMatrix.entrySet()){
            if(element.getValue()[itemId]==null||element.getValue()[itemId]==0){  }
            else{
                count++;
                mean += element.getValue()[itemId];
            }
        }
        mean = (mean/count);
        res[0] = mean; // rating = mean value
        res[1] = 0.0;//confidence = 0 since no nearest neightbors
        return res;
    }

    // PARENT FUNCTION: get rating for item
    public Double[] guessRating(int userId, int itemId, int k){
        // get K number of nearest numbers
        // returns array of neighbors (userid and pearsons number)
        Neighbor[] knn = getKNN(userId, k);
        Double[] prob = new Double[2];

        // get mean value if no neighbors were found
        if(knn==null){
            prob = getMeanTraining(userId);
        }
        // vote based on found neighbors
        //prob = vote(userId, knn, itemId);
        prob = vote(userId, knn, itemId);

        return prob;
    }

    //Non-weighted voting
    private Double[] vote(int userId, Neighbor[] knn, int itemId){
        //standard vote: mean of all ratings
        Double[] res = new Double[]{0.0, 0.0};
        int ratings[] = new int[run.MAX_RATING];
        Arrays.fill(ratings, 0);

        //Count how many of each rating is given by neighbors
        for(int i = 0; i< knn.length; i++){
            Integer userb = knn[i].getId();
            Integer val = run.ratingsMatrix.get(userb)[itemId];
            //Integer[] arr = run.ratingsMatrix.get(userb);
            if(val==null){} // nearest neighbor doesn't get a vote if they haven't rated the item
            else {
                int r = val;
                for(int j = 0; j< ratings.length; j++){
                    if((j+1) == r)
                        ratings[j] ++;
                }
            }
        }
        //determine Max Rating (no weight)
        int maxRating = -1;
        int maxCount = -1;
        for(int i =0; i<ratings.length; i++){
            if(ratings[i]>maxCount){
                maxCount = ratings[i];
                maxRating = i+1;
            }
        }
        //given maxRating, get the confidence level
        //Double p = nonWeightedVote(maxCount, knn.length);
        //
        Double p = weightedVote(maxCount, maxRating, knn, itemId, userId);
        //System.out.println("Results of standard voting: "+p); //FOR TESTING ONLY
        res[0] = (double)maxRating; // voted rating
        res[1] = p; // confidence
        return res;
    }

    // voting helper function - non-weighted
    public double nonWeightedVote(int maxCount, int neighbors){
        //given maxRating, get the confidence level
        double numerator = maxCount;
        double denomenator = neighbors;
        double p = numerator/denomenator;
        return p;
    }
    // voting helper function - weighted by pearson value
    public double weightedVote(int maxCount, int maxRating, Neighbor[] knn, int itemId, int userId){
        //numerator = Sum(pearsons(User, CompUser with Max Rating))
        //denomenator = Sum(pearsons(User, CompUser))
        double p = 0.0;
        double numerator = 0.0;
        double denomenator = 0.0;
        //given maxRating, get the confidence level
        for(int i=0; i< knn.length; i++){
            int id = knn[i].getId();
            int rating = (int)knn[i].getRating();
            //Neighbor only gets a vote if they have rated the title
            if(run.ratingsMatrix.get(id)[itemId]!=null) {
                if (run.ratingsMatrix.get(id)[itemId] == maxRating) {
                    numerator += Math.abs(pearsonMatrix[userId][id]);
                }
                denomenator += Math.abs(pearsonMatrix[userId][id]);
            }
        }
        p = numerator/denomenator;
        return p;
    }

    //PARENT FUNCTION: get recommendations
    public Neighbor[] getRecs(int userId, int k, int numRecs){
        Map<Integer, Double[]> unrated = new HashMap();
        //1 - identify all unrated items
        Integer[] userRow = run.ratingsMatrix.get(userId);
        for(int i=0; i< userRow.length; i++){
            if(userRow[i]==null||userRow[i]==0){
                Double[] empt = new Double[]{0.0, 0.0};
                unrated.put(i, empt);
            }
        }
        //2 - rate them using KNN guess rating method
        for(Map.Entry<Integer, Double[]> element: unrated.entrySet()){
            Double[] d = guessRating(userId, element.getKey(), k);
            unrated.put(element.getKey(), d); //TBD - double check this doesn't mess up the iteration
        }
        //3 - identigy top N of thos recs
        /////Put items into neighbor objects (so it is easier to sort by ratings
        Neighbor[] ratedItems = new Neighbor[unrated.size()];
        int j = 0;
        for(Map.Entry<Integer, Double[]> element: unrated.entrySet()){
            ratedItems[j] = new Neighbor(element.getKey(), element.getValue()[0], element.getValue()[1]);
            j++;
        }
        ///// Sort by rating
        Arrays.sort(ratedItems, new RatingSorter());
        ////3b. - if unrated < n, just return num of unrated items
        int length;
        if(numRecs < ratedItems.length){ length = numRecs; }
        else {length = ratedItems.length;}
        ///// Extract ids and ratings from top sorted objs into rec list
        Neighbor[] recs = new Neighbor[length];
        for(int i = 0; i< length; i++){
            int index = length - 1 - i;
            recs[i] = ratedItems[index];
        }
        return recs;
    }

    //PARENT FUNCTION: get Mean Accuracy Evaluation given K-value
    public double getMAEs(int k, boolean justTestUsers){
        double mae = 0.0;
        double numerator = 0.0;
        double denomenator = 0.0; // i.e. count of items used
        //1 for each test user u and test item i that user has rated, generate a guess
        for(Map.Entry<Integer, Integer[]>element: run.ratingsMatrix.entrySet()){
            if((justTestUsers && element.getKey()>29) || !justTestUsers) { // if boolean is true, just include users 30-39 in the MAEs calculation
                Integer[] rates = element.getValue();
                for (int i = 0; i < rates.length; i++) {
                    if (rates[i] == null || rates[i] == 0) {
                    } //DO NOTHING
                    else {
                        Double[] d = guessRating(element.getKey(), i, k);
                        //2 compare guess to documented rating: abs(prediction - actual) = prediction error
                        numerator += Math.abs(d[0] - rates[i]);
                        denomenator += 1; // increase count for average later
                    }
                }
            }
        }
        //3 average all prediction errors to get the MAE for the K value (can use to find ideal K)
        mae = numerator/denomenator;
        return mae;
    }


    // helper class, sorts the KNN array so lowest rating is lowest in array index
    class KnnSorter implements Comparator<Neighbor> {
        //comparison method for neighbor objects
        public int compare(Neighbor a, Neighbor b) {
            double va = Math.abs(a.getPvalue());
            double vb = Math.abs(b.getPvalue());
            if ((va) > (vb))
                return 1;
            else if ((va) < (vb))
                return -1;
            return 0;
        }
    }
    //Helper class to sort by ratings value
    class RatingSorter implements Comparator<Neighbor> {
        //comparison method for neighbor objects
        public int compare(Neighbor a, Neighbor b) {
            double va = Math.abs(a.getRating());
            double vb = Math.abs(b.getRating());
            if ((va) > (vb))
                return 1;
            else if ((va) < (vb))
                return -1;
            return 0;
        }
    }
}
