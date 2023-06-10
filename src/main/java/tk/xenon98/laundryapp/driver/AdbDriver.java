
package tk.xenon98.laundryapp.driver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import tk.xenon98.laundryapp.bundle.resources.util.FSUtils;
import tk.xenon98.laundryapp.common.utils.Utils;

public class AdbDriver implements IDriver {

    private final File adbExecutable;
    private final String deviceName;

    public AdbDriver() {
        this(FSUtils.findExecutableOnPath("adb"), "emulator-5554");
    }

    public AdbDriver(final File adbExecutable, final String deviceName) {
        this.adbExecutable = adbExecutable.getAbsoluteFile();
        this.deviceName = deviceName;
    }

    public AdbState getState() throws IOException, InterruptedException {
        final ProcessBuilder pb = new ProcessBuilder(adbExecutable.getAbsolutePath(), "-s", deviceName,
                "get-state");
        pb.redirectOutput(Redirect.PIPE);
        final var process = pb.start();
        if (process.waitFor() != 0) {
            return AdbState.OFFLINE;
        }
        try (final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return AdbState.valueOf(reader.readLine().toUpperCase());
        }
    }

    public Set<String> availableDevices() throws IOException {
        final var pb = new ProcessBuilder(adbExecutable.getAbsolutePath(), "devices");
        pb.redirectOutput(Redirect.PIPE);
        final var process = pb.start();
        try (final var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            return reader.lines()
                    .skip(1)
                    .map(line -> line.split(" ")[0])
                    .collect(Collectors.toUnmodifiableSet());
        }
    }

    public CompletableFuture<String> runShellCommand(final String cmd) throws IOException, InterruptedException {
        final Process process = createShellCommand(cmd);
        return process.onExit().thenApply(__ -> {
            try {
                return new String(process.getInputStream().readAllBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<InputStream> runShellCommandPiped(final String cmd) throws IOException {
        return CompletableFuture.completedFuture(createShellCommand(cmd).getInputStream());
    }

    private Process createShellCommand(final String cmd) throws IOException {
        final ProcessBuilder pb = new ProcessBuilder(adbExecutable.getAbsolutePath(), "-s", deviceName, "shell",
                cmd);
        return pb.start();
    }

    public void installAPK(final String filename) throws IOException, InterruptedException {
        final ProcessBuilder pb = new ProcessBuilder(adbExecutable.getAbsolutePath(), "-s", deviceName, "install",
                filename);
        final Process process = pb.start();
        process.waitFor();
    }

    public enum AdbState {
        OFFLINE,
        BOOTLOADER,
        DEVICE;
    }

    public void emuKill() throws IOException, InterruptedException, TimeoutException {
        final ProcessBuilder pb = new ProcessBuilder(adbExecutable.getAbsolutePath(), "-s", deviceName, "emu",
                "kill");
        final Process process = pb.start();
        process.waitFor();
        Utils.waitUntil(() -> {
            try {
                return !availableDevices().contains(deviceName) || getState() == AdbState.OFFLINE;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                // Do nothing
            }
            return false;
        }, "Emulator graceful shutdown", Duration.ofSeconds(30), Duration.ofSeconds(1));
    }
}
