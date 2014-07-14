/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

/**
 * 自動開始タイマーの動作モード設定
 * @author ucchy
 */
public enum AutoStartTimerMode {

    /** 参加受け付けを締め切るだけ */
    CLOSE_ONLY,

    /** 参加受け付けを締め切り、さらにチーム分けを行う */
    CLOSE_AND_TEAM;

    /**
     * 指定された名前に合った、AutoStartModeを返す
     * @param name 名前
     * @param def 指定された名前に合うAutoStartModeが見つからなかった時に返す、デフォルト値
     * @return 一致するAutoStartMode
     */
    public static AutoStartTimerMode getFromName(String name, AutoStartTimerMode def) {

        if ( name == null || name.equals("") ) {
            return def;
        }

        for ( AutoStartTimerMode mode : values() ) {
            if ( mode.name().equalsIgnoreCase(name) ) {
                return mode;
            }
        }

        return def;
    }
}
