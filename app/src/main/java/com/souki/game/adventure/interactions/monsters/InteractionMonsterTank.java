package com.souki.game.adventure.interactions.monsters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.interactions.IInteractionActivateBehavior;
import com.souki.game.adventure.interactions.Interaction;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionHero;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.interactions.InteractionState;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionMonsterTank extends Interaction implements IInteractionActivateBehavior {


    protected float mSpeedFactor;
    protected Vector2 mTmpVelocity;
    protected Vector2 mTmpPosFinale;
    protected Vector2 mTmpPosTarget;
    protected Vector2 mPos2D;
    protected Entity[] mEntities;

    public InteractionMonsterTank(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;
        mTmpVelocity = new Vector2();
        mTmpPosFinale = new Vector2();
        mPos2D = new Vector2();
        mTmpPosTarget = new Vector2();
        mSpeedFactor = 1F;
        mInteractionBorderSize = 0;
        if (mProperties != null) {
            if (mProperties.containsKey("speedFactor")) {

                mSpeedFactor = Float.valueOf((String) mProperties.get("speedFactor"));

            }


        }
        initialize(x, y, aMapping);

        setMovable(false);

    }


    @Override
    public void update(float dt) {

        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity == null && !mCurrentState.name.equals(InteractionState.STATE_EXPLODE)) {
            boolean canMove = false;

            if (mMap != null && mMap.getPlayer() != null && mMap.getPlayer().getHero() != null && mMap.getPlayer().getHero().getShapeInteraction() != null && mShapeInteraction != null) {
                Vector2 target = mMap.getPlayer().getHero().getPosition();
                Rectangle rectTarget = mMap.getPlayer().getHero().getShapeInteraction().getBounds();
                Rectangle bounds = mShapeInteraction.getBounds();
                TransformComponent transform = this.getComponent(TransformComponent.class);
                mPos2D.set(transform.position.x, transform.position.y);


                float angleWithTarget = 0;

                if (bounds.x + bounds.width >= rectTarget.x && bounds.x < rectTarget.x + rectTarget.width) {

                    angleWithTarget = bounds.y < rectTarget.y ? 90 : 270;
                    canMove = true;
                } else if (bounds.y + bounds.height >= rectTarget.y && bounds.y < rectTarget.y + rectTarget.height) {
                    angleWithTarget = bounds.x < rectTarget.x ? 0 : 180;
                    canMove = true;
                }
                if (canMove) {
                    mEntities = EntityEngine.getInstance().getEntitiesFor(Family.all(CollisionObstacleComponent.class).get()).toArray(Entity.class);

                    if (mEntities != null && mEntities.length > 0) {
                        for (int i = 0; i < mEntities.length; i++) {

                            CollisionObstacleComponent collision = mEntities[i].getComponent(CollisionObstacleComponent.class);
                            if (collision== null || collision.mShape == null || collision.mShape == mMap.getPlayer().getHero().getShapeCollision() || collision.mShape == getShapeCollision())
                                continue;

                            if ((collision.mType & CollisionObstacleComponent.OBSTACLE) != 0 || ((collision.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)) {
                                if (ShapeUtils.segmentIntersectShape(mPos2D, target, collision.mShape)) {
                                    canMove = false;
                                    break;
                                }
                            }
                        }
                    }

                }
                if (canMove) {

                    mTmpVelocity.set((float) (PathHero.HERO_VELOCITY * mSpeedFactor * Math.cos(angleWithTarget * MathUtils.degreesToRadians)), (float) (PathHero.HERO_VELOCITY * mSpeedFactor * Math.sin(angleWithTarget * MathUtils.degreesToRadians)));
                    //PathMap.computeVelocityForDisplacement(mTmpVelocity, dx, dy, PathHero.HERO_VELOCITY * mSpeedFactor);
                    setMovable(true);
                    setVelocity(mTmpVelocity);
                    canMove = true;
                }

            }

            if (!canMove) {
                setMovable(false);
            }

        }
        super.

                update(dt);

    }


    public boolean hasCollisionObstacle(CollisionObstacleComponent aEntity) {
        return (aEntity.mType & CollisionObstacleComponent.OBSTACLE) != 0 ||
                (((aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0));
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {

        boolean ret = super.onCollisionObstacleStart(aEntity, aIsPrediction);
        if (ret) {

            if (hasCollisionObstacle(aEntity)) {

                setMovable(false);
                setState(InteractionState.STATE_EXPLODE);
                mEffectAction = new Effect();
                mEffectAction.id = Effect.Type.WAVE;
                mEffectAction.targetDuration = 5;
                mEffectAction.targetState = InteractionState.STATE_EXPLODE;

                return true;

            }
        }
        return ret;
    }

    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return aEntity.mInteraction instanceof InteractionHero;
    }

    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
        if (aEntity.mHandler != null && !mCurrentState.name.equals(InteractionState.STATE_EXPLODE)) {
            EventDispatcher.getInstance().onMapReloadRequested(mMap.getMapName(), mMap.getFromMapId());
        }

    }
}