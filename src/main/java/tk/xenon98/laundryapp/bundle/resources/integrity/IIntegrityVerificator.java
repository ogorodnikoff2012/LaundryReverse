
package tk.xenon98.laundryapp.bundle.resources.integrity;

import jakarta.xml.bind.DatatypeConverter;
import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;
import tk.xenon98.laundryapp.bundle.resources.Resource;

public interface IIntegrityVerificator extends Predicate<File> {

    default void init(final Resource resource) {
    }

    static IIntegrityVerificator noOp() {
        return NoOpVerificator.INSTANCE;
    }

    static IIntegrityVerificator neverValid() {
        return NeverValidIntegrityVerificator.INSTANCE;
    }

    static IIntegrityVerificator fileExists() {
        return FileExistsIntegrityVerificator.INSTANCE;
    }

    static IIntegrityVerificator digest(final String algorithm, final String expectedDigest)
            throws NoSuchAlgorithmException {
        final MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
        final byte[] digestBytes = DatatypeConverter.parseHexBinary(expectedDigest);
        return new DigestVerificator(messageDigest, digestBytes);
    }

    static IIntegrityVerificator sha256(final String expectedDigest) {
        try {
            return digest("SHA-256", expectedDigest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
