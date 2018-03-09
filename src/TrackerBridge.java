import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.OptionalInt;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tracker bridge is a sub class of tracker base class.
 * 
 * Handles specific bridge bounds, plus some bridge
 * specific questions to handle a bridges crossing
 * at null points.
 * 
 * @author legge
 *
 */
public class TrackerBridge extends Tracker {

	static final int maxWeight = 2;
	Bridge bridge;
	
	public TrackerBridge(){
		thisLog = new Helper("TrackerBridge");
		entity = "Bridge";
	}
		
	public void init(Bridge b, SolverStatus status) {
		bridge = b;
		solverStatus = status;
		int limit = maxWeight;
		OptionalInt lower = b.constraints.stream()
				.filter(c -> c.hasTarget())
				.mapToInt(t -> t.Target()).min(); 
		if (lower.isPresent()) {
			if (lower.getAsInt() >= 0) {
				limit = Math.min(limit, lower.getAsInt());
			}
		}
		
		minPossibleScore = 0;
		maxPossibleScore = limit;
	}
	
	/*
	 * update
	 * Called when any bridge score is updated
	 * this will update any islands or constraints affected
	 * returns 
	 *     true if any change to the state
	 *     false if no change to the state
	 * @see Tracker#update()
	 */
	public boolean update() {
		
		Helper thisLog = new Helper("TrackerBridge::Update");
		//thisLog.debugIf(thisLog.interest(bridge));
		thisLog.Log( "On Entry " + Dump());
		//updateGroupId();
		
		if (solved) {
			return false;
		}
		
		// Allocated and current are the same for bridges
		massAllocated = minPossibleScore;
		currentScore = minPossibleScore;
		
		if (!isNullCrossingsPathAvailable(true)) {
			maxPossibleScore = 0;
		}
		if ( !this.zeroScoreAvailable() ) {
			claimNullCrossingsPath();
		}
		
		if (bridge.fromIsland.tracker.solved
				|| bridge.toIsland.tracker.solved) {
			solved = true;
		}
		if (minPossibleScore == maxPossibleScore) {
			solved = true;
		}

		if (solved) {
			massIsKnown = true;
			maxPossibleScore = minPossibleScore;
			solverStatus.decrementUnsolvedBridges();
		}
		// Update the islands
		bridge.fromIsland.tracker.update();
		bridge.toIsland.tracker.update();
		
		updateGroupId();
		valid = isValid();
		if (!valid) {
			Helper.Print("BridgeTracker", "Bridge status = false ");
			solverStatus.setValid(false);
		}
		thisLog.Log( "On Exit " + Dump());
		return solved;
	}
	
	public boolean isValid() {
		if (currentScore > maxWeight) {
			return false;
		}
		if (minPossibleScore > maxPossibleScore) {
			return false;
		}
		if (massAllocated > maxPossibleScore) {
			return false;
		}
		if (solved) {
			if (massAllocated != currentScore) {
				return false;
			}
		}
		return true;
	}

	// Is bridge modifiable?
	public boolean bridgeCanBeModified() {
		if (solved) {
			return false;
		}
		if (!isNullCrossingsPathAvailable(true)){
			return false;
		}
		// Is it solved by proxy?
		if (maxMinusMin() == 0) {
			return false;
		}
		if (bridge.fromIsland.tracker.solved || bridge.toIsland.tracker.solved) {
			return false;
		}
		for (Constraint c : bridge.constraints) {
			if (c.tracker.solved) {
				return false;
			}
		}
		return true;
	}
	
	
	public int updateGroupId() {
		if ( !this.zeroScoreAvailable() ) {
			groupId = Math.min( bridge.fromIsland.tracker.groupId,
								bridge.toIsland.tracker.groupId);
			
		}
		return groupId;
	}
	
	
	// Return the null crossing bridges
	//  do this by checking the bridges through each node that aren't this one.
	public ArrayList<Bridge> getNullCrossingBridges() {
		ArrayList<Bridge> nullBridge = new ArrayList<Bridge>();
		for (IslandBase ib : bridge.iNulls) {
			// If the bridge contains option 0 then it hasn't been assigned
			ib.bridges.stream()
				.filter(b -> !sameTracker(this, b.tracker))
				.forEach(b -> nullBridge.add(b));
		}
		return nullBridge;
	}
	
	// Check if the null path crossing is still available/undecided
	//  do this by checking the bridges through each node that aren't this one.
	public boolean isNullCrossingsPathAvailable(boolean forMeOnly) {
		int c = 0;
		for (IslandBase ib : bridge.iNulls) {
			// If the bridge contains option 0 then it hasn't been assigned
			c += ib.bridges.stream()
					.filter(b -> (!forMeOnly || !sameTracker(this, b.tracker)))
					.filter(b -> !b.tracker.zeroScoreAvailable())
					.count();
		}
		return (c==0);
	}
	
	
	// Claim the null crossing path
	private boolean claimNullCrossingsPath() {
		for (IslandBase ib : bridge.iNulls) {
			ib.bridges.stream()
				.filter(b -> !sameTracker(this,b.tracker))
				.forEach(b -> b.tracker.freezeFinalScore(0));
		}
		return true;
	}
	
	public String getGroupString() {
		return String.format("Bridge: %s -> %s %d",
				bridge.fromIsland.tracker.getGroupString(),
				bridge.toIsland.tracker.getGroupString(),
				bridge.tracker.minPossibleScore
				);
	}
	
	public String Dump() {
		return String.format("Bridge: %s -> %s \n",
				bridge.fromIsland.tracker.getGroupString(),
				bridge.toIsland.tracker.getGroupString())
				+ super.Dump();
				
	}
	
}
