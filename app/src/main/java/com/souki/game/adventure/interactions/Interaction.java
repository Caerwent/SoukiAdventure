package com.souki.game.adventure.interactions;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.audio.AudioEvent;
import com.souki.game.adventure.audio.AudioManager;
import com.souki.game.adventure.box2d.CircleShape;
import com.souki.game.adventure.box2d.PolygonShape;
import com.souki.game.adventure.box2d.RectangleShape;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.ICollisionInteractionHandler;
import com.souki.game.adventure.entity.components.ICollisionObstacleHandler;
import com.souki.game.adventure.entity.components.InputComponent;
import com.souki.game.adventure.entity.components.InteractionComponent;
import com.souki.game.adventure.entity.components.LightComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.entity.components.VisualComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.IInteractionEventListener;
import com.souki.game.adventure.events.IQuestListener;
import com.souki.game.adventure.gui.challenge.ChallengeLaser;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.quests.Quest;
import com.souki.game.adventure.quests.QuestManager;
import com.souki.game.adventure.quests.QuestTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vincent on 08/02/2017.
 */

public class Interaction extends Entity implements ICollisionObstacleHandler, ICollisionInteractionHandler, IInteraction, GestureDetector.GestureListener, IInteractionEventListener, IQuestListener {

    protected String mId;
    protected Type mType;
    protected InteractionDef mDef;
    protected boolean mIsMovable;

    protected float mStateTime; // elapsed time
    protected boolean mIsRended = false;
    protected TextureAtlas.AtlasRegion mCurrentFrame; // current animation frame

    protected InteractionState mCurrentState;

    protected Array<CollisionObstacleComponent> mCollisionsObstacle = new Array<CollisionObstacleComponent>();
    protected Array<CollisionInteractionComponent> mCollisionsInteraction = new Array<CollisionInteractionComponent>();
    protected Array<CollisionEffectComponent> mCollisionsEffect = new Array<CollisionEffectComponent>();

    protected byte mCollisionType = CollisionObstacleComponent.MAPINTERACTION;

    protected Shape mShapeCollision;
    protected int mCollisionHeightFactor = 8;
    protected float mInteractionBorderSize = 0.1F;
    protected Shape mShapeInteraction;
    protected Shape mShapeRendering;
    protected float[] mVertices = new float[8];
    protected int mZIndex = 1;


    protected Camera mCamera;

    public ArrayList<InteractionEventAction> mEventsAction;
    public ArrayList<InteractionEvent> mOutputEvents;

    public ArrayList<InteractionQuestAction> mQuestsActions;

    protected MapProperties mMapProperties;
    protected HashMap mProperties;
    protected GameMap mMap;
    protected Persistence mPersistence;

    protected Effect mEffectLaunched;
    protected float mEffectLaunchedTime;
    protected CircleShape mZoneLaunchedEffect;

    protected Effect mEffectAction;
    protected float mEffectActionTime;

    protected boolean mShouldNotfyStartState = false;

    protected String mAtlasFilename;

    public String getAtlasFilename() {
        return mAtlasFilename;
    }

    @Override
    public void setCamera(Camera aCamera) {
        mCamera = aCamera;
    }

    public Interaction(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        mId = aMapping.id;
        mDef = aDef;
        mPersistence = aDef.persistence;
        if (aMapping.persistence != null) {
            mPersistence = aMapping.persistence;
        }
        mEventsAction = aDef.eventsAction;
        if (mEventsAction == null) {
            mEventsAction = new ArrayList<>();
        }
        for (InteractionEventAction action : mEventsAction) {
            if (action.inputEvents != null) {
                for (InteractionEvent event : action.inputEvents) {
                    if (event.sourceId != null && event.sourceId.compareTo(InteractionEvent.THIS) == 0) {
                        event.sourceId = mId;
                    }
                }
            }
        }
        mOutputEvents = aDef.outputEvents;
        if (mOutputEvents == null) {
            mOutputEvents = new ArrayList<>();
        }
        mProperties = aDef.properties;
        if (mProperties == null) {
            mProperties = new HashMap<>();
        }
        if (aMapping.properties != null) {
            mProperties.putAll(aMapping.properties);
        }
        if (aMapping.eventsAction != null) {
            // check again to be sure interaction mapping has not overwrite an event using true id or THIS id
            for (InteractionEventAction action : aMapping.eventsAction) {
                if (action.inputEvents != null) {
                    for (InteractionEvent event : action.inputEvents) {
                        if (event.sourceId != null && event.sourceId.compareTo(InteractionEvent.THIS) == 0) {
                            event.sourceId = mId;
                        }
                    }
                }
            }
            mEventsAction.addAll(aMapping.eventsAction);
        }

        if (aMapping.outputEvents != null) {
            mOutputEvents.addAll(aMapping.outputEvents);
        }
        mQuestsActions = aMapping.questActions;
        mMap = aMap;
        mMapProperties = aProperties;

        EntityEngine.getInstance().addEntity(this);


        this.add(new TransformComponent());
        if (mProperties.containsKey("ZIndex")) {
            mZIndex = ((Float) mProperties.get("ZIndex")).intValue();
        }
        if (mProperties.containsKey("collisionHeightFactor")) {
            mCollisionHeightFactor = ((Float) mProperties.get("CollisionHeightFactor")).intValue();
        }
        setMovable(mDef.isMovable);
        if (mProperties.containsKey("startState")) {

            mCurrentState = getState((String) mProperties.get("startState"));
        }

        if (mCurrentState == null) {
            mCurrentState = getState(mDef.defaultState);
        }
        //    mType = IInteraction.Type.valueOf(mDef.type);
        if (mOutputEvents != null) {
            for (InteractionEvent event : mOutputEvents) {
                event.sourceId = mId;
            }
        }
        mShapeCollision = createShapeCollision();
        mShapeInteraction = createShapeInteraction();
        mShapeRendering = createShapeRendering();
        if (mProperties.containsKey("hasLight") && (Boolean) mProperties.get("hasLight")) {
            LightComponent lightComponent = new LightComponent();
            if (mProperties.containsKey("lightColor")) {
                lightComponent.setColor(Color.valueOf((String) mProperties.get("lightColor")));
            }
            if (mProperties.containsKey("lightRadius")) {
                lightComponent.setRadius((Float) mProperties.get("lightRadius"));
            }
            this.add(lightComponent);
        }

    }

