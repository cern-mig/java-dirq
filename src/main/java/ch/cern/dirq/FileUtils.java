package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;

/**
 * Convenient file related utilities.
 * <br>
 * This is a private class to be used only by the directory queue classes.
 * Its API can change at any time without any notice.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2016
 */
public final class FileUtils {

    // stat(2) constants
    public static final int S_IRWXU = 0700;
    public static final int S_IRUSR = 0400;
    public static final int S_IWUSR = 0200;
    public static final int S_IXUSR = 0100;
    public static final int S_IRWXG = 0070;
    public static final int S_IRGRP = 0040;
    public static final int S_IWGRP = 0020;
    public static final int S_IXGRP = 0010;
    public static final int S_IRWXO = 0007;
    public static final int S_IROTH = 0004;
    public static final int S_IWOTH = 0002;
    public static final int S_IXOTH = 0001;

    // this utility class cannot be instantiated
    private FileUtils() {
    }

    // helper for posixPermissionsFromInteger()
    private static boolean isSet(final int perm, final int bit) {
        return (perm & bit) == bit;
    }

    /**
     * Create NIO POSIX file permissions from numerical POSIX permissions.
     */
    public static Set<PosixFilePermission> posixPermissionsFromInteger(final int perm) {
        Set<PosixFilePermission> result = EnumSet.noneOf(PosixFilePermission.class);
        if (isSet(perm, S_IRUSR)) {
            result.add(PosixFilePermission.OWNER_READ);
        }
        if (isSet(perm, S_IWUSR)) {
            result.add(PosixFilePermission.OWNER_WRITE);
        }
        if (isSet(perm, S_IXUSR)) {
            result.add(PosixFilePermission.OWNER_EXECUTE);
        }
        if (isSet(perm, S_IRGRP)) {
            result.add(PosixFilePermission.GROUP_READ);
        }
        if (isSet(perm, S_IWGRP)) {
            result.add(PosixFilePermission.GROUP_WRITE);
        }
        if (isSet(perm, S_IXGRP)) {
            result.add(PosixFilePermission.GROUP_EXECUTE);
        }
        if (isSet(perm, S_IROTH)) {
            result.add(PosixFilePermission.OTHERS_READ);
        }
        if (isSet(perm, S_IWOTH)) {
            result.add(PosixFilePermission.OTHERS_WRITE);
        }
        if (isSet(perm, S_IXOTH)) {
            result.add(PosixFilePermission.OTHERS_EXECUTE);
        }
        return result;
    }

    /**
     * Create NIO file attributes from numerical POSIX permissions.
     */
    public static FileAttribute<?> fileAttributesFromInteger(final int perm) {
        return PosixFilePermissions.asFileAttribute(posixPermissionsFromInteger(perm));
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
