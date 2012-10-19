package ch.cern.dirq;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import ch.cern.mig.utils.RegExpFilenameFilter;

/**
 * Queue iterator to be implemented by each directory queue implementation.
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public abstract class QueueIterator implements Iterator<String> {
	protected Queue queue = null;
	protected ArrayList<String> dirs = new ArrayList<String>();
	protected ArrayList<String> elts = new ArrayList<String>();

	/**
	 * Constructor which creates an iterator over the given queue.
	 * @param queue the queue over which the iterator should be created
	 */
	public QueueIterator(Queue queue) {
		this.queue = queue;
		File[] content = new File(queue.getPath())
				.listFiles(new RegExpFilenameFilter(Queue.DirectoryRegexp));
		for (File dir : content) {
			dirs.add(dir.getName());
		}
		Collections.sort(dirs);
	}

	/**
	 * Return true if there are still elements to be iterated.
	 */
	public boolean hasNext() {
		if (!elts.isEmpty())
			return true;
		if (buildElements())
			return true;
		return false;
	}

	/**
	 * Collect the elements to be browsed.
	 * @return true if succeed
	 */
	public abstract boolean buildElements();

	/**
	 * Return the next element to be iterated.
	 */
	public String next() {
		if (!elts.isEmpty())
			return elts.remove(0);
		if (buildElements())
			return elts.remove(0);
		throw new NoSuchElementException();
	}

	/**
	 * Make sure visited element is removed from the list of
	 * iterable items.
	 */
	public void remove() {
		// already removed
	}

}
