package com.souki.game.adventure.player;

import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.IItemListener;
import com.souki.game.adventure.interactions.InteractionFactory;
import com.souki.game.adventure.interactions.InteractionHero;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.Profile;

import java.util.ArrayList;

/**
 * Created by gwalarn on 05/12/16.
 */

public class Player implements IItemListener {


    private Array<Item> mInventory;
    private InteractionHero mHero;

    public Player(GameMap aGameMap) {
        mInventory = new Array<Item>();
        ArrayList<String> savedInventory = Profile.getInstance().getInventory();

        if (savedInventory != null) {
            for (String itemId : savedInventory) {
                mInventory.add(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(itemId)));
            }
            updateHelpBook();
        }
        // instantiate the hero
        mHero = InteractionFactory.getInstance().createInteractionHero(aGameMap);
        EventDispatcher.getInstance().addItemListener(this);
        EventDispatcher.getInstance().onInventoryChanged(this);
    }

    public void destroy() {
        EventDispatcher.getInstance().removeItemListener(this);
    }


    public void updateHelpBook() {
        Array<Item> book = getItemsInventoryById(Item.ItemTypeID.Book.name());
        if (book != null && book.size > 0) {
            Item helpBook = book.get(0);
            ArrayList<String> foundScrolls = Profile.getInstance().getFoundScrolls();
            if (foundScrolls != null) {
                String genericStart = AssetsUtility.getString("item_scroll_generic_start");
                String helpBookContent = helpBook.getItemShortDescription();
                for (String scrollname : foundScrolls) {
                    Item scroll = ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(scrollname));
                    if (scroll != null) {
                        helpBookContent += "\\n" + scroll.getItemShortDescription().substring(genericStart.length());
                    }
                }
                helpBook.setItemShortDescription(helpBookContent);

            }

        }
    }

    public void addItem(Item aItem) {
        if (!mInventory.contains(aItem, true)) {
            mInventory.add(aItem);
            Profile.getInstance().updateInventory(mInventory);
            EventDispatcher.getInstance().onInventoryChanged(this);

        }
    }

    public void removeItem(Item aItem) {
        if (mInventory.contains(aItem, true)) {
            mInventory.removeValue(aItem, true);
            Profile.getInstance().updateInventory(mInventory);
            EventDispatcher.getInstance().onInventoryChanged(this);

        }
    }

    @Override
    public void onItemFound(Item aItem) {
        addItem(aItem);
    }

    @Override
    public void onItemLost(Item aItem) {
        removeItem(aItem);
    }

    public Array<Item> getInventory() {
        return mInventory;
    }

    public Array<Item> getItemsInventoryById(String aItemId) {

        Array<Item> result = new Array<Item>();
        for (Item item : mInventory) {
            if (item.getItemTypeID().name().equals(aItemId)) {
                result.add(item);
            }
        }
        return result;
    }

    public InteractionHero getHero() {
        return mHero;
    }
}
