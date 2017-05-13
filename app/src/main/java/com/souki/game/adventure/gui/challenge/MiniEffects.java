package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.effects.EffectFactory;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.screens.GenericUI;

import java.util.ArrayList;

/**
 * Created by vincent on 12/05/2017.
 */

public class MiniEffects extends Table {
    private ArrayList<Table> mSlots = new ArrayList<>();
    private ArrayList<Effect.Type> mEffects = new ArrayList<>();

    public MiniEffects() {
        super();
        init();
    }


    private void init() {
        setSkin(GenericUI.getInstance().getSkin());
        update();

    }

    public void update() {
        mSlots.clear();
        mEffects.clear();
        clear();

        ArrayList<Effect.Type> availableEffects = Profile.getInstance().getAvailableEffects();
        for (Effect.Type effectType : Effect.Type.values()) {
            Effect effect = EffectFactory.getInstance().getEffect(effectType);

            Table slot = new Table();
            slot.setBackground(GenericUI.getInstance().getSkin().getDrawable("window1"));
            mSlots.add(slot);
            slot.setColor(GenericUI.getInstance().getSkin().getColor("background-color-1"));
            slot.setSize(64, 64);
            mEffects.add(effectType);

            if (availableEffects != null && availableEffects.contains(effectType)) {
                Image img = new Image(effect.getIcon());
                img.setAlign(Align.center);
                img.setScaling(Scaling.fit);

                slot.center().add(img).size(64, 64);
                slot.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        int idx = mSlots.indexOf(event.getListenerActor());
                        if (idx >= 0) {
                            EventDispatcher.getInstance().onNewSelectedEffect(mEffects.get(idx));
                        }
                    }
                });
            }
            slot.row();

            left().add(slot);
        }

    }


}
