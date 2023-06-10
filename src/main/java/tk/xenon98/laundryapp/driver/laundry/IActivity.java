
package tk.xenon98.laundryapp.driver.laundry;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface IActivity {

    void init() throws IOException, ExecutionException, InterruptedException;

}
