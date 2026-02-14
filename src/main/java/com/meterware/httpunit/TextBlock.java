/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit;

import java.net.URL;
import java.util.ArrayList;

import org.w3c.dom.Node;

/**
 * A class which represents a block of text in a web page. Experimental.
 **/
public class TextBlock extends BlockElement {

    /** The lists. */
    private ArrayList _lists = new ArrayList<>();
    /** Predicate to match part or all of a block's class attribute. **/
    public static final HTMLElementPredicate MATCH_CLASS;
    /** Predicate to match the tag associated with a block (case insensitive). **/
    public static final HTMLElementPredicate MATCH_TAG;

    /**
     * Instantiates a new text block.
     *
     * @param response
     *            the response
     * @param frame
     *            the frame
     * @param baseURL
     *            the base URL
     * @param baseTarget
     *            the base target
     * @param rootNode
     *            the root node
     * @param characterSet
     *            the character set
     */
    public TextBlock(WebResponse response, FrameSelector frame, URL baseURL, String baseTarget, Node rootNode,
            String characterSet) {
        super(response, frame, baseURL, baseTarget, rootNode, characterSet);
    }

    /**
     * Returns any lists embedded in this text block.
     *
     * @return the lists
     */
    public WebList[] getLists() {
        return (WebList[]) _lists.toArray(new WebList[_lists.size()]);
    }

    /**
     * Adds the list.
     *
     * @param webList
     *            the web list
     */
    void addList(WebList webList) {
        _lists.add(webList);
    }

    /**
     * Gets the formats.
     *
     * @param characterPosition
     *            the character position
     *
     * @return the formats
     */
    String[] getFormats(int characterPosition) {
        return null;
    }

    static {
        MATCH_CLASS = (htmlElement, criteria) -> {
            if (criteria == null) {
                criteria = "";
            }
            return ((BlockElement) htmlElement).getClassName().equalsIgnoreCase(criteria.toString());
        };

        MATCH_TAG = (htmlElement, criteria) -> {
            if (criteria == null) {
                criteria = "";
            }
            return criteria.toString().equalsIgnoreCase(((BlockElement) htmlElement).getTagName());
        };
    }
}
