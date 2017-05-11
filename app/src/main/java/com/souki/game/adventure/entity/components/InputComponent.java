package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.input.GestureDetector;

/**
 * Created by vincent on 12/01/2017.
 */

public class InputComponent implements Component {

    protected GestureDetector.GestureListener mInputProcessor;

    public InputComponent(GestureDetector.GestureListener aInputProcessor) {
        mInputProcessor = aInputProcessor;
    }

    public boolean tap(float x, float y, int count, int button)
    {
        if (mInputProcessor != null) {
            return mInputProcessor.tap(x, y, count, button);
        } else return false;
    }

}
