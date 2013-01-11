package ch.cern.dirq;

import java.io.File;
import java.util.Iterator;

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
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 *
 */
public class QueueNull extends Queue {

	/**
	 * Constructor which creates a <b>null</b> directory queues which
	 * accept no parameters.
	 */
	public QueueNull() {
		path = "NULL";
		id = "NULL";
	}

	@Override
	public String add(byte[] data) {
		return "";
	}
	
	@Override
	public String add(String data) {
		return "";
	}

	@Override
	public String addPath(String path) {
		File file = new File(path);
		if (file.exists())
			file.delete();
		return "";
	}

	@Override
	public String get(String name) throws NotSupportedMethodException {
		throw new NotSupportedMethodException();
	}
	
	@Override
	public byte[] getAsByteArray(String name) throws NotSupportedMethodException {
		throw new NotSupportedMethodException();
	}

	@Override
	public String getPath(String path) throws NotSupportedMethodException {
		throw new NotSupportedMethodException();
	}

	@Override
	public boolean lock(String name, boolean permissive)
			throws NotSupportedMethodException {
		throw new NotSupportedMethodException();
	}

	@Override
	public boolean unlock(String name, boolean permissive)
			throws NotSupportedMethodException {
		throw new NotSupportedMethodException();
	}

	@Override
	public void remove(String name) throws NotSupportedMethodException {
		throw new NotSupportedMethodException();
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public void purge(int maxTemp, int maxLock) {
	}

	@Override
	public Iterator<String> iterator() {
		return null;
	}
}
