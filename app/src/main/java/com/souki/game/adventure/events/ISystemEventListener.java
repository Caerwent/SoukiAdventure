package com.souki.game.adventure.events;

import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.map.MapTownPortalInfo;

/**
 * Created by vincent on 27/01/2017.
 */

public interface ISystemEventListener {

    public void onNewMapRequested(String aMapId, MapTownPortalInfo aTownPortalInfo);

    public void onMapReloadRequested(String aMapId, String aFromMapId);

    public void onMapLoaded(GameMap aMap);

    public void onNewSelectedEffect(Effect.Type aEffectType);

    public void onEffectFound(Effect.Type aEffectType);

    public void onNewHelpPage();
}
