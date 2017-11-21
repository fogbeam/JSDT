
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
import java.util.*;

/**
 * This is just a simple server/client based proof-of-concept
 * implementation to make sure that the Shared Data classes have
 * all the required API in them.
 *
 * It's based on WhiteBoard; a test applet for the game server from
 * daniel@vpro.nl. It's been rewritten to use JSDT and to make it
 * easier to modify and maintain.
 *
 * @version     2.3 - 21st November 2017
 * @author      daniel@vpro.nl
 * @author      Rich Burridge
 */

public class
DrawingArea extends Panel implements KeyListener, MouseListener,
                                MouseMotionListener, WhiteBoardDebugFlags {

    // The WhiteBoardUser for this menu canvas.
    private final WhiteBoardUser wbu;

    // Offscreen backing store image of the menu controls.
    private Image image = null;

    // The Graphics context for the offscreen image.
    private Graphics gc;

    // The width of the white board drawing area canvas.
    private final int width;

    // The height of the white board drawing area canvas.
    private final int height;


    public
    DrawingArea(int width, int height, WhiteBoardUser client) {
        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: constructor:" +
                                " width: "  + width +
                                " height: " + height +
                                " client: " + client);
        }

        this.width  = width;
        this.height = height;
        wbu         = client;
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }


    public Dimension
    getPreferredSize() {
        return(new Dimension(width, height));
    }


    public void mouseReleased(MouseEvent event) {}

    public void mouseEntered(MouseEvent event) {}

    public void mouseExited(MouseEvent event) {}

    public void mouseClicked(MouseEvent event) {}

    public void mouseMoved(MouseEvent event) {}

    public void
    mousePressed(MouseEvent event) {
        int x = event.getX();
        int y = event.getY();

        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: mousePressed.");
        }

        if (wbu.drawType == 0) {                        // DOT.
            wbu.writeLine("DOT " + x + " " + y + "\n");
        } else if (wbu.drawType == 1) {                 // LINE.
            if (wbu.pointx1 == 0) {
                wbu.pointx1 = x;
                wbu.pointy1 = y;
            } else {
                wbu.writeLine("LINE " + wbu.pointx1 + " " +
                              wbu.pointy1 + " " + x + " " + y + "\n");
                wbu.pointx1 = 0;
            }
        } else if (wbu.drawType == 2) {                 // TEXT.
            if (wbu.pointx1 == 0) {
                wbu.pointx1   = x;
                wbu.pointy1   = y;
                wbu.textMode  = true;
                requestFocus();
            }
        } else if (wbu.drawType == 3) {                 // CIRCLE.
            if (wbu.pointx1 == 0) {
                wbu.pointx1 = x;
                wbu.pointy1 = y;
            } else {
                wbu.writeLine("CIRCLE " + wbu.pointx1 + " " +
                                wbu.pointy1 + " " + x + " " + y + "\n");
                wbu.pointx1 = 0;
            }
        }

        wbu.controls.repaint();
        repaint();
    }


    public void
    mouseDragged(MouseEvent event) {
        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: mouseDragged.");
        }

        if (wbu.drawType == 0) {
            wbu.writeLine("DOT " + event.getX() + " " + event.getY() + "\n");
        }

        wbu.controls.repaint();
        repaint();
    }


    public void keyPressed(KeyEvent event) {}


    public void keyTyped(KeyEvent event) {}


    public void
    keyReleased(KeyEvent event) {
        char keyChar = event.getKeyChar();
        int  keyCode = event.getKeyCode();

        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: keyReleased: " + event);
        }

        if (keyCode != KeyEvent.VK_SHIFT) {
            wbu.handleKey((int) keyChar);
        }
        wbu.controls.repaint();
        repaint();
    }


    public synchronized void
    update(Graphics g) {
        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: update.");
        }

        if (image == null ||
            image.getWidth(null) != width ||
            image.getHeight(null) != height) {
            image = createImage(width, height);
            if (image != null) {
                if (gc != null) {
                    gc.dispose();
                }
                gc = image.getGraphics();
                gc.setFont(new Font("Helvetica", Font.PLAIN, 24));
                gc.setColor(Color.white);
                gc.fillRect(0, 0, width, height);
            }
        }
        redraw();
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }


    private void
    redraw() {
        String drawcommand;
        int    who, i, j, px1, py1, px2, py2, radius;
        float  k, l;
        StringTokenizer tok;
        Image  selectedbrush;

        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: redraw: commandLine is " +
                                wbu.commandLine);
        }

