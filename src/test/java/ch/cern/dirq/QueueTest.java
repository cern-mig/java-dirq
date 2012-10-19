package ch.cern.dirq;

import java.io.File;

import junit.framework.TestCase;

/**
 * {@link ch.cern.dirq.Queue} base tests.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public abstract class QueueTest extends TestCase {
	public static final String dir = "test/";

	public QueueTest() {
		super();
	}

	public QueueTest(String name) {
		super(name);
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}

	@Override
	protected void setUp() throws Exception {
		deleteDir(new File(dir));
		new File(dir).mkdirs();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		deleteDir(new File(dir));
		super.tearDown();
	}

}