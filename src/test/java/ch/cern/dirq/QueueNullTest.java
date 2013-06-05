package ch.cern.dirq;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ch.cern.dirq.QueueNull}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2013
 */

public class QueueNullTest extends QueueTestBase {
    private QueueNull qnObject = null;

    @Before
    public void setUp() {
        qnObject = new QueueNull();
    }

    /**
     * Test constructor.
     */
    @Test
    public void creation() {
        assertEquals("NULL", qnObject.getId());
        // assertEquals("NULL", qnObject.getPath());
    }

    /**
     * Test add.
     */
    @Test
    public void add() {
        String elem = qnObject.add("foo bar");
        assertEquals("", elem);
    }

    /**
     * Test addPath.
     *
     * @throws IOException
     */
    @Test
    public void addPath() throws IOException {
        String name = tempPath() + File.separator + "foo bar";
        File file = new File(name);
        file.createNewFile();
        assertTrue(new File(name).exists());
        qnObject.addPath(name);
        assertFalse(new File(name).exists());
    }

    /**
     * Test lock.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void lock() {
        qnObject.lock("");
    }

    /**
     * Test unlock.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void unlock() throws Exception {
        qnObject.unlock("");
    }

    /**
     * Test get.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void get() {
        qnObject.get("");
    }

    /**
     * Test count.
     */
    @Test
    public void count() {
        qnObject.add("foo bar 1");
        assertEquals(0, qnObject.count());
        qnObject.add("foo bar 2");
        assertEquals(0, qnObject.count());
    }

    /**
     * Test remove.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        qnObject.remove("");
    }

    /**
     * Test purge.
     */
    @Test
    public void purge() {
        qnObject.purge();
    }
}
