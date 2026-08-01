/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.site;

import com.meterware.website.FragmentTemplate;

import java.util.ArrayList;

/**
 * The Class Citations.
 */
public class Citations extends FragmentTemplate {

    /** The sections. */
    private ArrayList _sections = new ArrayList<>();

    /**
     * New fragment.
     *
     * @return the fragment template
     */
    public FragmentTemplate newFragment() {
        return new Citations();
    }


    /**
     * As text.
     *
     * @return the string
     */
    public String asText() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _sections.size(); i++) {
            Section section = (Section) _sections.get( i );
            section.appendTo( sb );
        }
        return sb.toString();
    }


    /**
     * Gets the root node name.
     *
     * @return the root node name
     */
    protected String getRootNodeName() {
        return "citations";
    }


    /**
     * Create section.
     *
     * @return the section
     */
    public Section createSection() {
        Section section = new Section();
        _sections.add( section );
        return section;
    }


    /**
     * The Class Section.
     */
    public class Section {
        /** The citations. */
        private ArrayList _citations = new ArrayList<>();
        /** The title. */
        private String _title;


        /**
         * Sets the title.
         *
         * @param title the title
         */
        public void setTitle( String title ) {
            _title = title;
        }


        /**
         * Create citation.
         *
         * @return the citation
         */
        public Citation createCitation() {
            Citation citation = new Citation();
            _citations.add( citation );
            return citation;
        }


        /**
         * Append to.
         *
         * @param sb the sb
         */
        public void appendTo( StringBuffer sb ) {
            sb.append( "<h2>" ).append( _title ).append( "</h2>" ).append( LINE_BREAK );
            sb.append( "  <ul>" ).append( LINE_BREAK );
            for (int i = 0; i < _citations.size(); i++) {
                Citation citation = (Citation) _citations.get( i );
                citation.appendTo( sb );
            }
            sb.append( "  </ul>" ).append( LINE_BREAK );
        }

    }


    /**
     * The Class Citation.
     */
    public class Citation {

        /** The url. */
        private String _url;
        /** The name. */
        private String _name;
        /** The text. */
        private String _text;

        /**
         * Sets the url.
         *
         * @param url the url
         */
        public void setUrl( String url ) {
            _url = url;
        }


        /**
         * Sets the name.
         *
         * @param name the name
         */
        public void setName( String name ) {
            _name = name;
        }


        /**
         * Sets the text.
         *
         * @param text the text
         */
        public void setText( String text ) {
            _text = text;
        }


        /**
         * Append to.
         *
         * @param sb the sb
         */
        public void appendTo( StringBuffer sb ) {
            sb.append( "    <li><a href='" ).append( _url ).append( "'>" ).append( _name ).append( "</a><br/>").append( LINE_BREAK );
            sb.append( "    " ).append( _text ).append( LINE_BREAK );
        }

    }
}
