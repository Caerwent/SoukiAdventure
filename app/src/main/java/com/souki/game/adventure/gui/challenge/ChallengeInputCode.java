package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.screens.GenericUI;

import java.util.HashMap;

/**
 * Created by vincent on 07/04/2017.
 */

public class ChallengeInputCode extends ChallengeUI {
    private static final String KEY_IS_RESOLVED = "IS_RESOLVED";

    private static final int CODE_CLEAR = -1;

    protected boolean mIsResolved = false;

    protected int[] mCode = new int[4];
    int mNbInputs=0, mTargetCode;

    protected Image[] mCodeLabels = new Image[4];
    protected Image mFeedback;
    protected Table mScreenLayout;
    protected TextureAtlas mAtlas;


    public ChallengeInputCode() {
        super();


    }

    @Override
    protected void createView() {
        mFeedback = new Image();
        mScreenLayout = new Table();
        mContent.top().add(mScreenLayout).center().padBottom(20);
        mContent.row().top().fill().center();
    }

    protected void createUI() {

        mScreenLayout.setBackground(GenericUI.getInstance().getSkin().getDrawable("dialog"));
        for (int i = 0; i < 4; i++) {
            mCodeLabels[i] = new Image(mAtlas.findRegion("digit_null"));
            mScreenLayout.add(mCodeLabels[i]).pad(10);
        }
        mFeedback = new Image(mAtlas.findRegion(mIsResolved ? "bubble_on" : "bubble_off"));
        mFeedback.setTouchable(mIsResolved ? Touchable.enabled : Touchable.disabled);
        mFeedback.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                onInput(CODE_CLEAR);
            }
        });
        mScreenLayout.add(mFeedback).pad(10);

        Table keyboardLayout = new Table();
        mContent.add(keyboardLayout).pad(10);
        int[] idx = new int[]{1,2,3,4,5,6,7,8,9,0,CODE_CLEAR};
        for (int i = 0; i <idx.length; i++) {
            Container<Label> cell = new Container<>();
            cell.setBackground(new TextureRegionDrawable(mAtlas.findRegion( "keyboard")));
            cell.setTouchable(Touchable.enabled);
            cell.setSize(48, 41);
            int currIdx = idx[i];
            Label label = new Label("" + (currIdx !=CODE_CLEAR ? currIdx : "X"), GenericUI.getInstance().getSkin(), "big-font");
            label.setSize(48, 32);
            cell.setActor(label);
            final int code = currIdx;
            cell.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    onInput(code);
                }
            });
            keyboardLayout.add(cell);


            if (( (i+1) % 3) == 0) {
                keyboardLayout.row();
            }
        }
        refreshUI();
    }

    public void restoreFromPersistence(GameSession aGameSession) {
        Boolean isResolved = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED);
        if (isResolved != null && isResolved.booleanValue()) {
            mIsResolved = true;
            mCode[0] = mTargetCode/1000;
            mCode[1] = (mTargetCode - mCode[0]*1000)/100;
            mCode[2] =(mTargetCode - mCode[0]*1000 - mCode[1]*100)/10;
            mCode[3] = (mTargetCode - mCode[0]*1000 - mCode[1]*100 - mCode[2]*10);
            mNbInputs=4;
        }
        refreshUI();
    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED, mIsResolved);
        return aGameSession;
    }


    protected void setProperties(HashMap aProperties) {

        if (aProperties != null && aProperties.containsKey("code")) {
            mTargetCode = ((Float) aProperties.get("code")).intValue();
        }
        AssetsUtility.loadTextureAtlasAsset(mInteractionChallenge.getAtlasFilename());

        mAtlas = AssetsUtility.getTextureAtlasAsset(mInteractionChallenge.getAtlasFilename());
        createUI();

    }


    protected void refreshUI() {
        if(mAtlas==null)
            return;

        mFeedback.setDrawable(new TextureRegionDrawable(mAtlas.findRegion(mIsResolved ? "bubble_on" : "bubble_off")));
        mFeedback.setTouchable(mIsResolved ? Touchable.enabled : Touchable.disabled);

        for (int i = 0; i < 4; i++) {
            if(i<=mNbInputs-1)
            {
                mCodeLabels[i].setDrawable(new TextureRegionDrawable(mAtlas.findRegion( "digit",mCode[i])));
            }
            else
            {
                mCodeLabels[i].setDrawable(new TextureRegionDrawable(mAtlas.findRegion( "digit_null")));
            }

        }
    }

    protected void onInput(int aCode) {
        if(mIsResolved)
            return;
        if(aCode==CODE_CLEAR)
        {
            mNbInputs=0;
            for(int i=0;i<mCode.length;i++)
            {
                mCode[i]=-1;
            }
            refreshUI();
            return;

        }
        if(mNbInputs<4)
        {
            mCode[mNbInputs]=aCode;
            mNbInputs++;
        }
        if(!checkIsCorrectCode())
        {
            refreshUI();
        }

    }

    protected boolean checkIsCorrectCode() {
        if(mNbInputs==4)
        {
            int code = mCode[3] + 10*mCode[2] + 100*mCode[1]+ 1000*mCode[0];
            if(code==mTargetCode)
            {
                if(!mIsResolved)
                {
                    mIsResolved=true;
                    refreshUI();
                    mInteractionChallenge.setState("COMPLETED");
                    mInteractionChallenge.saveInPersistence();
                    challengeCompleted();
                }
                return true;
            }
        }
        return false;

    }


}
