package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionObstacle extends Interaction {
    private static final String KEY_IS_OPEN = "is_open";
    private static final String KEY_OPEN_STATE = "open_state";
    private static final String KEY_IS_DESTROYED = "is_destroyed";

    protected boolean mIsDestroyed = false;
    protected boolean mIsOpen;
    protected CollisionObstacleComponent mCollisionObstacleComponent;
    protected boolean mClosedBoundsAsObstacle;
    protected boolean mOpenBoundsAsObstacle;
    protected int mOpenZIndex = 1;
    protected boolean mKillableWhenClosed;
    protected boolean mHasCollisionWhenOpen;
    protected String mSetStateOnCollision;
    protected int mDefaultCollisionHeightFactor;

    public InteractionObstacle(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.OBSTACLE;

        if (mProperties != null) {
            /**
             * closedBoundsAsObstacle : true if collision when closed should be manage as obstacle and not as interaction obstacle
             * killableWhenClosed : restart map if hero has collision with bounds when obstacle becomes closed
             * hasCollisionWhenOpen : true if obstacle collision is managed even when obstacle is open (usefull for barrier)
             * openBoundsAsObstacle : true if collision when open should be manage as obstacle and not as interaction obstacle
             * openZIndex : if 0, hero is always be drawn over the open obstacle
             */
            if (mProperties.containsKey("closedBoundsAsObstacle") && Boolean.parseBoolean((String) mProperties.get("closedBoundsAsObstacle"))) {
                mClosedBoundsAsObstacle = true;
            }
            if (mProperties.containsKey("killableWhenClosed") && Boolean.parseBoolean((String) mProperties.get("killableWhenClosed"))) {
                mKillableWhenClosed = true;
            }
            if (mProperties.containsKey("hasCollisionWhenOpen") && Boolean.parseBoolean((String) mProperties.get("hasCollisionWhenOpen"))) {
                mHasCollisionWhenOpen = true;
            }
            if (mProperties.containsKey("openBoundsAsObstacle") && Boolean.parseBoolean((String) mProperties.get("openBoundsAsObstacle"))) {
                mOpenBoundsAsObstacle = true;
            }
            if (mProperties.containsKey("openZIndex")) {
                mOpenZIndex = ((Float) mProperties.get("openZIndex")).intValue();
            } else {
                mOpenZIndex = super.getZIndex();
            }
            if (mProperties.containsKey("setStateOnCollision")) {
                mSetStateOnCollision = (String) mProperties.get("setStateOnCollision");
            }


        }
        mDefaultCollisionHeightFactor = mCollisionHeightFactor;
        initialize(x, y, aMapping);


    }

    @Override
    public void startToInteract() {
        super.startToInteract();
        mCollisionObstacleComponent = getComponent(CollisionObstacleComponent.class);
        if (mCurrentState != null && mCurrentState.name.equals(InteractionState.STATE_OPEN)) {
            mIsOpen = true;
        } else {
            mIsOpen = false;
        }
        onStateChanged();

    }

    public int getZIndex() {
        return mIsOpen ? mOpenZIndex : super.getZIndex();
    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        Boolean isOpen = (Boolean) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_OPEN);
        if (isOpen != null && isOpen.booleanValue()) {
            mIsOpen = true;
            String state = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_OPEN_STATE);
            if (state != null) {
                mCurrentState = getState(state);
            }
        } else {
            if (mCurrentState != null && mCurrentState.name.equals(InteractionState.STATE_OPEN)) {
                mIsOpen = true;
            } else {
                mIsOpen = false;
            }
        }
        Boolean isDestroyed = (Boolean) GameSession.getInstance().getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_DESTROYED);
        if (isDestroyed != null && isDestroyed.booleanValue()) {
            mIsDestroyed = true;
        } else {
            mIsDestroyed = false;
        }

    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        GameSession.getInstance().putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_DESTROYED, mIsDestroyed);
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_OPEN, mIsOpen);
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_OPEN_STATE, mCurrentState.name);
        return aGameSession;
    }

    @Override
    protected boolean doAction(InteractionActionType aAction) {
        boolean res = super.doAction(aAction);
        boolean stateChanged = false;
        if (aAction != null && InteractionActionType.ActionType.OPEN == aAction.type) {
           //Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId);

            mIsOpen = true;
            stateChanged = true;
        } else if (aAction != null && InteractionActionType.ActionType.CLOSE == aAction.type) {
            //Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId);

            mIsOpen = false;
            stateChanged = true;
        } else if (aAction != null && InteractionActionType.ActionType.REMOVED == aAction.type) {
            //Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId);

            mIsDestroyed = true;
            stateChanged = true;
        }
        if (stateChanged) {
            onStateChanged();
            res = stateChanged;
        }

        return res;
    }


    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {

        boolean ret = super.onCollisionObstacleStart(aEntity,  aIsPrediction);
       // Gdx.app.debug("DEBUG", "onCollisionObstacleStart id=" + getId() + " aEntity=" + aEntity.mName + " ret=" + ret);

        if (ret && (aEntity.mType & CollisionObstacleComponent.HERO) != 0 && !mIsOpen && mKillableWhenClosed && !aIsPrediction && aEntity.mHandler != null && aEntity.mHandler == mMap.getPlayer().getHero()) {
            EventDispatcher.getInstance().onMapReloadRequested(mMap.getMapName(), mMap.getFromMapId());
            return false;

        }

        return ret;
    }


        @Override
        public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
            return mIsOpen && aEntity.mInteraction.getType() == Type.HERO;
        }
