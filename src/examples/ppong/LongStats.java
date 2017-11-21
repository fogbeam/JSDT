
/*
 *  Copyright (c) 1996-2005 James Begole.
 *  All Rights Reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Library General Public License as
 *  published by the Free Software Foundation; either version 2, or (at
 *  your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 *  02111-1307, USA.
 */

package examples.ppong;

/**
 * @version     2.3 - 20th November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

public class
LongStats implements PpongDebugFlags {

    private final int maxNumData;
    private long    mean        = 0;
    private long    data[]      = null;
    private int     nextIndex   = 0;
    private int     numElements = 0;
    private boolean updated     = false;


    public
    LongStats(int maxNumData) {
        data = new long[maxNumData];
        this.maxNumData = maxNumData;
    }


    public synchronized void
    add(long newItem) {
        data[nextIndex] = newItem;
        nextIndex++;
        nextIndex %= maxNumData;
        updated = true;
        if (numElements < maxNumData) {

// Once numElements == maxNumData, leave it at that value.

            numElements++;
        }
    }


    public synchronized long
    getMean() {
        if (updated) {
            calculateMean();
        }
        return(mean);
    }


    private void
    calculateMean() {
        long total = 0;

        for (int i = 0; i < numElements; i++) {
            total += data[i];
        }

        mean = total / numElements;

        if (LongStats_Debug) {
            System.err.println("LongStats: calculateMean: " +
                               " numElements=" + numElements +
                               " mean = " + mean);
        }
    }
}
