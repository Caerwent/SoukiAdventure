package com.souki.game.adventure.map;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.audio.AudioEvent;
import com.souki.game.adventure.audio.AudioManager;
import com.souki.game.adventure.box2d.PathHero;
import com.souki.game.adventure.box2d.PathMap;
import com.souki.game.adventure.box2d.PolygonShape;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.box2d.ShapeUtils;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.CollisionEffectComponent;
import com.souki.game.adventure.entity.components.CollisionInteractionComponent;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;
import com.souki.game.adventure.entity.components.ICollisionObstacleHandler;
import com.souki.game.adventure.entity.components.InteractionComponent;
import com.souki.game.adventure.entity.components.LightComponent;
import com.souki.game.adventure.entity.components.PathComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;
import com.souki.game.adventure.entity.components.VisualComponent;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.challenge.ChallengeUI;
import com.souki.game.adventure.interactions.IInteraction;
import com.souki.game.adventure.interactions.InteractionChallenge;
import com.souki.game.adventure.interactions.InteractionFactory;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.interactions.InteractionMappingManager;
import com.souki.game.adventure.interactions.InteractionPortal;
import com.souki.game.adventure.persistence.LocationProfile;
import com.souki.game.adventure.persistence.MapProfile;
import com.souki.game.adventure.persistence.PersistenceProvider;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.player.Player;
import com.souki.game.adventure.quests.Quest;
import com.souki.game.adventure.quests.QuestManager;

import net.dermetfan.gdx.math.BayazitDecomposer;
import net.dermetfan.gdx.math.GeometryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by vincent on 20/01/2017.
 */

public class GameMap implements ICollisionObstacleHandler {
    public final static String TAG = GameMap.class.getSimpleName();

    private static final String LIGHT_TEXTURE = "data/maps/light.png";
    private static MapTownPortalInfo sTownPortalInfo;
    private TiledMap map;
    private String mMapName;
    private String mFromMapId;
    // private IsoTileMapRendererWithSprites renderer;
    private MapAndSpritesRenderer2 renderer;
    private OrthographicCamera mCamera;

    private HashMap<String, Array<Shape>> mBodiesZindex = new HashMap<String, Array<Shape>>();
    private Array<Shape> mBodiesCollision = new Array<Shape>();


    private Array<CollisionObstacleComponent> mCollisions = new Array<CollisionObstacleComponent>();
    private Array<IItemInteraction> mItems = new Array<IItemInteraction>();
    private Array<IInteraction> mInteractions = new Array<IInteraction>();
    private HashMap<String, PathMap> mPaths = new HashMap<String, PathMap>();
    private InteractionMappingManager mInteractionMappingManager = new InteractionMappingManager();

    private boolean mIsInitialized = false;

    private Player mPlayer;
    private String mMusic;

    private int mMapWidth;
    private int mMapTileWidth;
    private int mMapHeight;
    private int mMapTileHeight;

    private boolean mIsZoomOut = false;
    private float mCamXBeforeZoomOut, mCamYBeforeZoomOut;

    private Texture mLightTexture;
    private Sprite mLightSprite;
    private FrameBuffer mLightFrameBuffer;
    private SpriteBatch mLightSpriteBatch;
    private ComponentMapper<LightComponent> cm = ComponentMapper.getFor(LightComponent.class);

