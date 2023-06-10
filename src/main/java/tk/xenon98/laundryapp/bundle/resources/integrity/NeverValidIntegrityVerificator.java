
package tk.xenon98.laundryapp.bundle.resources.integrity;

import java.io.File;

public class NeverValidIntegrityVerificator implements IIntegrityVerificator {

    public static final NeverValidIntegrityVerificator INSTANCE = new NeverValidIntegrityVerificator();

    private NeverValidIntegrityVerificator() {
    }

    @Override
    public boolean test(final File file) {
        return false;
    }
}
