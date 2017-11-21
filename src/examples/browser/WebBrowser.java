
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

package examples.browser;

import com.sun.media.jsdt.*;
import com.sun.media.jsdt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.*;
import java.util.*;

/**
 * A simple collaborative web browser using the Swing JEditorPane class
 * for HTML rendering.
 *
 * @version     2.3 - 21st November 2017
 * @author      Rich Burridge
 */

public class
WebBrowser extends JApplet
           implements WebBrowserDebugFlags {

    // Are we running as an application (as opposed to an applet)?
    boolean isApplication = false;

    // Are we a teacher (as opposed to a student)?
    boolean isTeacher = false;

    // The (random) name of this Client.
    protected final String name;

    // The web session that this client application will join.
    private Session session;

    // The client that will be joining the session and channel.
    WebClient client;

    // The channel that this client application will use to send data.
    Channel channel;

    // Indicates if the client is successfully connected to the server.
    private boolean connected = false;

    // Default setup, will mostly be overriden by attributes.
    String location = "http://google.com/";
    int    width    = 700;
    int    height   = 800;

    private String  hostname    = "localhost";
    private int     hostport    = 4468;
    private String  sessionType = "socket";

    // Vectors of URLS visited (for use by the Back/Forward buttons).
    Vector<String> urls = null;

    // Current index into the vector of URL's visited.
    int curURLIndex = -1;

    // Index of last URL visited in URL vector.
    int lastURLIndex = -1;

    protected Browser    browser;
    protected JPanel     panel;
    protected JTextField url;

    // The navigation buttons.
    protected JButton back, forward, reload, exit;


    public
    WebBrowser() {
        name = System.getProperties().getProperty("user.name") +
               new Random().nextInt();
        urls = new Vector<>();

        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: constructor:" +
                               " name: " + name);
        }
    }


    private void
    getOptions(String args[]) {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: getOptions:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        }

        if (getArg(args, "height") != null) {
            height = Integer.parseInt(getArg(args, "height"));
        }

        if (getArg(args, "server") != null) {
            hostname = getArg(args, "server");
        }

        if (getArg(args, "port") != null) {
            hostport = Integer.parseInt(getArg(args, "port"));
        }

        if (getArg(args, "type") != null) {
            sessionType = getArg(args, "type");
        }

        if (getArg(args, "location") != null) {
            location = getArg(args, "location");
        }
    }


    private String
    getArg(String args[], String arg) {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: getArg:" + "\n arg: "  + arg);
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        if (isApplication) {
            String option = "-" + arg;
            String retval = null;

            for (int i = 0 ; i < args.length ; i++) {
                if (args[i].equals(option)) {
                    if (++i < args.length) {
                        retval = args[i];
                    }
                    break;
                }
            }
            return(retval);
        } else {
            return(getParameter(arg));
        }
    }


    public void
    init() {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        Container contentPane = getContentPane();

        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: initialize.");
        }

        contentPane.setLayout(new BorderLayout());

        browser = new Browser(this);

        panel = new JPanel();
        panel.setLayout(new BorderLayout());

        panel.add("Center", new NavPanel(this, browser, isApplication));
        panel.add("South", new LocPanel(this, browser));

        contentPane.add("North",  panel);
        contentPane.add("Center", new JScrollPane(browser));

        if (isTeacher) {
            browser.gotoLocation(location, true);
        }

        connect();
    }


    private void
    connect() {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: connect.");
        }

        if (connected) {
            return;
        }

        try {
            String sessionName = "WebSession";

            client = new WebClient(name);

            try {
                // The consumer of all data sent over the channel.
                WebConsumer webConsumer;

                URLString url = URLString.createSessionURL(hostname, hostport,
                                                     sessionType, sessionName);

                if (isTeacher) {
                    if (!RegistryFactory.registryExists(sessionType)) {
                        RegistryFactory.startRegistry(sessionType);
                    }
                }

                session = SessionFactory.createSession(client, url, true);
                channel = session.createChannel(client, "WebChannel",
                                                true, true, true);
                webConsumer = new WebConsumer(browser, client.getName());
                if (isTeacher) {
                    WebListener listener = new WebListener(this);

                    channel.addChannelListener(listener);
                } else {
                    channel.addConsumer(client, webConsumer);
                }

                repaint();
                connected = true;
            } catch (Exception e) {
                System.err.println("WebBrowser: connection: exception: " + e);
                e.printStackTrace();
            }
        } catch (Throwable th) {
            System.err.println("WebBrowser: connect: caught: " + th);
            th.printStackTrace();
            throw new Error("WebBrowser: connect: failed : " + th);
        }
    }


    private void
    disconnect() {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: disconnect.");
        }

        if (!connected) {
            return;
        }

        try {

// Leave the channel and leave the session.

            channel.leave(client);
            session.leave(client);
        } catch (Exception e) {
            System.err.println("WebBrowser: disconnect: exception: " + e);
            if (WebBrowser_Debug) {
                e.printStackTrace();
            }
        }

