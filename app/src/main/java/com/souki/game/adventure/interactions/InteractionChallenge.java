package com.souki.game.adventure.interactions;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.RectangleShape;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.gui.UIStage;
import com.souki.game.adventure.gui.challenge.ChallengeUI;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.screens.GenericUI;

/**
 * Created by vincent on 04/04/2017.
 */

public class InteractionChallenge extends Interaction {

    protected ChallengeUI mChallengeUI;

    private boolean mIsInteractionShown = false;
    private RectangleShape mMarkShape;
    private TextureRegion mInteractionTextureRegion;

    protected String mChallengeType;
    private static final String KEY_STATE = "state";
    protected int mZindex = 1;

    public InteractionChallenge(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.CHALLENGE;
        mInteractionTextureRegion = GenericUI.getInstance().getTextureAtlas().findRegion("InteractionDialog");
        initialize(x, y, aMapping);
        mMarkShape = new RectangleShape();
        updateInteractionMarkShape();


    }

    @Override
    public void initialize(float x, float y, InteractionMapping aMapping) {
        super.initialize(x, y, aMapping);
        mChallengeType = (String) mProperties.get("type");
        mChallengeUI = ChallengeUI.createInstance(ChallengeUI.ChallengeType.valueOf(mChallengeType));
        mChallengeUI.setInteractionChallenge(this, mProperties);
        if (getPersistence() == Persistence.GAME) {
            mChallengeUI.restoreFromPersistence(Profile.getInstance().getPersistentGameSession());
        } else if (getPersistence() == Persistence.SESSION) {
            mChallengeUI.restoreFromPersistence(GameSession.getInstance());
        }
        if (mProperties.containsKey("zindex")) {
            mZindex = ((Float) mProperties.get("zindex")).intValue();
        } else {
            mZindex = super.getZIndex();
        }


    }

    public ChallengeUI.ChallengeType getChallengeType() {
        return ChallengeUI.ChallengeType.valueOf(mChallengeType);
    }

    @Override
    public void startToInteract() {
        super.startToInteract();
        if (getZIndex() == 0) {
            remove(CollisionObstacleComponent.class);
            remove(CollisionEffectComponent.class);
        }

    }

    @Override
    public int getZIndex() {
        return mZindex;
    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        String state = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE);
        if (state != null) {
            setState(state);
        }
        if (mChallengeUI != null) {
            mChallengeUI.restoreFromPersistence(aGameSession);
        }

    }

    @Override
    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE, mCurrentState.name);
        if (mChallengeUI != null) {
            aGameSession = mChallengeUI.saveInPersistence(aGameSession);
        }
        return aGameSession;
    }

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
        if (hasCollisionInteraction(aEntity)) {
            mIsInteractionShown = false;
            ChallengeUI currentChallengeUI = UIStage.getInstance().getChallengeUIOpened();
            if (currentChallengeUI != null && currentChallengeUI == mChallengeUI) {
                UIStage.getInstance().closeChallengeUI();
            }
        }
    }

    @Override
    protected boolean hasTouchInteraction(float x, float y) {

        return mIsInteractionShown && (mMarkShape.getBounds().contains(x, y) || getShapeInteraction().getBounds().contains(x, y));
    }

    @Override
    public void onTouchInteraction() {
        ChallengeUI currentChallengeUI = UIStage.getInstance().getChallengeUIOpened();
        if (currentChallengeUI != null) {
            UIStage.getInstance().closeChallengeUI();
            if (currentChallengeUI != mChallengeUI) {
                UIStage.getInstance().openChallengeUI(mChallengeUI);
            }
        } else {
            UIStage.getInstance().openChallengeUI(mChallengeUI);
        }

    }

    protected void closeChallengeUI() {
        ChallengeUI currentChallengeUI = UIStage.getInstance().getChallengeUIOpened();
        if (currentChallengeUI != null && currentChallengeUI != mChallengeUI) {
            UIStage.getInstance().closeChallengeUI();
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

    @Override
    public void destroy() {
        mChallengeUI.release();
        super.destroy();
    }
}
