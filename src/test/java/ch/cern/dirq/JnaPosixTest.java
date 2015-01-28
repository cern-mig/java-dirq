package ch.cern.dirq;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import com.sun.jna.LastErrorException;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

/**
 * Unit tests for {@link com.sun.jna} as used in java-dirq.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

public class JnaPosixTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private static final String line = "################################################";
    private static final boolean OK = true;
    private static final boolean FAIL = false;

    private String tmpath(String name) {
        return tempDir.getRoot().getPath() + File.separator + name;
    }

    private String tmpath(String parent, String name) {
        return tempDir.getRoot().getPath() + File.separator + parent + File.separator + name;
    }

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

    private static Exception unlink(String path) {
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

    private static void report(boolean ok, Exception exc, String test) {
        if (ok) {
            if (exc != null)
                throw new AssertionError("Got error: " + exc.getMessage());
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

    private static boolean mkfile(String name) throws IOException {
        return new File(name).createNewFile();
    }

    private static void println(String string) {
        System.out.println(string);
    }

    @Test
    public void testInfo() {
        println("Platform: "
                + System.getProperty("os.name") + " - "
                + System.getProperty("os.version") + " - "
                + System.getProperty("os.arch"));
        println("Java: "
                + System.getProperty("java.version") + " - "
                + System.getProperty("java.vendor"));
        println("current directory: "
                + new File(".").getAbsolutePath());
        println("temporary directory: "
                + tempDir.getRoot().getPath());
        println(line);
    }

    @Test
    public void testMkdir() {
        report(OK,   mkdir(tmpath("d3")),      "mkdir(d3)");
        report(FAIL, mkdir(tmpath("d1")),      "mkdir(d1)");
        report(FAIL, mkdir(tmpath("x1", "d")), "mkdir(x1/d)");
        // cleanup
        rmdir(tmpath("d3"));
        println(line);
    }

    @Test
    public void testRmdir() {
        report(OK,   rmdir(tmpath("d1")),      "rmdir(d1)");
        report(FAIL, rmdir(tmpath("x1")),      "rmdir(x1)");
        report(FAIL, rmdir(tmpath("x1", "d")), "rmdir(x1/d)");
        Posix.posix.mkdir(tmpath("d2", "d3"));
        report(FAIL, rmdir(tmpath("d2")),      "rmdir(d2)");
        // cleanup
        Posix.posix.mkdir(tmpath("d1"));
        Posix.posix.rmdir(tmpath("d2", "d3"));
        println(line);
    }

    @Test
    public void testOpendir() {
        report(OK,   opendir(tmpath("d1")),      "opendir(d1)");
        report(FAIL, opendir(tmpath("x1")),      "opendir(x1)");
        report(FAIL, opendir(tmpath("x1", "d")), "opendir(x1/d)");
        println(line);
    }

    @Test
    public void testRename() throws IOException {
        // additional setup
        mkfile(tmpath("d1", "f"));
        mkfile(tmpath("d2", "f"));
        // tests
        report(OK,   rename(tmpath("d1"), tmpath("d3")), "rename(d1, d3)");
        report(FAIL, rename(tmpath("x1"), tmpath("x2")), "rename(x1, x2)");
        report(FAIL, rename(tmpath("d2"), tmpath("d3")), "rename(d2, d3)");
        report(FAIL, rename(tmpath("x1"), tmpath("d2")), "rename(x1, d2)");
        // cleanup
        Posix.posix.rename(tmpath("d3"), tmpath("d1"));
        Posix.posix.unlink(tmpath("d1", "f"));
        Posix.posix.unlink(tmpath("d2", "f"));
        println(line);
    }

    @Test
    public void testOpen() {
        report(OK,   open(tmpath("f3")),      "open(f3)");
        report(FAIL, open(tmpath("f1")),      "open(f1)");
        report(FAIL, open(tmpath("x1", "f")), "open(x1/f)");
        // cleanup
        Posix.posix.unlink(tmpath("f3"));
        println(line);
    }

    @Test
    public void testLink() {
        report(OK,   link(tmpath("f1"), tmpath("f3")), "link(f1, f3)");
        report(FAIL, link(tmpath("x1"), tmpath("x2")), "link(x1, x2)");
        report(FAIL, link(tmpath("f1"), tmpath("f2")), "link(f1, f2)");
        report(FAIL, link(tmpath("x1"), tmpath("f2")), "link(x1, f2)");
        // cleanup
        unlink(tmpath("f3"));
        println(line);
    }

    @Test
    public void testUnlink() throws IOException {
        report(OK,   unlink(tmpath("f1")), "unlink(f1)");
        report(FAIL, unlink(tmpath("x1")), "unlink(x1)");
        // cleanup
        mkfile(tmpath("f1"));
        println(line);
    }

    @Test
    public void testStat() {
        report(OK,   stat(tmpath("f1")),      "stat(f1)");
        report(OK,   stat(tmpath("d1")),      "stat(d1)");
        report(FAIL, stat(tmpath("x1")),      "stat(x1)");
        report(FAIL, stat(tmpath("d1", "f")), "stat(d1/f)");
        report(FAIL, stat(tmpath("x1", "f")), "stat(x1/f)");
        println(line);
    }

    @Test
    public void testLstat() {
        report(OK,   lstat(tmpath("f1")),      "lstat(f1)");
        report(OK,   lstat(tmpath("d1")),      "lstat(d1)");
        report(FAIL, lstat(tmpath("x1")),      "lstat(x1)");
        report(FAIL, lstat(tmpath("d1", "f")), "lstat(d1/f)");
        report(FAIL, lstat(tmpath("x1", "f")), "lstat(x1/f)");
        println(line);
    }

    @Before
    public void init() throws IOException {
        mkfile(tmpath("f1"));
        mkfile(tmpath("f2"));
        mkdir(tmpath("d1"));
        mkdir(tmpath("d2"));
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
        testInfo();
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
