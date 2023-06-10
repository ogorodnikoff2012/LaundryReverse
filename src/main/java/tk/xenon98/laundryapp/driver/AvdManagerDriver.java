
package tk.xenon98.laundryapp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.common.utils.ListBuilder;

public class AvdManagerDriver implements IDriver {

    private static final Logger LOG = LoggerFactory.getLogger(AvdManagerDriver.class);
    private final File avdManagerExecutable;
    private final File avdPath;

    public AvdManagerDriver(final File avdManagerExecutable, final File avdPath) {
        this.avdManagerExecutable = avdManagerExecutable.getAbsoluteFile();
        this.avdPath = avdPath.getAbsoluteFile();
    }

    public void installAVD(final AvdDescription description) throws IOException, InterruptedException {
        LOG.info("Installing Android Virtual Device " + description);
        final var command = new ListBuilder<String>();
        command.add(avdManagerExecutable.getAbsolutePath(), "create", "avd", "-p", avdPath.getAbsolutePath(),
                "--force");
        command.add("--name", description.getName());
        if (description.getSystemImage() != null) {
            command.add("--package", description.getSystemImage());
        }
        if (description.getDeviceName() != null) {
            command.add("--device", description.getDeviceName());
        }
        if (description.getSkinName() != null) {
            command.add("--skin", description.getSkinName());
        }

        final var pb = new ProcessBuilder(command.toList());
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);
        final Process p = pb.start();
        if (p.waitFor() != 0) {
            throw new IOException("Failed to install AVD " + description);
        }
    }

    public Set<String> getInstalledAVDs() {
        try {
            final var pb = new ProcessBuilder(avdManagerExecutable.getAbsolutePath(), "list", "avd");
            pb.redirectOutput(Redirect.PIPE);
            final var process = pb.start();
            try (final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                return reader.lines()
                        .map(line -> {
                            final String[] parts = line.split(": ", 2);
                            if (parts.length != 2 || !parts[0].strip().equals("Name")) {
                                return null;
                            }
                            return parts[1].strip();
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toUnmodifiableSet());
            }
        } catch (Exception e) {
            return Collections.emptySet();
        }
    }

    @Builder
    @Data
    public static class AvdDescription {

        @NonNull
        private final String name;
        private final String systemImage;
        private final String deviceName;
        private final String skinName;
    }
}
