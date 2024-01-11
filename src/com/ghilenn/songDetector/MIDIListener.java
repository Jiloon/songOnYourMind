package com.ghilenn.songDetector;

import org.jfugue.devices.MusicTransmitterToSequence;
import org.jfugue.midi.*;

import static com.ghilenn.songDetector.Display.frame;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.swing.*;
import java.io.File;

/*

WORK IN PROGRESS. NOT IN USE. UNDER DEVELOPMENT

 */
public class MIDIListener {
    MidiDevice device;
    MusicTransmitterToSequence transmitter;
    Sequence sequence;
    String fileName;
    String[] deviceList;
    String deviceName;

    MIDIListener() {
        //getting the list of available MIDI devices
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        deviceList = new String[infos.length];

        //filling array string with names of devices to be displayed in dropdown joptionpane
        for(int i = 0; i < infos.length; i++) {
            deviceList[i] = infos[i].getName();
        }

        //user can select a midi device from available options
        deviceName = (String) JOptionPane.showInputDialog(
                frame,
                "Select a MIDI device to play from:",
                "MIDI Device Selection",
                JOptionPane.QUESTION_MESSAGE,
                null,
                deviceList,
                deviceList[0]
        );

        //if the device name the user selected matches the midi device's name, pick it
        for(int i = 0; i < infos.length; i++) {
            if(infos[i].getName().equals(deviceName)) {
                try {
                    //and set the device to the one the user chose
                    device = MidiSystem.getMidiDevice(infos[i]);
                    //System.out.println(device.isOpen());
                    device.open();
                    //System.out.println(device.isOpen());
                    //System.out.println(device.getMaxTransmitters());
                    //System.out.println(device.getTransmitters().size());
                    //device.getTransmitter();
                    //System.out.println(device.getDeviceInfo());
                    //if it finds the device, break out
                    break;
                }
                catch(Exception e) {
                    //error handling
                    System.out.println(e);
                }
            }
        }

        try {
            //set the transmitter to sequence pointing to the chosen device
            transmitter = new MusicTransmitterToSequence(device);
        }
        catch(Exception e) {
            //error handling
            JOptionPane.showMessageDialog(frame, "Cannot communicate with this device");
            System.out.println(e);
        }

    }

    public void record() {
        try {
            //get the transmitter to sequence to start listening to the chosen device
            transmitter.startListening();
            System.out.println("Started listening");
        }
        catch(Exception e) {
            //error handling
            JOptionPane.showMessageDialog(frame, "Something went wrong!");
            System.out.println(e);
        }
    }

    public void stopRecording() {
        try {
            //stop the sequence and get that inputted
            transmitter.stopListening();
            sequence = transmitter.getSequence();

            //user can choose file name, then save the sequence to a MIDI file
            fileName = JOptionPane.showInputDialog(frame, "File name:");
            MidiFileManager.save(sequence, new File(fileName + ".mid"));
        }
        catch(Exception e) {
            //error handling
            JOptionPane.showMessageDialog(frame, "File could not be saved");
            System.out.println(e);
        }
    }

    public void close() {
        //closing devices
        if(device != null) {
            device.close();
        }
    }

}