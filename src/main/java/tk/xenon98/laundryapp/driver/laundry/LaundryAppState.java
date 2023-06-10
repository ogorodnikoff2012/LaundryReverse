
package tk.xenon98.laundryapp.driver.laundry;

import com.google.common.base.Functions;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

public enum LaundryAppState {

    START_ACTIVITY("com.digitalsense.android.londris.StartActivity", null, false),
    REPORT_ACTIVITY("com.digitalsense.android.londris.ReportActivity", null, false),
    TC_LOGGED_OUT_ACTIVITY("com.digitalsense.android.londris.TCLoggedOutActivity", null, false),
    FORGOT_PASSWORD_ACTIVITY("com.digitalsense.android.londris.ForgotPasswordActivity", null, false),
    SELECT_MACHINE_ACTIVITY("com.digitalsense.android.londris.SelectMachineActivity", null, true),
    SELECT_PROGRAM_ACTIVITY("com.digitalsense.android.londris.SelectProgramActivity", null, false),
    SELECT_ADDON_ACTIVITY("com.digitalsense.android.londris.SelectAddonActivity", null, false),
    LOGIN_ACTIVITY("com.digitalsense.android.londris.LoginActivity", LoginActivity.class, false),
    HOME_ACTIVITY("com.digitalsense.android.londris.HomeActivity", HomeActivity.class, true),
    REGISTRATION_ACTIVITY("com.digitalsense.android.londris.RegistrationActivity", null, false),
    ORDER_HISTORY_ACTIVITY("com.digitalsense.android.londris.OrderHistoryActivity", null, true),
    PROFILE_ACTIVITY("com.digitalsense.android.londris.ProfileActivity", null, true),
    SETTINGS_ACTIVITY("com.digitalsense.android.londris.SettingsActivity", null, true),
    TC_PRIVACY_ACTIVITY("com.digitalsense.android.londris.TCPrivacyActivity", null, true),
    FAQ_ACTIVITY("com.digitalsense.android.londris.FAQActivity", null, true),
    ORDER_ACTIVITY("com.digitalsense.android.londris.OrderActivity", null, false),
    CHECKOUT_ACTIVITY("com.digitalsense.android.londris.CheckoutActivity", null, false),
    PAYZEE_ACTIVITY("com.digitalsense.android.londris.PayzeeActivity", null, false),
    START_MACHINES_ACTIVITY("com.digitalsense.android.londris.StartMachinesActivity", null, false),
    BOOKING_CONFIRMATION_ACTIVITY("com.digitalsense.android.londris.BookingConfirmationActivity", null, false),
    INFO_ACTIVITY("com.digitalsense.android.londris.InfoActivity", null, false),
    REMEMBER_ACTIVITY("com.digitalsense.android.londris.RememberActivity", null, false),
    ERROR_API_ACTIVITY("com.digitalsense.android.londris.ErrorApiActivity", null, false),
    CHOOSE_RESERVATION_ACTIVITY("com.digitalsense.android.londris.ChooseReservationActivity", null, false),
    EXTENDED_RESERVATION_ACTIVITY("com.digitalsense.android.londris.ExtendedReservationActivity", null, false),
    RESERVATIONS_ACTIVITY("com.digitalsense.android.londris.ReservationsActivity", null, false),
    EXTERNAL_LOCKERS_ACTIVITY("com.digitalsense.android.londris.ExternalLockersActivity", null, false),
    SELECT_COIN_PACKAGE_ACTIVITY("com.digitalsense.android.londris.SelectCoinPackageActivity", null, false),
    CHECKOUT_COIN_PACKAGE_ACTIVITY("com.digitalsense.android.londris.CheckoutCoinPackageActivity", null, false),

    MENU(null, ActivityWithMenu.class, true),
    SHUTDOWN(null, null, false),

    SELECT_WASHER(null, SelectWasherActivity.class, true),
    SELECT_DRYER(null, SelectDryerActivity.class, true),
    RESERVE_WASHER(null, ReserveWasherActivity.class, true),
    ;

    private static final Map<String, LaundryAppState> stateByActivityName;
    private static final Map<Class<? extends IActivity>, LaundryAppState> stateByClass;

    static {
        stateByActivityName = Arrays.stream(values())
                .filter(state -> state.activityName != null)
                .collect(Collectors.toMap(LaundryAppState::getActivityName, Functions.identity()));
        stateByClass = Arrays.stream(values())
                .filter(state -> state.activityClass != null)
                .collect(Collectors.toMap(LaundryAppState::getActivityClass, Function.identity()));
    }

    @Getter
    private final String activityName;

    @Getter
    private final Class<? extends IActivity> activityClass;
    private final boolean hasMenu;

    LaundryAppState(final String activityName, final Class<? extends IActivity> activityClass,
            final boolean hasMenu) {
        this.activityName = activityName;
        this.activityClass = activityClass;
        this.hasMenu = hasMenu;
    }

    public boolean hasMenu() {
        return this.hasMenu;
    }

    public boolean isRealActivity() {
        return this.activityName != null;
    }

    public static LaundryAppState findByActivityName(final String activityName) {
        return stateByActivityName.get(activityName);
    }

    public static LaundryAppState findByActivityClass(final Class<? extends IActivity> activityClass) {
        return stateByClass.get(activityClass);
    }

    public static void main(String[] args) {
        for (final LaundryAppState state : values()) {
            System.out.println(state.name());
        }
    }
}
