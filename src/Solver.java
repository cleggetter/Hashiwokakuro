

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.stream.IntStream;

public class Solver {
	private Model model;
	private GridDefinition grid;
	private Action action;
	private SolverStatus status;
	private InputSignal signals;
	private Helper allLog;
	 
	private int gCounter;
	private int maxLoops;
	private int updateInterval;
	
	private ArrayList<Integer> checkpoints;
	private ArrayList<Integer> replayCheckpoints;
	
	public Solver(Model modelIn, InputSignal insignals) {
		model = modelIn;
		signals = insignals;
		status = new SolverStatus();
		action = new Action();
		allLog = new Helper("Solver");
		checkpoints = new ArrayList<Integer> ();
		grid = model.grid;
		gCounter = 0;
		maxLoops = 1000;
		updateInterval = 1000;
	}

	public boolean solve() {
		reset();		
		if (!isFullyConnected()) {
			allLog.Error("Not fully connected");
			return false;
		}
		boolean solved = tryAllocatingConstraints();
		allLog.Log(true, "Solved = " + solved);
		updateUI();
		startReplay();
		return solved;
	}
	
	void nextStep() {
		String next = signals.getNext();
		updateUI();
	}
	
	
	public void process(String command) {
		if (command.equals("MainFrameClicked")){
			allLog.Log(true, " Click received ");
			nextReplayStep();
		}
		if (command.equals("back")){
			allLog.Log(true, " back received ");
			backReplayStep();
		}
		if (command.equals("forward")){
			allLog.Log(true, " forward received ");
			nextReplayStep();
		}
		return;
	}
	
	private void updateUI() {
		model.changeSomething();
	}
	public void startReplay() {
		replayCheckpoints = checkpoints;
		action.saveAllStates(grid);
		int thisCheckpoint = action.getHistoryState();
		checkpoints.add(thisCheckpoint);
	}
	public void nextReplayStep() {
		// A pseudo circular list
		Integer ind = replayCheckpoints.remove(0);
		replayCheckpoints.add(ind);
		action.replayState(grid, ind);
		updateUI();
	}
	public void backReplayStep() {
		// A pseudo circular list
		Integer ind = replayCheckpoints.remove(replayCheckpoints.size()-1);
		replayCheckpoints.add(0, ind);
		action.replayState(grid, ind);
		updateUI();
	}

	private void reset() {
		// Make sure all trackers are initialized
		
		grid.getAllIslands().stream().forEach(a -> a.tracker.init(a, status));
		grid.getAllConstraints().stream().forEach(a -> a.tracker.init(a, status));
		grid.getAllBridges().stream().forEach(a -> a.tracker.init(a, status));
		
		// Set up unique group ids for each island
		int n = 0;
		for (IslandBase i : grid.getAllSolidIslands()) {
			i.tracker.groupId = n++;
		}

		// An initial update ensures all info is propagated
		/*
		grid.getAllBridges().stream().forEach(b -> action.updateBridge(b));
		grid.getAllSolidIslands().stream().forEach(ib -> action.updateIsland(ib));
		grid.getAllConstraints().stream().forEach(c -> action.updateConstraint(c));
		*/
		status.init(grid);
		
	}
	
	private boolean isFullyConnected() {
		int expectedCount = (int) grid.getAllSolidIslands().size();
		
		// Finally check every node is connected
		TreeSet<IslandBase> visited = new TreeSet<IslandBase>();
		followBridge(grid.getAllSolidIslands().get(0), visited);
		return (visited.size() == expectedCount);
	}
	
