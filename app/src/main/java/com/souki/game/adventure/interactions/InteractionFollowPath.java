package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionFollowPath extends Interaction  {
    protected PathMap mPath;

    public InteractionFollowPath(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);

        if(mProperties!=null)
        {
            if(mProperties.containsKey("pathId"))
            {
                mPath = aMap.getPaths().get((String) mProperties.get("pathId"));
            }

        }
        initialize(x, y, aMapping);


    }
    @Override
    public void update(float dt) {
        super.update(dt);
        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity != null) {
            if (mPath != null && mPath.hasNextPoint()) {
                TransformComponent transform = this.getComponent(TransformComponent.class);
                Vector2 pos2D = new Vector2(transform.position.x, transform.position.y);
                setVelocity(mPath.getVelocityForPosAndTime(pos2D, dt));
            } else {
                setMovable(false);
            }

        }
    }
    @Override
    protected boolean doAction(InteractionActionType aAction) {
        boolean res = super.doAction(aAction);
        if (!res && aAction != null && InteractionActionType.ActionType.WAKEUP==aAction.type) {
            if (mPath != null) {
                setMovable(true);
            }
            return true;
        }
        return res;
    }
}