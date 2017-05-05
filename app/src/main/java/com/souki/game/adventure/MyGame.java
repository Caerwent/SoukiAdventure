package com.souki.game.adventure;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.ISystemEventListener;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.map.MapTownPortalInfo;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.persistence.LocationProfile;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.quests.QuestManager;
import com.souki.game.adventure.screens.GameScreen;
import com.souki.game.adventure.screens.GenericUI;
import com.souki.game.adventure.screens.LoadingScreen;
import com.souki.game.adventure.screens.MainMenuScreen;
import com.souki.game.adventure.screens.SettingsScreen;

/**
 * MyGame class that extends Game, which implements
 * ApplicationListener. It will be used as the "Main" libGDX class, the starting
 * point basically, in the core libGDX project. Its VIEWPORT and BATCHER are
 * used by the all screens. The Viewport is updated when the device's
 * orientation is changed. The Batcher is created once since it is memory
 * expensive.
 */
public class MyGame extends Game implements ISystemEventListener {
    public static float SCALE_FACTOR = 1.0F / 32.0F;
    public static String DEFAULT_MAP_NAME = "village";
    public static String INIT_MAP_START = "home";
    public static String QUEST_START_ID = "quest_start";

    static private MyGame s_instance;

    private boolean mIsGameReadyToBeShown = true;
    private Screen mScreenRequested = null;
    private Screen mCurrentScreen = null;

    @Override
    public void onNewMapRequested(String aMapId, MapTownPortalInfo aTownPortalInfo) {
        if (mGameScreen != null) {
            mScreenRequested = mLoadingScreen;
            mIsGameReadyToBeShown = false;
            mGameScreen.loadMap(aMapId, mGameScreen.getMap().getMapName(), aTownPortalInfo);
        }
    }

    @Override
    public void onMapReloadRequested(String aMapId, String aFromMapId) {
        if (mGameScreen != null) {
            mScreenRequested = mLoadingScreen;
            mIsGameReadyToBeShown = false;
            mGameScreen.loadMap(aMapId, aFromMapId,null);
        }
    }

    @Override
    public void onMapLoaded(GameMap aMap) {
        mScreenRequested = mGameScreen;
    }

    @Override
    public void onNewSelectedEffect(Effect.Type aEffectType) {
        Profile.getInstance().setSelectedEffect(aEffectType);
    }

    @Override
    public void onEffectFound(Effect.Type aEffectType)
    {
        Profile.getInstance().getAvailableEffects().add(aEffectType);
    }

    public static enum ScreenType {
        MainMenu,
        MainGame,
        LoadingGame,
        Settings
    }

    public void setScreen(ScreenType screenType) {

        switch (screenType) {
            case MainMenu:
                mScreenRequested = mMainMenuScreen;
                break;
            case LoadingGame:
                mScreenRequested = mLoadingScreen;
                break;
            case Settings:
                mScreenRequested = mSettingsScreen;
                break;
            case MainGame:

                if (mGameScreen == null) {
                    mScreenRequested = mLoadingScreen;
                    mGameScreen = new GameScreen();
                    loadDefaultMap();
                } else {
                    mScreenRequested = mGameScreen;

                }
                break;

            default: {
                mScreenRequested = mMainMenuScreen;

            }
        }
    }

    public Screen getScreenType(ScreenType screenType) {

        switch (screenType) {
            case MainMenu:
                return mMainMenuScreen;
            case LoadingGame:
                return mLoadingScreen;
            case MainGame:
                return mGameScreen;
            case Settings:
                return mSettingsScreen;
            default: {
                return mMainMenuScreen;
            }
        }

    }

    public static MyGame getInstance() {
        return s_instance;
    }

    public SpriteBatch batch;
    public BitmapFont font;
    private MainMenuScreen mMainMenuScreen;
    private LoadingScreen mLoadingScreen;
    private GameScreen mGameScreen;
    private SettingsScreen mSettingsScreen;

    public void create() {
        s_instance = this;
        GenericUI.createInstance();
        batch = new SpriteBatch();
        //Use LibGDX's default Arial font.
        font = new BitmapFont();
        mMainMenuScreen = new MainMenuScreen();
        mLoadingScreen = new LoadingScreen();
        mSettingsScreen = new SettingsScreen();
        mCurrentScreen = mMainMenuScreen;
        mScreenRequested = mMainMenuScreen;

        setScreen(mMainMenuScreen);


        EventDispatcher.getInstance().addSystemEventListener(this);
    }

    public void render() {
        super.render(); //important!
        if (mCurrentScreen != mScreenRequested) {
            mCurrentScreen = mScreenRequested;
            setScreen(mCurrentScreen);
        }

    }

    public void newProfile() {
        GameSession.createNewSession();
        Profile.newProfile();
        QuestManager.getInstance().restoreQuestsFromProfile();
        if (mGameScreen != null)
            mGameScreen.dispose();
        mGameScreen = new GameScreen();
        loadDefaultMap();
    }

    private void loadDefaultMap() {
        LocationProfile location = Profile.getInstance().getLocationProfile();
        if (location == null || location.mMapId == null) {
            mGameScreen.loadMap(INIT_MAP_START, null, null);
        } else {
            mGameScreen.loadMap(location.mMapId, location.mFromMapId, null);
        }
    }

    public void dispose() {

        EventDispatcher.getInstance().removeSystemEventListener(this);
        batch.dispose();
        font.dispose();
        mMainMenuScreen.dispose();
        mLoadingScreen.dispose();
        mSettingsScreen.dispose();
        if (mGameScreen != null) {
            mGameScreen.dispose();
            mGameScreen = null;
        }
    }


}
