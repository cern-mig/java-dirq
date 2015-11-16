package ch.cern.mig.utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Convenient file related utilities.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Return a unique string identifying the given file object.
     */
    public static String fileKey(final File file)
        throws IOException {
        return fileKey(file.toPath());
    }

    /**
     * Return a unique string identifying the given path string.
     */
    public static String fileKey(final String path)
        throws IOException {
        return fileKey(Paths.get(path));
    }

    /**
     * Return a unique string identifying the given path object.
     */
    public static String fileKey(final Path path)
        throws IOException {
        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        return attrs.fileKey().toString();
    }

    /**
     * Write a UTF-8 string to a file object.
     */
    public static void writeToFile(final File file, final String data)
        throws IOException {
        writeToFile(file.toPath(), data);
    }

    /**
     * Write a UTF-8 string to a path string.
     */
    public static void writeToFile(final String path, final String data)
        throws IOException {
        writeToFile(Paths.get(path), data);
    }

    /**
     * Write a UTF-8 string to a path object.
     */
    public static void writeToFile(final Path path, final String data)
        throws IOException {
        writeToFile(path, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Write bytes to a file object.
     */
    public static void writeToFile(final File file, final byte[] data)
        throws IOException {
        writeToFile(file.toPath(), data);
    }

    /**
     * Write bytes to a path string.
     */
    public static void writeToFile(final String path, final byte[] data)
        throws IOException {
        writeToFile(Paths.get(path), data);
    }

    /**
     * Write bytes to a path object.
     */
    public static void writeToFile(final Path path, final byte[] data)
        throws IOException {
        Files.write(path, data);
    }

    /**
     * Read a UTF-8 string from a file object.
     */
    public static String readToString(final File file)
        throws IOException {
        return readToString(file.toPath());
    }

    /**
     * Read a UTF-8 string from a path string.
     */
    public static String readToString(final String path)
        throws IOException {
        return readToString(Paths.get(path));
    }

    /**
     * Read a UTF-8 string from a path object.
     */
    public static String readToString(final Path path)
        throws IOException {
        byte[] bytes = readToByteArray(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Read all the bytes from a file object.
     */
    public static byte[] readToByteArray(final File file)
        throws IOException {
        return readToByteArray(file.toPath());
    }

    /**
     * Read all the bytes from a path string.
     */
    public static byte[] readToByteArray(final String path)
        throws IOException {
        return readToByteArray(Paths.get(path));
    }

    /**
     * Read all the bytes from a path object.
     */
    public static byte[] readToByteArray(final Path path)
        throws IOException {
        return Files.readAllBytes(path);
    }

    /**
     * Recursively delete the given path, stopping on the first error.
     */
    public static boolean recursiveDelete(final File path) {
        if (path.isDirectory()) {
            String[] children = path.list();
            if (children == null) {
                return false;
            }
            for (int i = 0; i < children.length; i++) {
                if (!recursiveDelete(new File(path, children[i]))) {
                    return false;
                }
            }
        }
        return path.delete();
    }

}
