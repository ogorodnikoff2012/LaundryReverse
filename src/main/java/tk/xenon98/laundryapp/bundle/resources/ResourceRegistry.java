
package tk.xenon98.laundryapp.bundle.resources;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.bundle.resources.integrity.NeverValidIntegrityVerificator;
import tk.xenon98.laundryapp.bundle.resources.resolver.IResourceResolver;

@RequiredArgsConstructor
public class ResourceRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ResourceRegistry.class);
    private final Set<Resource> verifiedResources = new HashSet<>();
    private final File resourceDirectory;
    private final Map<String, IResourceResolver> resolvers = new HashMap<>();
    private final IResourceResolver defaultResolver;

    public File getResource(final Resource resource) throws IOException {
        LOG.info("Retrieving resource " + resource);
        final File file = getFile(resource.filename());

        if (file.exists()) {
            if (verifiedResources.contains(resource)) {
                LOG.info("Using cached file");
                return file;
            }

            if (resource.integrity() != NeverValidIntegrityVerificator.INSTANCE) {
                LOG.info("Checking integrity");
                if (resource.integrity().test(file)) {
                    verifiedResources.add(resource);
                    return file;
                } else {
                    LOG.info("Integrity check failed, re-downloading");
                }
            }
        }

        retrieveResource(resource, file);
        verifiedResources.add(resource);
        return file;
    }

    private void retrieveResource(final Resource resource, final File file) throws IOException {
        resolvers.getOrDefault(resource.uri().getScheme(), defaultResolver).resolve(this, resource, file);
    }

    public File getFile(final String filename) {
        return Path.of(resourceDirectory.getAbsolutePath(), filename).toFile();
    }

    public void registerResourceResolver(final String uriScheme, final IResourceResolver resourceResolver) {
        resolvers.put(uriScheme, resourceResolver);
    }
}
