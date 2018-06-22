package com.souki.game.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.audio.AudioManager;
import com.souki.game.adventure.persistence.PersistenceProvider;

import static com.souki.game.adventure.Settings.TARGET_HEIGHT;
import static com.souki.game.adventure.Settings.TARGET_WIDTH;

public class AboutScreen implements Screen {

    private static String CREDITS_PATH = "strings/credits_";
    private Stage _stage;

    private ScrollPane mScrollPane;
    private Table mContent;

    public AboutScreen() {

        //creation
        _stage = new Stage(new ExtendViewport(TARGET_WIDTH, TARGET_HEIGHT));
        mContent = new Table();
        mContent.setFillParent(true);

        AssetsUtility.loadLanguage();
        FileHandle file;
        try {
            file = Gdx.files.internal(CREDITS_PATH + PersistenceProvider.getInstance().getSettings().language + ".txt");

        } catch (Exception e) {
            file = null;
        }
        if (file == null) {
            file = Gdx.files.internal(CREDITS_PATH + "fr.txt");
        }
        String textString = file.readString();
        Label textLabel = new Label(textString, GenericUI.getInstance().getSkin());
        textLabel.setWrap(true);
        mScrollPane = new ScrollPane(textLabel, GenericUI.getInstance().getSkin(), "inventoryPane");

        mScrollPane.setScrollingDisabled(true, false);
        mScrollPane.setFadeScrollBars(false);
        mScrollPane.setFlickScroll(true);
        mScrollPane.setForceScroll(false, true);

        mContent.add(mScrollPane).top().left().pad(10).expand().fill().row();


        TextButton exitButton = new TextButton(AssetsUtility.getString("ui_exit"), GenericUI.getInstance().getSkin());
        mContent.add(exitButton).spaceBottom(10).row();
        exitButton.addListener(new ClickListener() {

                                   @Override
                                   public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                                       AudioManager.getInstance().onAudioEvent(AudioManager.UI_CLIC_SOUND);
                                       return true;
                                   }

                                   @Override
                                   public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                                       MyGame.getInstance().setScreen(MyGame.ScreenType.MainMenu);
                                   }

                               }
        );
        _stage.addActor(mContent);


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

}

