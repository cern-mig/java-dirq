package ch.cern.mig.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2012
 *
 */
public class FileUtils {

	public static void writeToFile(File path, String data) throws IOException {
		FileWriter newFileStream = new FileWriter(path);
		BufferedWriter newFileOut = new BufferedWriter(newFileStream);
		newFileOut.write(data);
		newFileOut.close();
		newFileStream.close();
	}

	public static void writeToFile(String path, String data) throws IOException {
		writeToFile(new File(path), data);
	}

	public static String fileRead(String path) {
		return fileRead(new File(path));
	}

	public static String fileRead(File tmp) {
		String content = "";
		try {
			content = new Scanner(tmp, "UTF-8").useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			return null;
		}
		return content;
	}
}
