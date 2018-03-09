import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * This is the main program for BridgePuzzle
 * 
 * It implements an mvc pattern using a blocking queue for
 * input signals.
 * 
 * The model is the basic puzzle specification.
 * The view is the GUI display of the working state of attempt to solve
 * The controller runs the steps to solve the puzzle
 * 
 * @author legge
 *
 */
public class BridgePuzzle {
	
	public static void main(String[] args) {
		new BridgePuzzle();
	}

	private InputSignal inputSignals;
	private Model model;
	private View view;
	private Control control;
	
	/**
	 * Constuctor sets up MVC in threaded mode
	 */
	public BridgePuzzle() {
		inputSignals = new InputSignal();
		model = new Model();
		// GUI thread 
		view = new View(model, inputSignals);
		SwingUtilities.invokeLater( view );
		// Controller runs in thread to allow interaction with GUI
		Thread t = new Thread( new Control(model, inputSignals) );
		t.start();
	}
	

}
