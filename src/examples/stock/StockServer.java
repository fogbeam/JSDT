
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
import com.sun.media.jsdt.event.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Stock quote server.
 * Uses www.marketwatch.com to get stock quote information.
 *
 * @version     2.3 - 21st November 2017
 * @author      Amith Yamasani
 * @author      Rich Burridge
 */

public class
StockServer extends SessionAdaptor
            implements ChannelConsumer, StockDebugFlags {

    private static final int QUOTE_INTERVAL = 10;    // Seconds.

    private BufferedReader br  = null;

    private StockClient stockClient  = null;
    private Session     stockSession = null;

    private Hashtable<String, StockRecord> stocks = null;

    private String tickers;

    private int    noStocks = 0;
    private int    timer;


    public
    StockServer(String args[]) {
        URLString url;
        String    sessionName = "StockSession";
        String    sessionType;
        String    hostname;
        int       hostport;
        Channel   stockChannel;

        if (StockServer_Debug) {
            System.err.println("StockServer: constructor:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        setProps();

        stocks      = new Hashtable<>();
        hostname    = getHost(args);
        hostport    = getPort(args);
        sessionType = getType(args);
        url         = URLString.createSessionURL(hostname, hostport,
                                                 sessionType, sessionName);

        try {

// Registry running?  Start it if it isn't.

            if (!RegistryFactory.registryExists(sessionType)) {
                RegistryFactory.startRegistry(sessionType);
            }

            stockClient  = new StockClient("Server");
            stockSession = SessionFactory.createSession(stockClient,
                                                        url, true);
            stockSession.addSessionListener(this);

            stockChannel = stockSession.createChannel(stockClient,
                                        "StockChannel", true, true, true);
            stockChannel.addConsumer(stockClient, this);
        } catch (JSDTException e) {
            System.err.println("StockServer: constructor: exception: " + e);
            if (StockServer_Debug) {
                e.printStackTrace();
            }
        }
    }


    private void
    updateStockList(Session session, Client client) {
        StringBuffer buf     = new StringBuffer("");
        String       names[];

        if (StockServer_Debug) {
            System.err.println("StockServer: updateStockList:" +
                                " session: " + session +
                                " client: "  + client);
        }

        try {
            names = session.listByteArrayNames();
            if (names == null) {
                return;
            }

            for (int i = 0; i < names.length; i++) {
                if (!stocks.containsKey(names[i])) {
                    ByteArray sba = session.createByteArray(client,
                                                            names[i], true);
                    StockRecord record = new StockRecord(client, sba);

                    if (StockServer_Debug) {
                            System.err.println("StockServer: updateStockList:" +
                                           " adding: " + names[i]);
                    }
                    stocks.put(names[i], record);
                    noStocks++;
                }
                buf.append(names[i]).append(",");
            }
        } catch (JSDTException e) {
            System.err.println("StockServer: updateStockList: exception: " + e);
            if (StockServer_Debug) {
                e.printStackTrace();
            }
        }

        tickers = new String(buf);
    }


    private void
    run() {
        if (StockServer_Debug) {
            System.err.println("StockServer: run.");
        }

        try {
            while (true) {
                updateURL();
                timer = QUOTE_INTERVAL;
                while (timer > 0) {
                    Thread.sleep(1000);
                    timer--;
                }
            }
        } catch (Exception e) {
            System.err.println("StockServer: run: exception: " + e);
            if (StockServer_Debug) {
                e.printStackTrace();
            }
        }
    }


    private static String
    getHost(String args[]) {
        String defHost = "localhost";  // Default host name for connections.
        int length = args.length;

        if (StockServer_Debug) {
            System.err.println("StockServer: getHost.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-server")) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defHost);
    }


    private static int
    getPort(String args[]) {
        int defPort = 4464;   // Default port number for connections.
        int length = args.length;

        if (StockServer_Debug) {
            System.err.println("StockServer: getPort.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-port")) {
                if (++i < length) {
                    return(Integer.parseInt(args[i]));
                }
            }
        }
        return(defPort);
    }


    private static String
    getType(String args[]) {
        String defType = "socket";   // Default Session type.
        int length = args.length;

        if (StockServer_Debug) {
            System.err.println("StockServer: getType.");
        }

        for (int i = 0; i < length; i++) {
            if (args[i].equals("-type")) {
                if (++i < length) {
                    return(args[i]);
                }
            }
        }
        return(defType);
    }


    private void
    setProps() {
        Properties props = new Properties(System.getProperties());

        props = new Properties(props);

        File theUserPropertiesFile;
        String sep = File.separator;
        theUserPropertiesFile = new File(System.getProperty("user.home") +
                                         sep + ".hotjava" +
                                         sep + "properties");

        try {
            FileInputStream in = new FileInputStream(theUserPropertiesFile);
            props.load(new BufferedInputStream(in));
            in.close();
        } catch (Exception e) {
            System.err.println("StockServer: setProps: Error loading properties. Have you run hotjava or appletviewer before? Please do so, and set the firewall proxy in the preferences.");
        }

        System.setProperties(props);
    }


    private void
    readURL(String sUrl) {
        if (StockServer_Debug) {
            System.err.println("StockServer: readURL:" +
                                " url: " + sUrl);
        }

        try {
            InputStreamReader isr;

            if (StockServer_Debug) {
                System.err.println("StockServer: readURL: opening URL.");
            }

            URL url = new URL(sUrl);

            if (StockServer_Debug) {
                System.err.println("StockServer: readURL: reading content.");
            }

            isr = new InputStreamReader((InputStream) url.getContent());
            br  = new BufferedReader(isr);
        } catch (MalformedURLException e) {
            System.err.println("StockServer: readURL: URL error");
        } catch (IOException ie) {
            System.err.println("StockServer: readURL: IO error");
        }

        if (StockServer_Debug) {
            System.err.println("StockServer: readURL: done.");
        }
    }


    private synchronized void
    updateURL() {
        String url;

        if (StockServer_Debug) {
            System.err.println("StockServer: updateURL.");
        }

        updateStockList(stockSession, stockClient);

// If tickers is an empty string (ie: no stocks to monitor), just return.

        if (tickers == null || tickers.length() == 0) {
            if (StockServer_Debug) {
                System.err.println("StockServer: updateURL: " +
                                   " no stocks to monitor at present.");
            }
            return;
        }

// Read in the stock information again.

        url = "https://www.marketwatch.com/investing/multi?tickers=" + tickers;
        if (StockServer_Debug) {
            System.err.println("StockServer: updateURL: tickers: " + tickers);
        }
        readURL(url);
        parseReply();
    }


    private String
    getNextValue(String searchStr, boolean onNextLine,
                 String debugStr, int offset) {
        String line;
        String tokens[];
        String value;
        boolean finished = false;

        // Find the next line that starts with searchStr.
        try {
            do {
                if ((line = br.readLine()) == null) {
                    finished = true;
                    break;
                }
                // Remove initial whitespace.
                line = line.replaceFirst("^\\s*", "");
            } while (!line.startsWith(searchStr));
            if (finished) {
                return null;
            }

            if (onNextLine) {
                // Next line contains what we are looking for.
                if ((line = br.readLine()) == null) {
                    return null;
                }
            }
        } catch (IOException e) {
            return null ;
        }

        tokens = line.split(">");
        value = tokens[1].substring(0, tokens[1].length() - offset);
        if (StockServer_Debug) {
            System.err.println(debugStr + value);
        }
        return value;
    }


    private void
    parseReply() {
        String symbol, time, value, change, percentChange;

        if (StockServer_Debug) {
            System.err.println("StockServer: parseReply.");
        }

// Initially set each stock record invalid.

        for (Enumeration e = stocks.elements(); e. hasMoreElements();) {
            StockRecord next = (StockRecord) e.nextElement();

            next.setValid(false);
        }

// Extract information for all the stocks on the webpage.

        for (int i = 0; i < noStocks; i++) {
            StockRecord current = null;

            // Line ends with: TIME</p>
            time = getNextValue(
                           "<p class=\"lastcolumn bgTimestamp longformat\">",
                            false, "time: ", 3);

            // Line ends with: SYMBOL</a>
            symbol = getNextValue("<div class=\"ticker\">",
                                  true, "symbol: ", 3);
            if (symbol != null) {
                if (StockServer_Debug) {
                    System.err.println("UPDATE: Stock symbol: " + symbol);
                }
                current = stocks.get(symbol);
                if (current != null ) {
                    current.setValid(true);
                    current.setTime(time);
                    if (StockServer_Debug) {
                        System.err.println("UPDATE: Last Trade time: " + time);
                    }
                }
            }

            // Line ends with: PRICE>/p>
            value = getNextValue("<p class=\"data bgLast\">",
                                 false, "value: ", 3);
            if (value != null) {
                if (current != null) {
                    current.setValue(value);
                    if (StockServer_Debug) {
                        System.err.println("UPDATE: Last Trade value: " + value);
                    }
                }
            }

            // Line ends with: CHANGE>/span>
            change = getNextValue("<span class=\"bgChange\">",
                                  false, "change: ", 6);
            if (change != null) {
                if (current != null) {
                    current.setChange(change);
                    if (StockServer_Debug) {
                        System.err.println("UPDATE: Change: " + change);
                    }
                }
            }

            // Line ends with: PERCENT_CHANGE>/span>
            percentChange = getNextValue("<span class=\"bgPercentChange\">",
                                         false, "percent change: ", 6);
            if (percentChange != null)  {
                if (current != null) {
                    current.setPercentChange(percentChange);
                    if (StockServer_Debug) {
                        System.err.println("UPDATE: Percent change: " +
                                           percentChange);
                    }
                }
            }
        }

// Update all the shared byte arrays.

            for (Enumeration e = stocks.elements(); e. hasMoreElements();) {
                StockRecord next = (StockRecord) e.nextElement();

                next.update();
            }
    }


    public String
    getName() {
        if (StockServer_Debug) {
            System.err.println("StockServer: getName.");
        }

        return("Server");
    }


    public void
    dataReceived(Data data) {
        if (StockServer_Debug) {
            System.err.println("StockServer: getName.");
        }

        timer = 0;
    }


    public void
    byteArrayDestroyed(SessionEvent event) {
        String name = event.getResourceName();

        if (StockServer_Debug) {
            System.err.println("StockServer: byteArrayDestroyed:" +
                                " event: " + event);
        }

        stocks.remove(name);
    }


    public static void
    main(String args[]) {
        StockServer stockServer = new StockServer(args);

        if (StockServer_Debug) {
            System.err.println("StockServer: main:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        System.err.println("Setup and bound Stock server.");
        stockServer.run();
    }
}
