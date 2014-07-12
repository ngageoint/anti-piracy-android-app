package mil.nga.giat.asam.test;

import junit.framework.Assert;
import mil.nga.giat.asam.test.util.Screenshot;
import mil.nga.giat.asam.test.util.TestConstants;

import com.android.uiautomator.core.UiCollection;
import com.android.uiautomator.core.UiDevice;
import com.android.uiautomator.core.UiObject;
import com.android.uiautomator.core.UiObjectNotFoundException;
import com.android.uiautomator.core.UiScrollable;
import com.android.uiautomator.core.UiSelector;
import com.android.uiautomator.testrunner.UiAutomatorTestCase;

public class PhoneTest implements DeviceTest {
    
    private static final String DISCLAIMER_SCREENSHOT_FORMAT = "Disclaimer_%d";
    private static final String MAP_VIEW_SCREENSHOT_FORMAT = "Map_View_%d";
    private static final String SUBREGION_SCREENSHOT_FORMAT = "Subregion_%d";
    private static final String ASAM_QUERY_SCREENSHOT_FORMAT = "ASAM_Query_%d";
    private static final String SETTINGS_SCREENSHOT_FORMAT = "ASAM_Query_%d";
    private static final String ABOUT_SCREENSHOT_FORMAT = "About_%d";
    private UiAutomatorTestCase mUiAutomatorTestCase;
    private int mScreenshotCounter;
    
    public PhoneTest(UiAutomatorTestCase uiAutomatorTestCase) {
        mUiAutomatorTestCase = uiAutomatorTestCase;
        mScreenshotCounter = 1;
    }

