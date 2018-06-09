package com.souki.game.adventure.interactions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.RectangleShape;
import com.souki.game.adventure.dialogs.DialogsManager;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.effects.EffectFactory;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.quests.QuestManager;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 14/02/2017.
 */

public class InteractionNPC extends InteractionFollowPath {
    private static final String KEY_DIALOG_ID = "dialog_id";

    private boolean mIsInteractionShown = false;
    private RectangleShape mMarkShape;
    private TextureRegion mInteractionTextureRegion;

    public String mDialogId;

    protected String mQuestId;

    public InteractionNPC(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.NPC;
        mInteractionTextureRegion = GenericUI.getInstance().getTextureAtlas().findRegion("InteractionDialog");
        initialize(x, y, aMapping);
        mMarkShape = new RectangleShape();
        updateInteractionMarkShape();

    }

    @Override
    public void initialize(float x, float y, InteractionMapping aMapping) {
        super.initialize(x, y, aMapping);
        if (mProperties != null) {
            mDialogId = (String) mProperties.get("dialogId");
        }
    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        String dialogId = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), getId(), KEY_DIALOG_ID);
        if (dialogId != null) {
            mDialogId = dialogId;
        }

    }

    @Override
    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_DIALOG_ID, getDialogId());
        return aGameSession;
    }

    public void updateInteractionMarkShape() {
        if (mMarkShape == null)
            return;

        TransformComponent transform = this.getComponent(TransformComponent.class);
        float width_frame = mCurrentFrame.getRegionWidth();
        float height_frame = mCurrentFrame.getRegionHeight();

        float width = mInteractionTextureRegion.getRegionWidth();
        float height = mInteractionTextureRegion.getRegionHeight();

        //Allow for Offset
        float originX = (width_frame - width) / 2 * transform.scale;//transform.originOffset.x;
        float originY = height_frame * transform.scale;//transform.originOffset.y;

        mMarkShape.getShape().set(0, 0, width, height);
        mMarkShape.setX(transform.position.x + transform.originOffset.x + originX);
        mMarkShape.setY(transform.position.y + transform.originOffset.y + originY);

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

    public String getDialogId() {
        return mDialogId;
    }

    public void setDialogId(String aDialogId) {
        mDialogId = aDialogId;
        if (getPersistence() != Persistence.NONE) {
            saveInPersistence();
        }


    }

    /************************ RENDERING *********************************/
    @Override
    public void render(Batch batch) {
        super.render(batch);
        TransformComponent transform = this.getComponent(TransformComponent.class);

        if (mIsInteractionShown) {
            float width_frame = mCurrentFrame.getRegionWidth();
            float height_frame = mCurrentFrame.getRegionHeight();

            float width = mInteractionTextureRegion.getRegionWidth();
            float height = mInteractionTextureRegion.getRegionHeight();

            //Allow for Offset
            float originX = (width_frame - width) / 2 * transform.scale;//transform.originOffset.x;
            float originY = height_frame * transform.scale;//transform.originOffset.y;

            batch.draw(mInteractionTextureRegion,
                    transform.position.x + transform.originOffset.x, transform.position.y + transform.originOffset.y,
                    originX, originY,
                    width, height,
                    transform.scale, transform.scale,
                    transform.angle);
        }
    }

    /************************ INTERACTION *********************************/
    @Override
    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return aEntity.mInteraction.getType() == Type.HERO;
    }

    @Override
    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {
        if (isClickable()) {
            mIsInteractionShown = true;
        }
    }

    @Override
    public void onStopCollisionInteraction(CollisionInteractionComponent aEntity) {
        mIsInteractionShown = false;
        if (getDialogId() != null) {
            EventDispatcher.getInstance().onStopDialog(DialogsManager.getInstance().getDialog(getDialogId()));
        }
    }

    @Override
    protected boolean hasTouchInteraction(float x, float y) {

        return mIsInteractionShown && (mMarkShape.getBounds().contains(x, y) || getShapeInteraction().getBounds().contains(x, y));
    }

    @Override
    public void onTouchInteraction() {
        QuestManager.getInstance().onNPC(this);
        if (getDialogId() != null) {
            //Gdx.app.debug("DEBUG", "start dialog " + getDialogId());

            EventDispatcher.getInstance().onStartDialog(DialogsManager.getInstance().getDialog(getDialogId()));
        }
    }


    /************************ EVENTS*********************************/
    public String getQuestId() {
        return mQuestId;
    }

    public void setQuestId(String aQuestId) {
        mQuestId = aQuestId;
    }

    @Override
    protected boolean doAction(InteractionActionType aAction) {
        boolean res = super.doAction(aAction);
        //Gdx.app.debug("DEBUG", "doAction target=" + getId() + " actionType=" + aAction.type.name() + " value=" + aAction.value);
        if (!res && aAction != null && InteractionActionType.ActionType.DIALOG == aAction.type) {
            setDialogId(aAction.value);
        } else if (!res && aAction != null && InteractionActionType.ActionType.LAUNCH_EFFECT == aAction.type) {
            launchEffect(EffectFactory.getInstance().getEffect(Effect.Type.valueOf(aAction.value)));
        } else if (!res && aAction != null && InteractionActionType.ActionType.REMOVED == aAction.type) {
            mMap.getInteractions().removeValue(this, true);
            destroy();
            return true;
        }
        return res;
    }
}
