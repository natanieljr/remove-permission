package org.droidmate.analyzer.api;

/**
 * API identified during exploration or loaded from configuration file
 */
public interface IApi {
    String getURI();

    boolean hasRestriction();

    IApi getRestriction();

    String getURIParamName();
}
