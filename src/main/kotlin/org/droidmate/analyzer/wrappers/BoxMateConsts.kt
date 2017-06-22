package org.droidmate.analyzer.wrappers

/**
 * Constants used by BoxMate
 */
object BoxMateConsts {
    internal val ARGS_DIR = "-apksDir=%s"
    internal val ARGS_INLINE = "-inline"
    internal val ARGS_API23 = "-apiLevel=23"
    internal val ARGS_RESET = "-resetEvery=30"
    internal val ARGS_TIME = "-timeLimit=900"
    internal val ARGS_SEED = "-randomSeed=0"
    internal val ARGS_DEVICE_SEQ = "-device=%d"
    internal val ARGS_DEVICE_SN = "-deviceSN=%s"
    internal val ARGS_EXPL_OUTPUT_DIR = "-outputDir=%s"
    internal val ARGS_SNAP = "-getValidGuiSnapshotRetryAttempts=2"
    internal val ARGS_REPLACE_RESOURCES = "-replaceExtractedResources=false"
    internal val ARGS_LAUNCH_ACTIVITY_DELAY = "-launchActivityDelay=20000"

    val FILE_API_POLICIES = "api_policies.txt"
}
