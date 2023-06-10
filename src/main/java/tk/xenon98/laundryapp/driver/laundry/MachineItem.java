
package tk.xenon98.laundryapp.driver.laundry;

import java.time.Duration;
import java.util.Optional;

public record MachineItem(String name, String size, MachineStatus status, Optional<Duration> timeLeft) {

}
