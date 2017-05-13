package com.souki.game.adventure.screens;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Application;
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
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.souki.game.adventure.CharacterMoveController3;
import com.souki.game.adventure.MyGame;
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
    public Rectangle viewport;
    public int orientation;

    private GameMap map;

    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    //private OrthoCamController cameraController;
    private CharacterMoveController3 bobController;
    private AssetManager assetManager;
    private BitmapFont font;
    private SpriteBatch batch;
    ShapeRenderer pathRenderer;
    private ShapeRenderer mPathSpotRenderer;
    private Shape mSpot;
    private double accumulator;
    private double currentTime;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;

    private float physicsDeltaTime = 1.0f / 60.0f;

    InputMultiplexer mInputMultiplexer = new InputMultiplexer();


    public GameScreen() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        float worldViewPortHeight = 15/ (h / TARGET_HEIGHT);
        camera.setToOrtho(false, (w / h) * 10, 10);
        //camera.zoom = 2;
        camera.update();

        //cameraController = new OrthoCamController(camera);

        uiCamera = new OrthographicCamera(TARGET_WIDTH, TARGET_HEIGHT);

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
        bobController = new CharacterMoveController3(camera);
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

    public void loadMap(String aTargetMapId, String aFromMapId, MapTownPortalInfo aTownPortalInfo) {
        if (map != null) {
            map.playMusic(false);
            map.destroy();
            EntityEngine.destroyInstance();
        }
        map = null;

        EntityEngine.createInstance();
        EntityEngine.getInstance().addSystem(new MovementSystem());
        EntityEngine.getInstance().addSystem(new CollisionObstacleSystem());
        EntityEngine.getInstance().addSystem(new InteractionSystem());
        EntityEngine.getInstance().addSystem(new CollisionInteractionSystem());
        //  EntityEngine.getInstance().addSystem(new PathRenderSystem(pathRenderer));
        map = new GameMap(aTargetMapId, aFromMapId, camera, aTownPortalInfo);
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

    @Override
    public void render(float delta) {
       /* double newTime = TimeUtils.millis() / 1000.0;
        double frameTime = Math.min(newTime - currentTime, 0.25);
        float deltaTime = (float) frameTime;

        currentTime = newTime;*/
        UIStage.getInstance().act(delta);
        // set viewport
        //    Gdx.gl.glViewport((int) viewport.x, (int) viewport.y,
        //            (int) viewport.width, (int) viewport.height);

        // clear previous frame
        //Gdx.gl.glClearColor(100f / 255f, 100f / 255f, 250f / 255f, 1f);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (EntityEngine.getInstance() != null) {
            EntityEngine.getInstance().
                    update(delta/*Time*/);
        }

        if (map != null)
            map.render();
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


        UIStage.getInstance().

                draw();


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
                MyGame.getInstance().setScreen(MyGame.ScreenType.MainMenu);
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
        Gdx.app.debug("DEBUG", "touchX=" + curr.x + " touchY=" + curr.y);
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
