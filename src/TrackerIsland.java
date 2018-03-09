import java.util.ArrayList;
import java.util.OptionalInt;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrackerIsland extends Tracker {

	public IslandBase island;
	
	int numUnsolvedBridges;

		
	public TrackerIsland(){
		thisLog = new Helper("TrackerIsland");
		entity = "Island";
	}
	
	public void init(IslandBase s, SolverStatus status){
		island = s;
		solverStatus = status;
		int limit = island.bridges.size() * TrackerBridge.maxWeight;
		OptionalInt lower = island.constraints.stream()
				.filter(c -> c.hasTarget())
				.mapToInt(t -> t.Target()).min(); 
		if (lower.isPresent()) {
			limit = Math.min(limit, lower.getAsInt());
		}
		maxPossibleScore = limit;
		minPossibleScore = 1;
	
		// contiguous group starts with a unique id;
		groupId = this.uid;
	}	
	
	/*
	 * update
	 * Called when any bridge in the constraint is updated
	 * returns 
	 *     true if any change to the state
	 *     false if no change to the state
	 * @see Tracker#update()
	 */
	
	public boolean update() {
		
		// Depends on bridges
		Helper thisLog = new Helper("TrackerIsland::Update");
		thisLog.debugIf(thisLog.interest(this.island));
		thisLog.Log( Dump());
	
		if (solved) {
			// Cannot update solved islands
			return false;
		}
		boolean changed = false;
		
		// Make sure score reflects latest info
		currentScore = (int) island.bridges.stream()
				.mapToInt(b -> b.tracker.massAllocated).sum();	
		if (!massIsKnown) {
			massAllocated = currentScore;	
		}
		int bscore = Math.min( 
				maxPossibleScore,
				(int) island.bridges.stream()
				.mapToInt(s -> s.tracker.maxPossibleScore).sum());
		thisLog.Log("Bscore = " + bscore);
		if (bscore != maxPossibleScore) {
			maxPossibleScore = bscore;
			changed = true;
		}
		bscore = Math.max( minPossibleScore,
				(int) island.bridges.stream()
					.mapToLong(s ->  s.tracker.minPossibleScore).sum());
		if (bscore != minPossibleScore) {
			minPossibleScore = bscore;
			changed = true;
		}

		// Now check if solved.
		thisLog.Log("Unsolved bridges " +
				island.bridges.stream()
					.filter(m -> !m.tracker.solved)
					.count());
		
		// Check there are some remaining unsolved bridges
		if ((maxPossibleScore == minPossibleScore) 
				&& (massAllocated == maxPossibleScore)) {
			massIsKnown = true;
			solved = true;
		}
		// Check there are some remaining unsolved bridges
		else if (island.bridges.stream()
			.filter(m -> !m.tracker.solved)
			.count() == 0) {
			thisLog.Log(
					String.format("IN ISLAND: SOLVED: %s", Dump()));
			solved = true;
		}
		
		// Check mass allocated matches the expected
		else if ((massIsKnown) && (massAllocated == currentScore)){
					solved = true;
		}

		if (solved) {
			maxPossibleScore = currentScore;
			minPossibleScore = currentScore;
			massIsKnown = true;
			for (Bridge b : island.bridges) {
				b.tracker.freezeFinalScore(b.tracker.currentScore);
			}
			changed = true;
		}
		valid = isValid();
		if (!valid) {
			Helper.Print("IslandTracker", "Island status = false ");
			solverStatus.setValid(false);
		}
		if (solved) {
			solverStatus.decrementUnsolvedIslands();
		}
		if (changed) {
			// Propagate scores to constraint sets
			for (Constraint c : island.constraints) {
				c.tracker.update();
			}
			
		}
		updateGroupId();
		thisLog.Log("On Exit:" + Dump());
		return changed;
	}
	
	public boolean isValid() {
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
	
	public int updateGroupId() {
		OptionalInt v = island.bridges.stream()
			.filter(b -> !b.tracker.zeroScoreAvailable())
			.mapToInt( i -> Math.min(i.fromIsland.tracker.groupId, i.toIsland.tracker.groupId))
			.min();
		if (v.isPresent()) {
			if (groupId != v.getAsInt()) {
				solverStatus.removeGraphExemplar(this);
				groupId = v.getAsInt();
			}
			// Propagate to all other connected islands
			island.bridges.stream()
				.filter(b -> !b.tracker.zeroScoreAvailable())
				.filter(b -> b.otherEndIsland(island).tracker.groupId != groupId)
				.forEach( b -> b.otherEndIsland(island).tracker.updateGroupId());
			
		}
		return groupId;
	}
	
	public boolean fixMass(int v) {
		if ((minPossibleScore == v)
				&& maxPossibleScore == v) {
			return false;
		}
		minPossibleScore = v;
		maxPossibleScore = v;
		return true;
	}
	
	public boolean increaseMinPossible(int v) {
		if (v <= minPossibleScore) {
			return false;
		}
		if (v > maxPossibleScore) {
			minPossibleScore = maxPossibleScore;
			return false;
		}
		minPossibleScore = v;
		return true;
		
	}
	
	public int maxScoreExcludingNeighborIsland(IslandBase neighbour) {
		// Want to count all possible bridges to other islands,
		// but only the minimum bridges to the neighbor
		Helper thisLog = new Helper("maxScoreExcludingNeighborIsland");
		thisLog.debugIf( neighbour.matchPos(0, 1));
		thisLog.Log("Checking neighbour: " + neighbour.toString());
		island.bridges.stream().forEach(b -> thisLog.Log( b.toString() ));
		
		thisLog.Log("Non HoldOut score " + 
				island.bridges.stream()
					.mapToInt(s -> s.tracker.minPossibleScore ).sum());
		thisLog.Log("Bridges "); 	
		island.bridges.stream()
				.forEach(s -> thisLog.Log(s.tracker.Dump()));
		
		int bNeighbour = 
				(int) island.bridges.stream()
						.filter(b -> Tracker.sameTracker(b.toIsland.tracker, neighbour.tracker))
						.filter(b -> Tracker.sameTracker(b.fromIsland.tracker, neighbour.tracker))
						.mapToInt(s -> s.tracker.minPossibleScore ).sum();
		thisLog.Log("bNeighbour " + bNeighbour);
		int bOthers = 
				(int) island.bridges.stream()
						.filter(b -> neighbour.tracker.sameTracker(b.toIsland.tracker, neighbour.tracker))
						.filter(b -> neighbour.tracker.sameTracker(b.fromIsland.tracker, neighbour.tracker))
						.mapToInt(s -> s.tracker.maxPossibleScore ).sum();
		thisLog.Log("Num Others : " + 
				island.bridges.stream()
					.filter(b -> neighbour.tracker.sameTracker(b.toIsland.tracker, neighbour.tracker))
					.filter(b -> neighbour.tracker.sameTracker(b.fromIsland.tracker, neighbour.tracker))
					.count());
		
		thisLog.Log("bOthers " + bOthers);
		
		thisLog.Log("Finished");
		return bNeighbour + bOthers;
	}
	
	
	public String getGroupString() {
		return String.format("%s (G %d)", island.id, this.groupId);
	}
	
	public String asString() {
		int numUnsolvedBridges = (int) island.bridges.stream()
				.filter(b -> !b.tracker.isSolved()).count();
		return String.format(
		 "Island: %s %s maxP %d minP %d   Unresolved Bridges %d,  massAlloc %d group %d\n", 
				island.id,
				solved ? "Solved" : "NotSolved",
				maxPossibleScore, 
				minPossibleScore,
				numUnsolvedBridges,
				massAllocated, 
				groupId
				);
		
	}
	
	public String Dump() {
		return String.format("%s\n",  island.id) + super.Dump();
				
	}
}
