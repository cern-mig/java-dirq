package ch.cern.dirq;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.sun.jna.LastErrorException;

import ch.cern.mig.posix.BasePosix;
import ch.cern.mig.posix.FileStat;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;
import ch.cern.mig.utils.RegExpFilenameFilter;

/**
 * QueueSimple - object oriented interface to a simple directory based queue.
 * <p>
 * A port of Perl module Directory::Queue::Simple
 * <a href="http://search.cpan.org/~lcons/Directory-Queue/">
 * http://search.cpan.org/~lcons/Directory-Queue/</a>
 * <p>
 * The documentation from Directory::Queue::Simple module was adapted for Java.
 * <p>
 * <h3>Usage</h3>
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
 * <h3>Description</h3>
 * <p>
 * This module is very similar to normal dirq, but uses a different way to
 * store data in the filesystem, using less directories. Its API is almost
 * identical.
 * <p>
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
 * <p>
 * <h3>Directory Structure</h3>
 * <p>
 * The toplevel directory contains intermediate directories that contain
 * the stored elements, each of them in a file.<br />
 * The names of the intermediate directories are time based: the element
 * insertion time is used to create a 8-digits long hexadecimal number.<br />
 * The granularity (see the constructor) is used to limit the number of
 * new directories. For instance, with a granularity of 60 (the default),
 * new directories will be created at most once per minute.
 * <p>
 * Since there is usually a filesystem limit in the number of directories
 * a directory can hold, there is a trade-off to be made. If you want to
 * support many added elements per second, you should use a low
 * granularity to keep small directories. However, in this case, you will
 * create many directories and this will limit the total number of
 * elements you can store.
 * <p>
 * The elements themselves are stored in files (one per element) with a
 * 14-digits long hexadecimal name SSSSSSSSMMMMMR where:
 * <ul>
 * <li>SSSSSSSS represents the number of seconds since the Epoch
 * <li>MMMMM represents the microsecond part of the time since the Epoch
 * <li>R is a random digit used to reduce name collisions
 * </ul>
 * <p>
 * A temporary element (being added to the queue) will have a
 * <i>.tmp</i> suffix.
 * <p>
 * A locked element will have a hard link with the same name and the
 * <i>.lck</i> suffix.
 * <p>
 * Please refer to {@link ch.cern.dirq.Queue} for general information about
 * directory queues.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 * 
 */
public class QueueSimple extends Queue {
	private static final BasePosix posix = Posix.posix;
	private static boolean WARN = false;
	private static final String UPID = String.format("%01x",
			posix.getpid() % 16);
	public final static String TEMPORARY_SUFFIX = ".tmp";
	public final static String LOCKED_SUFFIX = ".lck";

	private static final int default_umask = posix.umask();
	private static final int default_granularity = 60;

	private int umask;
	private int granularity;

	/**
	 * Return the granularity value.
	 * @return granularity value
	 */
	public int getGranularity() {
		return granularity;
	}

	/**
	 * Set the granularity property.
	 * @param granularity value to be set as granularity
	 */
	public void setGranularity(int granularity) {
		this.granularity = granularity;
	}

	/**
	 * Constructor which takes only the path of the queue and set umask
	 * and granularity to default values.
	 * @param path the path of the directory queue
	 * @throws QueueException
	 */
	public QueueSimple(String path) throws QueueException {
		this(path, default_umask, default_granularity);
	}

	/**
	 * Constructor which takes the path of the directory queue,
	 * its granularity option and the umask the created folder.
	 * @param path the path of the directory queue
	 * @param umask umask the umask value to be set during folder creation
	 * @throws QueueException
	 */
	public QueueSimple(String path, int umask) throws QueueException {
		this(path, umask, default_granularity);
	}

