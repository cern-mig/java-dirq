package ch.cern.dirq;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import com.sun.jna.LastErrorException;

import ch.cern.mig.posix.FileStat;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.ProcessUtils;

/**
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class JnaPosixTest extends TestCase {
	String dir = "posixtmp";
	String f1 = dir + File.separator + "f1";
	String f2 = dir + File.separator + "f2";
	String f3 = dir + File.separator + "f3";
	String x1 = dir + File.separator + "x1";
	String x2 = dir + File.separator + "x2";
	String x3 = dir + File.separator + "x3";
	String d1 = dir + File.separator + "d1";
	String d2 = dir + File.separator + "d2";
	String d3 = dir + File.separator + "d3";

	static String line = "################################################";

	boolean OK = true;
	boolean FAIL = false;

	public JnaPosixTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		init();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public static Exception mkdir(String name) {
		try {
			Posix.posix.mkdir(name);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public static Exception rmdir(String path) {
		try {
			Posix.posix.rmdir(path);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public static Exception opendir(String path) {
		try {
			Posix.posix.opendir(path);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public static Exception open(String path) {
		try {
			Posix.posix.open(path);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public static Exception rename(String from, String to) {
		try {
			Posix.posix.rename(from, to);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public static Exception link(String from, String to) {
		try {
			Posix.posix.link(from, to);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public Exception unlink(String path) {
		try {
			Posix.posix.unlink(path);
		} catch (Exception e) {
			return e;
		}
		return null;
	}

	public static Exception stat(String path) {
		try {
			Posix.posix.stat(path);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public static Exception lstat(String path) {
		try {
			Posix.posix.lstat(path);
		} catch (LastErrorException e) {
			return e;
		}
		return null;
	}

	public void report(boolean ok, Exception exc, String test) throws Exception {
		if (ok) {
			if (exc != null)
				throw exc;
			println(test + ": ok");
		} else {
			if (exc == null)
				throw new Exception("Error expected");
			println(test + ": " + exc.getMessage());
			// if (exc instanceof LastErrorException)
			// println("" + ((LastErrorException)exc).getErrorCode());
		}
	}

	public void test_mkdir() throws Exception {
		report(OK, mkdir(d3), "mkdir(d3)");
		report(FAIL, mkdir(d1), "mkdir(d1)");
		report(FAIL, mkdir(x1 + "/d"), "mkdir(x1/d)");
		rmdir(d3);
		println(line);
	}

	public void test_rmdir() throws Exception {
		report(OK, rmdir(d1), "rmdir(d1)");
		report(FAIL, rmdir(x1), "rmdir(x1)");
		report(FAIL, rmdir(x1 + "/d"), "rmdir(x1/d)");
		Posix.posix.mkdir(d2 + "/d3");
		report(FAIL, rmdir(d2), "rmdir(d2)");
		// cleanup
		Posix.posix.mkdir(d1);
		Posix.posix.rmdir(d2 + "/d3");
		println(line);
	}

	public void test_opendir() throws Exception {
		report(OK, opendir(d1), "opendir(d1)");
		report(FAIL, opendir(x1), "opendir(x1)");
		report(FAIL, opendir(x1 + "/d"), "opendir(x1/d)");
		println(line);
	}

	public void test_rename() throws Exception {
		// setup
		mkfile(d1 + "/f");
		mkfile(d2 + "/f");
		// tests
		report(OK, rename(d1, d3), "rename(d1, d3)");
		report(FAIL, rename(x1, x2), "rename(x1, x2)");
		report(FAIL, rename(d2, d3), "rename(d2, d3)");
		report(FAIL, rename(x1, d2), "rename(x1, d2)");
		// cleanup
		Posix.posix.rename(d3, d1);
		Posix.posix.unlink(d1 + "/f");
		Posix.posix.unlink(d2 + "/f");
		println(line);
	}

	public void test_open() throws Exception {
		// tests
		report(OK, open(f3), "open(f3)");
		report(FAIL, open(f1), "open(f1)");
		report(FAIL, open(x1 + "/f"), "open(x1/f)");
		// cleanup
		Posix.posix.unlink(f3);
		println(line);
	}

	public void test_link() throws Exception {
		report(OK, link(f1, f3), "link(f1, f3)");
		report(FAIL, link(x1, x2), "link(x1, x2)");
		report(FAIL, link(f1, f2), "link(f1, f2)");
		report(FAIL, link(x1, f2), "link(x1, f2)");
		unlink(f3);
		println(line);
	}

	public void test_unlink() throws Exception {
		report(OK, unlink(f1), "unlink(f1)");
		report(FAIL, unlink(x1), "unlink(x1)");
		mkfile(f1);
		println(line);
	}

	public void test_stat() throws Exception {
		report(OK, stat(f1), "stat(f1)");
		report(OK, stat(d1), "stat(d1)");
		report(FAIL, stat(x1), "stat(x1)");
		report(FAIL, stat(d1 + "/f"), "stat(d1/f)");
		report(FAIL, stat(x1 + "/f"), "stat(x1/f)");
		println(line);
	}

	public void test_lstat() throws Exception {
		report(OK, lstat(f1), "lstat(f1)");
		report(OK, lstat(d1), "lstat(d1)");
		report(FAIL, lstat(x1), "lstat(x1)");
		report(FAIL, lstat(d1 + "/f"), "lstat(d1/f)");
		report(FAIL, lstat(x1 + "/f"), "lstat(x1/f)");
		println(line);
	}

	public void init() throws IOException {
		rmtree(dir);
		mkdir(dir);
		mkfile(dir, "f1");
		mkfile(dir, "f2");
		mkdir(d1);
		mkdir(d2);
	}

	public static boolean rmtree(String name) throws IOException {
		return QueueTest.deleteDir(new File(name));
	}

	public static boolean mkfile(String parent, String child)
			throws IOException {
		return new File(parent, child).createNewFile();
	}

	public static boolean mkfile(String name) throws IOException {
		return new File(name).createNewFile();
	}

	public static void print(String string) {
		System.out.print(string);
	}

	public static void println(String string) {
		System.out.println(string);
	}

	public static void main(String[] args) throws Exception {
		println(line);
		println("Platform: " + System.getProperty("os.name") + " - "
				+ System.getProperty("os.version") + " - "
				+ System.getProperty("os.arch"));
		println("Java: " + System.getProperty("java.version") + " - "
				+ System.getProperty("java.vendor"));
		println("current dir: " + new File(".").getAbsolutePath());
		println(line);

		JnaPosixTest jt = new JnaPosixTest("jna posix test");
		jt.init();
		jt.runAll();

		// System.setProperty("jna.predictable_field_order", "true");
		// FileStat stat = Posix.posix.stat("license.txt");
		// println("stat: " + stat);
	}
	
	public void test_stat_print() {
		String nStat =  Posix.posix.stat("license.txt").customRepr();
		String sStat = ProcessUtils.executeIt(Posix.posix.stat("license.txt").systemCommand() + " license.txt").get("out");
		assertEquals(sStat, nStat);
		System.out.println(
			"stat ok: \nnstat: " + nStat + "\nsstat: " + sStat);
	}

	public void runAll() throws Exception {
		println(line);
		test_link();
		test_unlink();
		test_mkdir();
		test_rmdir();
		test_opendir();
		test_open();
		test_stat();
		test_lstat();
		test_rename();
		// test_new();
	}
}