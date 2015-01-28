package ch.cern.dirq;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * QueueNull - object oriented interface to a <b>null</b> directory based queue.
 * <p/>
 * The goal of this module is to offer a <b>null</b> queue system using the same
 * API as the other directory queue implementations. The queue will behave like
 * a black hole: added data will disappear immediately so the queue will
 * therefore always appear empty.
 * <p/>
 * All the methods that add data will return an invalid element name.
 * All the methods that work on elements will throw a
 * <code>UnsupportedOperationException</code> exception.
 * <p/>
 * This can be used for testing purposes or to discard data like one would do on
 * Unix by redirecting output to <i>/dev/null</i>.
 * <p/>
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

public class QueueNull implements Queue {

    /**
     * Constructor creating a null directory queue.
     */
    public QueueNull() {
    }

    @Override
    public String getId() {
        return "NULL";
    }

    @Override
    public String add(String data) {
        return "";
    }

    @Override
    public String add(byte[] data) {
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
    public String get(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public byte[] getAsByteArray(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPath(String path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lock(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean lock(String name, boolean permissive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unlock(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean unlock(String name, boolean permissive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(String name) {
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
    public void purge(Integer maxLock) {
    }

    @Override
    public void purge(Integer maxLock, Integer maxTemp) {
    }

    /**
     * Iterator over QueueNull implementation.
     */
    @Override
    public Iterator<String> iterator() {
        return new QueueNullIterator();
    }

    private static class QueueNullIterator implements Iterator<String> {

        /**
         * Return true if there are still elements to be iterated.
         */
        @Override
        public boolean hasNext() {
            return false;
        }

        /**
         * Return the next element to be iterated.
         */
        @Override
        public String next() {
            throw new NoSuchElementException();
        }

        /**
         * Make sure visited element is removed from the list of iterable items.
         */
        @Override
        public void remove() {
        }

    }

}
