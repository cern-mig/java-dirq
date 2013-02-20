package ch.cern.dirq;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.junit.Test;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.posix.Timeval;
import ch.cern.mig.utils.FileUtils;
import ch.cern.mig.utils.StringUtils;

/**
 * Unit test for {@link ch.cern.dirq.QueueSimple}.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 */
public class QueueSimpleTest extends QueueTestBase {
	private static final String qsPath =
			dir + new Random().nextInt(32000) + "qs/";

	/**
	 * Test multi level directory queue creation.
	 *
	 * @throws IOException
	 */
	@Test public void multiLevelDirectory() throws IOException {
		String multiPath = qsPath + "three/ormore//levels";
		QueueSimple qs = new QueueSimple(multiPath);
		assertEquals(multiPath, qs.getPath());
		assertTrue(new File(multiPath).isDirectory());
		FileUtils.deleteDir(new File(multiPath));
	}

	/**
	 * Test addDir.
	 * 
	 * @throws IOException
	 */
	@Test public void addDir() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String dirname = qs.addDir();
		assertEquals(8, dirname.length());
	}

	/**
	 * Test queue creation.
	 * 
	 * @throws IOException
	 */
	@Test public void creation() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		assertEquals(qsPath, qs.getPath());
		assertTrue(new File(qsPath).isDirectory());
	}

	/**
	 * Test add operation.
	 * 
	 * @throws IOException
	 */
	@Test public void add() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String elem = qs.add(data);
		assertTrue(new File(qsPath + File.separator + elem).exists());
		assertEquals(data, FileUtils.readToString(qsPath + File.separator + elem));
		byte[] binaryData = data.getBytes();
		elem = qs.add(binaryData);
		assertTrue(new File(qsPath + File.separator + elem).exists());
		assertEquals(data, FileUtils.readToString(qsPath + File.separator + elem));
	}

	/**
	 * Test addPath operation.
	 * 
	 * @throws IOException
	 */
	@Test public void addPath() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String tmpDir = qsPath + File.separator + "elems";
		Posix.posix.mkdir(tmpDir);
		String tmpName = tmpDir + File.separator + "elem.tmp";
		File tmpFile = new File(tmpName);
		tmpFile.createNewFile();
		FileUtils.writeToFile(tmpFile, data);
		assertTrue(new File(tmpName).exists());
		String newName = qs.addPath(tmpName);
		assertFalse(new File(tmpName).exists());
		assertTrue(new File(qsPath + File.separator + newName).exists());
		// assertEquals(1, new File(tmpDir).listFiles().length);
		assertEquals(data,
				FileUtils.readToString(qsPath + File.separator + newName));
	}

	/**
	 * Test lock/unlock operations.
	 * 
	 * @throws IOException
	 */
	@Test public void lockUnlock() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String elemName = "foobar";
		String elemPath = qsPath + File.separator + elemName;
		FileUtils.writeToFile(elemPath, data);
		assertTrue(qs.lock(elemName));
		assertTrue(new File(elemPath + QueueSimple.LOCKED_SUFFIX).exists());
		qs.unlock(elemName);
	}

	/**
	 * Test get operation.
	 * 
	 * @throws IOException
	 */
	@Test public void get() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		String elem = qs.add(data);
		qs.lock(elem);
		assertEquals(data, qs.get(elem));
	}
	
	/**
	 * Test get as byte array operation.
	 * 
	 * @throws IOException
	 */
	@Test public void getAsByteArray() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		byte[] dataBytes = "abc".getBytes();
		String elem = qs.add(dataBytes);
		qs.lock(elem);
		assertTrue(Arrays.equals(dataBytes, qs.getAsByteArray(elem)));
	}

	/**
	 * Test count operation.
	 * 
	 * @throws IOException
	 */
	@Test public void count() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		qs.add(data);
		assertEquals(1, qs.count());
		String inDir = new File(qsPath).listFiles()[0].getPath();
		new File(inDir + File.separator + "foo.bar").createNewFile();
		assertEquals(1, qs.count());
	}

	/**
	 * Test remove operation.
	 * 
	 * @throws IOException
	 */
	@Test public void remove() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		String data = "abc";
		for (int i = 0; i < 5; i++) {
			qs.add(data);
		}
		assertEquals(5, qs.count());
		for (String element : qs) {
			qs.lock(element);
			qs.remove(element);
		}
		assertEquals(0, qs.count());
	}

	/**
	 * Test purge basic operation.
	 * 
	 * @throws IOException
	 */
	@Test public void purgeBasic() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		qs.purge();
		qs.purge(0, 0);
		qs.add("abc");
		assertEquals(1, qs.count());
		qs.purge();
	}

	/**
	 * Test purge one dir operation.
	 * 
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test public void purgeOneDir() throws IOException, InterruptedException {
		QueueSimple qs = new QueueSimple(qsPath);
		qs.add("abc");
		assertEquals(1, qs.count());
		String elem = qs.iterator().next();
		qs.lock(elem);
		String elemPathLock = qs.getPath() + File.separator + elem
				+ QueueSimple.LOCKED_SUFFIX;
		assertTrue(new File(elemPathLock).exists());
		Thread.sleep(2000);
		qs.purge(1);
		assertFalse(new File(elemPathLock).exists());
		assertEquals(1, qs.count());
		assertEquals(1, new File(qs.getPath()).listFiles().length);
	}

	/**
	 * Test purge multi dir operation.
	 * 
	 * @throws IOException
	 */
	@Test public void purgeMultiDir() throws IOException {
		QueueSimple qs = new QueueSimple(qsPath);
		File qsPath = new File(qs.getPath());
		qs.add("foo");
		assertEquals(1, qs.count());
		String[] list = qsPath.list();
		assertEquals("foo: " + StringUtils.join(list), 1, list.length);
		qs.add("bar");
		assertEquals("foo + bar count", 2, qs.count());
		list = qsPath.list();
		assertEquals("foo + bar list: " + StringUtils.join(list),
				2, list.length);
		qs.purge();
		assertEquals("still foo + bar count", 2, qs.count());

		String elem = qs.iterator().next();
		qs.lock(elem);
		qs.remove(elem);
		assertEquals(1, qs.count());
		qs.purge();
		list = qsPath.list();
		assertEquals("1 foo or bar: " + StringUtils.join(list),
				1, list.length);

		qs.add("abc");
		assertEquals("abc + 1 count", 2, qs.count());
		list = qsPath.list();
		assertEquals("abc + 1 list: " + StringUtils.join(list),
				2, list.length);
		for (String element : qs) {
			qs.lock(element);
		}
		Iterator<String> it = qs.iterator();
		String elem1 = it.next();
		String lockPath1 = qs.getPath() + File.separator + elem1
				+ QueueSimple.LOCKED_SUFFIX;
		assertTrue(new File(lockPath1).exists());
		long[] backInTime = new long[] {
				(System.currentTimeMillis() / 1000) - 25, 0 };
		Timeval[] timeval = (Timeval[]) new Timeval().toArray(2);
		timeval[0].setTime(backInTime);
		timeval[1].setTime(backInTime);
		Posix.posix.utimes(lockPath1, timeval);
		qs.purge(10);
		assertFalse(new File(lockPath1).exists());

		assertEquals("2 left count", 2, qs.count());
		String elem2 = it.next();
		String lockPath2 = qs.getPath() + File.separator + elem2
				+ QueueSimple.LOCKED_SUFFIX;
		assertTrue(new File(lockPath2).exists());
	}
	
}
