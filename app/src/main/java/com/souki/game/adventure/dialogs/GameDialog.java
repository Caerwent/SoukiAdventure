package com.souki.game.adventure.dialogs;

import com.badlogic.gdx.utils.Array;

/**
 * Created by vincent on 12/01/2017.
 */

public class GameDialog {

    protected String id;
    protected Array<GameDialogStep> dialogs;

    public String getId() {
        return id;
    }

    public Array<GameDialogStep> getDialogs() {
        return dialogs;
    }

    public GameDialog clone() {
        GameDialog dialog = new GameDialog();

        if (id != null) {
            dialog.id = new String(id);
        }
        if (dialogs != null) {
            dialog.dialogs = new Array<>();
            for (GameDialogStep step : dialogs) {
                dialog.dialogs.add(step.clone());
            }
        }
        return dialog;
    }
}
