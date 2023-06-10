package tk.xenon98.laundryapp.console.data.api;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public record MethodApi(String name, String returnType, List<String> parameterTypes) {

    public static MethodApi fromMethod(final Method method) {
        final String name = method.getName();
        final String returnType = formatType(method.getGenericReturnType());
        final List<String> parameterTypes = Arrays.stream(method.getGenericParameterTypes()).map(MethodApi::formatType)
                .toList();
        return new MethodApi(name, returnType, parameterTypes);
    }

    private static String formatType(final Type type) {
        return type.getTypeName();
    }

}