/*
        @Override
        public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
            super.onStartCollisionInteraction(aEntity);
            if (mIsOpen && mSetStateOnCollision != null && getState(mSetStateOnCollision) != null) {

                if (ShapeUtils.overlaps(mShapeInteraction, ((InteractionHero) aEntity.mHandler).getShapeCollision())) {
                    setState(mSetStateOnCollision);
                    //mCollisionsObstacle.removeValue(, false);
                }
                else
                {
                    mCollisionsInteraction.removeValue(aEntity, true);
                }
            }
        }
    */


    @Override
    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
        super.onStartCollisionInteraction(aEntity);
        if (mSetStateOnCollision != null && getState(mSetStateOnCollision) != null) {
            setState(mSetStateOnCollision);
        }
    }

    protected void onStateChanged() {
        if (mIsDestroyed) {
            //Gdx.app.debug("DEBUG", "remove obstacle " + getId());
            mMap.getInteractions().removeValue(this, true);
            destroy();
            return;
        }

        if (mIsOpen) {
            if (mHasCollisionWhenOpen) {
                if (mOpenBoundsAsObstacle) {
                    mCollisionHeightFactor = 1;
                } else {
                    mCollisionHeightFactor = mDefaultCollisionHeightFactor;
                }
                if (getComponent(CollisionObstacleComponent.class) == null) {
                    if(mCollisionObstacleComponent==null && isRendable())
                    {
                        mCollisionObstacleComponent =new CollisionObstacleComponent(mCollisionType, getShapeCollision(), mId, this, this);
                    }
                    add(mCollisionObstacleComponent);
                }
            } else {
                //Gdx.app.debug("DEBUG", "remove obstacle when open" + getId());
                for(CollisionObstacleComponent otherCollisionObstacleComponent:mCollisionsObstacle)
                {
                    if(otherCollisionObstacleComponent.mHandler!=null) {
                        otherCollisionObstacleComponent.mHandler.onCollisionObstacleStop(getComponent(CollisionObstacleComponent.class));
                    }
                }
                mCollisionsObstacle.clear();
                remove(CollisionObstacleComponent.class);
            }
        } else {
            if (mClosedBoundsAsObstacle) {
                mCollisionHeightFactor = 1;
            } else {
                mCollisionHeightFactor = mDefaultCollisionHeightFactor;
            }
            if (getComponent(CollisionObstacleComponent.class) == null) {
                //Gdx.app.debug("DEBUG", "add obstacle when close" + getId());
                if(mCollisionObstacleComponent==null && isRendable())
                {
                    mCollisionObstacleComponent =new CollisionObstacleComponent(mCollisionType, getShapeCollision(), mId, this, this);
                }
                add(mCollisionObstacleComponent);
            }
        }
    }


}
