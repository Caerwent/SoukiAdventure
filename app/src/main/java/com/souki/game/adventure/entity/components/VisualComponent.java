package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.souki.game.adventure.map.IMapRendable;

/**
 * Created by vincent on 07/07/2016.
 */

public class VisualComponent implements Component {
    public TextureRegion region;
    public IMapRendable rendable;

    public VisualComponent(TextureRegion region, IMapRendable rendable ) {
        this.region = region;
        this.rendable = rendable;
    }
}