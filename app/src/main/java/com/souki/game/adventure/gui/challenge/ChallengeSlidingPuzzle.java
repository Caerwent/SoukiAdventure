package com.souki.game.adventure.gui.challenge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.souki.game.adventure.AssetsUtility;
import com.souki.game.adventure.persistence.GameSession;
import com.souki.game.adventure.screens.GenericUI;

import java.util.HashMap;

/**
 * Created by vincent on 07/04/2017.
 */

public class ChallengeSlidingPuzzle extends ChallengeUI {
    private static final String KEY_PUZZLE_STATE = "PUZZLE_STATE";
    private static final String KEY_IS_RESOLVED = "IS_RESOLVED";
    private WidgetGroup mGroup;
    protected int mSize = 3;
    protected int mImgSizeInPx, mTotalSizeInPx;
    protected int[][] mPuzzleState;
    protected String mImageFile;
    boolean sizeInvalid = true;
    protected Image mCompletedImage;
    protected boolean mIsResolved = false;


    protected ClickListener mClickListener = new ClickListener() {
        private int mPointer = -1;
        private Actor mActor;
        private int mOriginActorRow, mOriginActorCol;
        private float mLastX, mLastY;

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            if (mPointer == -1) {
                mActor = mGroup.hit(x, y, true);
                if (mActor == null || !(mActor instanceof Container))
                    return false;
                mPointer = pointer;
                mLastX = x;
                mLastY = y;
                mOriginActorCol = xToGridCol(mActor.getX());
                mOriginActorRow = yToGridRow(mActor.getY() + mActor.getHeight());
                return true;
            }
            return false;


        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            if (pointer == mPointer) {
                mPointer = -1;
                int finalX = (int) (((mActor.getX() + mActor.getWidth() / 2) / mImgSizeInPx)) * mImgSizeInPx;
                int finalY = (int) (((mActor.getY() + mActor.getHeight() / 2) / mImgSizeInPx)) * mImgSizeInPx;
                mActor.moveBy(finalX - mActor.getX(), finalY - mActor.getY());

                int newCol = xToGridCol(mActor.getX());
                int newRow = yToGridRow(mActor.getY() + mActor.getHeight());
                int state = mPuzzleState[mOriginActorRow][mOriginActorCol];
                mPuzzleState[mOriginActorRow][mOriginActorCol] = mPuzzleState[newRow][newCol];
                mPuzzleState[newRow][newCol] = state;
                mActor = null;
                checkPuzzleResolution();
                saveInPersistence();

            }
        }

        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (pointer != mPointer) return;
            float dx = x - mLastX;
            float dy = y - mLastY;
            mLastY = y;
            mLastX = x;
            float targetX = mActor.getX() + (dx >= 0 ? mActor.getWidth() : 0) + dx;
            float targetY = mActor.getY() + (dy >= 0 ? mActor.getHeight() : 0) + dy;
            if (canMoveToY(mOriginActorRow, mOriginActorCol, targetY)) {
                mActor.moveBy(0, dy);

            } else if (canMoveToX(mOriginActorRow, mOriginActorCol, targetX)) {
                mActor.moveBy(dx, 0);

            }

        }
    };

    public ChallengeSlidingPuzzle() {
        super();


    }

    @Override
    protected void createView() {

        mGroup = new WidgetGroup();
        mContent.align(Align.center);
        mContent.top().add(mGroup).pad(20).expand().fill().center();
        mTotalSizeInPx = (int) Math.min(mGroup.getWidth(), mGroup.getHeight());

    }

    @Override
    public void layout() {
        super.layout();
        if (sizeInvalid) {
            mTotalSizeInPx = (int) Math.min(mGroup.getWidth(), mGroup.getHeight());
            createPuzzle();
            sizeInvalid = false;
        }
    }

    public void restoreFromPersistence(GameSession aGameSession) {
        int[][] puzzleState = (int[][]) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_PUZZLE_STATE);
        if (puzzleState != null) {
            mPuzzleState = puzzleState;
        }
        Boolean isResolved = (Boolean) aGameSession.getSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED);
        if (isResolved != null && isResolved.booleanValue()) {
            mIsResolved = true;
        }
        createPuzzle();
    }

    public GameSession saveInPersistence(GameSession aGameSession) {
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_PUZZLE_STATE, mPuzzleState);
        aGameSession.putSessionDataForMapAndEntity(mInteractionChallenge.getMap().getMapName(), mInteractionChallenge.getId(), KEY_IS_RESOLVED, mIsResolved);
        return aGameSession;
    }


    protected void setProperties(HashMap aProperties) {

        if (aProperties != null && aProperties.containsKey("size")) {
            mSize = ((Float) aProperties.get("size")).intValue();
        }
        if (aProperties != null && aProperties.containsKey("image")) {
            mImageFile = (String) aProperties.get("image");
        }
        createPuzzle();
    }

    protected void shuffle() {
        int col = 0, row = 0;
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (mPuzzleState[i][j] == 0) {
                    col = j;
                    row = i;
                    break;
                }
            }
        }
        for (int iter = 0; iter < 30; iter++) {
            int rand = (int) Math.round(Math.random() * 3);
            int lastMove = -1;
            switch (rand) {
                case 0: // left
                    if (col > 1 && lastMove != 1) {
                        mPuzzleState[row][col] = mPuzzleState[row][col - 1];
                        mPuzzleState[row][col - 1] = 0;
                        col = col - 1;
                        lastMove = rand;
                        Gdx.app.debug("DEBUG", "rand=" + rand);
                    } else {
                        iter--;
                    }
                    break;
                case 1: // right
                    if (col < mSize - 1 && lastMove != 0) {
                        mPuzzleState[row][col] = mPuzzleState[row][col + 1];
                        mPuzzleState[row][col + 1] = 0;
                        col = col + 1;
                        lastMove = rand;
                        Gdx.app.debug("DEBUG", "rand=" + rand);
                    } else {
                        iter--;
                    }
                    break;
                case 2: // top
                    if (row > 1 && lastMove != 3) {
                        mPuzzleState[row][col] = mPuzzleState[row - 1][col];
                        mPuzzleState[row - 1][col] = 0;
                        row = row - 1;
                        lastMove = rand;
                        Gdx.app.debug("DEBUG", "rand=" + rand);
                    } else {
                        iter--;
                    }
                    break;
                case 3: // bottom
                    if (row < mSize - 1 && lastMove != 2) {
                        mPuzzleState[row][col] = mPuzzleState[row + 1][col];
                        mPuzzleState[row + 1][col] = 0;
                        row = row + 1;
                        lastMove = rand;
                        Gdx.app.debug("DEBUG", "rand=" + rand);
                    } else {
                        iter--;
                    }
                    break;
            }
        }
    }

    protected void createPuzzle() {

        if (mImageFile == null || mTotalSizeInPx == 0)
            return;

        AssetsUtility.loadTextureAsset(mImageFile);
        Texture region = AssetsUtility.getTextureAsset(mImageFile);
        mCompletedImage = new Image(region);
        mCompletedImage.setSize(mTotalSizeInPx, mTotalSizeInPx);
        mCompletedImage.setScaling(Scaling.fit);
        mCompletedImage.setAlign(Align.top);

        if (mPuzzleState != null) {
            mSize = mPuzzleState.length;
        } else {
            mPuzzleState = new int[mSize][mSize];
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    mPuzzleState[i][j] = i * mSize + j;
                }

            }
            shuffle();
        }
        mGroup.clear();
        mImgSizeInPx = mTotalSizeInPx / mSize;

        if (mIsResolved) {
            mGroup.addActor(mCompletedImage);
        } else {
            int originalImgSplitSizeX = region.getWidth() / mSize;
            int originalImgSplitSizeY = region.getHeight() / mSize;
            TextureRegion[][] initRegions = TextureRegion.split(region, originalImgSplitSizeX, originalImgSplitSizeY);
            for (int i = 0; i < mSize; i++) {
                for (int j = 0; j < mSize; j++) {
                    if (mPuzzleState[i][j] == 0) {
                        continue;
                    }
                    int row = numToGridRow(mPuzzleState[i][j]);
                    int col = numToGridCol(mPuzzleState[i][j]);
                    Image img = new Image(initRegions[row][col]);
                    img.setTouchable(Touchable.disabled);
                    Container<Image> cell = new Container<>();
                    cell.setBackground(GenericUI.getInstance().getSkin().getDrawable("bg"));
                    cell.setTouchable(Touchable.enabled);
                    cell.setColor(GenericUI.getInstance().getSkin().getColor("background-color-2"));
                    mGroup.addActor(cell);
                    cell.setSize(mImgSizeInPx, mImgSizeInPx);
                    img.setSize(mImgSizeInPx - 1, mImgSizeInPx - 1);
                    img.setScaling(Scaling.fit);
                    img.setAlign(Align.top);
                    cell.pad(0.5F);
                    cell.setActor(img);
                    cell.setPosition(j * mImgSizeInPx, (mSize - i - 1) * mImgSizeInPx);
                }

            }


            mGroup.addListener(mClickListener);
        }

    }

    protected int numToGridRow(int aNum) {
        return aNum / mSize;
    }

    protected int numToGridCol(int aNum) {
        return aNum - (aNum / mSize) * mSize;
    }


    protected int yToGridRow(float y) {
        return y < 0 ? 0 : (int) Math.min(mSize - 1, (mTotalSizeInPx - y) / mImgSizeInPx);
    }

    protected int xToGridCol(float x) {
        return x < 0 ? 0 : (int) Math.min(mSize - 1, x / mImgSizeInPx);
    }


    protected boolean canMoveToX(int row, int col, float x) {
        if (x < 0 || x > mTotalSizeInPx)
            return false;
        int newCol = xToGridCol(x);
        //       Gdx.app.debug("DEBUG", "canMoveToX row=" + row + " col=" + col + " x="+x+" newCol="+newCol+" state="+mPuzzleState[row][newCol]);
        if (col < mSize && mPuzzleState[row][newCol] == 0) {
            return true;
        }

        return false;
    }

    protected boolean canMoveToY(int row, int col, float y) {
        if (y < 0 || y > mTotalSizeInPx)
            return false;
        int newRow = yToGridRow(y);
        if (row < mSize && mPuzzleState[newRow][col] == 0) {
            return true;
        }

        return false;
    }

    @Override
    public void release() {
        if (mImageFile != null)
            AssetsUtility.unloadAsset(mImageFile);
        super.release();
    }

    protected void checkPuzzleResolution() {
        int lastState = -1;
        for (int i = 0; i < mSize; i++) {
            for (int j = 0; j < mSize; j++) {
                if (mPuzzleState[i][j] < lastState) {
                    return;
                }
                lastState = mPuzzleState[i][j];
            }
        }
        mIsResolved=true;
        mGroup.removeListener(mClickListener);
        mGroup.clear();
        mGroup.addActor(mCompletedImage);
        challengeCompleted();
    }
}
