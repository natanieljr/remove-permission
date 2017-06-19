package org.droidmate.analyzer.api

/**
 * API identified during exploration or loaded from configuration file
 */
interface IApi {
    val uri: String

    fun hasRestriction(): Boolean

    val restriction: IApi?

    val uriParamName: String
}
