package mil.nga.giat.asam.test;

import junit.framework.Assert;
import mil.nga.giat.asam.test.util.Screenshot;
import mil.nga.giat.asam.test.util.TestConstants;

import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;



public class TabletTest implements DeviceTest {

    private static final String DISCLAIMER_SCREENSHOT_FORMAT = "Disclaimer_%d";
    private static final String LIST_SCREENSHOT_FORMAT = "List_%d";
    private UiAutomatorTestCase mUiAutomatorTestCase;
    private int mScreenshotCounter;
    
    public TabletTest(UiAutomatorTestCase uiAutomatorTestCase) {
        mUiAutomatorTestCase = uiAutomatorTestCase;
        mScreenshotCounter = 1;
    }
    
    @Override
    public void runTest() {
        closeDisclaimer();
        runListTest();
    }
    
    private void closeDisclaimer() {
        UiObject closeButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("I Agree"));
        try {
            System.out.println("Closing disclaimer.");
            Screenshot.takeScreenshot(String.format(DISCLAIMER_SCREENSHOT_FORMAT, mScreenshotCounter++));
            closeButton.click();
            if (!closeButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT)) {
                Assert.fail("Failed to close disclaimer prompt!");
            }
        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed to close disclaimer prompt! " + caught.getMessage());
        }
    }
    
    private void runListTest() {
        try {
            
            // NOTE: 5 * PAUSE is to give a little extra time for the initial web service call.
            Screenshot.takeScreenshot(String.format(LIST_SCREENSHOT_FORMAT, mScreenshotCounter++), 5 * TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            System.out.println("Clicking list.");
            UiObject listButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("List View"));
            listButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(LIST_SCREENSHOT_FORMAT, mScreenshotCounter++));
        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed exercising List! " + caught.getMessage());
        }
    }
}
