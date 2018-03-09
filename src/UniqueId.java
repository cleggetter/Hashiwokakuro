import java.util.concurrent.atomic.AtomicInteger;

/**
 * Syntactic sugar to get unique id for objects
 * @author legge
 *
 */
public class UniqueId {
	static AtomicInteger nextId = new AtomicInteger();
	
	private UniqueId() {
	}
	
	public static int getUid(){
		return nextId.incrementAndGet();
	}
}
