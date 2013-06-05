/**
 * Unit tests for {@link ch.cern.dirq.FileUtils}.
 *
 * @author Lionel Cons <lionel.cons@cern.ch>
 * @author Massimo Paladin <massimo.paladin@gmail.com>
 *
 * Copyright (C) CERN 2012-2013
 */

package ch.cern.dirq;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.Test;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

public class FileUtilsTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    public String tempPath() {
        return tempDir.getRoot().getPath();
    }

    /**
     * Test read write String.
     *
     * @throws IOException
     */
    @Test
    public void testReadWriteString() throws IOException {
        String content = "Hello World!";
        String filePath = tempPath() + File.separator + "hello_world";
        FileUtils.writeToFile(filePath, content);
        assertEquals(content, FileUtils.readToString(filePath));
        File file = new File(filePath);
        FileUtils.writeToFile(file, content);
        assertEquals(content, FileUtils.readToString(file));
    }

    /**
     * Test read write ByteArray.
     *
     * @throws IOException
     */
    @Test
    public void testReadWriteByteArray() throws IOException {
        String data = "Hello World!";
        byte[] dataAsBytes = data.getBytes();
        String filePath = tempPath() + File.separator + "hello_world_byte";
        FileUtils.writeToFile(filePath, dataAsBytes);
        assertEquals(data, new String(FileUtils.readToByteArray(filePath)));
        File file = new File(filePath);
        FileUtils.writeToFile(file, dataAsBytes);
        assertTrue(Arrays.equals(dataAsBytes, FileUtils.readToByteArray(file)));
    }

    /**
     * Test read write mix.
     *
     * @throws IOException
     */
    @Test
    public void testReadWriteMix() throws IOException {
        String data = "Hello World!";
        String filePath = tempPath() + File.separator + "hello_world_mix";
        FileUtils.writeToFile(filePath, data);
        assertEquals(data, new String(FileUtils.readToByteArray(filePath)));
        File file = new File(filePath);
        FileUtils.writeToFile(file, data);
        assertEquals(data, new String(FileUtils.readToByteArray(file)));
    }
}
