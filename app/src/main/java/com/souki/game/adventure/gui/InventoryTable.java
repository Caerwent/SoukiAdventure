package com.souki.game.adventure.gui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragScrollListener;
import com.badlogic.gdx.utils.ArrayMap;
import com.souki.game.adventure.Settings;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.IPlayerListener;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.player.Player;
import com.souki.game.adventure.screens.GenericUI;

import java.util.ArrayList;

/**
 * Created by vincent on 09/12/2016.
 */

public class InventoryTable extends Table implements IPlayerListener {
    private final int mSlotWidth = 68;
    private final int mSlotHeight = 68;
    private int mLengthSlotRow = 2;
    private Table mInventoryTable;
    private ScrollPane mScrollPane;
    protected DragScrollListener mDragListener;
    private ArrayMap<Item.ItemTypeID, InventorySlot> mSlots;
    private InventorySlot mSelectedItem;
    private InventoryDetails mDetails;
    ArrayList<InventorySlotTarget> mSlotTargets = new ArrayList<>();
    ArrayList<InventorySlotSource> mSlotSources = new ArrayList();

    private DragAndDrop _dragAndDrop;

    public InventoryTable() {
        super();
        init();
    }


    private void enableDragScroll(boolean aIsEnabled) {
        if (aIsEnabled) {
            _dragAndDrop.setCancelTouchFocus(false);
            mScrollPane.setFlickScroll(false);
            mScrollPane.setCancelTouchFocus(false);
            mScrollPane.addListener(mDragListener);
        } else {
            _dragAndDrop.setCancelTouchFocus(true);
            mScrollPane.setFlickScroll(true);
            mScrollPane.setCancelTouchFocus(true);
            mScrollPane.removeListener(mDragListener);
        }

    }

    private void init() {
        //setBackground(UIStage.getInstance().getSkin().getDrawable("window1"));
        // setColor(UIStage.getInstance().getSkin().getColor("lt-blue"));
        mSlots = new ArrayMap();
        _dragAndDrop = new DragAndDrop();
        _dragAndDrop.setCancelTouchFocus(true);
        mInventoryTable = new Table();

        mInventoryTable.setSkin(GenericUI.getInstance().getSkin());
        mInventoryTable.setName("Inventory_Slot_Table");
        mInventoryTable.padLeft(5).padRight(10);
        EventDispatcher.getInstance().addPlayerListener(this);
        mDetails = new InventoryDetails(200, (Settings.TARGET_HEIGHT - 64) / 2);

        mScrollPane = new ScrollPane(mInventoryTable, GenericUI.getInstance().getSkin(), "inventoryPane");
        mScrollPane.setScrollingDisabled(true, false);
        mScrollPane.setFadeScrollBars(false);
        mScrollPane.setFlickScroll(true);
        mScrollPane.setCancelTouchFocus(true);
        mScrollPane.setSize(150, (Settings.TARGET_HEIGHT - 64) / 2);
        mDragListener = new DragScrollListener(mScrollPane);
        //   mScrollPane.addListener(mDragListener);

        add(mScrollPane).top().left().expandY().fillX();
        add(mDetails).top().right().expand().fill();
        row();
        mDetails.setVisible(false);


    }

    public DragAndDrop getDragAndDrop() {
        return _dragAndDrop;
    }

    public void destroy() {
        EventDispatcher.getInstance().removePlayerListener(this);
    }

    public void update(Player aPlayer) {
        int nbItemInRow = 0;
        mInventoryTable.clear();
        for (InventorySlotTarget slotTarget : mSlotTargets) {
            _dragAndDrop.removeTarget(slotTarget);
        }
        mSlotTargets.clear();
        for (InventorySlotSource slotSource : mSlotSources) {
            _dragAndDrop.removeSource(slotSource);
        }
        mSlotSources.clear();
        mSlots.clear();
        mSelectedItem = null;

        for (Item item : aPlayer.getInventory()) {
            InventorySlot inventorySlot = mSlots.get(item.getItemTypeID());
            if (inventorySlot == null) {
                inventorySlot = new InventorySlot();

                mSlots.put(item.getItemTypeID(), inventorySlot);
                inventorySlot.setTouchable(Touchable.enabled);
                mInventoryTable.top().add(inventorySlot).size(mSlotWidth, mSlotHeight);
                inventorySlot.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        InventorySlot newSelectedItem = null;
                        if (event.getListenerActor() instanceof InventorySlot) {
                            newSelectedItem = (InventorySlot) event.getListenerActor();
                            if (mSelectedItem != newSelectedItem) {
                                if (mSelectedItem != null) {
                                    mSelectedItem.setSelected(false);
                                    _dragAndDrop.removeSource(mSlotSources.get(0));
                                    mSlotSources.remove(0);
                                }
                                mSelectedItem = newSelectedItem;
                                mSelectedItem.setSelected(true);
                                mDetails.setText(mSelectedItem.getItemOnTop().getItemShortDescription());
                                mDetails.setVisible(true);

                                InventorySlotSource source = new InventorySlotSource(mSelectedItem, _dragAndDrop, mDragListener);
                                mSlotSources.add(source);
                                //  mSelectedItem.addCaptureListener(mDragListener);
                                //   mSelectedItem.addListener(mDragListener);
                                _dragAndDrop.addSource(source);

                            } else if (mSelectedItem != null && mSelectedItem == newSelectedItem) {
                                mDetails.setVisible(false);
                                mSelectedItem.setSelected(false);
                                mSelectedItem = null;
                                _dragAndDrop.removeSource(mSlotSources.get(0));
                                mSlotSources.remove(0);
                            }

                        }
                        enableDragScroll(mSelectedItem != null);


                    }
                });
                InventorySlotTarget target = new InventorySlotTarget(inventorySlot);
                mSlotTargets.add(target);
                _dragAndDrop.addTarget(target);

                nbItemInRow++;
                if (nbItemInRow % mLengthSlotRow == 0) {
                    mInventoryTable.row();
                }


            }
            inventorySlot.add(item);


        }
        mScrollPane.layout();
        mScrollPane.updateVisualScroll();
    }

    @Override
    public void onInventoryChanged(Player aPlayer) {
        update(aPlayer);
    }
}
