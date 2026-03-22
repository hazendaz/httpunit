/*
 * SPDX-License-Identifier: MIT
 * See LICENSE file for details.
 *
 * Copyright 2000-2026 Russell Gold
 * Copyright 2021-2000 hazendaz
 */
package tutorial.persistence;


public class BettingPool {

    public static void reset() {
        for (int i = 0; i < _games.length; i++) _games[i] = new BettingPoolGame();
        _tieBreakerIndex = 0;
        _state = INITIAL_STATE;
    }


    public static BettingPoolGame[] getGames() {
        return _games;
    }


    public static int getTieBreakerIndex() {
        return _tieBreakerIndex;
    }


    public static void setTieBreakerIndex( int tieBreakerIndex ) {
        if (!isEditable()) throw new IllegalStateException( "May only modify the pool in INITIAL state" );
        _tieBreakerIndex = tieBreakerIndex;
    }


    public static boolean isEditable() {
        return _state == INITIAL_STATE;
    }


    public static void openPool() {
        if (!isEditable()) throw new IllegalStateException( "May only modify the pool in INITIAL state" );
        _state = POOL_OPEN;
    }


    private static final int NUM_GAMES = 10;

    private static final int INITIAL_STATE = 0;
    private static final int POOL_OPEN     = 1;

    private static int _state;

    private static int _tieBreakerIndex;

    private static BettingPoolGame[] _games = new BettingPoolGame[ NUM_GAMES ];


}
