/*
 * MIT License
 *
 * Copyright 2011-2023 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package tutorial;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.http.*;
import javax.servlet.ServletException;

import tutorial.persistence.BettingPool;
import tutorial.persistence.BettingPoolGame;


public class PoolEditorServlet extends HttpServlet {

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


    private void reportErrors( PrintWriter pw, String[] errors ) {
        pw.println( "<table width='90%' style='background-color=yellow; border-color: black; border-width: 2; border-style: solid'>" );
        pw.println( "<tr><td colspan='2'><b>Cannot open pool for betting:</b></td></tr>" );
        for (int i=0; i < errors.length; i++) {
            pw.println( "<tr><td width='5'>&nbsp;</td><td>" + errors[i] + "</td></tr>" );
        }
        pw.println( "</table>" );
    }


    String[] getValidationErrors() {
        ArrayList errorList = new ArrayList();
        BettingPoolGame game = BettingPool.getGames()[ BettingPool.getTieBreakerIndex() ];
        if (game.getAwayTeam().length() == 0 || game.getHomeTeam().length() == 0) {
            errorList.add( "Tiebreaker is not a valid game" );
        }
        BettingPoolGame[] games = BettingPool.getGames();
        for (int i = 0; i < games.length; i++) {
            if (games[i].getAwayTeam().length() == 0 && games[i].getHomeTeam().length() != 0) {
                errorList.add( "Game " + i + " has no away team" );
            } else if (games[i].getAwayTeam().length() != 0 && games[i].getHomeTeam().length() == 0) {
                errorList.add( "Game " + i + " has no home team" );
            }
        }
        String[] errors = (String[]) errorList.toArray( new String[ errorList.size() ] );
        return errors;
    }


    void updateBettingPool( HttpServletRequest request ) {
        BettingPoolGame[] games = BettingPool.getGames();
        for (int i = 0; i < games.length; i++) {
            games[i].setAwayTeam( request.getParameter( "away" + i ) );
            games[i].setHomeTeam( request.getParameter( "home" + i ) );
        }
        BettingPool.setTieBreakerIndex( getTieBreakerIndex( request ) );
    }


    private int getTieBreakerIndex( HttpServletRequest request ) {
        try {
            return Integer.parseInt( request.getParameter( "tiebreaker" ) );
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    protected void doGet( HttpServletRequest request, HttpServletResponse response )
            throws ServletException, IOException {
        response.setContentType( "text/html" );
        PrintWriter pw = response.getWriter();

        pw.println( "<html><head></head><body>" );
        printBody( pw );
        pw.println( "</body></html>" );
    }


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

    private String getReadOnlyFlag() {
        return BettingPool.isEditable() ? "" : " readonly";
    }

}
