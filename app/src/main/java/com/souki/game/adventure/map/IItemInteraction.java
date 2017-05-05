package com.souki.game.adventure.map;

/**
 * Created by gwalarn on 13/11/16.
 */

public interface IItemInteraction {
    public enum Type {
        ITEM
    }


    public float getX();

    public float getY();

    public Type getInteractionType();

    public void destroy();

}
