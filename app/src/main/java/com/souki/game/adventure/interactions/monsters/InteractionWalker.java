package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.interactions.Interaction;
import com.souki.game.adventure.interactions.InteractionActionType;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;


/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionWalker extends Interaction {

    public static enum Orientation {
        NONE,
        VERTICAL,
        HORIZONTAL
    }

    private static final String KEY_IS_DESTROYED = "is_destroyed";

    protected boolean mIsDestroyed = false;

    protected boolean mIsAutoStart;
    protected Orientation mOrientation = Orientation.NONE;

    public InteractionWalker(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;
        mCollisionHeightFactor = 4;
        if (mProperties != null) {


                if (mProperties.containsKey("autoStart")) {
                    mIsAutoStart = Boolean.valueOf((String) mProperties.get("autoStart"));
                }

                if (mProperties.containsKey("orientation")) {
                    mOrientation = Orientation.valueOf((String) mProperties.get("orientation"));
                }



        }
        initialize(x, y, aMapping);


    }
    public void restoreFromSessionPersistence() {
        Boolean isDestroyed = (Boolean) GameSession.getInstance().getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_DESTROYED);
        if (isDestroyed != null && isDestroyed.booleanValue()) {
            mIsDestroyed = true;
            mMap.getInteractions().removeValue(this, true);
            destroy();
        } else {
            mIsDestroyed = false;
        }

    }

    public void saveInSessionPersistence() {
        GameSession.getInstance().putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_DESTROYED, mIsDestroyed);
    }



    @Override
    protected boolean doAction(InteractionActionType aAction) {
        boolean res = super.doAction(aAction);
        if (!res && aAction != null && InteractionActionType.ActionType.REMOVED==aAction.type) {
            mIsDestroyed=true;
            mMap.getInteractions().removeValue(this, true);
            destroy();
            return true;
        }
        if (!res && aAction != null && InteractionActionType.ActionType.WAKEUP == aAction.type) {
            if (!isMovable()) {
                setMovable(true);
                setVelocityForNonPathMovement();
            }
            return true;
        }
        return res;
    }

    protected void setVelocityForNonPathMovement() {

            VelocityComponent velocity = this.getComponent(VelocityComponent.class);
            if (velocity != null) {
                Vector2 vel = new Vector2();
                PathMap.computeVelocityForDisplacement(vel,
                        mOrientation == Orientation.HORIZONTAL ? 1 : 0,
                        mOrientation == Orientation.VERTICAL ? 1 : 0, PathHero.HERO_VELOCITY);
                setVelocity(vel);
            }

    }

    @Override
    public void startToInteract() {
        super.startToInteract();
        if (mIsAutoStart) {
            setMovable(true);
            setVelocityForNonPathMovement();
        }

    }

    public boolean hasCollisionObstacle(CollisionObstacleComponent aEntity) {
        return (aEntity.mType & CollisionObstacleComponent.OBSTACLE) != 0
                || ((aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)
                || ((aEntity.mType & CollisionObstacleComponent.HERO) != 0);
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStart(aEntity);
        if (ret) {

            if (hasCollisionObstacle(aEntity)) {
                VelocityComponent velocity = this.getComponent(VelocityComponent.class);
                if (velocity != null) {
                    setVelocity(-1*velocity.x, -1*velocity.y); //change direction
                }
            }
        }
        return ret;
    }

}