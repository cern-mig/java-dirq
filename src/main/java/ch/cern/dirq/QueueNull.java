package ch.cern.dirq;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

/**
 * QueueNull - object oriented interface to a <b>null</b> directory based queue.
 * <p>
 * The goal of this module is to offer a <b>null</b> queue system using the same
 * API as the other directory queue implementations. The queue will behave like
 * a black hole: added data will disappear immediately so the queue will
 * therefore always appear empty.
 * <p>
 * This can be used for testing purposes or to discard data like one would do on
 * Unix by redirecting output to <i>/dev/null</i>.
 * <p>
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 * 
 */
public class QueueNull implements Queue {

	/**
	 * Constructor which creates a <b>null</b> directory queue which accept no
	 * parameters.
	 */
	public QueueNull() {
	}

	/**
	 * @return an empty String
	 */
	@Override
	public String add(byte[] data) {
		return "";
	}

	/**
	 * @return an empty String
	 */
	@Override
	public String add(String data) {
		return "";
	}

	/**
	 * Delete the File at the given path.
	 * 
	 * @return an empty String
	 */
	@Override
	public String addPath(String path) {
		File file = new File(path);
		if (file.exists())
			file.delete();
		return "";
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public String get(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public byte[] getAsByteArray(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public String getPath(String path) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	public boolean lock(String name, boolean permissive) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public boolean unlock(String name, boolean permissive) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public void remove(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Always return 0.
	 */
	@Override
	public int count() {
		return 0;
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public Iterator<String> iterator() {
		return new QueueNullIterator();
	}

	private static class QueueNullIterator implements Iterator<String> {

		/**
		 * @return false as it is a <b>null</b> queue.
		 */
		@Override
		public boolean hasNext() {
			return false;
		}

		/**
		 * @return null as it is a <b>null</b> queue.
		 */
		@Override
		public String next() {
			return null;
		}

		/**
		 * Does not do anything.
		 */
		@Override
		public void remove() {
		}

	}

	/**
	 * @return String "NULL"
	 */
	@Override
	public String getId() {
		return "NULL";
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public boolean lock(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Not implemented, always throws UnsupportedOperationException.
	 * 
	 * @throws UnsupportedOperationException
	 *             as does not make sense for a <b>null</b> queue.
	 */
	@Override
	public boolean unlock(String name) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Does not do anything.
	 */
	@Override
	public void purge() {
	}

	/**
	 * Does not do anything.
	 */
	@Override
	public void purge(Map<String, Integer> options) {
	}

	/**
	 * Does not do anything.
	 */
	@Override
	public void purge(int maxLock) {
	}

	/**
	 * Does not do anything.
	 */
	@Override
	public void purge(int maxTemp, int maxLock) {
	}
}
