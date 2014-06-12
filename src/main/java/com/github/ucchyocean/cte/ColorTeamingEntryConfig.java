/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * ColorTeaming Entry コンフィグクラス
 * @author ucchy
 */
public class ColorTeamingEntryConfig {

    private ChatColor entryColor;
    private boolean autoStartTimer;
    private int autoStartTimerPlayerNum;
    private int autoStartTimerSeconds;
    private List<String> autoStartTimerCommands;

    /**
     * config.yml をロードします。
     * @param folder プラグインのフォルダ
     * @param jarFile プラグインのjarファイル
     * @return コンフィグデータ
     */
    protected static ColorTeamingEntryConfig load(File folder, File jarFile) {

        // フォルダが無いなら作成する
        if ( !folder.exists() ) {
            folder.mkdirs();
        }

        // ファイルが無いなら作成する
        File file = new File(folder, "config.yml");
        if ( !file.exists() ) {
            Utility.copyFileFromJar(jarFile, file, "config_ja.yml", false);
        }

        // 読み込み
        ColorTeamingEntryConfig conf = new ColorTeamingEntryConfig();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        conf.entryColor = Utility.toChatColor(config.getString("entryColor", "gold"));
        conf.autoStartTimer = config.getBoolean("autoStartTimer", false);
        conf.autoStartTimerPlayerNum = config.getInt("autoStartTimerPlayerNum", 5);
        conf.autoStartTimerSeconds = config.getInt("autoStartTimerSeconds", 30);
        conf.autoStartTimerCommands = config.getStringList("autoStartTimerCommands");

        return conf;
    }

    /**
     * @return entryColor
     */
    public ChatColor getEntryColor() {
        return entryColor;
    }

    /**
     * @return autoStartTimer
     */
    public boolean isAutoStartTimer() {
        return autoStartTimer;
    }

    /**
     * @return autoStartTimerPlayerNum
     */
    public int getAutoStartTimerPlayerNum() {
        return autoStartTimerPlayerNum;
    }

    /**
     * @return autoStartTimerSeconds
     */
    public int getAutoStartTimerSeconds() {
        return autoStartTimerSeconds;
    }

    /**
     * @return autoStartTimerCommands
     */
    public List<String> getAutoStartTimerCommands() {
        return autoStartTimerCommands;
    }
}
