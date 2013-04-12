package ch.cern.dirq;

import static ch.cern.mig.posix.Posix.posix;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import com.sun.jna.LastErrorException;

import ch.cern.mig.posix.BasePosix;
import ch.cern.mig.posix.FileStat;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;
import ch.cern.mig.utils.RegExpFilenameFilter;

/**
 * QueueSimple - object oriented interface to a simple directory based queue.
 * <p/>
 * A port of Perl module Directory::Queue::Simple <a
 * href="http://search.cpan.org/dist/Directory-Queue/">
 * http://search.cpan.org/dist/Directory-Queue/</a>
 * <p/>
 * The documentation from Directory::Queue::Simple module was adapted for Java.
 * <p/>
 * <h3>Usage</h3>
 * <p/>
 * <pre>
 * {@code
 * // sample producer
 * QueueSimple dirq = new QueueSimple("/tmp/test");
 * for (int i=0; i < 100; i++) {
 * 	String name = dirq.add("element " + i);
 * 	System.out.println("# added element " + i + " as " + name);
 * }
 *
 * // sample consumer
 * dirq = QueueSimple('/tmp/test');
 * for (String name:dirq) {
 * 	if (! dirq.lock(name)) {
 * 		continue;
 * 	}
 * 	System.out.println("# reading element " + name);
 * 	String data = dirq.get(name);
 * 	// one could use dirq.unlock(name) to only browse the queue...
 * 	dirq.remove(name);
 * }
 * }
 * </pre>
 * <p/>
 * <h3>Description</h3>
 * <p/>
 * This module is very similar to normal dirq, but uses a different way to store
 * data in the filesystem, using less directories. Its API is almost identical.
 * <p/>
 * Compared to normal dirq, this module:
 * <ul>
 * <li>is simpler
 * <li>is faster
 * <li>uses less space on disk
 * <li>can be given existing files to store
 * <li>does not support schemas
 * <li>can only store and retrieve byte strings
 * <li>is not compatible (at filesystem level) with Queue
 * </ul>
 * <p/>
 * <h3>Directory Structure</h3>
 * <p/>
 * The toplevel directory contains intermediate directories that contain the
 * stored elements, each of them in a file.<br />
 * The names of the intermediate directories are time based: the element
 * insertion time is used to create a 8-digits long hexadecimal number.<br />
 * The granularity (see the constructor) is used to limit the number of new
 * directories. For instance, with a granularity of 60 (the default), new
 * directories will be created at most once per minute.
 * <p/>
 * Since there is usually a filesystem limit in the number of directories a
 * directory can hold, there is a trade-off to be made. If you want to support
 * many added elements per second, you should use a low granularity to keep
 * small directories. However, in this case, you will create many directories
 * and this will limit the total number of elements you can store.
 * <p/>
 * The elements themselves are stored in files (one per element) with a
 * 14-digits long hexadecimal name SSSSSSSSMMMMMR where:
 * <ul>
 * <li>SSSSSSSS represents the number of seconds since the Epoch
 * <li>MMMMM represents the microsecond part of the time since the Epoch
 * <li>R is a random digit used to reduce name collisions
 * </ul>
 * <p/>
 * A temporary element (being added to the queue) will have a <i>.tmp</i>
 * suffix.
 * <p/>
 * A locked element will have a hard link with the same name and the <i>.lck</i>
 * suffix.
 * <p/>
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 *
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 */
public class QueueSimple implements Queue {
    private static final String UPID = String.format("%01x",
            posix.getpid() % 16);
    public static final String TEMPORARY_SUFFIX = ".tmp";
    public static final String LOCKED_SUFFIX = ".lck";
    private static final int UMASK = posix.umask();
    private static final int GRANULARITY = 60;
    private static final int MAX_TMP = 300;
    private static final int MAX_LOCK = 600;
    public static final Pattern DIRECTORY_REGEXP = Pattern
            .compile("[0-9a-f]{8}");
    public static final Pattern ELEMENT_REGEXP = Pattern
            .compile("[0-9a-f]{14}");

    private static boolean WARN = false;

    private String id = null;
    private String queuePath = null;
    private int umask = UMASK;
    private int granularity = GRANULARITY;

    /**
     * Return the granularity value.
     *
     * @return granularity value
     */
    public int getGranularity() {
        return granularity;
    }

