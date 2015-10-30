package ch.cern.dirq;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.cern.mig.utils.FileUtils;

/**
 * Unit tests for {@link ch.cern.mig.utils.FileUtils}.
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */

public class FileUtilsTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    public String tempPath() {
        return tempDir.getRoot().getPath();
    }

    /**
     * Test read/write String.
     *
     * @throws IOException
     */
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

    /**
     * Test read/write ByteArray.
     *
     * @throws IOException
     */
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

    /**
     * Test read/write mix.
     *
     * @throws IOException
     */
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
    }

    /**
     * Test reading a non-existing file.
     */
    @Test
    public void testReadMissing() {
        String path = tempPath() + File.separator + "testReadMissing";
        Assert.assertNull(FileUtils.readToString(path));
        Assert.assertNull(FileUtils.readToByteArray(path));
        File file = new File(path);
        Assert.assertNull(FileUtils.readToString(file));
        Assert.assertNull(FileUtils.readToByteArray(file));
    }

}
