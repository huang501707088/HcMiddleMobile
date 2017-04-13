package com.android.hcframe.command;


import android.content.Context;
import android.os.Parcelable;
import android.util.SparseArray;

import com.android.hcframe.HcLog;

import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-2-15 14:36.
 */
public class CommandControl {

    private static final String TAG = "CommandControl";

    private static CommandControl mController;

    private SparseArray<Command> mCommands = new SparseArray<Command>();

    private final Command mNoCommand = new NoCommand();

    /** 有邮箱应用模块,启动应用时,对邮箱应用模块的一些初始化 */
    public static final int FLAG_INIT_EMAIL = 1;

    /** 启动应用时,对通讯录应用模块的一些初始化 */
    public static final int FLAG_INIT_CONTACTS = FLAG_INIT_EMAIL << 1;

    /** 通讯录详情页面写邮件 */
    public static final int FLAG_CONTACTS_WRITE_EMAIL = FLAG_INIT_CONTACTS << 1;

    public static CommandControl getInstance() {
        if (mController == null) {
            synchronized (CommandControl.class) {
                if (mController == null) {
                    mController = new CommandControl();
                }
            }
        }
        return mController;
    }

    private CommandControl() {
    }

    public void setCommand(int commandFlag, Command command) {
        mCommands.append(commandFlag, command);
    }

    public void setCommand(int commandFlag, String className) {
        try {
            Class<?> commandClass= Class.forName(className);
            Command command = (Command) commandClass.newInstance();
            mCommands.append(commandFlag, command);
        } catch (ClassNotFoundException e) {
            // TODO: handle exception
            HcLog.D(TAG + " ClassNotFoundException e = "+e + " className = "+className);
        } catch (Exception e) {
            // TODO: handle exception
            HcLog.D(TAG + " Exception e = "+e);
        }

    }

    public void removeCommand(int commandFlag) {
        mCommands.remove(commandFlag);
    }

    public void removeCommand(Command command) {
        int index = mCommands.indexOfValue(command);
        mCommands.removeAt(index);
    }

    public void sendCommand(Context context, int commandFlag) {
        Command command = getCommand(commandFlag);
        command.execute(context);
    }

    public void sendCommand(Context context, int commandFlag, Parcelable p) {
        Command command = getCommand(commandFlag);
        command.execute(context, p);
    }

    public void sendCommand(Context context, int commandFlag, Map<String, String> data) {
        Command command = getCommand(commandFlag);
        command.execute(context, data);
    }

    public Command getCommand(int commandFlag) {
        return mCommands.get(commandFlag, mNoCommand);
    }

    public static class NoCommand implements Command {

        @Override
        public void execute(Context context) {

        }

        @Override
        public void execute(Context context, Parcelable p) {

        }

        @Override
        public void execute(Context context, Map<String, String> data) {

        }
    }
}
