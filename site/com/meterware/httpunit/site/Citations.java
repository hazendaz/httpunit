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

public class Citations extends FragmentTemplate {

    private ArrayList _sections = new ArrayList<>();

    public FragmentTemplate newFragment() {
        return new Citations();
    }


    public String asText() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _sections.size(); i++) {
            Section section = (Section) _sections.get( i );
            section.appendTo( sb );
        }
        return sb.toString();
    }


    protected String getRootNodeName() {
        return "citations";
    }


    public Section createSection() {
        Section section = new Section();
        _sections.add( section );
        return section;
    }


    public class Section {
        private ArrayList _citations = new ArrayList<>();
        private String _title;


        public void setTitle( String title ) {
            _title = title;
        }


        public Citation createCitation() {
            Citation citation = new Citation();
            _citations.add( citation );
            return citation;
        }


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


    public class Citation {

        private String _url;
        private String _name;
        private String _text;

        public void setUrl( String url ) {
            _url = url;
        }


        public void setName( String name ) {
            _name = name;
        }


        public void setText( String text ) {
            _text = text;
        }


        public void appendTo( StringBuffer sb ) {
            sb.append( "    <li><a href='" ).append( _url ).append( "'>" ).append( _name ).append( "</a><br/>").append( LINE_BREAK );
            sb.append( "    " ).append( _text ).append( LINE_BREAK );
        }

    }
}
