package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.InventorySlot;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 07/04/2017.
 */

public class ChallengeMachine extends ChallengeUI {

    private static final int LIQUID_FULL_X = 38;
    private static final int LIQUID_FULL_Y = -116;
    private static final int LIQUID_RESULT_NONE = 0;
    private static final int LIQUID_RESULT_BLUE = 1;
    private static final int LIQUID_RESULT_RED = 2;
    private static final int LIQUID_RESULT_YELLOW_OR_GREEN = 3;
    private static final int LIQUID_RESULT_BLUE_RED = 4;
    private static final int LIQUID_RESULT_BLUE_YELLOW_OR_GREEN = 5;
    private static final int LIQUID_RESULT_RED_YELLOW_OR_GREEN = 6;
    private static final int LIQUID_RESULT_BLUE_RED_YELLOW_OR_GREEN = 7;

    private static final String KEY_HAS_BLUE = "has_blue";
    private static final String KEY_HAS_RED = "has_red";
    private static final String KEY_HAS_YELLOW_OR_GREEN = "has_yellow_or_green";
    private static final String KEY_MIX_RESULT = "mix_result";
    private static final String KEY_HAS_ACTIVATOR = "has_activator";

    private TextureAtlas mAtlas;
    private Image mBackground;
    private Image mBlue, mBubbleBlue;
    private Image mRed, mBubbleRed;
    private Image mYellowOrGreen, mBubbleYellowOrGreen;
    private Image mMixImg;
    private Image mActivatorOn;
    private Image mActivatorOff;
    private Image mPipe;
    private ChallengeTarget mBackgroundTarget;
    private Group mGroup;
    private Table mHelpTable;

    private boolean mHasBlue = false;
    private boolean mHasRed = false;
    private boolean mHasYellowOrGreen = false;
    private int mMixResult = LIQUID_RESULT_NONE;
    private boolean mHasActivator = false;
    private boolean mIsActivated = false;

    public final static boolean IS_ADDITIVE = false;

    public ChallengeMachine() {
        super();
    }

