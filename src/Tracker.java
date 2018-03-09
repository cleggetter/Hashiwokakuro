import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Tracker base class which is the main data store for a solution candidate
 * 
 * There will be a tracker for each bridge, island and constraint
 * 
 * This will track the bounds of the possible scores of each entity and
 * mark whether it is solved or not.
 * 
 * @author legge
 *
 */
public class Tracker {
	static AtomicInteger nextId = new AtomicInteger();
	// use the classname for the logger, this way you can refactor
    // private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    
	protected final int uid;   // A unique id for this tracker.
	
	protected Helper thisLog;  // A logger
	
	protected boolean valid;              // Tracker is consistent with valid solution
	protected boolean solved;  // Tracker is completely resolved
	protected boolean massIsKnown; // know the final score, but may not be solved
	
	//  Max and min give a range of allowable total scores;
	//  If these are equal then we know the expected final score
	protected int maxPossibleScore;
	protected int minPossibleScore;
	
	// This is the score currently allocated to the tracker
	//  i.e. bridges assigned to islands, islands total for constraints
	protected int currentScore;
	
	//  MassAllocated is the currently known score
	//  note that this may be less than the min
	//  since this is the work in progress not the final score
	protected int massAllocated;
	
	//  This is a group id for a set of connected elements.
	//  if two trackers are in the same group then they are
	//  connected by non-zero paths.
	protected int groupId;
	
	
	protected SolverStatus solverStatus;
	
	String entity;
	// For recording a stack of histories
	ArrayList<Tracker> history;
	int historyId;
	
	/**
	 * Constructor initializes all common variables
	 */
	public Tracker() {
		thisLog = new Helper("TrackerBase");
		entity = "Base";
		uid = nextId.incrementAndGet();
		valid = true;
		solved = false;
		currentScore = 0;
		massAllocated = 0;
		massIsKnown = false;
		groupId = 0;
		history = new ArrayList<Tracker> ();
		historyId = 0;
		solverStatus = null;
	}
	
	/**
	 * Checks whether 2 trackers are identical via comparison of the unique ids
	 * @param t1 Tracker 1
	 * @param t2 Tracker 2
	 * @return true if identical, false otherwise
	 */
	public static boolean sameTracker(Tracker t1, Tracker t2)
	{
		return (t1.uid == t2.uid);
	}

	public boolean nonZeroScoreAvailable() {
		return (maxPossibleScore > 0);
	}
	public boolean zeroScoreAvailable() {
		return (minPossibleScore == 0);
	}
	public boolean nonZeroScore() {
		return (minPossibleScore > 0);
	}
	public boolean zeroScore() {
		return (maxPossibleScore == 0);
	}
	public int maxMinusMin() {
		return maxPossibleScore - minPossibleScore;
	}
	
	//
	// A placeholder for independent overrides
	public boolean update() {
		return true;
	}

	// A placeholder for overrides
	public int updateGroupId() {
		return groupId;
	}
	//
	// Fix the final score if we know it
	public boolean freezeFinalScore(int v) {
		minPossibleScore = v;
		maxPossibleScore = v;
		massAllocated = v;
		// Note the current score is not frozen
		update();
		return true;
	}
	
	
	// Accessors
	public String Entity() {
		return entity;
	}
	boolean setMaxPossibleScore(int a) {
		if (a > maxPossibleScore) {
			return false;
		}
		maxPossibleScore = a;
		update();
		return true;
	}
	boolean setMinPossibleScore(int a) {
		if (a < minPossibleScore) {
			return false;
		}
		minPossibleScore = a;
		update();
		return true;
	}
	boolean setKnownScore(int a) {
		massAllocated = a;
		massIsKnown = true;
		update();
		return true;
	}
	
	// Return the score according to summing the (min) assigned values
	// of sub components
	public int getCurrentScore() {
		return currentScore;
	}
	// Return the score according to current knowledge
	// this could be current allocated / min score or known final
	public int getKnownScore() {
		return massAllocated;
	}
	
	public int getMinPossibleScore() {
		return minPossibleScore;
	}
	public int getMaxPossibleScore() {
		return maxPossibleScore;
	}
	public int getGroupId() {
		return groupId;
	}
	public boolean isValid() {
		return valid;
	}
	public boolean isSolved() {
		return solved;
	}
	public boolean isMassKnown() {
		return massIsKnown;
	}

	public boolean allowMin(int m) {
		return (m <= maxPossibleScore)
				&& (m >= minPossibleScore);
	}

	public boolean allowMax(int m) {
		return (m <= maxPossibleScore)
				&& (m >= minPossibleScore)
				&& (m >= massAllocated);
	}
	
	public boolean isError(boolean condition, String desc) {
		if (condition) {
			//thisLog.Error( desc);
			return true;
		}
		return false;
	}
	
	// This is a helper for common checks
	public boolean validateTracker(
			Tracker current,
			Tracker changed,
			String source) {
		boolean err = false;
		
		err = isError( current.solved, source + " Changing solved Tracker") || err;
		err = isError( (changed.maxPossibleScore > current.maxPossibleScore), 
				source + " maxPossible bounds: > existing max") || err;
			
		err = isError( (changed.minPossibleScore < current.minPossibleScore), 
					source + " minPossible bounds: < existing min") || err;
			
		err = isError( (changed.massAllocated < current.massAllocated),
					source + " massAllocated > maxPossible bounds") || err;
		
		if (err) {
			//thisLog.Log(thisLog.errActive, "BROKEN CURRENT TRACKER: " + current.Dump());
			//thisLog.Log(thisLog.errActive, "BROKEN CHANGED TRACKER: " + changed.Dump());
		}
		return !err;
	}
	
