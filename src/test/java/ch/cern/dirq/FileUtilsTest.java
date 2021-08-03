package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit tests for {@link ch.cern.dirq.FileUtils}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2021
 */
public class FileUtilsTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    public String tempPath() {
        return tempDir.getRoot().getPath();
    }

    //
    // Test FileUtils.fileKey()
    //
    @Test
    public void testFileKey() throws IOException {
        String path1 = tempPath() + File.separator + "testFileKey1";
        File file1 = new File(path1);
        FileUtils.writeToFile(path1, "");
        String path2 = tempPath() + File.separator + "testFileKey2";
        File file2 = new File(path2);
        FileUtils.writeToFile(file2, "");
        Assert.assertEquals(FileUtils.fileKey(path1), FileUtils.fileKey(file1));
        Assert.assertEquals(FileUtils.fileKey(path2), FileUtils.fileKey(file2));
        Assert.assertNotEquals(FileUtils.fileKey(path1), FileUtils.fileKey(file2));
        Assert.assertNotEquals(FileUtils.fileKey(path2), FileUtils.fileKey(file1));
    }

    //
    // Test read/write String
    //
    @Test
    public void testReadWriteString() throws IOException {
        String data = "Hell\u00f6 W\u00f8rld!\n";
        String path = tempPath() + File.separator + "testReadWriteString";
        FileUtils.writeToFile(path, data);
        Assert.assertEquals(data, FileUtils.readToString(path));
        File file = new File(path);
        FileUtils.writeToFile(file, data);
        Assert.assertEquals(data, FileUtils.readToString(file));
        Assert.assertEquals(15, file.length());
    }

    //
    // Test read/write ByteArray
    //
    @Test
    public void testReadWriteByteArray() throws IOException {
        byte[] data = { 0x20, 0x30, 0x40, 0x50, 0x00, 0x32, 0x3F };
        String path = tempPath() + File.separator + "testReadWriteByteArray";
        FileUtils.writeToFile(path, data);
        Assert.assertTrue(Arrays.equals(data, FileUtils.readToByteArray(path)));
        File file = new File(path);
        FileUtils.writeToFile(file, data);
        Assert.assertTrue(Arrays.equals(data, FileUtils.readToByteArray(file)));
        Assert.assertEquals(7, file.length());
    }

    //
    // Test read/write mix
    //
    @Test
    public void testReadWriteMix() throws IOException {
        String stringData = "Hello World!\n";
        byte[] bytesData = stringData.getBytes(StandardCharsets.UTF_8);
        String path = tempPath() + File.separator + "testReadWriteMix";
        FileUtils.writeToFile(path, stringData);
        Assert.assertTrue(Arrays.equals(bytesData, FileUtils.readToByteArray(path)));
        File file = new File(path);
        FileUtils.writeToFile(file, bytesData);
        Assert.assertEquals(stringData, FileUtils.readToString(file));
        Assert.assertEquals(13, file.length());
    }

    //
    // Test using a non-existing file
    //
    @Test(expected = NoSuchFileException.class)
    public void testReadMissing1() throws IOException {
        String path = tempPath() + File.separator + "testReadMissing";
        Assert.assertNotNull(FileUtils.readToString(path));
    }
    @Test(expected = NoSuchFileException.class)
    public void testReadMissing2() throws IOException {
        String path = tempPath() + File.separator + "testReadMissing";
        Assert.assertNotNull(FileUtils.readToByteArray(path));
    }
    @Test(expected = NoSuchFileException.class)
    public void testReadMissing3() throws IOException {
        String path = tempPath() + File.separator + "testReadMissing";
        File file = new File(path);
        Assert.assertNotNull(FileUtils.readToString(file));
    }
    @Test(expected = NoSuchFileException.class)
    public void testReadMissing4() throws IOException {
        String path = tempPath() + File.separator + "testReadMissing";
        File file = new File(path);
        Assert.assertNotNull(FileUtils.readToByteArray(file));
    }
    @Test(expected = NoSuchFileException.class)
    public void testFileKeyMissing() throws IOException {
        String path = tempPath() + File.separator + "testFileKeyMissing";
        Assert.assertNotNull(FileUtils.fileKey(path));
    }

}
