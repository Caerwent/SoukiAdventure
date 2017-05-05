package com.souki.game.adventure.events;

import com.souki.game.adventure.quests.Quest;
import com.souki.game.adventure.quests.QuestTask;

/**
 * Created by vincent on 13/01/2017.
 */

public interface IQuestListener {
    public void onQuestActivated(Quest aQuest);
    public void onQuestCompleted(Quest aQuest);
    public void onQuestTaskCompleted(Quest aQuest, QuestTask aTask);
}
