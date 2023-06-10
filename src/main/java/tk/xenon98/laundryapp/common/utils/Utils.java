
package tk.xenon98.laundryapp.common.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.console.cfg.AdbDriverConfig;

public class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static void waitUntil(final BooleanSupplier condition, final String conditionName,
            final Duration timeout,
            final Duration delay)
            throws InterruptedException, TimeoutException {
        final Instant startTime = Instant.now();
        final Instant endTime = startTime.plus(timeout);
        int attemptCount = 1;
        do {
            LOG.info("Waiting for condition \"" + conditionName + "\", attempt #" + attemptCount);
            if (condition.getAsBoolean()) {
                LOG.info("Condition \"" + conditionName + "\" is met");
                return;
            }
            ++attemptCount;
            Thread.sleep(delay.toMillis());
        } while (Instant.now().isBefore(endTime));
        throw new TimeoutException(
                "Condition \"" + conditionName + "\" is not met within timeout of " + timeout);
    }

    public static String escapePosixShell(final String text) {
        final StringBuilder sb = new StringBuilder();
        sb.append("$'");
        text.codePoints().forEach(ch -> {
            switch (ch) {
                case ':', '&', ';', '<', '>', '(', ')', '$', '`', '\'', '\\', '"', '\n', ' ', '\t' ->
                    sb.append('\\').appendCodePoint(ch);
                default -> sb.appendCodePoint(ch);
            }
        });
        return sb.append("'").toString();
    }

    public static <T> Iterator<T[]> slidingWindow(final Iterator<T> underlying, int windowSize, final T[] a) {
        final var iter = new Iterator<T[]>() {

            final Queue<T> buffer = new ArrayDeque<>(windowSize);

            @Override
            public boolean hasNext() {
                return underlying.hasNext();
            }

            @Override
            public T[] next() {
                if (buffer.size() == windowSize) {
                    buffer.remove();
                }
                buffer.add(underlying.next());
                return buffer.toArray(a);
            }
        };
        for (int i = 1; i < windowSize && iter.hasNext(); i++) {
            iter.next();
        }
        return iter;
    }

    public static <T> Stream<T[]> slidingWindow(final Stream<T> stream, final int windowSize, final T[] a) {
        final var iter = slidingWindow(stream.iterator(), windowSize, a);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false);
    }

    public static Duration parseDuration(final String mmss) {
        final String[] parts = mmss.split(":");
        int minutes = Integer.parseInt(parts[0]);
        int seconds = Integer.parseInt(parts[1]);
        return Duration.ofSeconds(60 * minutes + seconds);
    }
}
