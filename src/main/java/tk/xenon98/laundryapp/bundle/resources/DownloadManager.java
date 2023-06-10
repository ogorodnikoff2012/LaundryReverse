
package tk.xenon98.laundryapp.bundle.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.bundle.resources.integrity.IIntegrityVerificator;

public class DownloadManager {

    private static final Logger LOG = LoggerFactory.getLogger(DownloadManager.class);

    private static final int BUFFER_SIZE = 8192;

    public File download(final URL url, final File outputFile, final IIntegrityVerificator integrity)
            throws IOException {
        final var conn = url.openConnection();
        try (final var input = conn.getInputStream()) {
            LOG.info("Downloading " + url);
            try (final var output = new FileOutputStream(outputFile)) {
                copy(input, output, conn.getContentLengthLong());
            }
            integrity.test(outputFile);
            return outputFile;
        }
    }

    private void copy(final InputStream input, final OutputStream output, final long totalSize)
            throws IOException {
        final String totalSizeStr = FileUtils.byteCountToDisplaySize(totalSize);
        LOG.info("Total size: " + totalSizeStr);

        final long loggingOffset = Math.min(totalSize / 100, 1 << 20);
        long totalRead = 0;
        long nextMilestone = loggingOffset;

        final byte[] bytes = new byte[BUFFER_SIZE];
        while (true) {
            final int bytesRead = input.readNBytes(bytes, 0, bytes.length);
            if (bytesRead == 0) {
                break;
            }
            output.write(bytes, 0, bytesRead);
            totalRead += bytesRead;
            if (totalRead > nextMilestone) {
                LOG.info("Downloaded %s/%s (%.2f%%)".formatted(FileUtils.byteCountToDisplaySize(totalRead),
                        totalSizeStr, (100.0 * totalRead / totalSize)));
                nextMilestone += loggingOffset;
            }
        }

        LOG.info("Download completed");
    }

}
