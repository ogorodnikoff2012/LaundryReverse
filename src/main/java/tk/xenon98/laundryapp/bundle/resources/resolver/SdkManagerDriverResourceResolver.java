
package tk.xenon98.laundryapp.bundle.resources.resolver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;
import tk.xenon98.laundryapp.driver.SdkManagerDriver;

@RequiredArgsConstructor
public class SdkManagerDriverResourceResolver implements IResourceResolver {

    private final SdkManagerDriver driver;

    @Override
    public void resolve(final ResourceRegistry resourceRegistry, final Resource resource, final File targetDir)
            throws IOException {
        final String packageName = extractPackageName(resource.uri());
        try {
            driver.installPackage(packageName, targetDir);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Unable to install package " + packageName, e);
        }
    }

    private String extractPackageName(final URI uri) {
        return String.join(";", Arrays.stream(uri.getPath().split("/")).filter(part -> part.length() > 0)
                .toList());
    }
}
