package ch.cern.dirq;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * QueueNull - object oriented interface to a null directory based queue.
 * <p>
 * The goal of this module is to offer a <b>null</b> queue system using the
 * same API as the other directory queue implementations. The queue will
 * behave like a black hole: added data will disappear immediately so the
 * queue will therefore always appear empty.
 * <p>
 * This can be used for testing purposes or to discard data like one
 * would do on Unix by redirecting output to <i>/dev/null</i>.
 * <p>
 * Please refer to {@link ch.cern.dirq.QueueBase} for general information about
 * directory queues.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 *
 */
public class QueueNull implements Queue {

	/**
	 * Constructor which creates a <b>null</b> directory queues which
	 * accept no parameters.
	 */
	public QueueNull() {
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#add(byte[])
	 */
	@Override
	public String add(byte[] data) {
		return "";
	}
	
	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#add(String)
	 */
	@Override
	public String add(String data) {
		return "";
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#addPath(String)
	 */
	@Override
	public String addPath(String path) {
		File file = new File(path);
		if (file.exists())
			file.delete();
		return "";
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#get(String)
	 */
	@Override
	public String get(String name) {
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#getAsByteArray(String)
	 */
	@Override
	public byte[] getAsByteArray(String name) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#getPath(String)
	 */
	@Override
	public String getPath(String path) {
		throw new UnsupportedOperationException();
	}

	
	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#lock(String, boolean)
	 */@Override
	public boolean lock(String name, boolean permissive) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#unlock(String, boolean)
	 */
	@Override
	public boolean unlock(String name, boolean permissive) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#remove(String)
	 */
	@Override
	public void remove(String name) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#count()
	 */
	@Override
	public int count() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#purge(int, int)
	 */
	@Override
	public void purge(int maxTemp, int maxLock) {
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#getPath(String)
	 */
	@Override
	public Iterator<String> iterator() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#getId()
	 */
	@Override
	public String getId() {
		return "NULL";
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#lock(java.lang.String)
	 */
	@Override
	public boolean lock(String name) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#unlock(java.lang.String)
	 */
	@Override
	public boolean unlock(String name) {
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#purge()
	 */
	@Override
	public void purge() {
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#purge(java.util.Map)
	 */
	@Override
	public void purge(Map<String, Integer> options) {
	}

	/* (non-Javadoc)
	 * @see ch.cern.dirq.Queue#purge(int)
	 */
	@Override
	public void purge(int maxLock) {
	}
}
