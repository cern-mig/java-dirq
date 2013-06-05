package ch.cern.dirq;

import java.io.File;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import ch.cern.mig.posix.Posix;
import ch.cern.mig.utils.FileUtils;

/**
 * {@link ch.cern.dirq.QueueBase} base tests.
 *
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 */
public abstract class QueueTestBase {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    public String tempPath() {
        return tempDir.getRoot().getPath();
    }
}
