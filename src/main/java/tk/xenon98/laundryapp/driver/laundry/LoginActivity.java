
package tk.xenon98.laundryapp.driver.laundry;

import java.awt.Rectangle;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.HierarchyUtil;

public class LoginActivity implements IActivity {

    public static final String ACTIVITY_NAME = "com.digitalsense.android.londris.LoginActivity";
    private final LaundryAppDriver laundryAppDriver;
    private final UiDriver uiDriver;
    private Hierarchy uiHierarchy;
    private Rectangle emailTextField;
    private Rectangle passwordTextField;
    private Rectangle signInButton;

    public LoginActivity(final LaundryAppDriver laundryAppDriver) {
        this.laundryAppDriver = laundryAppDriver;
        this.uiDriver = laundryAppDriver.getUiDriver();
    }

    @Override
    public void init() throws IOException, ExecutionException, InterruptedException {
        this.uiDriver.hideKeyboard();
        uiHierarchy = this.uiDriver.getUiHierarchy();
        emailTextField = HierarchyUtil.findByResourceId(uiHierarchy,
                "com.innovationscript.lalaunderette:id/input_email_or_phone").getBounds();
        passwordTextField = HierarchyUtil.findByResourceId(uiHierarchy,
                "com.innovationscript.lalaunderette:id/input_password").getBounds();
        signInButton = HierarchyUtil.findByResourceId(uiHierarchy,
                "com.innovationscript.lalaunderette:id/button_login").getBounds();
    }

    public void enterEmail(final String email) throws IOException, ExecutionException, InterruptedException {
        this.uiDriver.hideKeyboard();
        this.uiDriver.tap(emailTextField);
        this.uiDriver.enterText(email);
    }

    public void enterPassword(final String password) throws IOException, ExecutionException, InterruptedException {
        this.uiDriver.hideKeyboard();
        this.uiDriver.tap(passwordTextField);
        this.uiDriver.enterText(password);
    }

    public void signIn() throws IOException, ExecutionException, InterruptedException {
        this.uiDriver.hideKeyboard();
        this.uiDriver.tap(signInButton);
    }
}
