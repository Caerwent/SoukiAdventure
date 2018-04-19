package com.souki.game.adventure.interactions;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionFollowPath extends Interaction {
    protected PathMap mPath;
    protected boolean mIsAutoStart;
    protected boolean mIsInterruptedByObstacle;
    protected boolean mIsStopOnObstacle;

    public InteractionFollowPath(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);

        if (mProperties != null) {
            if (mProperties.containsKey("pathId")) {
                mPath = aMap.getPaths().get((String) mProperties.get("pathId"));
                if (mProperties.containsKey("autoStart")) {
                    mIsAutoStart = Boolean.valueOf((String) mProperties.get("autoStart"));
                }
                if (mProperties.containsKey("looping")) {
                    mPath.setLoop(Boolean.valueOf((String) mProperties.get("looping")));
                }
                if (mProperties.containsKey("loopingReversing")) {
                    mPath.setLoopReversing(Boolean.valueOf((String) mProperties.get("loopingReversing")));
                }
            }
            if (mProperties.containsKey("stopOnObstacle")) {
                mIsStopOnObstacle = Boolean.valueOf((String) mProperties.get("stopOnObstacle"));
            }

        }
        initialize(x, y, aMapping);


    }

    @Override
    protected void setEndMoveState() {
        // stay on last state
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        VelocityComponent velocity = this.getComponent(VelocityComponent.class);
        if (velocity != null) {
            if (mPath != null && mPath.hasNextPoint()) {
                TransformComponent transform = this.getComponent(TransformComponent.class);
                Vector2 pos2D = new Vector2(transform.position.x, transform.position.y);
                setVelocity(mPath.getVelocityForPosAndTime(pos2D, dt, mId));
            } else {
                setMovable(false);
            }

        }
    }

    protected void setNearestPathPoint() {
        TransformComponent transform = this.getComponent(TransformComponent.class);
        if (transform != null && mPath != null) {
            Vector2 pos2D = new Vector2(transform.position.x, transform.position.y);
            mPath.setNearestPoint(pos2D);

        }

    }

    @Override
    protected boolean doAction(InteractionActionType aAction) {
        boolean res = super.doAction(aAction);
        if (!res && aAction != null && InteractionActionType.ActionType.WAKEUP == aAction.type) {
            if (mPath != null) {
                setMovable(true);
            }
            return true;
        }
        if (!res && aAction != null && InteractionActionType.ActionType.SLEEP == aAction.type) {
            if (mPath != null) {
                setMovable(false);
            }
            return true;
        }
        if (!res && aAction != null && InteractionActionType.ActionType.SET_PATH == aAction.type) {
            if (mPath != mMap.getPaths().get(aAction.value)) {
                mPath = mMap.getPaths().get(aAction.value);
                mPath.setCompleted(false);
                setNearestPathPoint();
                if (mProperties.containsKey("looping")) {
                    mPath.setLoop(Boolean.valueOf((String) mProperties.get("looping")));
                }
                if (mProperties.containsKey("loopingReversing")) {
                    mPath.setLoopReversing(Boolean.valueOf((String) mProperties.get("loopingReversing")));
                }
            }
            if (mPath != null) {
                mPath.setRevert(false);
                setMovable(true);
                InteractionEvent event = new InteractionEvent(mId, InteractionEvent.EventType.START_PATH.name(), aAction.value);
                EventDispatcher.getInstance().onInteractionEvent(event);

            }
            return true;
        }
        if (!res && aAction != null && InteractionActionType.ActionType.SET_PATH_REVERSE == aAction.type) {
            if (mPath != mMap.getPaths().get(aAction.value)) {
                mPath = mMap.getPaths().get(aAction.value);
                if (mProperties.containsKey("looping")) {
                    mPath.setLoop(Boolean.valueOf((String) mProperties.get("looping")));
                }
                if (mProperties.containsKey("loopingReversing")) {
                    mPath.setLoopReversing(Boolean.valueOf((String) mProperties.get("loopingReversing")));
                }
            }
            if (mPath != null) {
                mPath.setRevert(true);

                mPath.setCompleted(false);
                setNearestPathPoint();

                mPath.setRevert(!mPath.isRevert());
                setMovable(true);
                InteractionEvent event = new InteractionEvent(mId, InteractionEvent.EventType.START_PATH.name(), aAction.value);
                EventDispatcher.getInstance().onInteractionEvent(event);
            }
            return true;
        }

        return res;
    }

    @Override
    public void startToInteract() {
        super.startToInteract();
        if (mPath != null && mIsAutoStart) {
            setMovable(true);
        }

    }

    public boolean hasCollisionObstacle(CollisionObstacleComponent aEntity) {
        return (aEntity.mType & CollisionObstacleComponent.OBSTACLE) != 0 || (aEntity.mType & CollisionObstacleComponent.MAPINTERACTION) != 0;
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {

        boolean ret = super.onCollisionObstacleStart(aEntity, aIsPrediction);
        if (ret) {

            if (hasCollisionObstacle(aEntity)) {
                if (isMovable()) {
                    mIsInterruptedByObstacle = true;
                }
                setMovable(false);
                return true;


            }
        }
        return ret;
    }

    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {
        boolean ret = super.onCollisionObstacleStop(aEntity);
        if (ret && mCollisionsObstacle.size <= 0 && mIsInterruptedByObstacle && mPath != null) {
            mIsInterruptedByObstacle = false;
            if (!mIsStopOnObstacle) {
                setMovable(true);
            }
        }
        return ret;
    }
}