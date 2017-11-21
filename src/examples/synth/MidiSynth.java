
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

package examples.synth;

import java.awt.*;
import java.awt.event.*;
import java.applet.Applet;
import javax.sound.midi.*;

/**
 * Collaborative Midi Synthesiser jam session using JavaSound and JSDT.
 *
 * @version     2.3 - 21st November 2017
 * @author      Kara Kytle
 * @author      Rich Burridge
 */

public class
MidiSynth extends Applet
          implements MouseListener, KeyListener, SynthDebugFlags {

    private static final int VOLUME_CONTROLLER       = 7;
    private static final int PAN_CONTROLLER          = 10;
    private static final int SUSTAIN_CONTROLLER      = 64;
    private static final int REVERB_LEVEL_CONTROLLER = 91;

    private Synthesizer synth     = null;
    private Soundbank   soundbank = null;
    private MidiChannel midiChannels[];
    private Instrument  instruments[];
    Share       share;

    // Are we running as an application (as opposed to an applet)?
    private boolean isApplication = false;

    private final int transpose     = 24;
    private int lastKeyNumber = 0;
    private int channelNumber = 0;
    private int velocity      = 64;

    private boolean roll = false;    // True to play notes on mouseover.

    private final Label instrumentLabel = new Label("                                                  ");
    private final Label velocityLabel = new Label("64   ");
    private final Label pressureLabel = new Label("64   ");
    private final Label bendLabel     = new Label("64   ");
    private final Label volLabel      = new Label("100  ");
    private final Label panLabel      = new Label("64   ");
    private final Label revLabel      = new Label("64   ");

    private Scrollbar pressureScrollbar = null;
    private Scrollbar bendScrollbar     = null;
    private Scrollbar volScrollbar      = null;
    private Scrollbar panScrollbar      = null;
    private Scrollbar revScrollbar      = null;

    private Checkbox muteCheckbox    = null;
    private Checkbox monoCheckbox    = null;
    private Checkbox soloCheckbox    = null;
    private Checkbox sustainCheckbox = null;
    private Checkbox rollCheckbox    = null;

    private static final Choice instr[] = new Choice [8];

    private final Panel keyboardPanel     = new Panel();

    private final boolean singleNoteMode = true;

// Default setup, will mostly be overriden by attributes.

    int width  = 760;
    int height = 400;

    private String  hostname    = "localhost";
    private int     hostport    = 4470;
    private String  sessionType = "socket";


    private void
    getOptions(String args[]) {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: getOptions:");
            for (int i = 0; i < args.length ; i++) {
                System.err.println("args[" + i + "]: " + args[i]);
            }
        }

        if (getArg(args, "width") != null) {
            width = Integer.parseInt(getArg(args, "width"));
        } else {
            width = 760;
        }

        if (getArg(args, "height") != null) {
            height = Integer.parseInt(getArg(args, "height"));
        } else {
            height = 400;
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
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: getArg:" +
                                " arg: " + arg);
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
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: init.");
        }

        getOptions(null);
        initialize();
    }


    private void
    initialize() {
        Button quitButton, soundOffButton;
        Choice channelChoice;
        Panel instrumentsPanel, instrumentsPanel1;
        Panel instrumentsPanel2, instrumentsPanel3;
        Scrollbar velocityScrollbar;

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: initialize.");
        }

// Create the Synthesizer and get our MidiChannels.

        try {
            synth = MidiSystem.getSynthesizer();
        } catch (MidiUnavailableException mue) {
            System.out.println("Could not get default synthesizer");
            System.exit(1);
        }

// Open synthesizer.

        try {
            synth.open();
        } catch (MidiUnavailableException e) {
            System.out.println("Could not open default synthesizer");
            System.exit(1);
        }

// Load the soundbank.

        try {
            soundbank = synth.getDefaultSoundbank();
        } catch (Exception e) {
            System.out.println("Exception getting soundbank url: " + e);
            System.exit(1);
        }

// Populate the instrument choice boxes.

        instruments = soundbank.getInstruments();
        int index = 0;
        for (int i = 0; i < 8; i++) {
            instr[i] = new Choice();
            for (int j = 0; j < 16; j++) {
                instr[i].add(instruments[index].getName());
                index++;
            }
        }

// Load the first instrument.

        synth.loadInstrument(instruments[0]);
        midiChannels = synth.getChannels();

        share = new Share(sessionType, hostname, hostport,
                          synth, instruments, midiChannels);

        channelNumber = share.getNoUsers() - 1;

        instrumentLabel.setText("Instrument: " +
            instruments[midiChannels[channelNumber].getProgram()].getName());