	// This is a helper for common checks
	public boolean violation() {
		boolean err = false;
		
		if (solved) {
			if (massIsKnown) {
				if (massAllocated != currentScore) {
					err = true;
				}
			}
		}
		else {
			if (minPossibleScore > maxPossibleScore){
				err = true;
			}
		}
		if (err) {
			thisLog.Log("Error in tracker:" + Dump());
		}
		return err;
	}
	
	public void addHistory(int id) {
		Tracker tc = new Tracker();
		tc.copyBase(this);
		tc.historyId = id;
		history.add(tc);	
	}
	
	
	public static Comparator<Tracker> historyComparator = new Comparator<Tracker>() {
        @Override
        public int compare(Tracker t1, Tracker t2) {
            return (t1.historyId < t2.historyId ? -1 :
                    (t1.historyId == t2.historyId) ? 0 : 1);
          }
    };
	
	public boolean revertHistory(int id) {
		// Remove all histories in the new future;
		history.removeIf( h-> (h.historyId > id));
		if (!history.isEmpty()){
			Collections.sort(history, historyComparator);
			this.copyBase(history.get(history.size()-1));
			// update();
			//if (thisLog != null) {
			//thisLog.Log(true, "Reverting ID " + historyId  + "  asked " + id);
			//}
			return true;
		}
		return false;
	}
	
	public boolean revertHistoryButKeep(int id) {
		// Remove all histories in the new future;
		int h = 0;
		while ((h < history.size() -1) 
			&& (history.get(h+1).historyId <= id)) {
			h = h+1;
		}
		if (h < history.size()-1){
			this.copyBase(history.get(h));
			return true;
		}
		return false;
	}
	
	public void copyBase(Tracker tb) {
		minPossibleScore = tb.minPossibleScore;
		maxPossibleScore = tb.maxPossibleScore;
		massAllocated = tb.massAllocated;
		currentScore = tb.currentScore;
		solved = tb.solved;
		massIsKnown = tb.massIsKnown;
		groupId = tb.groupId;
		valid = tb.valid;
		historyId = tb.historyId;
	}

	
	
	// Pass in a function
	//  compute   function on (a) and (b)
	//  return if different
	public boolean diffBase(Tracker tb, Helper logger) {
		
		class CmpPair {
			Function<Tracker, Integer> intFunc;
			Function<Tracker, Boolean> boolFunc;
			String label;
			
			CmpPair() {}
			public CmpPair addInt(String l, Function<Tracker, Integer> ifunc) {
				intFunc = ifunc;
				label = l;
				boolFunc = null;
				return this;
			}
			public CmpPair addBool(String l, Function<Tracker, Boolean> bfunc) {
				boolFunc = bfunc;
				intFunc = null;
				label = l;
				return this;
			}
			public boolean same(Tracker t1, Tracker t2) {
				if (boolFunc != null) {
					return boolFunc.apply(t1) == boolFunc.apply(t2);
				}
				if (intFunc != null) {
					return intFunc.apply(t1) == intFunc.apply(t2);
				}
				return false;
			}
			public String diffString(Tracker t1, Tracker t2) {
				if (boolFunc != null) {
					return String.format("%s  %s vs %s", label,
							boolFunc.apply(t1),boolFunc.apply(t2));
				}
				if (intFunc != null) {
					return String.format("%s  %d vs %d", label,
							intFunc.apply(t1),boolFunc.apply(t2));
				}
				return "No function";
			}
		}
		ArrayList<CmpPair> cmps= new ArrayList<CmpPair>();
		cmps.add( new CmpPair().addInt("MinPossible", Tracker::getMinPossibleScore));
		cmps.add( new CmpPair().addInt("MaxPossible", Tracker::getMaxPossibleScore));
		cmps.add( new CmpPair().addInt("KnownScore", Tracker::getKnownScore));
		cmps.add( new CmpPair().addInt("CurrentScore", Tracker::getCurrentScore));
		cmps.add( new CmpPair().addInt("GroupId", Tracker::getGroupId));
		
		cmps.add( new CmpPair().addBool("isSolved", Tracker::isSolved));
		//cmps.add( new CmpPair().addBool("isValid", Tracker::isValid));
		cmps.add( new CmpPair().addBool("isMassKnown", Tracker::isMassKnown));
		
		boolean diff = false;
		for (CmpPair cp : cmps) {
			if (!cp.same(this, tb)) {
				logger.addSBuilder(cp.diffString(this, tb));
				diff = true;
			}
		}
		if (diff) {
			logger.Error( logger.getSBuilder());
			logger.resetSBuilder();
		}
		
		return diff;
	
	}
	
	
	public String Dump(String sep) {
		return String.join(sep,  
			String.format("Valid %s  ", valid)
			,String.format("Solved: %s  ", solved)
			, String.format("Min: %s  ", minPossibleScore)
			, String.format("Max: %s  ", maxPossibleScore)
			, String.format("Known mass %s  %d", massIsKnown ? 
					"Yes " : "No" , massAllocated )
			, String.format("Current %s  ", currentScore)
			, String.format("GroupId %s  ", groupId)
			, String.format("HistoryId %s  ", historyId)
			);
			
	}
	public String Dump() {
		return Dump(System.getProperty("line.separator"));

	}
}
