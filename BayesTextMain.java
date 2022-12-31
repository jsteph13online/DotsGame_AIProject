/*
    480: Fall 2022
    Assignment 4: Part 1 Naive Bayes CLassification for Text
    Code By: Joscelyn Stephens
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

public class BayesTextMain {

    //file names
    final String TERMS_FILE = "newsgroups5-terms.txt";
    final String TEST_FILE = "newsgroups5-test.csv";
    final String TEST_LABELS_FILE = "newsgroups5-test-labels.csv";
    final String TRAIN_FILE = "newsgroups5-train.csv";
    final String TRAIN_LABELS_FILE = "newsgroups5-train-labels.csv";
    final int TEST_RANGE = 20;

    BayesKnowledgeBase KB;

    ///* Turn off for testing part 2
    public static void main(String[] args){
        BayesTextMain run = new BayesTextMain();
        run.start();
    } // */

    public void start(){
        KB = new BayesKnowledgeBase();
        importFileData();
        train();
        hw2();
        test();
    }

    public void train(){
        KB.countTrainingTerms();
        KB.derivePriorsFromTraining();
        KB.derivePCXMap();
    }

    public void test(){
        int correct = 0;
        for(int i = 0; i< TEST_RANGE; i++) {
            int ci = checkTestDocument(i, true);
            if(ci == KB.testingDocLabels.get(i)) correct++;
        }
        for(int i = TEST_RANGE; i< KB.testingSet.size(); i++){
            int ci = checkTestDocument(i, false);
            if(ci == KB.testingDocLabels.get(i)) correct++;
        }
        printAccuracy(correct);
    }

    public void hw2(){
        KB.getXofP("game");
        KB.getXofP("god");
        KB.getXofP("match");
        KB.getXofP("program");
        KB.getXofP("sale");
        KB.getXofP("microsoft"); // just for comp: #0 #4 #1 #3 #2
    }

    public int checkTestDocument(int docNum, boolean printResults){
        if(!KB.testingSet.containsKey(docNum)){System.out.println("Error: Invalid Document Key"); return -1;}
        Map<String, Integer> document = KB.testingSet.get(docNum);

        double maxValue = (double) Integer.MIN_VALUE;
        int maxClass = -1;
        double tempSum = 0.0;
        double tempPC = 0.0;
        Terms terms = KB.getTerms();
        //category probabilities = max: log(P(c))+ sum[log(p(a|c))]
        for(int i = 0; i< KB.CATEGORIES.length; i++) {
            tempSum = 0.0;
            //get (log(P(c))
            tempPC = Math.log(KB.getClassPrior(i));
            tempSum += tempPC;
            for (Map.Entry<String, Integer> element : document.entrySet()) {
                try {
                    // add to sum[log(P(a|c))] for each instance of term in the document
                    String key = element.getKey();
                    int termNum = element.getValue();
                    if(termNum > 0) {
                        for(int j = 0; j < termNum; j++){
                            tempSum += Math.log(terms.prXC.get(i).get(key)); // WHY ALL GETTING SAME VALUES?!?!?
                    }}
                } catch(Exception e){
                    System.out.println("err!");
                }
            }
            if(tempSum > maxValue) {
                maxValue = tempSum; // if the current class's result is bigger than last, it is new most likely class
                maxClass = i;
            }
            //System.out.print("TEST: ");printTestRestults(i, docNum, tempSum); //FOR TESTING ONLY
        }
        if(printResults) printTestRestults(maxClass, docNum, maxValue);
        return maxClass;
    }

    public void printTestRestults(int guess, int docNum, double prob){
        System.out.println("\nDocument " + docNum + ":");
        System.out.println("Predicted Class: " + guess + "\t" + KB.CATEGORIES[guess] );
        int actual = KB.testingDocLabels.get(docNum);
        System.out.println("Actual Class: \t" + actual + "\t" + KB.CATEGORIES[actual] );
        System.out.println("Log Probability:"  + prob);
    }

    public void printAccuracy(int correct){
        int size = KB.testingSet.size();
        double percentage = (double)correct/size;
        System.out.println("\nOverall Accuracy of Test Set Guesses:");
        System.out.println("Test Set Size: " + size);
        System.out.println("Correct Guesses: " + correct + "/"+size);
        System.out.println(percentage);
    }

    //Import File Data Into HashMaps For Later Evaluation
    public void importFileData(){
        try {
            // Import Vocabulary
            File f = new File(TERMS_FILE);
            File f1 = new File(TRAIN_FILE);
            File f2 = new File(TRAIN_LABELS_FILE);
            File f3 = new File(TEST_FILE);
            File f4 = new File(TEST_LABELS_FILE);

            Scanner reader = new Scanner(f);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                KB.getTerms().addTerm(data);
            }
            reader.close();
            System.out.println(TERMS_FILE + " read into program");

            //Training Document Reading
            reader = new Scanner(f1);
            int i = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] splitData = data.split(",");
                int len = splitData.length;
                Integer[] splitDataInt = new Integer[len];
                for(int j = 0; j<len; j++){
                    splitDataInt[j] = Integer.parseInt(splitData[j]);
                }
                KB.AddToTrainingSet(i, splitDataInt);
                i++;
            }
            reader.close();
            System.out.println(TRAIN_FILE + " read into program");

            // Training Label Document Reading
            reader = new Scanner(f2);
            reader.nextLine(); //Skip first Line (labels)
            i = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                KB.AddToTrainingDocSet(i, Integer.parseInt(data));
                i++;
            }
            reader.close();
            System.out.println(TRAIN_LABELS_FILE + " read into program");

            //Training Document Reading
            reader = new Scanner(f3);
            i = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                String[] splitData = data.split(",");
                int len = splitData.length;
                Integer[] splitDataInt = new Integer[len];
                for(int j = 0; j<len; j++){
                    splitDataInt[j] = Integer.parseInt(splitData[j]);
                }
                KB.AddToTestingSet(i, splitDataInt);
                i++;
            }
            reader.close();
            System.out.println(TEST_FILE + " read into program");

            // Training Label Document Reading
            reader = new Scanner(f4);
            reader.nextLine(); //Skip first Line (labels)
            i = 0;
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                KB.AddToTestingDocSet(i, Integer.parseInt(data));
                i++;
            }
            reader.close();
            System.out.println(TEST_LABELS_FILE + " read into program");

        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found");
            e.printStackTrace();
        }
    }

}
