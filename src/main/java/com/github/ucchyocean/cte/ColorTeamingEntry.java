/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ColorTeaming Entry
 * @author ucchy
 */
public class ColorTeamingEntry extends JavaPlugin implements Listener {

    private static ColorTeamingEntry instance;

    private ArrayList<String> participants;
    private boolean isOpen;
    private ColorTeamingBridge ctbridge;
    private ColorTeamingEntryCommand cecommand;
    private ChatColor entryColor;

    /**
     * コンストラクタ
     */
    public ColorTeamingEntry() {
        instance = this;
        participants = new ArrayList<String>();
    }

    /**
     * プラグインが有効化されるときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {

        // コンフィグのロード
        reloadConfiguration();

        // ColorTeamingの取得
        ctbridge = new ColorTeamingBridge(
                getServer().getPluginManager().getPlugin("ColorTeaming"));

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

    public void reloadConfiguration() {

        // フォルダが無いなら作成する
        File folder = this.getDataFolder();
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        // ファイルが無いなら作成する
        File file = new File(folder, "config.yml");
        if ( !file.exists() ) {
            Utility.copyFileFromJar(this.getFile(), file, "config_ja.yml", false);
        }

        // 読み込み
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        entryColor = Utility.toChatColor(config.getString("entryColor", "gold"));
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
            player.setPlayerListName(entryColor + player.getName());
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
            return true;
        }
        return false;
    }

    /**
     * プレイヤーがサーバーに参加したときに呼び出されるイベント
     * @param event
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        // エントリーしているプレイヤーなら、名前色を変更する
        Player player = event.getPlayer();
        if ( participants.contains(player.getName()) ) {
            player.setPlayerListName(entryColor + player.getName());
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
     * エントリーカラーを返します。
     * @return
     */
    protected ChatColor getEntryColor() {
        return entryColor;
    }

    /**
     * このプラグインのフォルダを取得します。
     * @return
     */
    protected static File getConfigFolder() {
        return instance.getDataFolder();
    }

    /**
     * このプラグインのJarファイルを取得します。
     * @return
     */
    protected static File getPluginJarFile() {
        return instance.getFile();
    }
}