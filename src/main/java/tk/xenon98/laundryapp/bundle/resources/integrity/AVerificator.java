
package tk.xenon98.laundryapp.bundle.resources.integrity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public abstract class AVerificator implements IIntegrityVerificator {

    @Override
    public boolean test(final File file) {
        try (final var input = new FileInputStream(file)) {
            final byte[] data = input.readAllBytes();
            return test(data);
        } catch (final IOException e) {
            return false;
        }
    }

    protected abstract boolean test(final byte[] fileContents);
}