	private void followBridge(IslandBase ib, TreeSet<IslandBase> visited) {
		if (visited.contains(ib)) {
			return;
		}
		visited.add(ib);
		ib.bridges.stream().filter(b -> b.tracker.nonZeroScoreAvailable())
			.forEach(b -> followBridge(b.otherEndIsland(ib), visited));
	}
	

	
	private boolean tryAllocatingConstraints() {
		gCounter++;
		
		//nextStep();
		
		if (gCounter % updateInterval == 0){
			updateUI();
		}
		if (--maxLoops == 0) {
			return false;
		}
		action.saveAllStates(grid);
		int thisCheckpoint = action.getHistoryState();
		checkpoints.add(thisCheckpoint);
		ArrayList<Constraint> unsolved = new ArrayList<Constraint>();
		grid.getAllConstraints().stream()
			.filter(cn -> !cn.tracker.isSolved())
			.filter(cn -> cn.hasTarget())
			.forEach(cn -> unsolved.add(cn));
		if (unsolved.size() == 0) {
			return (status.isValid() && status.complete(grid)
					&& isFullyConnected()); 
		}
		ArrayList<ConstraintBridgeSolutions> cbs = new ArrayList<ConstraintBridgeSolutions> ();
		for (Constraint c : unsolved) {
			ConstraintBridgeSolutions cs = new ConstraintBridgeSolutions(c, 10000);
			cbs.add(cs);
		}
		cbs.sort(solutionsComparator);
		ConstraintBridgeSolutions cs = cbs.get(0);
		while (cs.updateWithNextSolution()){
			if (status.isValid()){
				if (tryAllocatingConstraints()) {
					return true;
				}
			}
			action.revertState(grid, status, thisCheckpoint);
		}
		action.revertState(grid, status, thisCheckpoint);
		checkpoints.remove(checkpoints.size()-1);
		return false;
	}
		
	private static Comparator<ConstraintBridgeSolutions> solutionsComparator = 
			new Comparator<ConstraintBridgeSolutions>() {
        @Override
        public int compare(ConstraintBridgeSolutions t1, ConstraintBridgeSolutions t2) {
            return (t1.solutions.size() < t2.solutions.size() ? -1 :
                    (t1.solutions.size() == t2.solutions.size()) ? 0 : 1);
          }
    };
    
	private class EstimateAndScore {
		private ArrayList<BridgeEstimate> bset;
		private int score;
		private int index;

		public EstimateAndScore(EstimateAndScore old)
		{
			this.score = old.score;
			this.bset = new ArrayList<BridgeEstimate> ();
			old.bset.stream().forEach(bs -> this.bset.add(bs.newInstance(bs)));
			this.index = old.index;
		}
		public EstimateAndScore(Constraint c) {
			this.bset = new ArrayList<BridgeEstimate> ();
			c.internalBridges.stream()
				.forEach(b -> bset.add(new BridgeEstimate(b, true, 2, b.tracker.getMinPossibleScore())));
			c.externalBridges.stream()
				.forEach(b -> bset.add(new BridgeEstimate(b, false, 1, b.tracker.getMinPossibleScore())));
			this.score = computeScore();
			this.index = 0;
		}
			
		public void setEstimate(int e) {
			bset.get(index).estimate = e;
			computeScore();
		}
		private int computeScore() {
			score = bset.stream()
					.mapToInt(b -> b.estimate * b.multiple)
					.sum();
			return score;
		}
		public String Dump() {
			StringBuilder builder = new StringBuilder();
			bset.stream().forEach(b -> builder.append(b.toString()));
			return String.format("EAS  score %d index %d bridges %s",
					score, index, builder.toString());
		}
	}
	
	private class ConstraintBridgeSolutions {
		private Constraint constraint;
		private ArrayList<EstimateAndScore> recurState;
		private ArrayList<EstimateAndScore> solutions;
		private int solutionLimit;
		private boolean recurValid;
		
		public ConstraintBridgeSolutions(Constraint c, int limit) {
			init(c, limit);
		}
		public ConstraintBridgeSolutions(Constraint c) {
			init(c, -1);
		}
		private void init(Constraint c, int limit) {
			this.constraint = c;
			this.solutionLimit = limit;
			recurState = new ArrayList<EstimateAndScore> ();
			solutions = new ArrayList<EstimateAndScore> ();
			if (!c.hasTarget() || c.tracker.isSolved()) {
				return;
			}
			recurState.add(new EstimateAndScore(c));
			recurValid = true;
			recur();
		}
	
		public boolean updateWithNextSolution() {
			if (solutions.size() == 0) {
				return false;
			}
			EstimateAndScore sol = solutions.remove(solutions.size()-1);
			for (BridgeEstimate be : sol.bset) {
				action.changeMinimum(be.bridge.tracker, be.estimate);
				action.changeMaximum(be.bridge.tracker, be.estimate);
			}
			
			return true; 
		}
		
