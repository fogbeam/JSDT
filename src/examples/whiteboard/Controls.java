
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

package examples.whiteboard;

import java.awt.*;
import java.awt.event.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on WhiteBoard; a test applet for the game server from
 * daniel@vpro.nl. It's been rewritten to use JSDT and to make it easier
 * to modify and maintain.
 *
 * @version     2.3 - 21st November 2017
 * @author      daniel@vpro.nl
 * @author      Rich Burridge
 */

public class
Controls extends Panel
         implements KeyListener, MouseListener, WhiteBoardDebugFlags {

    // The WhiteBoardUser for this menu canvas.
    private final WhiteBoardUser wbu;

    // Offscreen backing store image of the menu controls.
    private Image image = null;

    // The Graphics context for the offscreen image.
    private Graphics gc;

    // The width of the menu controls canvas.
    private int width;

    // The height of the menu controls canvas.
    private int height;

    public
    Controls(int width, int height, WhiteBoardUser client) {
        if (Controls_Debug) {
            System.err.println("Controls: constructor: " + client);
        }

        this.width  = width;
        this.height = height;
        wbu         = client;
        addKeyListener(this);
        addMouseListener(this);
    }


    public Dimension
    getPreferredSize() {
        return(new Dimension(width, height));
    }


    public void
    mouseReleased(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (Controls_Debug) {
            System.err.println("Controls: mouseReleased.");
        }

        if (y < height-40) {
            if (x > 428 && x < 596) {
                wbu.coll_offset = ((x - 428) / 34) * 8;
                wbu.writeLine("BRUSH " +
                              (wbu.bs_offset + wbu.coll_offset) + "\n");
            }
            if (x > 3 && x < 275) {
                wbu.bs_offset = (x-3) / 34;
                wbu.writeLine("BRUSH " +
                              (wbu.bs_offset + wbu.coll_offset) + "\n");
            }
        } else {
            if (x < 50) {
                wbu.writeLine("CLR\n");
            } else if (x < 135) {
                wbu.drawType = 0;
            } else if (x < 200) {
                wbu.drawType = 1;
            } else if (x < 280) {
                wbu.drawType = 2;
            } else if (x < 400) {
                wbu.drawType = 3;
            }
        }

        repaint();
        wbu.drawingArea.repaint();
    }


    public void mousePressed(MouseEvent event) {}

    public void mouseEntered(MouseEvent event) {}

    public void mouseExited(MouseEvent event) {}

    public void mouseClicked(MouseEvent event) {}

    public void keyPressed(KeyEvent event) {}

    public void keyTyped(KeyEvent event) {}


    public void
    keyReleased(KeyEvent event) {
        if (Controls_Debug) {
            System.err.println("Controls: keyReleased.");
        }

        wbu.handleKey(event.getKeyChar());
        repaint();
        wbu.drawingArea.repaint();
    }


    public synchronized void
    update(Graphics g) {
        Dimension d = getSize();

        if (Controls_Debug) {
            System.err.println("Controls: update.");
        }

        if ((image == null) || (d.width != width) || (d.height != height)) {
            image = createImage(width, height);
            width = d.width;
            height = d.height;
            gc = image.getGraphics();
            gc.setFont(new Font("Helvetica", Font.PLAIN, 24));
        }
        redraw();
        g.drawImage(image, 0, 0, this);
    }


    private void
    redraw() {
        int i;

        if (Controls_Debug) {
            System.err.println("Controls: redraw.");
        }

// Paint control bars.

        gc.setColor(Color.black);
        gc.fillRect(0, height-80, width-1, 39);
        gc.fillRect(0, height-40, width-1, 39);

// Paint brushes.

        for (i = 0; i < 8; i++) {
            gc.setColor(Color.white);
            gc.fillRect(3 + (i*34), height-77, 32, 32);
            gc.drawImage(wbu.bs[i + wbu.coll_offset],
                         3+(i*34), height-77, this);
        }

// White out area between brushes and color selector.

        gc.setColor(Color.white);
        gc.fillRect(241, height-77, 184, 32);

// Create simple color selector.

        gc.setColor(Color.black);
        gc.fillRect(428, height-77, 32, 32);

        gc.setColor(Color.red);
        gc.fillRect(462, height-77, 32, 32);

        gc.setColor(Color.green);
        gc.fillRect(496, height-77, 32, 32);

        gc.setColor(Color.blue);
        gc.fillRect(530, height-77, 32, 32);

        gc.setColor(Color.yellow);
        gc.fillRect(564, height-77, 32, 32);

        gc.setColor(Color.white);

// When in text mode draw current text in correct color.

        if (wbu.textMode) {
            if (wbu.coll_offset == 0) {
                gc.setColor(Color.white);
            }
            if (wbu.coll_offset == 8) {
                gc.setColor(Color.red);
            }
            if (wbu.coll_offset == 16) {
                gc.setColor(Color.green);
            }
            if (wbu.coll_offset == 24) {
                gc.setColor(Color.blue);
            }
            if (wbu.coll_offset == 32) {
                gc.setColor(Color.yellow);
            }
            gc.drawString("TEXT = " + wbu.textLine, 10, height-10);
            return;
        }

// Fill first control bar.

        gc.drawString("CLR",    10,  height-10);
        gc.drawString("DOT",    71,  height-10);
        gc.drawString("LINE",   139, height-10);
        gc.drawString("TEXT",   206, height-10);
        gc.drawString("CIRCLE", 280, height-10);
        gc.setColor(Color.black);
        gc.drawRect(4 + (wbu.bs_offset*34), height-76, 30, 30);
        gc.setColor(Color.yellow);
        gc.drawRect(3 + (wbu.bs_offset*34), height-77, 32, 32);

// When something is selected highlight it.

        if (wbu.drawType == 0) {
            gc.drawString("DOT", 71, height-10);
        } else if (wbu.drawType == 1) {
            gc.drawString("LINE", 139, height-10);
        } else if (wbu.drawType == 2) {
            gc.drawString("TEXT", 206, height-10);
        } else if (wbu.drawType == 3) {
            gc.drawString("CIRCLE", 280, height-10);
        }
        gc.setColor(Color.black);
    }


    public void
    paint(Graphics g) {
        if (Controls_Debug) {
            System.err.println("Controls: paint: image is " + image);
        }

        if (image != null) {
            g.drawImage(image, 0, 0, this);
        } else {
            update(g);
        }
    }
}
