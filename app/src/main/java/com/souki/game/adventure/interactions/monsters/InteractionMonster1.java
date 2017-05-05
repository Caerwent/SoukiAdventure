package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.interactions.InteractionActionType;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionFollowPath;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionMonster1 extends InteractionFollowPath {
    private static final String KEY_IS_DESTROYED = "is_destroyed";

    protected boolean mIsDestroyed = false;

    public InteractionMonster1(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;


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
        return res;
    }


}
