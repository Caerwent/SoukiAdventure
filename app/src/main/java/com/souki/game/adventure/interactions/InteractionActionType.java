package com.souki.game.adventure.interactions;

/**
 * Created by vincent on 01/05/2017.
 */

public class InteractionActionType {
    public static enum ActionType {
        DIALOG,
        SET_STATE,
        WAKEUP,
        OPEN,
        CLOSE,
        REMOVED,
        LAUNCH_EFFECT,
        ACTIVATE_QUEST;
    }

    public ActionType type;
    public String value;
}
