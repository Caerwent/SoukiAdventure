package com.souki.game.adventure.interactions.monsters;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapProperties;
import com.souki.game.adventure.interactions.Interaction;
import com.souki.game.adventure.interactions.InteractionActionType;
import com.souki.game.adventure.interactions.InteractionDef;
import com.souki.game.adventure.interactions.InteractionFactory;
import com.souki.game.adventure.interactions.InteractionMapping;
import com.souki.game.adventure.interactions.InteractionState;
import com.souki.game.adventure.interactions.monsters.InteractionMonsterFireball.Orientation;
import com.souki.game.adventure.map.GameMap;
import com.souki.game.adventure.persistence.GameSession;

import java.util.HashMap;

/**
 * Created by vincent on 17/04/2017.
 */

public class InteractionMonsterFireballGenerator extends Interaction {

    private static final String KEY_STATE = "state";

    protected float mSpeedFactor;
    protected float mDelay=1;
    protected float mLastLaunchedTime=0;
    protected Orientation mOrientation = Orientation.NONE;
    protected int mNbFireballLaunched=0;


    public InteractionMonsterFireballGenerator(InteractionDef aDef, float x, float y, InteractionMapping aMapping, MapProperties aProperties, GameMap aMap) {
        super(aDef, x, y, aMapping, aProperties, aMap);
        mType = Type.MONSTER;

        if (mProperties != null) {
            if (mProperties.containsKey("speedFactor")) {

                mSpeedFactor = Float.valueOf((String) mProperties.get("speedFactor"));

            }
            if (mProperties.containsKey("delay")) {

                mDelay = Float.valueOf((String) mProperties.get("delay"));

            }
            if (mProperties.containsKey("orientation")) {
                mOrientation = Orientation.valueOf((String) mProperties.get("orientation"));
            }


        }
        initialize(x, y, aMapping);


    }

    @Override
    public void restoreFromPersistence(GameSession aGameSession) {
        super.restoreFromPersistence(aGameSession);
        String state = (String) aGameSession.getSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE);
        if (state != null) {
            mCurrentState = getState(state);
        }

    }

    @Override
    public GameSession saveInPersistence(GameSession aGameSession) {
        super.saveInPersistence(aGameSession);
        aGameSession.putSessionDataForMapAndEntity(mMap.getMapName(), mId, KEY_STATE, mCurrentState.name);

        return aGameSession;
    }


    @Override
    protected boolean doAction(InteractionActionType aAction) {
        boolean res = super.doAction(aAction);
        if (!res && aAction != null && InteractionActionType.ActionType.WAKEUP == aAction.type) {
            Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId);

            setState(InteractionState.STATE_ACTIVATED);
            return true;
        }
        if (!res && aAction != null && InteractionActionType.ActionType.SLEEP == aAction.type) {
            Gdx.app.debug("DEBUG", "doAction type=" + aAction.type + " id=" + mId);

            setState(InteractionState.STATE_IDLE);
            return true;
        }
        return res;
    }


    @Override
    public void update(float dt) {
        super.update(dt);
        if(InteractionState.STATE_ACTIVATED.compareTo(mCurrentState.name)==0)
        {
            mLastLaunchedTime+=dt;
            if(mLastLaunchedTime>=mDelay)
            {
                launchFireball();
            }
        }
        else
        {
            mLastLaunchedTime=0;
        }


    }

    protected void launchFireball()
    {
        mLastLaunchedTime=0;
        mNbFireballLaunched++;
        InteractionMapping mapping = new InteractionMapping();
        mapping.persistence = Persistence.NONE;
        mapping.id = getId()+"_"+mNbFireballLaunched;
        mapping.template = "monster_fireball.json";
        mapping.properties = new HashMap();
        mapping.properties.put("speedFactor",""+mSpeedFactor);
        mapping.properties.put("orientation", mOrientation.name());
        mapping.properties.put("hasLight", true);
        mapping.properties.put("lightColor", "#f6af16");
        mapping.properties.put("lightRadius", 1F);
        mapping.properties.put("autoStart", "true");

        InteractionMonsterFireball interaction = (InteractionMonsterFireball) InteractionFactory.getInstance().createInteractionInstance(getX(), getY(), mapping, null, mMap);
        if (interaction != null) {
            interaction.setCamera(mCamera);
            mMap.getInteractions().add(interaction);
            interaction.setPosition(getX()+getShapeRendering().getWidth()/2-interaction.getShapeRendering().getWidth()/2,
                    getY()+getShapeRendering().getHeight()/2-interaction.getShapeRendering().getHeight()/2);
            interaction.startToInteract();
        }
    }


}