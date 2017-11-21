
/*
 *  Copyright (c) 1996-2005 Sun Microsystems, Inc.
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

package examples.sound;

import java.awt.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to test UDP channels.
 *
 * The server continuously sends a sound file over a UDP channel
 * to each client joined to the channel. These in turn, play the sound on
 * the workstation speaker.
 *
 * @version     2.3 - 20th November 2017
 * @author      Rich Burridge
 */

public class
SoundCanvas extends Canvas implements SoundDebugFlags {

    // The image to display when no audio file is currently selected.
    private final Image jukebox;

    // The artwork images for the various audio files.
    private final Image images[];

    // The current image number that we are displaying (or -1 if none).
    private int currentImageNo = -1;


    public
    SoundCanvas(Image jukebox, Image images[]) {
        if (SoundCanvas_Debug) {
            System.err.println("SoundCanvas: constructor:" +
                                " jukebox image: " + jukebox);
            for (int i = 0; i < images.length ; i++) {
                System.err.println("images[" + i + "]: " + images[i]);
            }
        }

        this.jukebox = jukebox;
        this.images  = images;
    }


    public void
    setImageNo(int imageNo) {
        if (SoundCanvas_Debug) {
            System.err.println("SoundCanvas: setImageNo:" +
                                " image#: " + imageNo);
        }

        currentImageNo = imageNo;
    }


    public void
    paint(Graphics g) {
        Image image;
        int cWidth, cHeight, iWidth, iHeight, x, y;

        if (SoundCanvas_Debug) {
            System.err.println("SoundCanvas: paint:" +
                                " graphics: " + g);
        }

        if (currentImageNo == -1) {
            image = jukebox;
        } else {
            image = images[currentImageNo];
        }

        cWidth  = getSize().width;
        cHeight = getSize().height;
        iWidth  = image.getWidth(null);
        iHeight = image.getHeight(null);
        x       = (cWidth - iWidth) / 2;
        y       = (cHeight - iHeight) / 2;

        g.drawImage(image, x, y, this);
    }
}
