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
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.audio.AudioManager;
import com.souki.game.adventure.persistence.LocationProfile;
import com.souki.game.adventure.persistence.PersistenceProvider;
import com.souki.game.adventure.persistence.Profile;

import static com.souki.game.adventure.Settings.TARGET_HEIGHT;
import static com.souki.game.adventure.Settings.TARGET_WIDTH;

/**
 * Created by vincent on 23/01/2017.
 */

public class MainMenuScreen implements Screen {

    private Stage _stage;

    public MainMenuScreen() {

        //creation
        _stage = new Stage(new ExtendViewport(TARGET_WIDTH, TARGET_HEIGHT));
        Table table = new Table();
        table.setFillParent(true);

        TextButton loadGameButton = new TextButton(AssetsUtility.getString("ui_continue_game"), GenericUI.getInstance().getSkin());
        TextButton newGameButton = new TextButton(AssetsUtility.getString("ui_new_game"), GenericUI.getInstance().getSkin());
        TextButton settingsButton = new TextButton(AssetsUtility.getString("ui_settings"), GenericUI.getInstance().getSkin());
        TextButton exitButton = new TextButton(AssetsUtility.getString("ui_exit"), GenericUI.getInstance().getSkin());

        TextButton debugButton = new TextButton("DEBUG", GenericUI.getInstance().getSkin());


        //Layout
        if (PersistenceProvider.isProfileExist()) {
            table.add(loadGameButton).spaceBottom(10).row();
        }
        table.add(newGameButton).spaceBottom(10).row();
        table.add(settingsButton).spaceBottom(10).row();
        table.add(exitButton).spaceBottom(10).row();

        table.add(debugButton).spaceBottom(10).row();

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
                                          }

                                      }
                                  }
        );

        loadGameButton.addListener(new ClickListener() {

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

        exitButton.addListener(new ClickListener() {

                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       Gdx.app.exit();
                                   }

                               }
        );

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

  /*      Quest quest = QuestManager.getInstance().getQuestFromId("quest_malo_blacksmith");
        quest.setActivated(true);
        quest.setCompleted(false);
        for(QuestTask task : quest.getTasks())
        {
            task.setCompleted(false);
        }
        Profile.getInstance().updateQuestProfile("quest_malo_blacksmith", quest);
*/
      /*
       CHANGE LOCATION*/

        LocationProfile locationProfile = new LocationProfile();
        locationProfile.mMapId = "mines4";
        // locationProfile.mFromMapId = aFromMap;

        Profile.getInstance().setLocationProfile(locationProfile);


/*      CHANGE ITEMS*/

 /*       Array<Item> inventory = new Array<Item>();
        ArrayList<String> savedInventory = Profile.getInstance().getInventory();

        if (savedInventory != null) {
            for (String itemId : savedInventory) {
                //if(!Item.ItemTypeID.FoodBread2.name().equals(itemId) && !Item.ItemTypeID.DaggerBroken.name().equals(itemId))
                    inventory.add(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.valueOf(itemId)));
            }
        }
        inventory.add(ItemFactory.getInstance().getInventoryItem(Item.ItemTypeID.MachineActivator));
        Profile.getInstance().updateInventory(inventory);
*/

       /*
       CHANGE MAP ITEM STATE*/
       /*
       MapProfile mapProfile = Profile.getInstance().getMapProfile("mountain8");
        mapProfile.items.remove(Item.ItemTypeID.Coal.name());
        Profile.getInstance().updateMapProfile("mountain8", mapProfile);
        */


       /** CHANGE INTERACTION STATE **/

      /*  GameSession session = Profile.getInstance().getPersistentGameSession();
        session.putSessionDataForMapAndEntity("mountain8", "portal2","state", "ACTIVATED");

        Profile.getInstance().updatePersistentGameSession(session);*/
    }

}




