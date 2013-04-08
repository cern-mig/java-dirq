package ch.cern.dirq;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.sun.jna.LastErrorException;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

/**
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 */
public class JnaPosixTest {
    private static final String dir = "posixtmp";
    private static final String f1 = dir + File.separator + "f1";
    private static final String f2 = dir + File.separator + "f2";
    private static final String f3 = dir + File.separator + "f3";
    private static final String x1 = dir + File.separator + "x1";
    private static final String x2 = dir + File.separator + "x2";
    private static final String d1 = dir + File.separator + "d1";
    private static final String d2 = dir + File.separator + "d2";
    private static final String d3 = dir + File.separator + "d3";

    private static final String line = "################################################";

    private static final boolean OK = true;
    private static final boolean FAIL = false;

    private static Exception mkdir(String name) {
        try {
            Posix.posix.mkdir(name);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private static Exception rmdir(String path) {
        try {
            Posix.posix.rmdir(path);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private static Exception opendir(String path) {
        try {
            Posix.posix.opendir(path);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private static Exception open(String path) {
        try {
            Posix.posix.open(path);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private static Exception rename(String from, String to) {
        try {
            Posix.posix.rename(from, to);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private static Exception link(String from, String to) {
        try {
            Posix.posix.link(from, to);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private Exception unlink(String path) {
        try {
            Posix.posix.unlink(path);
        } catch (Exception e) {
            return e;
        }
        return null;
    }

    private static Exception stat(String path) {
        try {
            Posix.posix.stat(path);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private static Exception lstat(String path) {
        try {
            Posix.posix.lstat(path);
        } catch (LastErrorException e) {
            return e;
        }
        return null;
    }

    private void report(boolean ok, Exception exc, String test) {
        if (ok) {
            if (exc != null)
                throw new AssertionError("Got error: " + exc);
            println(test + ": ok");
        } else {
            if (exc == null)
                throw new AssertionError("Error expected");
            println(test + ": " + exc.getMessage());
            // if (exc instanceof LastErrorException)
            // println("" + ((LastErrorException)exc).getErrorCode());
        }
    }

    private static boolean rmtree(String name) throws IOException {
        return FileUtils.deleteDir(new File(name));
    }

    private static boolean mkfile(String parent, String child)
            throws IOException {
        return new File(parent, child).createNewFile();
    }

    private static boolean mkfile(String name) throws IOException {
        return new File(name).createNewFile();
    }

    private static void println(String string) {
        System.out.println(string);
    }

    @Test
    public void testMkdir() {
        report(OK, mkdir(d3), "mkdir(d3)");
        report(FAIL, mkdir(d1), "mkdir(d1)");
        report(FAIL, mkdir(x1 + "/d"), "mkdir(x1/d)");
        rmdir(d3);
        println(line);
    }

    @Test
    public void testRmdir() {
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

    @Test
    public void testOpendir() {
        report(OK, opendir(d1), "opendir(d1)");
        report(FAIL, opendir(x1), "opendir(x1)");
        report(FAIL, opendir(x1 + "/d"), "opendir(x1/d)");
        println(line);
    }

    @Test
    public void testRename() throws IOException {
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

    @Test
    public void testOpen() {
        // tests
        report(OK, open(f3), "open(f3)");
        report(FAIL, open(f1), "open(f1)");
        report(FAIL, open(x1 + "/f"), "open(x1/f)");
        // cleanup
        Posix.posix.unlink(f3);
        println(line);
    }

    @Test
    public void testLink() {
        report(OK, link(f1, f3), "link(f1, f3)");
        report(FAIL, link(x1, x2), "link(x1, x2)");
        report(FAIL, link(f1, f2), "link(f1, f2)");
        report(FAIL, link(x1, f2), "link(x1, f2)");
        unlink(f3);
        println(line);
    }

    @Test
    public void testUnlink() throws IOException {
        report(OK, unlink(f1), "unlink(f1)");
        report(FAIL, unlink(x1), "unlink(x1)");
        mkfile(f1);
        println(line);
    }

    @Test
    public void testStat() {
        report(OK, stat(f1), "stat(f1)");
        report(OK, stat(d1), "stat(d1)");
        report(FAIL, stat(x1), "stat(x1)");
        report(FAIL, stat(d1 + "/f"), "stat(d1/f)");
        report(FAIL, stat(x1 + "/f"), "stat(x1/f)");
        println(line);
    }

    @Test
    public void testLstat() {
        report(OK, lstat(f1), "lstat(f1)");
        report(OK, lstat(d1), "lstat(d1)");
        report(FAIL, lstat(x1), "lstat(x1)");
        report(FAIL, lstat(d1 + "/f"), "lstat(d1/f)");
        report(FAIL, lstat(x1 + "/f"), "lstat(x1/f)");
        println(line);
    }

    @Before
    public void init() throws IOException {
        rmtree(dir);
        mkdir(dir);
        mkfile(dir, "f1");
        mkfile(dir, "f2");
        mkdir(d1);
        mkdir(d2);
    }

    // private void testStatPrint() {
    // String nStat = Posix.posix.stat("license.txt").customRepr();
    // String sStat =
    // ProcessUtils.executeIt(Posix.posix.stat("license.txt").systemCommand() +
    // " license.txt").get("out");
    // assertEquals(sStat, nStat);
    // System.out.println(
    // "stat ok: \nnstat: " + nStat + "\nsstat: " + sStat);
    // }

    public static void main(String[] args) throws Exception {
        println(line);
        println("Platform: " + System.getProperty("os.name") + " - "
                + System.getProperty("os.version") + " - "
                + System.getProperty("os.arch"));
        println("Java: " + System.getProperty("java.version") + " - "
                + System.getProperty("java.vendor"));
        println("current dir: " + new File(".").getAbsolutePath());
        println(line);

        JnaPosixTest jt = new JnaPosixTest();
        jt.init();
        jt.runAll();

        // System.setProperty("jna.predictable_field_order", "true");
        // FileStat stat = Posix.posix.stat("license.txt");
        // println("stat: " + stat);
    }

    public void runAll() throws Exception {
        println(line);
        testLink();
        testUnlink();
        testMkdir();
        testRmdir();
        testOpendir();
        testOpen();
        testStat();
        testLstat();
        testRename();
    }
}