    /**
     * Set the granularity property.
     *
     * @param granularity value to be set as granularity
     */
    public void setGranularity(int granularity) {
        this.granularity = granularity;
    }

    /**
     * @return the queue id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @return the queue path
     */
    public String getQueuePath() {
        return queuePath;
    }

    /**
     * Constructor which takes only the path of the queue and set umask and
     * granularity to default values.
     *
     * @param queuePath the path of the directory queue
     * @throws IOException
     */
    public QueueSimple(String queuePath) throws IOException {
        this(queuePath, UMASK, GRANULARITY);
    }

    /**
     * Constructor which takes the path of the directory queue, its granularity
     * option and the umask the created folder.
     *
     * @param queuePath the path of the directory queue
     * @param umask     umask the umask value to be set during folder creation
     * @throws IOException
     */
    public QueueSimple(String queuePath, int umask) throws IOException {
        this(queuePath, umask, GRANULARITY);
    }

    /**
     * Constructor which takes the path of the directory queue, its granularity
     * option and the umask the created folder.
     *
     * @param queuePath   the path of the directory queue
     * @param umask       the umask value to be set during folder creation
     * @param granularity the granularity of the directory queue
     * @throws IOException
     */
    public QueueSimple(String queuePath, int umask, int granularity)
            throws IOException {
        this.queuePath = queuePath;
        this.umask = umask;
        this.granularity = granularity;

        // check if directory exists
        File dir = new File(queuePath);
        if (dir.exists() && (!dir.isDirectory()))
            throw new IllegalArgumentException("not a directory: " + queuePath);

        // check umask option
        if (umask >= 512)
            throw new IllegalArgumentException("invalid umask: " + umask);

        // create top level directory
        String tmpPath = "";
        for (String subDir : dir.getPath().split("/+")) {
            tmpPath += subDir + "/";
            if (new File(tmpPath).exists()) {
                continue;
            }
            specialMkdir(tmpPath, umask);
        }

        // store the queue unique identifier
        if (System.getProperty("os.name").startsWith("Windows")) {
            id = queuePath;
        } else {
            // set id to stat->st_dev + stat->st_ino
            FileStat stat = posix.stat(queuePath);
            id = "" + stat.dev() + ":" + stat.ino();
        }
    }

    private static String name() {
        return String.format("%013x%s", System.nanoTime() / 1000, UPID);
    }

    private static boolean specialMkdir(String path) throws IOException {
        return specialMkdir(path, posix.umask());
    }

    private static boolean specialMkdir(String path, int umask)
            throws IOException {
        try {
            posix.mkdir(path, 0777 - umask);
        } catch (LastErrorException e) {
            if (Posix.getErrorCode(e) == BasePosix.EEXIST
                    && !new File(path).isFile())
                return false;
            else if (Posix.getErrorCode(e) == BasePosix.EISDIR)
                return false;
            throw new IOException(String.format("cannot mkdir(%s): %s", path,
                    e.getMessage()));
        }
        return true;
    }

    private boolean specialRmdir(String path) throws IOException {
        try {
            posix.rmdir(path);
        } catch (LastErrorException e) {
            if (!(Posix.getErrorCode(e) == BasePosix.ENOENT))
                throw new IOException(String.format("cannot rmdir(%s): %s",
                        path, e.getMessage()));
            return false;
        }
        return true;
    }

    @Override
    public String add(byte[] data) throws IOException {
        String dir = addDir();
        File tmp = addData(dir, data);
        return addPathHelper(tmp, dir);
    }

    @Override
    public String add(String data) throws IOException {
        String dir = addDir();
        File tmp = addData(dir, data);
        return addPathHelper(tmp, dir);
    }

    private String addPathHelper(File tmp, String dir) throws IOException {
        String name = null;
        while (name == null) {
            name = name();
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
        }
        return dir + File.separator + name;
    }

    private File fileCreate(String path) throws IOException {
        File file = null;
        try {
            file = posix.open(path);
        } catch (LastErrorException e) {
            // RACE: someone else may have created the file (EEXIST)
            // RACE: the containing directory may be mising (ENOENT)
            if (Posix.getErrorCode(e) != BasePosix.EEXIST
                    && Posix.getErrorCode(e) != BasePosix.ENOENT)
                throw new IOException(String.format("cannot create %s: %s",
                        path, e.getMessage()));
            return null;
        }
        return file;
    }

