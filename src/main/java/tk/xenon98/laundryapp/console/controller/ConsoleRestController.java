
package tk.xenon98.laundryapp.console.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tk.xenon98.laundryapp.console.data.api.DriverApi;
import tk.xenon98.laundryapp.console.data.api.MethodApi;
import tk.xenon98.laundryapp.console.data.api.MethodCallRequest;
import tk.xenon98.laundryapp.console.data.serial.RectangleSerializer;
import tk.xenon98.laundryapp.driver.AdbDriver;
import tk.xenon98.laundryapp.driver.AppManager;
import tk.xenon98.laundryapp.driver.AvdManagerDriver;
import tk.xenon98.laundryapp.driver.EmulatorDriver;
import tk.xenon98.laundryapp.driver.IDriver;
import tk.xenon98.laundryapp.driver.NexusLauncherAppDriver;
import tk.xenon98.laundryapp.driver.SdkManagerDriver;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.laundry.LaundryAppDriver;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;

@RestController
@RequestMapping("/console/driver")
public class ConsoleRestController {

    private static final JsonSerializer<Rectangle> RECTANGLE_SERIALIZER = new RectangleSerializer();

    private final Map<String, IDriver> drivers = new HashMap<>();
    private final ArgumentParser argumentParser = new ArgumentParser();

    @Autowired
    private AdbDriver adbDriver;
    @Autowired
    private UiDriver uiDriver;
    @Autowired
    private LaundryAppDriver laundryAppDriver;
    @Autowired
    private AvdManagerDriver avdManagerDriver;
    @Autowired
    private EmulatorDriver emulatorDriver;
    @Autowired
    private SdkManagerDriver sdkManagerDriver;
    @Autowired
    private NexusLauncherAppDriver nexusLauncherAppDriver;
    @Autowired
    private AppManager appManager;

    @Bean
    public Void setupConsoleRestController() {
        drivers.put("adb", adbDriver);
        drivers.put("ui", uiDriver);
        drivers.put("laundry_app", laundryAppDriver);
        drivers.put("launcher_app", nexusLauncherAppDriver);
        drivers.put("avd_manager", avdManagerDriver);
        drivers.put("emulator", emulatorDriver);
        drivers.put("sdk_manager", sdkManagerDriver);
        drivers.put("app_manager", appManager);

        return null;
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.serializers(RECTANGLE_SERIALIZER);
        };
    }

    @GetMapping("/api/{driverName}")
    public DriverApi getDriver(@PathVariable String driverName) {
        if (this.drivers.containsKey(driverName)) {
            return DriverApi.fromClass(this.drivers.get(driverName).getClass());
        } else {
            throw new IllegalArgumentException("DriverApi " + driverName + " doesn't exist");
        }
    }

    @GetMapping("/list")
    public List<String> driverList() {
        return new ArrayList<>(this.drivers.keySet());
    }

    @PostMapping("/call")
    public Object call(@RequestBody MethodCallRequest methodCallRequest) throws InvocationTargetException,
            IllegalAccessException, ExecutionException, InterruptedException, IOException {
        final Object driver = findDriver(methodCallRequest.driver());
        final Method method = findMethod(driver, methodCallRequest.method());
        final Object[] arguments = new Object[methodCallRequest.arguments().size()];
        for (int i = 0; i < arguments.length; ++i) {
            arguments[i] = this.argumentParser.parseArgument(method.getParameterTypes()[i],
                    methodCallRequest.arguments().get(i));
        }
        final Object result = method.invoke(driver, arguments);
        return prepareToSend(result);
    }

    @GetMapping("/api/ui/ui_info")
    public Hierarchy uiInfo() throws IOException, ExecutionException, InterruptedException {
        return uiDriver.getUiHierarchy();
    }

    @GetMapping("/api/ui/screenshot")
    public ImageHolder screenshot() throws IOException, ExecutionException, InterruptedException {
        return prepareImage(uiDriver.screenshot());
    }

    private Object prepareToSend(Object result) throws ExecutionException, InterruptedException, IOException {
        while (true) {
            if (result == null) {
                result = NullNode.getInstance();
            } else if (result instanceof CompletableFuture<?> future) {
                result = future.get();
            } else if (result instanceof Image img) {
                result = prepareImage(img);
            } else if (result instanceof InputStream input) {
                result = new String(input.readAllBytes());
            } else {
                break;
            }
        }

        return result;
    }

    private static ImageHolder prepareImage(final Image img) throws IOException {
        final BufferedImage bufImg = toBufferedImage(img);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bufImg, "PNG", out);
        return new ImageHolder("data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray()));
    }

    private static BufferedImage toBufferedImage(final Image img) {
        if (img instanceof BufferedImage bufImg) {
            return bufImg;
        }

        final BufferedImage bufImg = new BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = bufImg.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return bufImg;
    }

    private static class ArgumentParser {

        private final ObjectMapper jsonObjectMapper = new JsonMapper();

        private Object parseArgument(final Class<?> clazz, final String rawValue) throws JsonProcessingException {
            if (String.class.equals(clazz)) {
                return rawValue;
            } else if (int.class.equals(clazz)) {
                return Integer.parseInt(rawValue);
            } else if (Rectangle.class.equals(clazz)) {
                return RectangleSerializer.parseRawBounds(rawValue);
            } else if (clazz.isEnum()) {
                return Enum.valueOf((Class<? extends Enum>) clazz, rawValue);
            }
            return jsonObjectMapper.readValue(rawValue, clazz);
        }
    }

    private IDriver findDriver(final String driverName) {
        final var maybeDriver = this.drivers.values().stream().filter(driver -> driver.getClass().getSimpleName()
                .equals(driverName)).findFirst();
        if (maybeDriver.isPresent()) {
            return maybeDriver.get();
        } else {
            throw new IllegalArgumentException("IDriver " + driverName + " doesn't exist");
        }
    }

    private Method findMethod(final Object driver, final MethodApi methodApi) {
        return Arrays.stream(driver.getClass().getDeclaredMethods())
                .filter(method -> MethodApi.fromMethod(method).equals(methodApi)).findFirst().orElseThrow();
    }

    private record ImageHolder(String imgBase64) {

    }
}
