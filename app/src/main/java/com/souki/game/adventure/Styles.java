package com.souki.game.adventure;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * @author trey miller
 */
public class Styles {

    public void styleSkin(Skin skin, TextureAtlas atlas) {
        BitmapFont font18 = new BitmapFont(Gdx.files.internal("data/fonts/sans_serif_18.fnt"), false);
        skin.add("default", font18);


      /*  FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/fonts/CMSansSerif2012.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 18;
        parameter.color = Color.WHITE;
        BitmapFont font18 = generator.generateFont(parameter); // font white color, size 18 pixels
        generator.dispose();
        skin.add("default", font18, BitmapFont.class);*/
        LabelStyle lbs = new LabelStyle();
        lbs.font = font18;
        lbs.fontColor = Color.WHITE;
        skin.add("default", lbs);


     /*   parameter.size = 10;
        BitmapFont font10 = generator.generateFont(parameter); // font white color, size 18 pixels
        generator.dispose();
        skin.add("default-font", font10, BitmapFont.class);

        parameter.size = 6;
        BitmapFont font6 = generator.generateFont(parameter); // font white color, size 18 pixels
        generator.dispose();
        skin.add("small-font", font10, BitmapFont.class);
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




        TextButtonStyle tbs = new TextButtonStyle(btn1up, btn1down, btn1down, font18);
        tbs.fontColor = skin.getColor("black");
        tbs.pressedOffsetX = Math.round(1f * Gdx.graphics.getDensity());
        tbs.pressedOffsetY = tbs.pressedOffsetX * -1f;
        skin.add("default", tbs);

        tbs = new TextButtonStyle(tabInactive, tabActive, tabActive, font18);
        tbs.fontColor = skin.getColor("black");
        skin.add("tab", tbs);

    }
}
