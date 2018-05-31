package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.InventorySlot;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.screens.GenericUI;

import java.util.HashMap;

/**
 * Created by vincent on 07/04/2017.
 */

public class ChallengeLaser extends ChallengeUI {
    private static final String KEY_IS_RESOLVED = "IS_RESOLVED";

    private static final String KEY_HAS_POTION = "HAS_POTION";
    private static final String KEY_HAS_TORCH = "HAS_TORCH";
    private static final String KEY_HAS_CRYSTAL = "HAS_CRYSTAL";

    public static enum LASER_COLOR {
        BLUE("0000FF", Item.ItemTypeID.VialBlue, "laser_blue"),
        RED("FF0000", Item.ItemTypeID.VialRed, "laser_red"),
        GREEN("00FF00", Item.ItemTypeID.VialGreen, "laser_green"),
        PURPLE("d91aea", Item.ItemTypeID.PotionVioletLarge, "laser_purple"),
        TEAL("8bdcf7", Item.ItemTypeID.PotionTealBig, "laser_teal"),
        WHITE("FFFFFF", Item.ItemTypeID.PotionSilver, "laser_white"),
        YELLOW("f6ee16", Item.ItemTypeID.PotionYellowLarge, "laser_yellow");

        public Color mColor;
        public Item.ItemTypeID mPotionType;
        public String mLaserRegion;

        LASER_COLOR(String colorCode, Item.ItemTypeID  aPotionType, String aLaserRegion)
        {
            mColor = Color.valueOf(colorCode);
            mPotionType = aPotionType;
            mLaserRegion = aLaserRegion;

        }
    }

    protected boolean mIsResolved = false;
    protected boolean mhasPotion = false;
    protected boolean mHasTorch = false;
    protected boolean mHasCrystal = false;
    protected LASER_COLOR mColor;
    private ChallengeTarget mDropTarget;
    private WidgetGroup mGroup;

    protected Image  mMirrorImage, mCrystalImage, mPotionImage, mTorchImage, mPreLaserImage, mLaserImage;
    private Table mHelpTable;
    protected TextureAtlas mAtlas;



    public ChallengeLaser() {
        super();


    }



