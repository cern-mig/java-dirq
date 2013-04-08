package ch.cern.dirq;

import java.io.IOException;
import java.util.Map;

/**
 * Queue - object oriented interface to a directory based queue
 * <p/>
 * <h3>Description</h3> The goal of this module is to offer a queue system using
 * the underlying filesystem for storage, security and to prevent race
 * conditions via atomic operations. It focuses on simplicity, robustness and
 * scalability.
 * <p>
 * This module allows multiple concurrent readers and writers to interact with
 * the same queue.
 * <p>
 * Different implementations are available so readers and writers can be written
 * in different programming languages:
 * <ul>
 * <li>A Perl implementation of the same algorithms is available at <a
 * href="http://search.cpan.org/dist/Directory-Queue/">
 * http://search.cpan.org/dist/Directory-Queue/</a>
 * <li>A Python implementation of the same algorithms is available at <a
 * href="https://github.com/cern-mig/python-dirq">
 * https://github.com/cern-mig/python-dirq</a>
 * </ul>
 * <p>
 * There is no knowledge of priority within a queue. If multiple priorities are
 * needed, multiple queues should be used.
 * <p>
 * <h3>Terminology</h3>
 * An element is something that contains one or more pieces of data. With
 * {@link ch.cern.dirq.QueueSimple} queues, an element can only contain one
 * binary string.
 * <p>
 * A queue is a "best effort" FIFO (First In - First Out) collection of
 * elements.
 * <p>
 * It is very hard to guarantee pure FIFO behavior with multiple writers using
 * the same queue. Consider for instance:
 * <ul>
 * <li>Writer1: calls the add() method
 * <li>Writer2: calls the add() method
 * <li>Writer2: the add() method returns
 * <li>Writer1: the add() method returns
 * </ul>
 * Who should be first in the queue, Writer1 or Writer2?
 * <p>
 * For simplicity, this implementation provides only "best effort" FIFO, i.e.
 * there is a very high probability that elements are processed in FIFO order
 * but this is not guaranteed. This is achieved by using a high-resolution timer
 * and having elements sorted by the time their final directory gets created.
 * <p>
 * <h3>Locking</h3> Adding an element is not a problem because the add() method
 * is atomic.
 * <p>
 * In order to support multiple reader processes interacting with the same
 * queue, advisory locking is used. Processes should first lock an element
 * before working with it. In fact, the get() and remove() methods report a
 * fatal error if they are called on unlocked elements.
 * <p>
 * If the process that created the lock dies without unlocking the element, we
 * end up with a staled lock. The purge() method can be used to remove these
 * staled locks.
 * <p>
 * An element can basically be in only one of two states: locked or unlocked.
 * <p>
 * A newly created element is unlocked as a writer usually does not need to do
 * anything more with it.
 * <p>
 * Iterators return all the elements, regardless of their states.
 * <p>
 * There is no method to get an element state as this information is usually
 * useless since it may change at any time. Instead, programs should directly
 * try to lock elements to make sure they are indeed locked.
 * <p>
 * <h3>Security</h3> There are no specific security mechanisms in this module.
 * <p>
 * The elements are stored as plain files and directories. The filesystem
 * security features (owner, group, permissions, ACLs...) should be used to
 * adequately protect the data.
 * </p>
 * By default, the process' umask is respected. See the class constructor
 * documentation if you want an other behavior. </p> If multiple readers and
 * writers with different uids are expected, the easiest solution is to have all
 * the files and directories inside the toplevel directory world-writable (i.e.
 * umask=0). Then, the permissions of the toplevel directory itself (e.g.
 * group-writable) are enough to control who can access the queue.
 *
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 */
public interface Queue extends Iterable<String> {

    /**
     * @return the queue id
     */
    public String getId();

    /**
     * Add data as a string to the queue.
     *
     * @param data data to be added to the queue
     * @return return the element name (<directory_name>/<file_name>)
     * @throws IOException if any file operation fail
     */
    public String add(String data) throws IOException;

    /**
     * Add data as byte array to the queue.
     *
     * @param data data to be added to the queue
     * @return return the element name (<directory_name>/<file_name>)
     * @throws IOException if any file operation fail
     */
    public String add(byte[] data) throws IOException;

