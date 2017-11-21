
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

package examples.stock;

import com.sun.media.jsdt.*;
import java.io.*;
import java.awt.*;
import java.applet.Applet;

/**
 * Stock quote panel. Uses www.marketwatch.com to obtain stock quote
 * information.
 *
 * @version     2.3 - 21st November 2017
 * @author      Amith Yamasani
 * @author      Rich Burridge
 */

public class
StockPanel extends Panel implements StockDebugFlags, Runnable {

    // The "last trade time" label in the stock title bar.
    private final Label timeLabel;

    // The Shared byte array associated with this stock symbol.
    private ByteArray sba;

    // The byte array listener associated with this shared bytearray.
    private StockListener listener;

    private final Label symbol, value, change, percentChange;

    public
    StockPanel(Applet applet, Session session, Client client,
               Label timeLabel, String name, boolean isApplication) {
        Font   theFont;
        Thread timer;

        if (StockPanel_Debug) {
            System.err.println("StockPanel: constructor: " +
                                " applet: "     + applet +
                                " session: "    + session +
                                " client: "     + client +
                                " time label: " + timeLabel +
                                " name: "       + name +
                                " application? " + isApplication);
        }

        this.timeLabel = timeLabel;

        try {
            sba = session.createByteArray(client, name, true);
            sba.setValue(client, name.getBytes());
            listener = new StockListener(name);
            sba.addByteArrayListener(listener);
        } catch (JSDTException e) {
            if (StockPanel_Debug) {
                System.err.println("StockPanel: constructor: exception " + e);
                e.printStackTrace();
            }
        }

        setLayout(null);
        theFont = new Font("TimesRoman", Font.PLAIN, 14);
        setFont(theFont);
        setBackground(Color.black);
        setForeground(Color.yellow);

        symbol = new Label(name);               // Stock symbol.
        add(symbol);
        symbol.setSize(100, 25);
        symbol.setLocation(0, 0);

        value = new Label(" ");                 // Last trade value.
        add(value);
        value.setSize(100, 25);
        value.setLocation(110, 0);

        change = new Label(" ");                // Change.
        add(change);
        change.setSize(100, 25);
        change.setLocation(220, 0);

        percentChange = new Label(" ");         // Percent change.
        add(percentChange);
        percentChange.setSize(200, 25);
        percentChange.setLocation(330, 0);

        timer = new Thread(this);
        timer.start();
    }


    void
    removeListener() {
        if (StockPanel_Debug) {
            System.err.println("StockPanel: removeListener.");
        }

        try {
            sba.removeByteArrayListener(listener);
        } catch (JSDTException e) {
            if (StockPanel_Debug) {
                System.err.println("StockPanel: constructor: exception " + e);
                e.printStackTrace();
            }
        }
    }


    private void
    checkChange() {
        ByteArrayInputStream bais;
        DataInputStream      dis;
        boolean              isValid;

        if (!listener.hasChanged) {
            return;
        }

        if (StockPanel_Debug) {
            System.err.println("StockPanel: checkChange.");
        }

        bais = new ByteArrayInputStream(listener.newValue, 0,
                                        listener.newValue.length);
        dis  = new DataInputStream(bais);

        try {
            String symbolStr = dis.readUTF();

            if (StockPanel_Debug) {
                System.err.println("StockPanel: checkChanged: symbol: " +
                                   symbolStr);
            }
            symbol.setText(symbolStr);              // Stock symbol.

            isValid = dis.readBoolean();            // Valid stock ticker?
            if (StockPanel_Debug) {
                System.err.println("StockPanel: checkChanged: isValid: " +
                                   isValid);
            }

            if (isValid) {
                timeLabel.setText(dis.readUTF());   // Last trade time.
                value.setText(dis.readUTF());       // Last trade value.

                change.setText(dis.readUTF());          // Change.
                if (change.getText().startsWith("-")) {
                    change.setForeground(Color.red);
                } else if (change.getText().startsWith("+")) {
                    change.setForeground(Color.green);
                } else {
                    change.setForeground(Color.yellow);
                }

                percentChange.setText(dis.readUTF());     // Percent change.
                if (percentChange.getText().startsWith("-")) {
                    percentChange.setForeground(Color.red);
                } else if (percentChange.getText().startsWith("+")) {
                    percentChange.setForeground(Color.green);
                } else {
                    percentChange.setForeground(Color.yellow);
                }
            } else {
                symbol.setForeground(Color.red);
                percentChange.setText(" No such ticker symbol\n");
            }

            listener.hasChanged = false;
            if (StockPanel_Debug) {
                System.err.println("StockPanel: checkChange: " +
                                   " symbol: " + symbol +
                                   " time: "   + timeLabel +
                                   " value: "  + value +
                                   " change: " + change +
                                   " percent change: " + percentChange);
            }
        } catch (IOException e) {
            if (StockPanel_Debug) {
                System.err.println("StockPanel: checkChange:" +
                                   " exception " + e);
                e.printStackTrace();
            }
        }
    }


    public void
    run() {
        if (StockPanel_Debug) {
            System.err.println("StockPanel: run.");
        }

        while (true) {
            checkChange();
            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
            }
        }
    }
}
