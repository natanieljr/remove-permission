package org.droidmate.analyzer

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

/**
 * Application's main class
 */
object Main {
    @JvmStatic fun main(args: Array<String>) {
        val cfg = Configuration()
        val jc = JCommander(cfg)
        //jc.setDefaultProvider(DEFAULT_PROVIDER);
        try {
            jc.parse(*args)
        } catch (e: ParameterException) {
            System.err.println(e.message)
            jc.usage()
            System.exit(1)
        }

        val proc = BatchProcessor(cfg)
        proc.analyze()
    }
}