// Setup the GUI.

        setLayout(new GridLayout(4, 1));
        setBackground(Color.lightGray);

// Keyboard panel.

        keyboardPanel.setLayout(null);     // Absolutely no layout.

// Create the 6 octaves.

        for (int i = 0; i < 6; i++) {

// Black key first.

            for (int j = 0; j < 6; j++) {
                if (j == 2) {
                    continue;     // There's no E# in black.
                }

                int keyOffset = 1;

                switch (j) {
                    case 0:
                        keyOffset = 1;
                        break;
                    case 1:
                        keyOffset = 3;
                        break;
                    case 3:
                        keyOffset = 6;
                        break;
                    case 4:
                        keyOffset = 8;
                        break;
                    case 5:
                        keyOffset = 10;
                        break;
                }
                PianoKey k = new PianoKey(i*12 + keyOffset, false);
                k.setBounds(X(i*28 + j*4 + 3), Y(), D(2), D(11));
                k.addMouseListener(this);
                keyboardPanel.add(k);
            }

            for (int j = 0; j < 7; j++) {
                int keyOffset = 1;

                switch (j) {
                    case 0:
                        keyOffset = 0;
                        break;
                    case 1:
                        keyOffset = 2;
                        break;
                    case 2:
                        keyOffset = 4;
                        break;
                    case 3:
                        keyOffset = 5;
                        break;
                    case 4:
                        keyOffset = 7;
                        break;
                    case 5:
                        keyOffset = 9;
                        break;
                    case 6:
                        keyOffset = 11;
                        break;
                }

                PianoKey k = new PianoKey(i*12 + keyOffset, true);
                k.setBounds(X(i*28 + j*4), Y(), D(4), D(20));
                k.addMouseListener(this);

                keyboardPanel.add(k);
            }
        }

        keyboardPanel.addKeyListener(this);
        add(keyboardPanel);


