/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import org.bukkit.plugin.Plugin;

import com.github.ucchyocean.et.ExpTimer;
import com.github.ucchyocean.et.TimerTask;
import com.github.ucchyocean.et.TimerTask.Status;

/**
 * ExpTimer 連携クラス
 * @author ucchy
 */
public class ExpTimerBridge {

    private ExpTimer exptimer;

    /**
     * コンストラクタ
     * @param exptimer
     */
    public ExpTimerBridge(Plugin exptimer) {
        this.exptimer = (ExpTimer)exptimer;
    }

    /**
     * ExpTimerのタイマーが動作しているかどうかを返す。
     * @return
     */
    public boolean isTimerRunning() {
        TimerTask task = exptimer.getTask();
        return (task != null && task.getStatus() != Status.END && task.getSecondsGameRest() > 0);
    }
}
