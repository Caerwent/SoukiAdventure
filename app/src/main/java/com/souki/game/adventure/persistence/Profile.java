package com.souki.game.adventure.persistence;

import com.badlogic.gdx.utils.Array;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.quests.Quest;
import com.souki.game.adventure.quests.QuestTask;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vincent on 20/01/2017.
 */

public class Profile {

    public static enum GAME_MODE {
        MODE_NORMAL,
        MODE_FINAL,
        MODE_ENDED
    }
    HashMap<String, MapProfile> maps = new HashMap<>();
    ArrayList<String> inventory = new ArrayList<>();
    ArrayList<String> foundScrolls = new ArrayList<>();
    HashMap<String, QuestProfile> quests = new HashMap<>();
    LocationProfile location = new LocationProfile();
    Effect.Type mSelectedEffect = null;
    ArrayList<Effect.Type> mAvailableEffects = new ArrayList();
    GameSession mPersistentGameSession = new GameSession();
    ArrayList<String>  mPortalCheckpointMap = new ArrayList<>();
    GAME_MODE mGameMode = GAME_MODE.MODE_NORMAL;

    static private Profile sProfile;

    public static Profile getInstance() {

        if (sProfile == null) {
            sProfile = PersistenceProvider.getInstance().loadProfile();
            GameSession.createNewSession();
        }
        return sProfile;
    }

    public final MapProfile getMapProfile(String aMapKey) {
        return maps.get(aMapKey);
    }

    public void updateMapProfile(String aMapName, MapProfile aMapProfile) {
        maps.put(aMapName, aMapProfile);
        PersistenceProvider.getInstance().saveMapProfile(aMapName, aMapProfile);
    }

    public static synchronized void newProfile() {
        sProfile = new Profile();
        GameSession.createNewSession();
        PersistenceProvider.getInstance().save(sProfile);
    }

    public final QuestProfile getQuestProfile(String aQuestKey) {
        return quests.get(aQuestKey);
    }

    public void updateQuestProfile(String aQuestKey, Quest aQuest) {
        QuestProfile questProfile = new QuestProfile();
        questProfile.isActivated = aQuest.isActivated();
        questProfile.isCompleted = aQuest.isCompleted();
        for (QuestTask task : aQuest.getTasks()) {
            QuestTaskProfile taskProfile = new QuestTaskProfile();
            taskProfile.id = task.getId();
            taskProfile.isCompleted = task.isCompleted();
            questProfile.tasks.add(taskProfile);
        }
        quests.put(aQuestKey, questProfile);
        PersistenceProvider.getInstance().saveQuestProfile(aQuestKey, questProfile);

    }

    public final ArrayList<String> getInventory() {
        return inventory;
    }

    public void updateInventory(Array<Item> aInventory) {
        inventory.clear();
        for (Item item : aInventory) {
            inventory.add(item.getItemTypeID().name());
        }
        PersistenceProvider.getInstance().saveInventory(inventory);

    }
    public final ArrayList<String> getFoundScrolls() {
        return foundScrolls;
    }

    public void updateFoundScrolls(ArrayList<String> aFoundScrolls) {
        if(aFoundScrolls!=foundScrolls) {
            foundScrolls.clear();
            for (String item : aFoundScrolls) {
                foundScrolls.add(item);
            }
        }
        PersistenceProvider.getInstance().saveFoundScrolls(foundScrolls);

    }

    public LocationProfile getLocationProfile()
    {
        return location;
    }

    public void setLocationProfile(LocationProfile aLocation)
    {
        location = aLocation;
        PersistenceProvider.getInstance().saveLocationProfile(location);
    }

    public Effect.Type getSelectedEffect()
    {
        return mSelectedEffect;
    }

    public void setSelectedEffect(Effect.Type aSelectedEffect)
    {
        mSelectedEffect = aSelectedEffect;
        PersistenceProvider.getInstance().saveSelectedEffect(mSelectedEffect);

    }

    public final ArrayList<Effect.Type> getAvailableEffects() {
        return mAvailableEffects;
    }

    public void updateAvailableEffects(ArrayList<Effect.Type> aEffectsList) {
        mAvailableEffects=aEffectsList;
        PersistenceProvider.getInstance().saveEffectsList(mAvailableEffects);

    }

    public GameSession getPersistentGameSession()
    {
        return mPersistentGameSession;
    }

    public void updatePersistentGameSession(GameSession aGameSession)
    {
        mPersistentGameSession = aGameSession;
        PersistenceProvider.getInstance().saveGameSession(mPersistentGameSession);
    }

    public final ArrayList<String>  getPortalCheckpoints() {
        return mPortalCheckpointMap;
    }

    public void updatePortalCheckpoints(ArrayList<String>  PortalCheckpoints) {
        mPortalCheckpointMap=PortalCheckpoints;
        PersistenceProvider.getInstance().savePortalCheckpoints(mPortalCheckpointMap);

    }

    public final GAME_MODE getGameMode()
    {
        return mGameMode;
    }

    public void setGameMode(GAME_MODE aGameMode)
    {
        mGameMode = aGameMode;
        PersistenceProvider.getInstance().saveGameMode(mGameMode);
    }

}
