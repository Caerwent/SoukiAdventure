package com.souki.game.adventure.interactions;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vincent on 13/02/2017.
 */

public class InteractionMapping {
    public String id;
    public String template;
    public IInteraction.Persistence persistence;
    public ArrayList<InteractionEventAction> eventsAction;
    public ArrayList<InteractionEvent> outputEvents;
    public ArrayList<InteractionQuestAction> questActions;
    public HashMap properties;

}
