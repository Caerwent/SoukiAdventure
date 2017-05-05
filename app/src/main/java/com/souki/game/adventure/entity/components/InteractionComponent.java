package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.souki.game.adventure.interactions.IInteraction;

/**
 * Created by vincent on 20/07/2016.
 */

public class InteractionComponent implements Component {
    public IInteraction interaction;

    public InteractionComponent(IInteraction aInteraction) {
        interaction = aInteraction;

    }
}