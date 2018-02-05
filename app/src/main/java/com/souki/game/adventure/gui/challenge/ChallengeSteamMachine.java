package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.AnimatedImage;
import com.souki.game.adventure.gui.InventorySlot;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 07/04/2017.
 */

public class ChallengeSteamMachine extends ChallengeUI {

    private static final String KEY_HAS_GEAR = "has_gear";
    private static final String KEY_HAS_COAL = "has_coal";
    private static final String KEY_IS_RESOLVED = "is_resolved";

    private TextureAtlas mAtlas;
    private AnimatedImage mImage;
    private ChallengeTarget mDropTarget;
    private Table mGroup;
    private Table mHelpTable;

    private boolean mHasGear = false;
    private boolean mHasCoal = false;
    private boolean mIsResolved = false;


    public ChallengeSteamMachine() {
        super();


    }

    @Override
    protected void createView() {
        mAtlas = new TextureAtlas("data/interactions/challenge_steam_machine.atlas");
        mGroup = new Table();
        mImage = new AnimatedImage();

        mHelpTable = new Table();
        mContent.top().add(mHelpTable);
        mContent.row();
        Image gearImg = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.Gear).getTextureRegion());
        gearImg.setScaling(Scaling.none);
        mHelpTable.add(gearImg);

        Label helpPlus = new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus.setAlignment(Align.center);
        mHelpTable.add(helpPlus);

        Image coalImg = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.Coal).getTextureRegion());
        coalImg.setScaling(Scaling.none);
        mHelpTable.add(coalImg);


        mContent.align(Align.center);
        mContent.top().add(mGroup).pad(10).expand().fill().center();
       // mGroup.addActor(mImage);
        mGroup.center().add(mImage).center();
        mImage.setScaling(Scaling.fit);
        mImage.setAlign(Align.center);


    }

    public void restoreFromPersistence(GameSession aGameSession) {
        Boolean hasGear = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_GEAR);
        if (hasGear != null) {
            mHasGear = hasGear.booleanValue();
        }
        Boolean hasCoal = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_COAL);
        if (hasCoal != null) {
            mHasCoal = hasCoal.booleanValue();
        }
        Boolean isResolved = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED);
        if (isResolved != null) {
            mIsResolved = isResolved.booleanValue();
        }

        updateUI();
    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_GEAR, mHasGear);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_COAL, mHasCoal);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED, mIsResolved);
        return aGameSession;
    }

    @Override
    protected void createDragAndDropTarget(DragAndDrop aDragAndDrop) {
        mDropTarget = new ChallengeTarget(mImage, this);
        aDragAndDrop.addTarget(mDropTarget);
    }

    @Override
    protected void removeDragAndDropTarget(DragAndDrop aDragAndDrop) {
        aDragAndDrop.removeTarget(mDropTarget);
    }

    @Override
    public void onItemDrop(InventorySlot aSourceSlot) {
        if (!mHasGear && aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.Gear)) {
            mHasGear = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        else if (!mHasCoal && aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.Coal)) {
            mHasCoal = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        else
        {
            return;
        }
        checkIsResolved();
        updateUI();
        mInteractionChallenge.saveInPersistence();

    }

    protected void checkIsResolved() {
        mIsResolved = mHasCoal && mHasGear;
        if(mIsResolved) {
            challengeCompleted();
        }
    }


    protected void updateUI() {
        if(mIsResolved)
        {
            mImage.setState(mInteractionChallenge.getState("COMPLETED_BIG"));
            mInteractionChallenge.setState("COMPLETED");
        }
        else if(mHasGear && !mHasCoal)
        {
            mImage.setState(mInteractionChallenge.getState("HAS_GEAR_ONLY_BIG"));
            mInteractionChallenge.setState("HAS_GEAR_ONLY");
        }
        else if(!mHasGear && mHasCoal)
        {
            mImage.setState(mInteractionChallenge.getState("HAS_FIRE_ONLY_BIG"));
            mInteractionChallenge.setState("HAS_FIRE_ONLY");
        }
        else
        {
            mImage.setState(mInteractionChallenge.getState("IDLE_BIG"));
            mInteractionChallenge.setState("IDLE");
        }
    }
}
