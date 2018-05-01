package com.souki.game.adventure.interactions;

import com.badlogic.gdx.graphics.Camera;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.map.IMapRendable;

/**
 * Created by vincent on 08/02/2017.
 */

public interface IInteraction extends IMapRendable {
    public static enum Persistence {
        NONE,
        SESSION,
        GAME
    }

    public enum Type {
        NULL,
        HERO,
        CHEST,
        NPC,
        PORTAL,
        MONSTER,
        ACTIVATOR,
        OBSTACLE,
        CHALLENGE
    }


    public void setCamera(Camera aCamera);
    public String getId();
    public float getX();

    public float getY();

    public Type getType();

    public boolean isClickable();

    public boolean isMovable();

    public Persistence getPersistence();

    public void destroy();

    public void update(float dt);

    public Shape getShapeInteraction();

    public void startToInteract();
}
