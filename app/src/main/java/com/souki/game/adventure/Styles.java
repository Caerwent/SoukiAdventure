package com.souki.game.adventure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * @author trey miller
 */
public class Styles {

    ObjectMap<String, BitmapFont> fontMap = new ObjectMap<String, BitmapFont>();


    public Skin addFreeTypeFont(Skin skin) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/Roboto-Medium.ttf"));

        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 20;
        parameter.color = Color.WHITE;
        BitmapFont font_default = generator.generateFont(parameter); // font white color, size 18 pixels
        skin.add("default-font", font_default, BitmapFont.class);
        fontMap.put("default-font", font_default);

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 11;
        parameter.color = Color.WHITE;
        BitmapFont font_small = generator.generateFont(parameter); // font white color, size 18 pixels
        skin.add("small-font", font_small, BitmapFont.class);
        fontMap.put("small-font", font_small);

        parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 18;
        parameter.color = Color.WHITE;
        BitmapFont font_normal_text = generator.generateFont(parameter); // font white color, size 18 pixels
        skin.add("normal-text-font", font_normal_text, BitmapFont.class);
        fontMap.put("normal-text-font", font_normal_text);

        generator.dispose();

        return skin;

    }

    public Skin styleSkin(Skin skin, TextureAtlas atlas) {
   /*     BitmapFont font18 = new BitmapFont(Gdx.files.internal("data/fonts/sans_serif_18.fnt"), false);
        skin.add("default", font18);

*/


        skin.add("background-color-1", new Color(0x94a9aaff));
        skin.add("background-color-2", new Color(0x768a9dff));
        skin.add("background-color-3", new Color(0x9f9f9Fff));

        skin.add("lt-blue", new Color(.6f, .8f, 1f, 1f));
        skin.add("lt-green", new Color(.6f, .9f, .6f, 1f));
        skin.add("dark-blue", new Color(.1f, .3f, 1f, 1f));

        NinePatchDrawable btn1up = new NinePatchDrawable(atlas.createPatch("button_default"));
        NinePatchDrawable btn1down = new NinePatchDrawable(atlas.createPatch("button_pressed"));
        NinePatch window1patch = atlas.createPatch("window1");
        NinePatch window = atlas.createPatch("window");
        NinePatch dialog = atlas.createPatch("dialog");
        NinePatch bgpatch = atlas.createPatch("bg");
        NinePatch slider2patch = atlas.createPatch("slider2");
        NinePatch knobpatch = atlas.createPatch("knob");
        skin.add("btn1up", btn1up);
        skin.add("btn1down", btn1down);
        skin.add("window1", window1patch);
        skin.add("dialog", dialog);
        skin.add("window", window);
        skin.add("bg", bgpatch);
        skin.add("slider2", slider2patch);
        skin.add("knob", knobpatch);

        NinePatchDrawable tabActive = new NinePatchDrawable(atlas.createPatch("tab_active"));
        NinePatchDrawable tabInactive = new NinePatchDrawable(atlas.createPatch("tab_inactive"));
        skin.add("tabActive", tabActive);
        skin.add("tabInactive", tabInactive);


        TextButtonStyle tbs = new TextButtonStyle(btn1up, btn1down, btn1down, fontMap.get("default-font"));
        tbs.fontColor = skin.getColor("black");
        tbs.pressedOffsetX = Math.round(1f * Gdx.graphics.getDensity());
        tbs.pressedOffsetY = tbs.pressedOffsetX * -1f;
        skin.add("default", tbs);

        tbs = new TextButtonStyle(tabInactive, tabActive, tabActive, fontMap.get("default-font"));
        tbs.fontColor = skin.getColor("black");
        skin.add("tab", tbs);

        return skin;

    }
}
