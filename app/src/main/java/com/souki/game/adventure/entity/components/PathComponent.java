package com.souki.game.adventure.entity.components;

import com.badlogic.ashley.core.Component;
import com.souki.game.adventure.box2d.PathHero;

/**
 * Created by vincent on 26/07/2016.
 */

public class PathComponent implements Component {
    public PathHero mPath;


    public PathComponent(PathHero aPath) {
        mPath = aPath;
    }
}