    protected void initLight(String aColor) {
        Color color;

        try {
            color = ChallengeLaser.LASER_COLOR.valueOf(aColor).mColor;

        } catch (Exception e) {
            try {
                color = Color.valueOf(aColor);

            } catch (Exception e2) {

                return;
            }
        }

        LightComponent lightComponent = getComponent(LightComponent.class);
        if (lightComponent == null) {
            lightComponent = new LightComponent();
        }
        lightComponent.setColor(color);
        if (mProperties.containsKey("lightRadius")) {
            lightComponent.setRadius((Float) mProperties.get("lightRadius"));
        }
        TransformComponent transformComponent = this.getComponent(TransformComponent.class);

        if (mShapeRendering != null) {
            lightComponent.setPosition(transformComponent.position.x + transformComponent.originOffset.x + mShapeRendering.getWidth() / 2, transformComponent.position.y + transformComponent.originOffset.y + mShapeRendering.getHeight() / 2);
        } else {
            lightComponent.setPosition(transformComponent.position.x + transformComponent.originOffset.x, transformComponent.position.y + transformComponent.originOffset.y);
        }


        this.add(lightComponent);

    }

    protected void removeLight() {

        LightComponent lightComponent = getComponent(LightComponent.class);
        if (lightComponent != null) {
            remove(LightComponent.class);
        }

    }

    public GameMap getMap() {
        return mMap;
    }

    public void restoreFromPersistence(GameSession aGameSession) {

        String lightColor = (String) aGameSession.getSessionDataForMapAndEntity(getMap().getMapName(), getId(), "LIGHT");
        if (lightColor != null) {
            initLight(lightColor);
        }


    }

    public GameSession saveInPersistence(GameSession aGameSession) {


        LightComponent lightComponent = this.getComponent(LightComponent.class);
        if(lightComponent!=null)
        {
            aGameSession.putSessionDataForMapAndEntity(getMap().getMapName(), getId(), "LIGHT", lightComponent.color.toString());

        }
        return aGameSession;
    }

    public void restoreFromPersistence() {
        if (getPersistence() == Persistence.GAME) {
            restoreFromPersistence(Profile.getInstance().getPersistentGameSession());
        } else if (getPersistence() == Persistence.SESSION) {
            restoreFromPersistence(GameSession.getInstance());

        }
    }

    public void saveInPersistence() {
        if (getPersistence() == Persistence.GAME) {
            Profile.getInstance().updatePersistentGameSession(saveInPersistence(Profile.getInstance().getPersistentGameSession()));
        } else if (getPersistence() == Persistence.SESSION) {
            saveInPersistence(GameSession.getInstance());

        }
    }


    @Override
    public float getX() {
        return getPosition().x;
    }

    @Override
    public float getY() {
        return getPosition().y;
    }

    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public boolean isClickable() {
        return mDef.isClickable;
    }

    @Override
    public boolean isMovable() {
        return mIsMovable;
    }

    public void setMovable(boolean isMovable) {
        mIsMovable = isMovable;
        if (mIsMovable) {
            this.add(new VelocityComponent());
        } else {
            remove(VelocityComponent.class);
        }
    }

    @Override
    public Persistence getPersistence() {
        return mPersistence;
    }


