/*
    CSC 480 AI: Fall 2022
    Assignment 1: 8-Puzzle (Modified)
    Joscelyn Stephens
 */

//graphic components for GUI
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;

//LIST OF COMPONENTS OF 8-PUZZLE PROGRAM:
/*
    COMPONENTS OF THIS 8-PUZZLE PROGRAM:
        Main.java - init program, handles user selection GUI window, calls Solver.java, inits Solver and ResultsFrame
        Node.java - NODE login, contains PuzzleState objects
        Solver.java - logic brain of program, containing search algorithms, triggered by Main.java CONFIRM button
        PuzzleState.java - States created by algorithm searches in Solver, which are then added to nodes. Handle heuristic calculations
        Alg.java - ENUM of algorithm names
        Action.java - ENUM of action names
        ResultsFrame.java - handles the results GUI window
 */

public class Main implements ActionListener, ItemListener {
    // Frame objects that need to be referenced from multiple methods
    // (problem and alg selections)
    JFrame frame;
    JRadioButton easyRadio;
    JRadioButton medRadio;
    JRadioButton hardRadio;
    JRadioButton dRadio;
    JRadioButton bRadio;
    JRadioButton uRadio;
    JRadioButton gRadio;
    JRadioButton a1Radio;
    JRadioButton a2Radio;
    JLabel oneone; JLabel onetwo; JLabel onethree;
    JLabel twoone; JLabel twotwo; JLabel twothree;
    JLabel threeone; JLabel threetwo; JLabel threethree;

    // Results Window
    ResultsFrame resultsFrame;
    Solver solver;

    // MAIN : init user selection window, solver, results window (hidden)
    public static void main(String[] args) {
        System.out.println("Starting 8-Puzzle Program");
        Main run = new Main();
        run.CreateResultsFrame();
        run.CreateBrains();
        run.CreateWindow();
    }

    //INITs CLASS: SOLVER, which handles the brains and solving portion of the program
    public void CreateBrains(){ solver = new Solver(resultsFrame); }

    // Inits CLASS: RESULTSFRAME, which handles displaying the results from SOLVER
    public void CreateResultsFrame() {
        resultsFrame = new ResultsFrame();
    }

