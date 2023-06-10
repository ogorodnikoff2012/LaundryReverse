
package tk.xenon98.laundryapp.bundle.resources.resolver;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;
import tk.xenon98.laundryapp.bundle.resources.util.FSUtils;
import tk.xenon98.laundryapp.bundle.resources.util.URIUtils;
import tk.xenon98.laundryapp.bundle.resources.util.ZipUtils;

public class ZipResourceResolver implements IResourceResolver {

    public static final String ZIP_SET_EXECUTABLE = "chmod_x";
    public static final String ZIP_SCHEME = "zip";
    private static final Logger LOG = LoggerFactory.getLogger(ZipResourceResolver.class);

    @Override
    public void resolve(final ResourceRegistry resourceRegistry, final Resource resource, final File targetDir)
            throws IOException {

        final File zipFile = resourceRegistry
                .getFile(URLDecoder.decode(resource.uri().getPath(), StandardCharsets.UTF_8));
        LOG.info("Unzipping " + zipFile + " to " + targetDir);
        ZipUtils.unzip(zipFile, targetDir);
        final Map<String, String> query = URIUtils.decodeQuery(resource.uri().getQuery());
        if (query.containsKey(ZIP_SET_EXECUTABLE)) {
            final Path targetDirPath = targetDir.toPath();
            final PathMatcher fileFilter = FSUtils.buildFileFilter(query.get(ZIP_SET_EXECUTABLE));
            try (final var pathStream = Files.walk(targetDirPath)) {
                final boolean chmodSucceeded = pathStream
                        .filter(path -> fileFilter.matches(targetDirPath.relativize(path))).allMatch(path -> {
                            LOG.info("Setting file " + path + " executable");
                            return path.toFile().setExecutable(true);
                        });
                if (!chmodSucceeded) {
                    throw new IOException("Cannot make all requested files executable");
                }
            }
        }

    }
}
