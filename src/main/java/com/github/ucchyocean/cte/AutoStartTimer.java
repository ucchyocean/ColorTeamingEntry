/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 自動開始タイマークラス
 * @author ucchy
 */
public class AutoStartTimer extends BukkitRunnable {

    private ColorTeamingEntry parent;
    private int secondsLeft;
    private List<String> commands;
    private String preinfo;

    /**
     * コンストラクタ
     * @param seconds 秒数
     */
    protected AutoStartTimer(ColorTeamingEntry parent, int seconds, List<String> commands) {
        this.parent = parent;
        this.secondsLeft = seconds;
        this.commands = commands;
        preinfo = Messages.get("prefix_info");
    }

    /**
     * 1秒毎に呼び出されるメソッド
     */
    @Override
    public void run() {

        if ( secondsLeft > 0 ) {
            secondsLeft--;

            // キリのいいところで通知を送る
            if ( secondsLeft == 30 || secondsLeft == 15 || secondsLeft == 5 ) {
                broadcastInfoMessage("auto_start_timer_seconds", secondsLeft);
            }

        } else {
            secondsLeft = -1;

            // 自身のタスクを削除して、チーム分けを実行し、コマンドを実行する。
            this.cancel();
            doTimerEndAction();
            for ( String command : commands ) {
                if ( command.startsWith("/") ) {
                    command = command.substring(1); // スラッシュ削除
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }

    /**
     * メッセージリソースを取得し、broadcastする
     * @param key メッセージキー
     * @param args メッセージの引数
     */
    private void broadcastInfoMessage(String key, Object... args) {
        String msg = Messages.get(key, args);
        if ( msg.equals("") ) {
            return;
        }
        Bukkit.broadcastMessage(Utility.replaceColorCode(preinfo + msg));
    }

    /**
     * タイマー終了時処理を実行する
     */
    private void doTimerEndAction() {

        if ( parent.getCTEConfig().getAutoStartTimerMode() ==
                AutoStartTimerMode.CLOSE_AND_TEAM ) {

            // オンラインの参加者リストを作成する
            ArrayList<Player> players = new ArrayList<Player>();
            ArrayList<String> offlines = new ArrayList<String>();
            for ( String name : parent.getParticipants() ) {
                Player player = Utility.getPlayerExact(name);
                if ( player != null ) {
                    players.add(player);
                } else {
                    offlines.add(name);
                }
            }

            // この時点でオフラインだったプレイヤーは、リストから外す
            for ( String name : offlines ) {
                parent.removeParticipant(name);
            }

            // リストの色を消す
            for ( Player player : players ) {
                player.setPlayerListName(player.getName());
            }

            // チーム分けする
            parent.getColorTeaming().makeColorTeams(players, 2);
            //sendInfoMessage(sender, "info_team_done");
        }

        if ( parent.getCTEConfig().getAutoStartTimerMode() !=
                AutoStartTimerMode.NOTHING ) {

            // 受け付けを停止する
            parent.setOpen(false);
        }
    }

    /**
     * タイマーを開始する
     */
    public void startTimer() {
        this.runTaskTimer(ColorTeamingEntry.getInstance(), 20, 20);
        broadcastInfoMessage("auto_start_timer_seconds", secondsLeft);
    }

    /**
     * タイマーをキャンセルする
     */
    public void cancelTimer() {
        this.cancel();
    }

    /**
     * タイマーが終了しているかどうかを返す
     * @return 終了しているかどうか
     */
    public boolean isEnd() {
        return secondsLeft < 0;
    }
}
