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

    /**
     * コンストラクタ
     * @param seconds 秒数
     */
    protected AutoStartTimer(ColorTeamingEntry parent, int seconds, List<String> commands) {
        this.parent = parent;
        this.secondsLeft = seconds;
        this.commands = commands;
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
                broadcastMessage("auto_start_timer_seconds", secondsLeft);
            }

        } else {
            secondsLeft = -1;

            // 自身のタスクを削除して、チーム分けを実行し、コマンドを実行する。
            this.cancel();
            doTeam();
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
    private void broadcastMessage(String key, Object... args) {
        String msg = Messages.get(key, args);
        if ( msg.equals("") ) {
            return;
        }
        Bukkit.broadcastMessage(Utility.replaceColorCode(msg));
    }

    /**
     * チーム分けを実行する
     */
    private void doTeam() {

        // オンラインの参加者リストを作成する
        ArrayList<Player> players = new ArrayList<Player>();
        ArrayList<String> offlines = new ArrayList<String>();
        for ( String name : parent.getParticipants() ) {
            Player player = getPlayerExact(name);
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

        // 受け付けを停止する
        parent.setOpen(false);
    }

    /**
     * タイマーを開始する
     */
    public void startTimer() {
        this.runTaskTimer(ColorTeamingEntry.getInstance(), 20, 20);
        broadcastMessage("auto_start_timer_seconds", secondsLeft);
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

    /**
     * 指定されたプレイヤー名に一致するプレイヤーを返す
     * @param name プレイヤー名
     * @return プレイヤー
     */
    private Player getPlayerExact(String name) {
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            if ( player.getName().equals(name) ) {
                return player;
            }
        }
        return null;
    }
}
