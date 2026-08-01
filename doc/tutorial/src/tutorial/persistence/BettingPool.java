/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package tutorial.persistence;


/**
 * The Class BettingPool.
 */
public class BettingPool {

    /**
     * Reset.
     */
    public static void reset() {
        for (int i = 0; i < _games.length; i++) _games[i] = new BettingPoolGame();
        _tieBreakerIndex = 0;
        _state = INITIAL_STATE;
    }


    /**
     * Gets the games.
     *
     * @return the games
     */
    public static BettingPoolGame[] getGames() {
        return _games;
    }


    /**
     * Gets the tie breaker index.
     *
     * @return the tie breaker index
     */
    public static int getTieBreakerIndex() {
        return _tieBreakerIndex;
    }


    /**
     * Sets the tie breaker index.
     *
     * @param tieBreakerIndex the tie breaker index
     */
    public static void setTieBreakerIndex( int tieBreakerIndex ) {
        if (!isEditable()) throw new IllegalStateException( "May only modify the pool in INITIAL state" );
        _tieBreakerIndex = tieBreakerIndex;
    }


    /**
     * Checks if is editable.
     *
     * @return true, if successful
     */
    public static boolean isEditable() {
        return _state == INITIAL_STATE;
    }


    /**
     * Open pool.
     */
    public static void openPool() {
        if (!isEditable()) throw new IllegalStateException( "May only modify the pool in INITIAL state" );
        _state = POOL_OPEN;
    }


    /** The num games. */
    private static final int NUM_GAMES = 10;

    /** The initial state. */
    private static final int INITIAL_STATE = 0;
    /** The pool open. */
    private static final int POOL_OPEN     = 1;

    /** The state. */
    private static int _state;

    /** The tie breaker index. */
    private static int _tieBreakerIndex;

    /** The games. */
    private static BettingPoolGame[] _games = new BettingPoolGame[ NUM_GAMES ];


}
