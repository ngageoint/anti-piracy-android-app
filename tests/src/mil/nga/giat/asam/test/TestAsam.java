package mil.nga.giat.asam.test;

import java.io.IOException;

import mil.nga.giat.asam.test.util.TestConstants;

import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.core.UiWatcher;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class TestAsam extends UiAutomatorTestCase {

    
    public void testDevice() {
        DeviceTest deviceTest;
        if (isPhone()) {
            System.out.println("Testing phone.");
            deviceTest = new PhoneTest(this);
        }
        else {
            System.out.println("Testing tablet.");
            deviceTest = new TabletTest(this);
        }
        deviceTest.runTest();
    }

    @Override
    public void setUp() {
        try {
            closeApplication();
            runGooglePlayWatcher();
            launchApplication();
        }
        catch (Exception caught) {
            fail("Failed to start application " + caught.getMessage());
        }
    }

    private void launchApplication() throws IOException {
        Runtime.getRuntime().exec(String.format("am start -n %s/%s", TestConstants.PACKAGE_NAME, TestConstants.LAUNCH_ACTIVITY));

        UiObject root = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME));
        root.waitForExists(TestConstants.UI_ACTION_TIMEOUT);
    }

    private void closeApplication() throws IOException {
        Runtime.getRuntime().exec(String.format("am force-stop %s", TestConstants.PACKAGE_NAME));

        UiObject root = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME));
        root.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
    }

    private void runGooglePlayWatcher() {
        UiWatcher watcher = new UiWatcher() {

            @Override
            public boolean checkForCondition() {

                // Check to see if update message is there.
                UiObject updateText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).textContains("update Google Play"));
                if (updateText.exists()) {

                    // If update message is there, find the Update button.
                    UiObject updateButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").textContains("Update"));
                    System.out.println("update button exists: " + updateButton.exists());
                    try {

                        // Click the update button which should take you to Google play store app.
                        boolean clicked = updateButton.click();
                        if (!clicked) {
                            fail("Failed to click Update button");
                        }

                        // Click the UPDATE button for the Google Play Services app.
                        UiObject updateServicesButton = new UiObject(new UiSelector().packageName("com.android.vending").className("android.widget.Button").textContains("UPDATE"));
                        try {
                            clicked = updateServicesButton.click();
                            if (!clicked) {
                                fail("Failed to click GooglePlay Services UPDATE button");
                            }

                            // Click the ACCEPT button for the Google Play Services app permissions prompt.
                            UiObject acceptPermissionButton = new UiObject(new UiSelector().packageName("com.android.vending").className("android.widget.Button").textContains("ACCEPT"));
                            try {
                                clicked = acceptPermissionButton.click();
                                if (!clicked) {
                                    fail("Failed to click Google Play Services ACCEPT button");
                                }
                                return clicked;
                            }
                            catch (UiObjectNotFoundException caught) {
                                fail("Failed to find ACCEPT button in Google Play Services permissions prompt!");
                            }
                        }
                        catch (UiObjectNotFoundException caught) {
                            fail("Failed to find UPDATE button in Google play services!");
                        }

                    }
                    catch (UiObjectNotFoundException caught) {
                        fail("Update button not found?!?");
                    }
                }
                return false;
            }
        };
        UiDevice.getInstance().registerWatcher("GooglePlayServicesUpdater", watcher);
        UiDevice.getInstance().runWatchers();
    }
    
    private boolean isPhone() {
        UiObject disclaimerText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).textContains("Disclaimer"));
        if (!disclaimerText.waitForExists(5 * TestConstants.UI_ACTION_TIMEOUT)) {
             fail("Did not find disclaimer!");
             return false;
        }
        
        // Phone has an action bar item and table has a button.
        UiObject closePhoneButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").description("I Agree"));
        UiObject closeTabletButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("I Agree"));
        if (closePhoneButton.exists()) {
            return true;
        }
        else if (closeTabletButton.exists()) {
            return false;
        }
        else {
            fail("Cannot determine phone or tablet!");
        }
        return false;
    }
}
