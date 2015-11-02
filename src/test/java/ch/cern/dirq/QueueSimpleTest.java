/**
 * Unit tests for {@link ch.cern.dirq.QueueSimple}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.posix.Timeval;
import ch.cern.mig.utils.FileUtils;

/**
 * Unit tests for {@link ch.cern.dirq.QueueSimple}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */
public class QueueSimpleTest extends QueueTestBase {
    private String qsPath;
    private QueueSimple qsObject;

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
        Assert.assertEquals(multiPath, qsObject.getQueuePath());
        Assert.assertTrue(new File(multiPath).isDirectory());
        FileUtils.recursiveDelete(new File(multiPath));
    }

    /**
     * Test queue creation.
     *
     * @throws IOException
     */
    @Test
    public void creation() throws IOException {
        Assert.assertEquals(qsPath, qsObject.getQueuePath());
        Assert.assertTrue(new File(qsPath).isDirectory());
    }

    /**
     * Test addDir.
     *
     * @throws IOException
     */
    @Test
    public void addDir() throws IOException {
        String dirname = qsObject.addDir();
        Assert.assertEquals(8, dirname.length());
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
        Assert.assertTrue(new File(qsPath + File.separator + elem).exists());
        Assert.assertEquals(data,
                FileUtils.readToString(qsPath + File.separator + elem));
        byte[] binaryData = data.getBytes();
        elem = qsObject.add(binaryData);
        Assert.assertTrue(new File(qsPath + File.separator + elem).exists());
        Assert.assertEquals(data,
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
        Assert.assertTrue(new File(tmpName).exists());
        String newName = qsObject.addPath(tmpName);
        Assert.assertFalse(new File(tmpName).exists());
        Assert.assertTrue(new File(qsPath + File.separator + newName).exists());
        // Assert.assertEquals(1, new File(tmpDir).listFiles().length);
        Assert.assertEquals(data,
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
        Assert.assertTrue(qsObject.lock(elemName));
        Assert.assertTrue(new File(elemPath + QueueSimple.LOCKED_SUFFIX).exists());
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
        Assert.assertEquals(data, qsObject.get(elem));
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
        Assert.assertTrue(Arrays.equals(dataBytes, qsObject.getAsByteArray(elem)));
    }

    /**
     * Test count.
     *
     */
    @Test
    public void count() throws IOException {
        qsObject.add("foo bar 1");
        Assert.assertEquals(1, qsObject.count());
        qsObject.add("foo bar 2");
        Assert.assertEquals(2, qsObject.count());
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
        Assert.assertEquals(2, count);
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
        Assert.assertEquals(1, qsObject.count());
        String inDir = new File(qsPath).listFiles()[0].getPath();
        new File(inDir + File.separator + "foo.bar").createNewFile();
        Assert.assertEquals(1, qsObject.count());
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
        Assert.assertEquals(5, qsObject.count());
        for (String element : qsObject) {
            qsObject.lock(element);
            qsObject.remove(element);
        }
        Assert.assertEquals(0, qsObject.count());
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
        Assert.assertEquals(1, qsObject.count());
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
        Assert.assertEquals(1, qsObject.count());
        String elem = qsObject.iterator().next();
        qsObject.lock(elem);
        Assert.assertEquals(1, qsObject.count());
        String elemPathLock = qsObject.getQueuePath() + File.separator + elem
                + QueueSimple.LOCKED_SUFFIX;
        Assert.assertTrue(new File(elemPathLock).exists());
        Thread.sleep(2000);
        qsObject.purge(1);
        Assert.assertFalse(new File(elemPathLock).exists());
        Assert.assertEquals(1, qsObject.count());
        Assert.assertEquals(1, new File(qsObject.getQueuePath()).listFiles().length);
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
        Assert.assertEquals(1, qsObject.count());
        String elem = qsObject.iterator().next();
        String elemPath = qsObject.getQueuePath() + File.separator + elem;
        String elemPathLock = elemPath + QueueSimple.LOCKED_SUFFIX;
        qsObject.lock(elem);
        Assert.assertEquals(1, qsObject.count());
        Assert.assertTrue(new File(elemPathLock).exists());
        new File(elemPath).delete();
        Assert.assertFalse(new File(elemPath).exists());
        Assert.assertTrue(new File(elemPathLock).exists());
        Assert.assertEquals(0, qsObject.count());
        Thread.sleep(2000);
        qsObject.purge(1);
        Assert.assertFalse(new File(elemPathLock).exists());
        Assert.assertEquals(0, qsObject.count());
        Assert.assertEquals(1, new File(qsObject.getQueuePath()).listFiles().length);
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
        Assert.assertEquals(1, qsObject.count());
        String[] list = qsFile.list();
        Assert.assertEquals("foo: " + StringUtils.join(list, ", "), 1, list.length);
        qsObject.add("bar");
        Assert.assertEquals("foo + bar count", 2, qsObject.count());
        qsObject.purge();
        Assert.assertEquals("still foo + bar count", 2, qsObject.count());

        String elem = qsObject.iterator().next();
        qsObject.lock(elem);
        qsObject.remove(elem);
        Assert.assertEquals(1, qsObject.count());
        qsObject.purge();
        list = qsFile.list();
        Assert.assertEquals("1 foo or bar: " + StringUtils.join(list, ", "), 1, list.length);

        qsObject.add("abc");
        Assert.assertEquals("abc + 1 count", 2, qsObject.count());
        for (String element : qsObject) {
            qsObject.lock(element);
        }
        Iterator<String> it = qsObject.iterator();
        String elem1 = it.next();
        String lockPath1 = qsObject.getQueuePath() + File.separator + elem1
                + QueueSimple.LOCKED_SUFFIX;
        Assert.assertTrue(new File(lockPath1).exists());
        long[] backInTime = new long[]{
                (System.currentTimeMillis() / 1000) - 25, 0};
        Timeval[] timeval = (Timeval[]) new Timeval().toArray(2);
        timeval[0].setTime(backInTime);
        timeval[1].setTime(backInTime);
        Posix.posix.utimes(lockPath1, timeval);
        qsObject.purge(10);
        Assert.assertFalse(new File(lockPath1).exists());

        Assert.assertEquals("2 left count", 2, qsObject.count());
        String elem2 = it.next();
        String lockPath2 = qsObject.getQueuePath() + File.separator + elem2
                + QueueSimple.LOCKED_SUFFIX;
        Assert.assertTrue(new File(lockPath2).exists());
    }

}
