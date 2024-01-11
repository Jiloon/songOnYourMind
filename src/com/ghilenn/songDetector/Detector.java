package com.ghilenn.songDetector;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.Token;

import static com.ghilenn.songDetector.Display.frame;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

public class Detector {
    //tokens are objects that hold the data for any MIDI event. Every note will have its own token of type NOTE
    Pattern pattern;
    List<Token> tokens;
    String notes;
    File[] songInfos;
    Scanner scanner;

    Detector() {
        //defaults
        pattern = null;
        tokens = null;
        notes = "";

        try {
            //fill the songInfos with a list of all the info files in the database
            songInfos = new File(getClass().getResource("songData/infos").getPath()).listFiles();
        }
        catch(Exception e) {
            //error handling
            JOptionPane.showMessageDialog(frame, "Could not retrieve information from the database");
            System.out.println(e);
        }
    }

    public void readFromFile(File givenMIDI) {
        try {
            //clear notes from any previous data reading so it doesn't just add to it
            notes = "";
            //load the user's MIDI file and convert it to a pattern to be read, then make it into a list of tokens
            //tokens are objects that hold the data for any MIDI event. Every note will have its own token of type NOTE
            pattern = MidiFileManager.loadPatternFromMidi(givenMIDI);
            tokens = pattern.getTokens();

            //go through all the tokens BUT you only want the NOTE type tokens as that's when notes are
            for(int i = 0; i < tokens.size(); i++) {
                if(tokens.get(i).getType() == Token.TokenType.NOTE) {
                    //remove rests
                    if(tokens.get(i).toString().charAt(0) != 'R') {
                        //Only want to obtain the note itself, remove all other data
                        notes += tokens.get(i).toString().charAt(0);
                    }
                }
            }
        }
        catch(Exception e) {
            //error handling
            JOptionPane.showMessageDialog(frame, "There was an issue loading data from the MIDI file");
        }
    }

    public String[] match() throws FileNotFoundException {
        String currentSongMelody = "";
        String[] currentSongInfo = new String[2];

        //making sure its actually a melody. can't compare 2 notes to entire melodies it's unfair.
        if(notes.length() > 3) {
            //going through each song info file
            for (int i = 0; i < songInfos.length; i++) {

                //scan through the file. Each line is another data type. Melody, Song name, Artist
                scanner = new Scanner(songInfos[i]);
                currentSongMelody = scanner.nextLine();
                currentSongInfo[0] = scanner.nextLine();
                currentSongInfo[1] = scanner.nextLine();

                //make sure the melody from the song in the database is longer than the MIDI snippet from the user
                if (currentSongMelody.length() >= notes.length()) {
                    //moving the user's MIDI snippet along through the database song's melody, shifting to the right by 1 each time
                    for (int j = 0; j <= currentSongMelody.length() - notes.length(); j++) {
                        //if it finds a matching melody section, return the song name and artist, as you've found a matching song
                        if (currentSongMelody.regionMatches(j, notes, 0, notes.length())) {
                            return currentSongInfo;
                        }
                    }
                }
            }
        }

        //if it didn't match anything in the database, return and unknown song and artist
        currentSongInfo[0] = "UNKNOWN SONG";
        currentSongInfo[1] = "UNKNOWN ARTIST";

        return currentSongInfo;
    }
}
