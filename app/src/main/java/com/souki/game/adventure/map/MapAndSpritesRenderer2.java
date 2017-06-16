package com.souki.game.adventure.map;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.box2d.CircleShape;
import com.souki.game.adventure.box2d.PolygonShape;
import com.souki.game.adventure.box2d.RectangleShape;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.VisualComponent;
import com.souki.game.adventure.screens.GameScreen;

import java.util.Arrays;
import java.util.Comparator;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;

/**
 * Created by vincent on 06/07/2016.
 */

public class MapAndSpritesRenderer2 extends OrthogonalTiledMapRenderer {

    TiledMapTileLayer mDefaultlayer;
    //initiate shapeRenderer. Can remove later
    ShapeRenderer shapeRenderer = new ShapeRenderer();

    ShapeRenderer collisionRenderer = new ShapeRenderer();

    GameMap mMap;
    private ComponentMapper<VisualComponent> vm = ComponentMapper.getFor(VisualComponent.class);
    private ComponentMapper<CollisionObstacleComponent> cm = ComponentMapper.getFor(CollisionObstacleComponent.class);
    private ComponentMapper<CollisionInteractionComponent> im = ComponentMapper.getFor(CollisionInteractionComponent.class);
    private ComponentMapper<CollisionEffectComponent> em = ComponentMapper.getFor(CollisionEffectComponent.class);

    private IMapRendable[] mMapRendables;

    private PolygonShape mTileRegionShape = new PolygonShape();
    private Polygon mTileRegionPoly = new Polygon();

    public MapAndSpritesRenderer2(GameMap map) {
        super(map.getTiledMap());
        mMap = map;
        init();
    }

    public MapAndSpritesRenderer2(GameMap map, Batch batch) {
        super(map.getTiledMap(), batch);
        mMap = map;
        init();
    }

    public MapAndSpritesRenderer2(GameMap map, float unitScale) {
        super(map.getTiledMap(), unitScale);
        mMap = map;
        init();
    }

    public MapAndSpritesRenderer2(GameMap map, float unitScale, Batch batch) {
        super(map.getTiledMap(), unitScale, batch);
        mMap = map;
        init();
    }

