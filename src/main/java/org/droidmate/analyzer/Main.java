package org.droidmate.analyzer;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Application's main class
 */
public class Main {
    public static void main(String[] args) {
        Configuration cfg = new Configuration();
        JCommander jc = new JCommander(cfg);
        //jc.setDefaultProvider(DEFAULT_PROVIDER);
        try {
            jc.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            jc.usage();
            System.exit(1);
        }

        BatchProcessor proc = new BatchProcessor(cfg);
        proc.analyze();
    }
}