// Close the session.

        try {
            session.close(true);
            connected = false;
        } catch (Exception e) {
            System.err.println("WebBrowser: disconnect: exception: " + e);
            if (WebBrowser_Debug) {
                e.printStackTrace();
            }
        }
    }


    public void
    destroy() {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: destroy.");
        }

        disconnect();
    }


    void
    startApplication(String args[]) {
        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: startApplication:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        getOptions(args);
        initialize();
        new BrowserFrame(this);
    }
}


class
BrowserFrame extends JFrame
             implements WindowListener, WebBrowserDebugFlags {

    BrowserFrame(WebBrowser wb) {
        Container contentPane = getContentPane();

        if (BrowserFrame_Debug) {
            System.err.println("BrowserFrame: constructor:" +
                               " web browser: " + wb);
        }

        if (wb.isTeacher) {
            setTitle("Collaborative Web Browser [Teacher]");
        } else {
            setTitle("Collaborative Web Browser");
        }
        contentPane.add(wb);
        addWindowListener(this);
        pack();
        setSize(wb.width, wb.height);
        setVisible(true);
    }


    public void
    windowClosing(WindowEvent event) {
        if (BrowserFrame_Debug) {
            System.err.println("BrowserFrame: windowClosing:" +
                               " window event: " + event);
        }

        dispose();
    }


    public void
    windowClosed(WindowEvent event) {
        if (BrowserFrame_Debug) {
            System.err.println("BrowserFrame: windowClosed:" +
                               " window event: " + event);
        }

        System.exit(0);
    }


    public void windowActivated(WindowEvent event)   {}
    public void windowDeactivated(WindowEvent event) {}
    public void windowDeiconified(WindowEvent event) {}
    public void windowIconified(WindowEvent event)   {}
    public void windowOpened(WindowEvent event)      {}
}


class
NavPanel extends JPanel implements WebBrowserDebugFlags {

    public
    NavPanel(WebBrowser wb, final Browser browser, boolean isApplication) {
        if (NavPanel_Debug) {
            System.err.println("NavPanel: constructor:" +
                               "\n web browser: " + wb +
                               "\n browser: "     + browser +
                               "\n application? " + isApplication);
        }

        wb.back = new JButton("Back", new ImageIcon("images/Back-off.gif"));
        wb.back.setEnabled(false);
        add(wb.back);
        wb.back.addActionListener(
            (ActionEvent e) -> browser.goBack()
        );

        wb.forward = new JButton("Forward",
                              new ImageIcon("images/Forward-off.gif"));
        wb.forward.setEnabled(false);
        add(wb.forward);
        wb.forward.addActionListener(
            (ActionEvent e) -> browser.goForward()
        );

        wb.reload = new JButton("Reload",
                                new ImageIcon("images/Reload-off.gif"));
        add(wb.reload);
        wb.reload.addActionListener(
            (ActionEvent e) -> browser.reload()
        );

        wb.exit = new JButton("Exit", new ImageIcon("images/Exit-off.gif"));
        add(wb.exit);
        wb.exit.addActionListener(
            (ActionEvent e) -> System.exit(0)
        );
    }
}


class
LocPanel extends JPanel implements WebBrowserDebugFlags {

    public
    LocPanel(final WebBrowser wb, final Browser browser) {
        if (LocPanel_Debug) {
            System.err.println("LocPanel: constructor:" +
                               "\n web browser: " + wb +
                               "\n browser: "     + browser);
        }

        setLayout(new BorderLayout());
        add("West", new JLabel("Location:"));
        add("Center", (wb.url = new JTextField(50)));
        wb.url.addActionListener(
            (ActionEvent e) -> browser.gotoLocation(wb.url.getText(), true)
        );
    }
}


