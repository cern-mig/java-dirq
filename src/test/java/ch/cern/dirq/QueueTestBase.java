package ch.cern.dirq;

import java.io.File;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

import junit.framework.TestCase;

/**
 * {@link ch.cern.dirq.Queue} base tests.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 *
 */
public abstract class QueueTestBase extends TestCase {
	public static final String dir = Posix.posix.getpid() + "test/";

	public QueueTestBase() {
		super();
	}

	public QueueTestBase(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		FileUtils.deleteDir(new File(dir));
		new File(dir).mkdirs();
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		FileUtils.deleteDir(new File(dir));
		super.tearDown();
	}
	
}
