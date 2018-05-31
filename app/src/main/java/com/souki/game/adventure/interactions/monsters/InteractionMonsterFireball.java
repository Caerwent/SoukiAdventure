package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.interactions.Interaction;
import com.souki.game.adventure.interactions.InteractionActionType;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionHero;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.interactions.InteractionState;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionMonsterFireball extends Interaction {

    public static enum Orientation {
        NONE(0),
        LEFT(-1),
        RIGHT(1),
        UP(1),
        BOTTOM(-1);

        public double velocity;

        Orientation(double aVelocity)
        {
            velocity = aVelocity;
        }
    }

    private static final String KEY_IS_DESTROYED = "is_destroyed";

    protected boolean mIsDestroyed = false;
    protected float mSpeedFactor;
    protected boolean mIsAutoStart;
    protected Orientation mOrientation = Orientation.NONE;

    public InteractionMonsterFireball(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;

        if (mProperties != null) {
            if (mProperties.containsKey("speedFactor")) {

                mSpeedFactor = Float.valueOf((String) mProperties.get("speedFactor"));

            }

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
        if (!res && aAction != null && InteractionActionType.ActionType.REMOVED == aAction.type) {
            mIsDestroyed = true;
            mMap.getInteractions().removeValue(this, true);
            destroy();
            return true;
        }
        return res;
    }

    protected void setVelocityForNonPathMovement() {

        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity != null) {
            Vector2 vel = new Vector2();
            PathMap.computeVelocityForDisplacement(vel,
                    (mOrientation == Orientation.LEFT || mOrientation == Orientation.RIGHT) ? mOrientation.velocity : 0,
                    (mOrientation == Orientation.UP || mOrientation == Orientation.BOTTOM) ? mOrientation.velocity : 0,
                    PathHero.HERO_VELOCITY*mSpeedFactor);
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
        if((aEntity.mType & CollisionObstacleComponent.OBSTACLE) != 0 ||  (aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)
        {
            if(aEntity.mHandler!=null &&
                    aEntity.mHandler instanceof InteractionMonsterFireballGenerator &&
                    ((InteractionMonsterFireballGenerator) aEntity.mHandler).getCurrentState().name!=InteractionState.STATE_EXPLODE)
            {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {
        boolean ret = super.onCollisionObstacleStart(aEntity, aIsPrediction);
        if (ret) {

            if (hasCollisionObstacle(aEntity)) {

                if (isMovable()) {
                    setMovable(false);
                    setState(InteractionState.STATE_EXPLODE);
                    mEffectAction = new Effect();
                    mEffectAction.id = Effect.Type.BURN;
                    mEffectAction.targetDuration = -1;
                    mEffectAction.targetState = InteractionState.STATE_EXPLODE;
                }
                return true;

            }
        }
        return ret;
    }


    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return aEntity.mInteraction instanceof InteractionHero;
    }

    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
        if (aEntity.mHandler != null && !mCurrentState.name.equals(InteractionState.STATE_EXPLODE) && isMovable()) {
            EventDispatcher.getInstance().onMapReloadRequested(mMap.getMapName(), mMap.getFromMapId());
        }

    }

}