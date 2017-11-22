package com.souki.game.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.Styles;

/**
 * Created by vincent on 23/01/2017.
 */

public class GenericUI {
    private static GenericUI sInstance;

    public static synchronized void createInstance() {
        sInstance = new GenericUI();
    }

    public static GenericUI getInstance() {
        return sInstance;
    }

    protected TextureAtlas atlas;
    protected Skin skin;

    public TextureAtlas getTextureAtlas() {
        return atlas;
    }

    public Skin getSkin() {
        return skin;
    }

    public GenericUI() {
        super();
        init();
    }


    protected String atlasPath() {
        return "data/skins/ui.atlas";
    }


    protected String skinPath() {
        return AssetsUtility.UI_SKIN_PATH;
    }


    protected void init() {
        atlas = new TextureAtlas(atlasPath());
        skin = new Skin();
        String skinPath = skinPath();
        Styles style = new Styles();
        skin = style.addFreeTypeFont(skin);
        skin.addRegions(atlas);
        if (skinPath != null)
            skin.load(Gdx.files.internal(skinPath));
        skin = style.styleSkin(skin, atlas);


    }
}
