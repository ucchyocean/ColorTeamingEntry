/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * ColorTeaming Entry コンフィグクラス
 * @author ucchy
 */
public class ColorTeamingEntryConfig {

    private ChatColor entryColor;
    private boolean leaveOnQuitServer;
    private boolean autoStartTimer;
    private int autoStartTimerPlayerNum;
    private int autoStartTimerSeconds;
    private List<String> autoStartTimerCommands;
    private HashMap<String, List<String>> autoStartTimerCommandConfigs;
    private AutoStartTimerMode autoStartTimerMode;
    private boolean disableOpenOnRunningExpTimer;

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
        conf.leaveOnQuitServer = config.getBoolean("leaveOnQuitServer", true);
        conf.autoStartTimer = config.getBoolean("autoStartTimer", false);
        conf.autoStartTimerPlayerNum = config.getInt("autoStartTimerPlayerNum", 5);
        conf.autoStartTimerSeconds = config.getInt("autoStartTimerSeconds", 30);
        conf.autoStartTimerCommands = config.getStringList("autoStartTimerCommands");
        conf.autoStartTimerMode = AutoStartTimerMode.getFromName(
                config.getString("autoStartTimerMode"), AutoStartTimerMode.CLOSE_AND_TEAM);
        conf.disableOpenOnRunningExpTimer =
                config.getBoolean("disableOpenOnRunningExpTimer", false);

        // config.getValues() でまとめて取得できるけど、後でキャスト面倒なので、
        // 1つずつ取得します。。。
        conf.autoStartTimerCommandConfigs = new HashMap<String, List<String>>();
        if ( config.contains("autoStartTimerCommandConfigs") ) {
            ConfigurationSection section =
                    config.getConfigurationSection("autoStartTimerCommandConfigs");
            for ( String key : section.getKeys(false) ) {
                conf.autoStartTimerCommandConfigs.put(key, section.getStringList(key));
            }
        }

        return conf;
    }

    /**
     * @return entryColor
     */
    public ChatColor getEntryColor() {
        return entryColor;
    }

    /**
     * @return leaveOnQuitServer
     */
    public boolean isLeaveOnQuitServer() {
        return leaveOnQuitServer;
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

    /**
     * @return autoStartTimerCommandConfigs
     */
    public HashMap<String, List<String>> getAutoStartTimerCommandConfigs() {
        return autoStartTimerCommandConfigs;
    }

    /**
     * @return autoStartTimerMode
     */
    public AutoStartTimerMode getAutoStartTimerMode() {
        return autoStartTimerMode;
    }

    /**
     * @return disableOpenOnRunningExpTimer
     */
    public boolean isDisableOpenOnRunningExpTimer() {
        return disableOpenOnRunningExpTimer;
    }
}
