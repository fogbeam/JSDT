
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
import java.awt.event.*;
import java.applet.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import sun.audio.*;

/**
 * Description: A two player network game.  Based on classic Pong.
 *
 * @version     2.3 - 20th November 2017
 * @author      James "Bo" Begole
 * @author      Rich Burridge
 *
 * Usage Notes:
 *
 * Currently a server, sets up the game and then hooks the two clients up
 * together, echoing the messages from one to the other and vice versa.
 *
 * Then there needs to be a client that has an applet tag similar to:
 *
 *     <applet code="Ppong.class" width=600 height=450>
 *     <param name="server" value="localhost">
 *     <param name="port" value="4462">
 *     </applet>
 */

public class
Ppong extends Applet implements ActionListener, Runnable, PpongDebugFlags {

    // Are we running as an application (as opposed to an applet)?
    boolean isApplication = false;

    private static final int     defaultPort = 4462;
    private static final String  defaultHost = "localhost";
    private static final String  defaultType = "socket";
    private static final String  version     = "1.5";
    private static final String  copyright   = "Ppong! " + version +
           ".  Copyright (c) 1996 - 2017 James Begole, All Rights Reserved.";

// Variables with default values

    public final Label messageText =
                        new Label("Welcome to Ppong!  Press 'Start a Game.'");
    public Panel p = null;
    public final ToggleButton startButton =
                        new ToggleButton("Start a Game", "End Game");
    public final ToggleButton pauseButton =
                        new ToggleButton("Pause ", "Resume");
    public final ToggleButton musicButton =
                        new ToggleButton("No Music", "Music");
    public final ToggleButton soundsButton =
                        new ToggleButton("No Sound Effects", "Sound Effects");
    private boolean playSounds = true;
    private boolean playMusic  = true;

// Uninitialized variables

    private Thread     life  = null;
    public  Game       game  = null;
    private PpongCourt court = null;

//  Various sound types.

    public static final int S_MUSIC     = 0;
    public static final int S_ATTENTION = 1;
    public static final int S_HITRACKET = 2;
    public static final int S_HITWALL   = 3;
    public static final int S_IMISSED   = 4;
    public static final int S_OMISSED   = 5;

    private static final int MAXSOUNDS = 6;

// Array of audio clips / streams.

    private final Object sounds[] = new Object[MAXSOUNDS];

// Server information

    int    width      = 600;
    int    height     = 450;
    String remoteHost = defaultHost;
    private int    thePort    = defaultPort;
    private String theType    = defaultType;


    private void
    getOptions(String args[]) {
        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        }

        if (getArg(args, "height") != null) {
            height = Integer.parseInt(getArg(args, "height"));
        }

        if (getArg(args, "server") != null) {
            remoteHost = getArg(args, "server");
        }

        if (getArg(args, "port") != null) {
            thePort = Integer.parseInt(getArg(args, "port"));
        }

        if (getArg(args, "type") != null) {
            theType = getArg(args, "type");
        }
    }


    private String
    getArg(String args[], String arg) {
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
        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        System.out.println(copyright);
        if (!isApplication) {
            showStatus(copyright);
        }

// Layout the screen.

        setLayout(new BorderLayout());
        p = new Panel();
        p.setLayout(new BorderLayout());
        add("North", p);
        Panel buttons = new Panel();
        p.setFont(new Font("Dialog", Font.PLAIN, 16));

        p.add("North", buttons);
        buttons.add(startButton);
        buttons.add(pauseButton);
        buttons.add(musicButton);
        buttons.add(soundsButton);
        p.add("South", messageText);

        startButton.addActionListener(this);
        pauseButton.addActionListener(this);
        musicButton.addActionListener(this);
        soundsButton.addActionListener(this);

        pauseButton.setEnabled(false);
        soundsButton.setEnabled(false);

        try {
            getSound(S_MUSIC,     "sounds/funkymusic.au");
            getSound(S_ATTENTION, "sounds/longbell.au");
            getSound(S_HITRACKET, "sounds/smack.au");
            getSound(S_HITWALL,   "sounds/thud.au");
            getSound(S_IMISSED,   "sounds/ooh.au");
            getSound(S_OMISSED,   "sounds/joy.au");
        } catch (Exception e) {
            musicButton.setEnabled(false);
            soundsButton.setEnabled(false);
        }

// Keep the soundtrack from starting till a game starts.

        stopSound(S_MUSIC);

        if (life == null) {
            life = new Thread(this);
        }
        life.start();

        court = new PpongCourt(this);
        add("Center", court);
        court.init();

        resize(width, height);
        setVisible(true);
    }


    private void
    getSound(int soundType, String fileName) {
        if (isApplication) {
            URL docBase = Ppong.class.getResource("");
            fileName = docBase.toString().substring(5) + "../../" + fileName;
            if (Ppong_Debug) {
                System.err.println("docBase: " + docBase);
                System.err.println("fileName: " + fileName);
            }
            try {
                File theFile = new File(fileName);

                FileInputStream fis = new FileInputStream(theFile);
                AudioStream     as  = new AudioStream(fis);

                if (soundType == S_MUSIC) {
                    sounds[soundType] =
                        new ContinuousAudioDataStream(as.getData());
                } else {
                    sounds[soundType] = new AudioDataStream(as.getData());
                }
            } catch (NullPointerException npe) {
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            sounds[soundType] = getAudioClip(getCodeBase(), fileName);
        }
    }


    void
    playSound(int soundType) {
        boolean play = (soundType == S_MUSIC) ? playMusic : playSounds;

        if (play && sounds[soundType] != null) {
            if (isApplication) {
                AudioPlayer.player.start((AudioDataStream) sounds[soundType]);
            } else {
                if (soundType == S_MUSIC) {
                    ((AudioClip) sounds[soundType]).loop();
                } else {
                    ((AudioClip) sounds[soundType]).play();
                }
            }
        }
    }


    void
    stopSound(int soundType) {
        if (sounds[soundType] != null) {
            if (isApplication) {
                AudioPlayer.player.stop((InputStream) sounds[soundType]);
            } else {
                ((AudioClip) sounds[soundType]).stop();
            }
        }
    }


    public void
    destroy() {
        life = null;
        remove(court);
        court = null;
        game.end();
        game = null;
    }


    public void
    run () {

// Nothing to do.  This applet is Runnable so it can play sounds

    }



    public void
    actionPerformed(ActionEvent event) {
        ToggleButton button = (ToggleButton) event.getSource();

        if (button == startButton) {
            if (button.getLabel().equals("Start a Game")) {
                game = new Game(this, court, remoteHost, thePort, theType);
                game.start();
            } else if (button.getLabel().equals("End Game")) {
                if (Ppong_Debug) {
                    System.err.println("Ending Game 2");
                }
                game.end();
            }
        } else if (button == pauseButton) {
            if (button.getLabel().equals("Pause ")) {
                if (game.gameInProgress) {
                    game.pause();
                    court.send(Message.writeMessage(Message.M_PAUSE));
                }
            } else if (button.getLabel().equals("Resume")) {
                if (game.gameInProgress) {
                    game.resume();
                    court.send(Message.writeMessage(Message.M_RESUME));
                }
            }
            button.toggle();
        } else if (button == musicButton) {
            if (button.getLabel().equals("No Music")) {
                playMusic = false;
            } else if (button.getLabel().equals("Music")) {
                playMusic = true;
            }
            button.toggle();
            stopSound(S_MUSIC);
        } else if (button == soundsButton) {
            if (button.getLabel().equals("No Sound Effects")) {
                playSounds = false;
            } else if (button.getLabel().equals("Sound Effects")) {
                playSounds = true;
            }
            button.toggle();
        }
    }


    public static void
    main(String args[]) {
        Ppong ppong = new Ppong();

        ppong.isApplication = true;
        ppong.getOptions(args);
        ppong.initialize();
        new PpongFrame(ppong);
    }
}


class
PpongFrame extends Frame implements WindowListener {

    final Ppong ppong;

    PpongFrame(Ppong ppong) {
        this.ppong = ppong;

        setTitle("Ppong Player");
        add(ppong);
        addWindowListener(this);
        pack();
        setSize(ppong.width, ppong.height);
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
        ppong.stop();
        System.exit(0);
    }
}