    @Override
    protected void createView() {
        mAtlas = new TextureAtlas("data/challenges/the_machine.atlas");
        mGroup = new Group();
        mBackground = new Image(mAtlas.findRegion("theMachine_bkg"));
        mBlue = new Image(mAtlas.findRegion("theMachine_liquid_blue"));
        mBlue.setScaling(Scaling.none);
        mBlue.setVisible(false);
        mBubbleBlue = new Image(mAtlas.findRegion("theMachine_bubble_on"));
        mBubbleBlue.setScaling(Scaling.none);
        mBubbleBlue.setVisible(false);
        mRed = new Image(mAtlas.findRegion("theMachine_liquid_red"));
        mRed.setScaling(Scaling.none);
        mRed.setVisible(false);
        mBubbleRed = new Image(mAtlas.findRegion("theMachine_bubble_on"));
        mBubbleRed.setScaling(Scaling.none);
        mBubbleRed.setVisible(false);
        mYellowOrGreen = new Image(IS_ADDITIVE ? mAtlas.findRegion("theMachine_liquid_green") :mAtlas.findRegion("theMachine_liquid_yellow"));
        mYellowOrGreen.setScaling(Scaling.none);
        mYellowOrGreen.setVisible(false);
        mBubbleYellowOrGreen = new Image(mAtlas.findRegion("theMachine_bubble_on"));
        mBubbleYellowOrGreen.setScaling(Scaling.none);
        mBubbleYellowOrGreen.setVisible(false);
        mPipe = new Image(mAtlas.findRegion("theMachine_pipe"));
        mPipe.setScaling(Scaling.none);
        mPipe.setVisible(true);
        mPipe.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onPipe();
            }
        });

        mActivatorOn = new Image(mAtlas.findRegion("theMachine_liquid_activator_on"));
        mActivatorOn.setScaling(Scaling.none);
        mActivatorOn.setVisible(false);
        mActivatorOn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onActivate(false);
            }
        });

        mActivatorOff = new Image(mAtlas.findRegion("theMachine_liquid_activator_off"));
        mActivatorOff.setScaling(Scaling.none);
        mActivatorOff.setVisible(false);
        mActivatorOff.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onActivate(true);
            }
        });

        mHelpTable = new Table();
        mContent.top().add(mHelpTable).expandX().padBottom(20);
        mContent.row().top().fill().center();
        Image helpVialBlue = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialBlue).getTextureRegion());
        helpVialBlue.setScaling(Scaling.none);
        mHelpTable.add(helpVialBlue);

        Label helpPlus=new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus.setAlignment(Align.center);
        mHelpTable.add(helpPlus);

        Image helpVialGreen = new Image(IS_ADDITIVE ? ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialGreen).getTextureRegion() :
                ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialYellow).getTextureRegion());
        helpVialGreen.setScaling(Scaling.none);
        mHelpTable.add(helpVialGreen);

        Label helpPlus2=new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus2.setAlignment(Align.center);
        mHelpTable.add(helpPlus2);

        Image helpVialRed = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialRed).getTextureRegion());
        helpVialRed.setScaling(Scaling.none);
        mHelpTable.add(helpVialRed);

        Label helpPlus3=new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus3.setAlignment(Align.center);
        mHelpTable.add(helpPlus3);

        Image helpActivator = new Image(mAtlas.findRegion("theMachine_liquid_activator_on"));
        helpActivator.setScaling(Scaling.none);
        mHelpTable.add(helpActivator);

        Label helpPlus4=new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus4.setAlignment(Align.center);
        mHelpTable.add(helpPlus4);

        Image helpPipe = new Image(mAtlas.findRegion("theMachine_pipe"));
        helpPipe.setScaling(Scaling.none);
        mHelpTable.add(helpPipe);

        mContent.add(mGroup);
        mGroup.addActor(mBackground);
        mBackground.setPosition(0, -mBackground.getHeight());
        mGroup.addActor(mBlue);
        mGroup.addActor(mBubbleBlue);
        mBlue.setPosition(153, -137);
        mBubbleBlue.setPosition(152,-56);
        mGroup.addActor(mYellowOrGreen);
        mGroup.addActor(mBubbleYellowOrGreen);
        mYellowOrGreen.setPosition(182, -137);
        mBubbleYellowOrGreen.setPosition(182,-56);
        mGroup.addActor(mRed);
        mGroup.addActor(mBubbleRed);
        mRed.setPosition(208, -137);
        mBubbleRed.setPosition(210,-56);

        mMixImg = new Image();
        mMixImg.setScaling(Scaling.none);
        mMixImg.setVisible(false);
        mGroup.addActor(mMixImg);
        mMixImg.setPosition(LIQUID_FULL_X, LIQUID_FULL_Y);

        mGroup.addActor(mActivatorOn);
        mActivatorOn.setPosition(36 - mActivatorOn.getWidth(), -129 - mActivatorOn.getHeight() / 2 -3);
        mGroup.addActor(mActivatorOff);
        mActivatorOff.setPosition(36 - mActivatorOn.getWidth(), -129 - mActivatorOff.getHeight() / 2 +3);


        mGroup.addActor(mPipe);
        mPipe.setPosition(36 - mPipe.getWidth(), -129-44 - mPipe.getHeight() / 2 );


    }

    public void restoreFromPersistence(GameSession aGameSession) {
        Boolean hasBlue = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_BLUE);
        if (hasBlue != null) {
            mHasBlue = hasBlue.booleanValue();
        }
        Boolean hasYellow = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_YELLOW_OR_GREEN);
        if (hasYellow != null) {
            mHasYellowOrGreen = hasYellow.booleanValue();
        }
        Boolean hasRed = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_RED);
        if (hasRed != null) {
            mHasRed = hasRed.booleanValue();
        }
        Integer mixResult = (Integer) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_MIX_RESULT);
        if (mixResult != null) {
            mMixResult = mixResult.intValue();
        }
        Boolean hasActivator = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_ACTIVATOR);
        if (hasActivator != null) {
            mHasActivator = hasActivator.booleanValue();
        }
        updateUI();
    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_BLUE, mHasBlue);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_RED, mHasRed);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_YELLOW_OR_GREEN, mHasYellowOrGreen);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_MIX_RESULT, mMixResult);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_ACTIVATOR, mHasActivator);
        return aGameSession;
    }

    @Override
    protected void createDragAndDropTarget(DragAndDrop aDragAndDrop) {
        mBackgroundTarget = new ChallengeTarget(mBackground, this);
        aDragAndDrop.addTarget(mBackgroundTarget);
    }

    @Override
    protected void removeDragAndDropTarget(DragAndDrop aDragAndDrop) {
        aDragAndDrop.removeTarget(mBackgroundTarget);
    }

    @Override
    public void onItemDrop(InventorySlot aSourceSlot) {
        if (aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.VialBlue) && !mHasBlue) {
            mHasBlue = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        } else if ((IS_ADDITIVE ? aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.VialGreen):aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.VialYellow) ) && !mHasYellowOrGreen) {
            mHasYellowOrGreen = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        } else if (aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.VialRed) && !mHasRed) {
            mHasRed = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        else if (aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.MachineActivator) && !mHasActivator) {
            mHasActivator = true;
            mIsActivated=false;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        mInteractionChallenge.saveInPersistence();
        updateUI();
    }

    protected void onActivate(boolean isActivated) {
        mIsActivated=isActivated;
        if (isActivated) {
            if (mHasBlue && !mHasYellowOrGreen && !mHasRed) {
                mMixResult = LIQUID_RESULT_BLUE;
            } else if (mHasYellowOrGreen && !mHasBlue && !mHasRed) {
                mMixResult = LIQUID_RESULT_YELLOW_OR_GREEN;
            } else if (mHasRed && !mHasYellowOrGreen && !mHasBlue) {
                mMixResult = LIQUID_RESULT_RED;
            } else if (mHasRed && mHasYellowOrGreen && !mHasBlue) {
                mMixResult = LIQUID_RESULT_RED_YELLOW_OR_GREEN;
            } else if (!mHasRed && mHasYellowOrGreen && mHasBlue) {
                mMixResult = LIQUID_RESULT_BLUE_YELLOW_OR_GREEN;
            } else if (mHasRed && !mHasYellowOrGreen && mHasBlue) {
                mMixResult = LIQUID_RESULT_BLUE_RED;
            } else if (mHasRed && mHasYellowOrGreen && mHasBlue) {
                mMixResult = LIQUID_RESULT_BLUE_RED_YELLOW_OR_GREEN;
            } else {
                mMixResult = LIQUID_RESULT_NONE;
            }
            mHasBlue = false;
            mHasRed = false;
            mHasYellowOrGreen = false;

        }
        mInteractionChallenge.saveInPersistence();
        updateUI();

    }

    protected void onPipe()
    {
        boolean hasChanged = false;
        if (mMixResult == LIQUID_RESULT_BLUE) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialBlue));
        } else if (mMixResult == LIQUID_RESULT_YELLOW_OR_GREEN) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(IS_ADDITIVE ? ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialGreen) :ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialYellow));
        } else if (mMixResult == LIQUID_RESULT_RED) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.VialRed));
        } else if (mMixResult == LIQUID_RESULT_RED_YELLOW_OR_GREEN) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(IS_ADDITIVE ? Item.ItemTypeID.PotionYellowLarge : Item.ItemTypeID.PotionOrangeLarge));
        } else if (mMixResult == LIQUID_RESULT_BLUE_YELLOW_OR_GREEN) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(IS_ADDITIVE ? Item.ItemTypeID.PotionTealBig : Item.ItemTypeID.PotionGreenSmall));
        } else if (mMixResult == LIQUID_RESULT_BLUE_RED) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.PotionVioletLarge));
        } else if (mMixResult == LIQUID_RESULT_BLUE_RED_YELLOW_OR_GREEN) {
            hasChanged=true;
            EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.PotionSilver));
        }

        if(hasChanged) {
            mIsActivated=false;
            mMixResult = LIQUID_RESULT_NONE;
            mInteractionChallenge.saveInPersistence();
            updateUI();
        }
    }

    protected void updateUI() {
        mBlue.setVisible(mHasBlue);
        mBubbleBlue.setVisible(mHasBlue);
        mRed.setVisible(mHasRed);
        mBubbleRed.setVisible(mHasRed);
        mYellowOrGreen.setVisible(mHasYellowOrGreen);
        mBubbleYellowOrGreen.setVisible(mHasYellowOrGreen);

        if(mHasActivator)
        {
            mActivatorOff.setVisible(!mIsActivated);
            mActivatorOn.setVisible(mIsActivated);
        }
        else
        {
            mActivatorOff.setVisible(false);
            mActivatorOn.setVisible(false);
        }

        if (mMixResult == LIQUID_RESULT_BLUE) {
            mMixImg.setDrawable(new TextureRegionDrawable(mAtlas.findRegion("theMachine_liquid_few_blue")));
        } else if (mMixResult == LIQUID_RESULT_YELLOW_OR_GREEN) {
            mMixImg.setDrawable(new TextureRegionDrawable(IS_ADDITIVE ? mAtlas.findRegion("theMachine_liquid_few_green") : mAtlas.findRegion("theMachine_liquid_few_yellow")));
        } else if (mMixResult == LIQUID_RESULT_RED) {
            mMixImg.setDrawable(new TextureRegionDrawable(mAtlas.findRegion("theMachine_liquid_few_red")));
        } else if (mMixResult == LIQUID_RESULT_RED_YELLOW_OR_GREEN) {
            mMixImg.setDrawable(new TextureRegionDrawable(IS_ADDITIVE ? mAtlas.findRegion("theMachine_liquid_demi_green_red"): mAtlas.findRegion("theMachine_liquid_demi_yellow_red")));
        } else if (mMixResult == LIQUID_RESULT_BLUE_YELLOW_OR_GREEN) {
            mMixImg.setDrawable(new TextureRegionDrawable(IS_ADDITIVE ? mAtlas.findRegion("theMachine_liquid_demi_blue_green") :mAtlas.findRegion("theMachine_liquid_demi_blue_yellow")));
        } else if (mMixResult == LIQUID_RESULT_BLUE_RED) {
            mMixImg.setDrawable(new TextureRegionDrawable(mAtlas.findRegion("theMachine_liquid_demi_blue_red")));
        } else if (mMixResult == LIQUID_RESULT_BLUE_RED_YELLOW_OR_GREEN) {
            mMixImg.setDrawable(new TextureRegionDrawable(mAtlas.findRegion("theMachine_liquid_mixed_full")));
        }
        mMixImg.setScaling(Scaling.none);
        mMixImg.setSize(mMixImg.getPrefWidth(), mMixImg.getPrefHeight());
        mMixImg.setVisible(mMixResult!=LIQUID_RESULT_NONE);

        mMixImg.setPosition(LIQUID_FULL_X, LIQUID_FULL_Y);
    }
}
