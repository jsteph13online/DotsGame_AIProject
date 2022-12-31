/*
    480: Fall 2022
    Assignment 4: Part 1 Naive Bayes CLassification for Text
    Code By: Joscelyn Stephens
 */

import java.util.HashMap;
import java.util.Map;

public class Terms {
    private int totalVocab;
    public Map<String, Double> pv;
    private Map<String, Long> vocabulary; // <Term, InstanceCount>
    public Map<Integer, Map<String, Long>> vocabClassMap; // <Term <Class#, InstanceCount>>
    public Map<Integer, Map<String, Double>> prXC;


    public Terms(){
        totalVocab = 0;
        vocabulary = new HashMap<>(); vocabClassMap = new HashMap<>();
        pv = new HashMap<>(); prXC = new HashMap<>();

    }
    //set/get maps
    public Map<String, Long> getVocabulary(){return vocabulary;}

    public void addTerm(String s){vocabulary.put(s, 0l);}
    public void increaseTerm(String s, Integer v){long i = vocabulary.get(s); vocabulary.put(s, i+v);}

    //setup the classTermMap to ensure there is one entry per term
    // inits each term to 1 instance per class for smoothing
    public void setupTermClassMap(int classNum){
        for (int i = 0; i< classNum; i++) {
            Map<String, Long> row = new HashMap<>();
            for (Map.Entry<String, Long> element : vocabulary.entrySet()) {
                row.put(element.getKey(), 1l); //create entry for term in map, init to 1 for smoothing
            }
            vocabClassMap.put(i, row); // add outer to class/term map
        }
    }

    //From training, increment the term per class count
    public void increaseTermClassMap(String s, Integer cl, Integer v){
        Map<String, Long> row = vocabClassMap.get(cl);
            long i=1l;
            if(row.containsKey(s)){
                i = row.get(s);
            }
            row.put(s, i + v);

        vocabClassMap.put(cl, row);
    }

    //Derive Pr(term) based on the training set (number of instances of each term in the training)
    public void derivePrX(){
        totalVocab = vocabulary.size();
        for (Map.Entry<String,Long> mapElement : vocabulary.entrySet()) {
            String key = mapElement.getKey();
            double value = mapElement.getValue()/(double)totalVocab;
            pv.put(key, value);
            //System.out.println(value);
        }
    }

    // prep for smoothing, incrememnts total vocab instance count to account for artifical increase of terms due to smoothing
    public void smoothingPrep(){
        // increment total count of each term by 5 (one for each category
        for (Map.Entry<String,Long> mapElement : vocabulary.entrySet()) {
            String key = mapElement.getKey(); // get key
            Long value = mapElement.getValue() + 5; // get value
            vocabulary.put(key, value);
        }
        //(smoothing prep for vocabTermClass map was done in the Setup function)
        //(when every vocab instance per class was initiated to 1 instead of 0)
    }

    public void derivePrXC(){
        // iterate through training info to create probability map
        for(Map.Entry<Integer, Map<String, Long>> mapElement : vocabClassMap.entrySet()) {
            Integer key = mapElement.getKey(); // get key
            Map<String,Long> classCount = vocabClassMap.get(key);
            Long count = 0l; // total term count for this class set to 0 to start
            Map<String, Double> newInner = new HashMap<>(); // this map will hold the probabilities for class
            for(Map.Entry<String, Long> element : classCount.entrySet()){
                Long value = element.getValue();
                count += value; // increase total class term count
            }
            for(Map.Entry<String, Long> element : classCount.entrySet()){
                String innerKey = element.getKey();
                Long value = element.getValue();
                double p = value/(double)count;
                // add to the new inner
                newInner.put(innerKey, p); // put in new inner map
            }
            prXC.put(key, newInner); // add class to probability map
        }
    }


}
