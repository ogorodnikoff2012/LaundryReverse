
package tk.xenon98.laundryapp.bundle.resources.resolver;

import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import tk.xenon98.laundryapp.bundle.resources.DownloadManager;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;

@RequiredArgsConstructor
public class URLResourceResolver implements IResourceResolver {

    private final DownloadManager downloadManager;

    @Override
    public void resolve(final ResourceRegistry resourceRegistry, final Resource resource, final File targetFile)
            throws IOException {
        downloadManager.download(resource.uri().toURL(), targetFile, resource.integrity());
    }
}
