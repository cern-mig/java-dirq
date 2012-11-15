package ch.cern.dirq;

import java.util.ArrayList;

import junit.framework.TestCase;
import ch.cern.mig.utils.StringUtils;

/**
 * Unit tests for {@link ch.cern.dirq.StringUtils}.
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class StringUtilsTest extends TestCase {

	/**
	 * Create the test case
	 * 
	 * @param name name of the test case
	 */
	public StringUtilsTest(String name) {
		super(name);
	}

	/**
	 * Test join.
	 */
	public void testJoin() {
		assertEquals("", StringUtils.join(new ArrayList<Object>(), ","));
		assertEquals("", StringUtils.join(new String[]{}, ", "));
		assertEquals("hello, world",
			StringUtils.join(new String[]{"hello", "world"}, ", "));
		assertEquals("hello, magic, world",
			StringUtils.join(new String[]{"hello", "magic", "world"}, ", "));
	}

}