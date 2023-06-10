
package tk.xenon98.laundryapp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.console.data.sdk.PackageDescription;

public class SdkManagerDriver implements IDriver {

    public static final String URI_SCHEME = "sdkman";
    private static final Logger LOG = LoggerFactory.getLogger(SdkManagerDriver.class);
    private final File executable;

    public SdkManagerDriver(final File executable) {
        this.executable = executable.getAbsoluteFile();
    }

    public void installPackage(final String packageName, final File targetDir)
            throws IOException, InterruptedException {
        final ProcessBuilder builder = new ProcessBuilder(executable.getAbsolutePath(),
                "--sdk_root=" + targetDir.getPath(), "--install", packageName);
        builder.redirectOutput(Redirect.INHERIT);
        final var process = builder.start();
        try (final var writer = new PrintWriter(process.getOutputStream())) {
            for (int i = 0; i < 100; ++i) {
                writer.println("y");
            }
        }
        if (process.waitFor() != 0) {
            throw new IOException("Failed to install package " + packageName);
        }
    }

    public List<PackageDescription> listPackages() throws IOException {
        final var process = new ProcessBuilder(executable.getAbsolutePath(),
                "--sdk_root=" + executable.getParentFile().getParentFile().getParentFile(), "--list").start();
        try (final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines().dropWhile(line -> !line.startsWith("Available Packages:")).skip(2).map(line -> {
                final String[] parts = line.split(Pattern.quote("|"));
                if (parts.length != 3) {
                    return null;
                }
                final String packageName = parts[0].strip();
                final String version = parts[1].strip();
                final String description = parts[2].strip();
                return new PackageDescription(packageName, version, description);
            }).filter(Objects::nonNull).toList();

        }
    }

}
