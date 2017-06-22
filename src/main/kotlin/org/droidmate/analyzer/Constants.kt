package org.droidmate.analyzer

import java.nio.file.Paths

/**
 * Consts used by the application
 */
class Constants private constructor() {
    companion object {
        val EMPTY_PATH = Paths.get("EMPTY")!!
        val EMPTY_DEVICE_SN = "0"
    }
}