    private void init() {
        mDefaultlayer = null;
        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                mDefaultlayer = (TiledMapTileLayer) layer;
                break;
            }
        }
        shapeRenderer.setColor(Color.RED);
        collisionRenderer.setColor(Color.BLUE);
        collisionRenderer.setAutoShapeType(true);
        // shapeRenderer.setAutoShapeType(true);
    }

    protected void renderLayerByLayer(int aStartIdxLayer) {
        final Color batchColor = batch.getColor();
        final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * mDefaultlayer.getOpacity());


        // check if  a zindex object overlaps the tile and if  a rendable should be drawn before above or below zindex object

        for (int i = aStartIdxLayer; i < map.getLayers().getCount(); i++) {
            MapLayer layer = map.getLayers().get(i);
            if (layer.isVisible()) {
                Array<Shape> zindexList = mMap.getBodiesZindex(layer.getName());
                if (layer instanceof TiledMapTileLayer) {
//                            Gdx.app.debug("DEBUG", "------------------------- layer " + layer.getName());
                    int layerWidth = mDefaultlayer.getWidth();
                    int layerHeight = mDefaultlayer.getHeight();

                    float layerTileWidth = mDefaultlayer.getTileWidth() * unitScale;
                    float layerTileHeight = mDefaultlayer.getTileHeight() * unitScale;

                    int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
                    int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

                    int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
                    int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

                    float y = row2 * layerTileHeight;
                    float xStart = col1 * layerTileWidth;
                    float[] vertices = this.vertices;


                    for (int row = row2; row >= row1; row--) {
                        float x = xStart;
                        for (int col = col1; col < col2; col++) {


                            final TiledMapTileLayer.Cell cell = ((TiledMapTileLayer) layer).getCell(col, row);
                            if (cell == null) {
                                x += layerTileWidth;
                                continue;
                            }
                            final TiledMapTile tile = cell.getTile();

                            if (tile != null) {
//                                        Gdx.app.debug("DEBUG", "tile row=" + row+" col="+col);
                                final boolean flipX = cell.getFlipHorizontally();
                                final boolean flipY = cell.getFlipVertically();
                                final int rotations = cell.getRotation();

                                TextureRegion region = tile.getTextureRegion();

                                float x1 = x + tile.getOffsetX() * unitScale;
                                float y1 = y + tile.getOffsetY() * unitScale;
                                float x2 = x1 + region.getRegionWidth() * unitScale;
                                float y2 = y1 + region.getRegionHeight() * unitScale;

                                float u1 = region.getU();
                                float v1 = region.getV2();
                                float u2 = region.getU2();
                                float v2 = region.getV();

                                vertices[X1] = x1;
                                vertices[Y1] = y1;
                                vertices[C1] = color;
                                vertices[U1] = u1;
                                vertices[V1] = v1;

                                vertices[X2] = x1;
                                vertices[Y2] = y2;
                                vertices[C2] = color;
                                vertices[U2] = u1;
                                vertices[V2] = v2;

                                vertices[X3] = x2;
                                vertices[Y3] = y2;
                                vertices[C3] = color;
                                vertices[U3] = u2;
                                vertices[V3] = v2;

                                vertices[X4] = x2;
                                vertices[Y4] = y1;
                                vertices[C4] = color;
                                vertices[U4] = u2;
                                vertices[V4] = v1;

                                if (flipX) {
                                    float temp = vertices[U1];
                                    vertices[U1] = vertices[U3];
                                    vertices[U3] = temp;
                                    temp = vertices[U2];
                                    vertices[U2] = vertices[U4];
                                    vertices[U4] = temp;
                                }
                                if (flipY) {
                                    float temp = vertices[V1];
                                    vertices[V1] = vertices[V3];
                                    vertices[V3] = temp;
                                    temp = vertices[V2];
                                    vertices[V2] = vertices[V4];
                                    vertices[V4] = temp;
                                }
                                if (rotations != 0) {
                                    switch (rotations) {
                                        case TiledMapTileLayer.Cell.ROTATE_90: {
                                            float tempV = vertices[V1];
                                            vertices[V1] = vertices[V2];
                                            vertices[V2] = vertices[V3];
                                            vertices[V3] = vertices[V4];
                                            vertices[V4] = tempV;

                                            float tempU = vertices[U1];
                                            vertices[U1] = vertices[U2];
                                            vertices[U2] = vertices[U3];
                                            vertices[U3] = vertices[U4];
                                            vertices[U4] = tempU;
                                            break;
                                        }
                                        case TiledMapTileLayer.Cell.ROTATE_180: {
                                            float tempU = vertices[U1];
                                            vertices[U1] = vertices[U3];
                                            vertices[U3] = tempU;
                                            tempU = vertices[U2];
                                            vertices[U2] = vertices[U4];
                                            vertices[U4] = tempU;
                                            float tempV = vertices[V1];
                                            vertices[V1] = vertices[V3];
                                            vertices[V3] = tempV;
                                            tempV = vertices[V2];
                                            vertices[V2] = vertices[V4];
                                            vertices[V4] = tempV;
                                            break;
                                        }
                                        case TiledMapTileLayer.Cell.ROTATE_270: {
                                            float tempV = vertices[V1];
                                            vertices[V1] = vertices[V4];
                                            vertices[V4] = vertices[V3];
                                            vertices[V3] = vertices[V2];
                                            vertices[V2] = tempV;

                                            float tempU = vertices[U1];
                                            vertices[U1] = vertices[U4];
                                            vertices[U4] = vertices[U3];
                                            vertices[U3] = vertices[U2];
                                            vertices[U2] = tempU;
                                            break;
                                        }
                                    }
                                }
                                float[] tmpTileRegionPoly = new float[]{vertices[X1], vertices[Y1], vertices[X2], vertices[Y2], vertices[X3], vertices[Y3], vertices[X4], vertices[Y4]};
                                mTileRegionPoly.setPosition(0, 0);
                                mTileRegionPoly.setVertices(tmpTileRegionPoly);
                                mTileRegionShape.setShape(mTileRegionPoly);

                                boolean renderShape = checkTileOverlapsRendables(mTileRegionShape, zindexList, row, col);

                                batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
//                                if (renderShape) {
//                                    shapeRenderer.setColor(Color.CYAN);
//                                    shapeRenderer.polyline(new float[]{vertices[X1], vertices[Y1], vertices[X3], vertices[Y3]});
//                                    shapeRenderer.polyline(new float[]{vertices[X2], vertices[Y2], vertices[X4], vertices[Y4]});
//                                }
                            }
                            x += layerTileWidth;
                        }
                        y -= layerTileHeight;
                    }
                } else if (layer instanceof TiledMapImageLayer) {
                    TextureRegion region = ((TiledMapImageLayer) layer).getTextureRegion();

                    if (region == null) {
                        return;
                    }

                    final float xLayer = ((TiledMapImageLayer) layer).getX();
                    final float yLayer = ((TiledMapImageLayer) layer).getY();
                    final float x1 = xLayer * unitScale;
                    final float y1 = yLayer * unitScale;
                    final float x2 = x1 + region.getRegionWidth() * unitScale;
                    final float y2 = y1 + region.getRegionHeight() * unitScale;
                    float[] tmpTileRegionPoly = new float[]{x1, y1, x1, y2, x2, y2, x2, y1};
                    mTileRegionPoly.setPosition(0, 0);
                    mTileRegionPoly.setVertices(tmpTileRegionPoly);
                    mTileRegionShape.setShape(mTileRegionPoly);

                    boolean renderShape = checkTileOverlapsRendables(mTileRegionShape, zindexList, 0, 0);

                    renderImageLayer((TiledMapImageLayer) layer);
                } else {
                    renderObjects(layer);
                }
            }

        }

        renderRemainingObjects(mMapRendables);

    }

    protected void renderCellByCell(int aStartIdxLayer) {


        final Color batchColor = batch.getColor();
        final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * mDefaultlayer.getOpacity());

        final int layerWidth = mDefaultlayer.getWidth();
        final int layerHeight = mDefaultlayer.getHeight();

        final float layerTileWidth = mDefaultlayer.getTileWidth() * unitScale;
        final float layerTileHeight = mDefaultlayer.getTileHeight() * unitScale;

        final int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
        final int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));

        final int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
        final int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

        float y = row2 * layerTileHeight;
        float xStart = col1 * layerTileWidth;
        final float[] vertices = this.vertices;


        //      Gdx.app.debug("DEBUG", "************************************************************");


        for (int row = row2; row >= row1; row--) {
            float x = xStart;
            for (int col = col1; col < col2; col++) {
                // check if  a zindex object overlaps the tile and if  a rendable should be drawn before above or below zindex object

                for (int i = aStartIdxLayer; i < map.getLayers().getCount(); i++) {
                    MapLayer layer = map.getLayers().get(i);
                    if (layer.isVisible()) {
                        Array<Shape> zindexList = mMap.getBodiesZindex(layer.getName());
                        if (layer instanceof TiledMapTileLayer) {


                            final TiledMapTileLayer.Cell cell = ((TiledMapTileLayer) layer).getCell(col, row);
                            if (cell == null) {
                                //x += layerTileWidth;
                                continue;
                            }
                            final TiledMapTile tile = cell.getTile();

                            if (tile != null) {
                                final boolean flipX = cell.getFlipHorizontally();
                                final boolean flipY = cell.getFlipVertically();
                                final int rotations = cell.getRotation();

                                TextureRegion region = tile.getTextureRegion();

                                float x1 = x + tile.getOffsetX() * unitScale;
                                float y1 = y + tile.getOffsetY() * unitScale;
                                float x2 = x1 + region.getRegionWidth() * unitScale;
                                float y2 = y1 + region.getRegionHeight() * unitScale;

                                float u1 = region.getU();
                                float v1 = region.getV2();
                                float u2 = region.getU2();
                                float v2 = region.getV();

                                vertices[X1] = x1;
                                vertices[Y1] = y1;
                                vertices[C1] = color;
                                vertices[U1] = u1;
                                vertices[V1] = v1;

                                vertices[X2] = x1;
                                vertices[Y2] = y2;
                                vertices[C2] = color;
                                vertices[U2] = u1;
                                vertices[V2] = v2;

                                vertices[X3] = x2;
                                vertices[Y3] = y2;
                                vertices[C3] = color;
                                vertices[U3] = u2;
                                vertices[V3] = v2;

                                vertices[X4] = x2;
                                vertices[Y4] = y1;
                                vertices[C4] = color;
                                vertices[U4] = u2;
                                vertices[V4] = v1;

                                if (flipX) {
                                    float temp = vertices[U1];
                                    vertices[U1] = vertices[U3];
                                    vertices[U3] = temp;
                                    temp = vertices[U2];
                                    vertices[U2] = vertices[U4];
                                    vertices[U4] = temp;
                                }
                                if (flipY) {
                                    float temp = vertices[V1];
                                    vertices[V1] = vertices[V3];
                                    vertices[V3] = temp;
                                    temp = vertices[V2];
                                    vertices[V2] = vertices[V4];
                                    vertices[V4] = temp;
                                }
                                if (rotations != 0) {
                                    switch (rotations) {
                                        case TiledMapTileLayer.Cell.ROTATE_90: {
                                            float tempV = vertices[V1];
                                            vertices[V1] = vertices[V2];
                                            vertices[V2] = vertices[V3];
                                            vertices[V3] = vertices[V4];
                                            vertices[V4] = tempV;

                                            float tempU = vertices[U1];
                                            vertices[U1] = vertices[U2];
                                            vertices[U2] = vertices[U3];
                                            vertices[U3] = vertices[U4];
                                            vertices[U4] = tempU;
                                            break;
                                        }
                                        case TiledMapTileLayer.Cell.ROTATE_180: {
                                            float tempU = vertices[U1];
                                            vertices[U1] = vertices[U3];
                                            vertices[U3] = tempU;
                                            tempU = vertices[U2];
                                            vertices[U2] = vertices[U4];
                                            vertices[U4] = tempU;
                                            float tempV = vertices[V1];
                                            vertices[V1] = vertices[V3];
                                            vertices[V3] = tempV;
                                            tempV = vertices[V2];
                                            vertices[V2] = vertices[V4];
                                            vertices[V4] = tempV;
                                            break;
                                        }
                                        case TiledMapTileLayer.Cell.ROTATE_270: {
                                            float tempV = vertices[V1];
                                            vertices[V1] = vertices[V4];
                                            vertices[V4] = vertices[V3];
                                            vertices[V3] = vertices[V2];
                                            vertices[V2] = tempV;

                                            float tempU = vertices[U1];
                                            vertices[U1] = vertices[U4];
                                            vertices[U4] = vertices[U3];
                                            vertices[U3] = vertices[U2];
                                            vertices[U2] = tempU;
                                            break;
                                        }
                                    }
                                }
                                float[] tmpTileRegionPoly = new float[]{vertices[X1], vertices[Y1], vertices[X2], vertices[Y2], vertices[X3], vertices[Y3], vertices[X4], vertices[Y4]};
                                mTileRegionPoly.setPosition(0, 0);
                                mTileRegionPoly.setVertices(tmpTileRegionPoly);
                                mTileRegionShape.setShape(mTileRegionPoly);

                                boolean renderShape = checkTileOverlapsRendables(mTileRegionShape, zindexList, row, col);

                                batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
//                                if (renderShape) {
//                                    shapeRenderer.setColor(Color.CYAN);
//                                    shapeRenderer.polyline(new float[]{vertices[X1], vertices[Y1], vertices[X3], vertices[Y3]});
//                                    shapeRenderer.polyline(new float[]{vertices[X2], vertices[Y2], vertices[X4], vertices[Y4]});
//                                }
                            }
                        } else if (layer instanceof TiledMapImageLayer) {
                            TextureRegion region = ((TiledMapImageLayer) layer).getTextureRegion();

                            if (region == null) {
                                return;
                            }

                            final float xLayer = ((TiledMapImageLayer) layer).getX();
                            final float yLayer = ((TiledMapImageLayer) layer).getY();
                            final float x1 = xLayer * unitScale;
                            final float y1 = yLayer * unitScale;
                            final float x2 = x1 + region.getRegionWidth() * unitScale;
                            final float y2 = y1 + region.getRegionHeight() * unitScale;
                            float[] tmpTileRegionPoly = new float[]{x1, y1, x1, y2, x2, y2, x2, y1};
                            mTileRegionPoly.setPosition(0, 0);
                            mTileRegionPoly.setVertices(tmpTileRegionPoly);
                            mTileRegionShape.setShape(mTileRegionPoly);
                            boolean renderShape = checkTileOverlapsRendables(mTileRegionShape, zindexList, row, col);

                            renderImageLayer((TiledMapImageLayer) layer);
                        } else {
                            renderObjects(layer);
                        }
                    }
                }

                x += layerTileWidth;
            }
            y -= layerTileHeight;
        }

        renderRemainingObjects(mMapRendables);


    }

    @Override
    public void render() {

        ImmutableArray<Entity> entities = EntityEngine.getInstance().getEntitiesFor(Family.all(VisualComponent.class).get());
        mMapRendables = new IMapRendable[entities.size()];
        for (int i = 0; i < entities.size(); ++i) {
            Entity e = entities.get(i);
            mMapRendables[i] = vm.get(e).rendable;
            mMapRendables[i].setRended(false);
        }
        Arrays.sort(mMapRendables, new Comparator<IMapRendable>() {
            @Override
            public int compare(IMapRendable lhs, IMapRendable rhs) {
                float lhsY = lhs.getShapeRendering().getBounds().getY();
                float rhsY = rhs.getShapeRendering().getBounds().getY();
                int lhsZIndex = lhs.getZIndex();
                int rhsZIndex = rhs.getZIndex();
                if (lhsZIndex != rhsZIndex) {
                    return rhsZIndex < lhsZIndex ? 1 : -1;
                }
                if (lhsY == rhsY) {
                    return 0;
                } else {
                    return lhsY < rhsY ? 1 : -1;
                }

            }
        });


        beginRender();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        int startLayerIdx = 0;
        for (startLayerIdx = 0; startLayerIdx < map.getLayers().getCount(); startLayerIdx++) {
            MapLayer layer = map.getLayers().get(startLayerIdx);
            if (layer instanceof TiledMapTileLayer && layer.getName().startsWith("ground")) {
                super.renderTileLayer((TiledMapTileLayer) layer);
            } else {
                break;
            }
        }

        if (mMap.getZindexCount() > 1) {
            renderLayerByLayer(startLayerIdx);
        } else {
            renderCellByCell(startLayerIdx);
        }

        endRender();
        shapeRenderer.end();
//        renderInteractionShapes();
//        renderShapes(mMapRendables);
//        renderCollisionShapes();

    }

    private boolean checkTileOverlapsRendables(PolygonShape aTileShape, Array<Shape> aZindexList, int row, int col) {
        boolean renderShape = false;
        boolean debug = false;
        for (int idx = 0; idx < mMapRendables.length; idx++) {
            IMapRendable rendable = mMapRendables[idx];
            if (rendable.isRendable() && !rendable.isRended()) {
                if (ShapeUtils.overlaps(rendable.getShapeRendering(), aTileShape)) {

//                    if(rendable instanceof InteractionHero)
//                    {
//                        debug=true;
//                        Gdx.app.debug("DEBUG", "hero overlaps tile row=" +row+" col="+col);
//                    }
                    for (int i = 0; i < aZindexList.size; i++) {
                        Shape currentZIndex = aZindexList.get(i);
//                        Gdx.app.debug("DEBUG", "check zindex " + ShapeUtils.logShape(currentZIndex));

                        if (ShapeUtils.overlaps(rendable.getShapeRendering(), currentZIndex) && ShapeUtils.overlaps(aTileShape, currentZIndex)) {


                            float Yzindex = currentZIndex.getYAtX(rendable.getShapeRendering().getX());
                            float Ytmp = currentZIndex.getYAtX(rendable.getShapeRendering().getX()+rendable.getShapeRendering().getWidth());
                            if (Yzindex == -1 || (Ytmp != -1 && Ytmp < Yzindex)) {
                                Yzindex = Ytmp;
                            }
                            Ytmp = currentZIndex.getYAtX(rendable.getShapeRendering().getX()+rendable.getShapeRendering().getWidth()/ 2);
                            if (Yzindex == -1 || (Ytmp != -1 && Ytmp < Yzindex)) {
                                Yzindex = Ytmp;
                            }
                            if (Yzindex == -1) {
                                Yzindex = currentZIndex.getBounds().getY();
                            }
                            float Yrendable = rendable.getShapeRendering().getYAtX(rendable.getShapeRendering().getX());
                            Ytmp = rendable.getShapeRendering().getYAtX(rendable.getShapeRendering().getX()+rendable.getShapeRendering().getWidth());
                            if (Yrendable == -1 || (Ytmp != -1 && Ytmp < Yrendable)) {
                                Yrendable = Ytmp;
                            }
                            Ytmp = rendable.getShapeRendering().getYAtX(rendable.getShapeRendering().getX()+rendable.getShapeRendering().getWidth()/ 2);
                            if (Yrendable == -1 || (Ytmp != -1 && Ytmp < Yrendable)) {
                                Yrendable = Ytmp;
                            }
                            if (Yrendable == -1) {
                                Yrendable = rendable.getShapeRendering().getBounds().getY();
                            }
//                            if(debug)
//                            {
//                                Gdx.app.debug("DEBUG", "zindex overlaps " +ShapeUtils.logShape(currentZIndex)+ " x=" + aX + " zindexYatX=" + Yzindex + " heroYAtX=" + Yrendable);
//                            }

                            if (Yzindex < Yrendable  && rendable.getZIndex()<=1) {
//                                shapeRenderer.setColor(Color.GOLD);
//                                shapeRenderer.polygon(aTileShape.getShape().getTransformedVertices());
//                                shapeRenderer.setColor(Color.RED);
//                                if (rendable.getShapeRendering() instanceof PolygonShape) {
//                                    shapeRenderer.polygon(((PolygonShape) rendable.getShapeRendering()).getShape().getTransformedVertices());
//                                } else if (rendable.getShapeRendering() instanceof RectangleShape) {
//                                    Rectangle rect = ((RectangleShape) rendable.getShapeRendering()).getShape();
//                                    shapeRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
//                                }
//                                shapeRenderer.setColor(Color.GREEN);
//                                if (currentZIndex instanceof PolygonShape) {
//                                    shapeRenderer.polygon(((PolygonShape) currentZIndex).getShape().getTransformedVertices());
//                                } else if (currentZIndex instanceof RectangleShape) {
//                                    Rectangle rect = ((RectangleShape) currentZIndex).getShape();
//                                    shapeRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
//                                }
//                                renderShape = true;
                                // need to draw all overlaping entities with < Y
                                drawPreviousOverlapingEntity(rendable, idx, mMapRendables);
                                rendable.render(getBatch());
                                rendable.setRended(true);
                            }
                        }
                    }
                }
            }

        }
        return renderShape;
    }

    private void drawPreviousOverlapingEntity(IMapRendable rendable, int idx, IMapRendable[] sortedMapRendables) {
        if (idx <= 0)
            return;

        IMapRendable prevRendable = sortedMapRendables[idx - 1];
        if (prevRendable.isRendable() && !prevRendable.isRended()) {
            //     Gdx.app.debug("DEBUG", "check entity "+entity.getClass().getSimpleName()+" "+ShapeUtils.logShape(rendable.getShape()));
            if (ShapeUtils.overlaps(rendable.getShapeRendering(), prevRendable.getShapeRendering())) {
                //     Gdx.app.debug("DEBUG", "entity overlaps tile");
                if (rendable.getShapeRendering().getBounds().getY() < prevRendable.getShapeRendering().getBounds().getY() || rendable.getZIndex() > prevRendable.getZIndex()) {
                    drawPreviousOverlapingEntity(prevRendable, idx - 1, sortedMapRendables);
                    prevRendable.render(getBatch());
                    prevRendable.setRended(true);
                }
            }
        } else {
            drawPreviousOverlapingEntity(rendable, idx - 1, sortedMapRendables);
        }


    }

    private void renderRemainingObjects(IMapRendable[] sortedMapRendables) {

        for (IMapRendable rendable : sortedMapRendables) {
            if (rendable.isRendable() && !rendable.isRended()) {
                rendable.render(batch);
                rendable.setRended(true);
            }
        }

    }

    @Override
    public void setView(OrthographicCamera camera) {
        super.setView(camera);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }

    private void renderShapes(IMapRendable[] sortedMapRendables) {

        shapeRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (MapLayer layer : map.getLayers()) {
            Array<Shape> bodies = mMap.getBodiesZindex(layer.getName());
            for (Shape body : bodies) {
                if (body.getType() == Shape.Type.POLYGON) {
                    shapeRenderer.polygon(((PolygonShape) body).getShape().getTransformedVertices());
                }
            }
        }


        for (IMapRendable rendable : sortedMapRendables) {
            if (rendable.isRendable() && rendable.isRended()) {
                if (rendable.getShapeRendering() instanceof PolygonShape) {
                    shapeRenderer.polygon(((PolygonShape) rendable.getShapeRendering()).getShape().getTransformedVertices());
                } else if (rendable.getShapeRendering() instanceof RectangleShape) {
                    Rectangle rect = ((RectangleShape) rendable.getShapeRendering()).getShape();
                    shapeRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
                } else if (rendable.getShapeRendering() instanceof CircleShape) {
                    Circle circle = ((CircleShape) rendable.getShapeRendering()).getShape();
                    shapeRenderer.circle(circle.x, circle.y, circle.radius, 16);
                }
            }
        }
        shapeRenderer.end();
    }

    private void renderCollisionShapes() {

        collisionRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        collisionRenderer.begin(ShapeRenderer.ShapeType.Line);

        Array<Shape> mapCollision = mMap.getBodiesCollision();


        ImmutableArray<Entity> entities = EntityEngine.getInstance().getEntitiesFor(Family.all(CollisionObstacleComponent.class).get());
        for (Entity entity : entities) {

            CollisionObstacleComponent col = cm.get(entity);

            if (mapCollision.contains(col.mShape, true)) {
                collisionRenderer.setColor(Color.GREEN);
            } else {
                collisionRenderer.setColor(Color.BLUE);
            }

            if (col.mShape.getType() == Shape.Type.POLYGON) {
                collisionRenderer.polygon(((PolygonShape) col.mShape).getShape().getTransformedVertices());
            } else if (col.mShape instanceof RectangleShape) {
                Rectangle rect = ((RectangleShape) col.mShape).getShape();
                collisionRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
            } else if (col.mShape instanceof CircleShape) {
                Circle circle = ((CircleShape) col.mShape).getShape();
                collisionRenderer.circle(circle.x, circle.y, circle.radius, 16);
            }
        }
        Shape spot = ((GameScreen) MyGame.getInstance().getScreenType(MyGame.ScreenType.MainGame)).getSpotShape();
        if (spot != null) {
            collisionRenderer.setColor(Color.RED);
            if (spot.getType() == Shape.Type.POLYGON) {
                collisionRenderer.polygon(((PolygonShape) spot).getShape().getTransformedVertices());
            } else if (spot instanceof RectangleShape) {
                Rectangle rect = ((RectangleShape) spot).getShape();
                collisionRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
            } else if (spot instanceof CircleShape) {
                Circle circle = ((CircleShape) spot).getShape();
                collisionRenderer.circle(circle.x, circle.y, circle.radius, 16);
            }
        }


        collisionRenderer.end();
    }

    private void renderInteractionShapes() {

        collisionRenderer.setProjectionMatrix(getBatch().getProjectionMatrix());
        collisionRenderer.begin(ShapeRenderer.ShapeType.Line);
        collisionRenderer.setColor(Color.CHARTREUSE);

        ImmutableArray<Entity> entities = EntityEngine.getInstance().getEntitiesFor(Family.one(CollisionInteractionComponent.class, CollisionEffectComponent.class).get());
        for (Entity entity : entities) {

            CollisionInteractionComponent col = im.get(entity);
            CollisionEffectComponent effect = em.get(entity);

            if (col != null) {
                if (col.mShape.getType() == Shape.Type.POLYGON) {
                    collisionRenderer.polygon(((PolygonShape) col.mShape).getShape().getTransformedVertices());
                } else if (col.mShape instanceof RectangleShape) {
                    Rectangle rect = ((RectangleShape) col.mShape).getShape();
                    collisionRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
                } else if (col.mShape instanceof CircleShape) {
                    Circle circle = ((CircleShape) col.mShape).getShape();
                    collisionRenderer.circle(circle.x, circle.y, circle.radius, 16);
                }
            }
            if (effect != null) {
                if (effect.mZoneEffect.getType() == Shape.Type.POLYGON) {
                    collisionRenderer.polygon(((PolygonShape) effect.mZoneEffect).getShape().getTransformedVertices());
                } else if (effect.mZoneEffect instanceof RectangleShape) {
                    Rectangle rect = ((RectangleShape) effect.mZoneEffect).getShape();
                    collisionRenderer.rect(rect.getX(), rect.getY(), 0, 0, rect.getWidth(), rect.getHeight(), 1, 1, 0);
                } else if (effect.mZoneEffect instanceof CircleShape) {
                    Circle circle = ((CircleShape) effect.mZoneEffect).getShape();
                    collisionRenderer.circle(circle.x, circle.y, circle.radius, 16);
                }
            }
        }

        collisionRenderer.end();
    }
}