    public InteractionState getState(String aStateName) {
        if (aStateName == null)
            return null;
        for (InteractionState state : mDef.states) {
            if (state.name.compareTo(aStateName) == 0) {
                return state;
            }
        }
        return null;
    }

    public void setState(String aStateName) {
        InteractionState state = getState(aStateName);
        if (state != null && state != mCurrentState) {

            mCurrentState = state;
            //Gdx.app.debug("DEBUG", "set state " + mCurrentState.name+ " on id "+mId);
            mStateTime = 0;
            if (mOutputEvents != null) {
                for (InteractionEvent event : mOutputEvents) {

                    if (InteractionEvent.EventType.STATE == InteractionEvent.EventType.valueOf(event.type) &&
                            mCurrentState.name.equals(event.value)) {
                        EventDispatcher.getInstance().onInteractionEvent(event);
                    }
                }
            }
            if (mCurrentState.name.compareTo(InteractionState.STATE_FROZEN) == 0 ||
                    mCurrentState.name.compareTo(InteractionState.STATE_EXPLODE) == 0) {
                setMovable(false);
            }
        }
    }

    public void destroy() {
        EventDispatcher.getInstance().removeInteractionEventListener(this);
        saveInPersistence();

        EntityEngine.getInstance().removeEntity(this);
    }

    public void initialize(float x, float y, InteractionMapping aMapping) {


        if (mDef.isRendable) {

            String[] files = ("data/interactions/" + mDef.atlas).split("/");
            ArrayList<String> path = new ArrayList();
            for (int i = 0; i < files.length; i++) {
                if (files[i].compareTo("..") == 0 && path.size() > 0) {
                    path.remove(path.size() - 1);
                } else {
                    path.add(files[i]);
                }
            }
            mAtlasFilename = "";
            String sep = "";
            while (path.size() > 0) {
                mAtlasFilename += sep + path.remove(0);
                sep = "/";
            }

            AssetsUtility.loadTextureAtlasAsset(mAtlasFilename);

            for (InteractionState state : mDef.states) {
                state.init(AssetsUtility.getTextureAtlasAsset(mAtlasFilename));
            }
            mCurrentFrame = mCurrentState.getTextureRegion(0F);
            if (mCurrentFrame != null) {
                TransformComponent transform = this.getComponent(TransformComponent.class);
                transform.scale = MyGame.SCALE_FACTOR;

                transform.setOriginOffset(0, 0);
                this.add(new VisualComponent(mCurrentFrame, this));
            }

        }
        setPosition(x, y);

        restoreFromPersistence();

        mShouldNotfyStartState = false;
        // force state event if startState has been set
        if (mProperties.containsKey("startState")) {
            String initState = (String) mProperties.get("startState");
            InteractionState stateAfterPersistenceRestored = mCurrentState;
            if (initState != null) {
                mCurrentState = null;
                if (stateAfterPersistenceRestored != null) {
                    setState(stateAfterPersistenceRestored.name);
                    mShouldNotfyStartState = true;
                } else if (getState(initState) != null) {
                    setState(initState);
                    mShouldNotfyStartState = true;
                }

            }

        }

        updateRendering(0);
        updateCollision(0);
        updateInteraction(0);
        updateLight();

        if (mEventsAction != null) {
            EventDispatcher.getInstance().addInteractionEventListener(this);
        }
        if (mQuestsActions != null) {
            EventDispatcher.getInstance().addQuestListener(this);
        }
        add(new InteractionComponent(this));


    }


    public void setPosition(float x, float y) {
        TransformComponent transformComponent = this.getComponent(TransformComponent.class);
        transformComponent.position.x = x;
        transformComponent.position.y = y;
        LightComponent lightComponent = this.getComponent(LightComponent.class);
        if (lightComponent != null) {
            if (mShapeRendering != null) {
                lightComponent.setPosition(transformComponent.position.x + transformComponent.originOffset.x + mShapeRendering.getWidth() / 2, transformComponent.position.y + transformComponent.originOffset.y + mShapeRendering.getHeight() / 2);
            } else {
                lightComponent.setPosition(transformComponent.position.x + transformComponent.originOffset.x, transformComponent.position.y + transformComponent.originOffset.y);
            }
        }

    }

    public void setPosition(Vector2 pos) {
        TransformComponent transformComponent = this.getComponent(TransformComponent.class);
        transformComponent.position.x = pos.x;
        transformComponent.position.y = pos.y;
        LightComponent lightComponent = this.getComponent(LightComponent.class);
        if (lightComponent != null) {
            if (mShapeRendering != null) {
                lightComponent.setPosition(transformComponent.position.x + transformComponent.originOffset.x + mShapeRendering.getWidth() / 2, transformComponent.position.y + transformComponent.originOffset.y + mShapeRendering.getHeight() / 2);
            } else {
                lightComponent.setPosition(transformComponent.position.x + transformComponent.originOffset.x, transformComponent.position.y + transformComponent.originOffset.y);
            }
        }
    }

