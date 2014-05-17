/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * エントリーサインのリスナークラス
 * @author ucchy
 */
public class EntrySignListener implements Listener {

    private static final String ACTIVE = ChatColor.GREEN + "[ACTIVE]";
    private static final String INACTIVE = ChatColor.RED + "[INACTIVE]";
    private static final String FIRST_LINE_JOIN = "[entry]";
    private static final String FIRST_LINE_LEAVE = "[leave]";

    private static final String PERMISSION_PRE = "centry.sign.";
    private static final String PERMISSION_USER_USE = PERMISSION_PRE + "use";
    private static final String PERMISSION_ADMIN_TOGGLE = PERMISSION_PRE + "toggle";
    private static final String PERMISSION_ADMIN_PLACE = PERMISSION_PRE + "place";
    private static final String PERMISSION_ADMIN_BREAK = PERMISSION_PRE + "break";

    private ColorTeamingEntry parent;

    private String preinfo;
    private String preerr;

    /**
     * コンストラクタ
     */
    public EntrySignListener(ColorTeamingEntry parent) {
        this.parent = parent;
        preinfo = Messages.get("prefix_info");
        preerr = Messages.get("prefix_error");
    }

    /**
     * カンバンをクリックしたときのイベント
     * @param event
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {

        // クリックされたのがカンバンでないなら無視する
        if (event.getClickedBlock() == null ||
                !(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }

        Sign sign = (Sign)event.getClickedBlock().getState();

        // 関係のないカンバンなら無視する
        if (!sign.getLine(0).equals(FIRST_LINE_JOIN)
                && !sign.getLine(0).equals(FIRST_LINE_LEAVE)) {
            return;
        }

        // joinのカンバンかどうかを調べる
        boolean isJoin = false;
        if ( sign.getLine(0).equals(FIRST_LINE_JOIN) ) {
            isJoin = true;
        }

        Player player = event.getPlayer();
        if ( event.getAction() == Action.LEFT_CLICK_BLOCK
                || (event.getPlayer().getGameMode() == GameMode.ADVENTURE
                    && event.getAction() == Action.RIGHT_CLICK_BLOCK ) ) {

            if ( !player.hasPermission(PERMISSION_USER_USE) ) {
                // 権限がない
                return;
            }

            if ( sign.getLine(3).equals(INACTIVE) ) {
                // カンバンが無効状態
                return;
            }

            if ( !parent.isOpen() ) {
                // エントリーが開始していない
                sendErrorMessage(player, "error_closed");
                return;
            }

            if ( isJoin ) {

                // エントリーする
                boolean result = parent.addParticipant(player);
                if ( !result ) {
                    sendErrorMessage(player, "error_already_join");
                } else {
                    sendInfoMessage(player, "info_join");
                }
                return;

            } else {

                // 離脱する
                boolean result = parent.removeParticipant(player);
                if ( !result ) {
                    sendErrorMessage(player, "error_already_leave");
                } else {
                    sendInfoMessage(player, "info_leave");
                }
                return;

            }

        } else if ( event.getAction() == Action.RIGHT_CLICK_BLOCK ) {

            if ( !player.hasPermission(PERMISSION_ADMIN_TOGGLE) ) {
                // 権限が無い
                return;
            }

            // 有効状態と無効状態を切り替えする
            if ( sign.getLine(3).equals(ACTIVE) ) {
                sign.setLine(3, INACTIVE);
            } else if ( sign.getLine(3).equals(INACTIVE) ) {
                sign.setLine(3, ACTIVE);
            }
            sign.update();

            return;
        }
    }

    /**
     * カンバンを設置したときのイベント
     * @param event
     */
    @EventHandler
    public void onSignChange(SignChangeEvent event) {

        // 関係のないカンバンなら無視する
        if (!event.getLine(0).equals(FIRST_LINE_JOIN)
                && !event.getLine(0).equals(FIRST_LINE_LEAVE)) {
            return;
        }

        Player player = event.getPlayer();
        if ( !player.hasPermission(PERMISSION_ADMIN_PLACE) ) {
            // 権限が無い
            sendErrorMessage(player,
                    "error_no_permission", PERMISSION_ADMIN_PLACE);
            event.setLine(0, "");
            return;
        }

        // サインを有効状態に変更する
        event.setLine(3, ACTIVE);
    }

    /**
     * ブロックが壊されたときのイベント
     * @param event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        BlockState block = event.getBlock().getState();
        if ( !(block instanceof Sign) ) {
            // 壊されたブロックがカンバンでないなら無視する
            return;
        }

        Sign sign = (Sign) block;
        if (!sign.getLine(0).equals(FIRST_LINE_JOIN)
                && !sign.getLine(0).equals(FIRST_LINE_LEAVE)) {
            // 関係のないカンバンなら無視する
            return;
        }

        Player player = event.getPlayer();
        if ( !player.hasPermission(PERMISSION_ADMIN_BREAK) ) {
            // 権限が無い
            sendErrorMessage(player,
                    "error_no_permission", PERMISSION_ADMIN_BREAK);
            event.setCancelled(true);
            return;
        }
    }

    /**
     * エラーメッセージリソースを取得し、Stringを返す
     * @param key メッセージキー
     * @param args メッセージの引数
     */
    private void sendErrorMessage(
            CommandSender sender, String key, Object... args) {
        String msg = Messages.get(key, args);
        if ( msg.equals("") ) {
            return;
        }
        sender.sendMessage(Utility.replaceColorCode(preerr + msg));
    }

    /**
     * 情報メッセージリソースを取得し、Stringを返す
     * @param key メッセージキー
     * @param args メッセージの引数
     */
    private void sendInfoMessage(
            CommandSender sender, String key, Object... args) {
        String msg = Messages.get(key, args);
        if ( msg.equals("") ) {
            return;
        }
        sender.sendMessage(Utility.replaceColorCode(preinfo + msg));
    }
}
