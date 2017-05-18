package org.droidmate.analyzer;


/**
 * Application's main class
 */
public class Main {
    public static void main(String[] args) {
        Configuration cfg = new Configuration();
        BatchProcessor proc = new BatchProcessor(cfg);
        proc.analyze();
    }
}
