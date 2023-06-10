
package tk.xenon98.laundryapp.bundle.resources.integrity;

import java.io.File;

public final class NoOpVerificator implements IIntegrityVerificator {

    static final NoOpVerificator INSTANCE = new NoOpVerificator();

    private NoOpVerificator() {
    }

    @Override
    public boolean test(final File file) {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{}";
    }
}
