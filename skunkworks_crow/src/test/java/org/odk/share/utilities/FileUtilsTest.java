package org.odk.share.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest {

    /**
     * {@link Test} for getting a file's extension.
     */
    @Test
    public void getFileExtensionTest() {
        assertEquals("txt", FileUtils.getFileExtension("xxxx.txt"));
        assertEquals("pdf", FileUtils.getFileExtension("xxxx.pdf"));
        assertEquals("doc", FileUtils.getFileExtension("xxxx.doc"));
        assertEquals("jpeg", FileUtils.getFileExtension("xxxx.jpeg"));
        assertEquals("png", FileUtils.getFileExtension("xxxx.png"));
        assertEquals("jpg", FileUtils.getFileExtension("xxxx.jpg"));
        assertEquals("mp4", FileUtils.getFileExtension("xxxx.mp4"));
        assertEquals("avi", FileUtils.getFileExtension("xxxx.avi"));
    }
}
