package org.droidmate.analyzer.wrappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Wrapper to the ADB command line tool
 */
class AdbWrapper {
    private static final Logger logger = LoggerFactory.getLogger(AdbWrapper.class);

    private int reboot(){
        logger.info("Rebooting device before exploration");
        try {
            CommandLineWrapper exec = new CommandLineWrapper();
            String[] output = exec.execute(AdbConsts.REBOOT);

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
            String[] output = exec.execute(AdbConsts.UNLOCK);

            return CommandLineWrapper.evalOutput(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return -1;
    }

    public void rebootAndUnlock(){
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
    }
}
