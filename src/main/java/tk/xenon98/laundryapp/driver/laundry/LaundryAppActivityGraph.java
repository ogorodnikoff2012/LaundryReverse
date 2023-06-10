
package tk.xenon98.laundryapp.driver.laundry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import tk.xenon98.laundryapp.common.utils.graph.IGraph;

public class LaundryAppActivityGraph implements IGraph<LaundryAppState> {

    public static final LaundryAppActivityGraph INSTANCE = new LaundryAppActivityGraph();

    private LaundryAppActivityGraph() {
    }

    @Override
    public Collection<LaundryAppState> outgoingNeighbours(final LaundryAppState from) {
        final Set<LaundryAppState> result = new HashSet<>();
        switch (from) {
            case SHUTDOWN -> result.add(LaundryAppState.START_ACTIVITY);
            case MENU -> result.addAll(List.of(
                    LaundryAppState.HOME_ACTIVITY,
                    LaundryAppState.LOGIN_ACTIVITY,
                    LaundryAppState.ORDER_HISTORY_ACTIVITY,
                    LaundryAppState.PROFILE_ACTIVITY,
                    LaundryAppState.SETTINGS_ACTIVITY,
                    LaundryAppState.TC_PRIVACY_ACTIVITY,
                    LaundryAppState.FAQ_ACTIVITY
            ));
            case START_ACTIVITY, LOGIN_ACTIVITY -> result.add(LaundryAppState.HOME_ACTIVITY);
            case HOME_ACTIVITY -> result.addAll(List.of(
                    LaundryAppState.SELECT_WASHER,
                    LaundryAppState.SELECT_DRYER,
                    LaundryAppState.RESERVE_WASHER
            ));
        }
        result.add(LaundryAppState.SHUTDOWN);
        if (from.hasMenu()) {
            result.add(LaundryAppState.MENU);
        }
        return result;
    }

}
