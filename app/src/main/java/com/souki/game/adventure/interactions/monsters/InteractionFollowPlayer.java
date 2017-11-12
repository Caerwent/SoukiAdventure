package com.souki.game.adventure.interactions.monsters;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.interactions.Interaction;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionFollowPlayer extends Interaction {


    protected float mSpeedFactor;
    protected Vector2 mTmpVelocity;
    protected Vector2 mTmpPosFinale;
    protected Vector2 mTmpPosTarget;
    protected Vector2 mPos2D;
    protected Entity[] mEntities;

    public InteractionFollowPlayer(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mTmpVelocity = new Vector2();
        mTmpPosFinale = new Vector2();
        mPos2D = new Vector2();
        mTmpPosTarget = new Vector2();
        mSpeedFactor = 1F;
        if (mProperties != null) {
            if (mProperties.containsKey("speedFactor")) {

                mSpeedFactor = Float.valueOf((String) mProperties.get("speedFactor"));

            }


        }
        initialize(x, y, aMapping);


    }

    @Override
    public void update(float dt) {

        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity != null) {
            Vector2 target = mMap.getPlayer().getHero().getPosition();
            if (target != null) {
                TransformComponent transform = this.getComponent(TransformComponent.class);
                mPos2D.set(transform.position.x, transform.position.y);
                double dx = target.x - mPos2D.x;
                double dy = target.y - mPos2D.y;

                PathMap.computeVelocityForDisplacement(mTmpVelocity, dx, dy, PathHero.HERO_VELOCITY * mSpeedFactor);
//                Gdx.app.debug("DEBUG", "mPos2D=(" + mPos2D.x + "," + mPos2D.y + ")");
//                Gdx.app.debug("DEBUG", "mPosPlayer=(" + target.x + "," + target.y + ")");
                // compute virtual next pos to check collision

                mTmpPosTarget.set(mPos2D.x + mTmpVelocity.x * dt, mPos2D.y + mTmpVelocity.y * dt);

//                Gdx.app.debug("DEBUG", "mTmpPosTarget=(" + mTmpPosTarget.x + "," + mTmpPosTarget.y + ")");


                mEntities = EntityEngine.getInstance().getEntitiesFor(Family.all(CollisionObstacleComponent.class).get()).toArray(Entity.class);

                getShapeCollision().setX(mTmpPosTarget.x);
                getShapeCollision().setY(mTmpPosTarget.y);
                if (!ShapeUtils.overlaps(getShapeCollision(), mMap.getPlayer().getHero().getShapeCollision())) {
                    getShapeCollision().setX(mPos2D.x);
                    getShapeCollision().setY(mPos2D.y);

                    if (ShapeUtils.checkMove(mEntities, getShapeCollision(), mTmpPosTarget, null, mTmpPosFinale, 5)) {
                        //setPosition(mTmpPosFinale.x, mTmpPosFinale.y);
                        //Gdx.app.debug("DEBUG", "CAN MOVE to mTmpPosFinale=(" + mTmpPosFinale.x + "," + mTmpPosFinale.y + ")");

                        dx = mTmpPosFinale.x - mPos2D.x;
                        dy = mTmpPosFinale.y - mPos2D.y;
//                        Gdx.app.debug("DEBUG", "CAN MOVE by=(" + dx + "," + dy + ")");

                        PathMap.computeVelocityForDisplacement(mTmpVelocity, dx, dy, PathHero.HERO_VELOCITY * mSpeedFactor);
                        setMovable(true);
                        setVelocity(mTmpVelocity);
//                        Gdx.app.debug("DEBUG", "mTmpVelocity=(" + mTmpVelocity.x + "," + mTmpVelocity.y + ")");


                    } else {
                        setVelocity(0, 0);
                    }
                } else {
                    setVelocity(0, 0);
                }

            } else {
                setVelocity(0, 0);
            }
        }
        super.update(dt);


    }


    public boolean hasCollisionObstacle(CollisionObstacleComponent aEntity) {
        return (aEntity.mType & CollisionObstacleComponent.OBSTACLE) != 0 || ((aEntity.mType & CollisionObstacleComponent.HERO) != 0)
                || ((aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0);
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStart(aEntity);

        if (ret) {

            if (hasCollisionObstacle(aEntity)) {
                if ((aEntity.mType & CollisionObstacleComponent.HERO) != 0) {
                    setMovable(false);
                    return true;
                } else {
                    VelocityComponent velocity = this.getComponent(VelocityComponent.class);
                    if (velocity != null) {
                        setVelocity(0, 0);
                        return true;
                    }
                }

            }
        }
        return ret;
    }

    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStop(aEntity);
        if (ret) {
            if ((aEntity.mType & CollisionObstacleComponent.HERO) != 0 && (mEffectAction == null || mEffectAction.id != Effect.Type.FREEZE)) {
                setMovable(true);
            }
        }

        return ret;
    }

    @Override
    protected void stopEffectAction() {
        if (mEffectAction != null && mEffectAction.id == Effect.Type.FREEZE) {
            setMovable(true);
        }
        super.stopEffectAction();

    }
}