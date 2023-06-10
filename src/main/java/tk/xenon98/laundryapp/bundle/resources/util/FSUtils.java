package tk.xenon98.laundryapp.bundle.resources.util;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;

public class FSUtils {

    private FSUtils() {
    }

    public static final String FILELIST_SEPARATOR = ":";

    public static PathMatcher buildFileFilter(final String specification) {
        final String[] patterns = specification.split(FILELIST_SEPARATOR);
        final List<PathMatcher> matchers = Arrays.stream(patterns)
                .map(pattern -> FileSystems.getDefault().getPathMatcher("glob:" + pattern)).toList();
        return path -> matchers.stream().anyMatch(matcher -> matcher.matches(path));
    }

    public static File findExecutableOnPath(final String name) {
        for (String dirname : System.getenv("PATH").split(File.pathSeparator)) {
            File file = new File(dirname, name);
            if (file.isFile() && file.canExecute()) {
                return file.getAbsoluteFile();
            }
        }
        throw new AssertionError("should have found the executable");
    }

}
