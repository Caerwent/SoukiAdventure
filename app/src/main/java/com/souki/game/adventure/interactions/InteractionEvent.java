package com.souki.game.adventure.interactions;

/**
 * Created by vincent on 13/02/2017.
 */

public class InteractionEvent {
    public static final String THIS = "THIS";

    public static enum EventType {
        STATE,
        END_STATE,
        DIALOG,
        EFFECT_START,
        EFFECT_STOP,
        CHALLENGE_COMPLETED,
        END_PATH,
        COLLISION;
    }

    public String sourceId;
    public String type;
    public String value;
    public boolean isPersistent;
    public boolean isVolatile;
    public boolean isNotValue;

    private boolean mIsPerformed = false;

    public boolean isPerformed() {
        return mIsPerformed;
    }

    public void setPerformed(boolean isPerformed) {
        mIsPerformed = isPerformed;
    }

    public InteractionEvent() {
    }

    public InteractionEvent(String aSourceId, String aType, String aValue) {
        this(aSourceId, aType, aValue, false, false, false);
    }

    public InteractionEvent(String aSourceId, String aType, String aValue, boolean aIsNotValue) {
        this(aSourceId, aType, aValue, aIsNotValue, false, false);
    }

    public InteractionEvent(String aSourceId, String aType, String aValue, boolean aIsNotValue, boolean aIsPersistent) {
        this(aSourceId, aType, aValue, aIsNotValue, aIsPersistent, false );
    }

    public InteractionEvent(String aSourceId, String aType, String aValue, boolean aIsNotValue, boolean aIsPersistent, boolean isVolatile) {
        sourceId = aSourceId;
        type = aType;
        value = aValue;
        isNotValue = aIsNotValue;
        isPersistent = aIsPersistent;
    }

    public InteractionEvent clone() {
        return new InteractionEvent(sourceId, type, value, isNotValue, isPersistent, isVolatile);
    }
}
