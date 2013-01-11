package ch.cern.dirq;

/**
 * Exception to be thrown if an error occurs in the queue handling.
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 *
 */
public class QueueException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1650156498660245202L;

	public QueueException(String string) {
		super(string);
	}

}
