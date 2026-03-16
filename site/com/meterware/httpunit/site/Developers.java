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

public class Developers extends FragmentTemplate {

    private ArrayList _groups = new ArrayList<>();

    public FragmentTemplate newFragment() {
        return new Developers();
    }


    public String asText() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < _groups.size(); i++) {
            Group group = (Group) _groups.get( i );
            group.appendTo( sb );
        }
        return sb.toString();
    }


    protected String getRootNodeName() {
        return "developers";
    }


    public Group createGroup() {
        Group group = new Group();
        _groups.add( group );
        return group;
    }


    public class Group {

        private ArrayList _developers = new ArrayList<>();
        private ArrayList _summaries = new ArrayList<>();
        private String _type;
        private static final int MAX_SUMMARY_COLUMNS = 6;


        public void setType( String type ) {
            _type = type;
        }


        public Developer createDeveloper() {
            Developer developer = new Developer();
            _developers.add( developer );
            return developer;
        }


        void appendTo( StringBuffer sb ) {
            sb.append( "<h2>" ).append( _type ).append( "</h2>" ).append( LINE_BREAK );
            sb.append( "<dl class='developers'>" ).append( LINE_BREAK );
            for (int i = 0; i < _developers.size(); i++) {
                Developer developer = (Developer) _developers.get( i );
                if (developer.isNameOnly()) {
                    _summaries.add( developer );
                } else {
                    developer.appendTo( sb );
                }
            }
            sb.append( "</dl>" ).append( LINE_BREAK );
            if (_summaries.size() != 0) appendSummary( sb );
        }


        private void appendSummary( StringBuffer sb ) {
            sb.append( "<table>" ).append( LINE_BREAK );
            int numColumns = Math.min( _summaries.size(), MAX_SUMMARY_COLUMNS );
            int numRows = (_summaries.size() + numColumns - 1) / numColumns;

            for (int j = 0; j < numRows; j++) {
                sb.append( "<tr>" );
                for (int k = 0; k < numColumns; k++) {
                    int i = j + k*numRows;
                    if (i >= _summaries.size()) continue;
                    sb.append( "<td class='summaries'>");
                    Developer developer = (Developer) _summaries.get(i);
                    developer.appendNameTo( sb );
                    sb.append( "</td>" );
                }
                sb.append( "</tr>" ).append( LINE_BREAK );
            }
            sb.append( "</table>" ).append( LINE_BREAK );
        }
    }


    public class Developer {

        private String _name;
        private String _username;
        private String _email;
        private String _text;

        public void setName( String name ) {
            _name = name;
        }


        public void setUsername( String username ) {
            _username = username;
        }


        public void setEmail( String email ) {
            _email = email;
        }


        public void setText( String text ) {
            _text = text;
        }


        boolean isNameOnly() {
            return _text == null;
        }


        void appendTo( StringBuffer sb ) {
            sb.append( "<dt>" );
            appendNameTo( sb );
            sb.append( "</dt>" ).append( LINE_BREAK );
            if (_text != null) sb.append( "<dd>" ).append( _text ).append( "</dd>" ).append( LINE_BREAK );
        }


        void appendNameTo( StringBuffer sb ) {
            if (_username == null /* && _email == null */) {
                sb.append( _name );
            } else {
                sb.append( "<a" );
                if (_username != null) sb.append( " name='" ).append( _username ).append( "'" );
                if (_email != null) sb.append( " href='mailto:" ).append( _email ).append( "'" );
                sb.append( ">" ).append( _name ).append( "</a>" );
            }
        }
    }
}
