import java.util.ArrayList;
import java.util.Comparator;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tracker constraint is a sub class of tracker base class.
 * 
 * Implements necessary overrides
 * 
 * @author legge
 *
 */
public class TrackerConstraint extends Tracker {

	Constraint constraint;
	
		
	public TrackerConstraint(){
		thisLog = new Helper("TrackerConstraint");
		entity = "Constraint";
	}
	
	public void init(Constraint s, SolverStatus status){
		constraint = s;	
		solverStatus = status;
		solved = false;
		valid = true;;
		maxPossibleScore = (int) constraint.solidIslands.stream()
				.mapToLong(i -> i.tracker.maxPossibleScore).sum();
		minPossibleScore = (int) constraint.solidIslands.stream()
				.mapToLong(i -> i.tracker.minPossibleScore).sum();
		massIsKnown = s.hasTarget();
		massAllocated = massIsKnown ? s.Target() : 0;
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
		//updateGroupId();
		if (!constraint.hasTarget()) {
			return false;
		}
		if (solved == true) {
			return false;
		}
		boolean changed = false;
		int numUnsolvedIslands = (int)
				(constraint.solidIslands.stream()
						.filter(i -> !i.tracker.solved).count());
				
		if (numUnsolvedIslands == 0) {
			solved = true;
			changed = true;
		}
		
		currentScore = (int) constraint.solidIslands.stream()
				.mapToLong(i -> i.tracker.currentScore)
				.sum();
		if (currentScore == constraint.Target()) {
			solved = true;
			changed = true;
		}
		else  {
			int massscore = (int) constraint.solidIslands.stream()
					.mapToLong(i -> i.tracker.massAllocated).sum();
			if (massscore == constraint.Target()) {
				solved = true;
				changed = true;
				constraint.solidIslands.stream()
					.forEach(i -> 
					i.tracker.setMaxPossibleScore(i.tracker.massAllocated));
			}
			// Don't mark solved until currentScore is correct.
		}
		
		// Update min/max possible
		int newScore = (int) constraint.solidIslands.stream()
				.mapToLong(i -> i.tracker.maxPossibleScore).sum();
		if (newScore != maxPossibleScore) {
			maxPossibleScore = newScore;
			changed = true;
		}
		newScore = (int) constraint.solidIslands.stream()
				.mapToLong(i -> i.tracker.minPossibleScore).sum();
		if (newScore != minPossibleScore) {
			minPossibleScore = newScore;
			changed = true;
		}
		valid = isValid();
		if (!valid) {
			solverStatus.setValid(false);
		}
		if (solved) {
			solverStatus.decrementUnsolvedConstraints();
			if (false &&!valid) {
				Helper hh = new Helper("TrackerConstraint");
				hh.Error("Failed: " + Dump());
				hh.Error("Solved constraint not matched target");
				hh.Error(String.format("mmScores: %s",  
						(minPossibleScore != maxPossibleScore)));
				hh.Error(String.format("ccheck: %s",  
						constraint.matchTarget(maxPossibleScore)));
				hh.Error(this.Dump());
			}
		}
		return changed;
	}
	// These updates are stateful
	// ie depend on other states or bridges.
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
			if (!constraint.matchTarget(currentScore)){
				return false;
			}
		}
		return true;
	}

	public int updateGroupId() {
		OptionalInt v = constraint.solidIslands.stream()
			.mapToInt( b -> b.tracker.groupId)
			.min();
		if (v.isPresent()) {
			groupId = v.getAsInt();
		}
		return groupId;
	}
	
	public static Comparator<TrackerConstraint> mostBridgesComparator = 
			new Comparator<TrackerConstraint>() {
        @Override
        public int compare(TrackerConstraint t1, TrackerConstraint t2) {
        	int c1 = (int) t1.constraint.bridges.stream()
        				.filter(b -> !b.tracker.isSolved())
        				.mapToInt(b -> b.tracker.maxMinusMin())
        				.sum();
        	int c2 = (int) t2.constraint.bridges.stream()
    				.filter(b -> !b.tracker.isSolved())
    				.mapToInt(b -> b.tracker.maxMinusMin())
    				.count();
            return (c1 > c2) ? -1 :  (c1 == c2) ? 0 : 1;
 
          }
    };
    
    public static Comparator<TrackerConstraint> leastBridgesComparator = 
			new Comparator<TrackerConstraint>() {
        @Override
        public int compare(TrackerConstraint t1, TrackerConstraint t2) {
        	return  - mostBridgesComparator.compare(t1,t2);
        }
    };
	
	
	public String asString() {
		int numUnsolvedIslands = (int)
				(constraint.solidIslands.stream()
						.filter(i -> !i.tracker.solved).count());
		int numUnsolvedBridges = (int)
				(constraint.solidIslands.stream()
						.mapToInt(k -> k.tracker.numUnsolvedBridges)
						.sum());
		return String.format(
		 "Constraint: %s %s maxP %d minP %d   Unsolved Bridges %d, Unsolved Islands %d  massAlloc %d maxMassRemaining %d\n", 
				constraint.toString(),
				solved ? "Solved" : "Open",
				maxPossibleScore, 
				minPossibleScore,
				numUnsolvedBridges,
				numUnsolvedIslands
				);
		
	}
	
	public String Dump() {
		return constraint.toString()
				+ super.Dump();
				
	}
	
}
