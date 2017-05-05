package com.souki.game.adventure.entity;

import com.badlogic.ashley.core.Engine;

/**
 * Created by vincent on 07/07/2016.
 */

public class EntityEngine {

    static private Engine s_engine;

    public static void createInstance()
    {
        s_engine = new Engine();
    }
    public static Engine getInstance() {
        return s_engine;
    }
    public static void destroyInstance()
    {
        if(s_engine!=null)
        {
            s_engine.removeAllEntities();
            s_engine=null;
        }
    }

}
