/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package tutorial;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import tutorial.persistence.BettingPool;
import tutorial.persistence.BettingPoolGame;


/**
 * The Class PoolEditorServlet.
 */
public class PoolEditorServlet extends HttpServlet {

    /**
     * Do post.
     *
     * @param request the request
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException the io exception
     */
    protected void doPost( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        updateBettingPool( request );
        response.setContentType( "text/html" );
        PrintWriter pw = response.getWriter();

        pw.println( "<html><head></head><body>" );
        if (request.getParameter( "save" ).equals( "Open Pool" )) {
            String[] errors = getValidationErrors();
            if (errors.length != 0) reportErrors( pw, errors );
            else {
                BettingPool.openPool();
            }
        }
        printBody( pw );
        pw.println( "</body></html>" );
    }


    /**
     * Report errors.
     *
     * @param pw the pw
     * @param errors the errors
     */
    private void reportErrors( PrintWriter pw, String[] errors ) {
        pw.println( "<table width='90%' style='background-color=yellow; border-color: black; border-width: 2; border-style: solid'>" );
        pw.println( "<tr><td colspan='2'><b>Cannot open pool for betting:</b></td></tr>" );
        for (int i=0; i < errors.length; i++) {
            pw.println( "<tr><td width='5'>&nbsp;</td><td>" + errors[i] + "</td></tr>" );
        }
        pw.println( "</table>" );
    }


    /**
     * Gets the validation errors.
     *
     * @return the validation errors
     */
    String[] getValidationErrors() {
        ArrayList errorList = new ArrayList<>();
        BettingPoolGame game = BettingPool.getGames()[ BettingPool.getTieBreakerIndex() ];
        if (game.getAwayTeam().isEmpty() || game.getHomeTeam().isEmpty()) {
            errorList.add( "Tiebreaker is not a valid game" );
        }
        BettingPoolGame[] games = BettingPool.getGames();
        for (int i = 0; i < games.length; i++) {
            if (games[i].getAwayTeam().isEmpty() && games[i].getHomeTeam().length() != 0) {
                errorList.add( "Game " + i + " has no away team" );
            } else if (games[i].getAwayTeam().length() != 0 && games[i].getHomeTeam().isEmpty()) {
                errorList.add( "Game " + i + " has no home team" );
            }
        }
        String[] errors = (String[]) errorList.toArray( new String[ errorList.size() ] );
        return errors;
    }


    /**
     * Update betting pool.
     *
     * @param request the request
     */
    void updateBettingPool( HttpServletRequest request ) {
        BettingPoolGame[] games = BettingPool.getGames();
        for (int i = 0; i < games.length; i++) {
            games[i].setAwayTeam( request.getParameter( "away" + i ) );
            games[i].setHomeTeam( request.getParameter( "home" + i ) );
        }
        BettingPool.setTieBreakerIndex( getTieBreakerIndex( request ) );
    }


    /**
     * Get tie breaker index.
     *
     * @param request the request
     * @return the int
     */
    private int getTieBreakerIndex( HttpServletRequest request ) {
        try {
            return Integer.parseInt( request.getParameter( "tiebreaker" ) );
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    /**
     * Do get.
     *
     * @param request the request
     * @param response the response
     * @throws ServletException the servlet exception
     * @throws IOException the io exception
     */
    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        response.setContentType( "text/html" );
        PrintWriter pw = response.getWriter();

        pw.println( "<html><head></head><body>" );
        printBody( pw );
        pw.println( "</body></html>" );
    }


    /**
     * Print body.
     *
     * @param pw the pw
     */
    private void printBody( PrintWriter pw ) {
        pw.println( "<form id='pool' method='POST'>" );
        pw.println( "<table>" );
        pw.println( "<tr><th>Home Team</th><th>Away Team</th><th>Tiebreaker?</th></tr>" );

        BettingPoolGame[] games = BettingPool.getGames();
        for (int i = 0; i < games.length; i++) {
            pw.println( "<tr><td><input name='home" + i + "' value='" + games[i].getHomeTeam() + "'" + getReadOnlyFlag() + "></td>" );
            pw.println( "<td><input name='away" + i + "' value='" + games[i].getAwayTeam() + "'" + getReadOnlyFlag() + "></td>" );
            pw.print( "<td><input type='radio' name='tiebreaker' value='" + i + "'" + getReadOnlyFlag() );
            if (i == BettingPool.getTieBreakerIndex()) pw.print( " checked" );
            pw.println( " /></td></tr>" );
        }
        pw.println( "</table>" );
        if (BettingPool.isEditable()) {
            pw.println( "<input type='submit' name='save' value='Save' />" );
            pw.println( "<input type='submit' name='save' value='Open Pool' />" );
        }
        pw.println( "</form>" );
    }

    /**
     * Gets the read only flag.
     *
     * @return the read only flag
     */
    private String getReadOnlyFlag() {
        return BettingPool.isEditable() ? "" : " readonly";
    }

}
