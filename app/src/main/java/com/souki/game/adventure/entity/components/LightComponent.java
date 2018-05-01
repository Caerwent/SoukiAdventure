package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class LightComponent implements Component {

    public Vector2 position = new Vector2(0f, 0f);
    public Color color = Color.WHITE;
    public float radius = 6;

    public LightComponent() {

    }

    public void reset() {
        position.set(0.0f, 0.0f);
    }

    public LightComponent setPosition(float x, float y) {
        this.position.set(x, y);
        return this;
    }

    public void setRadius(float aRadius) {
        radius = aRadius;
    }

    public float getRadius() {
        return radius;
    }

    public void setColor(Color aColor) {
        color = aColor;
    }

    public Color getColor() {
        return color;
    }
}
