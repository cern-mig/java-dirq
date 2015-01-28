/**
 * Unit tests for {@link ch.cern.dirq.QueueSimple}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

package ch.cern.dirq;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.posix.Timeval;
import ch.cern.mig.utils.FileUtils;
import ch.cern.mig.utils.StringUtils;

public class QueueSimpleTest extends QueueTestBase {
    private String qsPath = null;
    private QueueSimple qsObject = null;

    @Before
    public void setUp() throws IOException {
        qsPath = tempPath() + File.separator + "qs";
        qsObject = new QueueSimple(qsPath);
    }

    /**
     * Test multi level directory queue creation.
     *
     * @throws IOException
     */
    @Test
    public void multiLevelDirectory() throws IOException {
        String multiPath = tempPath() + "/three/ormore//levels";
        qsObject = new QueueSimple(multiPath);
        assertEquals(multiPath, qsObject.getQueuePath());
        assertTrue(new File(multiPath).isDirectory());
        FileUtils.deleteDir(new File(multiPath));
    }

    /**
     * Test queue creation.
     *
     * @throws IOException
     */
    @Test
    public void creation() throws IOException {
        assertEquals(qsPath, qsObject.getQueuePath());
        assertTrue(new File(qsPath).isDirectory());
    }

    /**
     * Test addDir.
     *
     * @throws IOException
     */
    @Test
    public void addDir() throws IOException {
        String dirname = qsObject.addDir();
        assertEquals(8, dirname.length());
    }

    /**
     * Test add.
     *
     * @throws IOException
     */
    @Test
    public void add() throws IOException {
        String data = "abc";
        String elem = qsObject.add(data);
        assertTrue(new File(qsPath + File.separator + elem).exists());
        assertEquals(data,
                FileUtils.readToString(qsPath + File.separator + elem));
        byte[] binaryData = data.getBytes();
        elem = qsObject.add(binaryData);
        assertTrue(new File(qsPath + File.separator + elem).exists());
        assertEquals(data,
                FileUtils.readToString(qsPath + File.separator + elem));
    }

    /**
     * Test addPath.
     *
     * @throws IOException
     */
    @Test
    public void addPath() throws IOException {
        String data = "abc";
        String tmpDir = qsPath + File.separator + "elems";
        Posix.posix.mkdir(tmpDir);
        String tmpName = tmpDir + File.separator + "elem.tmp";
        File tmpFile = new File(tmpName);
        tmpFile.createNewFile();
        FileUtils.writeToFile(tmpFile, data);
        assertTrue(new File(tmpName).exists());
        String newName = qsObject.addPath(tmpName);
        assertFalse(new File(tmpName).exists());
        assertTrue(new File(qsPath + File.separator + newName).exists());
        // assertEquals(1, new File(tmpDir).listFiles().length);
        assertEquals(data,
                FileUtils.readToString(qsPath + File.separator + newName));
    }

    /**
     * Test lock/unlock.
     *
     * @throws IOException
     */
    @Test
    public void lockUnlock() throws IOException {
        String data = "abc";
        String elemName = "foobar";
        String elemPath = qsPath + File.separator + elemName;
        FileUtils.writeToFile(elemPath, data);
        assertTrue(qsObject.lock(elemName));
        assertTrue(new File(elemPath + QueueSimple.LOCKED_SUFFIX).exists());
        qsObject.unlock(elemName);
    }

    /**
     * Test get.
     *
     * @throws IOException
     */
    @Test
    public void get() throws IOException {
        String data = "abc";
        String elem = qsObject.add(data);
        qsObject.lock(elem);
        assertEquals(data, qsObject.get(elem));
    }

    /**
     * Test get as byte array.
     *
     * @throws IOException
     */
    @Test
    public void getAsByteArray() throws IOException {
        byte[] dataBytes = "abc".getBytes();
        String elem = qsObject.add(dataBytes);
        qsObject.lock(elem);
        assertTrue(Arrays.equals(dataBytes, qsObject.getAsByteArray(elem)));
    }

    /**
     * Test count.
     *
     */
    @Test
    public void count() throws IOException {
        qsObject.add("foo bar 1");
        assertEquals(1, qsObject.count());
        qsObject.add("foo bar 2");
        assertEquals(2, qsObject.count());
    }

    /**
     * Test iterate.
     *
     * @throws IOException
     */
    @Test
    public void iterate() throws IOException {
        qsObject.add("foo bar 1");
        qsObject.add("foo bar 2");
        int count = 0;
        for (String name : qsObject) {
            count++;
        }
        assertEquals(2, count);
    }

    /**
     * Test count with junk.
     *
     * @throws IOException
     */
    @Test
    public void junkCount() throws IOException {
        String data = "abc";
        qsObject.add(data);
        assertEquals(1, qsObject.count());
        String inDir = new File(qsPath).listFiles()[0].getPath();
        new File(inDir + File.separator + "foo.bar").createNewFile();
        assertEquals(1, qsObject.count());
    }

    /**
     * Test remove.
     *
     * @throws IOException
     */
    @Test
    public void remove() throws IOException {
        String data = "abc";
        for (int i = 0; i < 5; i++) {
            qsObject.add(data);
        }
        assertEquals(5, qsObject.count());
        for (String element : qsObject) {
            qsObject.lock(element);
            qsObject.remove(element);
        }
        assertEquals(0, qsObject.count());
    }

    /**
     * Test purge basic.
     *
     * @throws IOException
     */
    @Test
    public void purgeBasic() throws IOException {
        qsObject.purge();
        qsObject.purge(0, 0);
        qsObject.add("abc");
        assertEquals(1, qsObject.count());
        qsObject.purge();
    }

    /**
     * Test purge one dir.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void purgeOneDir() throws IOException, InterruptedException {
        qsObject.add("abc");
        assertEquals(1, qsObject.count());
        String elem = qsObject.iterator().next();
        qsObject.lock(elem);
        assertEquals(1, qsObject.count());
        String elemPathLock = qsObject.getQueuePath() + File.separator + elem
                + QueueSimple.LOCKED_SUFFIX;
        assertTrue(new File(elemPathLock).exists());
        Thread.sleep(2000);
        qsObject.purge(1);
        assertFalse(new File(elemPathLock).exists());
        assertEquals(1, qsObject.count());
        assertEquals(1, new File(qsObject.getQueuePath()).listFiles().length);
    }

    /**
     * Test purge one dir with an orphan lock.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void purgeOneDir2() throws IOException, InterruptedException {
        qsObject.add("abc");
        assertEquals(1, qsObject.count());
        String elem = qsObject.iterator().next();
        String elemPath = qsObject.getQueuePath() + File.separator + elem;
        String elemPathLock = elemPath + QueueSimple.LOCKED_SUFFIX;
        qsObject.lock(elem);
        assertEquals(1, qsObject.count());
        assertTrue(new File(elemPathLock).exists());
        new File(elemPath).delete();
        assertFalse(new File(elemPath).exists());
        assertTrue(new File(elemPathLock).exists());
        assertEquals(0, qsObject.count());
        Thread.sleep(2000);
        qsObject.purge(1);
        assertFalse(new File(elemPathLock).exists());
        assertEquals(0, qsObject.count());
        assertEquals(1, new File(qsObject.getQueuePath()).listFiles().length);
    }

    /**
     * Test purge multi dir.
     *
     * @throws IOException
     */
    @Test
    public void purgeMultiDir() throws IOException {
        File qsFile = new File(qsObject.getQueuePath());
        qsObject.add("foo");
        assertEquals(1, qsObject.count());
        String[] list = qsFile.list();
        assertEquals("foo: " + StringUtils.join(list), 1, list.length);
        qsObject.add("bar");
        assertEquals("foo + bar count", 2, qsObject.count());
        qsObject.purge();
        assertEquals("still foo + bar count", 2, qsObject.count());

        String elem = qsObject.iterator().next();
        qsObject.lock(elem);
        qsObject.remove(elem);
        assertEquals(1, qsObject.count());
        qsObject.purge();
        list = qsFile.list();
        assertEquals("1 foo or bar: " + StringUtils.join(list), 1, list.length);

        qsObject.add("abc");
        assertEquals("abc + 1 count", 2, qsObject.count());
        for (String element : qsObject) {
            qsObject.lock(element);
        }
        Iterator<String> it = qsObject.iterator();
        String elem1 = it.next();
        String lockPath1 = qsObject.getQueuePath() + File.separator + elem1
                + QueueSimple.LOCKED_SUFFIX;
        assertTrue(new File(lockPath1).exists());
        long[] backInTime = new long[]{
                (System.currentTimeMillis() / 1000) - 25, 0};
        Timeval[] timeval = (Timeval[]) new Timeval().toArray(2);
        timeval[0].setTime(backInTime);
        timeval[1].setTime(backInTime);
        Posix.posix.utimes(lockPath1, timeval);
        qsObject.purge(10);
        assertFalse(new File(lockPath1).exists());

        assertEquals("2 left count", 2, qsObject.count());
        String elem2 = it.next();
        String lockPath2 = qsObject.getQueuePath() + File.separator + elem2
                + QueueSimple.LOCKED_SUFFIX;
        assertTrue(new File(lockPath2).exists());
    }

}