    @Override
    public void runTest() {
        closeDisclaimer();
        runMapViewTest();
        runSubregionTest();
        runTextQueryTest();
        runSettingsTest();
        runAboutTest();
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

    private void runMapViewTest() {
        try {

            // Should be on main launch screen.
            System.out.println("Clicking map view.");
            UiObject mapViewIcon = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.ImageView").description("Map View"));
            mapViewIcon.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(MAP_VIEW_SCREENSHOT_FORMAT, mScreenshotCounter++), 5 * TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            // NOTE: 5 * PAUSE is to give a little extra time for the initial web service call.
            
            exerciseMapTypes(MAP_VIEW_SCREENSHOT_FORMAT);
            
            // View the results in a list.
            System.out.println("Clicking list view.");
            UiObject listButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("List View"));
            listButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(MAP_VIEW_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            exerciseList(MAP_VIEW_SCREENSHOT_FORMAT);
            
            // Go back to map view.
            System.out.println("Going back to map view.");
            UiDevice.getInstance().pressBack();

            // Run a time query from map view.
            System.out.println("Running a time query.");
            UiObject queryButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Query"));
            queryButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject queryTextPopup = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("180 days"));
            queryTextPopup.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(MAP_VIEW_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Reset the map view.
            System.out.println("Resetting the map view.");
            UiDevice device = UiDevice.getInstance();
            device.pressMenu();
            UiObject centerButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Center Map"));
            centerButton.click();
            mUiAutomatorTestCase.sleep(TestConstants.UI_ACTION_TIMEOUT); // Give some time for the map to reset.
            Screenshot.takeScreenshot(String.format(MAP_VIEW_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.

            // Go back to main launch screen.
            System.out.println("Go back to launch screen.");
            UiDevice.getInstance().pressBack();

        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed exercising Map View! " + caught.getMessage());
        }
    }
    
    private void runSubregionTest() {
        try {
            
            // Should be on main launch screen.
            System.out.println("Clicking subregion.");
            UiObject subregionIcon = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.ImageView").description("Subregions"));
            subregionIcon.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            exerciseMapTypes(SUBREGION_SCREENSHOT_FORMAT);
            
            // Click 3/4 in x direction and 1/3 down y direction.
            System.out.println("Click a subregion.");
            UiDevice device = UiDevice.getInstance();
            int x = device.getDisplayWidth() * 1 / 2; // Backwards.
            int y = device.getDisplayHeight() / 3;
            System.out.println(String.format("Clicking on coordinate x: %d, y: %d", x, y));
            device.click(x, y);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Reset the screen.
            System.out.println("Resetting screen.");
            device.pressMenu();
            UiObject resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Reset"));
            resetButton.click();
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            mUiAutomatorTestCase.sleep(TestConstants.UI_ACTION_TIMEOUT); // Give some time for the map to reset.
            
            // Selected subregions popup.
            System.out.println("Finding selected subregions.");
            device = UiDevice.getInstance();
            System.out.println(String.format("Clicking on coordinate x: %d, y: %d", x, y));
            device.click(x, y);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            UiObject selectedSubregionsButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Selected Subregions"));
            if (!selectedSubregionsButton.exists()) {
                device.pressMenu();
                selectedSubregionsButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Selected Subregions"));
            }
            selectedSubregionsButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            UiObject closeButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Close"));
            closeButton.click();
            closeButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            
            // Now run a query. NOTE: Should still be clicked.
            System.out.println("Run a subregions query.");
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            UiObject queryButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Query"));
            queryButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            UiObject timePeriodButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("5 years"));
            timePeriodButton.clickAndWaitForNewWindow(3 * TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Click on the list button.
            System.out.println("Clicking on list view.");
            UiObject listButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("List View"));
            listButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            exerciseList(SUBREGION_SCREENSHOT_FORMAT);
            
            // Go back to query results.
            System.out.println("Going back to query results.");
            UiDevice.getInstance().pressBack();
            
            // Go back to query subregions.
            System.out.println("Going back to subregions.");
            UiDevice.getInstance().pressBack();
            
            // Go back to main launch screen.
            System.out.println("Going back to launch screen.");
            UiDevice.getInstance().pressBack();

        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed exercising Subregions! " + caught.getMessage());
        }
    }
    
    private void runTextQueryTest() {
        try {

            // Should be on main launch screen.
            System.out.println("Clicking ASAM Query.");
            UiObject subregionIcon = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.ImageView").description("ASAM Query"));
            subregionIcon.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
         
            // Do a subregion query.
            System.out.println("Running a subregion query.");
            UiObject subregionSpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            subregionSpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
            UiScrollable subregionList = new UiScrollable(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.ListView"));
            subregionList.scrollIntoView(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Subregion 62"));
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
            UiObject subregionButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Subregion 62"));
            subregionButton.click();
            subregionButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
            System.out.println("Clicking search button.");
            UiObject searchButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Search"));
            searchButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Click on the list button.
            System.out.println("Clicking on list view.");
            UiObject listButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("List View"));
            listButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SUBREGION_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            exerciseList(ASAM_QUERY_SCREENSHOT_FORMAT);
            
            // Go back to query results.
            System.out.println("Going back to query results.");
            UiDevice.getInstance().pressBack();
            
            // Reset the map view.
            System.out.println("Resetting the map view.");
            UiDevice device = UiDevice.getInstance();
            device.pressMenu();
            UiObject resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Center Map"));
            resetButton.click();
            mUiAutomatorTestCase.sleep(TestConstants.UI_ACTION_TIMEOUT); // Give some time for the map to reset.
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Go back to query screen.
            System.out.println("Going back to query screen.");
            UiDevice.getInstance().pressBack();

            System.out.println("Resetting query screen.");
            resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Reset"));
            resetButton.click();
            
            // Do a reference number query.
            System.out.println("Running a reference number query.");
            UiObject referenceYearEditText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.EditText").text("YYYY"));
            referenceYearEditText.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            referenceYearEditText.setText("2013");
            device.pressBack();
            UiObject referenceNumberEditText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.EditText").text("###"));
            referenceNumberEditText.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            referenceNumberEditText.setText("100");
            device.pressBack();
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
            System.out.println("Clicking search button.");
            searchButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Search"));
            searchButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Go back to query screen.
            System.out.println("Going back to query screen.");
            UiDevice.getInstance().pressBack();

            System.out.println("Resetting query screen.");
            resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Reset"));
            resetButton.click();
            
//            // Do a date query.
//            System.out.println("Running a date query.");
//            UiObject dateFromEditText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.EditText").resourceId(TestConstants.PACKAGE_NAME + ":id/text_query_date_from_edit_text_ui"));
//            dateFromEditText.setText("01/01/2013");
//            UiObject closeDateDialogButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Done"));
//            if (closeDateDialogButton.exists()) {
//                closeDateDialogButton.click();
//                closeDateDialogButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
//            }
//            UiObject dateToEditText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.EditText").resourceId(TestConstants.PACKAGE_NAME + ":id/text_query_date_to_edit_text_ui"));
//            dateToEditText.setText("06/01/2013");
//            closeDateDialogButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Done"));
//            if (closeDateDialogButton.exists()) {
//                closeDateDialogButton.click();
//                closeDateDialogButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
//            }
//            UiDevice.getInstance().pressBack(); // Dismiss date picker.
//            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
//            System.out.println("Clicking search button.");
//            searchButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Search"));
//            searchButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
//            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
//            
//            // Go back to query screen.
//            System.out.println("Going back to query screen.");
//            backButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("ASAM"));
//            backButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
//            System.out.println("Resetting query screen.");
//            resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Reset"));
//            resetButton.click();
            
            // Do a victim query.
            System.out.println("Running a victim query.");
            UiObject victimEditText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.EditText").description("Victim"));
            victimEditText.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            victimEditText.setText("tug");
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
            device.pressBack();
            System.out.println("Clicking search button.");
            searchButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Search"));
            searchButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Go back to query screen.
            System.out.println("Going back to query screen.");
            UiDevice.getInstance().pressBack();

            System.out.println("Resetting query screen.");
            resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Reset"));
            resetButton.click();
            
            // Do an aggressor query.
            System.out.println("Running an aggressor query.");
            UiObject aggressorEditText = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.EditText").description("Aggressor"));
            aggressorEditText.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            aggressorEditText.setText("greenpeace");
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++));
            device.pressBack();
            System.out.println("Clicking search button.");
            searchButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Search"));
            searchButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ASAM_QUERY_SCREENSHOT_FORMAT, mScreenshotCounter++), TestConstants.PAUSE_BEFORE_MAP_SCREENSHOT); // Give some time to draw map before taking screenshot.
            
            // Go back to query screen.
            System.out.println("Going back to query screen.");
            UiDevice.getInstance().pressBack();

            System.out.println("Resetting query screen.");
            resetButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Reset"));
            resetButton.click();
            
            // Go back to main launch screen.
            System.out.println("Go back to launch screen.");
            UiDevice.getInstance().pressBack();

        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed exercising ASAM Query! " + caught.getMessage());
        }
    }
    
    private void runSettingsTest() {
        try {

            // Should be on main launch screen.
            System.out.println("Clicking Settings.");
            UiObject settingsIcon = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.ImageView").description("Settings"));
            settingsIcon.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(SETTINGS_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Hide disclaimer.
            System.out.println("Hiding discalimer.");
            UiObject hideDisclaimerCheckbox = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckBox").text("Hide Disclaimer"));
            hideDisclaimerCheckbox.click();
            Screenshot.takeScreenshot(String.format(SETTINGS_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Unhide disclaimer.
            System.out.println("Unhiding discalimer.");
            hideDisclaimerCheckbox = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckBox").text("Hide Disclaimer"));
            hideDisclaimerCheckbox.click();
            Screenshot.takeScreenshot(String.format(SETTINGS_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Synchronize.
            System.out.println("Synchronizing.");
            UiObject synchronizeButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sync Now"));
            synchronizeButton.click();
            UiObject okButton = null;
            do {
                okButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("OK"));
                mUiAutomatorTestCase.sleep(TestConstants.UI_ACTION_TIMEOUT); // Give some time to sync.
            } while (!okButton.exists());
            Screenshot.takeScreenshot(String.format(SETTINGS_SCREENSHOT_FORMAT, mScreenshotCounter++));
            okButton.click();
            okButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            
            // Go back to main launch screen.
            System.out.println("Go back to launch screen.");
            UiDevice.getInstance().pressBack();

        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed exercising Settings! " + caught.getMessage());
        }
    }
    
    private void runAboutTest() {
        try {

            // Should be on main launch screen.
            System.out.println("Clicking About.");
            UiObject aboutButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("About"));
            aboutButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Go to legal screen.
            System.out.println("Clicking legal row.");
            UiObject legalRow = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Legal Information"));
            legalRow.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Click on NGA Disclaimer.
            System.out.println("Clicking NGA disclaimer.");
            UiObject disclaimerRow = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Disclaimer"));
            disclaimerRow.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Go back to legal screen.
            System.out.println("Going back to legal screen.");
            UiDevice.getInstance().pressBack();
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Click on NGA Privacy Policy.
            System.out.println("Clicking NGA privacy policy.");
            UiObject privacyRow = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("ASAM app"));
            privacyRow.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Go back to legal screen.
            System.out.println("Going back to legal screen.");
            UiDevice.getInstance().pressBack();
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Go back to about screen.
            System.out.println("Going back to about screen.");
            UiDevice.getInstance().pressBack();
            Screenshot.takeScreenshot(String.format(ABOUT_SCREENSHOT_FORMAT, mScreenshotCounter++));
            
            // Go back to main launch screen.
            System.out.println("Go back to launch screen.");
            UiDevice.getInstance().pressBack();
        }
        catch (UiObjectNotFoundException caught) {
            Assert.fail("Failed exercising About! " + caught.getMessage());
        }
    }
    
    private void exerciseMapTypes(String screenshotFormat) throws UiObjectNotFoundException {
        System.out.println("Selecting Satellite Map Type");
        UiObject mapTypeButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Map Type"));
        mapTypeButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
        UiObject satelliteButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Satellite"));
        satelliteButton.click();
        Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
        satelliteButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
        
        System.out.println("Selecting Hybrid Map Type");
        mapTypeButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
        UiObject hybridButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Hybrid"));
        hybridButton.click();
        Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
        hybridButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);

        System.out.println("Selecting Offline Map Type");
        mapTypeButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
        UiObject offlineButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Offline"));
        offlineButton.click();
        Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
        offlineButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
        
        System.out.println("Selecting Normal Map Type");
        mapTypeButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
        UiObject normalButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").text("Normal"));
        normalButton.click();
        Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
        normalButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
    }
    
    private void exerciseList(String screenshotFormat) throws UiObjectNotFoundException {
     
        // Click the first item in the list.
        UiCollection list = new UiCollection(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.ListView"));
        if (list != null && list.getChildCount(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.LinearLayout")) > 0) {
            System.out.println("Clicking a row in the list.");
            UiObject firstRow = list.getChildByInstance(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.LinearLayout"), 0);
            firstRow.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Go back to list view.
            System.out.println("Going back to list view.");
            UiDevice.getInstance().pressBack();
            
            // Sort the list by occurrence date ascending.
            System.out.println("Sorting by occurrence date ascending.");
            UiObject sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject occurrenceDateButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Occurrence Date"));
            occurrenceDateButton.click();
            occurrenceDateButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            UiObject ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Ascending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            UiObject listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by occurrence date descending.
            System.out.println("Sorting by occurrence date descending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            occurrenceDateButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Occurrence Date"));
            occurrenceDateButton.click();
            occurrenceDateButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            UiObject descendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Descending"));
            descendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by subregions ascending.
            System.out.println("Sorting by subregions ascending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject subregionsButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Subregions"));
            subregionsButton.click();
            subregionsButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Ascending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by subregions descending.
            System.out.println("Sorting by subregions descending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            subregionsButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Subregions"));
            subregionsButton.click();
            subregionsButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Descending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by reference number ascending.
            System.out.println("Sorting by reference number ascending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject referenceNumberButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Reference Number"));
            referenceNumberButton.click();
            referenceNumberButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Ascending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by reference number descending.
            System.out.println("Sorting by reference number descending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            referenceNumberButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Reference Number"));
            referenceNumberButton.click();
            referenceNumberButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Descending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by victim ascending.
            System.out.println("Sorting by victim ascending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject victimButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Victim"));
            victimButton.click();
            victimButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Ascending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by victim descending.
            System.out.println("Sorting by victim descending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            victimButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Victim"));
            victimButton.click();
            victimButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Descending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by aggressor ascending.
            System.out.println("Sorting by aggressor ascending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            UiObject aggressorButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Aggressor"));
            aggressorButton.click();
            aggressorButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Ascending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            
            // Sort the list by aggressor descending.
            System.out.println("Sorting by aggressor descending.");
            sortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.TextView").description("Sort"));
            sortButton.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            sortBySpinner = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Spinner"));
            sortBySpinner.clickAndWaitForNewWindow(TestConstants.UI_ACTION_TIMEOUT);
            aggressorButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.CheckedTextView").text("Aggressor"));
            aggressorButton.click();
            aggressorButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            ascendingButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.RadioButton").text("Descending"));
            ascendingButton.click();
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
            listSortButton = new UiObject(new UiSelector().packageName(TestConstants.PACKAGE_NAME).className("android.widget.Button").text("Sort"));
            listSortButton.click();
            listSortButton.waitUntilGone(TestConstants.UI_ACTION_TIMEOUT);
            Screenshot.takeScreenshot(String.format(screenshotFormat, mScreenshotCounter++));
        }
        else {
            System.out.println("No row to click.");
        }
    }
}
