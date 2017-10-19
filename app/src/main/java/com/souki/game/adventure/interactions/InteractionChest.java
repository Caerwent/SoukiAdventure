package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.audio.AudioManager;
import com.souki.game.adventure.box2d.CircleShape;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.dialogs.DialogsManager;
import com.souki.game.adventure.dialogs.GameDialog;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.items.Chest;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionChest extends Interaction {
    private static final String KEY_IS_OPEN = "is_open";
    protected boolean mIsOpen;
    protected Chest mChest;
    protected String mRequiredItem;

    public InteractionChest(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.CHEST;
        initialize(x, y, aMapping);
        mChest = ItemFactory.getInstance().getChest(aMap.getMapName() + "_" + getId());

    }

    @Override
    public void initialize(float x, float y, InteractionMapping aMapping) {
        super.initialize(x, y, aMapping);
        if (mProperties != null) {
            mRequiredItem = (String) mProperties.get("requiredItem");
        }

    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        Boolean isOpen = (Boolean) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), getId(), KEY_IS_OPEN);
        if (isOpen != null && isOpen.booleanValue()) {
            mIsOpen = true;
            setState("OPEN");
        } else {
            mIsOpen = false;
            setState("CLOSED");
        }

    }

    @Override
    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_IS_OPEN, mIsOpen);
        return aGameSession;
    }

    @Override
    public Shape createShapeCollision() {
        if (isRendable()) {
            return super.createShapeCollision();
        } else {
            mShapeCollision = new CircleShape();
            mShapeCollision.setY(0);
            mShapeCollision.setX(0);
            float radius = /*isClickable() ? 1F :*/ 0.5F;
            ((CircleShape) mShapeCollision).setRadius(radius);
            return mShapeCollision;
        }
    }

    @Override
    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return !mIsOpen && aEntity.mInteraction.getType() == Type.HERO;
    }

    @Override
    protected boolean hasTouchInteraction(float x, float y) {

        return getShapeInteraction().getBounds().contains(x, y) && !mIsOpen;
    }

    @Override
    public void onTouchInteraction() {

        if (mRequiredItem != null && !mRequiredItem.isEmpty()) {
            Array<Item> item = mMap.getPlayer().getItemsInventoryById(mRequiredItem);
            if (item != null && item.size > 0) {
                mMap.getPlayer().removeItem(item.get(0));
            } else {
                GameDialog needDialog = DialogsManager.getInstance().getDialog("needKey").clone();
                String msg = needDialog.getDialogs().get(0).phrases.get(0);
                msg += " " + ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(mRequiredItem)).getItemShortDescription();
                needDialog.getDialogs().get(0).phrases.set(0, msg);
                EventDispatcher.getInstance().onStartDialog(needDialog);
                return;
            }
        }
        mIsOpen = true;
        setState("OPEN");

        for (String itemId : mChest.getItems()) {
            Item item = ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(itemId));
            mMap.getPlayer().onItemFound(item);
            AudioManager.getInstance().onAudioEvent(AudioManager.ITEM_FOUND_SOUND);

        }

        if (getPersistence() != Persistence.NONE) {
            saveInPersistence();
        }

    }
}