    // Setup of JFRAME for user selection GUI window
    public void CreateWindow() {
        //==Create Window
        //Create JFrame, make sure applications closes if we close Window
        frame = new JFrame("8-Puzzle GUI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //==Creation and Setup of Window Elements==
        //--Title
        JLabel titleLabel = new JLabel("8-Puzzle");
        titleLabel.setBounds(10, 10, 300, 30);

        //--Buttons: Problem Mode (Easy, Medium, Hard)
        JLabel modeTitle = new JLabel("Problem Mode");
        modeTitle.setBounds(10, 70, 300, 30);
        easyRadio = new JRadioButton("Run the Easy Input Problem", true);
        easyRadio.setBounds(50, 100, 300, 30);
        easyRadio.addItemListener(this);
        medRadio = new JRadioButton("Run the Medium Input Problem", false);
        medRadio.setBounds(50, 130, 300, 30);
        medRadio.addItemListener(this);
        hardRadio = new JRadioButton("Run the Hard Input Problem", false);
        hardRadio.setBounds(50, 160, 300, 30);
        hardRadio.addItemListener(this);
        //JRadioButton custRadio = new JRadioButton("Input a Custom Problem", false);
        //custRadio.setBounds(50, 190, 300, 30);
        //Group the radio buttons together
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(easyRadio);
        modeGroup.add(medRadio);
        modeGroup.add(hardRadio);
        //modeGroup.add(custRadio);

        //--Buttons: Algorithm Mode (Depth, Breadth, Uniform, Greedy, A1, A2)
        JLabel algTitle = new JLabel("Search Algorithm");
        algTitle.setBounds(350, 70, 300, 30);
        bRadio = new JRadioButton("Breadth First", true);
        bRadio.setBounds(350, 100, 150, 30);
        dRadio = new JRadioButton("Depth First", false);
        dRadio.setBounds(350, 130, 150, 30);
        uRadio = new JRadioButton("Uniform Search", false);
        uRadio.setBounds(350, 160, 150, 30);
        gRadio = new JRadioButton("Greedy Best First", false);
        gRadio.setBounds(500, 100, 150, 30);
        a1Radio = new JRadioButton("A*1", false);
        a1Radio.setBounds(500, 130, 150, 30);
        a2Radio = new JRadioButton("A*2", false);
        a2Radio.setBounds(500, 160, 150, 30);
        // Xtra --> Add iterative deepening TBD?
        // Xtra --> Add A*3 TBD?

        //Group the radio buttons together
        ButtonGroup algGroup = new ButtonGroup();
        algGroup.add(dRadio);
        algGroup.add(bRadio);
        algGroup.add(gRadio);
        algGroup.add(uRadio);
        algGroup.add(a1Radio);
        algGroup.add(a2Radio);

        // Show puzzle layout for selected problem
        JLabel gridTitle = new JLabel("Selected Puzzle Layout");
        gridTitle.setBounds(50, 270, 300, 30);
        oneone = new JLabel(String.valueOf(solver.EASY_START[0]));
        oneone.setBounds(50, 320, 300, 30);
        onetwo = new JLabel(String.valueOf(solver.EASY_START[1]));
        onetwo.setBounds(100, 320, 300, 30);
        onethree = new JLabel(String.valueOf(solver.EASY_START[2]));
        onethree.setBounds(150, 320, 300, 30);
        twoone = new JLabel(String.valueOf(solver.EASY_START[3]));
        twoone.setBounds(50, 370, 300, 30);
        twotwo = new JLabel(String.valueOf(solver.EASY_START[4]));
        twotwo.setBounds(100, 370, 300, 30);
        twothree = new JLabel(String.valueOf(solver.EASY_START[5]));
        twothree.setBounds(150, 370, 300, 30);
        threeone = new JLabel(String.valueOf(solver.EASY_START[6]));
        threeone.setBounds(50, 420, 300, 30);
        threetwo = new JLabel(String.valueOf(solver.EASY_START[7]));
        threetwo.setBounds(100, 420, 300, 30);
        threethree = new JLabel(String.valueOf(solver.EASY_START[8]));
        threethree.setBounds(150, 420, 300, 30);

        // Goal Layout
        JLabel gridGoalTitle = new JLabel("Puzzle Goal Layout");
        gridGoalTitle.setBounds(350, 270, 300, 30);
        JLabel goneone = new JLabel("1");
        goneone.setBounds(350, 320, 300, 30);
        JLabel gonetwo = new JLabel("2");
        gonetwo.setBounds(400, 320, 300, 30);
        JLabel gonethree = new JLabel("3");
        gonethree.setBounds(450, 320, 300, 30);
        JLabel gtwoone = new JLabel("8");
        gtwoone.setBounds(350, 370, 300, 30);
        JLabel gtwotwo = new JLabel("0");
        gtwotwo.setBounds(400, 370, 300, 30);
        JLabel gtwothree = new JLabel("4");
        gtwothree.setBounds(450, 370, 300, 30);
        JLabel gthreeone = new JLabel("7");
        gthreeone.setBounds(350, 420, 300, 30);
        JLabel gthreetwo = new JLabel("6");
        gthreetwo.setBounds(400, 420, 300, 30);
        JLabel gthreethree = new JLabel("5");
        gthreethree.setBounds(450, 420, 300, 30);

        //--Button: Confirm
        JButton confirmButton = new JButton("Confirm");
        confirmButton.setBounds(250, 500, 100, 30);
        confirmButton.addActionListener(this);

        // Add elements to frame
        frame.add(titleLabel);
        frame.add(modeTitle);
        frame.add(confirmButton);
        frame.add(easyRadio);
        frame.add(medRadio);
        frame.add(hardRadio);
        frame.add(algTitle);
        frame.add(dRadio);
        frame.add(bRadio);
        frame.add(uRadio);
        frame.add(gRadio);
        frame.add(a1Radio);
        frame.add(a2Radio);
        frame.add(gridTitle);
        frame.add(oneone);
        frame.add(onetwo);
        frame.add(onethree);
        frame.add(twoone);
        frame.add(twotwo);
        frame.add(twothree);
        frame.add(threeone);
        frame.add(threetwo);
        frame.add(threethree);
        frame.add(gridGoalTitle);
        frame.add(goneone);
        frame.add(gonetwo);
        frame.add(gonethree);
        frame.add(gtwoone);
        frame.add(gtwotwo);
        frame.add(gtwothree);
        frame.add(gthreeone);
        frame.add(gthreetwo);
        frame.add(gthreethree);
        //==

        //==Display Window
        frame.setLayout(null);
        frame.setSize(700, 600);
        frame.setVisible(true); //visibility
    }

    // CONFIRM BUTTON ACTIONS:
    // Call the SOLVER which is the brain
    public void actionPerformed(ActionEvent e) {
        System.out.println("CLICK");//TEMP ACTION ON BUTTON
        Alg algorithm = Alg.DEPTH;
        int prob = 1;
        // process which problem selected
        if (easyRadio.isSelected()) {
            System.out.println("EASY MODE"); //TEMP ACtiON FOR MODE;
            prob = 1;
        } else if (medRadio.isSelected()) {
            System.out.println("MEDIUM MODE"); //TEMP ACtiON FOR MODE;
            prob = 2;
        }
        else //hard is selected
        {
            System.out.println("HARD MODE"); //TEMP ACtiON FOR MODE;
            prob = 3;
        }
        // process which search algorithm selected
        if (dRadio.isSelected()) {
            System.out.println("DEPTH SEARCH"); //TEMP ACtiON FOR MODE;
            algorithm = Alg.DEPTH;
        } else if (bRadio.isSelected()) {
            System.out.println("BREADTH SEARCH"); //TEMP ACtiON FOR MODE;
            algorithm = Alg.BREADTH;
        }else if (uRadio.isSelected()) {
            System.out.println("UNIFORM SEARCH"); //TEMP ACtiON FOR MODE;
            algorithm = Alg.UNIFORM;
        } else if (gRadio.isSelected()) {
            System.out.println("GREEDY BEST FIRST SEARCH"); //TEMP ACtiON FOR MODE;
            algorithm = Alg.GREEDY;
        } else if (a1Radio.isSelected()) {
            System.out.println("A*1 SEARCH"); //TEMP ACtiON FOR MODE;
            algorithm = Alg.A1;
        } else //custom is selected
        {
            System.out.println("A*2 Search"); //TEMP ACtiON FOR MODE;
            algorithm = Alg.A2;
        }

        // Trigger the SOLVER to start doing its thing
        resultsFrame.ResetText();
        resultsFrame.Show();
        solver.StartSolving(prob,algorithm);
    }

    // Update display of the current problem
    // Based on which problem is selected
    public void itemStateChanged(ItemEvent e){
        int i = 1;
        if (easyRadio.isSelected()) {  i = 1; }
        else if (medRadio.isSelected()) { i = 2; }
        else { i = 3; }
        int[] p = solver.getProblem(i); // get the problem
        oneone.setText(String.valueOf(p[0]));
        onetwo.setText(String.valueOf(p[1]));
        onethree.setText(String.valueOf(p[2]));
        twoone.setText(String.valueOf(p[3]));
        twotwo.setText(String.valueOf(p[4]));
        twothree.setText(String.valueOf(p[5]));
        threeone.setText(String.valueOf(p[6]));
        threetwo.setText(String.valueOf(p[7]));
        threethree.setText(String.valueOf(p[8]));
    }


}
