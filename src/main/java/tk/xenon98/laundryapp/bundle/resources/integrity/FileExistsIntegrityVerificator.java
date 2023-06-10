
package tk.xenon98.laundryapp.bundle.resources.integrity;

import java.io.File;

public class FileExistsIntegrityVerificator implements IIntegrityVerificator {

    public static final FileExistsIntegrityVerificator INSTANCE = new FileExistsIntegrityVerificator();

    private FileExistsIntegrityVerificator() {
    }

    @Override
    public boolean test(final File file) {
        return file.exists();
    }
}
