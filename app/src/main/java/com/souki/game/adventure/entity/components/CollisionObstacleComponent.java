package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.souki.game.adventure.box2d.Shape;

/**
 * Created by vincent on 26/07/2016.
 */

public class CollisionObstacleComponent implements Component {

    public static byte OBSTACLE = 1;
    public static byte MAPINTERACTION = 2;
    public static byte MAPINTERACTION_NOT_OBSTACLE = 4;
    public static byte ITEM = 8;
    public static byte HERO = 16;


    public Shape mShape;
    public ICollisionObstacleHandler mHandler;
    public byte mType;
    public String mName;
    public Object mData;


    public CollisionObstacleComponent() {
    }

    public CollisionObstacleComponent(byte aType, Shape shape, String aName, Object aData, ICollisionObstacleHandler aHandler) {
        mShape = shape;
        mHandler = aHandler;
        mType=aType;
        mName=aName;
        mData = aData;
    }
}
