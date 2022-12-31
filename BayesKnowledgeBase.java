/*
    480: Fall 2022
    Assignment 4: Part 1 Naive Bayes CLassification for Text
    Code By: Joscelyn Stephens
 */

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class BayesKnowledgeBase {

    public final String[] CATEGORIES = {"comp.os.ms-windows", "sci.crypt", "soc.religion.christian", "rec.sport.hockey", "misc.forsale"};
    private Terms terms;
    // Pr(Category) priors map
    private Map<Integer, Double> pC_priors;
    // Pr(X|Category) map
    private Map<String, Map<Integer, Double>> pCX;
    public Map<Integer, Map<String, Integer>> trainingSet; // straight from .csv file
    public Map<Integer, Integer> trainingDocLabels; // straight from .csv file
    public Map<Integer, Map<String, Integer>> testingSet; // straight from .csv file
    public Map<Integer, Integer> testingDocLabels; // straight from .csv file

    // cstr, init maps and variables
    public BayesKnowledgeBase(){
        pC_priors = new HashMap<>(); pCX = new HashMap<>();
        terms = new Terms(); trainingSet = new HashMap<>();
        trainingDocLabels = new HashMap<>(); testingDocLabels = new HashMap<>();
        testingSet = new Hashtable<>();
    }


    //getters/setters for Pr(C) Map
    public Map<Integer,Double> getPriors(){return pC_priors;}
    public double getClassPrior(int key){return pC_priors.get(key);}

    //getters/setters for Pr(C|X) Map [pr(of C | given X)
    public Map<String, Map<Integer, Double>> getPCX(){return pCX;}
    public Terms getTerms(){return terms;}

    //public void AddToTrainingSet(Integer s, Integer[] str){trainingSet.put(s, str);}
    public void AddToTrainingSet(Integer s, Integer[] str){
        Map<String, Long> row = terms.getVocabulary();
        Map<String, Integer> saveRow = new HashMap<>();
        int i = 0;
        //iterate through map
        for (Map.Entry<String,Long> mapElement : row.entrySet()) {
            String key = mapElement.getKey(); // get key
            saveRow.put(key, str[i]); // put value into map at key
            i++; // increase index
        }
        trainingSet.put(s,saveRow);
        }

    public void AddToTrainingDocSet(Integer s, Integer t){trainingDocLabels.put(s, t);}

    public void AddToTestingSet(Integer s, Integer[] str){
        Map<String, Long> row = terms.getVocabulary();
        Map<String, Integer> saveRow = new HashMap<>();
        int i = 0;
        //iterate through map
        for (Map.Entry<String,Long> mapElement : row.entrySet()) {
            String key = mapElement.getKey(); // get key
            saveRow.put(key, str[i]); // put value into map at key
            i++; // increase index
        }
        testingSet.put(s,saveRow);
    }

    public void AddToTestingDocSet(Integer s, Integer t){testingDocLabels.put(s, t);}

    /* Processing Training Set for Terms:
            Ensure that Training Set Terms are counted
            Ensure that Training Set Term/Class matches are logged
    */
    public void countTrainingTerms(){
        terms.setupTermClassMap(CATEGORIES.length); // setups up term by class map, including smoothing prep
        for (Map.Entry<Integer,Map<String,Integer>> mapElement : trainingSet.entrySet()) {
            Integer key = mapElement.getKey(); // get key
            Map<String,Integer> inside = trainingSet.get(key);
            for (Map.Entry<String,Integer> element : inside.entrySet()) {
                String keyTerm = element.getKey(); // get key
                int value = inside.get(keyTerm);
                if(value>0)
                {
                    //increase the count of this term in the training list
                    terms.increaseTerm(keyTerm, value);
                    //Now check the class (Key is the same for training set and training Doc Labels)
                    //And increment Vocab/Class Term Map based on class
                    int c = trainingDocLabels.get(key);
                    terms.increaseTermClassMap(keyTerm, c, value);
                }

            }
        }
    }

    /* Processing Training Set for Class Counts:
            Derive pC priors
    */
    public void derivePriorsFromTraining(){
        int[] count = countTrainingClasses();
        int total = 0;
        for(int i = 0; i<count.length; i++){
            total += count[i];
        }
        System.out.println("\nPriors:");
        for(int i = 0; i<count.length; i++){
            double v = count[i]/(double)total;
            pC_priors.put(i, v);
            System.out.println("Pr("+CATEGORIES[i]+") = " + v);
        }
    }

    //helper: counts number of each class instance in training set
    private int[] countTrainingClasses(){
        // create array of size NUMBER OF CATEGORIES
        int[] count = new int[CATEGORIES.length];
        // iterate through labels, incement array at appropriate index when for each class
        for (Map.Entry<Integer,Integer> mapElement : trainingDocLabels.entrySet()) {
            int value = mapElement.getValue();
            count[value] += 1;
        }
        return count;
    }

    /* Processing Training Set for Pr(X|C)
    Pr(X|Y) = Px(C|X)Pr(C)/Pr(X)
    */
    public void derivePCXMap(){
        terms.smoothingPrep();
        terms.derivePrX();
        terms.derivePrXC();
        // rows = X (number of vocabulary terms
        for (Map.Entry<String, Double> mapElement : terms.pv.entrySet()) {
            String key = mapElement.getKey(); // get key
            //column = C, number of categories/classes
            Map<Integer, Double> innerRow = new HashMap<>();
            for(int i = 0; i<CATEGORIES.length; i++){
                double p = 0.0;
                double a = 0.0;
                if(terms.prXC.containsKey(i) && terms.prXC.get(i).containsKey(key)){a = terms.prXC.get(i).get(key);}
                double b = 0.0;
                if(pC_priors.containsKey(i)){b = pC_priors.get(i);}
                double c = 0.0;
                if(terms.pv.containsKey(key)){c = terms.pv.get(key);}
                p = (a*b)/(c); // smoothing accounted for in counts
                innerRow.put(i,p); // add entry to this inner row
            }
            pCX.put(key, innerRow); // add the row to the larger map
        }

    }

    // PRINT FUNCTION takes in key, prints out probabilities of each class
    public void getXofP(String key){
        System.out.println("\nPr(C | "+key+")");
        Map<Integer, Double> temp = pCX.get(key);
        for(Map.Entry<Integer, Double> element: temp.entrySet()){
            System.out.println("     Class: " + CATEGORIES[element.getKey()] + "\t\tProbability: " + element.getValue());
        }
    }



    /* From the newsgroup read-me.txt:
    * 1. newsgroup5-train.csv: the document-term frequency matrix for the training documents.
    * Each row of this matrix corresponds to one document and each column corresponds to one term
    * and the (i,j)th element of the matrix shows the raw frequency of the jth term in the ith document.
    * This matrix contains 2000 rows and 9328 columns.

    2. newsgroup5-test.csv: the document-term frequency matrix for the test documents.
    * The matrix contains 500 rows and 9328 columns.

    3. newsgroup5-train-labels.csv: This file contains the category/class labels associated with each training document.
    * Each line (excluding the first line containing the label names) corresponds to a document
    * indexed in the range of [0,2000) and contains the numeric class label (between 0 and 4) for that document.

    4. newsgroup5-test-labels.csv: Similar to the training labels, but in this case the lines contain class labels
    * for the 500 test documents (excluding the first line containing the label names).

    5. newsgroup5-terms.txt: This file contains the set of 9328 terms in the vocabulary. Each line contains a term
    * and corresponds to the corresponding columns in training and test document-term frequency matrices.
    * */


}
