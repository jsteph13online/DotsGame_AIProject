/*
    CSC 480 AI: Fall 2022
    Assignment 1: 8-Puzzle (Modified)
    Joscelyn Stephens
 */

import java.awt.*; //graphic components for window popup
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*; //graphic components for window popup

//CLASS: Display Results of Search Algorithm For 8-Puzzle in form of new JFrame Window
public class ResultsFrame {

    JFrame frame;
    JTextArea text;
    final String BASE_TEXT = "Calculation for 8-Puzzle Solution...\n";

    ResultsFrame(){
        //==Create Window
        //Create JFrame, make sure applications closes if we close Window
        frame = new JFrame("Problem-Search Results");
        //==Creation and Setup of Window Elements==
        // Scrollable text area
        JPanel p = new JPanel();
        p.setSize(380,680);
        text = new JTextArea(BASE_TEXT);
        text.setLineWrap(true);
        text.setEditable(false);
        JScrollPane scroll = new JScrollPane(text);
        scroll.setPreferredSize(new Dimension(380, 680));
        p.add(scroll,BorderLayout.CENTER);
        frame.getContentPane().add(p, BorderLayout.CENTER);
        frame.setSize(400, 730);
        //==Display Window
        frame.setLocationRelativeTo(null); //position
        frame.setVisible(false); //visibility
    }

    //FOR LATER: ADD VALUES PASSES IN TO SHOW ACTUAL VALUES
    public void Show(){
        frame.setVisible(true); }//visibility


    //FUNCTIONS FOR SETTING THE TEXT-BOX TEXT
    public void AppendText(String s){
        if(text == null){ FixNullText(BASE_TEXT + s);}
        else {text.append(s);}}
    public void ResetText(){
        if(text == null){ FixNullText(BASE_TEXT); }
        else{text.setText(BASE_TEXT);}} // null test for temp bug avoidance

    // Helper method that re-initiates the text field in certain cases
    public void FixNullText(String s){
        System.out.println("nullTextIssue");
        text = new JTextArea (s);
        text.setBounds(10, 50, 350, 550);
        text.setLineWrap(true);
        text.setEditable(false);
        text.setVisible(true);
        frame.add(text);
    }
}
