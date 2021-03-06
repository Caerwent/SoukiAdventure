package com.souki.game.adventure.entity.components;

import com.badlogic.gdx.utils.Array;

/**
 * Created by vincent on 26/07/2016.
 */

public interface ICollisionObstacleHandler {
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction);

    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity);

    public Array<CollisionObstacleComponent> getCollisionObstacle();
}
