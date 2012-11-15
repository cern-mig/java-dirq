package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

/**
 * Unit tests for {@link ch.cern.dirq.FileUtils}.
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class FileUtilsTest extends TestCase {
	public static final String dir = Posix.posix.getpid() + "test/";

	/**
	 * Create the test case
	 * 
	 * @param name name of the test case
	 */
	public FileUtilsTest(String name) {
		super(name);
	}

	/**
	 * Test read write String.
	 * @throws IOException 
	 */
	public void testReadWriteString() throws IOException {
		String content = "Hello World!";
		String filePath = dir + "hello_world";
		FileUtils.writeToFile(filePath, content);
		assertEquals(content, FileUtils.readToString(filePath));
		File file = new File(filePath);
		FileUtils.writeToFile(file, content);
		assertEquals(content, FileUtils.readToString(file));
	}
	
	/**
	 * Test read write ByteArray.
	 * @throws IOException 
	 */
	public void testReadWriteByteArray() throws IOException {
		String data = "Hello World!";
		byte[] dataAsBytes = data.getBytes();
		String filePath = dir + "hello_world_byte";
		FileUtils.writeToFile(filePath, dataAsBytes);
		assertEquals(data, new String(FileUtils.readToByteArray(filePath)));
		File file = new File(filePath);
		FileUtils.writeToFile(file, dataAsBytes);
		assertTrue(Arrays.equals(dataAsBytes, FileUtils.readToByteArray(file)));
	}
	
	/**
	 * Test read write mix.
	 * @throws IOException 
	 */
	public void testReadWriteMix() throws IOException {
		String data = "Hello World!";
		String filePath = dir + "hello_world_mix";
		FileUtils.writeToFile(filePath, data);
		assertEquals(data, new String(FileUtils.readToByteArray(filePath)));
		File file = new File(filePath);
		FileUtils.writeToFile(file, data);
		assertEquals(data, new String(FileUtils.readToByteArray(file)));
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