	/**
	 * Constructor which takes the path of the directory queue,
	 * its granularity option and the umask the created folder.
	 * @param path the path of the directory queue
	 * @param umask the umask value to be set during folder creation
	 * @param granularity the granularity of the directory queue
	 * @throws QueueException
	 */
	public QueueSimple(String path, int umask, int granularity)
			throws QueueException {
		this.path = path;
		this.umask = umask;
		this.granularity = granularity;

		// check if directory exists
		File dir = new File(path);
		if (dir.exists() && (!dir.isDirectory()))
			throw new QueueException("not a directory: " + path);

		// check umask option
		if (umask >= 512)
			throw new QueueException("invalid umask: " + umask);

		// create top level directory
		String tmpPath = "";
		for (String subDir:dir.getPath().split("/+")) {
			tmpPath += subDir + "/";
			if (new File(tmpPath).exists()) {
				continue;
			}
			specialMkdir(tmpPath, umask);
		}

		// store the queue unique identifier
		if (System.getProperty("os.name").startsWith("Windows"))
			id = path;
		else {
			// set id to stat->st_dev + stat->st_ino
			FileStat stat = posix.stat(path);
			id = "" + stat.dev() + ":" + stat.ino();
		}
	}

	protected String _name() {
		return String.format("%013x%s", System.nanoTime() / 1000, UPID);
	}
	
	private static boolean specialMkdir(String path) throws QueueException {
		return specialMkdir(path, posix.umask());
	}

	private static boolean specialMkdir(String path, int umask) throws QueueException {
		try {
			posix.mkdir(path, 0777 - umask);
		} catch (LastErrorException e) {
			if (e.getErrorCode() == BasePosix.EEXIST && !new File(path).isFile())
				return false;
			else if (e.getErrorCode() == BasePosix.EISDIR)
				return false;
			throw new QueueException(String.format("cannot mkdir(%s): %s",
					path, e.getMessage()));
		}
		return true;
	}

	private boolean specialRmdir(String path) throws QueueException {
		try {
			posix.rmdir(path);
		} catch (LastErrorException e) {
			if (!(e.getErrorCode() == BasePosix.ENOENT))
				throw new QueueException(String.format("cannot rmdir(%s): %s",
						path, e.getMessage()));
			return false;
		}
		return true;
	}
	
	@Override
	public String add(byte[] data) throws QueueException {
		String dir = _addDir();
		File tmp = _addData(dir, data);
		return _addPath(tmp, dir);
	}

	@Override
	public String add(String data) throws QueueException {
		String dir = _addDir();
		File tmp = _addData(dir, data);
		return _addPath(tmp, dir);
	}

	private String _addPath(File tmp, String dir) throws QueueException {
		while (true) {
			String name = _name();
			File newFile = new File(path + File.separator + dir
					+ File.separator + name);
			try {
				posix.link(tmp.getPath(), newFile.getPath());
			} catch (LastErrorException e) {
				if (e.getErrorCode() != BasePosix.EEXIST) {
					throw new QueueException(String.format(
							"cannot link(%s, %s): %s", tmp, newFile,
							e.getMessage()));
				} else {
					continue;
				}
			}
			try {
				posix.unlink(tmp.getPath());
			} catch (LastErrorException e) {
				throw new QueueException(String.format("cannot unlink(%s): %s",
						tmp, e.getMessage()));
			}
			return dir + File.separator + name;
		}
	}

	private File _fileCreate(String path) throws QueueException {
		File file = null;
		try {
			file = posix.open(path);
		} catch (LastErrorException e) {
			// RACE: someone else may have created the file (EEXIST)
			// RACE: the containing directory may be mising (ENOENT)
			if (e.getErrorCode() != BasePosix.EEXIST
					&& e.getErrorCode() != BasePosix.ENOENT)
				throw new QueueException(String.format("cannot create %s: %s",
						path, e.getMessage()));
			return null;
		}
		return file;
	}
	
	private File _addData(String dir, byte[] data) throws QueueException {
		File newFile = _getNewFile(dir);
		try {
			FileUtils.writeToFile(newFile, data);
		} catch (IOException e) {
			throw new QueueException("cannot write to file: " + newFile);
		}
		return newFile;
	}

	private File _addData(String dir, String data) throws QueueException {
		File newFile = _getNewFile(dir);
		try {
			FileUtils.writeToFile(newFile, data);
		} catch (IOException e) {
			throw new QueueException("cannot write to file: " + newFile);
		}
		return newFile;
	}

