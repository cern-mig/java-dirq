package ch.cern.dirq;

import java.io.File;
import java.io.IOException;

import ch.cern.dirq.QueueNull;

/**
 * Unit test for {@link ch.cern.dirq.QueueNull}.
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class QueueNullTest extends QueueTest {

	/**
	 * Create the test case
	 * 
	 * @param name
	 *            name of the test case
	 */
	public QueueNullTest(String name) {
		super(name);
	}

	/**
	 * Test constructor.
	 */
	public void testCreation() {
		QueueNull blackHole = new QueueNull();
		assertEquals("NULL", blackHole.getId());
		assertEquals("NULL", blackHole.getPath());
	}

	/**
	 * Test add.
	 */
	public void testAdd() {
		QueueNull blackHole = new QueueNull();
		String elem = blackHole.add("foo bar");
		assertEquals("", elem);
	}

	/**
	 * Test addPath.
	 * 
	 * @throws IOException
	 */
	public void testAddPath() throws IOException {
		QueueNull blackHole = new QueueNull();
		String name = dir + "foo bar";
		File file = new File(name);
		file.createNewFile();
		assertTrue(new File(name).exists());
		blackHole.addPath(name);
		assertFalse(new File(name).exists());
	}

	/**
	 * Test lock unlock.
	 * 
	 * @throws Exception
	 */
	public void testLockUnlock() throws Exception {
		boolean thrown = true;
		QueueNull blackHole = new QueueNull();
		try {
			blackHole.lock("");
		} catch (NotSupportedMethodException e) {
			thrown = thrown && true;
		}
		try {
			blackHole.unlock("");
		} catch (NotSupportedMethodException e) {
			thrown = thrown && true;
		}
		assertTrue(thrown);
	}

	/**
	 * Test get.
	 */
	public void testget() {
		boolean thrown = true;
		QueueNull blackHole = new QueueNull();
		try {
			blackHole.get("");
		} catch (NotSupportedMethodException e) {
			thrown = thrown && true;
		}
		assertTrue(thrown);
	}

	/**
	 * Test count.
	 */
	public void testCount() {
		QueueNull blackHole = new QueueNull();
		blackHole.add("foo bar 1");
		assertEquals(0, blackHole.count());
		blackHole.add("foo bar 2");
		assertEquals(0, blackHole.count());
	}

	/**
	 * Test remove.
	 */
	public void testRemove() {
		boolean thrown = true;
		QueueNull blackHole = new QueueNull();
		try {
			blackHole.remove("");
		} catch (NotSupportedMethodException e) {
			thrown = thrown && true;
		}
		assertTrue(thrown);
	}

	/**
	 * Test purge.
	 * 
	 * @throws QueueException
	 */
	public void testPurge() throws QueueException {
		QueueNull blackHole = new QueueNull();
		blackHole.purge();
	}
}