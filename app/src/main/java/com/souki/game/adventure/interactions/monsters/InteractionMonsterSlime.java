package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionMonsterSlime extends InteractionFollowPlayer {


    public InteractionMonsterSlime(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;

    }


}
