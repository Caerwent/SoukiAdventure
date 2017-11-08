package com.souki.game.adventure.gui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.Settings;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.screens.GenericUI;

import java.util.ArrayList;

/**
 * Created by vincent on 05/03/2017.
 */

public class HelpPanel extends Table {


    private final int mSlotWidth = 48;
    private final int mSlotHeight = 48;
    private Table mInventoryTable;
    private ArrayMap<String, InventorySlot> mSlots;
    private InventorySlot mSelectedItem;

    private InventoryDetails mDetails;


    public HelpPanel() {
        super();
        init();
    }


    private void init() {

        mSlots = new ArrayMap();
        mInventoryTable = new Table();
        //      mInventoryTable.setBackground(UIStage.getInstance().getSkin().getDrawable("window1"));
        //     mInventoryTable.setColor(UIStage.getInstance().getSkin().getColor("lt-green"));

        mInventoryTable.setSkin(GenericUI.getInstance().getSkin());
        //  mInventoryTable.align(Align.topLeft);
        mInventoryTable.setName("Inventory_Slot_Table");
        mInventoryTable.padLeft(5).padRight(10);
        //  mInventoryTable.setPosition(0,25);
        //  mInventoryTable.setSize(mLengthSlotRow*mSlotWidth+5, Settings.TARGET_HEIGHT - 64);

        mDetails = new InventoryDetails(300, (Settings.TARGET_HEIGHT - 64) / 2);

        ScrollPane scrollPane = new ScrollPane(mInventoryTable, GenericUI.getInstance().getSkin(), "inventoryPane");
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setFlickScroll(true);
        scrollPane.setCancelTouchFocus(true);
        add(scrollPane).fillY().expand().left();

        add(mDetails).top().left();
        row();
        // mDetails.setPosition(mLengthSlotRow*mSlotWidth+5, (Settings.TARGET_HEIGHT - 64)/2+25);
        mDetails.setVisible(false);


    }

    public void update() {

        mInventoryTable.clear();
        mSlots.clear();
        ArrayList<String> foundScrolls = Profile.getInstance().getFoundScrolls();
        if (foundScrolls != null) {
            final String genericStart = AssetsUtility.getString("item_scroll_generic_start");


            for (int i = 0; i < foundScrolls.size(); i++) {
                InventorySlot inventorySlot = mSlots.get(foundScrolls.get(i));
                if (inventorySlot == null) {
                    inventorySlot = new InventorySlot();
                    Item scroll = ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(foundScrolls.get(i)));
                    mSlots.put(foundScrolls.get(i), inventorySlot);
                    inventorySlot.setTouchable(Touchable.enabled);
                    mInventoryTable.top().add(inventorySlot).size(mSlotWidth, mSlotHeight);
                    inventorySlot.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            InventorySlot newSelectedItem = null;
                            if (event.getListenerActor() instanceof InventorySlot) {
                                newSelectedItem = (InventorySlot) event.getListenerActor();
                                if (mSelectedItem != newSelectedItem) {
                                    if (mSelectedItem != null)
                                        mSelectedItem.setSelected(false);
                                    mSelectedItem = newSelectedItem;
                                    mSelectedItem.setSelected(true);
                                    String page = mSelectedItem.getItemOnTop().getItemShortDescription().substring(genericStart.length());

                                    mDetails.setText(page);
                                    mDetails.setVisible(true);
                                } else if (mSelectedItem != null && mSelectedItem == newSelectedItem) {
                                    mDetails.setVisible(false);
                                    mSelectedItem.setSelected(false);
                                    mSelectedItem = null;
                                }

                            }

                        }
                    });

                    mInventoryTable.row();


                    inventorySlot.add(scroll);
                }


            }
        }

    }


}
