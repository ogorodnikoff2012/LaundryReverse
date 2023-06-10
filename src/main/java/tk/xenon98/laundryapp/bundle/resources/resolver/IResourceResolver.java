
package tk.xenon98.laundryapp.bundle.resources.resolver;

import java.io.File;
import java.io.IOException;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;

public interface IResourceResolver {

    void resolve(final ResourceRegistry resourceRegistry, final Resource resource, final File targetFile)
            throws IOException;
}
