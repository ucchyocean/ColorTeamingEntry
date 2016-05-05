/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ColorTeaming Entry
 * @author ucchy
 */
public class ColorTeamingEntry extends JavaPlugin implements Listener {

    private ArrayList<String> participants;
    private boolean isOpen;
    private ColorTeamingBridge ctbridge;
    private ExpTimerBridge etbridge;
    private ColorTeamingEntryCommand cecommand;
    private ColorTeamingEntryConfig config;

    private String preinfo;

    private AutoStartTimer timer;
    private List<String> timerCommands;

    /**
     * プラグインが有効化されるときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // 初期化
        participants = new ArrayList<String>();
        preinfo = Messages.get("prefix_info");

        // コンフィグのロード
        reloadCTEConfig();

        // ColorTeamingの取得、dependに指定しているので必ず取得できる。
        Plugin colorteaming = getServer().getPluginManager().getPlugin("ColorTeaming");
        String ctversion = colorteaming.getDescription().getVersion();
        if ( !Utility.isUpperVersion(ctversion, "2.3.0") ) {
            getLogger().warning("ColorTeaming のバージョンが古いため、ColorTeamingEntry が使用できません。");
            getLogger().warning("ColorTeaming v2.3.0 以降のバージョンをご利用ください。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ctbridge = new ColorTeamingBridge(colorteaming);

        // ExpTimerの取得、softdependなので取得できない場合がある。
        // バージョンチェックはしない。(面倒なので)
        if ( getServer().getPluginManager().isPluginEnabled("ExpTimer") ) {
            Plugin exptimer = getServer().getPluginManager().getPlugin("ExpTimer");
            etbridge = new ExpTimerBridge(exptimer);
        }

        // コマンドの生成
        cecommand = new ColorTeamingEntryCommand(this);

        // イベントの登録
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(
                new EntrySignListener(this), this);
    }

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return cecommand.onCommand(sender, command, label, args);
    }

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return cecommand.onTabComplete(sender, command, alias, args);
    }

    /**
     * 現在の参加者リストを取得する
     * @return 参加者リスト
     */
    public ArrayList<String> getParticipants() {
        return participants;
    }

    /**
     * 参加者を追加する
     * @param player 追加するプレイヤー
     * @return 追加したかどうか（既に追加されているプレイヤーなら、falseが返される）
     */
    public boolean addParticipant(Player player) {
        if ( !participants.contains(player.getName()) ) {
            participants.add(player.getName());
            player.setPlayerListName(config.getEntryColor() + player.getName());

            // 自動開始タイマーが有効で、既定の参加人数を超えたなら、タイマーを開始する
            if ( config.isAutoStartTimer() && (timer == null || timer.isEnd()) &&
                    config.getAutoStartTimerPlayerNum() <= participants.size() ) {
                startTimer();
            }

            return true;
        }
        return false;
    }

    /**
     * 参加者を削除する
     * @param player 削除するプレイヤー
     * @return 削除したかどうか（既にリストに存在しないプレイヤーなら、falseが返される）
     */
    public boolean removeParticipant(Player player) {
        if ( participants.contains(player.getName()) ) {
            participants.remove(player.getName());
            player.setPlayerListName(player.getName());

            // 自動開始タイマーが動作していて、既定の参加人数を下回ったなら、タイマーをキャンセルする
            if ( timer != null && !timer.isEnd() &&
                    config.getAutoStartTimerPlayerNum() > participants.size() ) {
                cancelTimer(true);
            }

            return true;
        }
        return false;
    }

    /**
     * 参加者を削除する
     * @param name 削除するプレイヤー名
     * @return 削除したかどうか（既にリストに存在しないプレイヤーなら、falseが返される）
     */
    public boolean removeParticipant(String name) {
        if ( participants.contains(name) ) {
            participants.remove(name);
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                player.setPlayerListName(player.getName());
            }

            // 自動開始タイマーが動作していて、既定の参加人数を下回ったなら、タイマーをキャンセルする
            if ( timer != null && !timer.isEnd() &&
                    config.getAutoStartTimerPlayerNum() > participants.size() ) {
                cancelTimer(true);
            }

            return true;
        }
        return false;
    }

    /**
     * 自動スタートタイマーを開始する
     */
    protected void startTimer() {
        if ( timerCommands == null ) {
            timerCommands = config.getAutoStartTimerCommands();
        }
        timer = new AutoStartTimer(this, config.getAutoStartTimerSeconds(), timerCommands);
        timer.startTimer();
    }

    /**
     * 自動スタートタイマーをキャンセルする
     * @param doAnnounce キャンセルしたことをアナウンスするかどうか
     */
    protected void cancelTimer(boolean doAnnounce) {
        if ( timer != null && !timer.isEnd() ) {
            timer.cancel();
            timer = null;
            if ( doAnnounce ) {
                broadcastInfoMessage("auto_start_timer_cancel");
            }
        }
        timer = null;
    }

    /**
     * タイマー満了時の実行コマンドセットを設定する
     * @param commands
     */
    protected void setTimerCommands(List<String> commands) {
        timerCommands = commands;
    }

    /**
     * 情報メッセージリソースを取得し、broadcastする
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
     * プレイヤーがサーバーに参加したときに呼び出されるイベント
     * @param event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        // エントリーしているプレイヤーなら、名前色を変更する
        Player player = event.getPlayer();
        if ( isOpen && participants.contains(player.getName()) ) {
            player.setPlayerListName(config.getEntryColor() + player.getName());
        }
    }

    /**
     * プレイヤーがサーバーから離脱したときに呼び出されるイベント
     * @param event
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        // エントリーしているプレイヤーで、離脱したときにエントリー削除する設定なら、
        // エントリーを削除する
        Player player = event.getPlayer();
        if ( isOpen && config.isLeaveOnQuitServer()
                && participants.contains(player.getName()) ) {
            removeParticipant(player.getName());
        }
    }

    /**
     * ColorTeamingを取得します。
     * @return
     */
    protected ColorTeamingBridge getColorTeaming() {
        return ctbridge;
    }

    /**
     * ExpTimerを取得します。
     * @return
     */
    protected ExpTimerBridge getExpTimer() {
        return etbridge;
    }

    /**
     * 受付を開始しているかどうかを返します。
     * @return 受付開始しているかどうか
     */
    protected boolean isOpen() {
        return isOpen;
    }

    /**
     * 受付開始・停止を設定します。
     * @param isOpen
     */
    protected void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    /**
     * ColorTeaming Entry のコンフィグデータを取得します。
     * @return ColorTeamingEntryConfig
     */
    protected ColorTeamingEntryConfig getCTEConfig() {
        return config;
    }

    /**
     * ColorTeaming Entry のコンフィグデータを再読み込みします。
     */
    protected void reloadCTEConfig() {
        config = ColorTeamingEntryConfig.load(this.getDataFolder(), this.getFile());
    }

    /**
     * このプラグインのフォルダを取得します。
     * @return
     */
    protected static File getConfigFolder() {
        return getInstance().getDataFolder();
    }

    /**
     * このプラグインのJarファイルを取得します。
     * @return
     */
    protected static File getPluginJarFile() {
        return getInstance().getFile();
    }

    /**
     * ColorTeamingEntryのインスタンスを返します。
     * @return インスタンス
     */
    protected static ColorTeamingEntry getInstance() {
        return (ColorTeamingEntry)Bukkit.getPluginManager().getPlugin("ColorTeamingEntry");
    }
}
