Build and Deploy ASAM Test Case

NOTE: Use "<android-sdk>/tools/android list target" to find the correct build version for the app.

1) <android-sdk>/tools/android create uitest-project -n "ASAM_Test" -t 11 -p "<path-to-asam>/tests"
	- NOTE: here "-t 11" is target 11 on my system, ie "Google Inc.:Google APIs:20"
	- NOTE: Make sure there is no space in the -n "name" property
	
2) export ANDROID_HOME=<path-to-sdk>

3) ant build

4) connect device with ASAM installed

5) adb push <path-to-output-jar> /data/local/tmp

6) adb shell uiautomator runtest "/data/local/tmp/ASAM_Test.jar" -c mil.nga.giat.asam.test.TestAsam
