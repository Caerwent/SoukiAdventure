package com.souki.game.adventure.events;

import com.souki.game.adventure.items.Item;

/**
 * Created by vincent on 13/01/2017.
 */

public interface IItemListener {
    public void onItemFound(Item aItem);
    public void onItemLost(Item aItem);
}
