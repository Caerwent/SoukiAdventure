package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionActivator extends Interaction{
    private static final String KEY_STATE = "state";

    public InteractionActivator(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.ACTIVATOR;
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
    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return aEntity.mInteraction.getType()==Type.HERO;
    }
    @Override
    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
        if(!isClickable())
        {
            toggleActivation();
        }
    }
    @Override
    public void onStopCollisionInteraction(CollisionInteractionComponent aEntity) {
        if(!isClickable())
        {
            toggleActivation();
        }
    }
    @Override
    protected boolean hasTouchInteraction(float x, float y) {

        return getShapeInteraction().getBounds().contains(x, y);
    }
    @Override
    public void onTouchInteraction() {
        toggleActivation();

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
