package ch.cern.dirq.extra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.cern.dirq.Queue;
import ch.cern.dirq.QueueSimple;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.ProcessUtils;

import com.lexicalscope.jewel.cli.Unparsed;
import com.lexicalscope.jewel.cli.ArgumentValidationException;
import com.lexicalscope.jewel.cli.CliFactory;
import com.lexicalscope.jewel.cli.CommandLineInterface;
import com.lexicalscope.jewel.cli.Option;

/**
 * Test suite used to compare and stress test different implementations
 * of directory queue across multiple programming languages.
 * 
 * Used in parallel with analog implementation in Perl and Python
 * in order to validate the algorithm and their interoperability.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class TestDirq {
	private static final List<String> TESTS = Arrays.asList("all", "add", "count", "size", "get", "iterate", "purge", "remove", "simple");
	private static final int pid = Posix.posix.getpid();
	private List<String> tests = null;
	private TestDirQArgs options = null;
	
	@CommandLineInterface(application="test_dirq-java")
	private interface TestDirQArgs {
		
		@Option(shortName="c", longName="count", defaultValue="-1", description="set the elements count")
		int getCount();
		
		@Option(shortName="d", longName="debug", description="show debugging information")
		boolean isDebug();
		
		@Option(longName="header", description="set header for added elements")
		boolean isHeader();
		
		@Option(helpRequest = true, description = "display help", longName = "help")
		boolean getHelp();
		
		@Option(helpRequest = true, shortName="l", longName="list", description="tests: all add count get iterate purge remove simple")
		boolean getList();

		@Option(longName="granularity", defaultValue="-1", description="time granularity for intermediate directories (QueueSimple)")
		int getGranularity();
		
		@Option(longName="maxlock", defaultValue="-1", description="maximum time for a locked element. 0 - locked elements will not be unlocked")
		int getMaxlock();
		
		@Option(longName="maxtemp", defaultValue="-1", description="maxmum time for a temporary element. 0 - temporary elements will not be removed")
		int getMaxtemp();
		
		@Option(shortName="p", longName="path", defaultValue="", description="set the queue path")
		String getPath();
		
		@Option(shortName="r", longName="random", description="randomize the body size")
		boolean isRandom();
		
		@Option(longName="simple", description="test QueueSimple")
		boolean isSimple();
		
		@Option(shortName="s", longName="size", defaultValue="-1", description="set the body size for added elements")
		int getSize();
		
		@Option(longName="sleep", defaultValue="0", description="sleep this amount of seconds before starting")
		int getSleep();
		
		@Unparsed(name="test")
		String getTest();
	}
	
	private TestDirQArgs parseArguments(final String[] args) {
		TestDirQArgs parsed = null;
		try {
			parsed = CliFactory.parseArguments(TestDirQArgs.class, args);
			if (! parsed.isSimple()) {
				throw new ArgumentValidationException("DirQ normal not supported, only DirQ simple");
			}
			if (! TESTS.contains(parsed.getTest())) {
				throw new ArgumentValidationException("test name not valid");
			}
			if (parsed.getTest().equals("all")) {
				tests = new ArrayList<String>(TESTS);
				tests.remove("all");
			} else {
				tests = new ArrayList<String>();
				tests.add(parsed.getTest());
			}
		} catch(ArgumentValidationException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return parsed;
	}
	
	private Queue newDirq() throws Exception {
		Queue queue = null;
		if (options.isSimple())	{
			QueueSimple tmp = new QueueSimple(options.getPath());
			if (options.getGranularity() > -1) {
				tmp.setGranularity(options.getGranularity());
			}
			queue = tmp;
		} else {
			throw new Exception("only DirQ simple is supported");
		}
		return queue;
	}

	private void testSize() throws Exception {
		Map<String, String> res = ProcessUtils.executeIt("du -ks " + options.getPath());
		int exitValue = Integer.parseInt(res.get("exitValue"));
		if (exitValue > 0) {
			die("du failed: " + exitValue);
		}
		debug("du output: " + res.get("out"));
	}
	
	private void testCount() throws Exception {
		Queue queue = newDirq();
		int count = queue.count();
		debug("queue has " + count + " elements");
	}
	
	private void testPurge() throws Exception {
		debug("purging the queue...");
		Queue queue = newDirq();
		Map<String, Integer> opts = new HashMap<String, Integer>();
		if (options.getMaxlock() > -1) {
			opts.put("maxLock", options.getMaxlock());
		}
		if (options.getMaxtemp() > -1) {
			opts.put("maxTemp", options.getMaxtemp());
		}
		queue.purge(opts);
	}
	
	private void testGet() throws Exception {
		debug("getting all elements in the queue (one pass)...");
		Queue queue = newDirq();
		int done = 0;
		for (String element:queue) {
			if (! queue.lock(element)) {
				continue;
			}
			queue.get(element);
			queue.unlock(element);
			done++;
		}
		debug(String.format("%d elements browsed", done));
	}
	
	private void testIterate() throws Exception {
		debug("iterating all elements in the queue (one pass)...");
		Queue queue = newDirq();
		int done = 0;
		for (String element:queue) {
			if (! queue.lock(element)) {
				continue;
			}
			queue.unlock(element);
			done++;
		}
		debug(String.format("%d elements locked/unlocked", done));
	}
	
	private String newBody(int size, boolean random) {
		if (random) {
			// see Irwin-Hall in http://en.wikipedia.org/wiki/Normal_distribution
			double rnd = 0;
			for (int i = 0; i < 12; i++) {
				rnd += Math.random();
			}
			rnd -= 6;
			rnd *= size / 6;
			size += (int) rnd;
		}
		if (size < 1) {
			return "";
		}
		char[] charArray = new char[size];
		Arrays.fill(charArray, 'A');
		return new String(charArray);
	}
	
	/**
	 * Test add action on a directory queue.
	 * 
	 * @throws Exception
	 */
	private void testAdd() throws Exception {
		boolean random = options.isRandom();
		int size = options.getSize();
		int count = options.getCount();
		if (count > -1) {
			debug(String.format("adding %d elements to the queue", count));
		} else {
			debug("adding elements to the queue forever...");
		}
		Queue queue = newDirq();
		int done = 0;
		String element;
		while (count == -1 || done < count) {
			done++;
			if (size > -1) {
				element = newBody(size, random);
			} else {
				element = "Element " + done;
			}
			queue.add(element);
		}
		debug(String.format("%d elements added", done));
	}
	
	/**
	 * Test remove action on a directory queue.
	 * 
	 * @throws Exception
	 */
	private void testRemove() throws Exception {
		int count = options.getCount();
		if (count > -1) {
			debug(String.format("removing %d elements from the queue...", count));
		} else {
			debug("removing all elements from the queue (one pass)...");
		}
		Queue queue = newDirq();
		int done = 0;
		if (count > -1) {
			// loop to iterate until enough are removed
			while (done < count) {
				for (String element:queue) {
					if (! queue.lock(element)) {
						continue;
					}
					done++;
					queue.remove(element);
					if (done == count) {
						break;
					}
				}
			}
		} else {
			// one pass only
			for (String element:queue) {
				if (! queue.lock(element)) {
					continue;
				}
				queue.remove(element);
				done++;
			}
			debug(String.format("%d elements removed", done));
		}
	}
	
	private void testSimple() throws Exception {
		File path = new File(options.getPath());
		if (path.exists()) {
			die("directory exists: " + path);
		}
		if (options.getCount() == -1) {
			die("missing option: --count");
		}
		testAdd();
		testCount();
		testSize();
		testPurge();
		testGet();
		testRemove();
		testPurge();
		int num = path.listFiles().length;
		if (num != 1) {
			throw new Exception("unexpected subdirs number: " + num);
		}
		deleteRecursively(path);
	}
	
	private void runTest(String name) throws Exception {
		long t1 = System.currentTimeMillis();
		if (name.equals("add")) {
			testAdd();
		} else if (name.equals("count")) {
			testCount();
		} else if (name.equals("get")) {
			testGet();
		} else if (name.equals("iterate")) {
			testIterate();
		} else if (name.equals("purge")) {
			testPurge();
		} else if (name.equals("remove")) {
			testRemove();
		} else if (name.equals("simple")) {
			testSimple();
		} else if (name.equals("size")) {
			testSize();
		} else {
			throw new Exception("unexpected test name: " + name);
		}
		long t2 = System.currentTimeMillis();
		debug(String.format("done in %.4f seconds", (t2 - t1) / 1000.0));
	}
	
	/**
	 * Delete recursively given path.
	 * 
	 * @param path path to be removed
	 * @return return true if removal succeed
	 */
	public static boolean deleteRecursively(File path) {
		if (path.isDirectory()) {
			String[] children = path.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteRecursively(new File(path, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return path.delete();
	}
	
	/**
	 * Allow to run a set of tests from unit tests.
	 * 
	 * @throws Exception
	 */
	public void mainSimple() throws Exception {
		String[] args = {"--simple", "--count", "10", "--path", "/tmp/dirq-" + pid, "--debug", "simple"};
		options = parseArguments(args);
		File path = new File(options.getPath());
		deleteRecursively(path);
		try {
			testSimple();
		} catch (Exception e) {
			deleteRecursively(path);
			throw e;
		}
		deleteRecursively(path);
	}
    
	
	/**
	 * Execute tests with given command line.
	 * 
	 * @param args command line arguments
	 * @throws Exception
	 */
    public void doMain(String[] args) throws Exception {
    	options = parseArguments(args);
    	if (options.getPath().length() == 0) {
    		die("Option is mandatory: -p/--path");
    	}
    	if (options.getSleep() > 0) {
    		Thread.sleep(options.getSleep() * 1000);
    	}
    	for (String test:tests) {
    		runTest(test);
    	}
    }
    
    /**
     * Die with given message.
     * 
     * @param message message printed before dieing
     */
    private static void die(String message) {
    	System.err.println(message);
    	System.err.flush();
		System.exit(1);
    }
    
    /**
     * Debug the given message.
     * 
     * @param message message logged
     */
    private void debug (String message) {
    	if (! options.isDebug()) {
    		return;
    	}
    	System.out.println(String.format("# %s [%d]: %s", new Date().toString(), pid, message));
    	System.out.flush();
    }
	
	/**
	 * Main called from command line.
	 * 
	 * @param args given command line arguments
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws Exception {
		new TestDirq().doMain(args);
	}
}
