package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

public class InteractionTrigger extends Interaction {


    private static final String KEY_STATE = "state";


    public InteractionTrigger(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mCollisionType = CollisionObstacleComponent.MAPINTERACTION_NOT_OBSTACLE;
        initialize(x, y, aMapping);


    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        String state = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE);
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
    public int getZIndex() {

        return 0;
    }

}
