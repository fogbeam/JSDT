
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

import java.awt.*;

/**
 * Description: Racket extends Rectangle.
 *
 * @version     2.3 - 1st November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 */

class
Racket extends Rectangle {

    public
    Racket(int x, int y, int width, int height) {
        super(x, y, width, height);
    }


    public
    Racket(Racket copyRacket) {
        super(copyRacket.x, copyRacket.y, copyRacket.width, copyRacket.height);
    }


    public void
    updatePosition(int mouseY, int lowerYBound, int upperYBound) {

// If mouse is not at the vertical center of the myRacket,

        if ((mouseY < y + height / 2) || (mouseY > y + height / 2)) {

// Move myRacket.

            if ((mouseY - height / 2)< lowerYBound) {
                setLocation(x, lowerYBound + 1);
            } else if ((mouseY + height / 2) > upperYBound) {
                setLocation(x, upperYBound - height);
            } else {
                setLocation(x, mouseY - height / 2);
            }
        }
    }


    public void
    paint(Graphics g) {
        g.setColor(Color.red);
        g.fillRect(x, y, width, height);
    }
}
