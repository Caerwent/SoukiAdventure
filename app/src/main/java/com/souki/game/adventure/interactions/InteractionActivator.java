package com.souki.game.adventure.interactions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.interactions.monsters.IInteractionActivateBehavior;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

import static com.souki.game.adventure.interactions.InteractionActivator.MODE.NORMAL;
import static com.souki.game.adventure.interactions.InteractionActivator.MODE.PUSH_ONLY;
import static com.souki.game.adventure.interactions.InteractionActivator.MODE.PUSH_PULL;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionActivator extends Interaction{

    public static enum MODE {
        NORMAL,
        PUSH_PULL,
        PUSH_ONLY
    }
    public final static String STATE_ACTIVATED="ACTIVATED";

    private static final String KEY_STATE = "state";
    protected MODE mMode = NORMAL;
    protected float mDelay=0;
    protected float mDelayTime=0;
    protected boolean mDelayRunning = false;


    public InteractionActivator(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap, MODE aMode) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mMode = aMode;
        if(mMode==PUSH_ONLY || mMode==PUSH_PULL)
        {
            mCollisionHeightFactor = 1;
            mCollisionType = CollisionObstacleComponent.MAPINTERACTION_NOT_OBSTACLE;
        }
        mType = Type.ACTIVATOR;
        if (mProperties != null) {
            if (mProperties.containsKey("delay")) {
                mDelay = ((Float)mProperties.get("delay")).floatValue();
            }
        }
        initialize(x, y, aMapping);


    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        String state  = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE);
        if (state != null) {
            mCurrentState = getState(state);
        }

    }

    @Override
    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE, mCurrentState);
        return aGameSession;
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if(mDelayRunning) {
            mDelayTime += dt;
            if (mDelayTime >= mDelay) {
                mDelayRunning = false;
                mDelayTime = 0;
                setState(InteractionState.STATE_IDLE);
            }
        }

    }
    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStart(aEntity);
        if (ret &&
                (((aEntity.mType & CollisionObstacleComponent.HERO) != 0) ||
                ((aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)  && aEntity.mHandler instanceof IInteractionActivateBehavior) &&
                    (mMode==PUSH_ONLY || mMode==PUSH_PULL)) {

            if(mMode==PUSH_PULL && mDelay>0) {
                mDelayTime=0;
                mDelayRunning=false;
            }
            setState(STATE_ACTIVATED);
            return true;

        }
        return ret;
    }
    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStop(aEntity);

        if (ret &&
                (((aEntity.mType & CollisionObstacleComponent.HERO) != 0) ||
                        ((aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)  && aEntity.mHandler instanceof IInteractionActivateBehavior ) &&
                        mMode==PUSH_PULL ) {
            if(mDelay>0) {
                Gdx.app.debug("DEBUG", "onCollisionObstacleStop start delay");
                mDelayTime=0;
                mDelayRunning=true;
            }
            else
            {
                setState(InteractionState.STATE_IDLE);
            }
            return true;

        }
        return ret;
    }
    @Override
    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return !(mMode==PUSH_ONLY || mMode==PUSH_PULL) && aEntity.mInteraction.getType()==Type.HERO ;
    }
    @Override
    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
        if(!(mMode==PUSH_ONLY || mMode==PUSH_PULL) && !isClickable())
        {
            toggleActivation();
        }
    }
    @Override
    public void onStopCollisionInteraction(CollisionInteractionComponent aEntity) {
        if(!(mMode==PUSH_ONLY || mMode==PUSH_PULL) && !isClickable())
        {
            toggleActivation();
        }
    }
    @Override
    protected boolean hasTouchInteraction(float x, float y) {

        return !(mMode==PUSH_ONLY || mMode==PUSH_PULL) && getShapeInteraction().getBounds().contains(x, y);
    }
    @Override
    public void onTouchInteraction() {
        toggleActivation();

    }

    @Override
    public int getZIndex() {

        return (mMode==PUSH_ONLY || mMode==PUSH_PULL) ? 0 : super.getZIndex();
    }
    protected void toggleActivation()
    {
        if(mCurrentState.name.compareTo(mDef.states.get(0).name)==0)
        {
            setState(mDef.states.get(1).name);
        }
        else
        {
            setState(mDef.states.get(0).name);
        }
    }
}
