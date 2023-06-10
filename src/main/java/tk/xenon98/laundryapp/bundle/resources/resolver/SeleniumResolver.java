
package tk.xenon98.laundryapp.bundle.resources.resolver;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import tk.xenon98.laundryapp.bundle.resources.DownloadManager;
import tk.xenon98.laundryapp.bundle.resources.Resource;
import tk.xenon98.laundryapp.bundle.resources.ResourceRegistry;

public class SeleniumResolver implements IResourceResolver {

    public static final String URI_SCHEME = "selenium+https";
    private static final AtomicBoolean ensureInitCalled = new AtomicBoolean();
    private final DownloadManager downloadManager;

    public SeleniumResolver(final DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
        ensureInit();
    }

    private static void ensureInit() {
        if (!ensureInitCalled.get()) {
            synchronized (ensureInitCalled) {
                if (!ensureInitCalled.get()) {
                    WebDriverManager.chromedriver().setup();
                    ensureInitCalled.set(true);
                }
            }
        }
    }

    @Override
    public void resolve(final ResourceRegistry resourceRegistry, final Resource resource, final File targetFile)
            throws IOException {
        ensureCanHandleHost(resource.uri());
        final var driver = createDriver();
        try {
            final var link = resource.uri().toString().split("\\+", 2)[1];
            driver.get(link);
            allowCookies(driver);
            clickDownloadButton(driver);
            final URL downloadLink = new URL(getDownloadLink(driver));
            downloadManager.download(downloadLink, targetFile, resource.integrity());
        } finally {
            driver.close();
        }
    }

    private void ensureCanHandleHost(final URI uri) throws IOException {
        if (!uri.getHost().equalsIgnoreCase("apkcombo.com")) {
            throw new IOException("Unsupported host: " + uri.getHost());
        }
    }

    private String getDownloadLink(final WebDriver driver) {
        return driver.findElements(new ByCssSelector("ul.file-list a")).get(0).getAttribute("href");
    }

    private void clickDownloadButton(final WebDriver driver) {
        final var downloadButton = new WebDriverWait(driver, Duration.ofSeconds(20)).until(
                (__) -> driver.findElements(By.cssSelector("a.button")).stream()
                        .filter(button -> button.getText().contains("Download APK")).findFirst().orElse(null));
        downloadButton.click();
    }

    private void allowCookies(final WebDriver driver) {
        new WebDriverWait(driver, Duration.ofSeconds(20)).until((__) -> driver.findElement(By.id("qc-cmp2-ui")));
        final var buttons = driver.findElements(new By.ByCssSelector("#qc-cmp2-ui button"));
        final var agreeButton =
                buttons.stream().filter(button -> button.getText().equalsIgnoreCase("agree")).findFirst()
                        .orElseThrow();
        agreeButton.click();
    }

    private WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        return new ChromeDriver(options);
    }
}
