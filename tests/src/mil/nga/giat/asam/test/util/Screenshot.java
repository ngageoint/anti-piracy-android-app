package mil.nga.giat.asam.test.util;

import java.io.File;

import com.android.uiautomator.core.UiDevice;


public class Screenshot {

    public static boolean takeScreenshot(String name, int preScreenshotSleepTime) {
        try {
            Thread.sleep(preScreenshotSleepTime);
        }
        catch (Exception ignore) {}
        File screenshotDirectory = new File(TestConstants.SCREENSHOT_LOCATION);
        if (!screenshotDirectory.exists()) {
            boolean successful = screenshotDirectory.mkdirs();
            if (!successful) {
                System.out.println("Couldn't make snapshot directory");
            }
        }
        String screenshotName = String.format("%s/%s.png", TestConstants.SCREENSHOT_LOCATION, name);
        System.out.println(String.format("Taking screenshot %s", screenshotName));
        return true; //UiDevice.getInstance().takeScreenshot(new File(screenshotName));
    }
    
    public static boolean takeScreenshot(String name) {
        return takeScreenshot(name, 0);
    }
}