// Instruments panel.

        instrumentsPanel  = new Panel(new GridLayout(3,1));
        instrumentsPanel1 = new Panel(new FlowLayout());
        instrumentsPanel2 = new Panel(new GridLayout(1,4));
        instrumentsPanel3 = new Panel(new GridLayout(1,4));

        for (int i = 0; i < 8; i++) {
            instr[i].addItemListener(new ProgramChange(this));
        }

        channelChoice = new Choice();
        for (int i = 0;i < 16; i++) {
            channelChoice.add(Integer.toString(i+1));
        }
        channelChoice.addItemListener(new ChannelChange(this));

        instrumentsPanel1.add(instrumentLabel);
        instrumentsPanel1.add(new Label("Channel: "));
        instrumentsPanel1.add(channelChoice);

        instrumentsPanel2.add(instr[0]);
        instrumentsPanel2.add(instr[1]);
        instrumentsPanel2.add(instr[2]);
        instrumentsPanel2.add(instr[3]);
        instrumentsPanel3.add(instr[4]);
        instrumentsPanel3.add(instr[5]);
        instrumentsPanel3.add(instr[6]);
        instrumentsPanel3.add(instr[7]);

        instrumentsPanel.add(instrumentsPanel1);
        instrumentsPanel.add(instrumentsPanel2);
        instrumentsPanel.add(instrumentsPanel3);

        instrumentsPanel.validate();

        instrumentsPanel.addKeyListener(this);
        add(instrumentsPanel);

        Panel p = new Panel(new FlowLayout());
        Panel p1 = new Panel(new FlowLayout());

        velocityScrollbar = new Scrollbar(Scrollbar.VERTICAL, -64, 1, -127, 0);
        velocityScrollbar.addAdjustmentListener(
            (AdjustmentEvent e) -> {
                velocity = -((Scrollbar)e.getSource()).getValue();
                velocityLabel.setText(Integer.toString(velocity));
            }
        );

        pressureScrollbar = new Scrollbar(Scrollbar.VERTICAL, -64, 1, -127, 0);
        pressureScrollbar.addAdjustmentListener(
            (AdjustmentEvent e) -> {
                int pressure = -((Scrollbar) e.getSource()).getValue();
                pressureLabel.setText(Integer.toString(pressure));
                share.setChannelPressure(channelNumber, pressure);
            }
        );

        bendScrollbar = new Scrollbar(Scrollbar.VERTICAL, -64, 1, -127, 0);
        bendScrollbar.addAdjustmentListener(
            (AdjustmentEvent e) -> {
                int bend = -((Scrollbar) e.getSource()).getValue();
                bendLabel.setText(Integer.toString(bend));
                share.setPitchBend(channelNumber, bend);
            }
        );

        volScrollbar = new Scrollbar(Scrollbar.VERTICAL, -100, 1, -127, 0);
        volScrollbar.addAdjustmentListener(
            (AdjustmentEvent e) -> {
                int vol = -((Scrollbar) e.getSource()).getValue();
                volLabel.setText(Integer.toString(vol));
                share.controlChange(channelNumber, VOLUME_CONTROLLER, vol);
            }
        );

        panScrollbar = new Scrollbar(Scrollbar.VERTICAL, -64, 1, -127, 0);
        panScrollbar.addAdjustmentListener(
            (AdjustmentEvent e) -> {
                int pan = -((Scrollbar) e.getSource()).getValue();
                panLabel.setText(Integer.toString(pan));
                share.controlChange(channelNumber, PAN_CONTROLLER, pan);
            }
        );

        revScrollbar = new Scrollbar(Scrollbar.VERTICAL, -64, 1, -127, 0);
        revScrollbar.addAdjustmentListener(
            (AdjustmentEvent e) -> {
                int rev = -((Scrollbar) e.getSource()).getValue();
                revLabel.setText(Integer.toString(rev));
                share.controlChange(channelNumber,
                                    REVERB_LEVEL_CONTROLLER, rev);
            }
        );

        muteCheckbox = new Checkbox("Mute", false);
        muteCheckbox.addItemListener(
            (ItemEvent e) ->
                share.setMute(channelNumber, muteCheckbox.getState())
        );

        monoCheckbox = new Checkbox("Mono", false);
        monoCheckbox.addItemListener(
            (ItemEvent e) ->
                share.setMono(channelNumber, monoCheckbox.getState())
        );

        soloCheckbox = new Checkbox("Solo", false);
        soloCheckbox.addItemListener(
            (ItemEvent e) ->
                share.setSolo(channelNumber, soloCheckbox.getState())
        );

        sustainCheckbox = new Checkbox("Sustain", false);
        sustainCheckbox.addItemListener(
            (ItemEvent e) ->
                share.controlChange(channelNumber, SUSTAIN_CONTROLLER,
                             sustainCheckbox.getState() ? 127 : 0)
        );

        rollCheckbox = new Checkbox("Roll", false);
        rollCheckbox.addItemListener(
            (ItemEvent e) -> roll = rollCheckbox.getState()
        );

        soundOffButton = new Button("Sound OFF");
        soundOffButton.setBackground(Color.yellow);
        soundOffButton.addActionListener(
            (ActionEvent e) -> {
                for (int i = 0; i < 16; i++)
                    share.allNotesOff(i);
            }
        );

        quitButton = new Button("QUIT");
        quitButton.setBackground(Color.red);

        quitButton.addActionListener(
            (ActionEvent e) -> System.exit(0)
        );

        p.add(new Label("Ch Vol:"));
        p.add(volScrollbar);
        p.add(volLabel);
        p.add(new Label("Ch Pan:"));
        p.add(panScrollbar);
        p.add(panLabel);
        p.add(new Label("Ch Rev:"));
        p.add(revScrollbar);
        p.add(revLabel);
        p.add(new Label("Ch Bend:"));
        p.add(bendScrollbar);
        p.add(bendLabel);
        p.add(new Label("Vel:"));
        p.add(velocityScrollbar);
        p.add(velocityLabel);
        p.add(new Label("Press:"));
        p.add(pressureScrollbar);
        p.add(pressureLabel);

        p1.add(rollCheckbox);
        p1.add(sustainCheckbox);
        p1.add(muteCheckbox);
        p1.add(monoCheckbox);
        p1.add(soloCheckbox);
        p1.add(soundOffButton);
        p1.add(quitButton);

        add(p);
        add(p1);

        resize(width, height);
        setVisible(true);

        updateGUIStatus();

        p.addKeyListener(this);
        p1.addKeyListener(this);
        addKeyListener(this);
        p.addMouseListener(this);
        p1.addMouseListener(this);
        addMouseListener(this);
        requestFocus();
    }


    public void
    destroy() {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: destroy.");
        }

        share.disconnect();
    }


    private int
    X(int x) {
        final int xOffset = 40;

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: X:" +
                               " x: " + x);
        }

        return(x*4 + xOffset);
    }


    private int
    Y() {
        final int yOffset = 10;

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: Y:");
        }

        return(yOffset);
    }


    private int
    D(int x) {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: D:" +
                               " x: " + x);
        }

        return(x*4);
    }


    private void
    updateGUIStatus() {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: updateGUIStatus.");
        }

// Update all the GUI fields to reflect the status of the current channel.

        int temp;

