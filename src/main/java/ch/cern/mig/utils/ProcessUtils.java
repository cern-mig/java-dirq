/**
 * 
 */
package ch.cern.mig.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Generic process utiitiess.
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 * 
 */
public class ProcessUtils {

	/**
	 * Execute the given system command and return a Map containing output
	 * results and exit value.
	 * 
	 * @param command
	 * @return Map containing output results and exit value
	 */
	public static Map<String, String> executeIt(String command) {
		StringBuilder output = new StringBuilder();
		Process process = null;
		try {
			process = Runtime.getRuntime().exec(command);
			Scanner sc = new Scanner(process.getInputStream());
			while (sc.hasNext()) {
				output.append(sc.nextLine());
			}
		} catch (IOException e) {
			output.append(e.getMessage());
		}
		Map<String, String> result = new HashMap<String, String>();
		result.put("exitValue", "" + process.exitValue());
		result.put("out", output.toString());
		return result;
	}

}