    private File addData(String dir, byte[] data) throws IOException {
        File newFile = getNewFile(dir);
        try {
            FileUtils.writeToFile(newFile, data);
        } catch (IOException e) {
            throw new IOException("cannot write to file: " + newFile);
        }
        return newFile;
    }

    private File addData(String dir, String data) throws IOException {
        File newFile = getNewFile(dir);
        try {
            FileUtils.writeToFile(newFile, data);
        } catch (IOException e) {
            throw new IOException("cannot write to file: " + newFile);
        }
        return newFile;
    }

    private File getNewFile(String dir) throws IOException {
        File newFile = null;
        while (true) {
            String name = name();
            newFile = fileCreate(queuePath + File.separator + dir
                    + File.separator + name + TEMPORARY_SUFFIX);
            if (newFile != null)
                break;
            if (!new File(queuePath + File.separator + dir).exists())
                specialMkdir(queuePath + File.separator + dir);
        }
        return newFile;
    }

    @Override
    public String addPath(String path) throws IOException {
        String dir = addDir();
        specialMkdir(this.queuePath + File.separator + dir, umask);
        return addPathHelper(new File(path), dir);
    }

    protected String addDir() {
        long now = System.currentTimeMillis() / 1000;
        if (granularity > 0)
            now -= now % granularity;
        return String.format("%08x", now);
    }

    @Override
    public String get(String name) {
        return FileUtils.readToString(queuePath + File.separator + name
                + LOCKED_SUFFIX);
    }

    @Override
    public byte[] getAsByteArray(String name) {
        return FileUtils.readToByteArray(queuePath + File.separator + name
                + LOCKED_SUFFIX);
    }

    @Override
    public String getPath(String name) {
        return queuePath + File.separator + name + LOCKED_SUFFIX;
    }

    @Override
    public boolean lock(String name) throws IOException {
        return lock(name, true);
    }

    @Override
    public boolean lock(String name, boolean permissive) throws IOException {
        File file = new File(queuePath + File.separator + name);
        File lock = new File(queuePath + File.separator + name + LOCKED_SUFFIX);
        try {
            posix.link(file.getPath(), lock.getPath());
        } catch (LastErrorException e) {
            if (permissive
                    && (Posix.getErrorCode(e) == BasePosix.EEXIST || Posix
                    .getErrorCode(e) == BasePosix.ENOENT))
                return false;
            throw new IOException(String.format("cannot link(%s, %s): %s",
                    file, lock, e.getMessage()));
        }
        try {
            posix.utimes(file.getPath(), null);
        } catch (LastErrorException e) {
            if (permissive && Posix.getErrorCode(e) == BasePosix.ENOENT) {
                posix.unlink(lock.getPath());
                return false;
            }
            throw new IOException(String.format("cannot utime(%s, null): %s",
                    file, e.getMessage()));
        }
        return true;
    }

    @Override
    public boolean unlock(String name) throws IOException {
        return unlock(name, false);
    }

    @Override
    public boolean unlock(String name, boolean permissive) throws IOException {
        String lock = queuePath + File.separator + name + LOCKED_SUFFIX;
        try {
            posix.unlink(lock);
        } catch (LastErrorException e) {
            if (permissive && Posix.getErrorCode(e) == BasePosix.ENOENT)
                return false;
            throw new IOException(String.format("cannot unlink(%s): %s", lock,
                    e.getMessage()));
        }
        return true;
    }

    @Override
    public void remove(String name) {
        posix.unlink(queuePath + File.separator + name);
        posix.unlink(queuePath + File.separator + name + LOCKED_SUFFIX);
    }

    /**
     * Used to filter directories while listing files.
     */
    private class DirFilter implements FileFilter {

        public boolean accept(File file) {
            return file.isDirectory();
        }
    }

    @Override
    public int count() {
        int count = 0;
        // get list of intermediate directories
        File[] elements = new File(queuePath).listFiles(new DirFilter());
        // count elements in sub-directories
        for (File element : elements) {
            File[] inElements = element.listFiles();
            for (File inElement : inElements) {
                if (ELEMENT_REGEXP.matcher(inElement.getName()).matches())
                    count += 1;
            }
        }
        return count;
    }

    @Override
    public void purge() throws IOException {
        purge(MAX_TMP, MAX_LOCK);
    }

