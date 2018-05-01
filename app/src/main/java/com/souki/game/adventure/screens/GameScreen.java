package com.souki.game.adventure.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.souki.game.adventure.CharacterMoveController5;
import com.souki.game.adventure.Settings;
import com.souki.game.adventure.box2d.Shape;
import com.souki.game.adventure.entity.EntityEngine;
import com.souki.game.adventure.entity.components.InputComponent;
import com.souki.game.adventure.entity.systems.CollisionInteractionSystem;
import com.souki.game.adventure.entity.systems.CollisionObstacleSystem;
import com.souki.game.adventure.entity.systems.InteractionSystem;
import com.souki.game.adventure.entity.systems.MovementSystem;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.gui.MainHUD;
import com.souki.game.adventure.gui.UIStage;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.map.MapTownPortalInfo;
import com.souki.game.adventure.quests.QuestManager;

import static com.souki.game.adventure.Settings.TARGET_HEIGHT;
import static com.souki.game.adventure.Settings.TARGET_WIDTH;

/**
 * Created by gwalarn on 16/11/16.
 */

public class GameScreen implements Screen, GestureDetector.GestureListener, InputProcessor {

    public final static String TAG = GameScreen.class.getSimpleName();

    private static class LoadMapInfo {
        public String mTargetMapId;
        public String mFromMapId;
        public MapTownPortalInfo mTownPortalInfo;
    }
    public Rectangle viewport;
    public int orientation;

    private GameMap map;

    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    //private OrthoCamController cameraController;
    private CharacterMoveController5 bobController;
    private AssetManager assetManager;
    private BitmapFont font;
    private SpriteBatch batch;
    ShapeRenderer pathRenderer;

    protected boolean mIsUnloading = false;
    protected LoadMapInfo mLoadMapInfo;
    boolean mIsIrisRunning = false;
    boolean mIsIrisReload = false;
    private float mIrisTime = 0;
    private static final float IRIS_DURATION = 1;
    private ShapeRenderer mIrisRenderer;
    private Array<Array<Vector3>> mIrisTriX = new Array<Array<Vector3>>();
    private Array<Array<Vector3>> mIrisTriY = new Array<Array<Vector3>>();

    final private int mIrisCircleSmoothness = 62; //the higher the more smooth the circle
    private float IRIS_OUTERBOUND = 500; //the higher this is, the further is expands 'out' at its fullest
    final private float IRIS_INNERBOUND = 1;
    private float mIrisCurrentBound = IRIS_OUTERBOUND; //where it currently is at


    private ShapeRenderer mPathSpotRenderer;
    private Shape mSpot;
    private double accumulator;
    private double currentTime;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    private float physicsDeltaTime = 1.0f / 60.0f;

    InputMultiplexer mInputMultiplexer = new InputMultiplexer();



    public GameScreen() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        float worldViewPortHeight = 15 / (h / TARGET_HEIGHT);
        camera.setToOrtho(false, (w / h) * 10, 10);
        //camera.zoom = 2;
        camera.update();

        //cameraController = new OrthoCamController(camera);

        uiCamera = new OrthographicCamera(TARGET_WIDTH, TARGET_HEIGHT);
        mIrisRenderer = new ShapeRenderer();
        createIrisWipe();

        font = new BitmapFont();
        batch = new SpriteBatch();
        pathRenderer = new ShapeRenderer();
        pathRenderer.setAutoShapeType(true);
        pathRenderer.setProjectionMatrix(camera.combined);
        mPathSpotRenderer = new ShapeRenderer();
        mPathSpotRenderer.setColor(new Color(0x80ff00ff));
        mPathSpotRenderer.setAutoShapeType(true);
        mPathSpotRenderer.setProjectionMatrix(camera.combined);

        accumulator = 0.0;
        currentTime = TimeUtils.millis() / 1000.0;

        QuestManager.getInstance();

        assetManager = new AssetManager();


        UIStage.createInstance(new ExtendViewport(TARGET_WIDTH, TARGET_HEIGHT, uiCamera));

