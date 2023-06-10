
package tk.xenon98.laundryapp.driver;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;
import lombok.Getter;
import org.apache.commons.text.StringEscapeUtils;
import tk.xenon98.laundryapp.common.utils.Utils;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.Node;
import tk.xenon98.laundryapp.driver.xml.ObjectFactory;

public class UiDriver implements IDriver {

    private static final Pattern FOCUSED_WINDOW_PATTERN = Pattern.compile("\\{.* (?<activity>[^ ]*)}");
    @Getter
    private final AdbDriver adbDriver;

    public UiDriver(final AdbDriver adbDriver) {
        this.adbDriver = adbDriver;
    }

    public Hierarchy getUiHierarchy() throws ExecutionException, InterruptedException, IOException {
        return adbDriver.runShellCommand("uiautomator dump").thenCompose(result -> {
            final String[] tokens = result.split(": ", 2);
            final String path = tokens[1];
            try {
                return adbDriver.runShellCommandPiped("cat " + path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenApply(xml -> {
            try {
                final JAXBContext ctx = JAXBContext.newInstance(ObjectFactory.class);
                return ((JAXBElement<Hierarchy>) ctx.createUnmarshaller().unmarshal(xml)).getValue();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }

    public void launchApp(final String packageName) throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("monkey -p " + packageName + " 1").get();
    }

    public void tap(final int x, final int y) throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("input tap " + x + " " + y).get();
    }

    public void tap(final Node node) throws IOException, ExecutionException, InterruptedException {
        final int x = (int) node.getBounds().getCenterX();
        final int y = (int) node.getBounds().getCenterY();
        tap(x, y);
    }

    public void tap(final Rectangle rectangle) throws IOException, ExecutionException, InterruptedException {
        tap((int) rectangle.getCenterX(), (int) rectangle.getCenterY());
    }

    public BufferedImage screenshot() throws IOException, ExecutionException, InterruptedException {
        return adbDriver.runShellCommandPiped("screencap -p").thenApply(input -> {
            try {
                return ImageIO.read(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).get();
    }

    public String focusedWindow() throws IOException, InterruptedException, ExecutionException {
        return adbDriver.runShellCommand("dumpsys activity activities | grep mFocusedWindow").thenApply(text -> {
            final Matcher matcher = FOCUSED_WINDOW_PATTERN.matcher(text);
            if (!matcher.find()) {
                return "NO_PACKAGE/NO_ACTIVITY";
            }
            return matcher.group("activity");
        }).get();
    }

    public void scrollUp() throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("input touchscreen swipe 540 480 540 1440 1000").get();
    }

    public void scrollDown() throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("input touchscreen swipe 540 1440 540 480 1000").get();
    }

    public void swipeUp() throws IOException, InterruptedException, ExecutionException {
        scrollDown();
    }

    public void swipeDown() throws IOException, InterruptedException, ExecutionException {
        scrollUp();
    }

    public void homeButton() throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("input keyevent KEYCODE_HOME").get();
    }

    public void backButton() throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("input keyevent KEYCODE_BACK").get();
    }

    public void appSwitchButton() throws IOException, InterruptedException, ExecutionException {
        adbDriver.runShellCommand("input keyevent KEYCODE_APP_SWITCH").get();
    }

    public boolean isKeyboardShown() throws IOException, InterruptedException, ExecutionException {
        final String[] tokens =
                adbDriver.runShellCommand("dumpsys input_method | grep mInputShown").get().strip().split(" ");
        for (final String token : tokens) {
            final String[] kv = token.split("=", 2);
            if (kv.length != 2) {
                continue;
            }
            if (kv[0].equals("mInputShown")) {
                return Boolean.parseBoolean(kv[1]);
            }
        }
        throw new IOException("Cannot determine whether keyboard is shown");
    }

    public void hideKeyboard() throws IOException, ExecutionException, InterruptedException {
        if (isKeyboardShown()) {
            backButton();
        }
    }

    public void enterText(final String text) throws IOException, InterruptedException, ExecutionException {
        final String escapedText = Utils.escapePosixShell(text);
        adbDriver.runShellCommand("input text " + escapedText).get();
    }

    public String focusedActivity() throws IOException, ExecutionException, InterruptedException {
        return focusedWindow().split("/")[1];
    }

    public String focusedWindowPackage() throws IOException, ExecutionException, InterruptedException {
        return focusedWindow().split("/")[0];
    }
}
