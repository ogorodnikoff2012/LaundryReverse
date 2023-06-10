package tk.xenon98.laundryapp.bundle.resources.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.compress.archivers.zip.ZipFile;

public class ZipUtils {

    private ZipUtils() {
    }

    public static void unzip(final File zipFile, final File targetDir) throws IOException {
        new Expander().expand(new ZipFile(zipFile), targetDir);
    }
}
