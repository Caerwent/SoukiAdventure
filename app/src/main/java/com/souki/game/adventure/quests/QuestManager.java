package com.souki.game.adventure.quests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.dialogs.DialogsManager;
import com.souki.game.adventure.dialogs.GameDialog;
import com.souki.game.adventure.effects.Effect;
import com.souki.game.adventure.events.EventDispatcher;
import com.souki.game.adventure.events.IItemListener;
import com.souki.game.adventure.events.IPlayerListener;
import com.souki.game.adventure.events.IQuestListener;
import com.souki.game.adventure.interactions.InteractionNPC;
import com.souki.game.adventure.items.Item;
import com.souki.game.adventure.items.ItemFactory;
import com.souki.game.adventure.persistence.Profile;
import com.souki.game.adventure.persistence.QuestProfile;
import com.souki.game.adventure.persistence.QuestTaskProfile;
import com.souki.game.adventure.player.Player;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vincent on 13/01/2017.
 */

public class QuestManager implements IItemListener, IQuestListener, IPlayerListener {
    private Json _json = new Json();
    private final String QUEST_LIST = "data/quests/quests.json";
    private static QuestManager _instance = null;

    private HashMap<String, Quest> mQuests = new HashMap<String, Quest>();

    private HashMap<String, Quest> mLivingQuests = new HashMap<String, Quest>();
    private HashMap<String, Quest> mCompletedQuests = new HashMap<String, Quest>();

    private Player mCurrentPlayer;

    public static QuestManager getInstance() {
        if (_instance == null) {
            _instance = new QuestManager();
        }

        return _instance;
    }

    private QuestManager() {
        _json.setIgnoreUnknownFields(true);
        ArrayList<String> list = _json.fromJson(ArrayList.class,
                Gdx.files.internal(QUEST_LIST));
        for (String questFileName : list) {
            Quest theQuest = _json.fromJson(Quest.class,
                    Gdx.files.internal("data/quests/" + questFileName + ".json"));
            // convert strings
            theQuest.setTitle(AssetsUtility.getString(theQuest.getTitle()));
            theQuest.setDescription(AssetsUtility.getString(theQuest.getDescription()));
            mQuests.put(theQuest.id, theQuest);
        }

        restoreQuestsFromProfile();
        EventDispatcher.getInstance().addItemListener(this);
        EventDispatcher.getInstance().addQuestListener(this);
        EventDispatcher.getInstance().addPlayerListener(this);
    }

    public void restoreQuestsFromProfile() {
        mLivingQuests.clear();
        mCompletedQuests.clear();
        for (String entry : mQuests.keySet()) {
            QuestProfile questProfile = Profile.getInstance().getQuestProfile(entry);
            Quest theQuest = mQuests.get(entry);
            if (questProfile != null && theQuest != null) {
                for (QuestTaskProfile taskProfile : questProfile.tasks) {
                    QuestTask theTask = theQuest.getTaskById(taskProfile.id);
                    if (theTask != null) {
                        theTask.setCompleted(taskProfile.isCompleted);
                    }

                }
                theQuest.setActivated(questProfile.isActivated);
                theQuest.setCompleted(questProfile.isCompleted);
                if (theQuest.isCompleted()) {
                    mCompletedQuests.put(theQuest.getId(), theQuest);
                } else if (theQuest.isActivated()) {
                    mLivingQuests.put(theQuest.getId(), theQuest);
                }
            }


        }
    }

    public Quest getQuestFromId(String aId) {
        return mQuests.get(aId);
    }

    public Quest getLivingQuestFromId(String aId) {
        return mLivingQuests.get(aId);
    }


