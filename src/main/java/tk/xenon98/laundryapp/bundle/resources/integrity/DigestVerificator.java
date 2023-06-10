
package tk.xenon98.laundryapp.bundle.resources.integrity;

import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Arrays;

final class DigestVerificator extends AVerificator {

    private final MessageDigest algorithm;
    private final byte[] expectedDigest;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "algorithm=" + algorithm.getAlgorithm() + ", expectedDigest="
                + DatatypeConverter.printHexBinary(expectedDigest).toLowerCase() + '}';
    }

    DigestVerificator(final MessageDigest algorithm, final byte[] expectedDigest) {
        this.algorithm = algorithm;
        this.expectedDigest = expectedDigest;
    }

    @Override
    protected boolean test(final byte[] data) {
        final byte[] digest = algorithm.digest(data);
        return Arrays.equals(expectedDigest, digest);
    }
}
