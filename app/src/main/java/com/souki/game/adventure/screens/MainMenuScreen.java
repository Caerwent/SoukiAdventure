package com.souki.game.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.audio.AudioManager;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.persistence.PersistenceProvider;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.quests.Quest;
import com.souki.game.adventure.quests.QuestManager;
import com.souki.game.adventure.quests.QuestTask;

import java.util.ArrayList;

import static com.souki.game.adventure.Settings.TARGET_HEIGHT;
import static com.souki.game.adventure.Settings.TARGET_WIDTH;

/**
 * Created by vincent on 23/01/2017.
 */

public class MainMenuScreen implements Screen {

    private Stage _stage;

    private TextButton mLoadGameButton;

    public MainMenuScreen() {

        //creation
        _stage = new Stage(new ExtendViewport(TARGET_WIDTH, TARGET_HEIGHT));
        Table table = new Table();
        table.setFillParent(true);

        mLoadGameButton = new TextButton(AssetsUtility.getString("ui_continue_game"), GenericUI.getInstance().getSkin());
        TextButton newGameButton = new TextButton(AssetsUtility.getString("ui_new_game"), GenericUI.getInstance().getSkin());
        TextButton settingsButton = new TextButton(AssetsUtility.getString("ui_settings"), GenericUI.getInstance().getSkin());
        TextButton exitButton = new TextButton(AssetsUtility.getString("ui_exit"), GenericUI.getInstance().getSkin());
        TextButton aboutButton = new TextButton(AssetsUtility.getString("ui_about"), GenericUI.getInstance().getSkin());

        TextButton debugButton = new TextButton("DEBUG", GenericUI.getInstance().getSkin());


        //Layout

        table.add(mLoadGameButton).spaceBottom(10).row();
        if (!PersistenceProvider.isProfileExist()) {
            mLoadGameButton.setVisible(false);
        }
        table.add(newGameButton).spaceBottom(10).row();
        table.add(settingsButton).spaceBottom(10).row();
        table.add(aboutButton).spaceBottom(10).row();

        table.add(debugButton).spaceBottom(10).row();

        table.add(exitButton).spaceBottom(10).row();


        _stage.addActor(table);

        //Listeners
        newGameButton.addListener(new ClickListener() {
                                      @Override
                                      public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                          AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                          return true;
                                      }

                                      @Override
                                      public void touchUp(InputEvent event, float x, float y, int pointer, int button) {

                                          if (PersistenceProvider.getInstance().hasProfile()) {
                                              new Dialog("", GenericUI.getInstance().getSkin(), "dialog") {
                                                  protected void result(Object object) {
                                                      if (object instanceof Boolean) {
                                                          if (((Boolean) object).booleanValue()) {
                                                              MyGame.getInstance().newProfile();
                                                              MyGame.getInstance().setScreen(MyGame.ScreenType.MainGame);
                                                          }
                                                      }
                                                  }
                                              }.text(AssetsUtility.getString("ui_dialog_new_profile_msg")).
                                                      button(AssetsUtility.getString("ui_dialog_continue"), true).
                                                      button(AssetsUtility.getString("ui_dialog_cancel"), false).key(Input.Keys.ENTER, true)
                                                      .key(Input.Keys.ESCAPE, false).show(_stage);
                                          } else {
                                              MyGame.getInstance().newProfile();
                                              MyGame.getInstance().setScreen(MyGame.ScreenType.MainGame);
                                              mLoadGameButton.setVisible(true);
                                          }

                                      }
                                  }
        );

        mLoadGameButton.addListener(new ClickListener() {

                                        @Override
                                        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                            AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                            return true;
                                        }

                                        @Override
                                        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                            MyGame.getInstance().setScreen(MyGame.ScreenType.MainGame);
                                        }
                                    }
        );
        settingsButton.addListener(new ClickListener() {

                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           MyGame.getInstance().setScreen(MyGame.ScreenType.Settings);
                                       }
                                   }
        );
        aboutButton.addListener(new ClickListener() {

                                       @Override
                                       public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                           AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                           return true;
                                       }

                                       @Override
                                       public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                           MyGame.getInstance().setScreen(MyGame.ScreenType.About);
                                       }
                                   }
        );

        exitButton.addListener(new ClickListener() {

                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       MyGame.getInstance().exit();
                                   }

                               }
        );
        debugButton.setVisible(false);
        debugButton.addListener(new ClickListener() {

                                    @Override
                                    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                        AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                        return true;
                                    }

                                    @Override
                                    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                        onDebug();
                                    }
                                }
        );


    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        _stage.act(delta);
        _stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        _stage.getViewport().setScreenSize(width, height);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(_stage);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        _stage.dispose();
    }

    private void onDebug() {

        /*CHANGE QUEST STATE*/

        Quest quest = QuestManager.getInstance().getQuestFromId("quest_malo");
        quest.setActivated(false);
        quest.setCompleted(false);
        for (QuestTask task : quest.getTasks()) {
            task.setCompleted(false);
        }
        Profile.getInstance().updateQuestProfile("quest_malo", quest);

      //  Profile.getInstance().setGameMode(Profile.GAME_MODE.MODE_NORMAL);

        /*CHANGE LOCATION*/

    /*    LocationProfile locationProfile = new LocationProfile();
        locationProfile.mMapId = "east_land3";
        // locationProfile.mFromMapId = aFromMap;

        Profile.getInstance().setLocationProfile(locationProfile);
*/

        /* CHANGE ITEMS*/

       Array<Item> inventory = new Array<Item>();
        ArrayList<String> savedInventory = Profile.getInstance().getInventory();

        if (savedInventory != null) {
            for (String itemId : savedInventory) {
               // if (!Item.ItemTypeID.JewelsBlue.name().equals(itemId) &&
               //         !Item.ItemTypeID.JewelsRed.name().equals(itemId) &&
               //         !Item.ItemTypeID.CrystalsGreen.name().equals(itemId) &&
               //         !Item.ItemTypeID.CrystalsPurple.name().equals(itemId) &&
               //         !Item.ItemTypeID.KeyIron.name().equals(itemId) &&
               //         !Item.ItemTypeID.Coal.name().equals(itemId) && !Item.ItemTypeID.Gear.name().equals(itemId))
                    inventory.add(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(itemId)));
            }
        }
        inventory.add(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.Hammer));
        Profile.getInstance().updateInventory(inventory);


        /*CHANGE MAP ITEM STATE*/

  /*      MapProfile mapProfile = Profile.getInstance().getMapProfile("mines5_1");
        if (mapProfile != null && mapProfile.items != null) {
            mapProfile.items.remove(Item.ItemTypeID.JewelsBlue.name());
            mapProfile.items.remove(Item.ItemTypeID.Gear.name());
            mapProfile.items.remove(Item.ItemTypeID.Coal.name());
            Profile.getInstance().updateMapProfile("mines5_1", mapProfile);
        }
*/

/** CHANGE INTERACTION STATE **/

    /*    GameSession session = Profile.getInstance().getPersistentGameSession();
        session.putSessionDataForMapAndEntity("east_land3", "portal2", "state", "IDLE");


        Profile.getInstance().updatePersistentGameSession(session);*/
    }

}




