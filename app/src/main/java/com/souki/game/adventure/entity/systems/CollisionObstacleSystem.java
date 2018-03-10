package com.souki.game.adventure.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by vincent on 26/07/2016.
 */

public class CollisionObstacleSystem extends EntitySystem {

    private Entity[] entities;
    private ImmutableArray<Entity> mImmutablesEntities;

    private Comparator<Entity> velocityComparator = new Comparator<Entity>() {
        @Override
        public int compare(Entity lhs, Entity rhs) {
            VelocityComponent lhsvelocity = lhs.getComponent(VelocityComponent.class);
            VelocityComponent rhsvelocity = rhs.getComponent(VelocityComponent.class);

            if (lhsvelocity == null && rhsvelocity == null) {
                return 0;
            } else if (lhsvelocity == null) {
                return -1;
            } else if (rhsvelocity == null) {
                return 1;
            } else {
                return rhsvelocity.x * rhsvelocity.x + rhsvelocity.y * rhsvelocity.y < lhsvelocity.x * rhsvelocity.x + lhsvelocity.y * rhsvelocity.y ? 1 : -1;
            }

        }
    };

    private ComponentMapper<CollisionObstacleComponent> pm = ComponentMapper.getFor(CollisionObstacleComponent.class);

    public CollisionObstacleSystem() {
        priority = 0;
    }

    public void addedToEngine(Engine engine) {
        mImmutablesEntities = engine.getEntitiesFor(Family.all(CollisionObstacleComponent.class).get());
    }

    public void update(float deltaTime) {

        entities = mImmutablesEntities.toArray(Entity.class);
        Arrays.sort(entities, velocityComparator);

        for (int i = 0; i < entities.length; i++) {
            Entity entity = entities[i];
            processEntity(entity, deltaTime, i);

        }
    }


    public void processEntity(Entity entity, float deltaTime, int startIdx) {
        CollisionObstacleComponent collisionObstacleComponent = entity.getComponent(CollisionObstacleComponent.class);
        if (collisionObstacleComponent == null || collisionObstacleComponent.mHandler == null)
            return;
        CollisionObstacleComponent otherCollisionObstacleComponent;
        Intersector.MinimumTranslationVector mvt = new Intersector.MinimumTranslationVector();

        for (int j = startIdx + 1; j < entities.length; j++) {
            Entity otherEntity = entities[j];
            if (otherEntity == entity)
                continue;

            otherCollisionObstacleComponent = otherEntity.getComponent(CollisionObstacleComponent.class);
            if (otherCollisionObstacleComponent == null)
                continue;

            boolean areBoundsOverlap = ShapeUtils.isBoundsOverlap(collisionObstacleComponent.mShape, otherCollisionObstacleComponent.mShape, 0.5F);

           if (areBoundsOverlap && ShapeUtils.overlaps(collisionObstacleComponent.mShape, otherCollisionObstacleComponent.mShape))
            {
                if (!otherCollisionObstacleComponent.mHandler.getCollisionObstacle().contains(collisionObstacleComponent, false)) {
                    otherCollisionObstacleComponent.mHandler.onCollisionObstacleStart(collisionObstacleComponent, false);
                }
                if (!collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                    collisionObstacleComponent.mHandler.onCollisionObstacleStart(otherCollisionObstacleComponent, false);
                }
            }
            else if (areBoundsOverlap && ShapeUtils.overlaps(collisionObstacleComponent.mShape, otherCollisionObstacleComponent.mShape, mvt)) {
                if (mvt.depth >= Math.sqrt(PathMap.CHECK_RADIUS)) {
                    if (!otherCollisionObstacleComponent.mHandler.getCollisionObstacle().contains(collisionObstacleComponent, false)) {
                        otherCollisionObstacleComponent.mHandler.onCollisionObstacleStart(collisionObstacleComponent, false);
                    }
                    if (!collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                        collisionObstacleComponent.mHandler.onCollisionObstacleStart(otherCollisionObstacleComponent, false);
                    }

                }
            } else {

                boolean hasCollisionInTime = areBoundsOverlap &&  hasCollisionInTime(entity, collisionObstacleComponent, otherCollisionObstacleComponent, deltaTime);
                if (!hasCollisionInTime && areBoundsOverlap) {
                    hasCollisionInTime = hasCollisionInTime(otherEntity, otherCollisionObstacleComponent, collisionObstacleComponent, deltaTime);
                }
                if (hasCollisionInTime && areBoundsOverlap) {
                    if (!otherCollisionObstacleComponent.mHandler.getCollisionObstacle().contains(collisionObstacleComponent, false)) {
                        otherCollisionObstacleComponent.mHandler.onCollisionObstacleStart(collisionObstacleComponent, true);
                    }
                    if (!collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                        collisionObstacleComponent.mHandler.onCollisionObstacleStart(otherCollisionObstacleComponent, true);
                    }

                } else {

                    if (collisionObstacleComponent.mHandler.getCollisionObstacle().contains(otherCollisionObstacleComponent, false)) {
                        collisionObstacleComponent.mHandler.onCollisionObstacleStop(otherCollisionObstacleComponent);
                    }
                    if (otherCollisionObstacleComponent.mHandler.getCollisionObstacle().contains(collisionObstacleComponent, false)) {
                        otherCollisionObstacleComponent.mHandler.onCollisionObstacleStop(collisionObstacleComponent);
                    }
                }
            }

        }

    }

    private boolean hasCollisionInTime(Entity aEntity, CollisionObstacleComponent aCollisionObstacleComponent, CollisionObstacleComponent aOtherCollisionObstacleComponent, float deltaTime) {
        VelocityComponent velocity = aEntity.getComponent(VelocityComponent.class);
        if (velocity != null) {
            float dx = velocity.x * deltaTime;
            float dy = velocity.y * deltaTime;
            Vector2 p1 = new Vector2();
            Vector2 p2 = new Vector2();
            Rectangle bounds = aCollisionObstacleComponent.mShape.getBounds();

            p1.set(bounds.x, bounds.y);
            p2.set(bounds.x + dx, bounds.y + dy);
            if (ShapeUtils.segmentIntersectShape(p1, p2, aOtherCollisionObstacleComponent.mShape)) {
                return true;
            } else {
                p1.set(bounds.x + bounds.getWidth(), bounds.y);
                p2.set(bounds.x + bounds.getWidth() + dx, bounds.y + dy);
                if (ShapeUtils.segmentIntersectShape(p1, p2, aOtherCollisionObstacleComponent.mShape)) {
                    return true;
                } else {
                    p1.set(bounds.x, bounds.y + bounds.getHeight());
                    p2.set(bounds.x + dx, bounds.y + bounds.getHeight() + dy);
                    if (ShapeUtils.segmentIntersectShape(p1, p2, aOtherCollisionObstacleComponent.mShape)) {
                        return true;
                    } else {
                        p1.set(bounds.x + bounds.getWidth(), bounds.y + bounds.getHeight());
                        p2.set(bounds.x + bounds.getWidth() + dx, bounds.y + bounds.getHeight() + dy);
                        if (ShapeUtils.segmentIntersectShape(p1, p2, aOtherCollisionObstacleComponent.mShape)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
