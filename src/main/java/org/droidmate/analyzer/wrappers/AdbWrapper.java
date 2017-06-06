package org.droidmate.analyzer.wrappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper to the ADB command line tool
 */
class AdbWrapper {
    private static final Logger logger = LoggerFactory.getLogger(AdbWrapper.class);
    private final int deviceIdx;
    private String deviceSerialNumber;

    AdbWrapper(int deviceIdx){
        this.deviceIdx = deviceIdx;

        this.setDeviceSerialNumber();
    }

    private void setDeviceSerialNumber()
    {
        if (this.deviceSerialNumber == null) {

            //logger.info("Executing adb to get the list of available Android Devices");
            try {
                CommandLineWrapper exec = new CommandLineWrapper();
                String[] output = exec.execute(AdbConsts.DEVICES);

                String[] adbOutput = output[0].split("\n");
                // adb devices output has 1 header line
                String targetDevice = adbOutput[this.deviceIdx + 1];

                // Line structure <SERIAL>\t<STATUS>
                this.deviceSerialNumber = targetDevice.split("\t")[0];

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        assert this.deviceSerialNumber != null;
    }

    int getDeviceIdx(){
        //logger.info("Executing adb to get the list of available Android Devices");
        try {
            CommandLineWrapper exec = new CommandLineWrapper();
            String[] output = exec.execute(AdbConsts.DEVICES);

            String[] adbOutput = output[0].split("\n");

            // adb devices output has 1 header line
            for (int i = 0; i < adbOutput.length; ++i)
                if (adbOutput[i].contains(this.deviceSerialNumber))
                    return i - 1;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

    /*private int reboot(){
        logger.info("Rebooting device before exploration");
        try {
            CommandLineWrapper exec = new CommandLineWrapper();
            String[] output = exec.execute(String.format(AdbConsts.REBOOT, this.deviceSerialNumber));

            return CommandLineWrapper.evalOutput(output);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

    private int unlock(){
        logger.info("Unlocking device screen");
        try {
            CommandLineWrapper exec = new CommandLineWrapper();
            String[] output = exec.execute(String.format(AdbConsts.UNLOCK, this.deviceSerialNumber));

            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

    void rebootAndUnlock(){
        int res = this.reboot();
        assert res == 0;

        try {
            logger.debug(String.format("Waiting %d seconds for reboot", AdbConsts.REBOOT_TIME));
            Thread.sleep(AdbConsts.REBOOT_TIME);
        }
        catch(InterruptedException e){
            logger.error(e.getMessage(), e);
        }

        res = this.unlock();
        assert res == 0;
    }*/
}