// Volume.

        temp = midiChannels[channelNumber].getController(VOLUME_CONTROLLER);
        volScrollbar.setValue(-temp);
        volLabel.setText(Integer.toString(temp));

// Pan.

        temp = midiChannels[channelNumber].getController(PAN_CONTROLLER);
        panScrollbar.setValue(-temp);
        panLabel.setText(Integer.toString(temp));

// Pitch bend.

        temp = midiChannels[channelNumber].getPitchBend();
        bendScrollbar.setValue(-temp);
        bendLabel.setText(Integer.toString(temp));

// Reverb.

        temp = midiChannels[channelNumber].getController(REVERB_LEVEL_CONTROLLER);
        revScrollbar.setValue(-temp);
        revLabel.setText(Integer.toString(temp));

// Pressure.

        temp = midiChannels[channelNumber].getChannelPressure();
        pressureScrollbar.setValue(-temp);
        pressureLabel.setText(Integer.toString(temp));

// Mute.

        muteCheckbox.setState(midiChannels[channelNumber].getMute());

// Sustain.

        sustainCheckbox.setState(
            midiChannels[channelNumber].getController(SUSTAIN_CONTROLLER) != 0);

// Mono.

        monoCheckbox.setState(midiChannels[channelNumber].getMono());

// Solo.

        soloCheckbox.setState(midiChannels[channelNumber].getSolo());
    }


    public void
    mouseClicked(MouseEvent e) {
    }


    public void
    mousePressed(MouseEvent e) {
        Component component = e.getComponent();

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: mousePressed:" +
                               " event: " + e);
        }


        if (!(component instanceof Label)) {
            return;
        }

        int keyNumber = ((PianoKey) component).keyNumber + transpose;

        if (singleNoteMode) {
            lastKeyNumber = keyNumber;
            share.noteOn(channelNumber, keyNumber, velocity);
        }
    }


    public void
    mouseReleased(MouseEvent e) {
        Component component = e.getComponent();

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: mouseReleased:" +
                               " event: " + e);
        }

        if (!(component instanceof Label)) {
            return;
        }

        if (singleNoteMode) {
            share.noteOff(channelNumber, lastKeyNumber, velocity);
        }
    }


    public void
    mouseEntered(MouseEvent e) {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: mouseEntered:" +
                               " event: " + e);
        }

        requestFocus();

        if (roll) {
            Component component = e.getComponent();

            if (!(component instanceof Label)) {
                return;
            }

            int keyNumber = ((PianoKey) component).keyNumber + transpose;

            if (singleNoteMode) {
                lastKeyNumber = keyNumber;
                midiChannels[channelNumber].noteOn( keyNumber, velocity);
            }
        }
    }


    public void
    mouseExited(MouseEvent e) {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: mouseExited:" +
                               " event: " + e);
        }

        if (roll) {
            Component component = e.getComponent();

            if (!(component instanceof Label)) {
                return;
            }

            if (singleNoteMode) {
                midiChannels[channelNumber].noteOff( lastKeyNumber, velocity);
            }
        }
    }


