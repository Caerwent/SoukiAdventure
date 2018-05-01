package com.souki.game.adventure.interactions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.box2d.CircleShape;
import com.souki.game.adventure.box2d.RectangleShape;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VisualComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.IQuestListener;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.map.IMapRendable;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.quests.Quest;
import com.souki.game.adventure.quests.QuestManager;
import com.souki.game.adventure.quests.QuestTask;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionPortal extends Interaction implements IQuestListener {

    private static final String KEY_STATE = "state";

    protected String mTargetMapId;
    protected boolean mIsDefaultStart = false;
    boolean mIsActivated = false;
    protected String mActivatedByQuestId = null;
    protected String mQuestId = null;
    protected String mActivatedByItem = null;

    protected int mWidth = -1;
    protected int mHeight = -1;

    private RectangleShape mMarkShape;
    private TextureRegion mInteractionTextureRegion;
    private TextureRegion mInteractionEmptyTextureRegion;
    private boolean mIsInteractionShown = false;

    public InteractionPortal(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.PORTAL;

        if (mProperties != null) {
            if (mProperties.containsKey("activateQuestId")) {
                mQuestId = (String) aMapping.properties.get("activateQuestId");
            }
            if (mProperties.containsKey("targetMapId")) {
                mTargetMapId = (String) aMapping.properties.get("targetMapId");
            }
            if (mProperties.containsKey("isDefaultStart")) {
                mIsDefaultStart = Boolean.parseBoolean((String) aMapping.properties.get("isDefaultStart"));
            }
            if (mProperties.containsKey("activatedByQuestId")) {
                mActivatedByQuestId = (String) aMapping.properties.get("activatedByQuestId");
            }
            if (mProperties.containsKey("activatedByItem")) {
                mActivatedByItem = (String) aMapping.properties.get("activatedByItem");
            }
            if (mProperties.containsKey("width")) {
                mWidth = ((Float) aMapping.properties.get("width")).intValue();
            }
            if (mProperties.containsKey("height")) {
                mHeight = ((Float) aMapping.properties.get("height")).intValue();
            }
        }
        initialize(x, y, aMapping);
        if (isClickable()) {
            mInteractionTextureRegion = GenericUI.getInstance().getTextureAtlas().findRegion("Interaction");
            mInteractionEmptyTextureRegion = GenericUI.getInstance().getTextureAtlas().findRegion("InteractionEmpty");
            mMarkShape = new RectangleShape();
            updateInteractionMarkShape();
            if (!isRendable()) {
                final IMapRendable self = this;
                this.add(new VisualComponent(mInteractionTextureRegion, new IMapRendable() {

                    boolean mIsRended = false;

                    @Override
                    public boolean isRendable() {
                        return true;
                    }

                    @Override
                    public boolean isRended() {
                        return mIsRended;
                    }

                    @Override
                    public void setRended(boolean aRended) {
                        mIsRended = aRended;
                    }

                    @Override
                    public void render(Batch batch) {
                        renderMark(batch);
                    }

                    @Override
                    public Shape getShapeRendering() {
                        return mMarkShape;
                    }

                    @Override
                    public int getZIndex() {
                        return 2;
                    }
                }));

            }
        }


        remove(CollisionObstacleComponent.class);
        remove(CollisionEffectComponent.class);
        if (mActivatedByQuestId != null) {
            Quest quest = QuestManager.getInstance().getQuestFromId(mActivatedByQuestId);
            if (quest.isCompleted()) {
                mActivatedByQuestId = null;
            } else {
                EventDispatcher.getInstance().addQuestListener(this);
            }
        }
    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        super.restoreFromPersistence(aGameSession);
        String state = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE);
        if (state != null) {
            mCurrentState = getState(state);
        }

    }

    @Override
    public GameSession saveInPersistence(GameSession aGameSession) {
        super.saveInPersistence(aGameSession);
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE, mCurrentState.name);

        return aGameSession;
    }

    @Override
    public void updateInteraction(float dt) {
        super.updateInteraction(dt);
        if (isClickable()) {
            updateInteractionMarkShape();
        }
    }

    public void updateInteractionMarkShape() {
        if (mMarkShape == null)
            return;
        float width_frame = mShapeInteraction.getWidth() / MyGame.SCALE_FACTOR;
        float height_frame = mShapeInteraction.getHeight() / MyGame.SCALE_FACTOR;
        if (mWidth > 0) {
            width_frame = mWidth;
        }
        if (mHeight > 0) {
            height_frame = mHeight;
        }

        float width = mInteractionTextureRegion.getRegionWidth();
        float height = mInteractionTextureRegion.getRegionHeight();

        //Allow for Offset
        float originX = (width_frame - width) / 2 * MyGame.SCALE_FACTOR;//transform.originOffset.x;
        float originY = height_frame * MyGame.SCALE_FACTOR;//transform.originOffset.y;

        mMarkShape.getShape().set(0, 0, width, height);
        mMarkShape.setX(mShapeInteraction.getX() + originX);
        mMarkShape.setY(mShapeInteraction.getY() + originY);

    }

    @Override
    public void setPosition(float x, float y) {
        super.setPosition(x, y);
        updateInteractionMarkShape();

    }

    @Override
    public void setPosition(Vector2 pos) {
        super.setPosition(pos);
        updateInteractionMarkShape();
    }

    @Override
    public void startToInteract() {
        super.startToInteract();
        remove(CollisionObstacleComponent.class);
        remove(CollisionEffectComponent.class);

    }

    @Override
    public Shape createShapeInteraction() {
        if (isRendable()) {
            return super.createShapeInteraction();
        }
        mShapeInteraction = new CircleShape();
        mShapeInteraction.setY(getPosition().x);
        mShapeInteraction.setX(getPosition().y);
        float radius = /*isClickable() ? 1F :*/ 0.5F;
        ((CircleShape) mShapeInteraction).setRadius(radius);
        return mShapeInteraction;
    }

    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return mActivatedByQuestId == null && mIsActivated && InteractionState.STATE_ACTIVATED.equals(mCurrentState.name) && aEntity.mInteraction.getType() == Type.HERO;
    }


    protected void teleport() {
        if (mIsActivated && mTargetMapId != null)
            EventDispatcher.getInstance().onNewMapRequested(mTargetMapId, null);
    }

    /************************ RENDERING *********************************/
    @Override
    public void render(Batch batch) {
        super.render(batch);
        renderMark(batch);
    }

    public void renderMark(Batch batch) {


        if (mIsInteractionShown) {
            float width_frame = mShapeInteraction.getWidth() / MyGame.SCALE_FACTOR;
            float height_frame = mShapeInteraction.getHeight() / MyGame.SCALE_FACTOR;

            TextureRegion interactionRegion;
            if (mActivatedByItem != null) {
                interactionRegion = mInteractionEmptyTextureRegion;
            } else {
                interactionRegion = mInteractionTextureRegion;
            }
            float width = interactionRegion.getRegionWidth();
            float height = interactionRegion.getRegionHeight();

            batch.draw(interactionRegion,
                    mMarkShape.getX(), mMarkShape.getY(),
                    0, 0,
                    width, height,
                    MyGame.SCALE_FACTOR, MyGame.SCALE_FACTOR,
                    0);
            if (mActivatedByItem != null) {
                Item item = ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(mActivatedByItem));
                batch.draw(item.getTextureRegion(),
                        mMarkShape.getX() + (width - item.getTextureRegion().getRegionWidth()) / 2 * MyGame.SCALE_FACTOR, mMarkShape.getY() + (height - item.getTextureRegion().getRegionHeight()) / 2 * MyGame.SCALE_FACTOR,
                        0, 0,
                        item.getTextureRegion().getRegionWidth(), item.getTextureRegion().getRegionHeight(),
                        MyGame.SCALE_FACTOR, MyGame.SCALE_FACTOR,
                        0);
            }
        }
    }

    @Override
    public void onTouchInteraction() {
        if (mActivatedByItem == null && InteractionState.STATE_ACTIVATED.equals(mCurrentState.name)) {
            teleport();
        } else {

        }
    }

    @Override
    protected boolean hasTouchInteraction(float x, float y) {

        return mDef.isClickable && mIsActivated && mIsInteractionShown && (mMarkShape.getBounds().contains(x, y) || getShapeInteraction().getBounds().contains(x, y));
    }

    @Override
    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {

        if (!mDef.isClickable) {
            teleport();
        } else {
            mIsInteractionShown = true;
            if (mActivatedByItem != null) {
                Array<Item> items = mMap.getPlayer().getItemsInventoryById(mActivatedByItem);
                if (items.size > 0) {
                    mActivatedByItem = null;
                    //  mMap.getPlayer().removeItem(items.first());
                    // TO DO store activation in persitence before removing item
                }
            }
        }
    }

    @Override
    public void onStopCollisionInteraction(CollisionInteractionComponent aEntity) {
        if (mActivatedByQuestId == null) {
            mIsActivated = true;
        }
        mIsInteractionShown = false;
    }

    public boolean isActivated() {
        return mIsActivated;
    }

    public void setActivated(boolean isActivated) {
        mIsActivated = isActivated;
    }

    public String getTargetMapId() {
        return mTargetMapId;
    }

    public boolean isDefaultStart() {
        return mIsDefaultStart;
    }


    @Override
    public void onQuestActivated(Quest aQuest) {

    }

    @Override
    public void onQuestCompleted(Quest aQuest) {
        if (mActivatedByQuestId != null && aQuest.getId().compareTo(mActivatedByQuestId) == 0) {
            mActivatedByQuestId = null;
        }
    }

    @Override
    public void onQuestTaskCompleted(Quest aQuest, QuestTask aTask) {

    }

    public String getQuestId() {
        return mQuestId;
    }

    public void setQuestId(String aQuestId) {
        mQuestId = aQuestId;
    }

}
