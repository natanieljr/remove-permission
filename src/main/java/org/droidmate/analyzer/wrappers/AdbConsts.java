package org.droidmate.analyzer.wrappers;

/**
 * Constants to perform actions using ADB command line tool
 */
class AdbConsts {
    static final String REBOOT = "adb reboot";
    static final String UNLOCK = "adb shell input keyevent 82";
    // Returns Parcel(00000000 00000000) if locked and Parcel(00000000 00000001) if unlocked
    static final String CHECK_LOCKED = "adb shell service call power 12";

    static final int REBOOT_TIME = 60 * 1000; // ms
}
