package org.droidmate.analyzer

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException

/**
 * Entry point to generate results for the paper
 */
object ResultsMain {
    @JvmStatic fun main(args: Array<String>) {
        val cfg = Configuration()
        val jc = JCommander(cfg)
        try {
            jc.parse(*args)
        } catch (e: ParameterException) {
            System.err.println(e.message)
            jc.usage()
            System.exit(1)
        }

        val proc = ResultsGenerator(cfg)
        proc.generate()
    }
}
