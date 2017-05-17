package org.droidmate.analyzer.wrappers;

/**
 * Constants to perform actions using ADB command line tool
 */
class AdbConsts {
    static String INSTALL = "adb install";
    static String UNINSTALL = "adb uninstall";
    static String PULL = "adb pull";
    static String RUN_INTENT = "adb shell am start";
    static String REMOVE = "adb shell rm";
}
