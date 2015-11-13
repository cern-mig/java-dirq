package ch.cern.dirq;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.regex.Pattern;

import com.sun.jna.LastErrorException;

import ch.cern.mig.posix.BasePosix;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

import static ch.cern.mig.posix.Posix.posix;

/**
 * QueueSimple - object oriented interface to a <i>simple</i> directory based queue.
 * <br>
 * A port of Perl module Directory::Queue::Simple
 * <a href="http://search.cpan.org/dist/Directory-Queue/">
 * http://search.cpan.org/dist/Directory-Queue/</a>
 * <br>
 * The documentation from Directory::Queue::Simple module has been adapted for Java.
 * <br>
 * <h3>Usage</h3>
 * <pre>
 * {@code
 * // sample producer
 * QueueSimple dirq = new QueueSimple("/tmp/test");
 * for (int i=0; i < 100; i++) {
 *     String name = dirq.add("element " + i);
 *     System.out.println("# added element " + i + " as " + name);
 * }
 *
 * // sample consumer
 * dirq = QueueSimple("/tmp/test");
 * for (String name: dirq) {
 *     if (! dirq.lock(name)) {
 *         continue;
 *     }
 *     System.out.println("# reading element " + name);
 *     String data = dirq.get(name);
 *     // one could use dirq.unlock(name) to only browse the queue...
 *     dirq.remove(name);
 * }
 * }
 * </pre>
 * <h3>Description</h3>
 * This module is very similar to the normal directory queue, but uses a
 * different way to store data in the filesystem, using less directories. Its
 * API is almost identical.
 * <br>
 * Compared to normal directory queue, this module:
 * <ul>
 * <li>is simpler
 * <li>is faster
 * <li>uses less space on disk
 * <li>can be given existing files to store
 * <li>does not support schemas
 * <li>can only store and retrieve byte strings
 * <li>is not compatible (at filesystem level) with the normal directory queue
 * </ul>
 * <h3>Directory Structure</h3>
 * The toplevel directory contains intermediate directories that contain the
 * stored elements, each of them in a file.
 * <br>
 * The names of the intermediate directories are time based: the element
 * insertion time is used to create a 8-digits long hexadecimal number.
 * The granularity (see the constructor) is used to limit the number of new
 * directories. For instance, with a granularity of 60 (the default), new
 * directories will be created at most once per minute.
 * <br>
 * Since there is usually a filesystem limit in the number of directories a
 * directory can hold, there is a trade-off to be made. If you want to support
 * many added elements per second, you should use a low granularity to keep
 * small directories. However, in this case, you will create many directories
 * and this will limit the total number of elements you can store.
 * <br>
 * The elements themselves are stored in files (one per element) with a
 * 14-digits long hexadecimal name <i>SSSSSSSSMMMMMR</i> where:
 * <ul>
 * <li><i>SSSSSSSS</i> represents the number of seconds since the Epoch
 * <li><i>MMMMM</i> represents the microsecond part of the time since the Epoch
 * <li><i>R</i> is a random hexadecimal digit used to reduce name collisions
 * </ul>
 * <br>
 * A temporary element (being added to the queue) will have a <code>.tmp</code>
 * suffix.
 * <br>
 * A locked element will have a hard link with the same name and the
 * <code>.lck</code> suffix.
 * <br>
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

public class QueueSimple implements Queue {

    public static final String TEMPORARY_SUFFIX = ".tmp";
    public static final String LOCKED_SUFFIX = ".lck";
    public static final Pattern DIRECTORY_REGEXP =
        Pattern.compile("^[0-9a-f]{8}$");
    public static final Pattern ELEMENT_REGEXP =
        Pattern.compile("^[0-9a-f]{14}$");

    private static final FileFilter INTERMEDIATE_DIRECTORY_FF =
        new IntermediateDirectoryFF();
    private static final FileFilter ELEMENT_FF =
        new ElementFF();
    private static final FileFilter DOT_ELEMENT_FF =
        new DotElementFF();

    private static boolean WARN = false;
    private static Random rand = new Random();

    private String id = null;
    private String queuePath = null;
    private int granularity = 60;
    private int umask = 0; /* set in constructor */
    private int defaultMaxLock = 600;
    private int defaultMaxTemp = 300;
    private int rndHex = 0; /* set in constructor */

    private void warn(final String string) {
        if (!WARN) {
            return;
        }
        System.out.println(string);
        System.out.flush();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Get the queue path.
     *
     * @return queue path
     */
    public String getQueuePath() {
        return queuePath;
    }

    /**
     * Get the granularity.
     *
     * @return granularity (in seconds)
     */
    public int getGranularity() {
        return granularity;
    }

    /**
     * Set the granularity.
     *
     * @param granularity to be set (in seconds)
     * @return the object itself
     */
    public QueueSimple setGranularity(final int granularity) {
        this.granularity = granularity;
        return this;
    }

    /**
     * Get the umask.
     *
     * @return numerical umask
     */
    public int getUmask() {
        return umask;
    }

    /**
     * Set the umask.
     *
     * @param umask to be set (numerical)
     * @return the object itself
     */
    public QueueSimple setUmask(final int umask) {
        this.umask = umask;
        return this;
    }

    /**
     * Get the default maxLock for purge().
     *
     * @return maximum lock time (in seconds)
     */
    public int getMaxLock() {
        return defaultMaxLock;
    }

    /**
     * Set the default maxLock for purge().
     *
     * @param maxLock maximum lock time (in seconds)
     * @return the object itself
     */
    public QueueSimple setMaxLock(final int maxLock) {
        this.defaultMaxLock = maxLock;
        return this;
    }

    /**
     * Get the default maxTemp for purge().
     *
     * @return maximum temporary time (in seconds)
     */
    public int getMaxTemp() {
        return defaultMaxTemp;
    }

    /**
     * Set the default maxTemp for purge().
     *
     * @param maxTemp maximum temporary time (in seconds)
     * @return the object itself
     */
    public QueueSimple setMaxTemp(final int maxTemp) {
        this.defaultMaxTemp = maxTemp;
        return this;
    }

    /**
     * Get the random hexadecimal digit.
     *
     * @return numerical hexadecimal digit
     */
    public int getRndHex() {
        return rndHex;
    }

    /**
     * Set the random hexadecimal digit.
     *
     * @param rndHex hexadecimal digit to be set (numerical)
     * @return the object itself
     */
    public QueueSimple setRndHex(final int rndHex) {
        this.rndHex = rndHex % 16;
        return this;
    }

    /**
     * Constructor creating a simple directory queue from the given path.
     *
     * @param queuePath path of the directory queue
     * @throws IOException if any file operation fails
     */
    public QueueSimple(final String queuePath) throws IOException {

        this.queuePath = queuePath;
        this.umask = posix.umask();
        this.rndHex = rand.nextInt(0x10);

        // check if directory exists
        File dir = new File(queuePath);
        if (dir.exists() && !dir.isDirectory()) {
            throw new IllegalArgumentException("not a directory: " + queuePath);
        }

        // check umask option
        if (umask >= 512) {
            throw new IllegalArgumentException("invalid umask: " + umask);
        }

        // create top level directory
        String tmpPath = "";
        for (String subDir: dir.getPath().split("/+")) {
            tmpPath += subDir + File.separator;
            if (new File(tmpPath).exists()) {
                continue;
            }
            specialMkdir(tmpPath, umask);
        }

        // store the queue unique identifier
        this.id = FileUtils.fileKey(queuePath);
    }

    private static String name(final int r) {
        return String.format("%013x%01x", System.nanoTime() / 1000, r);
    }

    private static boolean specialMkdir(final String path) throws IOException {
        return specialMkdir(path, posix.umask());
    }

    private static boolean specialMkdir(final String path, final int umask)
            throws IOException {
        try {
            posix.mkdir(path, 0777 - umask);
        } catch (LastErrorException e) {
            if (Posix.getErrorCode(e) == BasePosix.EEXIST
                && !new File(path).isFile()) {
                return false;
            } else if (Posix.getErrorCode(e) == BasePosix.EISDIR) {
                return false;
            }
            throw new IOException(String.format("cannot mkdir(%s): %s", path,
                    e.getMessage()));
        }
        return true;
    }

    private boolean specialRmdir(final String path) throws IOException {
        try {
            posix.rmdir(path);
        } catch (LastErrorException e) {
            if (!(Posix.getErrorCode(e) == BasePosix.ENOENT)) {
                throw new IOException(String.format("cannot rmdir(%s): %s",
                                                    path, e.getMessage()));
            }
            return false;
        }
        return true;
    }

    @Override
    public String add(final String data) throws IOException {
        String dir = addDir();
        File tmp = addData(dir, data);
        return addPathHelper(tmp, dir);
    }

    @Override
    public String add(final byte[] data) throws IOException {
        String dir = addDir();
        File tmp = addData(dir, data);
        return addPathHelper(tmp, dir);
    }

    private String addPathHelper(final File tmp, final String dir) throws IOException {
        String name = null;
        while (true) {
            name = name(rndHex);
            File newFile = new File(queuePath + File.separator + dir
                    + File.separator + name);
            try {
                posix.link(tmp.getPath(), newFile.getPath());
            } catch (LastErrorException e) {
                if (Posix.getErrorCode(e) != BasePosix.EEXIST) {
                    throw new IOException(String.format(
                            "cannot link(%s, %s): %s", tmp, newFile,
                            e.getMessage()));
                } else {
                    continue;
                }
            }
            try {
                posix.unlink(tmp.getPath());
            } catch (LastErrorException e) {
                throw new IOException(String.format("cannot unlink(%s): %s",
                        tmp, e.getMessage()));
            }
            break;
        }
        return dir + File.separator + name;
    }

    private File fileCreate(final String path) throws IOException {
        File file = null;
        try {
            file = posix.open(path);
        } catch (LastErrorException e) {
            // RACE: someone else may have created the file (EEXIST)
            // RACE: the containing directory may be mising (ENOENT)
            if (Posix.getErrorCode(e) != BasePosix.EEXIST
                && Posix.getErrorCode(e) != BasePosix.ENOENT) {
                throw new IOException(String.format("cannot create %s: %s",
                                                    path, e.getMessage()));
            }
            return null;
        }
        return file;
    }

    private File addData(final String dir, final byte[] data) throws IOException {
        File newFile = getNewFile(dir);
        try {
            FileUtils.writeToFile(newFile, data);
        } catch (IOException e) {
            throw new IOException("cannot write to file: " + newFile);
        }
        return newFile;
    }

    private File addData(final String dir, final String data) throws IOException {
        File newFile = getNewFile(dir);
        try {
            FileUtils.writeToFile(newFile, data);
        } catch (IOException e) {
            throw new IOException("cannot write to file: " + newFile);
        }
        return newFile;
    }

    private File getNewFile(final String dir) throws IOException {
        File newFile = null;
        while (true) {
            String name = name(rndHex);
            newFile = fileCreate(queuePath + File.separator + dir
                    + File.separator + name + TEMPORARY_SUFFIX);
            if (newFile != null) {
                break;
            }
            if (!new File(queuePath + File.separator + dir).exists()) {
                specialMkdir(queuePath + File.separator + dir);
            }
        }
        return newFile;
    }

    @Override
    public String addPath(final String path) throws IOException {
        String dir = addDir();
        specialMkdir(this.queuePath + File.separator + dir, umask);
        return addPathHelper(new File(path), dir);
    }

    protected String addDir() {
        long now = System.currentTimeMillis() / 1000;
        if (granularity > 0) {
            now -= now % granularity;
        }
        return String.format("%08x", now);
    }

    @Override
    public String get(final String name) {
        return FileUtils.readToString(queuePath + File.separator + name
                + LOCKED_SUFFIX);
    }

    @Override
    public byte[] getAsByteArray(final String name) {
        return FileUtils.readToByteArray(queuePath + File.separator + name
                + LOCKED_SUFFIX);
    }

    @Override
    public String getPath(final String name) {
        return queuePath + File.separator + name + LOCKED_SUFFIX;
    }

    @Override
    public boolean lock(final String name) throws IOException {
        return lock(name, true);
    }

    @Override
    public boolean lock(final String name, final boolean permissive) throws IOException {
        File file = new File(queuePath + File.separator + name);
        File lock = new File(queuePath + File.separator + name + LOCKED_SUFFIX);
        try {
            posix.link(file.getPath(), lock.getPath());
        } catch (LastErrorException e) {
            if (permissive
                && (Posix.getErrorCode(e) == BasePosix.EEXIST || Posix
                    .getErrorCode(e) == BasePosix.ENOENT)) {
                return false;
            }
            throw new IOException(String.format("cannot link(%s, %s): %s",
                                                file, lock, e.getMessage()));
        }
        if (file.setLastModified(System.currentTimeMillis())) {
            return true;
        }
        if (permissive && !file.exists()) {
            // RACE: the file probably has been removed by someone else
            posix.unlink(lock.getPath());
            return false;
        }
        throw new IOException(String.format("cannot touch(%s)", file));
    }

    @Override
    public boolean unlock(final String name) throws IOException {
        return unlock(name, false);
    }

    @Override
    public boolean unlock(final String name, final boolean permissive) throws IOException {
        String lock = queuePath + File.separator + name + LOCKED_SUFFIX;
        try {
            posix.unlink(lock);
        } catch (LastErrorException e) {
            if (permissive && Posix.getErrorCode(e) == BasePosix.ENOENT) {
                return false;
            }
            throw new IOException(String.format("cannot unlink(%s): %s", lock,
                                                e.getMessage()));
        }
        return true;
    }

    @Override
    public void remove(final String name) {
        posix.unlink(queuePath + File.separator + name);
        posix.unlink(queuePath + File.separator + name + LOCKED_SUFFIX);
    }

    @Override
    public int count() {
        // get the list of intermediate directories
        File[] idirs = new File(queuePath).listFiles(INTERMEDIATE_DIRECTORY_FF);
        if (idirs == null) {
            return 0;
        }
        // count the elements in each intermediate directory
        int count = 0;
        for (File idir: idirs) {
            File[] elts = idir.listFiles(ELEMENT_FF);
            if (elts != null) {
                count += elts.length;
            }
        }
        return count;
    }

    @Override
    public void purge() throws IOException {
        purge(defaultMaxLock, defaultMaxTemp);
    }

    @Override
    public void purge(final int maxLock) throws IOException {
        purge(maxLock, defaultMaxTemp);
    }

    @Override
    public void purge(final int maxLock, final int maxTemp) throws IOException {
        long now = System.currentTimeMillis();
        long oldtemp = 0;
        long oldlock = 0;
        // get the list of intermediate directories
        File[] idirs = new File(queuePath).listFiles(INTERMEDIATE_DIRECTORY_FF);
        if (idirs == null) {
            return;
        }
        if (maxLock > 0) {
            oldlock = now - maxLock * 1000L;
        }
        if (maxTemp > 0) {
            oldtemp = now - maxTemp * 1000L;
        }
        if (maxTemp > 0 || maxLock > 0) {
            for (File idir: idirs) {
                File[] elts = idir.listFiles(DOT_ELEMENT_FF);
                if (elts == null) {
                    continue;
                }
                for (File elt: elts) {
                    long mtime = elt.lastModified();
                    if (mtime == 0L) {
                        if (elt.exists()) {
                            throw new IOException(String.format("cannot stat(%s)", elt));
                        } else {
                            continue;
                        }
                    }
                    if (elt.getName().endsWith(TEMPORARY_SUFFIX) && mtime >= oldtemp) {
                        continue;
                    }
                    if (elt.getName().endsWith(LOCKED_SUFFIX) && mtime >= oldlock) {
                        continue;
                    }
                    warn("removing too old volatile file: " + elt);
                    try {
                        posix.unlink(elt.getPath());
                    } catch (LastErrorException e) {
                        if (Posix.getErrorCode(e) == BasePosix.ENOENT) {
                            continue;
                        }
                        throw new IOException(String.format(
                            "cannot unlink(%s): %s", elt, e.getMessage()));
                    }
                }
            }
        }
        // try to purge all but the last intermediate directory
        if (idirs.length > 1) {
            Arrays.sort(idirs);
            for (int i = 0; i < idirs.length - 1; i++) {
                File idir = idirs[i];
                if (idir.exists()) {
                    File[] elts = idir.listFiles();
                    if (elts != null && elts.length == 0) {
                        specialRmdir(idir.getPath());
                    }
                }
            }
        }
    }

    /**
     * FileFilter class to iterate over intermediate directories.
     */
    private static class IntermediateDirectoryFF implements FileFilter {
        public boolean accept(final File file) {
            if (!file.isDirectory()) {
                return false;
            }
            if (!DIRECTORY_REGEXP.matcher(file.getName()).matches()) {
                return false;
            }
            return true;
        }
    }

    /**
     * FileFilter class to iterate over (normal) elements.
     */
    private static class ElementFF implements FileFilter {
        public boolean accept(final File file) {
            if (file.isDirectory()) {
                return false;
            }
            if (!ELEMENT_REGEXP.matcher(file.getName()).matches()) {
                return false;
            }
            return true;
        }
    }

    /**
     * FileFilter class to iterate over temporary or locked elements.
     */
    private static class DotElementFF implements FileFilter {
        public boolean accept(final File file) {
            if (file.isDirectory()) {
                return false;
            }
            if (!file.getName().contains(".")) {
                return false;
            }
            return true;
        }
    }

    /**
     * Iterator for the simple directory queue.
     */
    @Override
    public Iterator<String> iterator() {
        return new QueueSimpleIterator(this);
    }

    /**
     * Iterator for the simple directory queue (private).
     */
    private static class QueueSimpleIterator implements Iterator<String> {

        private QueueSimple itQueue = null;
        private List<String> itDirs = new ArrayList<String>();
        private List<String> itElts = new ArrayList<String>();

        /**
         * Helper method to build the list of elements to iterate over.
         */
        private boolean buildElements() {
            boolean result = false;
            while (!result && !itDirs.isEmpty()) {
                String iname = itDirs.remove(0);
                File idir = new File(itQueue.getQueuePath() + File.separator + iname);
                File[] elts = idir.listFiles(ELEMENT_FF);
                if (elts == null || elts.length == 0) {
                    continue;
                } else {
                    result = true;
                }
                for (File elt: elts) {
                    itElts.add(iname + File.separator + elt.getName());
                }
                Collections.sort(itElts);
            }
            return result;
        }

        /**
         * Constructor for the simple directory queue iterator.
         *
         * @param queue queue to be iterated on
         */
        public QueueSimpleIterator(final QueueSimple queue) {
            itQueue = queue;
            File[] idirs = new File(itQueue.getQueuePath())
                .listFiles(INTERMEDIATE_DIRECTORY_FF);
            if (idirs != null) {
                for (File idir: idirs) {
                    itDirs.add(idir.getName());
                }
                Collections.sort(itDirs);
            }
        }

        /**
         * Returns true if the iteration has more elements.
         */
        @Override
        public boolean hasNext() {
            if (!itElts.isEmpty()) {
                return true;
            }
            if (buildElements()) {
                return true;
            }
            return false;
        }

        /**
         * Returns the next element in the iteration.
         */
        @Override
        public String next() {
            if (!itElts.isEmpty()) {
                return itElts.remove(0);
            }
            if (buildElements()) {
                return itElts.remove(0);
            }
            throw new NoSuchElementException();
        }

        /**
         * Removes from the underlying collection the last element returned by this iterator.
         */
        @Override
        public void remove() {
        }

    }

}
