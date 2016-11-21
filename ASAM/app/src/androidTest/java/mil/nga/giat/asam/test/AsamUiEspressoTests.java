package mil.nga.giat.asam.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import mil.nga.giat.asam.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;


import mil.nga.giat.asam.R;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AsamUiEspressoTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);


    @Before
    public void agreeToTerms() {
        // agree to disclaimer
        onView(withId(R.id.disclaimer_agree_button_ui)).perform(click());
        sleep(1000);
    }

    private void clearFilters() {
        Log.d("clearFilters", "Clearing Filters");
        // click on filters icon
        onView(withId(R.id.all_asams_map_menu_search_ui)).perform(click());
        sleep(1000);

        // click the CLEAR button
        onView(withId(R.id.reset)).perform(click());
        sleep(1000);

        // click the APPLY button
        onView(withId(R.id.apply)).perform(click());
        sleep(1000);
    }

    @Test
    public void filterTimeIntervalTest() {
        Log.d("filterTimeIntervalTest", "Filter Time Interval Test");

        clearFilters();

        List<String> options = new ArrayList<>();
        options.add("60 days");
        options.add("90 days");
        options.add("180 days");
        options.add("1 year");
        options.add("5 years");
        options.add("All");

        for (String option : options) {
            // click on filters icon
            onView(withId(R.id.all_asams_map_menu_search_ui)).perform(click());
            sleep(1000);

            onView(withId(R.id.interval_spinner)).perform(click());
            sleep(1000);

            onData(allOf(is(instanceOf(String.class)), is(option))).perform(click());
            sleep(1000);

            onView(withId(R.id.apply)).perform(click());
            sleep(1000);
        }
    }

    @Test
    public void filterCustomTimeIntervalTest() {

        // click on filters icon
        onView(withId(R.id.all_asams_map_menu_search_ui)).perform(click());
        sleep(1000);

        onView(withId(R.id.interval_spinner)).perform(click());
        sleep(1000);

        onData(allOf(is(instanceOf(String.class)), is("Custom"))).perform(click());
        sleep(1000);

        // Set From Date
        onView(withId(R.id.text_query_date_from_edit_text_ui)).perform(click());
        sleep(1000);
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2014, 10, 30));
        sleep(1000);
        onView(withId(android.R.id.button1)).perform(click());
        sleep(1000);

        // Set To Date
        onView(withId(R.id.text_query_date_to_edit_text_ui)).perform(click());
        sleep(1000);
        onView(isAssignableFrom(DatePicker.class)).perform(PickerActions.setDate(2016, 10, 30));
        sleep(1000);
        onView(withId(android.R.id.button1)).perform(click());
        sleep(1000);

        // click the APPLY button
        onView(withId(R.id.apply)).perform(click());
        sleep(1000);

        clearFilters();

    }

    @Test
    public void runSubregionFilterTest() {

        // click on filters icon
        onView(withId(R.id.all_asams_map_menu_search_ui)).perform(click());
        sleep(1000);

        // open subregions display
        onView(withId(R.id.subregions)).perform(click());
        sleep(1000);

        // click on a subregion
        onView(withId(R.id.subregion_map_map_view_ui)).perform(clickPercent(.50f, .50f));
        sleep(1000);

        onView(withId(R.id.apply)).perform(click());
        sleep(1000);

        onView(withId(R.id.apply)).perform(click());
        sleep(1000);

        clearFilters();
    }

    @Test
    public void runSubregionMapOverlayTest() {
        Log.d("runSubMapOverlayTest", "Click Filter");
        onView(withId(R.id.all_asams_map_menu_search_ui)).perform(click());
        sleep(1000);

        Log.d("runSubMapOverlayTest", "Click Subregion EditText");
        onView(withId(R.id.subregions)).perform(click());
        sleep(1000);

        Log.d("runSubMapOverlayTest", "Selecting Satellite Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_satellite_text)).perform(click());
        sleep(1000);

        Log.d("runSubMapOverlayTest", "Selecting Hybrid Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_hybrid_text)).perform(click());
        sleep(1000);

        Log.d("runSubMapOverlayTest", "Selecting Offline Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_offline_text)).perform(click());
        sleep(1000);

        Log.d("runSubMapOverlayTest", "Selecting Normal Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_normal_text)).perform(click());
        sleep(1000);
    }

    @Test
    public void runSubregionMenuTest() {
        // click on filters icon
        onView(withId(R.id.all_asams_map_menu_search_ui)).perform(click());
        sleep(1000);

        // open subregions display
        onView(withId(R.id.subregions)).perform(click());
        sleep(1000);

        // click on a subregion
        onView(withId(R.id.subregion_map_map_view_ui)).perform(clickPercent(.50f, .50f));
        sleep(1000);

        // open menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        sleep(1000);
        // click on selected subregions
        onView(withText(R.string.subregion_map_menu_selected_subregions_text)).perform(click());
        sleep(1000);
        // verify toast value
        onView(withText("57")).inRoot(isDialog()).check(matches(isDisplayed()));
        // exit toast
        pressBack();

        // apply
        onView(withId(R.id.apply)).perform(click());
        sleep(1000);

        // open subregions display
        onView(withId(R.id.subregions)).perform(click());
        sleep(1000);

        // open menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        sleep(1000);
        // click clear
        onView(withText(R.string.subregion_map_menu_clear_text)).perform(click());
        sleep(1000);

        // open menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        sleep(1000);
        // click on selected subregions
        onView(withText(R.string.subregion_map_menu_reset_text)).perform(click());
        sleep(1000);

        // open menu
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        sleep(1000);
        // click on selected subregions
        onView(withText(R.string.subregion_map_menu_selected_subregions_text)).perform(click());
        sleep(1000);
        // verify toast value
        onView(withText("57")).inRoot(isDialog()).check(matches(isDisplayed()));
        // exit toast
        pressBack();

        // apply
        onView(withId(R.id.apply)).perform(click());
        sleep(1000);

        // apply
        onView(withId(R.id.apply)).perform(click());
        sleep(1000);

        clearFilters();

    }

    @Test
    public void runMapOverlayTest() {
        Log.d("runMapOverlayTest", "Selecting Satellite Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_satellite_text)).perform(click());
        sleep(1000);

        Log.d("runMapOverlayTest", "Selecting Hybrid Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_hybrid_text)).perform(click());
        sleep(1000);

        Log.d("runMapOverlayTest", "Selecting Offline Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_offline_text)).perform(click());
        sleep(1000);

        Log.d("runMapOverlayTest", "Selecting Normal Map Type");
        onView(withId(R.id.map_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.map_type_normal_text)).perform(click());
        sleep(1000);
    }

    @Test
    public void runGraticulesTest() {
        Log.d("runGraticulesTest", "Selecting 10 degree Gratucles");
        onView(withId(R.id.graticules_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.grat_10_degrees)).perform(click());
        sleep(1000);

        Log.d("runGraticulesTest", "Selecting 15 degree Gratucles");
        onView(withId(R.id.graticules_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.grat_15_degrees)).perform(click());
        sleep(1000);

        Log.d("runGraticulesTest", "Selecting 20 degree Gratucles");
        onView(withId(R.id.graticules_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.grat_20_degrees)).perform(click());
        sleep(1000);

        Log.d("runGraticulesTest", "Selecting 30 degree Gratucles");
        onView(withId(R.id.graticules_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.grat_30_degrees)).perform(click());
        sleep(1000);

        Log.d("runGraticulesTest", "Selecting None Gratucles");
        onView(withId(R.id.graticules_overlay_menu)).perform(click());
        sleep(1000);
        onView(withText(R.string.grat_none)).perform(click());
        sleep(1000);
    }

    @Test
    public void runModuInformationTest() {
        Log.d("runModuInformationTest", "Selection About Menu");
        onView(withId(R.id.about)).perform(click());
        sleep(1000);

        Log.d("runModuInformationTest", "Click About Item");
        onView(withText(R.string.all_asams_about_title_text)).perform(click());
        sleep(1000);
        pressBack();
        sleep(1000);

        Log.d("runModuInformationTest", "Click Submit Report Item");
        onView(withText(R.string.all_asams_report_title_text)).perform(click());
        sleep(1000);
        pressBack();
        sleep(1000);

        Log.d("runModuInformationTest", "Click Disclaimer Item");
        onView(withText(R.string.disclaimer_title_text)).perform(click());
        sleep(1000);
        pressBack();
        sleep(1000);

        Log.d("runModuInformationTest", "Click NGA Privacy Policy Item");
        onView(withText(R.string.legal_fragment_nga_privacy_policy_label_text)).perform(click());
        sleep(1000);
        pressBack();
        sleep(1000);

        Log.d("runModuInformationTest", "Click Open Source Licenses Item");
        onView(withText(R.string.legal_fragment_nga_open_source_licenses_label_text)).perform(click());
        sleep(1000);
        pressBack();
        sleep(1000);
        pressBack();
        sleep(1000);
    }

    @Test
    public void runListSortTest() {
        Log.d("runListSortTest", "Click on List Menu Item");
        onView(withId(R.id.all_asams_map_menu_list_view_ui)).perform(click());
        sleep(1000);

        List<String> options = new ArrayList<>();
        options.add("Occurrence Date");
        options.add("Subregions");
        options.add("Reference Number");
        options.add("Victim");
        options.add("Aggressor");

        List<String> directions = new ArrayList<>();
        directions.add("Ascending");
        directions.add("Descending");

        for (String option : options) {
            for (String direction: directions) {
                Log.d("runListSortTest", "Click on Sort Menu Item");
                onView(withId(R.id.asam_list_fragment_menu_sort_ui)).perform(click());
                sleep(1000);

                Log.d("runListSortTest", "Click on Sort Spinner");
                onView(withId(R.id.sort_asam_list_dialog_fragment_sort_spinner_ui)).inRoot(isDialog()).perform(click());
                sleep(1000);

                Log.d("runListSortTest", "Click on Sort Type");
                onData(allOf(is(instanceOf(String.class)), is(option))).inRoot(isPlatformPopup()).perform(click());
                sleep(1000);

                Log.d("runListSortTest", "Click on Direction");
                onView(withText(direction)).perform(click());
                sleep(1000);

                Log.d("runListSortTest", "Click on SORT button");
                onView(withId(android.R.id.button1)).perform(click());
                sleep(1000);

            }
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static ViewAction clickPercent(final float pctX, final float pctY){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);
                        int w = view.getWidth();
                        int h = view.getHeight();

                        float x = w * pctX;
                        float y = h * pctY;

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
    }



}
