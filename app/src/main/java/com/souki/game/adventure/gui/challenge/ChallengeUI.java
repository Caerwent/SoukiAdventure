package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.InventorySlot;
import com.souki.game.adventure.gui.UIStage;
import com.souki.game.adventure.interactions.InteractionChallenge;
import com.souki.game.adventure.interactions.InteractionEvent;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.screens.GenericUI;

import java.util.HashMap;

/**
 * Created by vincent on 04/04/2017.
 */

public class ChallengeUI extends Window {

    public static enum ChallengeType {
        TEST(ChallengeTest.class),
        MACHINE(ChallengeMachine.class),
        PUZZLE(ChallengeSlidingPuzzle.class),
        PORTAL_CHECKPOINT(ChallengePortalCheckpoint.class),
        STEAM_MACHINE(ChallengeSteamMachine.class),
        INPUT_CODE(ChallengeInputCode.class);

        protected Class mClass;

        ChallengeType(Class aClass) {
            mClass = aClass;
        }
    }

    public static ChallengeUI createInstance(ChallengeType aType) {

        try {
            return (ChallengeUI) aType.mClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

    protected Table mTable;
    protected Image mClose;
    protected Table mContent;
    protected InteractionChallenge mInteractionChallenge;
    protected boolean mSizeInvalid = true;

    public ChallengeUI() {
        super("", GenericUI.getInstance().getSkin(), "solidbackground");

        mClose = new Image(GenericUI.getInstance().getTextureAtlas().findRegion("checkbox_cross"));
        mClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                UIStage.getInstance().closeChallengeUI();
            }
        });
        add(mClose).top().right().expandX();
        row();
        mContent = new Table();
        add(mContent).top().left().fill().expand();
        createView();

    }

    public void setInteractionChallenge(InteractionChallenge aInteractionChallenge, HashMap aProperties) {
        mInteractionChallenge = aInteractionChallenge;
        setProperties(aProperties);
        createDragAndDropTarget(UIStage.getInstance().getMainHUD().getItemDragAndDrop());
    }

    public void restoreFromPersistence() {
        if (mInteractionChallenge != null) {
            mInteractionChallenge.restoreFromPersistence();
        }
    }

    protected void saveInPersistence() {
        if (mInteractionChallenge != null) {
            mInteractionChallenge.saveInPersistence();
        }
    }


    protected void setProperties(HashMap aProperties) {

    }

    protected void createDragAndDropTarget(DragAndDrop aDragAndDrop) {

    }

    protected void removeDragAndDropTarget(DragAndDrop aDragAndDrop) {

    }

    public void onItemDrop(InventorySlot aSourceSlot) {

    }

    protected void createView() {

    }

    protected void layoutView() {

    }

    @Override
    public void layout() {
        super.layout();
        if (mSizeInvalid) {
            mSizeInvalid = false;
            layoutView();
        }
    }

    public void release() {
        if (UIStage.getInstance().getMainHUD() != null) {
            removeDragAndDropTarget(UIStage.getInstance().getMainHUD().getItemDragAndDrop());
        }
    }

    public void restoreFromPersistence(GameSession aGameSession) {

    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        return aGameSession;
    }

    protected void challengeCompleted() {
        EventDispatcher.getInstance().onInteractionEvent(new InteractionEvent(mInteractionChallenge.getId(), InteractionEvent.EventType.CHALLENGE_COMPLETED.name(), null));
    }
}
