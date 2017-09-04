package com.souki.game.adventure;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.screens.GameScreen;

/**
 * Created by vincent on 01/07/2016.
 */

public class CharacterMoveController5 extends GestureDetector.GestureAdapter {
    final OrthographicCamera camera;
    final Vector3 mCursorPoint = new Vector3();
    final Vector3 last = new Vector3(-1, -1, -1);
    final Vector3 delta = new Vector3();
    Shape mPathSpot;
    int mPointer = -1;

    Vector2 mLastPoint = new Vector2();
    Vector2 mTmp1 = new Vector2();
    Vector2 mTmp2 = new Vector2();
    PathHero path;
    public boolean isActive = false;
    private GameMap mMap;

    private ComponentMapper<CollisionObstacleComponent> cm = ComponentMapper.getFor(CollisionObstacleComponent.class);

    private Entity[] entities;

    public CharacterMoveController5(OrthographicCamera camera) {
        this.camera = camera;


    }

    public PathHero getPath() {
        return path;
    }

    public void setMap(GameMap aMap) {
        mMap = aMap;

    }


    @Override
    public boolean pan(float x, float y, float deltaX, float deltaY) {
        if (!isActive)
            return false;
        camera.unproject(mCursorPoint.set(x, y, 0));
        //   Gdx.app.debug("DEBUG", "segment (last, mCursorPoint)=[(" + last.x + "," + last.y + ")" + "(" + mCursorPoint.x + "," + mCursorPoint.y + ")]");

        if (mMap != null && !(last.x == -1 && last.y == -1 && last.z == -1) && !(last.x == mCursorPoint.x && last.y == mCursorPoint.y)) {
            delta.set(mCursorPoint);
            delta.sub(last);

            if (delta.x * delta.x + delta.y * delta.y < PathHero.CHECK_RADIUS)
                return true;
//            Gdx.app.debug("DEBUG", "delta=(" + delta.x + "," + delta.y + ")");

            //si delta.y > 0,5 touchDragged par dichotomie sur le segment delta


            last.set(mCursorPoint);

            Vector2 posFinale = new Vector2();
            Vector2 posTarget = new Vector2(mPathSpot.getX() + delta.x, mPathSpot.getY() + delta.y);
            if (ShapeUtils.checkMove(entities, mPathSpot, posTarget, null, posFinale, 5)) {
                mPathSpot.setX(posFinale.x);
                mPathSpot.setY(posFinale.y);
                mLastPoint.set(posFinale.x, posFinale.y);
                path.addPoint(mLastPoint.x, mLastPoint.y);

            }


        }
        return true;
    }


    public boolean tap(float x, float y, int count, int button) {
        camera.unproject(mCursorPoint.set(x, y, 0));

        if (mMap != null && mMap.getPlayer() != null && mMap.getPlayer().getHero() != null && mMap.getPlayer().getHero().getShapeRendering().getBounds().contains(mCursorPoint.x, mCursorPoint.y)) {
            if (path != null)
                path.destroy();
            path = new PathHero();
            mMap.getPlayer().getHero().setPath(null);
            isActive = false;
            ((GameScreen) MyGame.getInstance().getScreenType(MyGame.ScreenType.MainGame)).setSpotShape(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {


        if (mPointer != -1 && mPointer != pointer) {
            return false;
        }
        mPointer = pointer;
        entities = EntityEngine.getInstance().getEntitiesFor(Family.all(CollisionObstacleComponent.class).get()).toArray(Entity.class);

        camera.unproject(mCursorPoint.set(x, y, 0));

        if (mMap != null && mMap.getPlayer() != null && mMap.getPlayer().getHero() != null && mMap.getPlayer().getHero().getShapeRendering().getBounds().contains(mCursorPoint.x, mCursorPoint.y)) {
            if (path != null)
                path.destroy();
            path = new PathHero(mMap.getPlayer().getHero().getShapeRendering().getWidth() / 2);
            mMap.getPlayer().getHero().setPath(null);
            Vector2 bobPos = mMap.getPlayer().getHero().getPosition();
            //   float heroShapeHalfWidth = mMap.getPlayer().getHero().getShapeRendering().getWidth() / 2;
            path.addPoint(bobPos.x /*+ heroShapeHalfWidth*/, bobPos.y);
            mPathSpot = mMap.getPlayer().getHero().getShapeCollision().clone();
            mPathSpot.setX(bobPos.x);
            mPathSpot.setY(bobPos.y);
            ((GameScreen) MyGame.getInstance().getScreenType(MyGame.ScreenType.MainGame)).setSpotShape(mPathSpot);
            last.set(mCursorPoint);
            mLastPoint.set(bobPos.x /*+ heroShapeHalfWidth*/, bobPos.y);
            isActive = true;
        }
        return false;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        if (mPointer != pointer)
            return false;
        last.set(-1, -1, -1);
        if (mMap != null && mMap.getPlayer() != null && mMap.getPlayer().getHero() != null && isActive) {
            isActive = false;
            mMap.getPlayer().getHero().setPath(path);
            path = null;
            ((GameScreen) MyGame.getInstance().getScreenType(MyGame.ScreenType.MainGame)).setSpotShape(null);
        }
        return false;
    }


}