// Key listener methods.

    public void
    keyTyped(KeyEvent e) {
    }


    public void
    keyPressed(KeyEvent e) {
        char keyChar   = e.getKeyChar();
        int  keyNumber = getKeyNumber(keyChar);

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: keyPressed:" +
                               " event: " + e);
        }

        if (keyNumber == -1) {
            return;
        }

        keyNumber += transpose;

        if (singleNoteMode) {
            midiChannels[channelNumber].noteOn( keyNumber, velocity);
        }
    }


    public void
    keyReleased(KeyEvent e) {
        char keyChar   = e.getKeyChar();
        int  keyNumber = getKeyNumber(keyChar);

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: keyReleased:" +
                               " event: " + e);
        }

        if (keyNumber == -1) {
            return;
        }

        keyNumber += transpose;

        if (singleNoteMode) {
            midiChannels[channelNumber].noteOff( keyNumber, velocity);
        }
    }


    private int
    getKeyNumber(char keyChar) {
        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: getKeyNumber:" +
                               " key char: " + keyChar);
        }

        if (keyChar == 'a') {
            return(60 - 24);
        }
        if (keyChar == 'w') {
            return(61 - 24);
        }

        if (keyChar == 's') {
            return(62 - 24);
        }
        if (keyChar == 'e') {
            return(63 - 24);
        }

        if (keyChar == 'd') {
            return(64 - 24);
        }

        if (keyChar == 'f') {
            return(65 - 24);
        }
        if (keyChar == 't') {
            return(66 - 24);
        }

        if (keyChar == 'g') {
            return(67 - 24);
        }
        if (keyChar == 'y') {
            return(68 - 24);
        }

        if (keyChar == 'h') {
            return(69 - 24);
        }
        if (keyChar == 'u') {
            return(70 - 24);
        }

        if (keyChar == 'j') {
            return(71 - 24);
        }

        if (keyChar == 'k') {
            return(72 - 24);
        }

        return(-1);
    }


    class
    ProgramChange implements ItemListener {

        MidiSynth ts = null;

        public
        ProgramChange(MidiSynth ts) {
            if (ProgramChange_Debug) {
                System.err.println("ProgramChange: constructor:" +
                                   " midi synth: " + ts);
            }

            this.ts = ts;
        }


        public void
        itemStateChanged(ItemEvent e) {
            Object component = e.getSource();

            if (ProgramChange_Debug) {
                System.err.println("ProgramChange: itemStateChanged:" +
                                   " event: " + e);
            }

            if (!(component instanceof Choice)) {
                return;
            }

            Choice c     = (Choice) component;
            int    index = 0;

            if (c.equals(instr[0])) {
                index = c.getSelectedIndex();
            } else if (c.equals(instr[1])) {
                index = c.getSelectedIndex() + 16;
            } else if (c.equals(instr[2])) {
                index = c.getSelectedIndex() + 32;
            } else if (c.equals(instr[3])) {
                index = c.getSelectedIndex() + 48;
            } else if (c.equals(instr[4])) {
                index = c.getSelectedIndex() + 64;
            } else if (c.equals(instr[5])) {
                index = c.getSelectedIndex() + 80;
            } else if (c.equals(instr[6])) {
                index = c.getSelectedIndex() + 96;
            } else if (c.equals(instr[7])) {
                index = c.getSelectedIndex() + 112;
            }

            share.loadInstrument(index);
            share.programChange(channelNumber, index);

            instrumentLabel.setText("Instrument: " +
                                    instruments[index].getName());

            ts.validate();
        }
    }


    class
    ChannelChange implements ItemListener {

        final MidiSynth ts;

        public
        ChannelChange(MidiSynth ts) {
            if (ChannelChange_Debug) {
                System.err.println("ChannelChange: constructor:" +
                                   " midi synth: " + ts);
            }

            this.ts = ts;
        }


        public void
        itemStateChanged(ItemEvent e) {
            Object component = e.getSource();

            if (ChannelChange_Debug) {
                System.err.println("ChannelChange: itemStateChanged:" +
                                   " event: " + e);
            }

            if (!(component instanceof Choice)) {
                return;
            }

            Choice c = (Choice) component;

// Update the channel.

            channelNumber = c.getSelectedIndex();

// Update GUI fields for status of this channel.

            instrumentLabel.setText("Instrument: " +
              instruments[midiChannels[channelNumber].getProgram()].getName());

            ts.updateGUIStatus();
            ts.validate();
        }
    }


    class
    PianoKey extends Label {

        public final int keyNumber;

        PianoKey(int keyNumber, boolean white) {
            if (ChannelChange_Debug) {
                System.err.println("ChannelChange: constructor:" +
                                   " key number: " + keyNumber +
                                   " white: "      + white);
            }

            this.keyNumber = keyNumber;
            setBackground((white ? Color.white : Color.black));
        }


        public void
        paint(Graphics g) {
            if (ChannelChange_Debug) {
                System.err.println("ChannelChange: paint:" +
                                   " graphics: " + g);
            }

            g.drawRect(0, 0, getSize().width - 1, getSize().height -1);
        }
    }


    public static void
    main(String args[]) {
        MidiSynth midiSynth = new MidiSynth();

        if (MidiSynth_Debug) {
            System.err.println("MidiSynth: main.");
        }

        midiSynth.isApplication = true;
        midiSynth.getOptions(args);
        midiSynth.initialize();
        new MidiSynthUserFrame(midiSynth);
    }
}


class
MidiSynthUserFrame extends Frame implements WindowListener, SynthDebugFlags {

    private final MidiSynth midiSynth;

    MidiSynthUserFrame(MidiSynth midiSynth) {
        if (MidiSynthUserFrame_Debug) {
            System.err.println("MidiSynthUserFrame: constructor.");
        }

        this.midiSynth = midiSynth;

        setTitle("General MIDI");
        add(midiSynth);
        addWindowListener(this);
        pack();
        setSize(midiSynth.width, midiSynth.height);
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
        if (MidiSynthUserFrame_Debug) {
            System.err.println("MidiSynthUserFrame: windowClosing.");
        }

        midiSynth.share.disconnect();
        System.exit(0);
    }
}
