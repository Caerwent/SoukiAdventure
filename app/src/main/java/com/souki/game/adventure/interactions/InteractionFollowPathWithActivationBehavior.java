package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionFollowPathWithActivationBehavior extends InteractionFollowPath implements IInteractionActivateBehavior {

    public InteractionFollowPathWithActivationBehavior(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.ACTIVATOR;

    }

}
