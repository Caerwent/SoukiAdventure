package com.souki.game.adventure.interactions.monsters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.interactions.IInteractionActivateBehavior;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionFollowPath;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.interactions.InteractionState;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 04/01/2018.
 */

public class InteractionMovable extends InteractionFollowPath implements IInteractionActivateBehavior {
    protected float mDistance;

    public InteractionMovable(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;
        if (mProperties != null) {
            if (mProperties.containsKey("distance")) {

                mDistance = Float.valueOf((String) mProperties.get("distance"));

            }


        }
        initialize(x, y, aMapping);


    }

    @Override
    public void setMovable(boolean isMovable) {
        super.setMovable(isMovable);
        setState(InteractionState.STATE_IDLE);
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {

        boolean ret = super.onCollisionObstacleStart(aEntity, aIsPrediction);
        if (ret) {

            if (hasCollisionObstacle(aEntity)) {
                setMovable(false);
            }
        }
        mIsInterruptedByObstacle = false;
        return ret;
    }

    @Override
    public boolean onStartEffectInteraction(CollisionEffectComponent aEntity) {
        Effect effect = (Effect) aEntity.mEffect;
        if (effect != null && effect.id == Effect.Type.WAVE) {
            computeMovement();
        }
        return false;
    }


    protected void computeMovement() {
        VelocityComponent velocity = this.getComponent(VelocityComponent.class);

        boolean canMove = false;
        mPath = null;

        if (mMap != null && mMap.getPlayer() != null && mMap.getPlayer().getHero() != null && mMap.getPlayer().getHero().getShapeInteraction() != null && mShapeInteraction != null) {
            Vector2 target = mMap.getPlayer().getHero().getPosition();
            Rectangle rectTarget = mMap.getPlayer().getHero().getShapeInteraction().getBounds();
            Rectangle bounds = mShapeInteraction.getBounds();
            TransformComponent transform = this.getComponent(TransformComponent.class);
            Vector2 selfPos2D = new Vector2();
            selfPos2D.set(transform.position.x + bounds.width / 2, transform.position.y);
            Vector2 targetPos2D = new Vector2();
            targetPos2D.set(rectTarget.x + rectTarget.width / 2, rectTarget.y);


            double angle = Math.atan2(selfPos2D.y - targetPos2D.y, selfPos2D.x - targetPos2D.x);


            Vector2 finalPos2D = new Vector2();
            float newX = transform.position.x + mDistance * (float) Math.cos(angle);
            float newY = transform.position.y + mDistance * (float) Math.sin(angle);
            finalPos2D.set(newX, newY);


            Entity[] entities = EntityEngine.getInstance().getEntitiesFor(Family.all(CollisionObstacleComponent.class).get()).toArray(Entity.class);
            canMove = true;
            if (entities != null && entities.length > 0) {
                for (int i = 0; i < entities.length; i++) {

                    CollisionObstacleComponent collision = entities[i].getComponent(CollisionObstacleComponent.class);
                    if (collision.mShape == null || collision.mShape == getShapeCollision())
                        continue;

                    if ((collision.mType & CollisionObstacleComponent.OBSTACLE) != 0 || ((collision.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)) {
                        if (ShapeUtils.segmentIntersectShape(selfPos2D, finalPos2D, collision.mShape)) {
                            canMove = false;
                            break;
                        }
                    }
                }
            }


            if (canMove) {
                Vector2 mTmpVelocity = new Vector2();
                PathMap.computeVelocityForDisplacement(mTmpVelocity, finalPos2D.x - selfPos2D.x, finalPos2D.y - selfPos2D.y, PathHero.HERO_VELOCITY);
                setMovable(true);
                mPath = new PathMap(mId);
                mPath.addPoint(transform.position.x, transform.position.y);
                mPath.addPoint(finalPos2D.x, finalPos2D.y);
                canMove = true;
            }

        }

        if (!canMove) {
            setMovable(false);
        }

    }
}
