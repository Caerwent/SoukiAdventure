package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
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

    public boolean hasCollisionObstacle(CollisionObstacleComponent aEntity) {
        return super.hasCollisionObstacle(aEntity) || (aEntity.mData != null && aEntity.mData instanceof InteractionMonsterSlime);
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStart(aEntity);
        if (ret) {

            if (aEntity.mData != null && aEntity.mHandler instanceof InteractionMonsterSlime) {
                setMovable(false);
                return true;
            }
        }
        return ret;
    }

    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {

        boolean ret = super.onCollisionObstacleStop(aEntity);
        if (ret) {
            if ((aEntity.mData != null && aEntity.mHandler instanceof InteractionMonsterSlime) && (mEffectAction == null || mEffectAction.id != Effect.Type.FREEZE)) {
                setMovable(true);
            }
        }

        return ret;
    }


}