    @Override
    protected void createView() {
        mHelpTable = new Table();
        mContent.top().add(mHelpTable);
        mContent.row();
        Image torch = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.TorchFire).getTextureRegion());
        torch.setScaling(Scaling.none);
        mHelpTable.add(torch);

        Label helpPlus = new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus.setAlignment(Align.center);
        mHelpTable.add(helpPlus);

        Image crystal = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.CrystalAnimBlue).getTextureRegion());
        crystal.setScaling(Scaling.none);
        mHelpTable.add(crystal);

         helpPlus = new Label("+", GenericUI.getInstance().getSkin(), "big-font");
        helpPlus.setAlignment(Align.center);
        mHelpTable.add(helpPlus);


        mContent.align(Align.top);
        mGroup = new WidgetGroup();
        mContent.top().add(mGroup).padTop(10).padBottom(10).expand().fill().center();




    }

    protected void createUI() {

        Image backImage  = new Image(mAtlas.findRegion("laser_bkg"));
        backImage.setSize(128, 128);
        backImage.setPosition(0, 128);
        mGroup.addActor(backImage);
        backImage  = new Image(mAtlas.findRegion("laser_bkg"));
        backImage.setSize(128, 128);
        backImage.setPosition(128, 128);
        mGroup.addActor(backImage);

        mMirrorImage = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.Mirror).getTextureRegion());
        mMirrorImage.setScaling(Scaling.none);
        mMirrorImage.setPosition(10, 48+128);
        mGroup.addActor(mMirrorImage);

        mTorchImage = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.TorchFire).getTextureRegion());
        mTorchImage.setScaling(Scaling.none);
        mTorchImage.setVisible(false);
        mTorchImage.setPosition(42,48+128);
        mGroup.addActor(mTorchImage);

        mCrystalImage = new Image(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.CrystalAnimBlue).getTextureRegion());
        mCrystalImage.setScaling(Scaling.none);
        mCrystalImage.setVisible(false);
        mCrystalImage.setPosition(74, 48+128);
        mGroup.addActor(mCrystalImage);


        Image potion = new Image(ItemFactory.getInstance().getInventoryItem(mColor.mPotionType).getTextureRegion());
        potion.setScaling(Scaling.none);
        mHelpTable.add(potion);


        mPreLaserImage= new Image(mAtlas.findRegion(LASER_COLOR.WHITE.mLaserRegion));
        mPreLaserImage.setScaling(Scaling.none);
        mPreLaserImage.setVisible(false);
        mPreLaserImage.setPosition(110, 48+128);
        mGroup.addActor(mPreLaserImage);

        mPotionImage = new Image(ItemFactory.getInstance().getInventoryItem(mColor.mPotionType).getTextureRegion());
        mPotionImage.setScaling(Scaling.none);
        mPotionImage.setVisible(false);
        mPotionImage.setPosition(174, 48+128);
        mGroup.addActor(mPotionImage);

        mLaserImage = new Image(mAtlas.findRegion(mColor.mLaserRegion));
        mLaserImage.setScaling(Scaling.none);
        mLaserImage.setVisible(false);
        mLaserImage.setPosition(206,48+128);
        mGroup.addActor(mLaserImage);

        refreshUI();
    }

    public void restoreFromPersistence(GameSession aGameSession) {
        Boolean isResolved = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED);
        if (isResolved != null && isResolved.booleanValue()) {
            mIsResolved = true;
        }
        Boolean hasPotion = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_POTION);
        if (hasPotion != null && hasPotion.booleanValue()) {
            mhasPotion = true;
        }
        Boolean hasTorch = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_TORCH);
        if (hasTorch != null && hasTorch.booleanValue()) {
            mHasTorch = true;
        }
        Boolean hasCrystal = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_CRYSTAL);
        if (hasCrystal != null && hasCrystal.booleanValue()) {
            mHasCrystal = true;
        }
        refreshUI();
    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED, mIsResolved);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_TORCH, mHasTorch);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_CRYSTAL, mHasCrystal);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_HAS_POTION, mhasPotion);
        return aGameSession;
    }

    @Override
    protected void setProperties(HashMap aProperties) {

        if (aProperties != null && aProperties.containsKey("color")) {
            try {
                mColor = LASER_COLOR.valueOf((String) aProperties.get("color"));
            }catch(Exception e)
            {

            }
        }
        AssetsUtility.loadTextureAtlasAsset(mInteractionChallenge.getAtlasFilename());

        mAtlas = AssetsUtility.getTextureAtlasAsset(mInteractionChallenge.getAtlasFilename());
        createUI();

    }


    protected void refreshUI() {
        if(mAtlas==null)
            return;
        mCrystalImage.setVisible(mHasCrystal);
        mTorchImage.setVisible(mHasTorch);
        mPotionImage.setVisible(mhasPotion);
        mPreLaserImage.setVisible(mHasCrystal && mHasTorch );
        mLaserImage.setVisible(mHasCrystal && mHasTorch &&mhasPotion);

    }
    @Override
    protected void createDragAndDropTarget(DragAndDrop aDragAndDrop) {
        mDropTarget = new ChallengeTarget(mGroup, this);
        aDragAndDrop.addTarget(mDropTarget);
    }

    @Override
    protected void removeDragAndDropTarget(DragAndDrop aDragAndDrop) {
        aDragAndDrop.removeTarget(mDropTarget);
    }

    @Override
    public void onItemDrop(InventorySlot aSourceSlot) {
        if (!mHasTorch && aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.TorchFire)) {
            mHasTorch = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        else if (!mHasCrystal && aSourceSlot.doesAcceptItemUseType(Item.ItemTypeID.CrystalAnimBlue)) {
            mHasCrystal = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        else if (!mhasPotion && aSourceSlot.doesAcceptItemUseType(mColor.mPotionType)) {
            mhasPotion = true;
            EventDispatcher.getInstance().onItemLost(aSourceSlot.getItemOnTop());
        }
        else
        {
            return;
        }
        checkIsResolved();
        refreshUI();
        mInteractionChallenge.saveInPersistence();

    }

    protected void checkIsResolved() {
        mIsResolved = mHasCrystal && mHasTorch && mhasPotion;
        if(mIsResolved) {
            mInteractionChallenge.setState("COMPLETED");
            challengeCompleted();
        }
    }
}
