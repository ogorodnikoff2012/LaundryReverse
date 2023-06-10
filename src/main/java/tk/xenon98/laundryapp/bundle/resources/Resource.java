
package tk.xenon98.laundryapp.bundle.resources;

import java.net.URI;
import lombok.NonNull;
import tk.xenon98.laundryapp.bundle.resources.integrity.IIntegrityVerificator;

public record Resource(@NonNull String filename, @NonNull URI uri, @NonNull IIntegrityVerificator integrity) {

    public Resource(@NonNull String filename, @NonNull URI uri, @NonNull IIntegrityVerificator integrity) {
        this.filename = filename;
        this.uri = uri;
        this.integrity = integrity;
        this.integrity.init(this);
    }
}
