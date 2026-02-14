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

abstract class UrlPatternMatcher {

    static UrlPatternMatcher[] _templates = { new ExtensionUrlPatternMatcher(), new PathMappingUrlPatternMatcher() };

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
     */
    abstract UrlPatternMatcher create(String pattern);

    /**
     * Returns true if the specified resource matches this pattern.
     */
    abstract boolean matchesResourceName(String resourceName);
}

class ExactUrlPatternMatcher extends UrlPatternMatcher {
    private String _pattern;

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

class ExtensionUrlPatternMatcher extends UrlPatternMatcher {
    private String _suffix;

    ExtensionUrlPatternMatcher() {
    }

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

class PathMappingUrlPatternMatcher extends UrlPatternMatcher {
    private String _exactPath;
    private String _prefix;

    PathMappingUrlPatternMatcher() {
    }

    PathMappingUrlPatternMatcher(String exactPath) {
        _exactPath = exactPath;
        _prefix = exactPath + '/';
    }

    @Override
    UrlPatternMatcher create(String pattern) {
        return !handlesPattern(pattern) ? null
                : new PathMappingUrlPatternMatcher(pattern.substring(0, pattern.length() - 2));
    }

    private boolean handlesPattern(String pattern) {
        return pattern.startsWith("/") && pattern.endsWith("/*");
    }

    @Override
    boolean matchesResourceName(String resourceName) {
        return resourceName.startsWith(_prefix) || resourceName.equals(_exactPath);
    }
}
