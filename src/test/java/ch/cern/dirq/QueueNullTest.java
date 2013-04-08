package ch.cern.dirq;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Unit test for {@link ch.cern.dirq.QueueNull}.
 *
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 */
public class QueueNullTest extends QueueTestBase {

    /**
     * Test constructor.
     */
    @Test
    public void creation() {
        QueueNull blackHole = new QueueNull();
        assertEquals("NULL", blackHole.getId());
        // assertEquals("NULL", blackHole.getPath());
    }

    /**
     * Test add.
     */
    @Test
    public void add() {
        QueueNull blackHole = new QueueNull();
        String elem = blackHole.add("foo bar");
        assertEquals("", elem);
    }

    /**
     * Test addPath.
     *
     * @throws IOException
     */
    @Test
    public void addPath() throws IOException {
        QueueNull blackHole = new QueueNull();
        String name = dir + "foo bar";
        File file = new File(name);
        file.createNewFile();
        assertTrue(new File(name).exists());
        blackHole.addPath(name);
        assertFalse(new File(name).exists());
    }

    /**
     * Test lock.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void lock() {
        QueueNull blackHole = new QueueNull();
        blackHole.lock("");
    }

    /**
     * Test unlock.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void unlock() throws Exception {
        QueueNull blackHole = new QueueNull();
        blackHole.unlock("");
    }

    /**
     * Test get.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void get() {
        QueueNull blackHole = new QueueNull();
        blackHole.get("");
    }

    /**
     * Test count.
     */
    @Test
    public void count() {
        QueueNull blackHole = new QueueNull();
        blackHole.add("foo bar 1");
        assertEquals(0, blackHole.count());
        blackHole.add("foo bar 2");
        assertEquals(0, blackHole.count());
    }

    /**
     * Test remove.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        QueueNull blackHole = new QueueNull();
        blackHole.remove("");
    }

    /**
     * Test purge.
     */
    @Test
    public void purge() {
        QueueNull blackHole = new QueueNull();
        blackHole.purge();
    }
}
