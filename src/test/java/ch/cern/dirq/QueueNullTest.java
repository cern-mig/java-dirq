package ch.cern.dirq;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ch.cern.dirq.QueueNull}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2019
 */
public class QueueNullTest extends QueueTestBase {

    private QueueNull qnObject;

    @Before
    public void setUp() {
        qnObject = new QueueNull();
    }

    /**
     * Test constructor.
     */
    @Test
    public void creation() {
        Assert.assertEquals("NULL", qnObject.getId());
        // Assert.assertEquals("NULL", qnObject.getPath());
    }

    /**
     * Test add.
     */
    @Test
    public void add() {
        String elem = qnObject.add("foo bar");
        Assert.assertEquals("", elem);
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
        Assert.assertTrue(new File(name).exists());
        qnObject.addPath(name);
        Assert.assertFalse(new File(name).exists());
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
    public void unlock() {
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
     * Test remove.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void remove() {
        qnObject.remove("");
    }

    /**
     * Test count.
     */
    @Test
    public void count() {
        qnObject.add("foo bar 1");
        Assert.assertEquals(0, qnObject.count());
        qnObject.add("foo bar 2");
        Assert.assertEquals(0, qnObject.count());
    }

    /**
     * Test iterate.
     */
    @Test
    public void iterate() {
        qnObject.add("foo bar 1");
        int count = 0;
        for (String name : qnObject) {
            count++;
        }
        Assert.assertEquals(0, count);
    }

    /**
     * Test purge.
     */
    @Test
    public void purge() {
        qnObject.purge();
    }

}
