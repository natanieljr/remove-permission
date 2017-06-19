package org.droidmate.analyzer

import org.droidmate.analyzer.api.Api
import org.droidmate.analyzer.api.IApi
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Manages data read from resource files
 */
class ResourceManager {

    private fun processLine(line: String) {
        logger.trace("Processing line %s", line)
        var classAndMethodNameStr = line
        var uri = ""

        // contains URI
        if (line.contains("\t")) {
            val data = line.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            classAndMethodNameStr = data[0]
            uri = data[1]
        }

        val classAndMethodName = classAndMethodNameStr
                .split("->".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val className = classAndMethodName[0]
        val methodSignature = classAndMethodName[1]
        val params = Api.getParamsFromMethodSignature(methodSignature)
        val methodName = Api.getMethodNameFromSignature(methodSignature)

        val api = Api.build(className, methodName, params, uri)

        ResourceManager.restrictableApis!!.add(api)
    }

    private fun loadResourceFile(fileName: String): Path? {
        val classLoader = javaClass.classLoader
        try {
            val resource = classLoader.getResource(fileName)

            if (resource != null)
                return Paths.get(resource.toURI())
        } catch (e: Exception) {
            logger.error(e.message, e)
        }

        return null
    }

    private fun initializeApiMapping() {
        ResourceManager.restrictableApis = ArrayList<IApi>()

        try {
            val file = this.loadResourceFile("api_restrictions.txt")!!
            val restrictions = Files.readAllLines(file)

            restrictions.forEach { p -> this.processLine(p) }

        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(ResourceManager.restrictableApis!!.size > 0)
    }

    fun getRestriction(api: IApi): IApi? {
        if (ResourceManager.restrictableApis == null)
            this.initializeApiMapping()

        if (restrictableApis!!.contains(api))
            return restrictableApis!![restrictableApis!!.indexOf(api)]

        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResourceManager::class.java)
        private var restrictableApis: MutableList<IApi>? = null
    }
}
