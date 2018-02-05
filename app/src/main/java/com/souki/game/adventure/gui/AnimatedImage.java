package com.souki.game.adventure.gui;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.souki.game.adventure.interactions.InteractionState;

/**
 * Created by vincent on 24/01/2018.
 */

public class AnimatedImage extends Image {
    private float stateTime = 0;
    public InteractionState mState;
    protected float mStateTime; // elapsed time

    public AnimatedImage() {
        super();
    }


    public void setState(InteractionState aState) {
        mState = aState;
        mStateTime = 0;
        if (mState != null) {
            TextureAtlas.AtlasRegion region = mState.getTextureRegion(0);
            if (getDrawable() == null) {
                setDrawable(new TextureRegionDrawable(region));
            } else {
                ((TextureRegionDrawable) getDrawable()).setRegion(region);
            }
            setSize(region.getRegionWidth(), region.getRegionHeight());
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        mStateTime += delta;
        if (mState != null) {
            TextureAtlas.AtlasRegion region = mState.getTextureRegion(mStateTime);
            if (getDrawable() == null) {
                setDrawable(new TextureRegionDrawable(region));
            } else {
                ((TextureRegionDrawable) getDrawable()).setRegion(region);
            }
            setSize(region.getRegionWidth(), region.getRegionHeight());
        }

    }
}
