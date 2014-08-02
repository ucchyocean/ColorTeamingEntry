/*
 * @author     ucchy
 * @license    LGPLv3
 * @copyright  Copyright ucchy 2014
 */
package com.github.ucchyocean.cte;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

/**
 * ColorTeaming Entry コマンドクラス
 * @author ucchy
 */
public class ColorTeamingEntryCommand implements TabExecutor {

    private static final String[] COMMANDS = {
        "join", "leave", "list", "announce", "open", "close", "team", "reload"
    };

    private ColorTeamingEntry parent;

    private String preinfo;
    private String preerr;

    private AutoStartTimer timer;
    private List<String> timerCommands;

    /**
     * コンストラクタ
     */
    public ColorTeamingEntryCommand(ColorTeamingEntry parent) {
        this.parent = parent;
        preinfo = Messages.get("prefix_info");
        preerr = Messages.get("prefix_error");
    }

    /**
     * コマンドが実行された時に呼び出されるメソッド
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     * @see org.bukkit.command.CommandExecutor#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( args.length <= 0 ) {
            return false;
        }

        String com = args[0].toLowerCase();

        if ( !isValidCommand(com) ) {
            return false;
        }

        if ( !sender.hasPermission("centry." + com) ) {
            sendErrorMessage(sender,
                    "error_no_permission", "centry." + com);
            return true;
        }

        if ( com.equals("join") ) {
            return doJoin(sender, command, label, args);
        } else if ( com.equals("leave") ) {
            return doLeave(sender, command, label, args);
        } else if ( com.equals("list") ) {
            return doList(sender, command, label, args);
        } else if ( com.equals("announce") ) {
            return doAnnounce(sender, command, label, args);
        } else if ( com.equals("open") ) {
            return doOpen(sender, command, label, args);
        } else if ( com.equals("close") ) {
            return doClose(sender, command, label, args);
        } else if ( com.equals("team") ) {
            return doTeam(sender, command, label, args);
        } else if ( com.equals("reload") ) {
            return doReload(sender, command, label, args);
        }

        return false;
    }

    /**
     * TAB補完が実行された時に呼び出されるメソッド
     * @param sender
     * @param command
     * @param alias
     * @param args
     * @return
     * @see org.bukkit.command.TabCompleter#onTabComplete(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        // コマンド権限が無い場合は何も返さない
        if ( !sender.hasPermission("centry.command") ) {
            return new ArrayList<String>();
        }

        // 第1引数で補完された場合の処理
        if ( args.length == 1 ) {
            String pre = args[0];
            ArrayList<String> candidates = new ArrayList<String>();
            for ( String com : COMMANDS ) {
                if ( com.startsWith(pre) && sender.hasPermission("centry." + com) ) {
                    candidates.add(com);
                }
            }
            return candidates;
        }


        // 第1引数がjoinまたはleaveで、第2引数で補完された場合、プレイヤー名＋allで補完
        if ( args.length == 2 &&
                ( args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("leave") ) ) {
            String pre = args[1];
            ArrayList<String> candidates = new ArrayList<String>();
            if ( "all".startsWith(pre) &&
                    sender.hasPermission("centry." + args[0].toLowerCase() + ".all") ) {
                candidates.add("all");
            }
            if ( sender.hasPermission("centry." + args[0].toLowerCase() + ".other") ) {
                for ( Player player : Bukkit.getOnlinePlayers() ) {
                    if ( player.getName().startsWith(pre) ) {
                        candidates.add(player.getName());
                    }
                }
            }
            return candidates;
        }

        // その他の場合はnullを返してデフォルト動作（プレイヤー名で補完）
        return null;
    }

    /**
     * ジョインコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doJoin(CommandSender sender, Command command, String label, String[] args) {

        Player target;

        // 指定引数の解析
        if ( args.length == 1 ) {
            // 自分がエントリーする

            if ( !sender.hasPermission("centry.join.self") ) {
                sendErrorMessage(sender,
                        "error_no_permission", "centry.join.self");
                return true;
            }

            if ( !(sender instanceof Player) ) {
                sendErrorMessage(sender,
                        "error_not_ingame", label, "join");
                return true;
            }

            if ( !parent.isOpen() ) {
                sendErrorMessage(sender, "error_closed");
                return true;
            }

            target = (Player)sender;

            boolean result = parent.addParticipant(target);
            if ( !result ) {
                sendErrorMessage(sender, "error_already_join");
                return true;
            } else {
                sendInfoMessage(sender, "info_join");
            }

        } else if ( args[1].equalsIgnoreCase("all") ) {
            // 全プレイヤーをエントリーさせる

            if ( !sender.hasPermission("centry.join.all") ) {
                sendErrorMessage(sender,
                        "error_no_permission", "centry.join.all");
                return true;
            }

            if ( !parent.isOpen() ) {
                sendErrorMessage(sender, "error_closed");
                return true;
            }

            boolean result = false;
            for ( Player player : Bukkit.getOnlinePlayers() ) {
                if ( parent.addParticipant(player) ) {
                    result = true;
                    sendInfoMessage(player, "info_join");
                }
            }

            if ( !result ) {
                sendErrorMessage(sender, "error_already_join_all");
                return true;
            } else {
                sendInfoMessage(sender, "info_join_all");
            }

        } else {
            // 指定した人をエントリーさせる

            if ( !sender.hasPermission("centry.join.other") ) {
                sendErrorMessage(sender,
                        "error_no_permission", "centry.join.other");
                return true;
            }

            target = Utility.getPlayerExact(args[1]);
            if ( target == null ) {
                sendErrorMessage(sender, "error_player_not_found");
                return true;
            }

            boolean result = parent.addParticipant(target);
            if ( !result ) {
                sendErrorMessage(sender, "error_already_join_other", target.getName());
                return true;
            } else {
                sendInfoMessage(sender, "info_join_other", target.getName());
                sendInfoMessage(target, "info_join");
            }
        }

        // 自動開始タイマーが有効で、既定の参加人数を超えたなら、タイマーを開始する
        ColorTeamingEntryConfig config = parent.getCTEConfig();
        if ( config.isAutoStartTimer() && (timer == null || timer.isEnd()) &&
                config.getAutoStartTimerPlayerNum() <= parent.getParticipants().size() ) {

            if ( timerCommands == null ) {
                timerCommands = config.getAutoStartTimerCommands();
            }
            timer = new AutoStartTimer(parent, config.getAutoStartTimerSeconds(), timerCommands);
            timer.startTimer();
        }

        return true;
    }

    /**
     * リーブコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doLeave(CommandSender sender, Command command, String label, String[] args) {

        Player target;

        // 指定引数の解析
        if ( args.length == 1 ) {
            // 自分がエントリーから離脱する

            if ( !sender.hasPermission("centry.leave.self") ) {
                sendErrorMessage(sender,
                        "error_no_permission", "centry.leave.self");
                return true;
            }

            if ( !(sender instanceof Player) ) {
                sendErrorMessage(sender,
                        "error_not_ingame", label, "leave");
                return true;
            }

            target = (Player)sender;

            boolean result = parent.removeParticipant(target);
            if ( !result ) {
                sendErrorMessage(sender, "error_already_leave");
                return true;
            } else {
                sendInfoMessage(sender, "info_leave");
            }

        } else if ( args[1].equalsIgnoreCase("all") ) {
            // 全プレイヤーを離脱させる

            if ( !sender.hasPermission("centry.leave.all") ) {
                sendErrorMessage(sender,
                        "error_no_permission", "centry.leave.all");
                return true;
            }

            if ( parent.getParticipants().size() <= 0 ) {
                sendErrorMessage(sender, "error_already_leave_all");
                return true;
            }

            ArrayList<String> participants = new ArrayList<String>(parent.getParticipants());
            for ( String name : participants ) {
                parent.removeParticipant(name);
                Player player = Utility.getPlayerExact(name);
                if ( player != null ) {
                    sendInfoMessage(player, "info_leave");
                }
            }
            sendInfoMessage(sender, "info_leave_all");

        } else {
            // 指定したプレイヤーをエントリーから離脱させる

            if ( !sender.hasPermission("centry.leave.other") ) {
                sendErrorMessage(sender,
                        "error_no_permission", "centry.leave.other");
                return true;
            }

            target = Utility.getPlayerExact(args[1]);
            if ( target == null ) {
                sendErrorMessage(sender, "error_player_not_found");
                return true;
            }

            boolean result = parent.removeParticipant(target);
            if ( !result ) {
                sendErrorMessage(sender, "error_already_leave_other", target.getName());
                return true;
            } else {
                sendInfoMessage(sender, "info_leave_other", target.getName());
                sendInfoMessage(target, "info_leave");
            }
        }

        // 自動開始タイマーが動作していて、既定の参加人数を下回ったなら、タイマーをキャンセルする
        ColorTeamingEntryConfig config = parent.getCTEConfig();
        if ( timer != null && !timer.isEnd() &&
                config.getAutoStartTimerPlayerNum() > parent.getParticipants().size() ) {
            timer.cancel();
            timer = null;
            broadcastInfoMessage("auto_start_timer_cancel");
        }

        return true;
    }

    /**
     * リストコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doList(CommandSender sender, Command command, String label, String[] args) {

        // 参加者リストを表示する
        sendMessage(sender, "list_first_line", parent.getParticipants().size());
        int count = 0;
        StringBuffer buffer = new StringBuffer();
        for ( String name : parent.getParticipants() ) {
            Player player = Utility.getPlayerExact(name);
            ChatColor color = (player != null) ? ChatColor.WHITE : ChatColor.GRAY;
            if ( count == 0 ) {
                buffer.append(color + name);
            } else {
                buffer.append(", " + color + name);
            }
            count++;
            if ( count >= 5 ) {
                sender.sendMessage(buffer.toString());
                buffer = new StringBuffer();
                count = 0;
            }
        }
        if ( count > 0 ) {
            sender.sendMessage(buffer.toString());
        }
        sendMessage(sender, "list_last_line");

        return true;
    }

    /**
     * アナウンスコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doAnnounce(CommandSender sender, Command command, String label, String[] args) {

        // 参加者リストを表示する
        broadcastMessage("list_first_line", parent.getParticipants().size());
        int count = 0;
        StringBuffer buffer = new StringBuffer();
        for ( String name : parent.getParticipants() ) {
            Player player = Utility.getPlayerExact(name);
            ChatColor color = (player != null) ? ChatColor.WHITE : ChatColor.GRAY;
            if ( count == 0 ) {
                buffer.append(color + name);
            } else {
                buffer.append(", " + color + name);
            }
            count++;
            if ( count >= 5 ) {
                Bukkit.broadcastMessage(buffer.toString());
                buffer = new StringBuffer();
                count = 0;
            }
        }
        if ( count > 0 ) {
            Bukkit.broadcastMessage(buffer.toString());
        }
        broadcastMessage("list_last_line");

        return true;
    }

    /**
     * オープンコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doOpen(CommandSender sender, Command command, String label, String[] args) {

        // 既にオープンなら、エラー終了
        if ( parent.isOpen() ) {
            sendErrorMessage(sender, "error_already_open");
            return true;
        }

        // ExpTimerを確認する設定で、ExpTimerがロードされていて、
        // タイマーが動作しているなら、エラー終了。
        ColorTeamingEntryConfig config = parent.getCTEConfig();
        if ( config.isDisableOpenOnRunningExpTimer() &&
                parent.getExpTimer() != null &&
                parent.getExpTimer().isTimerRunning() ) {
            sendErrorMessage(sender, "error_exptimer_working");
            return true;
        }

        // 引数にコンフィグが指定されていて、
        // 自動開始タイマー有効かつコマンド設定名が無効なら、エラー終了
        if ( args.length >= 2 && config.isAutoStartTimer() &&
                !config.getAutoStartTimerCommandConfigs().containsKey(args[1]) ) {
            sendErrorMessage(sender, "error_not_exist_config");
            return true;
        }

        // タイマーに使用するコマンドを記録しておく
        if ( args.length >= 2 && config.isAutoStartTimer() ) {
            timerCommands = config.getAutoStartTimerCommandConfigs().get(args[1]);
        } else {
            timerCommands = config.getAutoStartTimerCommands();
        }

        // この時点でオンラインだったリストプレイヤーは、名前色を付ける
        ArrayList<String> offlines = new ArrayList<String>();
        for ( String name : parent.getParticipants() ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                player.setPlayerListName(
                        parent.getCTEConfig().getEntryColor() + player.getName());
            } else {
                offlines.add(name);
            }
        }

        for ( String name : offlines ) {
            parent.removeParticipant(name);
        }

        // 募集開始
        parent.setOpen(true);

        // 募集開始を通知する
        broadcastInfoMessage("info_open1");
        broadcastInfoMessage("info_open2");
        broadcastInfoMessage("info_open3");

        // 自動開始タイマーが有効で、既に人数を超えているなら、タイマーを作成して開始する
        if ( config.isAutoStartTimer() &&
                config.getAutoStartTimerPlayerNum() <= parent.getParticipants().size() ) {

            timer = new AutoStartTimer(parent, config.getAutoStartTimerSeconds(), timerCommands);
            timer.startTimer();
        }

        return true;
    }

    /**
     * クローズコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doClose(CommandSender sender, Command command, String label, String[] args) {

        // 既にクローズなら、エラー終了
        if ( !parent.isOpen() ) {
            sendErrorMessage(sender, "error_already_close");
            return true;
        }

        // この時点でオフラインだったプレイヤーは、リストから外す
        // オンラインだったプレイヤーは、名前色を消す
        ArrayList<String> offlines = new ArrayList<String>();
        for ( String name : parent.getParticipants() ) {
            Player player = Utility.getPlayerExact(name);
            if ( player != null ) {
                player.setPlayerListName(player.getName());
            } else {
                offlines.add(name);
            }
        }

        for ( String name : offlines ) {
            parent.removeParticipant(name);
        }

        // 募集しめきり
        parent.setOpen(false);

        // 通知する
        broadcastInfoMessage("info_close");

        // タイマーが残っているなら、キャンセルして除去する
        if ( timer != null && !timer.isEnd() ) {
            timer.cancel();
            timer = null;
        }

        return true;
    }

    /**
     * チームコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doTeam(CommandSender sender, Command command, String label, String[] args) {

//        // 受け付けが開始されていないなら終了する
//        if ( !parent.isOpen() ) {
//            sendErrorMessage(sender, "error_closed_team");
//            return true;
//        }

        // 作成するチーム数
        int teamNum = 2;
        if ( args.length >= 2 && args[1].matches("[2-9]") ) {
            teamNum = Integer.parseInt(args[1]);
        }

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

        // 誰も居ないなら終了する
        if ( players.size() == 0 ) {
            sendErrorMessage(sender, "error_zero_player");
            return true;
        }

        // プレイヤーが足りないなら終了する。
        if ( players.size() < teamNum ) {
            sendErrorMessage(sender, "error_too_few_player");
            return true;
        }

        // リストの色を消す
        for ( Player player : players ) {
            player.setPlayerListName(player.getName());
        }

        // チーム分けする
        parent.getColorTeaming().makeColorTeams(players, teamNum);
        sendInfoMessage(sender, "info_team_done");

        // 受け付けを停止する
        parent.setOpen(false);

        // タイマーが残っているなら、キャンセルして除去する
        if ( timer != null && !timer.isEnd() ) {
            timer.cancel();
            timer = null;
        }

        return true;
    }

    /**
     * リロードコマンドの実行
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    private boolean doReload(CommandSender sender, Command command, String label, String[] args) {

        Messages.initialize();
        parent.reloadCTEConfig();
        sendInfoMessage(sender, "info_reload");
        return true;
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
     * メッセージリソースを取得し、Stringを返す
     * @param key メッセージキー
     * @param args メッセージの引数
     */
    private void sendMessage(
            CommandSender sender, String key, Object... args) {
        String msg = Messages.get(key, args);
        if ( msg.equals("") ) {
            return;
        }
        sender.sendMessage(Utility.replaceColorCode(msg));
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

    /**
     * 指定されたコマンドが有効かどうかを返す
     * @param command コマンド
     * @return 有効かどうか
     */
    private boolean isValidCommand(String command) {
        for ( String c : COMMANDS ) {
            if ( c.equals(command) ) {
                return true;
            }
        }
        return false;
    }
}
