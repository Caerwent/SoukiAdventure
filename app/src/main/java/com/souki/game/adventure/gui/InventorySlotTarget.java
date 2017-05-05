package com.souki.game.adventure.gui;

import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemCombinaison;
import com.souki.game.adventure.items.ItemFactory;

/**
 * Created by vincent on 24/02/2017.
 */

public class InventorySlotTarget extends Target {

    InventorySlot _targetSlot;

    public InventorySlotTarget(InventorySlot actor) {
        super(actor);
        _targetSlot = actor;
    }

    @Override
    public boolean drag(Source source, Payload payload, float x, float y, int pointer) {
        return true;
    }

    @Override
    public void reset(Source source, Payload payload) {
    }

    @Override
    public void drop(Source source, Payload payload, float x, float y, int pointer) {
        InventorySlot sourceSlot = ((InventorySlotSource) source).getSourceSlot();

        if (sourceSlot ==_targetSlot) {
            return;
        }

        //First, does the target accept the source item type?
        Item sourceItem = sourceSlot.getItemOnTop();
        Item targetItem = _targetSlot.getItemOnTop();
        if(sourceItem!=null && targetItem!=null)
        {
            ItemCombinaison combinaison = ItemFactory.getInstance().getItemCombinaison(sourceItem.getItemTypeID(), targetItem.getItemTypeID());
            if(combinaison!=null && combinaison.resultItems !=null)
            {
                for(Item.ItemTypeID itemId : combinaison.resultItems )
                {
                    EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(itemId));
                }
                EventDispatcher.getInstance().onItemLost(sourceItem);
                EventDispatcher.getInstance().onItemLost(targetItem);


            }

        }



    }
}
