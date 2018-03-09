

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A blocking queue implementation used to relay any input signals
 * from GUI or keyboard to the controller.
 * Generic message implementation to be agnostic to use cases.
 * @author legge
 *
 */
public class InputSignal {
	BlockingQueue<Message> queue;
	/**
	 * Create the blocking queue
	 */
	public InputSignal(){
		queue = new ArrayBlockingQueue<>(10);
	}
	/**
	 * Add a new message to the queue
	 * @param signal - message to add
	 */
	public void add(String signal) {
		queue.add(new Message(signal));
	}
	/**
	 * Block until there is a message in queue,
	 * @return first message in queue
	 */
	public String getNext() {
		try {
			return queue.take().getMsg();
		}
		catch(InterruptedException e) {
            e.printStackTrace();
        }
		return "exit";
	}
	
	/** 
	 * Simple wrapper around String to create messages.
	 * @author legge
	 *
	 */
	private class Message {
		private String msg;
	    
		public Message(String str){
			this.msg=str;
		}

		public String getMsg() {
			return msg;
		}
	}
}
