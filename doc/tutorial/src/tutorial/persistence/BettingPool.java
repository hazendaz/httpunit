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


    private final static int NUM_GAMES = 10;

    private final static int INITIAL_STATE = 0;
    private final static int POOL_OPEN     = 1;

    private static int _state;

    private static int _tieBreakerIndex;

    private static BettingPoolGame[] _games = new BettingPoolGame[ NUM_GAMES ];


}
