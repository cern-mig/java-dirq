package ch.cern.dirq;

import java.io.File;

import org.junit.After;
import org.junit.Before;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

/**
 * {@link ch.cern.dirq.QueueBase} base tests.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 * 
 */
public abstract class QueueTestBase {
	protected static final String dir = Posix.posix.getpid() + "test/";

	@Before
	public void setUp() throws Exception {
		FileUtils.deleteDir(new File(dir));
		new File(dir).mkdirs();
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDir(new File(dir));
	}

}
