
package tk.xenon98.laundryapp.driver.laundry;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum MachineStatus {

    SELECT,
    BUSY,
    RESERVED,
    OUT_OF_ORDER,
    RESERVED_FOR_YOU,
    PUSH_START,
    UNKNOWN,
    ;

    private static final Map<String, MachineStatus> values = Arrays.stream(values()).collect(Collectors.toMap(
            MachineStatus::name,
            Function.identity()));

    public static MachineStatus parseValue(final String text) {
        return values.getOrDefault(text.toUpperCase().replace(' ', '_'), UNKNOWN);
    }
}
