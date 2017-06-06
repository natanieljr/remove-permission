package org.droidmate.analyzer.wrappers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Constants used by BoxMate
 */
public class BoxMateConsts {
    static final String ARGS_DIR = "-apksDir=%s";
    static final String ARGS_INLINE = "-inline";
    static final String ARGS_API23 = "-apiLevel=23";
    static final String ARGS_RESET = "-resetEvery=30";
    static final String ARGS_TIME = "-timeLimit=900";
    static final String ARGS_SEED = "-randomSeed=0";
    static final String ARGS_DEVICE = "-device=%d";
    static final String ARGS_EXPL_OUTPUT_DIR = "-outputDir=%s";
    static final String ARGS_SNAP = "-getValidGuiSnapshotRetryAttempts=2";
    static final String ARGS_REPLACE_RESOURCES = "-replaceExtractedResources=false";
    static final String ARGS_UNPACK = "-unpack";

    public static final String FILE_API_POLICIES = "api_policies.txt";
}
