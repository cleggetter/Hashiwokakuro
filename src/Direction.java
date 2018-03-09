
/**
 * A convenient representation for relative directions
 * from a point in the grid.
 * 
 * @author legge
 *
 */
public enum Direction {
		left(0),
		up(1),
		right (2),
		down(3);	
		public int value;
        private Direction(int value) {
                this.value = value;
        }
};