        UIStage.getInstance().setHUD(new MainHUD());


        // bobController = new ChararcterMoveController2(camera);
        bobController = new CharacterMoveController5(camera);
        GestureDetector gd = new GestureDetector(bobController);

        mInputMultiplexer.addProcessor(UIStage.getInstance());
        mInputMultiplexer.addProcessor(gd);
        GestureDetector gd2 = new GestureDetector(this);
        mInputMultiplexer.addProcessor(gd2);


    }

    public void setSpotShape(Shape aSpot) {
        mSpot = aSpot;
    }

    public Shape getSpotShape() {
        return mSpot;
    }

    protected void releaseMap()
    {
        if (map != null) {

            map.playMusic(false);
            map.destroy();
            EntityEngine.destroyInstance();
            map=null;

        }
    }
    public void loadMap(String aTargetMapId, String aFromMapId, MapTownPortalInfo aTownPortalInfo) {
        mIsIrisReload = false;
        mIsIrisRunning = true;
        mIrisTime = 0;
        mLoadMapInfo = new LoadMapInfo();
        mLoadMapInfo.mTargetMapId = aTargetMapId;
        mLoadMapInfo.mFromMapId = aFromMapId;
        mLoadMapInfo.mTownPortalInfo = aTownPortalInfo;
        if (map != null) {
            mIsUnloading = true;
            mIsIrisReload = true;
            return;
        }

        releaseMap();
        internalLoadMap();

    }

    protected void internalLoadMap()
    {
        if(mLoadMapInfo==null)
            return;

        EntityEngine.createInstance();
        EntityEngine.getInstance().addSystem(new MovementSystem());
        EntityEngine.getInstance().addSystem(new CollisionObstacleSystem());
        EntityEngine.getInstance().addSystem(new InteractionSystem());
        EntityEngine.getInstance().addSystem(new CollisionInteractionSystem());
        //  EntityEngine.getInstance().addSystem(new PathRenderSystem(pathRenderer));
        map = new GameMap(mLoadMapInfo.mTargetMapId, mLoadMapInfo.mFromMapId, camera, mLoadMapInfo.mTownPortalInfo);
        bobController.setMap(map);
        EventDispatcher.getInstance().onMapLoaded(map);
        map.playMusic(true);
    }


    @Override
    public void show() {

        if (map != null) {
            map.playMusic(true);
        }
        Gdx.input.setInputProcessor(mInputMultiplexer);
        if (EntityEngine.getInstance() != null && EntityEngine.getInstance().getSystems().size() == 0) {
            EntityEngine.getInstance().addSystem(new MovementSystem());
            EntityEngine.getInstance().addSystem(new CollisionObstacleSystem());
            EntityEngine.getInstance().addSystem(new InteractionSystem());
            EntityEngine.getInstance().addSystem(new CollisionInteractionSystem());
            //  EntityEngine.getInstance().addSystem(new PathRenderSystem(pathRenderer));
        }
    }

    @Override
    public void hide() {
        if (map != null) {
            map.playMusic(false);
        }
        Gdx.input.setInputProcessor(null);
        if (EntityEngine.getInstance() != null) {
            EntityEngine.getInstance().removeSystem(EntityEngine.getInstance().getSystem(MovementSystem.class));
            EntityEngine.getInstance().removeSystem(EntityEngine.getInstance().getSystem(CollisionObstacleSystem.class));
            EntityEngine.getInstance().removeSystem(EntityEngine.getInstance().getSystem(CollisionInteractionSystem.class));
            EntityEngine.getInstance().removeSystem(EntityEngine.getInstance().getSystem(InteractionSystem.class));
            //   EntityEngine.getInstance().removeSystem(EntityEngine.getInstance().getSystem(PathRenderSystem.class));
        }

    }

    public boolean isUnloading()
    {
        return mIsUnloading;
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(!mIsUnloading)
        {
            //EventDispatcher.getInstance().processInteractionEvent();
            UIStage.getInstance().act(delta);


            if (EntityEngine.getInstance() != null) {
                EntityEngine.getInstance().
                        update(delta/*Time*/);
            }
        }



        if (map != null)
            map.render();
        if(!mIsUnloading) {
            pathRenderer.setProjectionMatrix(camera.combined);
            if (map.getPlayer().getHero().getPath() != null) {
                pathRenderer.begin();
                map.getPlayer().getHero().getPath().render(pathRenderer);
                pathRenderer.end();
            } else if (bobController.getPath() != null) {
                pathRenderer.begin();
                bobController.getPath().render(pathRenderer);
                pathRenderer.end();
            }
            batch.begin();
            //font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
            if (mSpot != null) {
                batch.setProjectionMatrix(camera.combined);

                batch.setColor(Color.YELLOW);
                batch.enableBlending();
                map.getPlayer().getHero().renderShadowed(mSpot.getX(), mSpot.getY(), batch);
            }

            batch.end();
        }


        UIStage.getInstance().

                draw();

        if (mIsIrisRunning) {
            mIrisTime += delta;
            drawIris(mIrisTime);
            if (mIrisTime >= IRIS_DURATION) {
                if (mIsIrisReload) {
                    mIsIrisReload = false;
                    mIrisTime = 0;
                } else {
                    mIsIrisRunning = false;
                }
                if(mIsUnloading)
                {
                    mIsUnloading = false;
                    releaseMap();
                    internalLoadMap();
                }

            }
        }


    }

    @Override
    public void resize(int width, int height) {
        float aspectRatio = (float) width / (float) height;
        float scale = 1f;
        Vector2 crop = new Vector2(0f, 0f);

        if (aspectRatio > Settings.ASPECT_RATIO) {
            scale = (float) height / (float) TARGET_HEIGHT;
            crop.x = (width - TARGET_WIDTH * scale) / 2f;
        } else if (aspectRatio < Settings.ASPECT_RATIO) {
            scale = (float) width / (float) Settings.TARGET_WIDTH;
            crop.y = (height - TARGET_HEIGHT * scale) / 2f;
        } else {
            scale = (float) width / (float) Settings.TARGET_WIDTH;
        }

        float w = (float) TARGET_WIDTH * scale;
        float h = (float) TARGET_HEIGHT * scale;

        viewport = new Rectangle(crop.x, crop.y, w, h);
        if (height > width)
            orientation = 0;
        else
            orientation = 1;

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }


    public Camera getCamera() {
        return camera;
    }

    public GameMap getMap() {
        return map;
    }

    @Override
    public void dispose() {

        UIStage.getInstance().removeHUD();


        if (map != null) {
            map.destroy();
        }
        EntityEngine.destroyInstance();
    }

    @Override
    public boolean touchDragged(int x, int y, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int i, int i1) {
        return false;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean keyDown(int keyCode) {

        switch (keyCode) {
            case Input.Keys.ESCAPE:
            case Input.Keys.BACK:
            case Input.Keys.MENU:
               // MyGame.getInstance().setScreen(MyGame.ScreenType.MainMenu);
                break;
            default:
                //  Log.out("unknown key");

        }
        return true;
    }

    @Override
    public boolean keyTyped(char arg0) {
        return false;
    }

    @Override
    public boolean keyUp(int arg0) {
        return false;
    }

    @Override
    public boolean scrolled(int arg0) {
        return false;
    }

    @Override
    public boolean touchDown(int x, int y, int pointer, int button) {
        return false;

    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        final Vector3 curr = new Vector3();
        camera.unproject(curr.set(x, y, 0));

        if (EntityEngine.getInstance() != null) {
            ImmutableArray<Entity> inputEntities = EntityEngine.getInstance().getEntitiesFor(Family.all(InputComponent.class).get());
            if (inputEntities != null) {
                for (Entity entity : inputEntities) {
                    InputComponent input = entity.getComponent(InputComponent.class);
                    if (input.tap(x, y, count, button)) {
                        return true;
                    }
                }
            }
        }
      //  Gdx.app.debug("DEBUG", "touchX=" + curr.x + " touchY=" + curr.y);
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


    private void createIrisWipe()
    {
  /*
  * create two arrays of arrays for circle triangles
  */
        mIrisTriX = new Array<Array<Vector3>>();
        mIrisTriY = new Array<Array<Vector3>>();

  /*
 * this is angle "slice" for each triangle that makes up the circle
  */
        float step = (360f / mIrisCircleSmoothness);

        for(float angle = 0; angle < 360; angle = angle + step)
        {
            //first vertex
            float x1 = (float)Math.cos(Math.toRadians(angle));
            float y1 = (float)Math.sin(Math.toRadians(angle));

            //second vertex
            float x2 = (float)Math.cos(Math.toRadians(angle + step));
            float y2 = (float)Math.sin(Math.toRadians(angle + step));

            //third vertex
            float x3 = (float)Math.cos(Math.toRadians(angle +  step * 2f ));
            float y3 = (float)Math.sin(Math.toRadians(angle + step * 2f));

            //add x positions to array of arrays
            //combined, these give the vertices of the triangle to draw later
            Array<Vector3> tempTriX = new Array<Vector3>();
            tempTriX.add(new Vector3(x1,x2,x3));
            mIrisTriX.add(tempTriX);

            //add y positions to array of arrays
            Array<Vector3> tempTriY = new Array<Vector3>();
            tempTriY.add(new Vector3(y1,y2,y3));
            mIrisTriY.add(tempTriY);

        }
    }

    void drawIris(float time) {
        // http://sloppycoding.com/index.php/java-libgdx/iris-wipe/
        if (mIsIrisReload) {
            mIrisCurrentBound = IRIS_OUTERBOUND- (time * (IRIS_OUTERBOUND-IRIS_INNERBOUND) / IRIS_DURATION);
        }
        else
        {
            mIrisCurrentBound = IRIS_INNERBOUND+ (time * (IRIS_OUTERBOUND-IRIS_INNERBOUND) / IRIS_DURATION);
        }
        Gdx.app.debug("DEBUG", "time=" + time + " mIrisCurrentBound=" + mIrisCurrentBound);
        mIrisRenderer.setProjectionMatrix(uiCamera.combined);
        mIrisRenderer.setColor(Color.BLACK);
        mIrisRenderer.begin(ShapeRenderer.ShapeType.Filled);
        float halfScreenWidth = uiCamera.position.x;
        float halfScreenHeight = uiCamera.position.y;

        Color blackColor = Color.BLACK;
        for(int v = 0; v < mIrisTriX.size; v++)
        {
            mIrisRenderer.triangle(mIrisTriX.get(v).get(0).x * mIrisCurrentBound + halfScreenWidth, mIrisTriY.get(v).get(0).x * mIrisCurrentBound + halfScreenHeight,
                    mIrisTriX.get(v).get(0).y * mIrisCurrentBound  + halfScreenWidth, mIrisTriY.get(v).get(0).y * mIrisCurrentBound + halfScreenHeight,
                    mIrisTriX.get(v).get(0).y * IRIS_OUTERBOUND  + halfScreenWidth, mIrisTriY.get(v).get(0).y * IRIS_OUTERBOUND + halfScreenHeight
                    , blackColor, blackColor, blackColor
            );

            mIrisRenderer.triangle(mIrisTriX.get(v).get(0).y * mIrisCurrentBound + halfScreenWidth, mIrisTriY.get(v).get(0).y * mIrisCurrentBound + halfScreenHeight,
                    mIrisTriX.get(v).get(0).y * IRIS_OUTERBOUND  + halfScreenWidth, mIrisTriY.get(v).get(0).y * IRIS_OUTERBOUND + halfScreenHeight,
                    mIrisTriX.get(v).get(0).z * IRIS_OUTERBOUND  + halfScreenWidth, mIrisTriY.get(v).get(0).z * IRIS_OUTERBOUND + halfScreenHeight
                    , blackColor, blackColor, blackColor
            );


        }

        mIrisRenderer.end();
    }

}
