package com.souki.game.adventure;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.souki.game.adventure.dialogs.DialogsManager;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.ISystemEventListener;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.map.MapTownPortalInfo;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.persistence.LocationProfile;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.quests.QuestManager;
import com.souki.game.adventure.screens.AboutScreen;
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
    final public static float SCALE_FACTOR = 1.0F / 32.0F;
    final public static String DEFAULT_MAP_NAME = "village";
    final protected static String[] DEFAULT_MAP_NAME_OR_ASSOCIATED = new String[]{DEFAULT_MAP_NAME, "home", "house1", "house2", "house3", "village_tower1", "village_tower2"};
    final public static String INIT_MAP_START = "home";
    final public static String FINAL_MAP = "village_final";
    final public static String ENDED_MAP = "ended";
    final public static String QUEST_START_ID = "quest_start";
    final public static String QUEST_EFFECT_ID = "quest_obtain_portal";
    final public static String QUEST_HELP_BOOK_ID = "quest_help_book";
    final public static String QUEST_FINAL_ID = "quest_final";
    final public static String FINAL_DIALOG_ID = "final_finished_dialog";
    final public static String FINAL_PORTAL_TARGET_ID = "FINAL_PORTAL_TARGET";
    final public static String ENDED_DIALOG_ID = "thats_all_folks_dialog";

    static private MyGame s_instance;

    private boolean mIsGameReadyToBeShown = true;
    private Screen mScreenRequested = null;
    private Screen mCurrentScreen = null;

    /**
     * check if a mapname is the default map or associated (house from village)
     *
     * @param aMapName the map name to test
     * @return true if it's a default map name or associated
     */
    public boolean isDefaultMapOrAssociated(String aMapName) {
        if (aMapName == null || aMapName.isEmpty())
            return false;
        for (String name : DEFAULT_MAP_NAME_OR_ASSOCIATED) {
            if (aMapName.compareTo(name) == 0)
                return true;
        }

        return false;
    }

    @Override
    public void onNewMapRequested(String aMapId, MapTownPortalInfo aTownPortalInfo) {
        if (mGameScreen != null) {
            mScreenRequested = mLoadingScreen;
            mIsGameReadyToBeShown = false;
            if (aMapId != null && aMapId.compareTo(FINAL_PORTAL_TARGET_ID) == 0) {
                // unload current map
                mGameScreen.loadMap(null, mGameScreen.getMap().getMapName(), aTownPortalInfo);
            } else {
                mGameScreen.loadMap(aMapId, mGameScreen.getMap().getMapName(), aTownPortalInfo);
            }

        }
    }

    @Override
    public void onMapReloadRequested(String aMapId, String aFromMapId) {
        if (mGameScreen != null) {
            mScreenRequested = mLoadingScreen;
            mIsGameReadyToBeShown = false;
            mGameScreen.loadMap(aMapId, aFromMapId, null);
        }
    }

    @Override
    public void onMapLoaded(GameMap aMap) {
        mScreenRequested = mGameScreen;
        if (aMap != null && aMap.getMapName().compareTo(FINAL_MAP) == 0) {
            EventDispatcher.getInstance().onStartDialog(DialogsManager.getInstance().getDialog(FINAL_DIALOG_ID));
        } else if (aMap != null && aMap.getMapName().compareTo(ENDED_MAP) == 0) {
            EventDispatcher.getInstance().onStartDialog(DialogsManager.getInstance().getDialog(ENDED_DIALOG_ID));
        }
    }

    @Override
    public void onMapUnloaded(String aMapName) {
        if (aMapName != null && aMapName.compareTo(FINAL_MAP) == 0) {
            Profile.getInstance().setGameMode(Profile.GAME_MODE.MODE_ENDED);
            setScreen(MyGame.ScreenType.About);
        }
    }

    @Override
    public void onNewSelectedEffect(Effect.Type aEffectType) {
        Profile.getInstance().setSelectedEffect(aEffectType);
    }

    @Override
    public void onEffectFound(Effect.Type aEffectType) {
        Profile.getInstance().getAvailableEffects().add(aEffectType);
    }

    @Override
    public void onNewHelpPage() {

    }

    public static enum ScreenType {
        MainMenu,
        MainGame,
        LoadingGame,
        Settings,
        About
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
            case About:
                mScreenRequested = mAboutScreen;
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
            case About:
                return mAboutScreen;
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
    private AboutScreen mAboutScreen;

    public void create() {
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        s_instance = this;
        GenericUI.createInstance();
        batch = new SpriteBatch();
        //Use LibGDX's default Arial font.
        font = new BitmapFont();
        mMainMenuScreen = new MainMenuScreen();
        mLoadingScreen = new LoadingScreen();
        mSettingsScreen = new SettingsScreen();
        mAboutScreen = new AboutScreen();
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
        if (mCurrentScreen != mGameScreen && mGameScreen != null && mGameScreen.isUnloading()) {
            mGameScreen.render(Gdx.graphics.getDeltaTime());
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
        if (Profile.getInstance().getGameMode() == Profile.GAME_MODE.MODE_ENDED) {
            mGameScreen.loadMap(ENDED_MAP, null, null);
        } else if (Profile.getInstance().getGameMode() == Profile.GAME_MODE.MODE_FINAL) {
            mGameScreen.loadMap(FINAL_MAP, null, null);
        } else {
            LocationProfile location = Profile.getInstance().getLocationProfile();

            if (location == null || location.mMapId == null) {
                mGameScreen.loadMap(INIT_MAP_START, null, null);
            } else {
                mGameScreen.loadMap(location.mMapId, location.mFromMapId, null);
            }
        }

    }

    public void dispose() {

        EventDispatcher.getInstance().removeSystemEventListener(this);
        batch.dispose();
        font.dispose();
        mMainMenuScreen.dispose();
        mLoadingScreen.dispose();
        mSettingsScreen.dispose();
        mAboutScreen.dispose();
        if (mGameScreen != null) {
            mGameScreen.dispose();
            mGameScreen = null;
        }
    }

    public void exit() {
        dispose();
        Gdx.app.exit();
    }


}
