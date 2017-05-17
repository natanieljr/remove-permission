package org.droidmate.analyzer.wrappers;

import com.google.common.base.Joiner;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Execute actions using the command line
 */
class CommandLineWrapper {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineWrapper.class);

    static int evalOutput(String[] output) {
        if (output[1].length() > 0) {
            logger.error(output[1]);
            return 1;
        }

        return 0;
    }

    private static String quoteIfIsPathToExecutable(String path) {
        if (SystemUtils.IS_OS_WINDOWS) {
            if (Files.isExecutable(Paths.get(path)))
                return '"' + path + '"';
            else
                return path;
        } else {
            return path;
        }
    }

    private static String quoteAbsolutePath(String s) {
        if (new File(s).isAbsolute())
            return '"' + s + '"';

        return s;
    }

    String[] execute(String... params) throws IOException {
        assert params.length >= 1 : "At least one command line parameters has to be given, denoting the executable.";

        // If the command string to be executed is a file path to an executable (as opposed to plain command e.g. "java"),
        // then it should be quoted so spaces in it are handled properly.
        params[0] = CommandLineWrapper.quoteIfIsPathToExecutable(params[0]);
        logger.info(String.format("Executing command: %s", params[0]));

        // If a parameter is an absolute path it might contain spaces in it and if yes, the parameter has to be quoted
        // to be properly interpreted.

        if (params.length > 1)
            for (int i = 1; i < params.length; ++i)
                params[i] = CommandLineWrapper.quoteAbsolutePath(params[i]);
        //String[] quotedCmdLineParamsTail = CommandLineWrapper.quoteAbsolutePaths(params.drop(1));

        // Prepare the command to execute.
        String commandLine = Joiner.on(" ").join(params);

        CommandLine command = CommandLine.parse(commandLine);

        // Prepare the process stdout and stderr listeners.
        ByteArrayOutputStream processStdoutStream = new ByteArrayOutputStream();
        ByteArrayOutputStream processStderrStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(processStdoutStream, processStderrStream);

        // Prepare the process executor.
        DefaultExecutor executor = new DefaultExecutor();

        //if (workDir != null)
        //  executor.setWorkingDirectory(workDir.toFile());

        executor.setStreamHandler(pumpStreamHandler);

        // Only exit value of 0 is allowed for the call to return successfully.
        executor.setExitValue(0);

        logger.trace("Command:");
        logger.trace(commandLine);

        Integer exitValue;
        try {
            exitValue = executor.execute(command);

        } catch (ExecuteException e) {
            throw new IOException(String.format("Failed to execute a system command.\n"
                            + "Command: %s\n"
                            + "Captured exit value: %d\n"
                            + "Captured stdout: %s\n"
                            + "Captured stderr: %s",
                    command.toString(),
                    e.getExitValue(),
                    processStdoutStream.toString().equals("") ? "" : "<stdout is empty>",
                    processStderrStream.toString().equals("") ? "" : "<stderr is empty>"),
                    e);

        } catch (IOException e) {
            throw new IOException(String.format("Failed to execute a system command.\n"
                            + "Command: %s\n"
                            + "Captured stdout: %s\n"
                            + "Captured stderr: %s", command.toString(),
                    processStdoutStream.toString().equals("") ? "" : "<stdout is empty>",
                    processStderrStream.toString().equals("") ? "" : "<stderr is empty>"),
                    e);
        } finally {
            logger.trace("Captured stdout:");
            logger.trace(processStdoutStream.toString());

            logger.trace("Captured stderr:");
            logger.trace(processStderrStream.toString());
        }
        logger.trace("Captured exit value: " + exitValue);
        logger.trace("DONE executing system command");

        return new String[]{processStdoutStream.toString(), processStderrStream.toString()};
    }
}
