package org.droidmate.analyzer.api

import org.droidmate.analyzer.ResourceManager
import org.slf4j.LoggerFactory
import java.util.*

/**
 * API identified during exploration
 */
class Api private constructor(private val className: String, private val methodName: String,
                              private val params: List<String>, override val uri: String) : IApi {
    override fun hasRestriction(): Boolean {
        return this.restriction != null
    }

    override val restriction: IApi?
        get() {
            val restriction = ResourceManager().getRestriction(this)
            logger.debug(String.format("(%s) => getRestriction: %s", this.toString(), (restriction == null).toString() + ""))

            return restriction
        }

    override val uriParamName: String
        get() {
            for (i in this.params.indices) {
                val param = this.params[i]
                if (param == "android.net.Uri")
                    return String.format("p%d", i)
            }

            return ""
        }

    override fun toString(): String {
        val paramStr = this.params.joinToString(",")
        return String.format("%s->%s(%s)\t%s", this.className, this.methodName, paramStr, this.uri)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Api)
            return false

        // Can use the toString method comparison because it generates a unique signature for each API
        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        var result = className.hashCode()
        result = 31 * result + methodName.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + uri.hashCode()
        return result
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Api::class.java)

        fun build(className: String, methodName: String, paramStr: String, uri: String): IApi {
            val paramList = ArrayList<String>()
            val internalParamStr = paramStr.replace("(", "").replace(")", "")

            if (internalParamStr.contains(",")) {
                val paramArr = internalParamStr.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                paramList.addAll(Arrays.asList(*paramArr))
            } else if (internalParamStr.isNotEmpty()) {
                paramList.add(internalParamStr)
            }

            return Api.build(className, methodName, paramList, uri)
        }

        fun build(className: String, methodName: String, params: List<String>, uri: String): IApi {
            return Api(className, methodName, params, uri)
        }

        fun getParamsFromMethodSignature(methodName: String): String {
            try {
                val pattern: String
                if (methodName.contains("("))
                    pattern = "\\("
                else
                    pattern = "<"
                val data = methodName.split(pattern.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                if (data.isNotEmpty())
                    return data[1].replace(")", "")
                else
                    return ""
            } catch (e: Exception) {
                logger.error(methodName)
                logger.error(e.message, e)
            }

            return ""
        }

        fun getMethodNameFromSignature(methodSignature: String): String {
            val params = Api.getParamsFromMethodSignature(methodSignature)

            return methodSignature
                    .replace(params, "")
                    .replace("(", "")
                    .replace(")", "")
        }
    }
}
