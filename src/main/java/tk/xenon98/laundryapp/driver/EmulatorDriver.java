
package tk.xenon98.laundryapp.driver;

import java.io.File;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.common.utils.ListBuilder;

public class EmulatorDriver implements IDriver {

	private static final Logger LOG = LoggerFactory.getLogger(EmulatorDriver.class);
	private final File emulatorExecutable;

	public EmulatorDriver(final File emulatorExecutable) {
		this.emulatorExecutable = emulatorExecutable.getAbsoluteFile();
	}

	public ProcessBuilder runEmulatorInstance(final String avdName, final int port, boolean noWindow) {
		final var cmd = new ListBuilder<String>();
		cmd.add(emulatorExecutable.getAbsolutePath());
		cmd.add("-avd").add(avdName);
		cmd.add("-port").add(String.valueOf(port));
		if (noWindow) {
			cmd.add("-no-window");
		}
		return new ProcessBuilder(cmd.toList());
	}

}
