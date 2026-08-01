/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.servletunit;

import java.util.ArrayList;

/**
 * The Class FilterUrlMap.
 */
class FilterUrlMap {

    /** The url patterns. */
    private ArrayList _urlPatterns = new ArrayList<>();

    /** The filters. */
    private ArrayList _filters = new ArrayList<>();

    /**
     * Put.
     *
     * @param urlPattern
     *            the url pattern
     * @param metaData
     *            the meta data
     */
    void put(String urlPattern, FilterMetaData metaData) {
        _urlPatterns.add(UrlPatternMatcher.newPatternMatcher(urlPattern));
        _filters.add(metaData);
    }

    /**
     * Gets the matching filters.
     *
     * @param resourceName
     *            the resource name
     *
     * @return the matching filters
     */
    FilterMetaData[] getMatchingFilters(String resourceName) {
        ArrayList matches = new ArrayList<>();
        for (int i = 0; i < _urlPatterns.size(); i++) {
            UrlPatternMatcher urlPattern = (UrlPatternMatcher) _urlPatterns.get(i);
            if (urlPattern.matchesResourceName(resourceName)) {
                matches.add(_filters.get(i));
            }
        }
        return (FilterMetaData[]) matches.toArray(new FilterMetaData[matches.size()]);
    }

}

/**
 * The Class UrlPatternMatcher.
 */
abstract class UrlPatternMatcher {

    /** The templates. */
    static UrlPatternMatcher[] _templates = { new ExtensionUrlPatternMatcher(), new PathMappingUrlPatternMatcher() };

    /**
     * New pattern matcher.
     *
     * @param pattern
     *            the pattern
     *
     * @return the url pattern matcher
     */
    static UrlPatternMatcher newPatternMatcher(String pattern) {
        for (UrlPatternMatcher _template : _templates) {
            UrlPatternMatcher matcher = _template.create(pattern);
            if (matcher != null) {
                return matcher;
            }
        }
        return new ExactUrlPatternMatcher(pattern);
    }

    /**
     * Returns a suitable pattern matcher if this class is compatible with the pattern. Will return null otherwise.
     *
     * @param pattern
     *            the pattern
     *
     * @return the url pattern matcher
     */
    abstract UrlPatternMatcher create(String pattern);

    /**
     * Returns true if the specified resource matches this pattern.
     *
     * @param resourceName
     *            the resource name
     *
     * @return true, if successful
     */
    abstract boolean matchesResourceName(String resourceName);
}

/**
 * The Class ExactUrlPatternMatcher.
 */
class ExactUrlPatternMatcher extends UrlPatternMatcher {
    /** The pattern. */
    private String _pattern;

    /**
     * Instantiates a new exact url pattern matcher.
     *
     * @param pattern
     *            the pattern
     */
    public ExactUrlPatternMatcher(String pattern) {
        _pattern = pattern;
    }

    @Override
    UrlPatternMatcher create(String pattern) {
        return new ExactUrlPatternMatcher(pattern);
    }

    @Override
    boolean matchesResourceName(String resourceName) {
        return _pattern.equals(resourceName);
    }
}

/**
 * The Class ExtensionUrlPatternMatcher.
 */
class ExtensionUrlPatternMatcher extends UrlPatternMatcher {
    /** The suffix. */
    private String _suffix;

    /**
     * Instantiates a new extension url pattern matcher.
     */
    ExtensionUrlPatternMatcher() {
    }

    /**
     * Instantiates a new extension url pattern matcher.
     *
     * @param suffix
     *            the suffix
     */
    ExtensionUrlPatternMatcher(String suffix) {
        _suffix = suffix;
    }

    @Override
    UrlPatternMatcher create(String pattern) {
        return !pattern.startsWith("*.") ? null : new ExtensionUrlPatternMatcher(pattern.substring(1));
    }

    @Override
    boolean matchesResourceName(String resourceName) {
        return resourceName.endsWith(_suffix);
    }
}

/**
 * The Class PathMappingUrlPatternMatcher.
 */
class PathMappingUrlPatternMatcher extends UrlPatternMatcher {
    /** The exact path. */
    private String _exactPath;
    /** The prefix. */
    private String _prefix;

    /**
     * Instantiates a new path mapping url pattern matcher.
     */
    PathMappingUrlPatternMatcher() {
    }

    /**
     * Instantiates a new path mapping url pattern matcher.
     *
     * @param exactPath
     *            the exact path
     */
    PathMappingUrlPatternMatcher(String exactPath) {
        _exactPath = exactPath;
        _prefix = exactPath + '/';
    }

    @Override
    UrlPatternMatcher create(String pattern) {
        return !handlesPattern(pattern) ? null
                : new PathMappingUrlPatternMatcher(pattern.substring(0, pattern.length() - 2));
    }

    /**
     * Handles pattern.
     *
     * @param pattern
     *            the pattern
     *
     * @return true, if successful
     */
    private boolean handlesPattern(String pattern) {
        return pattern.startsWith("/") && pattern.endsWith("/*");
    }

    @Override
    boolean matchesResourceName(String resourceName) {
        return resourceName.startsWith(_prefix) || resourceName.equals(_exactPath);
    }
}
