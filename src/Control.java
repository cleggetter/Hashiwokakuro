import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Control class which runs the solver code in its own thread.
 * @author legge
 *
 */
public class Control implements Runnable{
	
	// Run control in its own thread;
	private Model model;
	private InputSignal signals;
	
	/**
	 * 
	 * @param model - the intialized grid definition
	 * @param signals - the inputsignal interface to the view
	 */
	public Control(Model model, InputSignal signals) {
		this.model = model;
		this.signals = signals;
	}
	@Override
	public void run() {
		Solver solver = new Solver(model, signals);
		if (solver.solve()) {
			System.out.println("Solved puzzle");
			String next = "start";
			while (!next.equals("exit")) {
				next = signals.getNext();
				solver.process(next);
			}
		}
		else {
			System.out.println("No solution found");
		}
	}
	
}
