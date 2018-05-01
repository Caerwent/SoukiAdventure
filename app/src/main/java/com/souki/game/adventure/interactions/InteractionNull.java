package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.InteractionComponent;
import com.souki.game.adventure.map.GameMap;

public class InteractionNull extends Interaction {

    public InteractionNull(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType=Type.NULL;
        initialize(x, y, aMapping);

    }







    @Override
    public void startToInteract() {
        super.startToInteract();
        remove(CollisionObstacleComponent.class);
        remove(CollisionEffectComponent.class);
        remove(InteractionComponent.class);

    }


}
