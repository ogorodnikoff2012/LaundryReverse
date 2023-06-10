package tk.xenon98.laundryapp.console.data.api;

import java.util.List;

public record MethodCallRequest(String driver, MethodApi method, List<String> arguments) {

}