    public GameMap(String aMapName, String aFromMap, OrthographicCamera aCamera, MapTownPortalInfo aTownPortalInfo) {
        mMapName = aMapName;
        mFromMapId = aFromMap;
        boolean firstMapEntrance = false;
        MapProfile mapProfile = Profile.getInstance().getMapProfile(mMapName);
        if (mapProfile == null) {
            firstMapEntrance = true;
            mapProfile = new MapProfile();
            Profile.getInstance().updateMapProfile(mMapName, mapProfile);
        }
        String filename = "data/maps/" + aMapName + ".tmx";
        AssetsUtility.loadMapAsset(filename);
        if (AssetsUtility.isAssetLoaded(filename)) {
            map = AssetsUtility.getMapAsset(filename);
        } else {
            Gdx.app.debug(TAG, "Map not loaded");
            return;
        }
        mCamera = aCamera;
        MapProperties mapProperties = map.getProperties();
        mMapWidth = mapProperties.get("width", Integer.class);
        mMapTileWidth = mapProperties.get("tilewidth", Integer.class);
        mMapHeight = mapProperties.get("height", Integer.class);
        mMapTileHeight = mapProperties.get("tileheight", Integer.class);
        mMapWidth = (int) (mMapWidth * mMapTileWidth * MyGame.SCALE_FACTOR);
        mMapHeight = (int) (mMapHeight * mMapTileHeight * MyGame.SCALE_FACTOR);


        if (PersistenceProvider.getInstance().getSettings().musicActivated) {
            mMusic = mapProperties.get("music", String.class);
            if (mMusic != null && !mMusic.isEmpty()) {
                mMusic = AssetsUtility.getString(mMusic);
                AudioManager.getInstance().onAudioEvent(new AudioEvent(AudioEvent.Type.MUSIC_LOAD, mMusic));
            }
        }
        mPlayer = new Player(this);
        mPlayer.getHero().setCamera(mCamera);
        mPlayer.getHero().setPath(null);
        mPaths = buildPaths(map, "path");
        mItems = buildItems(map, "items");
        mInteractionMappingManager.loadMappingFile("data/interactions/" + aMapName + "_interactions_mapping.json");
        buildMapInteractions(map, "interactions");

        boolean isMapBackWithTownPortal = aTownPortalInfo != null && aTownPortalInfo.originMap.compareTo(getMapName()) == 0 && !aTownPortalInfo.isCheckpoint;
        boolean isCurrentMapIsDefault = getMapName().compareTo(MyGame.DEFAULT_MAP_NAME) == 0;
        boolean isCheckpointPortal = aTownPortalInfo != null && aTownPortalInfo.isCheckpoint;

        LocationProfile locationProfile = new LocationProfile();
        locationProfile.mMapId = mMapName;


        if (isMapBackWithTownPortal) {
            sTownPortalInfo = null;
            // set hero at the town portal position
            mPlayer.getHero().setPosition(aTownPortalInfo.x, aTownPortalInfo.y);
            tryToSetCameraAtPosition(aTownPortalInfo.x, aTownPortalInfo.y);
            mPlayer.getHero().launchTownPortalArrivalEffect(aTownPortalInfo);
        }
        if (isCheckpointPortal) {
            for (IInteraction interaction : mInteractions) {
                if (interaction.getType() == IInteraction.Type.CHALLENGE &&
                        ((InteractionChallenge) interaction).getChallengeType() == ChallengeUI.ChallengeType.PORTAL_CHECKPOINT) {
                    sTownPortalInfo = null;
                    mPlayer.getHero().setPosition(interaction.getX(), interaction.getY());
                    tryToSetCameraAtPosition(interaction.getX(), interaction.getY());
                    mPlayer.getHero().launchTownPortalArrivalEffect(aTownPortalInfo);
                }
            }
        }
        if (aTownPortalInfo == null) {
            locationProfile.mFromMapId = aFromMap;
        }
        Profile.getInstance().setLocationProfile(locationProfile);


        InteractionPortal defaultPortal = null;
        InteractionPortal arrivalPortal = null;
        for (IInteraction control : mInteractions) {
            if (control.getType() == IInteraction.Type.PORTAL) {
                InteractionPortal portal = (InteractionPortal) control;

                if (portal.isDefaultStart()) {
                    defaultPortal = portal;
                }
                boolean isDefaultCase = aFromMap == null && portal.isDefaultStart();
                boolean isPortalToPortalCase = aTownPortalInfo == null && aFromMap != null && portal.getTargetMapId() != null && portal.getTargetMapId().compareTo(aFromMap) == 0;
                boolean isDefaultMapArrivalFromTownPortal = aTownPortalInfo != null && isCurrentMapIsDefault && portal.isDefaultStart();
                if (isDefaultCase || isPortalToPortalCase || isDefaultMapArrivalFromTownPortal) {
                    mPlayer.getHero().setPosition(control.getX(), control.getY());
                    tryToSetCameraAtPosition(control.getX(), control.getY());
                    if (isDefaultMapArrivalFromTownPortal) {
                        sTownPortalInfo = aTownPortalInfo;
                        mPlayer.getHero().launchTownPortalArrivalEffect(aTownPortalInfo);
                    } else if (isPortalToPortalCase && MyGame.getInstance().isDefaultMapOrAssociated(mMapName) && sTownPortalInfo != null) {
                        mPlayer.getHero().setTownPortalInfo(sTownPortalInfo);
                    } else {
                        sTownPortalInfo = null;
                    }
                    arrivalPortal = portal;
                    portal.setActivated(false);

                    if (portal.getQuestId() != null) {
                        Quest theQuest = QuestManager.getInstance().getQuestFromId(portal.getQuestId());
                        if (theQuest != null && !theQuest.isCompleted()) {
                            theQuest.setActivated(true);
                            EventDispatcher.getInstance().onQuestActivated(theQuest);
                        }
                    }
                    //  Gdx.app.debug("DEBUG", "init camera pointX=" + mCamera.position.x + " pointY=" + mCamera.position.y);


                } else {
                    if (aTownPortalInfo != null) {
                        // check hero is not on the portal
                        if (ShapeUtils.overlaps(mPlayer.getHero().getShapeInteraction(), portal.getShapeInteraction())) {
                            portal.setActivated(false);
                        } else {
                            portal.setActivated(true);
                        }
                    } else {
                        portal.setActivated(true);
                    }
                }
            }
        }
        if (arrivalPortal == null && defaultPortal != null && !isMapBackWithTownPortal && !isCheckpointPortal) {
            mPlayer.getHero().setPosition(defaultPortal.getX(), defaultPortal.getY());
            tryToSetCameraAtPosition(defaultPortal.getX(), defaultPortal.getY());

            defaultPortal.setActivated(false);

            if (defaultPortal.getQuestId() != null) {
                Quest theQuest = QuestManager.getInstance().getQuestFromId(defaultPortal.getQuestId());
                if (theQuest != null && !theQuest.isCompleted()) {
                    theQuest.setActivated(true);
                    EventDispatcher.getInstance().onQuestActivated(theQuest);
                }
            }
        }

        if (mMapWidth <= mCamera.viewportWidth) {
            mCamera.position.x = mMapWidth / 2;
            mCamera.update();
        }
        if (mMapHeight <= mCamera.viewportHeight) {
            mCamera.position.y = mMapHeight / 2;
            mCamera.update();
        }


        renderer = new MapAndSpritesRenderer2(this, MyGame.SCALE_FACTOR);
        // mBodiesZindex = buildShapes(map, "zindex");
        for (MapLayer layer : map.getLayers()) {
            if (layer.getName().startsWith("zindex_")) {
                mBodiesZindex.put(layer.getName().substring("zindex_".length(), layer.getName().length()), buildShapes(map, layer.getName()));
            } else if (layer.getName().compareTo("zindex") == 0) {
                mBodiesZindex.put("zindex", buildShapes(map, layer.getName()));
            }
        }
        if (!mBodiesZindex.containsKey("zindex")) {
            mBodiesZindex.put("zindex", new Array());
        }
        mBodiesCollision = buildShapes(map, "collision");

        if (mPaths.containsKey("hero") && firstMapEntrance) {
            mPlayer.getHero().setPath(new PathHero(mPaths.get("hero")));
        }
        mPlayer.getHero().startToInteract();
        mIsInitialized = true;

        buildLight(map);

    }

