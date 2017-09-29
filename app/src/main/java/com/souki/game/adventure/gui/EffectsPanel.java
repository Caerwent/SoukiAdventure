package com.souki.game.adventure.gui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.Settings;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.effects.EffectFactory;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.screens.GenericUI;

import java.util.ArrayList;

/**
 * Created by vincent on 05/03/2017.
 */

public class EffectsPanel extends Table {

    private Table mList = new Table();
    private ArrayList<Table> mSlots = new ArrayList<>();
    private Table mSelectedItem;

    private InventoryDetails mDetails;

    public EffectsPanel() {
        super();
        init();
    }


    private void init() {
        mList.setSkin(GenericUI.getInstance().getSkin());
        mList.setName("Effects_Slot_Table");
        mList.padLeft(5).padRight(10);
        mDetails = new InventoryDetails(200, (Settings.TARGET_HEIGHT - 64) / 2);
        ScrollPane scrollPane = new ScrollPane(mList, GenericUI.getInstance().getSkin(), "inventoryPane");
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFlickScroll(true);
        add(scrollPane).fillY().expand().left();

        add(mDetails).top().left();
        row();

    }

    public void update() {
        mList.clear();
        mSlots.clear();
        int idx = 0;
        ArrayList<Effect.Type> availableEffects = Profile.getInstance().getAvailableEffects();
        for (Effect.Type effectType : Effect.Type.values()) {
            Effect effect = EffectFactory.getInstance().getEffect(effectType);
            Table slot = new Table();
            slot.setBackground(GenericUI.getInstance().getSkin().getDrawable("window1"));
            mSlots.add(slot);
            slot.setColor(GenericUI.getInstance().getSkin().getColor("background-color-1"));
            slot.setSize(68,68);


            if(availableEffects!=null && availableEffects.contains(effectType)) {
                Image img = new Image(effect.getIcon());
                img.setAlign(Align.center);
                img.setScaling(Scaling.fit);

                slot.center().add(img).size(68,68);
                slot.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        int idx = mSlots.indexOf(event.getListenerActor());
                        if (idx >= 0) {
                            Effect clickedEffect = EffectFactory.getInstance().getEffect(Effect.Type.values()[idx]);
                            Effect.Type selectedEffectType = Profile.getInstance().getSelectedEffect();
                            if (selectedEffectType != null) {
                                Effect selectedEffect = EffectFactory.getInstance().getEffect(selectedEffectType);
                                if (selectedEffect != clickedEffect) {
                                    EventDispatcher.getInstance().onNewSelectedEffect(Effect.Type.values()[idx]);
                                }
                            }
                            else
                            {
                                EventDispatcher.getInstance().onNewSelectedEffect(Effect.Type.values()[idx]);
                            }

                        }
                    }
                });
            }
            slot.row();
            Effect.Type selectedEffectType = Profile.getInstance().getSelectedEffect();
            if (selectedEffectType != null && selectedEffectType == effectType) {
                setSelected(idx);

            }
            idx++;

            mList.top().left().add(slot);
            mList.row();
        }

    }


    private void setSelected(int idx) {
        if (idx >= 0) {
            if (mSelectedItem != null) {
                mSelectedItem.setColor(GenericUI.getInstance().getSkin().getColor("background-color-1"));
            }
            mSelectedItem = mSlots.get(idx);
            mSelectedItem.setColor(GenericUI.getInstance().getSkin().getColor("background-color-2"));

            mDetails.setText(EffectFactory.getInstance().getEffect(Effect.Type.values()[idx]).description);
        }

    }
}
