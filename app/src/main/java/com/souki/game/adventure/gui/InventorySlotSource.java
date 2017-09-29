package com.souki.game.adventure.gui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Source;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Target;
import com.badlogic.gdx.scenes.scene2d.utils.DragScrollListener;
import com.badlogic.gdx.utils.Scaling;

/**
 * Created by vincent on 24/02/2017.
 */

public class InventorySlotSource extends Source {

    private DragAndDrop _dragAndDrop;
    private InventorySlot _sourceSlot;
    protected DragScrollListener mDragListener;
private Image img;
    public InventorySlotSource(InventorySlot sourceSlot, DragAndDrop dragAndDrop, DragScrollListener aDragListener) {
        super(sourceSlot);
        this._sourceSlot = sourceSlot;
        this._dragAndDrop = dragAndDrop;
        mDragListener = aDragListener;
        //getActor().addCaptureListener(mDragListener);
    }

    @Override
    public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        Payload payload = new Payload();

        img = new Image(_sourceSlot.getItemOnTop().getTextureRegion());
        img.setSize(_sourceSlot.getWidth(), _sourceSlot.getHeight());
        img.setScaling(Scaling.fit);
        Color color = img.getColor();
        color.a*=0.5;
        img.setColor(color);
        _sourceSlot.addActor(img);
        img.addCaptureListener(mDragListener);
        _sourceSlot.addListener(mDragListener);
        payload.setDragActor(img);
        _dragAndDrop.setDragActorPosition(x-img.getWidth()/2, y - img.getHeight()/2);

        return payload;
    }

    @Override
    public void dragStop (InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
        _sourceSlot.removeActor(img);
        img=null;
    }

    public InventorySlot getSourceSlot() {
        return _sourceSlot;
    }
}