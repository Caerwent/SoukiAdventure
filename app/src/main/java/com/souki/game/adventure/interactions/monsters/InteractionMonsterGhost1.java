package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionFollowPath;
import com.souki.game.adventure.interactions.InteractionHero;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionMonsterGhost1 extends InteractionFollowPath {


    public InteractionMonsterGhost1(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;
        mInteractionBorderSize=0;

    }
//    @Override
//    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity) {
//
//        boolean ret = super.onCollisionObstacleStart(aEntity);
//        if (ret && (aEntity.mType & CollisionObstacleComponent.HERO) != 0 && aEntity.mHandler != null && aEntity.mHandler == mMap.getPlayer().getHero()) {
//            EventDispatcher.getInstance().onMapReloadRequested(mMap.getMapName(), mMap.getFromMapId());
//            return false;
//
//        }
//        return ret;
//    }
    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return aEntity.mInteraction instanceof InteractionHero;
    }

    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
       if( aEntity.mHandler != null ) {
           EventDispatcher.getInstance().onMapReloadRequested(mMap.getMapName(), mMap.getFromMapId());
       }

    }

}