    private void buildLight(Map map) {

        if (!map.getProperties().get("hasLight", Boolean.FALSE, Boolean.class)) {
             mLightSpriteBatch = null;
        } else {
            mLightSpriteBatch = new SpriteBatch();
            AssetsUtility.loadTextureAsset(LIGHT_TEXTURE);
            mLightTexture = AssetsUtility.getTextureAsset(LIGHT_TEXTURE);
            mLightSprite = new Sprite(mLightTexture);
            mLightSprite.setSize(6, 6);
            int frameWidth = mMapWidth * mMapTileWidth;
            int frameHeight = mMapHeight * mMapTileHeight;
            if (mLightFrameBuffer != null && (mLightFrameBuffer.getWidth() != frameWidth || mLightFrameBuffer.getHeight() != frameHeight)) {
                mLightFrameBuffer.dispose();
                mLightFrameBuffer = null;
            }

            if (mLightFrameBuffer == null) {
                try {
                    mLightFrameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, frameWidth, frameHeight, false);
                } catch (GdxRuntimeException e) {
                    mLightFrameBuffer = new FrameBuffer(Pixmap.Format.RGB565, frameWidth, frameHeight, false);
                }
            }
        }



    }

    public void zoomOut(boolean isZoomOut) {
        if (isZoomOut) {
            float xZoom = mMapWidth / mCamera.viewportWidth;
            float yZoom = mMapHeight / mCamera.viewportHeight;

            float zoom = Math.max(xZoom, yZoom);
            mIsZoomOut = true;
            mCamera.zoom = zoom;
            mCamXBeforeZoomOut = mCamera.position.x;
            mCamYBeforeZoomOut = mCamera.position.y;
            mCamera.position.x = mMapWidth / 2;
            mCamera.position.y = mMapHeight / 2;

        } else {
            mIsZoomOut = false;
            mCamera.zoom = 1;
            mCamera.position.x = mCamXBeforeZoomOut;
            mCamera.position.y = mCamYBeforeZoomOut;
        }
        mCamera.update();
    }

    protected void tryToSetCameraAtPosition(float x, float y) {
        if (mIsZoomOut) {
            mIsZoomOut = false;
            mCamera.zoom = 1;
        }
        mCamera.position.x = x;
        mCamera.position.y = y;

        if (x + (mCamera.viewportWidth / 2) > mMapWidth) {
            mCamera.position.x = mMapWidth - (mCamera.viewportWidth / 2);
        } else if (x - (mCamera.viewportWidth / 2) < 0) {
            mCamera.position.x = mCamera.viewportWidth / 2;
        }
        if (y + (mCamera.viewportHeight / 2) > mMapHeight) {
            mCamera.position.y = mMapHeight - (mCamera.viewportHeight / 2);
        } else if (y - (mCamera.viewportHeight / 2) < 0) {
            mCamera.position.y = mCamera.viewportHeight / 2;
        }
        mCamera.update();
    }

    public String getMapName() {
        return mMapName;
    }

    public String getFromMapId() {
        return mFromMapId;
    }

    public Player getPlayer() {
        return mPlayer;
    }

    public boolean isInitialized() {
        return mIsInitialized;
    }

    public void playMusic(boolean aPlay) {
        if (mMusic != null && !mMusic.isEmpty()) {
            AudioManager.getInstance().onAudioEvent(new AudioEvent(aPlay ? AudioEvent.Type.MUSIC_PLAY_LOOP : AudioEvent.Type.MUSIC_STOP, mMusic));
        }
    }

    public void destroy() {
        ImmutableArray<Entity> entities = EntityEngine.getInstance().getEntitiesFor(Family.one(CollisionObstacleComponent.class, CollisionInteractionComponent.class, CollisionEffectComponent.class, InteractionComponent.class, PathComponent.class, TransformComponent.class, VelocityComponent.class, VisualComponent.class).get());
        int size = entities.size();
        for (int i = size - 1; i >= 0; i--) {
            EntityEngine.getInstance().removeEntity(entities.get(i));
        }
        for (IInteraction it : mInteractions) {
            it.destroy();
        }
        for (IItemInteraction it : mItems) {
            it.destroy();
        }
        mBodiesCollision.clear();
        mItems.clear();
        mBodiesZindex.clear();
        mInteractions.clear();
        String filename = "data/maps/" + mMapName + ".tmx";
        AssetsUtility.unloadAsset(filename);
        if (mLightTexture != null) {
            AssetsUtility.unloadAsset(LIGHT_TEXTURE);
        }
    }

    public void render() {
        if (!mIsInitialized)
            return;


        MapProperties mapProperties = map.getProperties();
        int mapWidth = mapProperties.get("width", Integer.class);
        int mapTileWidth = mapProperties.get("tilewidth", Integer.class);
        int mapHeight = mapProperties.get("height", Integer.class);
        int mapTileHeight = mapProperties.get("tileheight", Integer.class);
        mapWidth = (int) (mapWidth * mapTileWidth * MyGame.SCALE_FACTOR);
        mapHeight = (int) (mapHeight * mapTileHeight * MyGame.SCALE_FACTOR);

        if (mPlayer.getHero().getPosition().x > mCamera.viewportWidth / 2 && mPlayer.getHero().getPosition().x < mapWidth - (mCamera.viewportWidth / 2)) {
            if (mIsZoomOut)
                mCamXBeforeZoomOut = mPlayer.getHero().getPosition().x;
            else
                mCamera.position.x = mPlayer.getHero().getPosition().x;
        }
        if (mPlayer.getHero().getPosition().y > mCamera.viewportHeight / 2 && mPlayer.getHero().getPosition().y < mapHeight - (mCamera.viewportHeight / 2)) {
            if (mIsZoomOut)
                mCamYBeforeZoomOut = mPlayer.getHero().getPosition().y;
            else
                mCamera.position.y = mPlayer.getHero().getPosition().y;
        }

        mCamera.update();
        renderer.setView(mCamera);
        renderer.render();
        renderLight();


    }

    private void renderLight()
    {
        if (mLightSpriteBatch != null) {
            mLightFrameBuffer.begin();

            Gdx.gl.glClearColor(.3f, .3f, .3f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            mLightSpriteBatch.setProjectionMatrix(mCamera.combined);
            mLightSpriteBatch.setBlendFunction(GL20.GL_ONE, GL20.GL_ONE);
            mLightSpriteBatch.begin();
            mLightSprite.setSize(6,6);
            mLightSprite.setCenterX(mPlayer.getHero().getShapeInteraction().getX() + mPlayer.getHero().getShapeInteraction().getWidth() / 2);
            mLightSprite.setCenterY(mPlayer.getHero().getShapeInteraction().getY() + mPlayer.getHero().getShapeInteraction().getHeight() / 2);

            mLightSprite.setColor(Color.WHITE);
            mLightSprite.draw(mLightSpriteBatch);



            ImmutableArray<Entity> entities = EntityEngine.getInstance().getEntitiesFor(Family.all(LightComponent.class).get());
            for (Entity entity : entities) {

                LightComponent col = cm.get(entity);
                mLightSprite.setSize(col.getRadius(), col.getRadius());
                mLightSprite.setCenterX(col.position.x);
                mLightSprite.setCenterY(col.position.y);
                mLightSprite.setColor(col.getColor());

                mLightSprite.draw(mLightSpriteBatch);


            }

            mLightSpriteBatch.end();

            mLightFrameBuffer.end();

            mLightSpriteBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            mLightSpriteBatch.setProjectionMatrix(mLightSpriteBatch.getProjectionMatrix().idt());

            mLightSpriteBatch.setBlendFunction(GL20.GL_ZERO, GL20.GL_SRC_COLOR);
            mLightSpriteBatch.begin();

            mLightSpriteBatch.draw(mLightFrameBuffer.getColorBufferTexture(), -1, 1, 2, -2);
            mLightSpriteBatch.end();
        }
    }


    public TiledMap getTiledMap() {
        return map;
    }

    public int getZindexCount() {
        return mBodiesZindex.size();
    }

    public Array<Shape> getBodiesZindex(String aLayerName) {
        if (mBodiesZindex.containsKey(aLayerName))
            return mBodiesZindex.get(aLayerName);
        else
            return mBodiesZindex.get("zindex");
    }

    public Array<Shape> getBodiesCollision() {
        return mBodiesCollision;
    }

    public Array<IItemInteraction> getItems() {
        return mItems;
    }

    public Array<IInteraction> getInteractions() {
        return mInteractions;
    }


    public HashMap<String, PathMap> getPaths() {
        return mPaths;
    }

    @Override
    public boolean onCollisionObstacleStart(CollisionObstacleComponent aEntity, boolean aIsPrediction) {
        return false;
    }

    @Override
    public boolean onCollisionObstacleStop(CollisionObstacleComponent aEntity) {
        return false;
    }

    @Override
    public Array<CollisionObstacleComponent> getCollisionObstacle() {
        return mCollisions;
    }

    private Array<Shape> buildShapes(Map map, String layerName) {
        boolean isCollision = layerName.contains("zindex") ? false : true;

        Array<Shape> bodies = new Array<Shape>();
        if (map.getLayers().get(layerName) == null)
            return bodies;

        MapObjects objects = map.getLayers().get(layerName).getObjects();

        for (MapObject object : objects) {

            if (object instanceof TextureMapObject) {
                continue;
            }

            Polygon polygon = null;

            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                polygon = new Polygon(new float[]{0, 0, rect.getWidth() * MyGame.SCALE_FACTOR, 0, rect.getWidth() * MyGame.SCALE_FACTOR, rect.getHeight() * MyGame.SCALE_FACTOR, 0, rect.getHeight() * MyGame.SCALE_FACTOR});
                polygon.setPosition(rect.x * MyGame.SCALE_FACTOR, rect.y * MyGame.SCALE_FACTOR);
            } else if (object instanceof PolygonMapObject) {
                float[] mapVertices = ((PolygonMapObject) object).getPolygon().getTransformedVertices();
                float[] vertices = new float[mapVertices.length]; // last point is the first
                for (int i = 0; i < vertices.length; ++i) {
                    vertices[i] = mapVertices[i] * MyGame.SCALE_FACTOR;
                }
                polygon = new Polygon(vertices);
            }/* else if (object instanceof PolylineMapObject) {
                shape = ShapeFactory.getPolyline((PolylineMapObject) object);
            } else if (object instanceof CircleMapObject) {
                shape = ShapeFactory.getCircle((CircleMapObject) object);
            }*/ else {
                continue;
            }
            if (isCollision && polygon != null) {

                BayazitDecomposer.maxPolygonVertices = Math.max(BayazitDecomposer.maxPolygonVertices, polygon.getVertices().length / 2);
                float[][] convexPolys = GeometryUtils.decompose(polygon.getTransformedVertices());
                for (int currPolyId = 0; currPolyId < convexPolys.length; currPolyId++) {
                    polygon = new Polygon(convexPolys[currPolyId]);
                    PolygonShape shape = new PolygonShape();
                    shape.setShape(polygon);
                    bodies.add(shape);
                    Entity entity = new Entity();
                    entity.add(new CollisionObstacleComponent(CollisionObstacleComponent.OBSTACLE, shape, object.getName(), this, this));
                    EntityEngine.getInstance().addEntity(entity);
                }

            } else {
                PolygonShape shape = new PolygonShape();
                shape.setShape(polygon);
                bodies.add(shape);

            }

        }
        Shape[] sortedShapes = bodies.toArray(Shape.class);
        Arrays.sort(sortedShapes, new Comparator<Shape>() {
            @Override
            public int compare(Shape lhs, Shape rhs) {

                float lhsY = lhs.getBounds().getY();
                float rhsY = rhs.getBounds().getY();
                if (lhsY == rhsY) {
                    return 0;
                } else {
                    return lhsY < rhsY ? 1 : -1;
                }
            }
        });
        bodies.clear();
        bodies = new Array<Shape>(sortedShapes);
        return bodies;
    }

    private HashMap<String, PathMap> buildPaths(Map map, String layerName) {
        HashMap<String, PathMap> paths = new HashMap<String, PathMap>();

        if (map.getLayers().get(layerName) == null)
            return paths;
        MapObjects objects = map.getLayers().get(layerName).getObjects();


        for (MapObject object : objects) {


            if (object instanceof PolylineMapObject) {
                float[] mapVertices = ((PolylineMapObject) object).getPolyline().getTransformedVertices();
                float[] vertices = new float[mapVertices.length];
                PathMap path = new PathMap(object.getName());
                for (int i = 0; i < vertices.length - 1; i += 2) {
                    path.addPoint(mapVertices[i] * MyGame.SCALE_FACTOR, mapVertices[i + 1] * MyGame.SCALE_FACTOR);
                }
                boolean isLoop = false;
                if (object.getProperties().containsKey("isLoop")) {
                    isLoop = object.getProperties().get("isLoop", Boolean.class);
                    path.setLoop(isLoop);
                }
                if (object.getProperties().containsKey("isLoopReversing")) {
                    isLoop = object.getProperties().get("isLoopReversing", Boolean.class);
                    path.setLoopReversing(isLoop);
                }

                paths.put(object.getName(), path);

            } else {
                continue;
            }
        }
        return paths;
    }

    private void buildMapInteractions(Map map, String layerName) {
        mInteractions = new Array<IInteraction>();
        if (map.getLayers().get(layerName) == null) {
            return;
        }
        MapObjects objects = map.getLayers().get(layerName).getObjects();

        MapProfile mapProfile = Profile.getInstance().getMapProfile(mMapName);
        for (MapObject object : objects) {

            if (object instanceof TextureMapObject) {
                TextureMapObject textureObject = (TextureMapObject) object;
                float x = textureObject.getX() * MyGame.SCALE_FACTOR;
                float y = textureObject.getY() * MyGame.SCALE_FACTOR;
                InteractionMapping mapping = mInteractionMappingManager.getInterationMapping(object.getName());
                if (mapping == null) {
                    continue;
                }
                IInteraction interaction = InteractionFactory.getInstance().createInteractionInstance(x, y, mapping, textureObject.getProperties(), this);
                if (interaction != null) {
                    interaction.setCamera(mCamera);
                    mInteractions.add(interaction);
                }

            } else if (object instanceof RectangleMapObject) {
                RectangleMapObject textureObject = (RectangleMapObject) object;
                float x = textureObject.getRectangle().getX() * MyGame.SCALE_FACTOR;
                float y = textureObject.getRectangle().getY() * MyGame.SCALE_FACTOR;
                InteractionMapping mapping = mInteractionMappingManager.getInterationMapping(object.getName());
                if (mapping == null) {
                    continue;
                }
                IInteraction interaction = InteractionFactory.getInstance().createInteractionInstance(x, y, mapping, textureObject.getProperties(), this);
                if (interaction != null) {
                    interaction.setCamera(mCamera);
                    mInteractions.add(interaction);

                }

            }
        }
        IInteraction[] interactions = new IInteraction[mInteractions.size];
        interactions = mInteractions.toArray(IInteraction.class);

        for (int i = 0; i < interactions.length; i++) {
            interactions[i].startToInteract();
        }
    }

    private Array<IItemInteraction> buildItems(Map map, String layerName) {
        Array<IItemInteraction> interactions = new Array<IItemInteraction>();
        if (map.getLayers().get(layerName) == null) {
            return interactions;
        }
        MapObjects objects = map.getLayers().get(layerName).getObjects();


        MapProfile mapProfile = Profile.getInstance().getMapProfile(mMapName);
        for (MapObject object : objects) {

            if (object instanceof TextureMapObject || object instanceof RectangleMapObject) {
                float x = 0;
                float y = 0;
                String type = null;
                String name = null;
                if (object instanceof TextureMapObject) {
                    TextureMapObject textureObject = (TextureMapObject) object;
                    x = textureObject.getX() * MyGame.SCALE_FACTOR;
                    y = textureObject.getY() * MyGame.SCALE_FACTOR;
                    type = textureObject.getProperties().get("type", String.class);
                    name = textureObject.getName();
                } else if (object instanceof RectangleMapObject) {
                    RectangleMapObject textureObject = (RectangleMapObject) object;
                    x = textureObject.getRectangle().getX() * MyGame.SCALE_FACTOR;
                    y = textureObject.getRectangle().getY() * MyGame.SCALE_FACTOR;
                    type = textureObject.getProperties().get("type", String.class);
                    name = textureObject.getName();
                }
                if (type == null) {
                    continue;
                }
                IItemInteraction interaction = null;
                if (type.compareTo(IItemInteraction.Type.ITEM.name()) == 0) {
                    boolean itemAlreadyFound = false;
                    if (mapProfile != null) {
                        ArrayList<Vector2> itemsFound = mapProfile.items.get(name);
                        if (itemsFound != null) {
                            for (Vector2 v : itemsFound) {
                                if (Math.abs(v.x - x) <= 0.2 && Math.abs(v.y - y) <= 0.2) {
                                    itemAlreadyFound = true;
                                    break;

                                }
                            }
                        }
                    }
                    if (!itemAlreadyFound) {
                        interaction = new ItemInteraction(x, y, name, this);
                    }

                }


            }

        }
        Profile.getInstance().getMapProfile(mMapName);
        return interactions;
    }

    public void removeItem(ItemInteraction aItem) {
        mItems.removeValue(aItem, true);
        MapProfile mapProfile = Profile.getInstance().getMapProfile(mMapName);
        ArrayList<Vector2> itemsFound = mapProfile.items.get(aItem.getId());
        if (itemsFound == null) {
            itemsFound = new ArrayList<Vector2>();
        }
        boolean itemAlreadyFound = false;
        for (Vector2 v : itemsFound) {
            if (Math.abs(v.x - aItem.getX()) <= 0.2 && Math.abs(v.y - aItem.getY()) <= 0.2) {
                itemAlreadyFound = true;
                break;
            }
        }
        if (!itemAlreadyFound) {
            itemsFound.add(new Vector2(aItem.getX(), aItem.getY()));
        }
        mapProfile.items.put(aItem.getId(), itemsFound);
        Profile.getInstance().updateMapProfile(mMapName, mapProfile);
    }
}
