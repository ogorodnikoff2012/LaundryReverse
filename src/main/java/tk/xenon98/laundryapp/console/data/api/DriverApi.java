package tk.xenon98.laundryapp.console.data.api;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public record DriverApi(String driverName, List<MethodApi> methods) {

    public static DriverApi fromClass(final Class<?> driverClass) {
        final var methods = Arrays.stream(driverClass.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers())).map(MethodApi::fromMethod).toList();
        return new DriverApi(driverClass.getSimpleName(), methods);
    }
}
