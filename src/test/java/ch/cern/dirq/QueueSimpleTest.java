package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link ch.cern.dirq.QueueSimple}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2024
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
     */
    @Test
    public void creation() throws IOException {
        Assert.assertEquals(qsPath, qsObject.getQueuePath());
        Assert.assertTrue(new File(qsPath).isDirectory());
    }

    /**
     * Test add.
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
     */
    @Test
    public void addPath() throws IOException {
        String data = "abc";
        String tmpDir = qsPath + File.separator + "elems";
        Files.createDirectories(Paths.get(tmpDir));
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
     */
    @Test
    public void lockUnlock() throws IOException {
        String data = "abc";
        String elemName = "foobar";
        String elemPath = qsPath + File.separator + elemName;
        FileUtils.writeToFile(elemPath, data);
        Assert.assertTrue(qsObject.lock(elemName));
        Assert.assertFalse(qsObject.lock(elemName, true));
        Assert.assertTrue(new File(elemPath + QueueSimple.LOCKED_SUFFIX).exists());
        Assert.assertTrue(qsObject.unlock(elemName));
        Assert.assertFalse(qsObject.unlock(elemName, true));
    }

    /**
     * Test failing lock (non permissive).
     */
    @Test(expected = FileAlreadyExistsException.class)
    public void failLock() throws IOException {
        String data = "abc";
        String elem = qsObject.add(data);
        Assert.assertTrue(qsObject.lock(elem));
        Assert.assertFalse(qsObject.lock(elem, false));
    }

    /**
     * Test failing unlock (non permissive).
     */
    @Test(expected = NoSuchFileException.class)
    public void failUnock() throws IOException {
        String data = "abc";
        String elem = qsObject.add(data);
        Assert.assertTrue(qsObject.lock(elem));
        Assert.assertTrue(qsObject.unlock(elem));
        Assert.assertFalse(qsObject.unlock(elem, false));
    }

    /**
     * Test get.
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
     */
    @Test
    public void iterate() throws IOException {
        qsObject.add("foo bar 1");
        qsObject.add("foo bar 2");
        int count = 0;
        for (String name: qsObject) {
            String[] parts = name.split(File.separator);
            Assert.assertEquals(2, parts.length);
            Assert.assertTrue(QueueSimple.DIRECTORY_REGEXP.matcher(parts[0]).matches());
            Assert.assertTrue(QueueSimple.ELEMENT_REGEXP.matcher(parts[1]).matches());
            count++;
        }
        Assert.assertEquals(2, count);
    }

    /**
     * Test count with junk.
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
     */
    @Test
    public void remove() throws IOException {
        String data = "abc";
        for (int i = 0; i < 5; i++) {
            qsObject.add(data);
        }
        Assert.assertEquals(5, qsObject.count());
        for (String element: qsObject) {
            qsObject.lock(element);
            qsObject.remove(element);
        }
        Assert.assertEquals(0, qsObject.count());
    }

    /**
     * Test purge basic.
     */
    @Test
    public void purgeBasic() throws IOException {
        qsObject.purge();
        qsObject.purge(0, 0);
        qsObject.add("abc");
        Assert.assertEquals(1, qsObject.count());
        qsObject.purge();
        Assert.assertEquals(1, qsObject.count());
    }

    /**
     * Test purge one directory.
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
     * Test purge one directory with an orphan lock.
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
     * Test purge multiple.
     */
    @Test
    public void purgeMultiDir() throws IOException {
        File qsFile = new File(qsObject.getQueuePath());
        qsObject.add("foo");
        Assert.assertEquals(1, qsObject.count());
        String[] list = qsFile.list();
        Assert.assertEquals("foo list", 1, list.length);
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
        Assert.assertEquals("foo or bar list", 1, list.length);

        qsObject.add("abc");
        Assert.assertEquals("abc + 1 count", 2, qsObject.count());
        for (String element: qsObject) {
            qsObject.lock(element);
        }
        Iterator<String> it = qsObject.iterator();
        String elem1 = it.next();
        String lockPath1 = qsObject.getQueuePath() + File.separator + elem1
            + QueueSimple.LOCKED_SUFFIX;
        File lockFile1 = new File(lockPath1);
        Assert.assertTrue(lockFile1.exists());
        Assert.assertTrue(lockFile1.setLastModified(System.currentTimeMillis() - 25000));
        qsObject.purge(10);
        Assert.assertFalse(lockFile1.exists());

        Assert.assertEquals("2 left count", 2, qsObject.count());
        String elem2 = it.next();
        String lockPath2 = qsObject.getQueuePath() + File.separator + elem2
            + QueueSimple.LOCKED_SUFFIX;
        File lockFile2 = new File(lockPath2);
        Assert.assertTrue(lockFile2.exists());
    }

}
