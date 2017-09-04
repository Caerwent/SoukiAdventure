package com.souki.game.adventure.gui;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 09/12/2016.
 */

public class InventorySlot extends Stack {

    //All slots have this default image
    private Table _defaultBackground;
    private Image _mImg;
    private Label _numItemsLabel;
    private int _numItemsVal = 0;
    private Item.ItemTypeID _filterItemType;
    private Array<Item> mItems;
    private boolean mIsSelected = false;


    public InventorySlot() {
        _defaultBackground = new Table();
        _defaultBackground.setBackground(GenericUI.getInstance().getSkin().getDrawable("window1"));
        _defaultBackground.setColor(GenericUI.getInstance().getSkin().getColor("background-color-1"));
        _numItemsLabel = new Label(String.valueOf(_numItemsVal), GenericUI.getInstance().getSkin(), "inventory-item-count");
        _numItemsLabel.setAlignment(Align.bottomRight);
        _numItemsLabel.setVisible(false);
        setTouchable(Touchable.enabled);
        mItems = new Array<Item>();
        this.add(_defaultBackground);
        this.add(_numItemsLabel);
    }


    public void updateItemCount() {
        _numItemsVal++;
        _numItemsLabel.setText(String.valueOf(mItems.size));

        checkVisibilityOfItemCount();

    }


    public void add(Item aItem) {
        if (_numItemsLabel == null) {
            return;
        }

        if (mItems.size <= 0) {
            _filterItemType = aItem.getItemTypeID();
            mItems.add(aItem);
            _mImg = new Image(aItem.getTextureRegion());
            _defaultBackground.add(_mImg);
            updateItemCount();
        } else if (!mItems.contains(aItem, true)) {
            mItems.add(aItem);
            updateItemCount();

        }

    }

    public void remove(Item aItem) {

        if (_numItemsLabel == null) {
            return;
        }

        if (mItems.contains(aItem, true)) {
            mItems.removeValue(aItem, true);
            updateItemCount();

        }
        if (mItems.size <= 0) {
            _defaultBackground.removeActor(_mImg);
            _mImg = null;
        }
    }


    private void checkVisibilityOfItemCount() {
        if (mItems.size < 2) {
            _numItemsLabel.setVisible(false);
        } else {
            _numItemsLabel.setVisible(true);
        }
    }


    public int getNumItems() {
        return mItems.size;
    }

    public Item getItemOnTop() {
        if (mItems.size <= 0) {
            return null;
        } else {
            return mItems.first();
        }
    }

    public boolean doesAcceptItemUseType(Item.ItemTypeID aType) {
        return mItems.size <= 0 || _filterItemType == aType;
    }

    public void setSelected(boolean aIsSelected) {
        mIsSelected = aIsSelected;
        _defaultBackground.setColor(GenericUI.getInstance().getSkin().getColor(aIsSelected ? "background-color-2" : "background-color-1"));
    }

    public void setDragOver(boolean aIsDragOver) {

        if (aIsDragOver) {
            _defaultBackground.setColor(GenericUI.getInstance().getSkin().getColor("background-color-3"));
        } else {
            setSelected(mIsSelected);
        }

    }


}