class
Browser extends JEditorPane
        implements HyperlinkListener, PropertyChangeListener,
                   WebBrowserDebugFlags {

    private final WebBrowser wb;

    public
    Browser(WebBrowser wb) {
        if (Browser_Debug) {
            System.err.println("Browser: constructor:" +
                               " eb browser: " + wb);
        }

        this.wb = wb;
        setEditable(false);                /* Make it read-only. */
        addPropertyChangeListener(this);
        addHyperlinkListener(this);
    }


    public void
    goBack() {
        if (Browser_Debug) {
            System.err.println("Browser: goBack.");
        }

        wb.curURLIndex--;
        gotoLocation(wb.urls.elementAt(wb.curURLIndex), false);
        if (wb.curURLIndex <= 0) {
            wb.back.setEnabled(false);
        }
        wb.forward.setEnabled(true);
    }


    public void
    goForward() {
        if (Browser_Debug) {
            System.err.println("Browser: goForward.");
        }

        wb.curURLIndex++;
        gotoLocation(wb.urls.elementAt(wb.curURLIndex), false);
        if (wb.curURLIndex == wb.lastURLIndex) {
            wb.forward.setEnabled(false);
        }
        wb.back.setEnabled(true);
    }


    public void
    reload() {
        if (Browser_Debug) {
            System.err.println("Browser: reload.");
        }

        gotoLocation(wb.url.getText(), false);
    }


    private void
    addURL(String url) {
        if (Browser_Debug) {
            System.err.println("Browser: addURL:" +
                               " url: " + url);
        }

        wb.urls.addElement(url);
        wb.curURLIndex++;
        wb.lastURLIndex++;
        if (wb.curURLIndex > 0) {
            wb.back.setEnabled(true);
        }
    }


    public void
    gotoLocation(String url, boolean add) {
        if (Browser_Debug) {
            System.err.println("Browser: goLocation:" +
                               "\n url: " + url +
                               "\n add: " + add);
        }

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            wb.browser.setPage(url);
            wb.url.setText(url);
            if (add) {
                addURL(url);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(wb.browser,
                                          new String[] {
                                                    "Unable to open file",
                                              url
                                          }, "File Open Error",
                                          JOptionPane.ERROR_MESSAGE);
        }

        setCursor(Cursor.getDefaultCursor());
    }


    public void
    hyperlinkUpdate(HyperlinkEvent event) {
        URL url = event.getURL();

        if (url != null) {
            String urlString = url.toString();

            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                if (Browser_Debug) {
                    System.err.println("Browser: hyperlinkUpdate:" +
                                       " url: " + url);
                }

                gotoLocation(urlString, true);
            }
        }
    }


    public void
    propertyChange(PropertyChangeEvent event) {
        String prop = event.getPropertyName();

        if (WebBrowser_Debug) {
            System.err.println("WebBrowser: propertyChange:" +
                               " property change event: " + event);
        }

        if (prop.equals("page")) {
            if (wb.isTeacher) {
                String newURL = wb.url.getText();

                if (newURL.startsWith("http://") ||
                    newURL.startsWith("file:/")) {
                    wb.location = newURL;
                    try {
                        wb.channel.sendToOthers(wb.client, new Data(newURL));
                    } catch (JSDTException e) {
                        System.err.println("WebBrowser: propertyChange:" +
                                           " exception: " + e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


class
WebClient implements Client, WebBrowserDebugFlags {

    protected final String name;

    public
    WebClient(String name) {
        if (WebClient_Debug) {
            System.err.println("WebClient: constructor:" +
                                " name: " + name);
        }

        this.name = name;
    }


    public Object
    authenticate(AuthenticationInfo info) {
        if (WebClient_Debug) {
            System.err.println("WebClient: authenticate:" +
                                " auth info: " + info);
        }

        return(null);
    }


    public String
    getName() {
        if (WebClient_Debug) {
            System.err.println("WebClient: getName.");
        }

        return(name);
    }
}


class
WebConsumer implements ChannelConsumer, WebBrowserDebugFlags {

    protected final Browser browser;

    // The name of this channel consumer.
    protected final String name;


    public
    WebConsumer(Browser browser, String name) {
        if (WebConsumer_Debug) {
            System.err.println("WebConsumer: constructor:" +
                                " browser: " + browser +
                                " name: "    + name);
        }

        this.browser = browser;
        this.name    = name;
    }


    public synchronized void
    dataReceived(Data data) {
        String theURL = data.getDataAsString();

        if (WebConsumer_Debug) {
            System.err.println("WebConsumer: dataReceived:" +
                                " data: " + data);
        }

        browser.gotoLocation(theURL, true);
    }
}


class
WebListener extends ChannelAdaptor implements WebBrowserDebugFlags {

    private WebBrowser webBrowser = null;

    public
    WebListener(WebBrowser webBrowser) {
        if (WebListener_Debug) {
            System.err.println("WebListener: constructor:" +
                                " web browser: " + webBrowser);
        }

        this.webBrowser = webBrowser;
    }


    public void
    channelConsumerAdded(ChannelEvent event) {
        Channel channel       = event.getChannel();
        String  newClientName = event.getClientName();

        if (WebListener_Debug) {
            System.err.println("WebListener: channelConsumerAdded:" +
                                " event: " + event);
        }

        try {
            channel.sendToClient(webBrowser.client, newClientName,
                                 new Data(webBrowser.location));
        } catch (JSDTException e) {
            System.err.println("WebListener: channelConsumerAdded:" +
                               " exception: " + e);
            e.printStackTrace();
        }
    }
}
