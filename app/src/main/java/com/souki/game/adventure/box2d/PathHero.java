package com.souki.game.adventure.box2d;


import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.PathComponent;

/**
 * Created by vincent on 20/07/2016.
 */

public class PathHero extends PathMap {
      static public final float CHECK_RADIUS = 0.01f;
    static public final float HERO_VELOCITY = 3;
    Entity entity;
    float mOffset;

    public PathHero()
    {
        this(0);
    }
    public PathHero(float aOffset) {
      super("path_hero");
        mOffset = aOffset;
        setLoop(false);
        setRevert(false);
        setVelocityCte(HERO_VELOCITY);
        entity = new Entity();
        entity.add(new PathComponent(this));
        EntityEngine.getInstance().addEntity(entity);
    }

    public PathHero(PathMap aPath)
    {
        super("path_hero");
        positions=aPath.positions;
        mVelocity=aPath.mVelocity;
        currentPointIndex=aPath.currentPointIndex;
        nextPointIndex=aPath.nextPointIndex;
        mIsCompleted=aPath.mIsCompleted;

        isRevert=aPath.isRevert;
        isLoop=aPath.isLoop;
        lastTime=aPath.lastTime;

    }


    public void destroy() {
        if (entity != null) {
            EntityEngine.getInstance().removeEntity(entity);
            entity = null;
        }
    }

    public void render(ShapeRenderer renderer) {
        for (int i = currentPointIndex; i < positions.size() - 1; i++) {
            Vector2 pointStart = positions.get(i);
            Vector2 pointEnd = positions.get(i + 1);
            renderer.line(pointStart.x+mOffset, pointStart.y, 0, pointEnd.x+mOffset, pointEnd.y, 0, Color.YELLOW, Color.YELLOW);
        }
    }
}
