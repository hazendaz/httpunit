/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package tutorial.persistence;


/**
 * The Class BettingPoolGame.
 */
public class BettingPoolGame {

    /**
     * Instantiates a new betting pool game.
     */
    BettingPoolGame() {
        _homeTeam = _awayTeam = "";
    }


    /**
     * Gets the home team.
     *
     * @return the home team
     */
    public String getHomeTeam() {
        return _homeTeam;
    }


    /**
     * Sets the home team.
     *
     * @param homeTeam the home team
     */
    public void setHomeTeam( String homeTeam ) {
        if (!BettingPool.isEditable()) throw new IllegalStateException( "The pool is not editable" );
        _homeTeam = homeTeam;
    }


    /**
     * Gets the away team.
     *
     * @return the away team
     */
    public String getAwayTeam() {
        return _awayTeam;
    }


    /**
     * Sets the away team.
     *
     * @param awayTeam the away team
     */
    public void setAwayTeam( String awayTeam ) {
        if (!BettingPool.isEditable()) throw new IllegalStateException( "The pool is not editable" );
        _awayTeam = awayTeam;
    }


    /** The home team. */
    private String _homeTeam = "";
    /** The away team. */
    private String _awayTeam = "";
}
