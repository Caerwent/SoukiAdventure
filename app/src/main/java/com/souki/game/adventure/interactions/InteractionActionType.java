package com.souki.game.adventure.interactions;

/**
 * Created by vincent on 01/05/2017.
 */

public class InteractionActionType {
    public static enum ActionType {
        DIALOG,
        SET_STATE,
        WAKEUP,
        SLEEP,
        OPEN,
        CLOSE,
        REMOVED,
        LAUNCH_EFFECT,
        ACTIVATE_QUEST,
        SET_PATH,
        SET_PATH_REVERSE,
        SET_LIGHT,
        REMOVE_LIGHT;
    }

    public ActionType type;
    public String value;
    public boolean stopPropagation;
}
