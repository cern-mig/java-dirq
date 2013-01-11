package ch.cern.mig.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

/**
 * 
 * @author Massimo Paladin - massimo.paladin@gmail.com
 * <br />Copyright CERN 2010-2013
 *
 */
public class FileUtils {
	
	public static void writeToFile(File path, byte[] data) throws IOException {
		FileOutputStream newFileStream = new FileOutputStream(path);
		BufferedOutputStream newFileOut = new BufferedOutputStream(newFileStream);
		newFileOut.write(data);
		newFileOut.close();
		newFileStream.close();
	}
	
	public static void writeToFile(String path, byte[] data) throws IOException {
		writeToFile(new File(path), data);
	}

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

	public static String readToString(String path) {
		return readToString(new File(path));
	}

	public static String readToString(File tmp) {
		String content = "";
		try {
			content = new Scanner(tmp, "UTF-8").useDelimiter("\\A").next();
		} catch (FileNotFoundException e) {
			return null;
		}
		return content;
	}
	
	public static byte[] readToByteArray(String path) {
		return readToByteArray(new File(path));
	}
	
	public static byte[] readToByteArray(File file) {
		byte content[] = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			content = new byte[(int) file.length()];
			fileInputStream.read(content);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException ioe) {
			return null;
		}
		return content;
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
