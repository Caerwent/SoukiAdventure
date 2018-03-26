package com.souki.game.adventure.box2d;

import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.interactions.InteractionEvent;

import java.util.ArrayList;

/**
 * Created by vincent on 03/02/2017.
 */

public class PathMap {
    protected ArrayList<Vector2> positions;
    protected Vector2 mVelocity = new Vector2();
    protected int currentPointIndex;
    protected int nextPointIndex;
    protected boolean mIsCompleted = false;
    protected String mId;

    protected boolean isRevert = false;
    protected boolean isLoop = false;
    protected boolean isLoopReversing = false;
    protected float lastTime = 0;
    static public final float CHECK_RADIUS = 0.05f;
    public float mVelocityCte = 2;

    public PathMap(String aId) {
        mId = aId;
        positions = new ArrayList<Vector2>();
    }

    public void addPoint(float x, float y) {
        positions.add(new Vector2(x, y));
    }

    public void reset() {
        currentPointIndex = 0;
        nextPointIndex = getNextPoint();
        mIsCompleted = false;
    }

    public void setVelocityCte(float aVelocity) {
        mVelocityCte = aVelocity;
    }

    public boolean isLoop() {
        return isLoop;
    }

    public void setLoop(boolean loop) {
        isLoop = loop;
    }

    public boolean isLoopReversing() {
        return isLoopReversing;
    }

    public void setLoopReversing(boolean loop) {
        isLoopReversing = loop;
    }

    public boolean isRevert() {
        return isRevert;
    }

    public void setRevert(boolean revert) {
        isRevert = revert;
    }

    public boolean isCompleted() {
        return mIsCompleted;
    }

    public void setCompleted(boolean aIsCompleted) {
        this.mIsCompleted = aIsCompleted;
    }

    public Vector2 getCurrentPoint() {
        return positions.get(currentPointIndex);
    }


    public Vector2 getVelocityForPosAndTime(Vector2 bodyPosition, float dT, String aSourceId) {
        lastTime += dT;
        Vector2 nextPointPosition = positions.get(nextPointIndex);
        float d = nextPointPosition.dst2(bodyPosition);
        boolean hasLooped = false;
        if (d < CHECK_RADIUS) {
            currentPointIndex = nextPointIndex;
            lastTime = 0;
            if (hasNextPoint()) {
                if (hasFinishPath())
                {
                    InteractionEvent event = new InteractionEvent(aSourceId, InteractionEvent.EventType.END_PATH.name(), mId);
                    EventDispatcher.getInstance().onInteractionEvent(event);
                    hasLooped=true;
                }
                nextPointIndex = getNextPoint();
                mIsCompleted = false;
            } else {
                mIsCompleted = true;
                mVelocity.set(0, 0);
                InteractionEvent event = new InteractionEvent(aSourceId, InteractionEvent.EventType.END_PATH.name(), mId);
                EventDispatcher.getInstance().onInteractionEvent(event);
            }

        }
        if (!mIsCompleted) {
            computeVelocity(bodyPosition, dT);
            if(hasLooped)
            {
                InteractionEvent event = new InteractionEvent(aSourceId, InteractionEvent.EventType.START_PATH.name(), mId);
                EventDispatcher.getInstance().onInteractionEvent(event);
            }
        }
        return mVelocity;
    }

    protected boolean hasFinishPath()
    {
        if (isRevert) {
            return currentPointIndex <= 0;
        } else {
            return currentPointIndex >= positions.size() - 1;
        }
    }
    public boolean hasNextPoint() {
        if (isLoop || isLoopReversing)
            return true;

        if (isRevert) {
            return currentPointIndex > 0;
        } else {
            return currentPointIndex < positions.size() - 1;
        }


    }

    public void setNearestPoint(Vector2 bodyPosition)
    {
        int nearestIndex=-1;
        float minD = Float.MAX_VALUE;
        for(int i=0; i<positions.size(); i++)
        {
            Vector2 nextPointPosition = positions.get(i);
            float d = nextPointPosition.dst2(bodyPosition);
            if(d<minD)
            {
                nearestIndex = i;
                minD=d;
            }
        }
        if(nearestIndex>=0)
        {
            currentPointIndex=nearestIndex;
            nextPointIndex=nearestIndex;
        }

    }

    int getNextPoint() {
        if (isRevert) {
            if (currentPointIndex <= 0) {
                if (isLoop)
                    return positions.size() - 1;
                else if (isLoopReversing) {
                    isRevert = false;
                    return currentPointIndex + 1;
                } else return -1;
            } else {
                return currentPointIndex - 1;
            }
        } else {
            if (currentPointIndex < positions.size() - 1) {
                return currentPointIndex + 1;
            } else {
                if (isLoop)
                    return 0;
                else if (isLoopReversing) {
                    isRevert = true;
                    return positions.size() - 1;
                } else
                    return -1;
            }

        }

    }

    void computeVelocity(Vector2 aCurrentPosition, float dT) {

        Vector2 nextPosition = positions.get(nextPointIndex);
        double dx = nextPosition.x - aCurrentPosition.x;
        double dy = nextPosition.y - aCurrentPosition.y;

        computeVelocityForDisplacement(mVelocity, dx, dy, mVelocityCte);


    }

    public static void computeVelocityForDisplacement(Vector2 aVelocity, double dx, double dy, float aVelocityCte) {
        double D = Math.sqrt(dx * dx + dy * dy);

        if (D == 0) {
            aVelocity.set(0, 0);
        } else {
            double angle = Math.acos(dx / D);
            angle = angle * (dy < 0 ? -1d : 1d);

            double vx = Math.cos(angle) * aVelocityCte;
            double vy = Math.sin(angle) * aVelocityCte;

            aVelocity.set((float) vx, (float) vy);
        }
    }
}
