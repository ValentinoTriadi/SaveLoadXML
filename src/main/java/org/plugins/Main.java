package org.plugins;

public class Main {
    public static void main(String[] args) {
        SaveLoadXML saveLoadXML = new SaveLoadXML("state");
        saveLoadXML.loadGameState();
        saveLoadXML.loadPlayer(1);
        saveLoadXML.loadPlayer(2);

        saveLoadXML = new SaveLoadXML("SAVE");
        saveLoadXML.save();
    }
}