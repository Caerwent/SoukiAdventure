package com.souki.game.adventure.dialogs;

import java.util.ArrayList;

/**
 * Created by vincent on 15/03/2017.
 */

public class GameDialogStep {
    public String speaker;
    public ArrayList<String> phrases;

    public GameDialogStep clone() {
        GameDialogStep clone = new GameDialogStep();
        if (speaker != null) {
            clone.speaker = new String(speaker);
        }
        if (phrases != null) {
            clone.phrases = new ArrayList<>();
            for (String phrase : phrases) {
                clone.phrases.add(phrase);
            }
        }
        return clone;
    }
}
