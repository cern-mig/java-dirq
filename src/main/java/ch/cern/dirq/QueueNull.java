package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * QueueNull - object oriented interface to a <i>null</i> directory based queue.
 * <p>
 * The goal of this module is to offer a <i>null</i> queue system using the same
 * API as the other directory queue implementations. The queue will behave like
 * a black hole: added data will disappear immediately so the queue will
 * therefore always appear empty.
 * <p>
 * This can be used for testing purposes or to discard data like one would do
 * on Unix by redirecting output to <code>/dev/null</code>.
 * <p>
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 * <p>
 * All the methods that add data will return an invalid element name.
 * <p>
 * All the methods that work on elements will throw an
 * <code>UnsupportedOperationException</code> exception.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

public class QueueNull implements Queue {

    /**
     * Constructor for the null directory queue.
     */
    public QueueNull() {
    }

    @Override
    public String getId() {
        return "NULL";
    }

    @Override
    public String add(final String data) {
        return "";
    }

    @Override
    public String add(final byte[] data) {
        return "";
    }

    @Override
    public String addPath(final String path) throws IOException {
        File file = new File(path);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("cannot delete: " + path);
            }
        }
        return "";
    }

    @Override
    public String get(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getAsByteArray(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPath(final String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lock(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lock(final String name, final boolean permissive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unlock(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unlock(final String name, final boolean permissive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(final String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int count() {
        return 0;
    }

    @Override
    public void purge() {
    }

    @Override
    public void purge(final int maxLock) {
    }

    @Override
    public void purge(final int maxLock, final int maxTemp) {
    }

    /**
     * Iterator for the null directory queue.
     */
    @Override
    public Iterator<String> iterator() {
        return new QueueNullIterator();
    }

    /**
     * Iterator for the null directory queue (private).
     */
    private static class QueueNullIterator implements Iterator<String> {

        /**
         * Returns true if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            return false;
        }

        /**
         * Returns the next element in the iteration.
         */
        @Override
        public String next() {
            throw new NoSuchElementException();
        }

        /**
         * Removes from the underlying collection the last element returned by this iterator.
         */
        @Override
        public void remove() {
        }

    }

}
