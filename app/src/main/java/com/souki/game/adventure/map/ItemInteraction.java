package com.souki.game.adventure.map;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.box2d.RectangleShape;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.ICollisionObstacleHandler;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VisualComponent;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;

/**
 * Created by gwalarn on 27/11/16.
 */

public class ItemInteraction extends Entity implements IItemInteraction, IMapRendable, ICollisionObstacleHandler {

    protected IItemInteraction.Type mType;
    protected float mX, mY;



    protected String mId;
    protected Item mItem;
    protected RectangleShape mShape;
    protected boolean mIsRended = false;
    private Array<CollisionObstacleComponent> mCollisions = new Array<CollisionObstacleComponent>();
    private  GameMap mMap;


    public ItemInteraction(float aX, float aY, String aId, GameMap aMap) {
        mX = aX;
        mY = aY;
        mType=Type.ITEM;
        mMap = aMap;
        mId = aId;
        mItem = ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(aId));
        EntityEngine.getInstance().addEntity(this);
        mShape = new RectangleShape();
        mShape.setShape(new Rectangle(getX(), getY(), mItem.getTextureRegion().getRegionWidth() * MyGame.SCALE_FACTOR, mItem.getTextureRegion().getRegionHeight() * MyGame.SCALE_FACTOR));
        add(new CollisionObstacleComponent(CollisionObstacleComponent.ITEM, mShape, aId, this, this));
        add(new VisualComponent(mItem.getTextureRegion(), this));
    }
    @Override
    public float getX() {
        return mX;
    }

    @Override
    public float getY() {
        return mY;
    }

    @Override
    public IItemInteraction.Type getInteractionType() {
        return mType;
    }
    public String getId() {
        return mId;
    }

    public RectangleShape getShape() {
        return mShape;
    }

    public void render(Batch batch) {
        float width = mItem.getTextureRegion().getRegionWidth();
        float height = mItem.getTextureRegion().getRegionHeight();
        float halfWidth = width / 2f;
        float halfHeight = height / 2f;
        //Allow for Offset
        float originX = 0;//transform.originOffset.x;
        float originY = 0;//transform.originOffset.y;

        batch.draw(mItem.getTextureRegion(),
                getX(), getY(),
                originX, originY,
                width, height,
                MyGame.SCALE_FACTOR, MyGame.SCALE_FACTOR,
                0);
    }

    @Override
    public Shape getShapeRendering() {
        return mShape;
    }

    public Item getItem() {
        return mItem;
    }
    @Override
    public boolean isRendable()
    {
        return true;
    }
    @Override
    public boolean isRended()
    {
        return mIsRended;
    }
    @Override
    public void setRended(boolean aRended)
    {
        mIsRended = aRended;
    }
    @Override
    public int getZIndex()
    {
        return 1;
    }
    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity) {
        if ((aEntity.mType & CollisionObstacleComponent.HERO) !=0) {
            mMap.removeItem(this);
            EntityEngine.getInstance().removeEntity(this);
            return true;
        }
        return false;
    }

    @Override
    public void destroy()
    {
        EntityEngine.getInstance().removeEntity(this);
    }

    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {
        return false;
    }

    @Override
    public Array<CollisionObstacleComponent> getCollisionObstacle() {
        return mCollisions;
    }
}
