package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.InventorySlot;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.map.MapTownPortalInfo;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.persistence.Profile;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vincent on 07/04/2017.
 */

public class ChallengePortalCheckpoint extends ChallengeUI {
    private static final String KEY_ACTIVATION_MAP = "ACTIVATION_MAP";

    public static enum PortalMap {
        FOREST("forest1", "EmptyStone13", Item.ItemTypeID.Stone13),
        CAVES("village", "EmptyStone1", Item.ItemTypeID.Stone1),
        DESERT("village", "EmptyStone2", Item.ItemTypeID.Stone2),
        MOUNTAIN("village", "EmptyStone12", Item.ItemTypeID.Stone12),
        MINES("village", "EmptyStone4", Item.ItemTypeID.Stone4),
        VILLAGE("village_tower2", "EmptyStone14", Item.ItemTypeID.Stone14);

        PortalMap(String aMapName, String aEmptyStone, Item.ItemTypeID aActivateStone) {
            mMapName = aMapName;
            mEmptyStone = aEmptyStone;
            mActivateStone = aActivateStone;
        }

        public String mMapName;
        public String mEmptyStone;
        public Item.ItemTypeID mActivateStone;
    }

    private WidgetGroup mGroup;
    protected boolean mIsActivated = false;
    protected TextureAtlas mAtlas;

    protected PortalMap mCurrentPortal;

    Image mStoneLocationImage;
    Image mCompletedImage;


    private ChallengeTarget mStoneTarget;


    public ChallengePortalCheckpoint() {
        super();


    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        if (mCurrentPortal != null && !Profile.getInstance().getPortalCheckpoints().contains(mCurrentPortal.name()) && mIsActivated) {
            Profile.getInstance().getPortalCheckpoints().add(mCurrentPortal.name());
            Profile.getInstance().updatePortalCheckpoints(Profile.getInstance().getPortalCheckpoints());
        }
        return aGameSession;
    }


    protected void setProperties(HashMap aProperties) {

        for (PortalMap portal : PortalMap.values()) {
            if (portal.mMapName.equals(mInteractionChallenge.getMap().getMapName())) {
                mCurrentPortal = portal;
                break;
            }
        }
        if (mCurrentPortal != null && Profile.getInstance().getPortalCheckpoints().contains(mCurrentPortal.name())) {
            mIsActivated = true;
        } else {
            mIsActivated = false;
        }
        AssetsUtility.loadTextureAtlasAsset(mInteractionChallenge.getAtlasFilename());

        mAtlas = AssetsUtility.getTextureAtlasAsset(mInteractionChallenge.getAtlasFilename());
        mCompletedImage = new Image(mAtlas.findRegion("bkg"));

  /*      if (aProperties != null && aProperties.containsKey("size")) {
            mSize = ((Float) aProperties.get("size")).intValue();
        }
        if (aProperties != null && aProperties.containsKey("image")) {
            mImageFile = (String) aProperties.get("image");
        }*/
    }

    @Override
    protected void createView() {

        mGroup = new WidgetGroup();
        mContent.align(Align.center);
        mContent.top().add(mGroup).pad(20).expand().fill().center();

    }

    @Override
    protected void layoutView() {
        mCompletedImage.setScaling(Scaling.none);
        mCompletedImage.setAlign(Align.center);
        mGroup.clear();
        mGroup.addActor(mCompletedImage);
        mCompletedImage.setPosition((mGroup.getWidth() - mCompletedImage.getWidth()) / 2, (mGroup.getHeight() - mCompletedImage.getHeight()) / 2);

        if (mCurrentPortal == null) {
            mStoneLocationImage = new Image(mAtlas.findRegion("EmptyStone"));
        } else {
            mStoneLocationImage = new Image(mAtlas.findRegion(mCurrentPortal.mEmptyStone));
        }
        mStoneLocationImage.setScaling(Scaling.none);
        mStoneLocationImage.setAlign(Align.center);
        mGroup.addActor(mStoneLocationImage);
        mStoneLocationImage.setPosition((mGroup.getWidth() - mStoneLocationImage.getWidth()) / 2, (mGroup.getHeight() - mStoneLocationImage.getHeight()) / 2);


        if (mIsActivated) {
            Item stone = ItemFactory.getInstance().getInventoryItem(mCurrentPortal.mActivateStone);
            setCurrentStone(stone);
        }

        setOtherStones();
    }

    protected void setCurrentStone(Item stone) {
        if (stone != null) {
            Image stoneImage = new Image(stone.getTextureRegion());
            stoneImage.setScaling(Scaling.none);
            stoneImage.setAlign(Align.center);
            mGroup.addActor(stoneImage);
            stoneImage.setPosition((mGroup.getWidth() - stoneImage.getWidth()) / 2, (mGroup.getHeight() - stoneImage.getHeight()) / 2);
        }
    }

    protected void setOtherStones() {
        ArrayList<String> activatedPortalNames = Profile.getInstance().getPortalCheckpoints();

        double PI3 = Math.PI / 3;
        double PI6 = Math.PI / 6;
        double dist = Math.max(mCompletedImage.getWidth() / 2, mCompletedImage.getHeight() / 2) + 20;
        double offsetX = mGroup.getWidth() / 2;
        double offsetY = mGroup.getHeight() / 2;

        for (int i = 0; i < activatedPortalNames.size(); i++) {
            double x = dist * Math.cos(PI3 * i + PI6);
            double y = dist * Math.sin(PI3 * i + PI6);
            PortalMap portal = PortalMap.valueOf(activatedPortalNames.get(i));
            if (portal == null || portal == mCurrentPortal)
                continue;
            Item stone = ItemFactory.getInstance().getInventoryItem(portal.mActivateStone);
            if (stone == null)
                continue;
            final Image stoneImage = new Image(mAtlas.findRegion("Symbol" + portal.mActivateStone.name()));
            stoneImage.setScaling(Scaling.none);
            stoneImage.setAlign(Align.center);
            stoneImage.setName(portal.name());
            mGroup.addActor(stoneImage);
            stoneImage.setPosition((int) (offsetX + x) - stoneImage.getWidth() / 2, (int) (offsetY + y) - stoneImage.getHeight() / 2);
            stoneImage.setTouchable(Touchable.enabled);
            stoneImage.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    teleport(event.getTarget().getName());
                }
            });
        }
    }

    @Override
    protected void createDragAndDropTarget(DragAndDrop aDragAndDrop) {
        mStoneTarget = new ChallengeTarget(mCompletedImage, this);
        aDragAndDrop.addTarget(mStoneTarget);
    }

    @Override
    protected void removeDragAndDropTarget(DragAndDrop aDragAndDrop) {
        aDragAndDrop.removeTarget(mStoneTarget);
    }

    @Override
    public void onItemDrop(InventorySlot aSourceSlot) {
        if (mCurrentPortal != null && aSourceSlot!=null && aSourceSlot.doesAcceptItemUseType(mCurrentPortal.mActivateStone)) {

            mIsActivated = true;
            saveInPersistence();
            Item stone = aSourceSlot.getItemOnTop();
            EventDispatcher.getInstance().onItemLost(stone);
            setCurrentStone(stone);
            challengeCompleted();
        }

    }

    protected void teleport(String aPortalName) {
        PortalMap portal = PortalMap.valueOf(aPortalName);
        if (portal == null)
            return;
        MapTownPortalInfo portalInfo = new MapTownPortalInfo();
        portalInfo.originMap = mInteractionChallenge.getMap().getMapName();
        portalInfo.isCheckpoint = true;

        EventDispatcher.getInstance().onNewMapRequested(portal.mMapName, portalInfo);
    }


}
