package com.souki.game.adventure.interactions;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.utils.Json;
import com.souki.game.adventure.interactions.monsters.InteractionMonster1;
import com.souki.game.adventure.interactions.monsters.InteractionMonsterGhost1;
import com.souki.game.adventure.interactions.monsters.InteractionMonsterSlime;
import com.souki.game.adventure.interactions.monsters.InteractionMonsterTank;
import com.souki.game.adventure.interactions.monsters.InteractionWalker;
import com.souki.game.adventure.map.GameMap;

/**
 * Created by vincent on 08/02/2017.
 */

public class InteractionFactory {

    private static InteractionFactory sInstance = new InteractionFactory();

    static public InteractionFactory getInstance() {
        return sInstance;
    }

    private Json mJson;

    public InteractionFactory() {
        mJson = new Json();
        mJson.setIgnoreUnknownFields(true);
    }

    public InteractionHero createInteractionHero(GameMap aMap) {
        InteractionMapping mapping = new InteractionMapping();
        mapping.template = "hero.json";
        mapping.id = "hero";
        InteractionDef def = mJson.fromJson(InteractionDef.class, Gdx.files.internal("data/interactions/" + mapping.template));
        InteractionHero hero = new InteractionHero(def, 0, 0, mapping, null, aMap);

        return hero;

    }

    public IInteraction createInteractionInstance(float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        if (aMapping == null) {
            return null;
        }

        InteractionDef def = mJson.fromJson(InteractionDef.class, Gdx.files.internal("data/interactions/" + aMapping.template));

        if (def.type.compareTo("MONSTER1") == 0) {
            InteractionMonster1 interaction = new InteractionMonster1(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("MONSTER_GHOST1") == 0) {
            InteractionMonsterGhost1 interaction = new InteractionMonsterGhost1(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("MONSTER_SLIME") == 0) {
            InteractionMonsterSlime interaction = new InteractionMonsterSlime(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("MONSTER_TANK") == 0) {
            InteractionMonsterTank interaction = new InteractionMonsterTank(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("ACTIVATOR1") == 0) {
            InteractionActivator interaction = new InteractionActivator(def, x, y, aMapping, aProperties, aMap, InteractionActivator.MODE.NORMAL);
            return interaction;
        } else if (def.type.compareTo("ACTIVATOR_PUSH_PULL") == 0) {
            InteractionActivator interaction = new InteractionActivator(def, x, y, aMapping, aProperties, aMap, InteractionActivator.MODE.PUSH_PULL);
            return interaction;
        } else if (def.type.compareTo("ACTIVATOR_PUSH_ONLY") == 0) {
            InteractionActivator interaction = new InteractionActivator(def, x, y, aMapping, aProperties, aMap, InteractionActivator.MODE.PUSH_ONLY);
            return interaction;
        } else if (def.type.compareTo("CHEST") == 0) {
            InteractionChest interaction = new InteractionChest(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("PORTAL") == 0) {
            InteractionPortal interaction = new InteractionPortal(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("NPC") == 0) {
            InteractionNPC interaction = new InteractionNPC(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("OBSTACLE") == 0) {
            InteractionObstacle interaction = new InteractionObstacle(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("CHALLENGE") == 0) {
            InteractionChallenge interaction = new InteractionChallenge(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        } else if (def.type.compareTo("WALKER") == 0) {
            InteractionWalker interaction = new InteractionWalker(def, x, y, aMapping, aProperties, aMap);
            return interaction;
        }
        return null;
    }
}