    /**
     * Add the given file (identified by its path) to the queue and return the
     * corresponding element name, the file must be on the same filesystem and
     * will be moved to the queue.
     *
     * @param path the path of the file to be added
     * @return return the element name (<directory_name>/<file_name>)
     * @throws IOException if any file operation fail
     */
    public String addPath(String path) throws IOException;

    /**
     * Get locked element as a string.
     *
     * @param name the name of the element to be returned
     * @return return the value associated to the given name
     */
    public String get(String name);

    /**
     * Get locked element as a byte array.
     *
     * @param name the name of the element to be returned
     * @return the value associated to the given name
     */
    public byte[] getAsByteArray(String name);

    /**
     * Return the path given the name of the element.
     *
     * @param name the name of the element
     * @return the path of the element
     */
    public String getPath(String name);

    /**
     * Lock an element in permissive mode.
     *
     * @param name name of the element to be locked
     * @return <code>true</code> on success, <code>false</code> if the element
     *         could not be locked
     * @throws IOException if any file operation fail
     */
    public boolean lock(String name) throws IOException;

    /**
     * Lock an element.
     *
     * @param name       name of the element to be locked
     * @param permissive work in permissive mode
     * @return <code>true</code> on success, <code>false</code> if the element
     *         could not be locked
     * @throws IOException if any file operation fail
     */
    public boolean lock(String name, boolean permissive) throws IOException;

    /**
     * Unlock an element in non-permissive mode.
     *
     * @param name name of the element to be locked
     * @return <code>true</code> on success, <code>false</code> if the element
     *         could not be unlocked
     * @throws IOException if any file operation fail
     */
    public boolean unlock(String name) throws IOException;

    /**
     * Unlock an element.
     *
     * @param name       name of the element to be locked
     * @param permissive work in permissive mode
     * @return <code>true</code> on success, <code>false</code> if the element
     *         could not be unlocked
     * @throws IOException if any file operation fail
     */
    public boolean unlock(String name, boolean permissive) throws IOException;

    /**
     * Remove a locked element from the queue.
     *
     * @param name name of the element to be removed
     */
    public void remove(String name);

    /**
     * Return the number of elements in the queue, locked or not (but not
     * temporary).
     *
     * @return the number of elements in the queue
     */
    public int count();

    /**
     * Purge the queue by removing unused intermediate directories, removing too
     * old temporary elements and unlocking too old locked elements (aka staled
     * locks); note: this can take a long time on queues with many elements.
     * <p/>
     * It uses default value for maxTemp and maxLock
     *
     * @throws IOException if any file operation fail
     */
    public void purge() throws IOException;

    /**
     * Purge the queue by removing unused intermediate directories, removing too
     * old temporary elements and unlocking too old locked elements (aka staled
     * locks); note: this can take a long time on queues with many elements.
     *
     * @param options map containing purge options, only <i>maxLock</i> and
     *                <i>maxTemp</i> values are used, the others are ignored
     * @throws IOException if any file operation fail
     */
    public void purge(Map<String, Integer> options) throws IOException;

    /**
     * Purge the queue by removing unused intermediate directories, removing too
     * old temporary elements and unlocking too old locked elements (aka staled
     * locks); note: this can take a long time on queues with many elements.
     *
     * @param maxLock maximum time for a locked element (in seconds, default 600);
     *                if set to 0, locked elements will not be unlocked
     * @throws IOException if any file operation fail
     */
    public void purge(int maxLock) throws IOException;

    /**
     * Purge the queue by removing unused intermediate directories, removing too
     * old temporary elements and unlocking too old locked elements (aka staled
     * locks); note: this can take a long time on queues with many elements.
     *
     * @param maxTemp maximum time for a temporary element (in seconds, default
     *                300); if set to 0, temporary elements will not be removed
     * @param maxLock maximum time for a locked element (in seconds, default 600);
     *                if set to 0, locked elements will not be unlocked
     * @throws IOException if any file operation fail
     */
    public void purge(int maxTemp, int maxLock) throws IOException;

}
