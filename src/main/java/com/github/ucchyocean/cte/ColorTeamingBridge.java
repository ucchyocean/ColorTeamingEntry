/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2013
 */
package com.github.ucchyocean.cte;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.github.ucchyocean.ct.ColorTeaming;

/**
 * カラーチーミング 連携クラス
 * @author ucchy
 */
public class ColorTeamingBridge {

    private ColorTeaming colorteaming;

    /**
     * コンストラクタ
     * @param colorteaming
     */
    public ColorTeamingBridge(Plugin colorteaming) {
        this.colorteaming = (ColorTeaming)colorteaming;
    }

    /**
     * ランダムな順序で、プレイヤーをチームわけします。<br/>
     * 既にチームわけが存在する場合は、全部クリアしてから分けられます。
     * @param players チームわけを行うプレイヤー
     * @param teamNum チーム数（2から9までの数を指定可能です）
     */
    public void makeColorTeams(ArrayList<Player> players, int teamNum) {
        colorteaming.getAPI().makeColorTeamsWithRandomSelection(players, teamNum);
    }
}
