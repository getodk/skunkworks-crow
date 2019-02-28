package org.odk.skunkworks_crow.utilities;

import java.util.Locale;

public final class FileUtils {

    private FileUtils() {

    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
