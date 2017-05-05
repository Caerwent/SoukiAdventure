package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.interactions.IInteraction;

/**
 * Created by vincent on 26/07/2016.
 */

public class CollisionInteractionComponent implements Component {



    public Shape mShape;
    public ICollisionInteractionHandler mHandler;
    public IInteraction mInteraction;


    public CollisionInteractionComponent() {
    }

    public CollisionInteractionComponent(Shape shape, IInteraction aInteraction, ICollisionInteractionHandler aHandler) {
        mShape = shape;
        mHandler = aHandler;
        mInteraction=aInteraction;
    }
}
