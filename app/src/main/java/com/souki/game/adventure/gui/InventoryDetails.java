package com.souki.game.adventure.gui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 16/12/2016.
 */

public class InventoryDetails extends Table {


    private Label mItemDescLabel;
    private ScrollPane mScrollPane;


    public InventoryDetails(int aWidth, int aHeight) {
        setSize(aWidth, aHeight);
        mItemDescLabel = new Label("", GenericUI.getInstance().getSkin(), "inventory-detail");
        mItemDescLabel.setWrap(true);
        mItemDescLabel.setSize(aWidth, aHeight);
        mItemDescLabel.setAlignment(Align.topLeft);
        mScrollPane = new ScrollPane(mItemDescLabel, GenericUI.getInstance().getSkin(), "inventoryPane");
        this.add(mScrollPane).width(aWidth).top().left().pad(5).expand().fill();
        mScrollPane.setScrollingDisabled(true, false);
        mScrollPane.setFadeScrollBars(false);
        mScrollPane.setFlickScroll(true);
        setSkin(GenericUI.getInstance().getSkin());
        setName("Inventory_Details");
        row();


    }

    public void setText(String aText) {
        mItemDescLabel.setText(aText);
        //mItemDescLabel.setPrefRows(aItem.getItemShortDescription().split("\n").length);
        mScrollPane.layout();
    }

}
