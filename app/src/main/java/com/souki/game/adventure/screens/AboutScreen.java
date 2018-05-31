package com.souki.game.adventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.MyGame;
import com.souki.game.adventure.audio.AudioManager;

import static com.souki.game.adventure.Settings.TARGET_HEIGHT;
import static com.souki.game.adventure.Settings.TARGET_WIDTH;

public class AboutScreen implements Screen {

    private Stage _stage;

    public AboutScreen() {

        //creation
        _stage = new Stage(new ExtendViewport(TARGET_WIDTH, TARGET_HEIGHT));
        Table table = new Table();
        table.setFillParent(true);

        Label loadGameLabel = new Label("THE END", GenericUI.getInstance().getSkin());


        table.add(loadGameLabel).spaceBottom(10).row();

        TextButton exitButton = new TextButton(AssetsUtility.getString("ui_exit"), GenericUI.getInstance().getSkin());
        table.add(exitButton).spaceBottom(10).row();
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
        _stage.addActor(table);




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

