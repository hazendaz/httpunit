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

/**
 * The Class WebSite.
 */
public class WebSite extends Site {

    /** The group id. */
    private int _groupId;


    /**
     * Main.
     *
     * @param args the args
     */
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



    /**
     * Generate.
     *
     * @param siteFile the site file
     * @param directory the directory
     * @throws SAXException the sax exception
     * @throws IOException the io exception
     */
    public static void generate( File siteFile, File directory ) throws SAXException, IOException {
        generate( siteFile, new FilePageGenerator( directory ) );
    }


    /**
     * Generate.
     *
     * @param siteFile the site file
     * @param generator the generator
     * @throws SAXException the sax exception
     * @throws IOException the io exception
     */
    public static void generate( File siteFile, PageGenerator generator ) throws SAXException, IOException {
        Site site = new WebSite();
        BasicSiteTemplate template = new SourceForgeSiteTemplate();
        generate( site, siteFile, template, generator );
    }



    /**
     * Print usage.
     */
    private static void printUsage() {
        System.out.println( "Usage: " + WebSite.class.getName() + " <target-directory>" );
    }


    /**
     * Create page.
     *
     * @return the web page
     */
    public WebPage createPage() {
        return (WebPage) addedSiteElement( new SourceForgeWebPage() );
    }


    /**
     * Create project page.
     *
     * @return the project page
     */
    public ProjectPage createProjectPage() {
        return (ProjectPage) addedSiteElement( new ProjectPage() );
    }


    /**
     * Sets the project.
     *
     * @param project the project
     */
    public void setProject( String project ) {
        setSiteName( project );
    }


    /**
     * Gets the bug entry url.
     *
     * @return the bug entry url
     */
    private String getBugEntryURL() {
        return getTrackerURL( 100000 );
    }


    /**
     * Gets the feature entry url.
     *
     * @return the feature entry url
     */
    private String getFeatureEntryURL() {
        return getTrackerURL( 350000 );
    }


    /**
     * Get tracker url.
     *
     * @param trackerType the tracker type
     * @return the string
     */
    private String getTrackerURL( int trackerType ) {
        return "http://sourceforge.net/tracker/?group_id=" + _groupId + "&atid=" + (trackerType + _groupId);
    }


    /**
     * Gets the group id.
     *
     * @return the group id
     */
    public int getGroupId() {
        return _groupId;
    }


    /**
     * Sets the group id.
     *
     * @param groupId the group id
     */
    public void setGroupId( int groupId ) {
        _groupId = groupId;
    }


    /**
     * Append source forge hosting notice.
     *
     * @param sb the sb
     */
    public void appendSourceForgeHostingNotice( StringBuffer sb ) {
        if (_groupId != 0) {
            sb.append( "  <div class='sourceforge'>Hosted by <IMG SRC='http://sourceforge.net/sflogo.php?group_id=" );
            sb.append( _groupId ).append( "&type=1' width='88' height='31' border='0' alt='SourceForge Logo' " );
            sb.append( "naturalsizeflag='0' align='TOP'></div>" );
            sb.append( "</div>" ).append( FragmentTemplate.LINE_BREAK );
        }
    }


    /**
     * The Class ProjectPage.
     */
    public class ProjectPage extends ExternalPage {

        /**
         * Sets the type.
         *
         * @param type the type
         */
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


        /**
         * Sets the location.
         *
         * @param location the location
         */
        public void setLocation( String location ) {}

    }
}