// Loop for commands, it stays there until no new commands are in the buffer.

        while (wbu.commandLine != null) {
            tok = new StringTokenizer(wbu.commandLine, " \n\r\t");
            wbu.commandLine = null;
            who = Integer.parseInt(tok.nextToken());
            drawcommand = tok.nextToken();

            switch (drawcommand) {
                case "BRUSH":

// Brush change found set new drawimage for that whiteboard.

                    wbu.brushes.put(who,
                                    wbu.bs[Integer.parseInt(tok.nextToken())]);
                    break;
                case "DOT":

// Dot found draw dot in correct brush/color on screen.

                    selectedbrush = wbu.brushes.get(who);

                    if (selectedbrush == null) {
                       wbu.brushes.put(who, wbu.bs[0]);
                    }
                    selectedbrush = wbu.brushes.get(who);

                    gc.drawImage(selectedbrush,
                        Integer.parseInt(tok.nextToken()) -
                                         (selectedbrush.getWidth(this) / 2),
                        Integer.parseInt(tok.nextToken()) -
                                         (selectedbrush.getHeight(this) / 2),
                                         this);
                    break;

                case "TEXT":

// Line of text found, select color and print it.

                    switch (tok.nextToken()) {
                        case "BLACK":
                            gc.setColor(Color.black);
                            break;
                        case "RED":
                            gc.setColor(Color.red);
                            break;
                        case "GREEN":
                            gc.setColor(Color.green);
                            break;
                        case "BLUE":
                            gc.setColor(Color.blue);
                            break;
                        case "YELLOW":
                            gc.setColor(Color.yellow);
                    }
                    gc.drawString(tok.nextToken().replace('|',' '),
                                 Integer.parseInt(tok.nextToken()),
                                 Integer.parseInt(tok.nextToken()));
                    break;
                case "LINE":

// Line command found, calc the RC and draw the line with the current brush.

                    selectedbrush = wbu.brushes.get(who);

                    if (selectedbrush == null) {
                       wbu.brushes.put(who, wbu.bs[0]);
                    }
                    selectedbrush = wbu.brushes.get(who);

                    px1 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getWidth(this) / 2);
                    py1 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getHeight(this) / 2);
                    px2 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getWidth(this) / 2);
                    py2 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getHeight(this) / 2);
                    if (Math.abs(px1 - px2) > Math.abs(py1 - py2)) {
                        k = py1;
                        l = (py2 - py1) / (float) (Math.abs(px2 - px1));
                        j = px1;
                        if (px1 < px2) {
                            i = 1;
                        } else {
                            i = -1;
                        }
                        while (j != px2) {
                            gc.drawImage(selectedbrush, j, (int) k, this);
                            k += l;
                            j += i;
                        }
                    } else {
                        k = px1;
                        l = (px2 - px1) / (float) (Math.abs(py2-py1));
                        j = py1;
                        if (py1 < py2) {
                            i = 1;
                        } else {
                            i = -1;
                        }
                        while (j != py2) {
                            gc.drawImage(selectedbrush, (int) k, j, this);
                            k += l;
                            j += i;
                        }
                    }
                    break;

                case "CIRCLE":

/*  Circle command found, calculate the radius and draw the circle with the
 *  current brush.
 */

                    selectedbrush = wbu.brushes.get(who);

                    if (selectedbrush == null) {
                       wbu.brushes.put(who, wbu.bs[0]);
                    }
                    selectedbrush = wbu.brushes.get(who);

                    px1 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getWidth(this) / 2);
                    py1 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getHeight(this) / 2);
                    px2 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getWidth(this) / 2);
                    py2 = Integer.parseInt(tok.nextToken()) -
                                           (selectedbrush.getHeight(this) / 2);

                    k = Math.abs(px1 - px2);
                    l = Math.abs(py1 - py2);
                    radius = (int) Math.sqrt((k * k) + (l * l));
                    drawCircle(selectedbrush, px1, py1, radius);
                    break;

                case "CLR":

// Clear screen found, clear the drawing area.

                    gc.setColor(Color.white);
                    gc.fillRect(0, 0, width, height);
            }
        }
    }


    private void
    drawCirclePoints(Image brush, int cx, int cy, int dx, int dy) {
        gc.drawImage(brush, cx + dx, cy + dy, this);
        gc.drawImage(brush, cx - dx, cy + dy, this);
        gc.drawImage(brush, cx + dx, cy - dy, this);
        gc.drawImage(brush, cx - dx, cy - dy, this);
        gc.drawImage(brush, cx + dy, cy + dx, this);
        gc.drawImage(brush, cx - dy, cy + dx, this);
        gc.drawImage(brush, cx + dy, cy - dx, this);
        gc.drawImage(brush, cx - dy, cy - dx, this);
    }


    private void
    drawCircle(Image brush, int cx, int cy, int r) {
        int dx = 0;
        int dy = r;
        int p = 3 - 2*r;

        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: drawCircle: cx: " + cx +
                                "  cy: " + cy + "  radius: " + r);
        }

        while (dx < dy) {
            drawCirclePoints(brush, cx, cy, dx, dy);
            if (p < 0) {
                p = p + 4*dx + 6;
            } else {
                p = p + 4*(dx - dy) + 10;
                dy--;
            }
            dx++;
        }
        if (dx == dy) {
            drawCirclePoints(brush, cx, cy, dx, dy);
        }
    }


    public void
    paint(Graphics g) {
        if (DrawingArea_Debug) {
            System.err.println("DrawingArea: paint: image is " + image);
        }

        if (image != null) {
            g.drawImage(image, 0, 0, this);
        } else {
            update(g);
        }
    }
}
