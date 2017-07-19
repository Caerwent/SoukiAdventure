package com.souki.game.adventure.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Intersector;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;

/**
 * Created by vincent on 26/07/2016.
 */

public class CollisionObstacleSystem extends IteratingSystem {

    public CollisionObstacleSystem() {
        super(Family.all(CollisionObstacleComponent.class).get());
    }


    @Override
    public void processEntity(Entity entity, float deltaTime) {
        ImmutableArray<Entity> entities = getEntities();

        CollisionObstacleComponent collisionObstacleComponent = entity.getComponent(CollisionObstacleComponent.class);
        if (collisionObstacleComponent.mHandler == null)
            return;
        CollisionObstacleComponent otherCollisionObstacleComponent;
        Intersector.MinimumTranslationVector mvt = new Intersector.MinimumTranslationVector();

        for (Entity otherEntity : entities) {
            if (otherEntity == entity)
                continue;

            otherCollisionObstacleComponent = otherEntity.getComponent(CollisionObstacleComponent.class);
            if (otherCollisionObstacleComponent == null)
                continue;


            if (ShapeUtils.overlaps(collisionObstacleComponent.mShape, otherCollisionObstacleComponent.mShape, mvt)) {
                if (mvt.depth >= Math.sqrt(PathMap.CHECK_RADIUS) && !collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                    collisionObstacleComponent.mHandler.onCollisionObstacleStart(otherCollisionObstacleComponent);
                }
            } else {
                if (collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                    collisionObstacleComponent.mHandler.onCollisionObstacleStop(otherCollisionObstacleComponent);
                }
            }

        }

    }
}
