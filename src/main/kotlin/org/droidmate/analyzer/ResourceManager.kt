package org.droidmate.analyzer

import org.droidmate.analyzer.api.Api
import org.droidmate.analyzer.api.IApi
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.collections.ArrayList

/**
 * Manages data read from resource files
 */
class ResourceManager {

    private fun processLine(line: String) : IApi {
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

        return api
    }

    internal fun loadResourceFile(fileName: String): Path {
        val classLoader = javaClass.classLoader
        try {
            val resource = classLoader.getResource(fileName)!!

            return Paths.get(resource.toURI())
        } catch (e: Exception) {
            logger.error(e.message, e)
            throw UnsupportedOperationException(e.message)
        }
    }

    internal fun initializeApiMapping(apiRestrictions : String = "api_restrictions.txt"): List<IApi> {
        val apiList = ArrayList<IApi>()

        try {
            val file = this.loadResourceFile(apiRestrictions)
            val restrictions = Files.readAllLines(file)

            restrictions.forEach { p -> if (!p.startsWith("#")) apiList.add(this.processLine(p)) }

        } catch (e: IOException) {
            logger.error(e.message, e)
        }

        assert(apiList.isNotEmpty())

        return apiList
    }

    fun getRestriction(api: IApi): IApi? {
        if (ResourceManager.restrictableApis.isEmpty())
            ResourceManager.restrictableApis = this.initializeApiMapping()

        if (restrictableApis.contains(api))
            return restrictableApis[restrictableApis.indexOf(api)]

        return null
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ResourceManager::class.java)
        private var restrictableApis: List<IApi> = ArrayList()
    }
}
