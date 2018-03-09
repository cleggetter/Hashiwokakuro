import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Class to track the status of a solution path
 * 
 * This doesn't do any solution processing but tracks validity
 * by inspecting the current trackers
 * 
 * @author legge
 *
 */
public class SolverStatus {

	private HashMap<Integer, TrackerIsland> graphExemplar;
	
	private int numUnsolvedBridges;
	private int numUnsolvedIslands;
	private int numUnsolvedConstraints;

	private int numIslandGroups;
	private boolean valid;
	
	public SolverStatus() {
		graphExemplar = new HashMap<Integer, TrackerIsland> ();

		numUnsolvedBridges = 1;
		numUnsolvedIslands = 1;
		numUnsolvedConstraints = 1;
		
		numIslandGroups = 0;
		valid = true;
	}
	

	public void setValid(boolean v) {
		valid = v;
	}
	public boolean isValid() {
		return valid;
	}
	
	public void decrementUnsolvedBridges() {
		numUnsolvedBridges -= 1;
	}
	public void decrementUnsolvedIslands() {
		numUnsolvedBridges -= 1;
	}
	public void decrementUnsolvedConstraints() {
		numUnsolvedBridges -= 1;
	}
	
	public boolean complete(GridDefinition gd) {
		return 
				(gd.getAllSolidIslands().stream()
				.filter(ib -> !ib.tracker.isSolved())
				.count() == 0);
	}
	
	public void init(GridDefinition gd) {

		gd.getAllSolidIslands().stream()
		.filter(ib -> !ib.tracker.isSolved())
		.forEach(ib -> updateGraphExemplar(ib.tracker));
				
		resetCounts(gd);
	}
	
	public void resetCounts(GridDefinition gd) {
		numUnsolvedBridges = (int) gd.getAllBridges().stream()
				.filter(c -> !c.tracker.isSolved())
				.count();
			
		numUnsolvedIslands = (int) gd.getAllSolidIslands().stream()
				.filter(ib -> !ib.tracker.isSolved())
				.count();
			
		numUnsolvedConstraints = (int) gd.getAllConstraints().stream()
				.filter(c -> c.hasTarget())
				.filter(c -> !c.tracker.isSolved())
				.count();
		
		resetGraphExemplars(gd.getAllSolidIslands().stream()
				.map( i -> i.tracker)
				.collect(Collectors.toCollection(ArrayList::new))
				);
		
	}
	
	public void resetGraphExemplars(ArrayList<TrackerIsland> tlist ) {
		/*
		graphExemplar.clear();
		for (TrackerIsland ti : tlist) {
			updateGraphExemplar(ti);
		}
		numIslandGroups = graphExemplar.size();
		*/
	}
	
	public void updateGraphExemplar(TrackerIsland it) {
		/*
		if (!graphExemplar.containsKey(it.groupId)){
			graphExemplar.put(it.groupId, it);
			numIslandGroups = graphExemplar.size();
		}
		*/
	}
	
	public void removeGraphExemplar(TrackerIsland it) {
		/*
		if (graphExemplar.containsKey(it.groupId)){
			graphExemplar.remove(it.groupId);
			numIslandGroups = graphExemplar.size();
		}
		*/
	}
	
	public String Dump(String delim) {
		return String.join(delim
			,String.format("NumUnsolvedBridges %d", numUnsolvedBridges)
			,String.format("NumUnsolvedIslands %d", numUnsolvedIslands)
			,String.format("NumUnsolvedConstraints %d", numUnsolvedConstraints)
			,String.format("NumIslandGroups %d", numIslandGroups)
			);
	}
	public String Dump() {
		return Dump(System.getProperty("line.separator"));
	}
}
