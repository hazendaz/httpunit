/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package com.meterware.httpunit.site;

import com.meterware.website.*;

import java.io.File;
import java.io.IOException;

import org.xml.sax.SAXException;

public class WebSite extends Site {

    private int _groupId;


    public static void main( String[] args ) {
        try {
            if (args.length == 0) {
                printUsage();
            } else {
                FragmentTemplate.registerTemplate( new Faq() );
                FragmentTemplate.registerTemplate( new News() );
                FragmentTemplate.registerTemplate( new Citations() );
                FragmentTemplate.registerTemplate( new Developers() );
                generate( new File( "sitedocs/site.xml" ), new File( args[0] ) );
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }



    public static void generate( File siteFile, File directory ) throws SAXException, IOException {
        generate( siteFile, new FilePageGenerator( directory ) );
    }


    public static void generate( File siteFile, PageGenerator generator ) throws SAXException, IOException {
        Site site = new WebSite();
        BasicSiteTemplate template = new SourceForgeSiteTemplate();
        generate( site, siteFile, template, generator );
    }



    private static void printUsage() {
        System.out.println( "Usage: " + WebSite.class.getName() + " <target-directory>" );
    }


    public WebPage createPage() {
        return (WebPage) addedSiteElement( new SourceForgeWebPage() );
    }


    public ProjectPage createProjectPage() {
        return (ProjectPage) addedSiteElement( new ProjectPage() );
    }


    public void setProject( String project ) {
        setSiteName( project );
    }


    private String getBugEntryURL() {
        return getTrackerURL( 100000 );
    }


    private String getFeatureEntryURL() {
        return getTrackerURL( 350000 );
    }


    private String getTrackerURL( int trackerType ) {
        return "http://sourceforge.net/tracker/?group_id=" + _groupId + "&atid=" + (trackerType + _groupId);
    }


    public int getGroupId() {
        return _groupId;
    }


    public void setGroupId( int groupId ) {
        _groupId = groupId;
    }


    public void appendSourceForgeHostingNotice( StringBuffer sb ) {
        if (_groupId != 0) {
            sb.append( "  <div class='sourceforge'>Hosted by <IMG SRC='http://sourceforge.net/sflogo.php?group_id=" );
            sb.append( _groupId ).append( "&type=1' width='88' height='31' border='0' alt='SourceForge Logo' " );
            sb.append( "naturalsizeflag='0' align='TOP'></div>" );
            sb.append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
        }
    }


    public class ProjectPage extends ExternalPage {

        public void setType( String type ) {
            if (type.equalsIgnoreCase( "bugs" )) {
                super.setLocation( getBugEntryURL() );
            } else if (type.equalsIgnoreCase( "enhancements" )) {
                super.setLocation( getFeatureEntryURL() );
            } else if (type.equalsIgnoreCase( "donations" )) {
                // xxx link from image: http://images.sourceforge.net/images/project-support.jpg
                super.setLocation( "http://sourceforge.net/project/project_donations.php?group_id=" + _groupId );
            } else if (type.equalsIgnoreCase( "cvs" )) {
                super.setLocation( "http://sourceforge.net/cvs/?group_id=" + _groupId );
            } else if (type.equalsIgnoreCase( "svn" )) {
                super.setLocation( "http://sourceforge.net/svn/?group_id=" + _groupId );
            }
        }


        public void setLocation( String location ) {}

    }
}
