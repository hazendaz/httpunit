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


public class BettingPoolGame {

    BettingPoolGame() {
        _homeTeam = _awayTeam = "";
    }


    public String getHomeTeam() {
        return _homeTeam;
    }


    public void setHomeTeam( String homeTeam ) {
        if (!BettingPool.isEditable()) throw new IllegalStateException( "The pool is not editable" );
        _homeTeam = homeTeam;
    }


    public String getAwayTeam() {
        return _awayTeam;
    }


    public void setAwayTeam( String awayTeam ) {
        if (!BettingPool.isEditable()) throw new IllegalStateException( "The pool is not editable" );
        _awayTeam = awayTeam;
    }


    private String _homeTeam = "";
    private String _awayTeam = "";
}
