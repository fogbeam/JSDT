
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

import com.sun.media.jsdt.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import java.net.*;
import java.util.StringTokenizer;

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
SoundUser extends Applet
          implements ActionListener, ItemListener, SoundDebugFlags {

    private static final String STOPLABEL    = "Stop ";
    private static final String STOPSYMBOL   = " o ";
    private static final String PLAYLABEL    = "Play ";
    private static final String PLAYSYMBOL   = " > ";
    private static final String NEXTLABEL    = "Next ";
    private static final String NEXTSYMBOL   = " >> ";
    private static final String QUITLABEL    = "Quit";
    private static final String QUITSYMBOL   = " ! ";

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    // The name to use when connecting to the sound session/channel.
    private String name = null;

    // Set true if we are currently joined to the sound session.
    private boolean joinedSession = false;

    // Set true if we are currently joined to an audio channel.
    private boolean joinedChannel = false;

    // The sound session that this client application will join.
    private Session session;

    // The client that will be joining the session and channel.
    private SoundClient client;

    // Number of audio tracks available from the server.
    private int noTracks;

    // The complete audio file names.
    private String audioFileNames[];

    // The audio channels that the sound files will be sent over.
    private Channel audioChannels[];

    // The corresponding titles.
    private String titles[];

    // The corresponding image names.
    private String imageNames[];

    // The corresponding images.
    private Image images[];

    // The image to display if no audio file is selected.
    private Image jukebox;

    // The list of audio tracks available.
    private List list;

    // Canvas displaying artwork for the currently selected audio file.
    private SoundCanvas soundCanvas;

    // The consumer of all data sent over the audio channel.
    private AudioConsumer audioConsumer;

    // The channel that the sound file information will be sent over.
    private Channel commandChannel;

    // The consumer of all data sent over the command channel.
    private CommandConsumer commandConsumer;

    // Used to send the audio file to the workstation speaker.
    private Speaker speaker = null;

    // The current channel number that's playing (or -1 if none).
    private int currentChannelNo = -1;

// Default setup, will mostly be overriden by attributes.

    int width  = 400;
    int height = 450;

    private String hostname    = "localhost";
    private int    hostport    = 4463;
    private String sessionType = "socket";

    private Button playButton, stopButton, nextButton, quitButton;


    private void
    getOptions(String args[]) {
        if (SoundUser_Debug) {
            System.err.println("SoundUser: getOptions:");
            if (args != null) {
                for (int i = 0; i < args.length ; i++) {
                    System.err.println("args[" + i + "]: " + args[i]);
                }
            }
        }

        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        } else {
            width = 300;
        }

        if (getArg(args, "height") != null) {
            height = Integer.parseInt(getArg(args, "height"));
        } else {
            height = 250;
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
    }


    private String
    getArg(String args[], String arg) {
        if (SoundUser_Debug) {
            System.err.println("SoundUser: getArg:" +
                                " arg: " + arg);
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
        if (SoundUser_Debug) {
            System.err.println("SoundUser: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        Panel button1, button2, button3, button4, buttonGrid, panel;
        URL docBase = null;

        if (SoundUser_Debug) {
            System.err.println("SoundUser: initialize.");
        }

        name = uniqueName(this);

        startSession();

        if (!isApplication) {
            docBase = getDocumentBase();
        } else {
            docBase = SoundUser.class.getResource("");
        }

        getImages(docBase);

        setBackground(Color.white);
        setLayout(new BorderLayout());

        panel = new Panel();
        panel.setLayout(new BorderLayout());
        list = new List(3, false);
        for (int i = 0; i < titles.length; i++) {
            list.add(titles[i]);
        }
        list.addItemListener(this);
        panel.add("West", new Label("Selection:"));
        panel.add("Center", list);
        add("North", panel);

        buttonGrid = new Panel();

        button1 = new Panel();
        button1.setLayout(new BorderLayout());
        button1.add("North", new Label(STOPLABEL, Label.CENTER));
        stopButton = new Button(STOPSYMBOL);
        stopButton.setFont(new Font("Courier", Font.BOLD, 12));
        stopButton.addActionListener(this);
        stopButton.setEnabled(false);
        button1.add("South", stopButton);
        buttonGrid.add(button1);

        button2 = new Panel();
        button2.setLayout(new BorderLayout());
        button2.add("North", new Label(PLAYLABEL, Label.CENTER));
        playButton = new Button(PLAYSYMBOL);
        playButton.setFont(new Font("Courier", Font.BOLD, 12));
        playButton.addActionListener(this);
        playButton.setEnabled(false);
        button2.add("South", playButton);
        buttonGrid.add(button2);

        button3 = new Panel();
        button3.setLayout(new BorderLayout());
        button3.add("North", new Label(NEXTLABEL, Label.CENTER));
        nextButton = new Button(NEXTSYMBOL);
        nextButton.setFont(new Font("Courier", Font.BOLD, 12));
        nextButton.addActionListener(this);
        nextButton.setEnabled(false);
        button3.add("South", nextButton);
        buttonGrid.add(button3);

        button4 = new Panel();
        if (isApplication) {
            button4.setLayout(new BorderLayout());
            button4.add("North", new Label(QUITLABEL, Label.CENTER));
            quitButton = new Button(QUITSYMBOL);
            quitButton.setFont(new Font("Courier", Font.BOLD, 12));
            quitButton.addActionListener(this);
            button4.add("South", quitButton);
            buttonGrid.add(button4);
        }

        add("South", buttonGrid);

        soundCanvas = new SoundCanvas(jukebox, images);
        add("Center", soundCanvas);

        resize(width, height);
        setVisible(true);
    }


/**
 * <A NAME="SD_UNIQUENAME"></A>
 * <EM>uniqueName</EM> generates a unique name for object based on its
 * hashcode and the host it's running on.
 *
 * @param object the object in question.
 *
 * @return a String which is a unique name.
 */

    private String
    uniqueName(Object object) {
        String name = null;

        if (SoundUser_Debug) {
            System.err.println("SoundUser: uniqueName.");
        }

        try {
            name = InetAddress.getLocalHost().getHostName() + object.hashCode();
        } catch (Exception e) {
            System.err.println("SoundUser: uniqueName: exception " + e);
            if (SoundUser_Debug) {
                e.printStackTrace();
            }
        }
        return(name);
    }


    private void
    getImages(URL docBase) {
        int i;
        int count = imageNames.length;
        MediaTracker tracker = new MediaTracker(this);

        if (SoundUser_Debug) {
            System.err.println("SoundUser: getImages:" +
                                " docBase: " + docBase);
        }

        for (i = 0; i < count; i++) {
            if (docBase == null) {
                images[i] = getToolkit().getImage(imageNames[i]);
            } else {
                try {
                    images[i] = getToolkit().getImage(
                                             new URL(docBase, imageNames[i]));
                } catch (MalformedURLException me) {
                }
            }
            tracker.addImage(images[i], i);
        }

        if (docBase == null) {
            jukebox = getToolkit().getImage("images/jukebox.gif");
        } else {
            try {
                String imageName = "../../images/jukebox.gif";

                jukebox = getToolkit().getImage(new URL(docBase, imageName));
            } catch (MalformedURLException me) {
            }
        }
        tracker.addImage(jukebox, i);

        if (SoundUser_Debug) {
            System.err.println("Waiting for images to be loaded.");
        }
        try {
            tracker.waitForAll();
        } catch (InterruptedException e) {
        }
    }


    private boolean
    startSession() {
        String sessionName        = "SoundSession";
        String commandChannelName = "CommandChannel";

        if (SoundUser_Debug) {
            System.err.println("SoundUser: startSession.");
        }

// Resolve the sound session and join it with a newly created client.

        System.err.println("Creating a Sound Client...");
        client = new SoundClient(name);

        try {
            String    names[];
            URLString url     = URLString.createSessionURL(hostname, hostport,
                                                     sessionType, sessionName);

            session = SessionFactory.createSession(client, url, true);

            commandChannel = session.createChannel(client,
                                commandChannelName, true, true, true);
            commandConsumer = new CommandConsumer(client.getName());
            commandChannel.addConsumer(client, commandConsumer);

            names = session.listChannelNames();
            noTracks = names.length - 1;

            audioFileNames = new String[noTracks];
            audioChannels  = new Channel[noTracks];
            titles         = new String[noTracks];
            imageNames     = new String[noTracks];
            images         = new Image[noTracks];

            for (int i = 0, n = 0; i < names.length; i++) {
                if (!names[i].equals(commandChannelName)) {
                    StringTokenizer tok = new StringTokenizer(names[i], ";");

                    audioChannels[n]  = session.createChannel(client,
                                            names[i], false, false, false);
                    titles[n]         = tok.nextToken();
                    imageNames[n]     = tok.nextToken();
                    audioFileNames[n] = tok.nextToken();
                    n++;
                }
            }

            joinedSession = true;
            repaint();
        } catch (Exception e) {
            System.err.println("SoundUser: startSession: " +
                               "caught exception: " + e);
            if (SoundUser_Debug) {
                e.printStackTrace();
            }
            return(false);
        }
        return(true);
    }


    private boolean
    startChannel(int channelNo) {
        if (SoundUser_Debug) {
            System.err.println("SoundUser: startChannel:" +
                                " channel #:" + channelNo);
        }

        if (currentChannelNo == channelNo && joinedChannel) {
            return(true);
        }

        try {
            audioChannels[channelNo].join(client);
            audioConsumer = new AudioConsumer(client.getName());
            audioChannels[channelNo].addConsumer(client, audioConsumer);

            joinedChannel    = true;
            currentChannelNo = channelNo;

            if (speaker == null) {
                String name = audioFileNames[channelNo];

                while (!commandConsumer.isSoundInfo(name)) {
                    Thread.sleep(1000);
                }

                speaker = new Speaker(commandConsumer.getSampleRate(name),
                                      commandConsumer.getEncoding(name),
                                      commandConsumer.getChannels(name));
            }
            speaker.setConsumer(audioConsumer);
            speaker.play();
        } catch (Exception e) {
            System.err.println("SoundUser: startChannel: " +
                               "caught exception: " + e);
            if (SoundUser_Debug) {
                e.printStackTrace();
            }
            return(false);
        }
        return(true);
    }


    boolean
    stopSession() {
        if (SoundUser_Debug) {
            System.err.println("SoundUser: stopSession.");
        }

        if (!stopChannel(currentChannelNo)) {
            return(false);
        }

        if (joinedSession) {
            try {
                commandChannel.removeConsumer(client, commandConsumer);
                commandChannel.leave(client);
                session.leave(client);
            } catch (Exception e) {
                System.err.println("SoundUser: startChannel: " +
                                   "caught exception: " + e);
                if (SoundUser_Debug) {
                    e.printStackTrace();
                }
                return(false);
            }

            try {
                session.close(true);
                joinedSession = false;
            } catch (Exception e) {
                System.err.println("SoundUser: startChannel: " +
                                   "caught exception: " + e);
                if (SoundUser_Debug) {
                    e.printStackTrace();
                }
                return(false);
            }
        }
        return(true);
    }


    private boolean
    stopChannel(int channelNo) {
        if (SoundUser_Debug) {
            System.err.println("SoundUser: stopChannel:" +
                                " channel #: " + channelNo);
        }

        if (channelNo == -1 || !joinedChannel) {
            return(true);
        }

        try {
            if (speaker != null) {
                speaker.stop();
            }

            audioChannels[channelNo].removeConsumer(client, audioConsumer);
            audioChannels[channelNo].leave(client);
            joinedChannel = false;
        } catch (Exception e) {
            System.err.println("SoundUser: stopChannel: " +
                               "caught exception: " + e);
            if (SoundUser_Debug) {
                e.printStackTrace();
            }
            return(false);
        }
        return(true);
    }


    public void
    itemStateChanged(ItemEvent event) {
        Object source = event.getSource();

        if (SoundUser_Debug) {
            System.err.println("SoundUser: itemStateChanged:" +
                                " event: " + event);
        }

        if (source == list) {
            int oldChannelNo = currentChannelNo;

            currentChannelNo = list.getSelectedIndex();
            soundCanvas.setImageNo(currentChannelNo);
            if (currentChannelNo == -1) {
                playButton.setEnabled(false);
                stopButton.setEnabled(false);
                nextButton.setEnabled(false);
            } else {
                stopChannel(oldChannelNo);
                playButton.setEnabled(true);
                stopButton.setEnabled(true);
                nextButton.setEnabled(true);
            }
            soundCanvas.repaint();
        }
    }


    public void
    actionPerformed(ActionEvent event) {
        Object source = event.getSource();

        if (SoundUser_Debug) {
            System.err.println("SoundUser: actionPerformed:" +
                                " event: " + event);
        }

        if (source == stopButton) {
            stopChannel(currentChannelNo);
        } else if (source == playButton) {
            startChannel(currentChannelNo);
        } else if (source == nextButton) {
            stopChannel(currentChannelNo);

            currentChannelNo++;
            if (currentChannelNo == noTracks) {
                currentChannelNo = 0;
            }
            soundCanvas.setImageNo(currentChannelNo);
            list.select(currentChannelNo);
            soundCanvas.repaint();

            startChannel(currentChannelNo);
        } else if (source == quitButton) {
            stopSession();
            System.exit(0);
        }
    }


    public void
    destroy() {
        if (SoundUser_Debug) {
            System.err.println("SoundUser: destroy.");
        }

        stopSession();
    }


    public static void
    main(String args[]) {
        SoundUser soundUser = new SoundUser();

        if (SoundUser_Debug) {
            System.err.println("SoundUser: main.");
        }

        soundUser.isApplication = true;
        soundUser.getOptions(args);
        soundUser.initialize();
        new SoundUserFrame(soundUser);
    }
}


class
SoundUserFrame extends Frame implements WindowListener, SoundDebugFlags {

    private final SoundUser soundUser;

    SoundUserFrame(SoundUser soundUser) {
        if (SoundUserFrame_Debug) {
            System.err.println("SoundUserFrame: constructor.");
        }

        this.soundUser = soundUser;

        setTitle("Sound Player");
        add(soundUser);
        addWindowListener(this);
        pack();
        setSize(soundUser.width, soundUser.height);
        setVisible(true);
    }


    public void windowClosed(WindowEvent event) {}

    public void windowDeiconified(WindowEvent event) {}

    public void windowIconified(WindowEvent event) {}

    public void windowActivated(WindowEvent event) {}

    public void windowDeactivated(WindowEvent event) {}

    public void windowOpened(WindowEvent event) {}

    public void
    windowClosing(WindowEvent event) {
        if (SoundUserFrame_Debug) {
            System.err.println("SoundUserFrame: windowClosing.");
        }

        soundUser.stopSession();
        System.exit(0);
    }
}