		private boolean reachOutside(IslandBase ib, TreeSet<IslandBase> seen, ArrayList<BridgeEstimate> be) {
			boolean withinConstraint = false;
			for (IslandBase cIsles: constraint.solidIslands){
				withinConstraint = withinConstraint || Tracker.sameTracker(cIsles.tracker, ib.tracker);
			}
			if (!withinConstraint) {
				return true;
			}
			if (seen.contains(ib)) {
				return false;
			}
			seen.add(ib);
			for (BridgeEstimate b : be) {
				// If bridge leads to/from this node check if it connects outside
				// or leaves an outside one isolated
				if (Tracker.sameTracker(ib.tracker, b.bridge.toIsland.tracker)
						|| Tracker.sameTracker(ib.tracker, b.bridge.fromIsland.tracker)) {
					if (b.estimate > 0) {
						if (reachOutside(b.bridge.otherEndIsland(ib), seen, be)) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
		private void checkSolution(EstimateAndScore e) {
			// Check (a) it doesn't leave any islands within constraint isolated
			//       (b) none of the island limits are violated
			for (BridgeEstimate b : e.bset) {
				int tscore = b.bridge.toIsland.tracker.currentScore - b.bridge.tracker.currentScore + b.estimate;
				if (tscore > b.bridge.toIsland.tracker.getMaxPossibleScore()) {
					return;	
				}
				int fscore = b.bridge.fromIsland.tracker.currentScore - b.bridge.tracker.currentScore + b.estimate;
				if (fscore > b.bridge.fromIsland.tracker.getMaxPossibleScore()) {
					return;	
				}
				if (!reachOutside(b.bridge.toIsland, new TreeSet<IslandBase>(), e.bset)) {
					return;
				}
				if (!reachOutside(b.bridge.fromIsland, new TreeSet<IslandBase>(), e.bset)) {
					return;
				}
			}
			solutions.add(e);
		}
		
		private boolean recur() {
			int rIndex = recurState.size()-1;
			if ((rIndex < 0) || !recurValid){
				return false;
			}
			EstimateAndScore est = recurState.remove( rIndex );
			int space = constraint.Target() - est.score;
			if (space == 0) {
				checkSolution(est);
				if (solutions.size() == solutionLimit) {
					recurState.clear();
					recurValid = false;
					return true;
				}
				
			}
			else if (space < 0) {
				// Already over target
			}
			else if (est.index >= est.bset.size()) {
				// no more bridges to extend
			}
			else {
				// Extend to find more solutions
				BridgeEstimate be = est.bset.get(est.index);
				if (be.extendable()) {
					// change estimate
					for (Integer s : IntStream.rangeClosed(be.estimate, be.maxScore()).toArray()) {
						EstimateAndScore newEst = new EstimateAndScore(est);
						newEst.setEstimate(s);
						newEst.index += 1;
						recurState.add(newEst);
						recur();
					}
				}
				else {
					// Leave this bridge, but try to change future bridges.
					est.index = est.index+1;
					recurState.add(est);
				}
			}
			return recur();
		}
		
		public String Dump() {
			String s = "Num solutions = " + solutions.size();
			
			return s;
 		}
	}
		
	private class BridgeEstimate {
		private Bridge bridge;
		private int estimate;
		private int multiple;
		private boolean internal;

		public BridgeEstimate(Bridge bridge, boolean internal, int multiple, int estimate) {
			this.bridge = bridge;
			this.multiple = multiple;
			this.estimate = estimate;
			this.internal = internal;
		}
		public boolean extendable() {
			return (estimate < bridge.tracker.getMaxPossibleScore());
		}
		public int maxScore() {
			return bridge.tracker.getMaxPossibleScore();
		}
		public BridgeEstimate newInstance(BridgeEstimate be) {
			return new BridgeEstimate(bridge, internal, multiple, estimate);
		}
		public String toString() {
			return String.format("%s (%d)  ",  bridge.toString(), estimate * multiple);
		}
	}	
}