	private File _getNewFile(String dir) throws QueueException {
		File newFile = null;
		while (true) {
			String name = _name();
			newFile = _fileCreate(path + File.separator + dir + File.separator
					+ name + TEMPORARY_SUFFIX);
			if (newFile != null)
				break;
			if (!new File(path + File.separator + dir).exists())
				specialMkdir(path + File.separator + dir);
		}
		return newFile;
	}

	@Override
	public String addPath(String path) throws QueueException {
		String dir = _addDir();
		specialMkdir(this.path + File.separator + dir, umask);
		return _addPath(new File(path), dir);
	}

	protected String _addDir() {
		return String.format("%08x", System.currentTimeMillis() % granularity);
	}

	@Override
	public String get(String name) {
		return FileUtils.readToString(path + File.separator + name + LOCKED_SUFFIX);
	}
	
	@Override
	public byte[] getAsByteArray(String name) {
		return FileUtils.readToByteArray(path + File.separator + name + LOCKED_SUFFIX);
	}

	@Override
	public String getPath(String name) {
		return path + File.separator + name + LOCKED_SUFFIX;
	}

	@Override
	public boolean lock(String name, boolean permissive) throws QueueException {
		File file = new File(path + File.separator + name);
		File lock = new File(path + File.separator + name + LOCKED_SUFFIX);
		try {
			posix.link(file.getPath(), lock.getPath());
		} catch (LastErrorException e) {
			if (permissive
					&& (e.getErrorCode() == BasePosix.EEXIST || e.getErrorCode() == BasePosix.ENOENT))
				return false;
			throw new QueueException(String.format("cannot link(%s, %s): %s",
					file, lock, e.getMessage()));
		}
		try {
			posix.utimes(file.getPath(), null);
		} catch (LastErrorException e) {
			if (permissive && e.getErrorCode() == BasePosix.ENOENT) {
				posix.unlink(lock.getPath());
				return false;
			}
			throw new QueueException(String.format(
					"cannot utime(%s, null): %s", file, e.getMessage()));
		}
		return true;
	}

	@Override
	public boolean unlock(String name, boolean permissive)
			throws QueueException {
		String lock = path + File.separator + name + LOCKED_SUFFIX;
		try {
			posix.unlink(lock);
		} catch (LastErrorException e) {
			if (permissive && e.getErrorCode() == BasePosix.ENOENT)
				return false;
			throw new QueueException(String.format("cannot unlink(%s): %s",
					lock, e.getMessage()));
		}
		return true;
	}

	@Override
	public void remove(String name) {
		posix.unlink(path + File.separator + name);
		posix.unlink(path + File.separator + name + LOCKED_SUFFIX);
	}

	/**
	 * Used to filter directories while listing files.
	 */
	public class DirFilter implements FileFilter {

		public boolean accept(File file) {
			return file.isDirectory();
		}
	}

	@Override
	public int count() {
		int count = 0;
		// get list of intermediate directories
		File[] elements = new File(path).listFiles(new DirFilter());
		// count elements in sub-directories
		for (File element : elements) {
			File[] inElements = element.listFiles();
			for (File inElement : inElements) {
				if (ElementRegexp.matcher(inElement.getName()).matches())
					count += 1;
			}
		}
		return count;
	}

	@Override
	public void purge(int maxTemp, int maxLock) throws QueueException {
		// get list of intermediate directories
		File[] elements = new File(path).listFiles(new DirFilter());
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
						throw new QueueException(String.format(
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
						throw new QueueException(String.format(
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
	 * @author Massimo Paladin - massimo.paladin@gmail.com
	 * <br />Copyright CERN 2010-2012
	 *
	 */
	public class QueueSimpleIterator extends QueueIterator {

		/**
		 * Constructor which creates an iterator over the given queue.
		 * @param queue queue to be iterated
		 */
		public QueueSimpleIterator(Queue queue) {
			super(queue);
		}

		@Override
		public boolean buildElements() {
			while (!dirs.isEmpty()) {
				String dir = dirs.remove(0);
				File[] content = new File(queue.path + File.separator + dir)
						.listFiles(new RegExpFilenameFilter(Queue.ElementRegexp));
				if (content == null || content.length == 0)
					continue;
				Arrays.sort(content);
				for (File element : content) {
					elts.add(dir + File.separator + element.getName());
				}
				return true;
			}
			return false;
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
