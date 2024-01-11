package com.ghilenn.songDetector;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;

public class Display implements ActionListener {
    final JFXPanel jfxPanel = new JFXPanel(); //Will create a warning, is useless. Only here to INITIALIZE JavaFX for Media Player

    //declaring constants
    final int buttonWidth = 200;
    final int buttonHeight = 100;

    //special required objects
    File midiFile;
    MediaPlayer audioPlayer;

    //declaration of GUI parts
    public static JFrame frame; //accessible from other classes for parenting of JOptionPanes
    JLabel logo;
    JPanel pSongData;
    JLabel epCover;
    JLabel tSongName;
    JLabel tArtistName;
    JLabel graphicsLine;
    JPanel pButtons;
    JLabel tChosenFile;
    JLabel tTextChosenFile;
    JButton bBrowse;
    JButton bRecord;
    JButton bStopRecording;
    JButton bDetect;

    //creating more objects (my own)
    MIDIListener midiListener;
    Detector detector;

    //string array to receive artist name and song name from song matching
    String[] songData = new String[2];

    Display() {
        midiListener = new MIDIListener();
        detector = new Detector();

        frame = new JFrame("Song On Your MIND");
        frame.setLayout(null);

        //Adding extra required bits to the window closing event of the app
        frame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    midiListener.close();
                    System.exit(0);
                }
            }
        );

        frame.getContentPane().setBackground(Color.BLACK);

        //setting up the main logo
        logo = new JLabel(new ImageIcon(getClass().getResource("images/logo.jpg")));
        logo.setLocation(0, 0);
        logo.setSize(1000, 300);

        //setting up just a graphically pleasing white line for separation
        graphicsLine = new JLabel(new ImageIcon(getClass().getResource("images/white.jpg")));
        graphicsLine.setSize(400, 2);
        graphicsLine.setLocation(0, 248);

        //setting up the ep or album cover of the song
        epCover = new JLabel(new ImageIcon(getClass().getResource("songData/covers/UNKNOWN SONG.jpg")));
        epCover.setSize(240, 240);
        epCover.setLocation(0, 0);

        //setting up label to display song name
        tSongName = new JLabel("UNKNOWN SONG");
        tSongName.setForeground(Color.WHITE);
        tSongName.setFont(new Font("Arial", Font.BOLD, 40));
        tSongName.setSize(400, 40);
        tSongName.setLocation(0, 255);

        //setting up label to display artist name
        tArtistName = new JLabel("UNKNOWN ARTIST");
        tArtistName.setForeground(Color.WHITE);
        tArtistName.setFont(new Font("Helvetica", Font.PLAIN, 30));
        tArtistName.setSize(400, 40);
        tArtistName.setLocation(0, 295);

        //creating the right panel, holding song data
        pSongData = new JPanel();
        pSongData.setLayout(null);
        pSongData.setBackground(Color.BLACK);
        pSongData.add(graphicsLine);
        pSongData.add(epCover);
        pSongData.add(tSongName);
        pSongData.add(tArtistName);
        pSongData.setBounds(520, 320, 400, 380);

        //setting up chosen file display
        tChosenFile = new JLabel("unknown");
        tChosenFile.setForeground(Color.WHITE);
        tChosenFile.setFont(new Font("Arial", Font.PLAIN, 20));
        tChosenFile.setSize(200, 40);
        tChosenFile.setLocation(buttonWidth + buttonWidth / 10, 40);

        //setting up label for indication of chosen file display
        tTextChosenFile = new JLabel("CURRENT FILE:");
        tTextChosenFile.setForeground(Color.WHITE);
        tTextChosenFile.setFont(new Font("Arial", Font.PLAIN, 20));
        tTextChosenFile.setSize(200, 40);
        tTextChosenFile.setLocation(buttonWidth + buttonWidth / 10, 20);

        //setting up the browse button
        bBrowse = new JButton(new ImageIcon(getClass().getResource("images/browse.jpg")));
        bBrowse.setSize(buttonWidth, buttonHeight);
        bBrowse.setLocation(0, 0);

        //Overriding actionPerformed in a new abstract action for each button allows for independent button activity
        bBrowse.addActionListener(
            new AbstractAction("browse") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //open up a file chooser for the user to select their desired file
                    JFileChooser chosenFile = new JFileChooser();
                    int chosen = chosenFile.showOpenDialog(pButtons);

                    if(chosen == JFileChooser.APPROVE_OPTION) {
                        //make the midiFile that chosen file and display it as the chosen one on the UI
                        midiFile = chosenFile.getSelectedFile();
                        tChosenFile.setText(midiFile.getName());
                    }
                }
            }
        );

        //setting up record button. ************NOT IN USE. UNDER DEVELOPMENT
        bRecord = new JButton(new ImageIcon(getClass().getResource("images/record.jpg")));
        bRecord.setSize(buttonWidth, buttonHeight);
        bRecord.setLocation(0, buttonHeight + buttonHeight / 4);
        bRecord.addActionListener(
            new AbstractAction("record") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    midiListener.record();
                }
            }
        );

        //setting up stop recording button. ****************NOT IN USE. UNDER DEVELOPMENT
        bStopRecording = new JButton(new ImageIcon(getClass().getResource("images/stopRecording.jpg")));
        bStopRecording.setSize(buttonWidth, buttonHeight);
        bStopRecording.setLocation(buttonWidth + buttonWidth / 10, buttonHeight + buttonHeight / 4);
        bStopRecording.addActionListener(
            new AbstractAction("stop recording") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    midiListener.stopRecording();
                }
            }
        );

        //setting up detect button to match played songs with ones in the database
        bDetect = new JButton(new ImageIcon(getClass().getResource("images/detect.jpg")));
        bDetect.setSize(buttonWidth, buttonHeight);
        bDetect.setLocation(buttonWidth / 2 + buttonWidth / 20, (buttonHeight + buttonHeight / 4) * 2);
        bDetect.addActionListener(
            new AbstractAction("detect") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //first get the detector object to read from the user's file and get the required melody
                    detector.readFromFile(midiFile);
                    try {
                        //then try and match that melody to one in the database, returning matching song and artist
                        songData = detector.match();

                        //display the results
                        tSongName.setText(songData[0]);
                        tArtistName.setText(songData[1]);
                        epCover.setIcon(new ImageIcon(getClass().getResource("songData/covers/" + songData[0] + ".jpg")));

                        //and play the song for better user experience and to have a fuller program
                        if(!songData[0].equals("UNKNOWN SONG")) {
                            audioPlayer = new MediaPlayer(new Media(
                                    new File(getClass().getResource(
                                            "songData/songs/" + songData[0].replaceAll("\\s", "") + ".mp3").getPath()).toURI().toString()));
                            audioPlayer.play();
                        }
                        else {
                            if(audioPlayer != null) {
                                audioPlayer.stop();
                            }
                        }
                    }
                    catch (FileNotFoundException ex) {
                        //error handling
                        JOptionPane.showMessageDialog(frame, "Could not find file. Something went wrong!");
                        System.out.println(ex);
                    }
                }
            }
        );

        //creating the left pane, holding the buttons and file settings
        pButtons = new JPanel();
        pButtons.setLayout(null);
        pButtons.setBackground(Color.BLACK);
        pButtons.add(tChosenFile);
        pButtons.add(tTextChosenFile);
        pButtons.add(bBrowse);
        pButtons.add(bRecord);
        pButtons.add(bStopRecording);
        pButtons.add(bDetect);
        pButtons.setBounds(40, 320, buttonWidth * 2 + buttonWidth / 10, buttonHeight * 4);

        //completing the frame
        frame.add(pButtons);
        frame.add(logo);
        frame.add(pSongData);

        frame.setVisible(true);
        frame.setSize(1000, 750);
    }

    public void actionPerformed(ActionEvent e) {
        //regular actionPerformed, unoverridden, testing purposes mostly
        System.out.println("Button Pressed");
    }

}
