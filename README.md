
# README for JSDT (Java Shared Data Toolkit).

![](https://github.com/richburridge/JSDT/blob/master/Duke.JSDT.gif)

## CONTENTS:

### What is JSDT?

The Shared Data Toolkit for Java Technology (JSDT) implements a multipoint
data delivery service for use in support of highly interactive, collaborative
applications.

It provides the basic abstraction of a session (i.e., a group of objects
associated with some common communications pattern), and supports full-duplex
multipoint communication among an arbitrary number of connected application
entities -- all over a variety of different types of networks

In addition, this toolkit provides efficient support of multicast message
communications. This is accomplished by way of a single send method, and
allows the user to define whether or not uniformly sequenced reception of
data at all receiving locations is required.

The ability to create shared byte arrays and get and set their values is also
provided to session members.

A token-based distributed synchronization mechanism is also provided, which
can be used to ensure mutually exclusive access to a resource, to perform
distributed, multi-application, atomic signalling, etc.

Two implementations are currently provided:

  socket - uses TCP/IP sockets
  http   - uses HTTP protocol.

There is an alias for sending the author/maintainer email related to JSDT.
It is:

  jsdt.interest@gmail.com


### Source distribution directory overview.

The JSDT source distribution consists of the following:

AUTHORS			- the people who wrote JSDT.  
MAINTAINERS		- the people who maintain JSDT.  
NEWS			- the main changes with each new JSDT release.  
README.md		- the file you are reading now.  
ChangeLog		- description of changes made to JSDT.  
ChangeLog-199\<n>	- description of changes made to JSDT in previous years.  
TODO			- the list of known problems, bugs and suggested enhancements.

doc/			- the JSDT documentation.  
doc/api			- the generated JavaDoc for all the JSDT classes.  
doc/userguide		- the User Guide in LibreOffice and PDF format.  
doc/implguide		- the Implementers Guide in LibreOffice and PDF format.

impl/			- various files to help JSDT implementors.  
impl/Authentication	- describes the various messages passed between a client trying to join a managed session and the server and manager for that session.  
impl/Messages		- a description of the messages between the proxies and the server.  
impl/NOTES		- more detailed notes for some of the entries in the TODO file.  

out/production/JSDT/		- the JSDT class files and examples.  

src				- the various JSDT source files.  
src/com/sun/media/jsdt/		- the JSDT API classes and interfaces.  
src/com/sun/media/jsdt/event	- the event/listener JSDT API classes and interfaces.  
src/com/sun/media/jsdt/impl	- the implementation independent JSDT classes and interfaces.  
src/com/sun/media/jsdt/socket	- a socket based implementation of JSDT.  
src/com/sun/media/jsdt/http	- an HTTP implementation of JSDT.  
src/com/sun/media/jsdt/template	- JSDT template files to use, to start to create a new transpoirt implementation.  

src/examples/			- simple examples using JSDT.  
src/examples/chat/		- a chat applet.  
src/examples/whiteboard		- a shared whiteboard applet.  
src/examples/ppong		- two player networked game based on Atari Pong  
src/examples/sound		- a audio file server and receiver applet.  
src/examples/stock		- a stock quote server and viewer.  
src/examples/browser		- a collaborative web browser and server.  
src/examples/phone		- an Internet phone and server.  
src/examples/synth		- a midi keyboard "jam" player and server.  
src/images			- the various images used by the examples.  
src/sounds			- the various sounds used by the examples.  

src/test/	- a test application that exercises the various JSDT methods.


### Building and configuring the distribution.

You will need Java 8 (or later) in order to build JSDT.

The build environment is setup to use IntelliJ IDEA. See their documentation
at:

  https://www.jetbrains.com/idea/documentation/

In particular the section on working with git repositories:

  https://www.jetbrains.com/help/idea/using-git-integration.html

To build JSDT with the IntelliJ IDEA select Build -> Build Project from the
main window menubar.


### Running the example programs.

Each of the examples is setup to recognise certain command line options:

The example servers use the following command line options:

  -server \<string>  - the host name where the server is running.  
  -port   \<integer> - the port number the server is using.  
  -type   \<string>  - the type of JSDT implementation (socket).  

The example user applets use the following param attributes:

  width  \<integer> - the width of the example user applet.  
  height \<integer> - the height of the example user applet.  
  server \<string>  - the host name where the server is running.  
  port   \<integer> - the port number the server is using.  
  type   \<string>  - the type of JSDT implementation (socket).  

The example user applications recognize the following command line options:

  -width  \<integer> - the width of the example user applet.  
  -height \<integer> - the height of the example user applet.  
  -server \<string>  - the host name where the server is running.  
  -port   \<integer> - the port number the server is using.  
  -type   \<string>  - the type of JSDT implementation (socket).  


The examples are setup to use the socket based implementation of JSDT on
the localhost. Should you want something different you will need to adjust
the parameters passed to the server and user applications at startup time.

By default, the servers are setup to run on host "localhost" and use various
port numbers:

CHAT_SERVER_HOST       = localhost  
CHAT_SERVER_PORT       = 4461  

PPONG_SERVER_HOST      = localhost  
PPONG_SERVER_PORT      = 4462  

SOUND_SERVER_HOST      = localhost  
SOUND_SERVER_PORT      = 4463  

STOCK_SERVER_HOST      = localhost  
STOCK_SERVER_PORT      = 4464  

WHITEBOARD_SERVER_HOST = localhost  
WHITEBOARD_SERVER_PORT = 4466  

TEST_SERVER_HOST       = localhost  
TEST_SERVER_PORT       = 4467  
TEST_CLIENT_HOST       = localhost  
TEST_CLIENT_PORT       = 4567  

BROWSER_SERVER_HOST    = localhost  
BROWSER_SERVER_PORT    = 4468  

PHONE_SERVER_HOST      = localhost  
PHONE_SERVER_PORT      = 4469  

SYNTH_SERVER_HOST      = localhost  
SYNTH_SERVER_PORT      = 4470  

REGISTRY_PORT          = 4561  

#### Running the JSDT registry.

The first thing you need to do before you run any of the example programs
is to launch the JSDT registry of the appropriate type.

This can be achieved by:

* Selecting the src/com.sun.media.jsdt/socket/Registry item from the Project
  menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run Registry.main()" from the
  menu (Ctrl-Shift-F10).

#### Running the chat example.

You need to start one copy of the chat server. This is done by:

* Selecting the src/examples/chat/ChatServer item from the Project
  menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run ChatServer.main()" from
  the menu (Ctrl-Shift-F10).

You can have multiple copies of the chat user applet/application running.

To launch a single instance of the application:

* Select the src/examples/chat/ChatUser.java item from the Project
  menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run ChatUser.main()" from
  the menu (Ctrl-Shift-F10).

To launch a single instance of the applet, type:

	% cd $(TOP_DIR)/run/$(PLATFORM)
	% gnumake run-chat-user

#### Using the chat example.

This is a very simple chat program. The applet consists of three parts:

    - a means of setting a name for the user.
    - a scrolling list of messages.
    - a means of sending a new message.

The first thing to do is to set a name for the user. Type the users
name in the upper text field, and press Return. The text field is grayed
out, and the "SignOff" button is activated.

You have now joined the chat session. You can send messages by typing them
in the bottom text field and when you are ready to send them, press Return.
The message will be sent to all users joined to the chat session. It will
appear as the last entry in the scrolling message list.

See the TODO file in this distribution for the list of currently known bugs
and suggested enhancements for this example.


#### Running the ppong example.

You need to start one copy of the ppong server. This is done by:

* Selecting the src/examples/ppong/PpongServer.java item from the Project
  menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run PpongServer.main()" from
  the menu (Ctrl-Shift-F10).

You can have two copies of the ppong user applet/application running.

To launch a single instance of the application:

* Select the src/examples/ppong/Ppong.java item from the Project
  menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run Ppong.main()" from
  the menu (Ctrl-Shift-F10).

To launch a single instance of the applet, type:

        % cd $(TOP_DIR)/run/$(PLATFORM)
        % gnumake run-ppong-user

#### Using the ppong example.

Ppong is a two player network game, based on the classic Atari Pong game.

Press "Start a Game" to begin. If someone else is already waiting to play,
you will start playing with them right away. If there is not a partner
waiting for you, go get a pal to fire up this URL or hang out and wait for
a partner.

You can also fire up another instance of your browser and play against
yourself. Not as easy as you think!

The game plays to 7 or until one of you presses "End Game".
The paddle on the left is your paddle.  To control your paddle, just
move your mouse up and down the screen and it will follow.


#### Running the whiteboard example.

You need to start one copy of the whiteboard server. This is done by:

* Selecting the src/examples/whiteboard/WhiteBoardServer.java item from
  the Project menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run WhiteBoardServer.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the whiteboard user applet/application running.

To launch a single instance of the application:

* Select the src/examples/whiteboard/WhiteBoardUser.java item from the Project
  menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run WhiteBoardUser.main()"
  from the menu (Ctrl-Shift-F10).

To launch a single instance of the applet, type:

	% cd $(TOP_DIR)/run/$(PLATFORM)
	% gnumake run-whiteboard-user

#### Using the whiteboard example.

This is a simple shared whiteboard program. The applet consists of an
upper drawing area and a lower control area.

There are eight different brush sizes in five colors (black, red, blue,
green and yellow). There are five different drawing operations:

  - clearing the drawing area.
  - drawing a dot in the current brush style and color.
  - drawing a line in the current brush style and color.
  - drawing text.
  - drawing a circle in the current brush style and color.

When you do a drawing operation, all whiteboard users will also see the
same operation in their drawing areas.

Select a brush by clicking on the appropriate style in the control panel.

Select a color by clicking on the color in the control panel.

To clear the drawing area, select CLR from the control panel.

To draw a dot, select the brush style and color you want. Then select the
DOT option in the control panel. Then click at the appropriate point in
the drawing area.

To draw a line, select the brush style and color you want. Then select the
LINE option in the control panel. Then click twice in the drawing area to
define the lines endpoints. A line will be drawn between these two points.

To draw text, select the TEXT in the control point. Then click in the drawing
area where you want the text to appear. Then type in the text, hitting the
Return key to end it.

To draw a circle, select the brush style and color you want. Then select the
CIRCLE option in the control panel. Then click twice in the drawing area to
define the center of the circle and the radius. A circle will be drawn using
the given center point and radius.

See the TODO file in this distribution for the list of currently known bugs
and suggested enhancements for this example.


#### Running the sound example.

You need to start one copy of the sound server. This is done by:

* Selecting the src/examples/sound/SoundServer.java item from
  the Project menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run SoundServer.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the sound user applet/application running.

To launch a single instance of the application:

* Select the src/examples/sound/SoundUser.java item from the Project menu
  on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run SoundUser.main()"
  from the menu (Ctrl-Shift-F10).

To launch a single instance of the applet, type:

	% cd $(TOP_DIR)/run/$(PLATFORM)
	% gnumake run-sound-user

#### Using the sound example.

This is a very simple sound server program and corresponding receiver applet.

When a sound user program (the receiver) starts up, there are four buttons
displayed (some of them initially inactive):

    - Connect       used to connect to the sound server
    - Start         used to start the audio file being sent to this receiver.
    - Stop:         used to stop the audio file being sent to this receiver.
    - Disconnect    used to disconnect from the sound server.

The sound server is continuously playing the audio file to interested parties.
When you connect to the server and press start, you are added to that list and
will receive audio packets from the server, which are sent out to the speaker.

Pressing Stop will remove you from this list of interested party, and hence
stop you receiving further audio packets.

The sound example uses an unreliable (UDP) channel to send out the audio data.


#### Running the stock example.

You need to start one copy of the stock server. This is done by:

* Selecting the src/examples/stock/StockServer.java item from
  the Project menu on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run StockServer.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the stock user applet/application running.

To launch a single instance of the application:

* Select the src/examples/stock/StockViewer.java item from the Project menu
  on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run StockViewer.main()"
  from the menu (Ctrl-Shift-F10).

To launch a single instance of the applet, type:

        % cd $(TOP_DIR)/run/$(PLATFORM)
        % gnumake run-stock-user

#### Using the stock example.

The stock viewer will display multiple stock quotes simultaneously, which
are being sent out by the stock server. The stock server is using the
"quote.yahoo.com" web site to get quote information. The stock quotes are
being continuously updated. Each quote also displays news quotes associated
with that stock symbol, which are being continuously scrolled.

The controls to the left of each stock quote news determine the direction
the news scrolls, or whether it's stopped.

Stock symbols can be added with the "Add:" textfield.
Stock symbols can be removed with the "Remove:" textfield.

The "Update" button will get the latest stock information, and restart the
scrolling of any stock quote news.
The "Quit" button terminates the stock viewer.

A stock viewer application can be started with a -stocks command line option
with a set of quotes that should be initially displayed
(eg: -stocks AAPL+GOOGL+MSFT)

The stock viewer applet has a similar "stocks" parameter.


#### Running the browser example.

You need to start one copy of the Teacher (server) application. This is by:

* Selecting the src/examples/browser/Teacher item from the Project menu on
  the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run Teacher.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the Student user application running.

To launch a single instance of the application:

* Select the src/examples/browser/Student item from the Project menu on the
  left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run Student.main()"
  from the menu (Ctrl-Shift-F10).

#### Using the browser example.

The Teacher uses the browser to drive an HTML based presentation. The Students
use the same browser in "slave-mode", to view (and keep in synch with) what
the Teacher is showing.

There are four buttons at the top; "Back", "Forward", "Reload" and "Exit"
to help control the presentation. You can also type new URL's into the
"Location" text field. Currently, only URL's starting with "http:" and "file:"
are recognised.


#### Running the phone example.

You need to start one copy of the phone server. This is done by:

* Selecting the src/examples/phone/PhoneServer item from the Project menu on
  the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run PhoneServer.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the phone user application running.

To launch a single instance of the application:

* Select the src/examples/phone/PhoneUser.java item from the Project menu
  on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run PhoneUser.main()"
  from the menu (Ctrl-Shift-F10).

Note that each instance must be on a different machine. JavaSound can
only open the Mixer once per machine.

#### Using the phone example.

This example provides an audio phone connection between two or more computers
over the network.

When the PhoneUser application starts up, enter your name in the text field
at the top and press return. You will be added to the phone conference, and
you will see a list of the current callers.

JavaSound will automatically open the input channel (ie. microphone) and
output channel (ie. speakers) and you can start your phone call.


#### Running the synth example.

You need to start one copy of the MidiServer server. This is done by:

* Selecting the src/examples/synth/MidiServer item from the Project menu on
  the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run MidiServer.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the MidiSynth user application running.

To launch a single instance of the application:

* Select the src/examples/synth/MidiSynth.java item from the Project menu
  on the left side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run MidiSynth.main()"
  from the menu (Ctrl-Shift-F10).

Note that each instance must be on a different machine. JavaSound can
only open the Mixer once per machine.


#### Using the synth example.

Each user of the MidiSynth application will automatically be started on a
different Midi Channel. This will allow you to "jam" with the other users,
selecting different instruments, keyboard notes etc...


#### Running the test environment.
-----------------------------------

You need to start one copy of the test environment server. This is by:

* Selecting the src/test/TestServer item from the Project menu on the left
  side of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run TestServer.main()"
  from the menu (Ctrl-Shift-F10).

You can have multiple copies of the test environment user applications
running. To launch a single instance:

* Select the src/test/TestUser item from the Project menu on the left side
  of the IntelliJ IDEA window.
* Right mouse clicking on this item and select "Run TestUser.main()"
  from the menu (Ctrl-Shift-F10).

The test environment will perform a variety of method calls that are designed
to exercise the majority of the JSDT implementation.


### Added new transport implementations.

The type of the Session is dynamically loaded depending upon the URL
given. The URL definition for JSDT now looks like this:

  Session:     jsdt://\<host>:\<port>/\<type>/Session/\<sessionName>  
  Client:      jsdt://\<host>:\<port>/\<type>/Client/\<clientName>

where \<type> currently can be:

  socket    - TCP socket based.  
  http      - HTTP based.

The class files associated with a particular implementation are now found
in the \<type> subdirectory under the $(TOP_DIR)/src/share/com/sun/media/jsdt
directory.


### Known problems and limitations.

* Date priorities are ignored (both implementations).

* No unreliable channels (http implementation).

* Cannot handle Data messages greater than 8 Kbytes on unreliable (UDP)
  Channels (socket implementation).


### Acknowledgements.

This toolkit has borrowed text, ideas, and definitions from the ITU T.122
recommendation for Multipoint Communication Service for Audiographics and
Audiovisual Conferencing Services Definition.

It's definition has received input from a lot of people to whom I'm very
grateful. These include:

Duane Northcutt, Alan Ruberg, Randall Smith, Bo Begole, Craig Struble,
Ed Grossman, Trevor Morris, Tom Rodriguez, Brian Knep, Daniel Enting,
Christopher Nicholas, John Patterson, Eduardo Pelegri-Llopart, Rolande Kendal,
Kevin Solie, Justin Couch, Haam Tham, Jeff Kesselman and various members of
the ShowMe team at SMCC.

Suggestions for further improvement would be most welcome, plus bug reports
and comments.

## Copyright

Copyright (c) 1996-2005 Sun Microsystems, Inc.  All Rights Reserved.