    public Vector2 getPosition() {
        TransformComponent transformComponent = this.getComponent(TransformComponent.class);
        return new Vector2(transformComponent.position.x, transformComponent.position.y);
    }

    public void setVelocity(float vx, float vy) {
        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity != null) {
            velocity.x = vx;
            velocity.y = vy;
        }
    }

    public void setVelocity(Vector2 v) {
        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity != null) {
            velocity.x = v.x;
            velocity.y = v.y;
        }
    }

    @Override
    public void update(float dt) {
        mStateTime += dt;
        if (mCurrentState.isCompleted(mStateTime)) {

            if (mOutputEvents != null) {
                for (InteractionEvent event : mOutputEvents) {
                    if (InteractionEvent.EventType.END_STATE == InteractionEvent.EventType.valueOf(event.type) &&
                            mCurrentState.name.equals(event.value)) {
                        EventDispatcher.getInstance().onInteractionEvent(event);
                    }
                }
            }
        }
        updateRendering(dt);
        updateCollision(dt);
        updateInteraction(dt);
        updateEffects(dt);


    }

    /*************************************** RENDERING ************************************/
    @Override
    public boolean isRendable() {
        return mDef.isRendable;
    }

    @Override
    public boolean isRended() {
        return mIsRended;
    }

    @Override
    public int getZIndex() {
        return mZIndex;
    }

    @Override
    public void setRended(boolean aRended) {
        mIsRended = aRended;
    }

    public Shape createShapeRendering() {
        return new PolygonShape();
    }

    public void updateRendering(float dt) {
        TransformComponent tfm = this.getComponent(TransformComponent.class);
        if (isRendable()) {
            float width = mCurrentFrame.getRegionWidth() * tfm.scale;
            float height = mCurrentFrame.getRegionHeight() * tfm.scale;

            tfm.setOriginOffset(mCurrentFrame.offsetX * tfm.scale, mCurrentFrame.offsetY * tfm.scale);

            mVertices[0] = 0;
            mVertices[1] = 0;
            mVertices[2] = 0;
            mVertices[3] = height;
            mVertices[4] = width;
            mVertices[5] = height;
            mVertices[6] = width;
            mVertices[7] = 0;


            if (mShapeRendering.getType() == Shape.Type.POLYGON) {
                ((PolygonShape) mShapeRendering).getShape().setVertices(mVertices);
            }

        }
        mShapeRendering.setX(tfm.position.x + tfm.originOffset.x);
        mShapeRendering.setY(tfm.position.y + tfm.originOffset.y);

    }

    public void updateLight() {
        TransformComponent tfm = this.getComponent(TransformComponent.class);

        LightComponent lightComponent = this.getComponent(LightComponent.class);
        if (lightComponent != null) {
            if (mShapeRendering != null) {
                lightComponent.setPosition(tfm.position.x + tfm.originOffset.x + mShapeRendering.getWidth() / 2, tfm.position.y + tfm.originOffset.y + mShapeRendering.getHeight() / 2);
            } else if (mShapeCollision != null) {
                lightComponent.setPosition(tfm.position.x + tfm.originOffset.x + mShapeCollision.getWidth() / 2, tfm.position.y + tfm.originOffset.y + mShapeCollision.getHeight() / 2);
            } else {
                lightComponent.setPosition(tfm.position.x + tfm.originOffset.x, tfm.position.y + tfm.originOffset.y);
            }
        }
    }

    @Override
    public Shape getShapeRendering() {

        return mShapeRendering;
    }

    protected void setEndMoveState() {
        setState(mDef.defaultState);
    }

    @Override
    public void render(Batch batch) {
        TransformComponent transform = this.getComponent(TransformComponent.class);
        VisualComponent visual = this.getComponent(VisualComponent.class);
        VelocityComponent velocity = this.getComponent(VelocityComponent.class);

        if (velocity != null) {
            if (velocity.y == 0 && velocity.x == 0) {
                setEndMoveState();
                mStateTime = 0;
            } else {
                if (velocity.y == 0) {
                    setState(velocity.x < 0 ? InteractionState.STATE_MOVE_LEFT : InteractionState.STATE_MOVE_RIGHT);
                } else {
                    double angle = Math.atan2(velocity.y, velocity.x);
                    double PI4 = Math.PI / 4;
                    double PI8 = Math.PI / 8;
                    if (angle < 0) {
                        angle += Math.PI * 2;
                    }

                    if ((angle > PI8 && angle <= 3 * PI8) && getState(InteractionState.STATE_MOVE_RIGHT_UP) != null) {
                        setState(InteractionState.STATE_MOVE_RIGHT_UP);
                    } else if ((angle > 13 * PI8 && angle <= 15 * PI8) && getState(InteractionState.STATE_MOVE_RIGHT_DOWN) != null) {
                        setState(InteractionState.STATE_MOVE_RIGHT_DOWN);
                    } else if ((angle > 5 * PI8 && angle <= 7 * PI8) && getState(InteractionState.STATE_MOVE_LEFT_UP) != null) {
                        setState(InteractionState.STATE_MOVE_LEFT_UP);
                    } else if ((angle > 5 * PI8 && angle <= 7 * PI8) && getState(InteractionState.STATE_MOVE_LEFT_UP) != null) {
                        setState(InteractionState.STATE_MOVE_LEFT_UP);
                    } else if ((angle > 9 * PI8 && angle <= 11 * PI8) && getState(InteractionState.STATE_MOVE_LEFT_DOWN) != null) {
                        setState(InteractionState.STATE_MOVE_LEFT_DOWN);
                    } else if (angle > 7 * PI4 || angle <= PI4) {
                        setState(InteractionState.STATE_MOVE_RIGHT);
                    } else if (angle > PI4 && angle <= 3 * PI4) {
                        setState(InteractionState.STATE_MOVE_UP);
                    } else if (angle > 3 * PI4 && angle <= 5 * PI4) {
                        setState(InteractionState.STATE_MOVE_LEFT);
                    } else if (angle > 5 * PI4 && angle <= 7 * PI4) {
                        setState(InteractionState.STATE_MOVE_DOWN);
                    }
                }
            }
        }
        mCurrentFrame = mCurrentState.getTextureRegion(mStateTime);
        if (visual != null && mCurrentFrame != null) {
            visual.region = mCurrentFrame;
            float width = visual.region.getRegionWidth();
            float height = visual.region.getRegionHeight();
            float halfWidth = width / 2f;
            float halfHeight = height / 2f;
            //Allow for Offset
            float originX = 0;//transform.originOffset.x;
            float originY = 0;//transform.originOffset.y;

            batch.draw(visual.region,
                    transform.position.x + transform.originOffset.x, transform.position.y + transform.originOffset.y,
                    originX, originY,
                    width, height,
                    transform.scale, transform.scale,
                    transform.angle);
        }
        renderEffect(batch);
    }


    /*************************************** PHYSICAL COLLISION ************************************/

    public void updateCollision(float dt) {
        TransformComponent tfm = this.getComponent(TransformComponent.class);
        mShapeCollision.setX(tfm.position.x + tfm.originOffset.x);
        mShapeCollision.setY(tfm.position.y + tfm.originOffset.y);
        if (isRendable()) {
            if (mShapeCollision instanceof RectangleShape) {
                RectangleShape shape = (RectangleShape) mShapeCollision;
                shape.getShape().setWidth(mShapeRendering.getWidth());
                shape.getShape().setHeight((float) Math.max(0.2, mShapeRendering.getHeight() / mCollisionHeightFactor));

            } else if (mShapeCollision instanceof CircleShape) {
                CircleShape shape = (CircleShape) mShapeCollision;
                shape.getShape().setRadius((float) Math.max(0.2, mShapeRendering.getHeight() / mCollisionHeightFactor));
            }
        }
    }

    public Shape createShapeCollision() {
        RectangleShape shape = new RectangleShape();
        shape.setShape(new Rectangle(0, 0, 1, 1));
        return shape;
    }

    public Shape getShapeCollision() {

        return mShapeCollision;
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {

        if (!mCollisionsObstacle.contains(aEntity, false)) {
            mCollisionsObstacle.add(aEntity);
            if (mOutputEvents != null) {
                for (InteractionEvent event : mOutputEvents) {
                    if (InteractionEvent.EventType.COLLISION == InteractionEvent.EventType.valueOf(event.type)) {
                        InteractionEvent emittedEvent = event.clone();
                        emittedEvent.value = aEntity.mName;
                        EventDispatcher.getInstance().onInteractionEvent(emittedEvent);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {
        if (mCollisionsObstacle.contains(aEntity, false)) {
            mCollisionsObstacle.removeValue(aEntity, false);
            return true;
        }
        return false;
    }

    @Override
    public Array<CollisionObstacleComponent> getCollisionObstacle() {
        return mCollisionsObstacle;
    }

    /*************************************** INTERACTIONS  ************************************/

    public void startToInteract() {
        updateRendering(0);
        updateCollision(0);
        updateInteraction(0);
        updateLight();
        if (isRendable()) {
            this.add(new CollisionObstacleComponent(mCollisionType, getShapeCollision(), mId, this, this));
        }
        this.add(new CollisionInteractionComponent(getShapeInteraction(), this, this));
        if (mQuestsActions != null) {
            for (InteractionQuestAction action : mQuestsActions) {
                onQuestEvent(QuestManager.getInstance().getQuestFromId(action.questId));
            }
        }
        if (mShouldNotfyStartState) {
            mShouldNotfyStartState = false;
            if (mOutputEvents != null) {
                for (InteractionEvent event : mOutputEvents) {

                    if (InteractionEvent.EventType.STATE == InteractionEvent.EventType.valueOf(event.type) &&
                            mCurrentState.name.equals(event.value)) {
                        EventDispatcher.getInstance().onInteractionEvent(event);
                    }
                }
            }
        }

    }

    public void updateInteraction(float dt) {
        TransformComponent tfm = this.getComponent(TransformComponent.class);
        if (isRendable()) {
            RectangleShape interactionArea = (RectangleShape) mShapeInteraction;
            interactionArea.getShape().set(mShapeRendering.getX() - mInteractionBorderSize, mShapeRendering.getY() - mInteractionBorderSize, mShapeRendering.getWidth() + 2 * mInteractionBorderSize, mShapeRendering.getHeight() + 2 * mInteractionBorderSize);

        } else if (getComponent(CollisionObstacleComponent.class) != null) {
            mShapeInteraction = mShapeCollision;
        } else {
            mShapeInteraction.setX(tfm.position.x + tfm.originOffset.x);
            mShapeInteraction.setY(tfm.position.y + tfm.originOffset.y);
        }

    }

    public Shape createShapeInteraction() {
        RectangleShape shape = new RectangleShape();
        shape.setShape(new Rectangle(0, 0, 1, 1));
        return shape;
    }

    @Override
    public Shape getShapeInteraction() {
        return mShapeInteraction;
    }

    public boolean hasCollisionInteraction(CollisionInteractionComponent aEntity) {
        return false;
    }

    public void onStartCollisionInteraction(CollisionInteractionComponent aEntity) {

    }

    public void onStopCollisionInteraction(CollisionInteractionComponent aEntity) {

    }

    @Override
    public boolean onCollisionInteractionStart(CollisionInteractionComponent aEntity) {

        if (!mCollisionsInteraction.contains(aEntity, false)) {
            mCollisionsInteraction.add(aEntity);
            if (hasCollisionInteraction(aEntity)) {

                if (isClickable()) {
                    add(new InputComponent(this));
                }

                onStartCollisionInteraction(aEntity);
                return true;
            }

        }
        return false;

    }

    @Override
    public boolean onCollisionInteractionStop(CollisionInteractionComponent aEntity) {
        if (mCollisionsInteraction.contains(aEntity, false)) {
            mCollisionsInteraction.removeValue(aEntity, false);

            if (isClickable()) {
                remove(InputComponent.class);
            }
            onStopCollisionInteraction(aEntity);
            return true;
        }
        return false;
    }

    @Override
    public Array<CollisionInteractionComponent> getCollisionInteraction() {
        return mCollisionsInteraction;
    }

    /*************************************** EVENTS ************************************/
    @Override
    public void onInteractionEvent(InteractionEvent aEvent) {
        Gdx.app.debug("DEBUG", "onInteractionEvent id=" + getId() + " event.sourceId=" + aEvent.sourceId + " aEvent.type=" + aEvent.type + " aEvent.value=" + aEvent.value);

        if (mEventsAction != null && aEvent != null) {
            for (InteractionEventAction eventAction : mEventsAction) {
                if (eventAction.inputEvents != null) {

                    boolean eventFound = false;
                    boolean eventPerformed = false;
                    boolean allPerformed = true;
                    for (InteractionEvent expectedEvent : eventAction.inputEvents) {
                        if ((expectedEvent.sourceId == null || expectedEvent.sourceId.isEmpty() || expectedEvent.sourceId.equals(aEvent.sourceId)) && expectedEvent.type.equals(aEvent.type)) {
                            boolean conditionValue = expectedEvent.value.equals(aEvent.value) || (expectedEvent.value == null && aEvent.value.isEmpty()) || (aEvent.value == null && expectedEvent.value.isEmpty());
                            eventPerformed = expectedEvent.isNotValue ? !conditionValue : conditionValue;
                            expectedEvent.setPerformed(expectedEvent.isVolatile ? false : eventPerformed);
                            eventFound = true;
                            //  Gdx.app.debug("DEBUG", "id=" + getId() + " expectedEvent.sourceId=" + expectedEvent.sourceId + " expectedEvent.type=" + expectedEvent.type + " expectedEvent.value=" + expectedEvent.value+" eventPerformed=" + eventPerformed);
                            if (!eventPerformed) {
                                allPerformed = false;
                                break; // no need to check more events
                            }
                        } else {
                            if (eventFound && !allPerformed) {
                                break; // no need to check more events
                            } else if (!expectedEvent.isPerformed()) {
                                allPerformed = false; // at least one is not performed
                                if (eventFound) {
                                    break; // if current event has been found, non need to check more
                                }
                            }
                        }

                    }
                    // Gdx.app.debug("DEBUG", "id=" + getId() + " check action=" + eventAction.action.type.name()+ " with value=" + eventAction.action.value+" allPerformed="+allPerformed);

                    if (allPerformed && eventFound) {
                        for (InteractionEvent expectedEvent : eventAction.inputEvents) {
                            if (!expectedEvent.isPersistent) {
                                expectedEvent.setPerformed(false);
                            }
                        }
                        doAction(eventAction.action);
                        if (eventAction.action.stopPropagation) {
                            return;
                        }
                    }

                }
            }
        }
    }

    /**
     * check if an action should be done
     *
     * @param aAction the action to be checked
     * @return true if action has be done, false in other cases
     */
    protected boolean doAction(InteractionActionType aAction) {
        if (aAction != null && InteractionActionType.ActionType.SET_STATE == aAction.type) {
            Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId + " value=" + aAction.value);

            if (getState(aAction.value) != null) {
                setState(aAction.value);
                return true;
            }
        } else if (aAction != null && InteractionActionType.ActionType.SET_LIGHT == aAction.type) {
            Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId + " value=" + aAction.value);
            initLight(aAction.value);
            return true;
        } else if (aAction != null && InteractionActionType.ActionType.REMOVE_LIGHT == aAction.type) {
            Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId + " value=" + aAction.value);
            removeLight();
            return true;
        } else if (aAction != null && InteractionActionType.ActionType.ACTIVATE_QUEST == aAction.type) {
            Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId);

            Quest quest = QuestManager.getInstance().getQuestFromId(aAction.value);
            if (quest != null && !quest.isActivated() && !quest.isCompleted()) {

                QuestManager.getInstance().activateQuestIfAllDependenciesCompleted(quest);
                return true;
            }
        }
        return false;
    }


    protected void onQuestEvent(Quest aQuest) {
        if (mQuestsActions != null && aQuest != null) {
            Gdx.app.debug("DEBUG", "onQuestEvent questID=" + aQuest.getId() + " isActivated=" + aQuest.isActivated() + " isCompleted=" + aQuest.isCompleted());
            for (InteractionQuestAction questAction : mQuestsActions) {
                if ((aQuest.getId() != null && questAction.questId != null && aQuest.getId().equals(questAction.questId))) {
                    boolean doAction = false;
                    if (questAction.questState == InteractionQuestAction.QuestState.FINISHED && aQuest.isCompleted() && QuestManager.getInstance().getLivingQuestFromId(aQuest.getId()) == null) {
                        doAction = true;
                    } else if (questAction.questState == InteractionQuestAction.QuestState.COMPLETED && aQuest.isCompleted() && QuestManager.getInstance().getLivingQuestFromId(aQuest.getId()) != null) {
                        doAction(questAction.action);
                        return;
                    } else if (questAction.questState == InteractionQuestAction.QuestState.NOT_COMPLETED &&
                            QuestManager.getInstance().getLivingQuestFromId(aQuest.getId()) != null &&
                            aQuest.isActivated() &&
                            !aQuest.isCompleted()) {
                        doAction(questAction.action);
                        return;
                    } else if (questAction.questState == InteractionQuestAction.QuestState.NOT_ACTIVATED &&
                            QuestManager.getInstance().getLivingQuestFromId(aQuest.getId()) == null &&
                            !aQuest.isCompleted()) {
                        doAction(questAction.action);
                        return;
                    }

                    if (doAction) {
                        doAction(questAction.action);
                        return;
                    }


                }
            }
        }
    }

    ;

    @Override
    public void onQuestActivated(Quest aQuest) {
        onQuestEvent(aQuest);
    }

    @Override
    public void onQuestCompleted(Quest aQuest) {
        onQuestEvent(aQuest);
    }

    @Override
    public void onQuestTaskCompleted(Quest aQuest, QuestTask aTask) {
    }

    /*************************************** EFFECTS ************************************/

    protected void updateEffects(float dt) {
        if (mEffectLaunched != null) {
            updateLaunchedEffect(dt);
        }
        if (mEffectAction != null) {
            updateEffectAction(dt);
        }
    }


    protected void updateLaunchedEffect(float dt) {
        float xOffset = getShapeInteraction().getWidth() / 2 - mZoneLaunchedEffect.getShape().radius;
        float yOffset = getShapeInteraction().getHeight() / 2 - mEffectLaunched.distance;
        mZoneLaunchedEffect.setX(getShapeInteraction().getX() + xOffset);
        mZoneLaunchedEffect.setY(getShapeInteraction().getY() + yOffset);
        mEffectLaunchedTime += dt;
        float timeAction = mEffectLaunched.duration;
        if (timeAction < 0) {
            timeAction = mEffectLaunched.frames.size() / mEffectLaunched.fps;
        }
        if (mEffectLaunchedTime > timeAction) {
            stopLaunchedEffect();
        }
    }

    protected void updateEffectAction(float dt) {
        if (mEffectAction != null && mEffectAction.targetDuration != 0) {
            mEffectActionTime += dt;
            float timeAction = mEffectAction.targetDuration;
            boolean isTerminated = false;
            if (timeAction < 0 || !mCurrentState.isLooping) {
                //timeAction =  mEffectAction.frames.size()/mEffectAction.fps;
                isTerminated = mCurrentState.isCompleted(mStateTime);
            } else if (mEffectActionTime > timeAction) {
                isTerminated = true;

            }
            if (isTerminated) {
                stopEffectAction();
            }
        }
    }


    public boolean onStartEffectInteraction(CollisionEffectComponent aEntity) {
        Effect effect = (Effect) aEntity.mEffect;
        if (effect != null) {
            if (getState(effect.targetState) != null) {
                mEffectAction = effect;
            }
            EventDispatcher.getInstance().onInteractionEvent(new InteractionEvent(getId(), InteractionEvent.EventType.EFFECT_START.name(), effect.id.name()));
        }
        return false;
    }

    public boolean onStopEffectInteraction(CollisionEffectComponent aEntity) {
        if (mEffectAction != null && mEffectAction.duration == 0) {
            stopEffectAction();
            return true;
        }

        return false;
    }

    @Override
    public boolean onCollisionEffectStart(CollisionEffectComponent aEntity) {
        if (!mCollisionsEffect.contains(aEntity, false)) {
            mCollisionsEffect.add(aEntity);
            onStartEffectInteraction(aEntity);
            return true;
        }
        return false;
    }

    @Override
    public boolean onCollisionEffectStop(CollisionEffectComponent aEntity) {
        if (mCollisionsEffect.contains(aEntity, false)) {
            mCollisionsEffect.removeValue(aEntity, false);
            onStopEffectInteraction(aEntity);
            return true;
        }
        return false;
    }

    @Override
    public Array<CollisionEffectComponent> getCollisionEffect() {
        return mCollisionsEffect;
    }

    public void launchEffect(Effect aEffect) {
        mEffectLaunched = aEffect;
        mEffectLaunchedTime = 0;
        mZoneLaunchedEffect = new CircleShape();
        mZoneLaunchedEffect.setRadius(mEffectLaunched.distance);
        float xOffset = getShapeInteraction().getWidth() / 2 - mZoneLaunchedEffect.getShape().radius;
        float yOffset = getShapeInteraction().getHeight() / 2 - mEffectLaunched.distance;
        mZoneLaunchedEffect.setX(getShapeInteraction().getX() + xOffset);
        mZoneLaunchedEffect.setY(getShapeInteraction().getY() + yOffset);
        if (mEffectLaunched.distance > 0) {
            add(new CollisionEffectComponent(mEffectLaunched, mZoneLaunchedEffect, mId));
        }
        if (mEffectLaunched.sound != null && !mEffectLaunched.sound.isEmpty()) {
            AudioManager.getInstance().onAudioEvent(new AudioEvent(AudioEvent.Type.SOUND_PLAY_ONCE, mEffectLaunched.sound));
        }
    }

    protected void stopLaunchedEffect() {
        remove(CollisionEffectComponent.class);
        if (mEffectLaunched == null)
            return;

        if (mEffectLaunched.sound != null && !mEffectLaunched.sound.isEmpty()) {
            AudioManager.getInstance().onAudioEvent(new AudioEvent(AudioEvent.Type.SOUND_STOP, mEffectLaunched.sound));
        }

        if (mEffectLaunched.id == Effect.Type.PORTAL) {
            EventDispatcher.getInstance().onInteractionEvent(new InteractionEvent(getId(), InteractionEvent.EventType.EFFECT_STOP.name(), mEffectLaunched.id.name()));
        }
        mEffectLaunched = null;
        mEffectLaunchedTime = 0;
    }

    protected void stopEffectAction() {
        EventDispatcher.getInstance().onInteractionEvent(new InteractionEvent(getId(), InteractionEvent.EventType.EFFECT_STOP.name(), mEffectAction.id.name()));
        mEffectAction = null;
        mEffectActionTime = 0;
    }

    protected void renderEffect(Batch aBatch) {
        if (mEffectLaunched != null) {
            TransformComponent transform = this.getComponent(TransformComponent.class);

            mEffectLaunched.renderEffect(aBatch,
                    mCurrentFrame.getRegionWidth(),
                    mCurrentFrame.getRegionHeight(),
                    transform.position.x + transform.originOffset.x,
                    transform.position.y + transform.originOffset.y,
                    transform.scale,
                    mEffectLaunchedTime);


        }
    }

    /*************************************** TOUCH ************************************/

    protected boolean hasTouchInteraction(float x, float y) {
        return isClickable();
    }

    public void onTouchInteraction() {

    }


    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        Vector3 cursorPoint = new Vector3();

        mCamera.unproject(cursorPoint.set(x, y, 0));

        if (hasTouchInteraction(cursorPoint.x, cursorPoint.y)) {
            onTouchInteraction();
            return true;
        }
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {

    }
}