    public void onDialogEnd(GameDialog aDialog) {

        Quest[] quests = new Quest[mLivingQuests.values().size()];
        quests = mLivingQuests.values().toArray(quests);
        for (int i = 0; i < quests.length; i++) {
            if (quests[i].isActivated() && !quests[i].isCompleted()) {
                for (QuestTask task : quests[i].getTasks()) {
                    if (!task.isCompleted()) {
                        if (task.getType() == QuestTask.TypeTask.DIALOG) {
                            if (task.getTargetId() != null && task.getTargetId().equals(aDialog.getId())) {
                                if (quests[i].isTaskDependenciesCompleted(task)) {
                                    task.setCompleted(true);
                                    Gdx.app.debug("QestManager", "onDialogEnd quest " + quests[i].getId() + " task " + task.getType() + " completed " + task.getId());
                                    //check if quest is completed
                                    quests[i].computeCompleted();
                                    if (quests[i].isCompleted()) {
                                        internalQuestcompleted(quests[i]);
                                    }
                                    EventDispatcher.getInstance().onQuestTaskCompleted(quests[i], task);
                                }
                            }

                        }

                    }
                }
            }
        }
    }

    public void onNPC(InteractionNPC aNPC) {
        Quest[] quests = new Quest[mLivingQuests.values().size()];
        quests = mLivingQuests.values().toArray(quests);
        for (int i = 0; i < quests.length; i++) {
            if (quests[i].isActivated() && !quests[i].isCompleted()) {
                for (QuestTask task : quests[i].getTasks()) {
                    if (!task.isCompleted()) {
                        if (task.getTargetId() != null && task.getTargetId().equals(aNPC.getId())) {
                            if (task.getType() == QuestTask.TypeTask.RETURN_ITEM) {
                                // check items not already be found before talking with npj
                                checkItemFoundTask(quests[i]);
                                if (quests[i].isTaskDependenciesCompleted(task)) {
                                    task.setCompleted(true);
                                    Gdx.app.debug("QestManager", "onNPC quest " + quests[i].getId() + " task " + task.getType() + " completed " + task.getId());

                                    quests[i].computeCompleted();
                                    if (quests[i].isCompleted()) {
                                        internalQuestcompleted(quests[i]);
                                    }
                                    EventDispatcher.getInstance().onQuestTaskCompleted(quests[i], task);
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @Override
    public void onItemFound(Item aItem) {
        if (mCurrentPlayer == null)
            return;
        Quest[] quests = new Quest[mLivingQuests.values().size()];
        quests = mLivingQuests.values().toArray(quests);
        for (int i = 0; i < quests.length; i++) {
            if (quests[i].isActivated() && !quests[i].isCompleted()) {
                for (QuestTask task : quests[i].getTasks()) {
                    if (!task.isCompleted()) {
                        if (task.getTargetId() != null && task.getTargetId().equals(aItem.getItemTypeID().name())) {
                            if (task.getType() == QuestTask.TypeTask.FIND_ITEM) {
                                Array<Item> foundItems = mCurrentPlayer.getItemsInventoryById(task.getTargetId());
                                int nbItemInInventory = foundItems.size;
                                if(nbItemInInventory<=0)
                                {
                                    nbItemInInventory = task.getTargetId().equals(aItem.getItemTypeID().name()) ? 1 : 0;
                                }
                                int nbItem = 1;
                                if (task.getValue() != null) {
                                    nbItem = Integer.valueOf(task.getValue());
                                }
                                if (nbItemInInventory >= nbItem) {
                                    if (quests[i].isTaskDependenciesCompleted(task)) {
                                        task.setCompleted(true);
                                        Gdx.app.debug("QestManager", "onItemFound quest " + quests[i].getId() + " task " + task.getType() + " completed " + task.getId());

                                        quests[i].computeCompleted();
                                        if (quests[i].isCompleted()) {
                                            internalQuestcompleted(quests[i]);
                                        }
                                        EventDispatcher.getInstance().onQuestTaskCompleted(quests[i], task);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkItemFoundTask(Quest aQuest) {
        if (mCurrentPlayer == null)
            return;
        for (QuestTask task : aQuest.getTasks()) {
            if (!task.isCompleted() && task.getType() == QuestTask.TypeTask.FIND_ITEM) {
                Array<Item> foundItems = mCurrentPlayer.getItemsInventoryById(task.getTargetId());
                int nbItem = 1;
                if (task.getValue() != null) {
                    nbItem = Integer.valueOf(task.getValue());
                }
                if (foundItems.size >= nbItem) {
                    if (aQuest.isTaskDependenciesCompleted(task)) {
                        task.setCompleted(true);
                        Profile.getInstance().updateQuestProfile(aQuest.getId(), aQuest);
                    }
                }
            }

        }
    }

    private void updateItemsFromFoundTask(Quest aQuest) {
        if (mCurrentPlayer == null)
            return;
        for (QuestTask task : aQuest.getTasks()) {
            if (task.isCompleted() && task.getType() == QuestTask.TypeTask.FIND_ITEM) {
                Array<Item> foundItems = mCurrentPlayer.getItemsInventoryById(task.getTargetId());
                for (int i = 0; i < foundItems.size; i++) {
                    EventDispatcher.getInstance().onItemLost(foundItems.get(i));
                }
            }

        }
    }

    @Override
    public void onItemLost(Item aItem) {

    }

    @Override
    public void onQuestActivated(Quest aQuest) {
        if (!mLivingQuests.containsKey(aQuest.getId())) {
            mLivingQuests.put(aQuest.getId(), aQuest);
            if (aQuest.getStartQuestDialogId() != null) {
                EventDispatcher.getInstance().onStartDialog(DialogsManager.getInstance().getDialog(aQuest.getStartQuestDialogId()));
            }
        }
        Profile.getInstance().updateQuestProfile(aQuest.getId(), aQuest);
    }

    public void activateQuestIfAllDependenciesCompleted(Quest aQuest) {
        boolean isAllDependenciesCompleted = true;

        if(aQuest.getRequiredCompletedQuest() != null) {
            for (String requiredId : aQuest.getRequiredCompletedQuest()) {
                Quest dependenciyQuest = mQuests.get(requiredId);
                if (!dependenciyQuest.isCompleted()) {
                    isAllDependenciesCompleted = false;
                    break;
                }
            }
        }
        if (isAllDependenciesCompleted) {
            aQuest.setActivated(true);
            EventDispatcher.getInstance().onQuestActivated(aQuest);
        }
    }

    private void internalQuestcompleted(Quest aQuest) {
        Gdx.app.debug("QestManager", "quest completed " + aQuest.getId());

        updateItemsFromFoundTask(aQuest);
        if (aQuest.getItemsReward() != null) {
            for (Item.ItemTypeID itemID : aQuest.getItemsReward()) {
                EventDispatcher.getInstance().onItemFound(ItemFactory.getInstance().getInventoryItem(itemID));
            }
        }
        if (aQuest.getEffectsReward() != null) {
            for (Effect.Type effectType : aQuest.getEffectsReward()) {
                EventDispatcher.getInstance().onEffectFound(effectType);
            }
        }
        EventDispatcher.getInstance().onGameEvent(aQuest.getId());

        for (Quest quest : mQuests.values()) {
            if (!mLivingQuests.containsKey(quest.getId()) &&
                    !mCompletedQuests.containsKey(quest.getId()) &&
                    quest.getRequiredCompletedQuest() != null  &&
                    quest.getRequiredCompletedQuest().size()>0) {
                activateQuestIfAllDependenciesCompleted(quest);


            }
        }

        EventDispatcher.getInstance().onQuestCompleted(aQuest);
        if (mLivingQuests.containsKey(aQuest.getId())) {
            mLivingQuests.remove(aQuest.getId());
        }
        if (!mCompletedQuests.containsKey(aQuest.getId())) {
            mCompletedQuests.put(aQuest.getId(), aQuest);
        }
        aQuest.setActivated(false);
        Profile.getInstance().updateQuestProfile(aQuest.getId(), aQuest);

    }

    @Override
    public void onQuestCompleted(Quest aQuest) {

    }

    @Override
    public void onQuestTaskCompleted(Quest aQuest, QuestTask aTask) {


    }

    @Override
    public void onInventoryChanged(Player aPlayer) {
        mCurrentPlayer = aPlayer;

    }
}
