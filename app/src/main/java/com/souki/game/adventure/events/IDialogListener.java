package com.souki.game.adventure.events;

import com.souki.game.adventure.dialogs.GameDialog;

/**
 * Created by vincent on 12/01/2017.
 */

public interface IDialogListener {

    public void onStartDialog(GameDialog aDialog);
    public void onStopDialog(GameDialog aDialog);
}
