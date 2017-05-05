package com.souki.game.adventure.map;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.souki.game.adventure.box2d.Shape;

/**
 * Created by vincent on 05/01/2017.
 */

public interface IMapRendable {
    public boolean isRendable();

    public boolean isRended();

    public void setRended(boolean aRended);

    public void render(Batch batch);

    public Shape getShapeRendering();

    public int getZIndex();
}