    @Override
    public void purge(Map<String, Integer> options) throws IOException {
        int maxLock = options.get("maxLock") == null ? MAX_LOCK : options
                .get("maxLock");
        int maxTemp = options.get("maxTemp") == null ? MAX_TMP : options
                .get("maxTemp");
        purge(maxTemp, maxLock);
    }

    @Override
    public void purge(int maxLock) throws IOException {
        purge(MAX_TMP, maxLock);
    }

    @Override
    public void purge(int maxTemp, int maxLock) throws IOException {
        // get list of intermediate directories
        File[] elements = new File(queuePath).listFiles(new DirFilter());
        long now = System.currentTimeMillis() / 1000;
        long oldtemp = now - maxTemp;
        long oldlock = now - maxLock;
        if (oldtemp > 0 || oldlock > 0) {
            for (File element : elements) {
                File[] inElements = element.listFiles(new RegExpFilenameFilter(
                        Pattern.compile("\\."), false));
                if (inElements == null)
                    continue;
                for (File inElement : inElements) {
                    FileStat stat = null;
                    try {
                        stat = posix.stat(inElement.getPath());
                    } catch (LastErrorException e) {
                        if (Posix.getErrorCode(e) == BasePosix.ENOENT)
                            continue;
                        throw new IOException(String.format(
                                "cannot stat(%s): %s", inElement,
                                e.getMessage()));
                    }
                    if (inElement.getName().endsWith(TEMPORARY_SUFFIX)
                            && stat.mtime() >= oldtemp)
                        continue;
                    if (inElement.getName().endsWith(LOCKED_SUFFIX)
                            && stat.mtime() >= oldlock)
                        continue;
                    warn("removing too old volatile file: " + inElement);
                    try {
                        posix.unlink(inElement.getPath());
                    } catch (LastErrorException e) {
                        if (Posix.getErrorCode(e) == BasePosix.ENOENT)
                            continue;
                        throw new IOException(String.format(
                                "cannot unlink(%s): %s", inElement,
                                e.getMessage()));
                    }
                }
            }
        }
        // try to purge all but the last intermediate directory
        if (elements.length > 1) {
            Arrays.sort(elements);
            for (int c = 0; c < elements.length - 1; c++) {
                if (elements[c].exists() && elements[c].listFiles().length == 0)
                    specialRmdir(elements[c].getPath());
            }
        }
    }

    /**
     * Iterator over QueueSimple implementation.
     *
     * @author Massimo Paladin - massimo.paladin@gmail.com <br />
     *         Copyright (C) CERN 2012-2013
     */
    private static class QueueSimpleIterator implements Iterator<String> {
        private QueueSimple queue = null;
        private List<String> dirs = new ArrayList<String>();
        private List<String> elts = new ArrayList<String>();

        /**
         * Constructor which creates an iterator over the given queue.
         *
         * @param queue queue to be iterated
         */
        public QueueSimpleIterator(QueueSimple queue) {
            this.queue = queue;
            File[] content = new File(queue.getQueuePath())
                    .listFiles(new RegExpFilenameFilter(DIRECTORY_REGEXP));
            for (File dir : content) {
                dirs.add(dir.getName());
            }
            Collections.sort(dirs);
        }

        /**
         * Return true if there are still elements to be iterated.
         */
        @Override
        public boolean hasNext() {
            if (!elts.isEmpty())
                return true;
            if (buildElements())
                return true;
            return false;
        }

        /**
         * Return the next element to be iterated.
         */
        @Override
        public String next() {
            if (!elts.isEmpty())
                return elts.remove(0);
            if (buildElements())
                return elts.remove(0);
            throw new NoSuchElementException();
        }

        /**
         * Make sure visited element is removed from the list of iterable items.
         */
        @Override
        public void remove() {
            // already removed
        }

        private boolean buildElements() {
            boolean result = false;
            while (!result && !dirs.isEmpty()) {
                String dir = dirs.remove(0);
                File[] content = new File(queue.queuePath + File.separator
                        + dir).listFiles(new RegExpFilenameFilter(
                        ELEMENT_REGEXP));
                if (content == null || content.length == 0)
                    continue;
                else
                    result = true;
                Arrays.sort(content);
                for (File element : content) {
                    elts.add(dir + File.separator + element.getName());
                }
            }
            return result;
        }
    }

    @Override
    public Iterator<String> iterator() {
        return new QueueSimpleIterator(this);
    }

    private void warn(String string) {
        if (!WARN)
            return;
        System.out.println(string);
        System.out.flush();
    }
}
