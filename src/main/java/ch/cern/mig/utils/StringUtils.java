/**
 * General String useful stuff.
 */
package ch.cern.mig.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Useful String utility.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class StringUtils {

	public StringUtils () {
		
	}
	
	public static String join(Object[] arguments) {
		return join(Arrays.asList(arguments));
	}
	
	public static String join(Object[] arguments, String glue) {
		return join(Arrays.asList(arguments), glue);
	}
	
	public static String join(List<Object> arguments) {
		return join(arguments, ", ");
	}
	
	public static String join(List<Object> arguments, String glue) {
		if (arguments == null) {
			return "";
		}
		StringBuilder output = new StringBuilder();
		Iterator<Object> it = arguments.iterator();
		while (it.hasNext()) {
			output.append(it.next().toString());
			if (it.hasNext()) {
				output.append(glue);
			}
		}
		return output.toString();
	}
}
