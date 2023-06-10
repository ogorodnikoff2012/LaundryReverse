package tk.xenon98.laundryapp.bundle.resources.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SubprocessUtils {
    private SubprocessUtils() {
    }

    public static ProcessBuilder runAsSubprocess(Class<?> clazz, List<String> jvmArgs, List<String> args) {
        String javaHome = System.getProperty("java.home");
        String javaBin = Path.of(javaHome, "bin", "java").toAbsolutePath().toString();
        String classPath = System.getProperty("java.class.path");
        String className = clazz.getName();

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.addAll(jvmArgs);
        command.add("-cp");
        command.add(classPath);
        command.add(className);
        command.addAll(args);

        return new ProcessBuilder(command);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final ProcessBuilder builder = runAsSubprocess(Yes.class, List.of(), List.of());
        final Process process = builder.inheritIO().start();
        Thread.sleep(1000);
        process.destroyForcibly();
        process.waitFor();
    }

    public static class Yes {

        public static void main(String[] args) {
            final String line = args.length == 0 ? "y" : String.join(" ", args);
            while (true) {
                System.out.println(line);
            }
        }
    }
}