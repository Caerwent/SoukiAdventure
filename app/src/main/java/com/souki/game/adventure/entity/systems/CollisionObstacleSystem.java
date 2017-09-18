package com.souki.game.adventure.entity.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;

/**
 * Created by vincent on 26/07/2016.
 */

public class CollisionObstacleSystem extends IteratingSystem {

    public CollisionObstacleSystem() {
        super(Family.all(CollisionObstacleComponent.class).get());
        priority = 0;
    }


    @Override
    public void processEntity(Entity entity, float deltaTime) {
        ImmutableArray<Entity> entities = getEntities();

        CollisionObstacleComponent collisionObstacleComponent = entity.getComponent(CollisionObstacleComponent.class);
        if (collisionObstacleComponent==null || collisionObstacleComponent.mHandler == null)
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
                VelocityComponent velocity = entity.getComponent(VelocityComponent.class);
                boolean hasCollisionInTime = false;
                if (velocity != null) {
                    float dx = velocity.x * deltaTime;
                    float dy = velocity.y * deltaTime;
                    Vector2 p1 = new Vector2();
                    Vector2 p2 = new Vector2();
                    Rectangle bounds = collisionObstacleComponent.mShape.getBounds();

                    p1.set(bounds.x, bounds.y);
                    p2.set(bounds.x + dx, bounds.y + dy);
                    if (ShapeUtils.segmentIntersectShape(p1, p2, otherCollisionObstacleComponent.mShape)) {
                        hasCollisionInTime = true;
                    } else {
                        p1.set(bounds.x + bounds.getWidth(), bounds.y);
                        p2.set(bounds.x + bounds.getWidth() + dx, bounds.y + dy);
                        if (ShapeUtils.segmentIntersectShape(p1, p2, otherCollisionObstacleComponent.mShape)) {
                            hasCollisionInTime = true;
                        } else {
                            p1.set(bounds.x, bounds.y + bounds.getHeight());
                            p2.set(bounds.x + dx, bounds.y + bounds.getHeight() + dy);
                            if (ShapeUtils.segmentIntersectShape(p1, p2, otherCollisionObstacleComponent.mShape)) {
                                hasCollisionInTime = true;
                            } else {
                                p1.set(bounds.x + bounds.getWidth(), bounds.y + bounds.getHeight());
                                p2.set(bounds.x + bounds.getWidth() + dx, bounds.y + bounds.getHeight() + dy);
                                if (ShapeUtils.segmentIntersectShape(p1, p2, otherCollisionObstacleComponent.mShape)) {
                                    hasCollisionInTime = true;
                                }
                            }
                        }
                    }
                }

                if (hasCollisionInTime) {
                    if (!collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                        collisionObstacleComponent.mHandler.onCollisionObstacleStart(otherCollisionObstacleComponent);
                    }
                } else if (collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                    collisionObstacleComponent.mHandler.onCollisionObstacleStop(otherCollisionObstacleComponent);
                }
            }

        }

    }
}
