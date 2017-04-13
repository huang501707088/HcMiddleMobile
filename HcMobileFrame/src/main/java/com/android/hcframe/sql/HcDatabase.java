/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-4-27 上午10:21:32
 */
package com.android.hcframe.sql;

import android.provider.BaseColumns;

public final class HcDatabase {

    private HcDatabase() {
    }

    /**
     * 保存在本地的应用列表
     *
     * @author jrjin
     * @time 2015-5-28 下午2:23:37
     */
    public static final class HcAppMarket implements BaseColumns {

        private HcAppMarket() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "app_market";
        /**
         * 应用ID
         */
        public static final String APP_ID = "app_id";
        /**
         * 应用名
         */
        public static final String APP_NAME = "app_name";
        /**
         * 应用安装的当前版本
         */
        public static final String APP_VERSION = "app_version";
        /**
         * 应用包名
         */
        public static final String APP_PACKAGE = "app_package";
        /**
         * 应用下载地址，或者html的主页面地址
         */
        public static final String APP_URL = "app_url";
        /**
         * 应用ICON,以Url形式存在
         */
        public static final String APP_ICON = "app_icon";
        /**
         * 应用类型 原生或者Html
         */
        public static final String APP_TYPE = "app_type";
        /**
         * 应用分类
         */
        public static final String APP_CATEGORY = "app_category";
        /**
         * 应用在本地的状态 0:未安装；1：已安装；2：可更新
         */
        public static final String APP_STATE = "app_state";
        /** 应用最新的版本 */
        // public static final String APP_LATEST_VERSION = "app_latest_version";
        /**
         * 应用大小
         */
        public static final String APP_SIZE = "app_size";
        /**
         * 应用在全部列表中的排序
         */
        public static final String APP_ORDER_ALL = "app_order_all";
        /**
         * 应用在类别列表中的排序
         */
        public static final String APP_ORDER_CATEGORY = "app_order_category";
        /**
         * 当前登录用户
         */
        public static final String APP_ACCOUNT = "account";//"app_account";
        /**
         * 应用是否使用过 0:未使用；1：已使用
         */
        public static final String APP_USED = "app_used";
        /**
         * 应用分类名
         */
        public static final String APP_CATEGORY_NAME = "app_category_name";
        /**
         * 应用在服务端的排序
         */
        public static final String APP_ORDER_SERVER = "app_order_server";

    }

    /**
     * 服务端返回的应用列表
     *
     * @author jrjin
     * @time 2015-5-28 下午2:23:09
     */
    public static final class HcInstallApp implements BaseColumns {

        private HcInstallApp() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "app_install";
        /**
         * 应用ID
         */
        public static final String APP_ID = "app_id";
        /**
         * 应用名
         */
        public static final String APP_NAME = "app_name";
        /**
         * 应用版本
         */
        public static final String APP_VERSION = "app_version";
        /**
         * 应用包名
         */
        public static final String APP_PACKAGE = "app_package";
        /**
         * 当前登录用户
         */
        public static final String APP_ACCOUNT = "account";//"app_account";
    }

    public static final class HcAppOperLog implements BaseColumns {

        private HcAppOperLog() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "app_log";
        /**
         * 进入时间
         */
        public static final String START = "start_time";
        /**
         * 退出时间
         */
        public static final String END = "end_time";
        /**
         * 用户
         */
        public static final String ACCOUNT = "account";
        /**
         * 设备标示符
         */
        public static final String IMEI = "imei";
        /**
         * 应用ID
         */
        public static final String APP_ID = "app_id";
        /**
         * 操作类型 1:应用程序；2：应用模块；3：接口请求
         */
        public static final String TYPE = "type";
        /**
         * 应用版本
         */
        public static final String VERSION = "version";
        /**
         * 应用模块ID
         */
        public static final String MODULE_ID = "module_id";
        /**
         * 应用程序名/应用模块名/接口名
         */
        public static final String NAME = "name";
        /**
         * 接口请求是否成功 0：成功；1：失败
         */
        public static final String RESULT = "result";
    }

    public static final class HcNewsColumen implements BaseColumns {

        private HcNewsColumen() {

        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "app_newscolumns";
        /**
         * 新闻栏目编号
         */
        public static final String NEWSID = "newsid";
        /**
         * 新闻栏目类型：0：图片+文字 1：文字 2：图片
         */
        public static final String TYPE = "type";
        /**
         * 新闻内容类型：0在线编辑1文本导入，2互联网链接
         */
        public static final String CONTENTTYPE = "contenttype";
        /**
         * 新闻栏目名
         */
        public static final String NAME = "name";
        /**
         * 上传滚动图片:0:否1是
         */
        public static final String ISSCROLLTOPIC = "isscrolltopic";
        /**
         * 用户
         */
        public static final String NEWS_USER = "account";//"news_account";
        /**
         * 栏目类型： 0-新闻栏目;1-资料中心栏目
         */
        public static final String COLUMN_TYPE = "column_type";
    }

    public static final class DataRecord implements BaseColumns {

        private DataRecord() {
        }

        /**
         * 历史使用记录表名
         */
        public static final String TABLE_NAME = "doc_record";
        /**
         * 资料的名字，也可能是文件的名字
         */
        public static final String DATA_NAME = "doc_name";
        /**
         * 文件的大小
         */
        public static final String DATA_SIZE = "doc_size";
        /**
         * 资料的ID
         */
        public static final String DATA_ID = "doc_id";
        /**
         * 最后一次读取文件的时间，显示列表的时候按照这个排序
         */
        public static final String DATA_READ_TIME = "doc_readTime";
        /**
         * 资料显示类型：0—标题，1—主文件，2—附件
         */
        public static final String DATA_FLAG = "data_flag";
        /**
         * 登陆用户名
         */
        public static final String ACCOUNT = "account";
        /**
         * 日期
         */
        public static final String DATA_DATE = "mdate";
    }

    public static final class DataRecordDetail implements BaseColumns {

        private DataRecordDetail() {
        }

        /**
         * 历史使用记录表名
         */
        public static final String TABLE_NAME = "doc_recordDetail";
        /**
         * 资料的名字
         */
        public static final String DATA_NAME = "doc_name";
        /**
         * 资料的ID
         */
        public static final String DATA_ID = "doc_id";
        /**
         * 资料显示类型：1—主文件，2—附件
         */
        public static final String DATA_FLAG = "data_flag";
        /**
         * 资料的出处
         */
        public static final String DATA_SOURCE = "doc_source";
        /**
         * 资料发布日期
         */
        public static final String DATA_TIME = "doc_time";
        /**
         * 文件的大小
         */
        public static final String FILE_SIZE = "doc_fileSize";
        /**
         * 文件的名字
         */
        public static final String FILE_NAME = "doc_fileName";
        /**
         * 文件的编号
         */
        public static final String FILE_ID = "doc_fileId";
        /**
         * 文件的内容地址
         */
        public static final String FILE_URL = "doc_fileUrl";
        /**
         * 登陆用户名
         */
        public static final String ACCOUNT = "account";
    }

    public static final class Contacts implements BaseColumns {

        private Contacts() {
        }

        /**
         * 通讯录表名
         */
        public static final String TABLE_NAME = "hc_contacts";
        /**
         * 员工工号/部门编号
         */
        public static final String ID = "id";
        /**
         * 员工姓名/部门名称
         */
        public static final String NAME = "name";
        /**
         * 类型：0—员工，1—部门
         */
        public static final String TYPE = "type";
        /**
         * 部门名称/上级部门名称
         */
        public static final String PARENT_NAME = "parentName";
        /**
         * 部门编号/上级部门编号
         */
        public static final String PARENT_ID = "parentId";
        /**
         * 手机号码
         */
        public static final String MOBILE_PHONE = "mobilePhone";
        /**
         * 备用手机号码
         */
        public static final String STANDBY_PHONE = "standbyPhone";
        /**
         * 固定号码
         */
        public static final String FIXED_PHONE = "fixedPhone";
        /**
         * 分机号
         */
        public static final String EXTENSION_NUMBER = "extensionNumber";
        /**
         * 虚拟网号
         */
        public static final String VIRTUAL_NET_NUMBER = "virtualNetNubmer";
        /**
         * 员工邮箱
         */
        public static final String EMAIL = "email";
        /**
         * 备用邮箱
         */
        public static final String STANDBY_EMAIL = "standbyEmail";
        /**
         * 名字全拼
         */
        public static final String NAME_QUANPIN = "quanpin";
        /**
         * 名字简拼
         */
        public static final String NAME_JAINPIN = "jianpin";
        /**
         * 名字字母排序
         */
        public static final String NAME_A = "name_a";
        /**
         * 显示两个汉字
         */
        public static final String NAME_ICON = "icon";
        /**
         * 是否隐藏移动电话 0：不隐藏；1：隐藏
         */
        public static final String VISIBILITY = "visibility";
        /**
         * 用户的id
         */
        public static final String USER_ID = "user_id";
    }

    public static final class SysMessage implements BaseColumns {

        private SysMessage() {
        }

        /**
         * 系统消息表名
         */
        public static final String TABLE_NAME = "hc_system";
        /**
         * 消息标题
         */
        public static final String TITLE = "title";
        /**
         * 消息内容描述
         */
        public static final String CONTENT = "content";
        /**
         * 消息具体内容,由具体的模块定义,可能是ID,也可能是具体的类名.
         */
        public static final String CONTENT_ID = "content_id";
        /**
         * 消息接收时间
         */
        public static final String DATE = "date";
        /**
         * 消息是否已读 0：已读;1：未读
         * @deprecated
         */
        public static final String READ = "read";
        /**
         * 消息应用ID
         */
        public static final String APP_ID = "app_id";
        /**
         * 消息类型,由具体的模块定义
         */
        public static final String TYPE = "type";
        /**
         * 登录帐号
         */
        public static final String ACCOUNT = "account";//"accout";
        /**
         * 是否原生应用0:原生;1:html;默认为-1
         */
        public static final String APP_TYPE = "app_type";

        /**
         * 每个模块首页的url或者className
         */
        public static final String INDEX_CONTENT = "index_content";

        /** 应用的名字 */
        public static final String APP_NAME = "app_name";

    }

    public static final class AnnualProgram implements BaseColumns {

        private AnnualProgram() {
        }

        /**
         * 年会节目表名
         */
        public static final String TABLE_NAME = "annual_program";
        /**
         * 节目标题
         */
        public static final String TITLE = "title";
        /**
         * 节目内容
         */
        public static final String CONTENT = "content";
        /**
         * 节目ID
         */
        public static final String PROGRAM_ID = "program_id";
        /**
         * 节目评分
         */
        public static final String SCORE = "score";
        /**
         * 节目类型
         * 1、歌曲声乐
         * 2、舞蹈表演
         * 3、情景相声
         */
        public static final String TYPE = "type";
        /**
         * 登录帐号
         */
        public static final String ACCOUNT = "account";//"accout";
        /**
         * 年会标识
         */
        public static final String ANNUAL_ID = "annual_id";
    }

    public static final class NewsList implements BaseColumns {

        private NewsList() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "news_list";
        /**
         * 新闻栏目编号
         */
        public static final String NEWS_COLUMN_ID = "news_column_id";
        /**
         * 新闻编号
         */
        public static final String NEWS_ID = "news_id";
        /**
         * 新闻内容
         */
        public static final String NEWS_CONTENT = "news_content";
        /**
         * 新闻内容类型：0在线编辑;1文本导入;2互联网链接;3图片新闻
         */
        public static final String CONTENT_TYPE = "content_type";
        /**
         * 新闻栏目名
         */
        public static final String NEWS_TITLE = "news_title";
        /**
         * 是否为滚动图片:0:否；1：是 暂时不用
         */
        public static final String SCROLL = "scroll";
        /**
         * 用户
         */
        public static final String ACCOUNT = "account";
        /**
         * 新闻编辑地址
         */
        public static final String NEWS_ADDRESS = "news_address";
        /**
         * 新闻图片链接,要是图片新闻的话,则用分号隔开
         */
        public static final String NEWS_ICON_URI = "news_icon_uri";
        /**
         * 新闻发布时间
         */
        public static final String NEWS_DATA = "news_data";
        /**
         * 新闻内容链接
         */
        public static final String NEWS_CONTENT_URI = "news_content_uri";
        /**
         * 图片新闻的图片张数
         */
        public static final String NEWS_PIC_COUNT = "news_pic_count";

    }

    public static final class Badge implements BaseColumns {

        private Badge() {
        }

        /**
         * 登录帐号
         */
        public static final String ACCOUNT = "account";//"accout";
        /**
         * 表名
         */
        public static final String TABLE_NAME = "badge";
        /**
         * 客户端ID/应用ID
         */
        public static final String APP_ID = "app_id";
        /**
         * 角标类型 1、数字型
         * 2、圆点型
         */
        public static final String BADGE_TYPE = "badge_type";
        /**
         * 应用ID/模块ID
         */
        public static final String MODULE_ID = "module_id";
        /**
         * 角标数
         */
        public static final String COUNT = "count";
        /**
         * 角标是否可见,有数量不一定可见 0：不显示 1：显示
         */
        public static final String VISIBILITY = "visibility";
        /**
         * 数据类型 0：应用角标数据 1：模块角标数据
         */
        public static final String TYPE = "type";

    }

    public static final class UploadFile implements BaseColumns {

        private UploadFile() {
        }

        /**
         * 登录帐号
         */
        public static final String ACCOUNT = "account";//"accout";
        /**
         * 表名
         */
        public static final String TABLE_NAME = "upload";
        /**
         * 文件唯一标示
         */
        public static final String FILEKEY = "filekey";
        /**
         * 文件秒传标示
         */
        public static final String MD5 = "md5";
        /**
         * 文件路径
         */
        public static final String PATH = "path";
        /**
         * 分片总数
         */
        public static final String ALL_SLICE = "all_slice";
        /**
         * 传输的分片数
         */
        public static final String SLICE = "slice";
        /**
         * 文件名
         */
        public static final String NAME = "name";
        /**
         * 文件类型
         */
        public static final String EXT = "ext";
        /**
         * 文件是否分片
         */
        public static final String TYPE = "type";
        /**
         * 文件读取位置
         */
        public static final String POSITION = "position";
        /**
         * 状态
         */
        public static final String STATE = "state";
        /**
         * 文件大小
         */
        public static final String FILESIZE = "filesize";
        /**
         * 文件上传目录
         */
        public static final String DIRID = "dirid";
        /**
         * 文件上传url
         */
        public static final String URL = "url";

    }

    public static final class DownloadFile implements BaseColumns {

        private DownloadFile() {
        }

        /**
         * 登录帐号
         */
        public static final String ACCOUNT = "account";//"accout";
        /**
         * 表名
         */
        public static final String TABLE_NAME = "download";
        /**
         * 文件ID
         */
        public static final String FILEId = "fileid";
        /**
         * 文件名
         */
        public static final String NAME = "name";
        /**
         * 文件类型
         */
        public static final String EXT = "ext";
        /**
         * 文件当前级数ID
         */
        public static final String UPDIRId = "updirid";
        /**
         * 文件路径
         */
        public static final String PATH = "path";
        /**
         * 文件大小
         */
        public static final String FILESIZE = "filesize";
        /**
         * 文件下载位置
         */
        public static final String POSITION = "position";
        /**
         * 状态（0：下载，1，暂停，2等待）
         */
        public static final String STATE = "state";
        /**
         * 文件上传url
         */
        public static final String URL = "url";

    }

    public static final class TaskInfo implements BaseColumns {

        private TaskInfo() {
        }

        /**
         * 任务表名
         */
        public static final String TABLE_NAME = "task_info";
        /**
         * 任务编号
         */
        public static final String ID = "id";
        /**
         * 发布者
         */
        public static final String PUBLISHER = "publisher";
        /**
         * 任务状态
         * 0:待接收；1：进行中；2：已完成；3：已结束；4：已取消
         */
        public static final String STATUS = "status";
        /**
         * 执行者
         */
        public static final String EXECUTOR = "executor";
        /**
         * 发布者UserID
         */
        public static final String PUBLISHER_ID = "publisher_id";
        /**
         * 执行者UserID
         */
        public static final String EXECUTOR_ID = "executor_id";
        /**
         * 发布日期
         */
        public static final String RELEASE_DATE = "release_date";
        /**
         * 截止时间
         */
        public static final String DEADLINE = "deadline";
        /**
         * 任务内容
         */
        public static final String CONTENT = "content";
        /**
         * 发布者头像url
         */
        public static final String PUBLISHER_URL = "publisher_url";
        /**
         * 执行者头像url
         */
        public static final String EXECUTOR_URL = "executor_url";
        /**
         * 当前登录的用户ID
         */
        public static final String USER_ID = "user_id";

    }

    /**
     * 消息列表的数据存储,角标的数据还是存储在原来的表中
     */
    public static final class AppMessage implements BaseColumns {

        private AppMessage() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "app_message";
        /**
         * 登录帐号的ID
         */
        public static final String USER_ID = "user_id";
        /**
         * 消息应用的ID/群的ID/单聊的ID(双方userId合并的MD5值)/其他(可能是系统的消息ID)
         */
        public static final String MESSAGE_ID = "message_id";
        /**
         * 消息类型 1.应用模块推送的消息
         * 2.群聊的消息
         * 3.单聊的消息
         * 4.系统的消息
         */
        public static final String MESSAGE_TYPE = "type";
        /**
         * 消息的发布最后日期
         */
        public static final String MESSAGE_DATE = "date";
        /**
         * 消息的标题,可能是应用的名字,群名字,联系人
         */
        public static final String MESSAGE_TITLE = "title";
        /**
         * 消息的内容
         */
        public static final String MESSAGE_CONTENT = "content";
        /** 消息对象的图标Uri */
        public static final String MESSAGE_ICON = "icon";

        /** 未读消息的数量 */
        public static final String MESSAGE_COUNT = "count";

    }

    /**
     * 聊天的信息,资源的存储的文件夹以CHAT_ID为准
     */
    public static final class ChatMessage implements BaseColumns {

        private ChatMessage() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "chat_message";
        /**
         * 消息的帐号的ID,可能是自己,也可能是其他人
         */
        public static final String USER_ID = "user_id";
        /**
         * 单聊时为双方userId合并的MD5值/群聊时为群的ID
         */
        public static final String Chat_ID = "chat_id";
        /**
         * 消息内容类型 1.文本
         * 2.图片
         * 3.语音
         * 4.文件
         * 5.其他
         */
        public static final String CHAT_TYPE = "type";
        /**
         * 消息的发布日期
         */
        public static final String CHAT_DATE = "date";
        /**
         * 消息是否为本人,虽然可以根据userId判断,0:本人；1:其他人
         */
        public static final String CHAT_OWN = "own";
        /**
         * 消息的内容,根据不同的类型,为不同的内容
         */
        public static final String CHAT_CONTENT = "content";
        /**
         * 发送消息的人的名字,群聊的时候有用
         */
        public static final String CHAT_NAME = "name";

        /** 一条消息的ID */
        public static final String CHAT_MESSAGE_ID = "message_id";

        /**
         * 是否显示时间,需要根据上一条的消息判断.
         * 0:显示;1:不显示
         */
        public static final String CHAT_SHOW_TIME = "show_time";

        /**
         * 发送消息的语音文件或者图片文件的绝对路径
         */
        public static final String CHAT_FILE_NAME = "file_path";

        /** 录音时长 */
        public static final String CHAT_VOICE_DURATION = "voice_duration";

        /** 录音是否已读 0:未读取, 1:已经读取 */
        public static final String CHAT_VOICE_READED = "voice_readed";

        /** 发送给对方的消息的状态 0:已发送;1:未发送2:正在发送;;3:发送失败 */
        public static final String CHAT_SEND_STATE = "state";

        /** 群消息时,@的人员的userId,多人用";"隔开 */
        public static final String CHAT_RECEIVER = "receiver";
    }

    /**
     * IM群组信息
     */
    public static final class ChatGroup implements BaseColumns {

        private ChatGroup() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "chat_group";
        /**
         * 登录帐号的ID
         */
        public static final String USER_ID = "user_id";
        /**
         * 群的ID
         */
        public static final String GROUP_ID = "group_id";
        /**
         * 群名字
         */
        public static final String GROUP_NAME = "group_name";
        /** 群的图标Uri,暂时用不到 */
        public static final String GROUP_ICON = "group_icon";
        /** 群的成员 */
        public static final String GROUP_MEMBERS = "group_members";
        /** 是否消息免打扰 */
        public static final String GROUP_NOTICED = "group_noticed";
        /** 群成员数 */
        public static final String GROUP_COUNT = "group_count";
    }

    /**
     * 日程安排列表的数据存储
     */
    public static final class Schedule implements BaseColumns {

        private Schedule() {
        }

        /**
         * 表名
         */
        public static final String TABLE_NAME = "shedule";
        /**
         * 登录帐号的ID
         */
        public static final String USER_ID = "user_id";
        /**
         * 日程ID
         */
        public static final String SCHEDULE_ID = "schedule_id";

        /**
         * 日程创建日期
         */
        public static final String SCHEDULE_DATE = "schedule_date";

        /**
         * 日程开始时间
         */
        public static final String SCHEDULE_STARTTIME = "schedule_starttime";
        /**
         * 日程结束时间
         */
        public static final String SCHEDULE_ENDTIME = "schedule_endtime";
        ;
        /**
         * 日程的任务类型：外派，内部任务等
         */
        public static final String SCHEDULE_TASKTYPE = "schedule_tasktype";
        /**
         * 日程参与人员,用分号隔开
         */
        public static final String SCHEDULE_TASKMEMBERS = "schedule_taskmembers";
        /**
         * 日程主题
         **/
        public static final String SCHEDULE_THEME = "schedule_theme";
        /**
         * 创建者
         **/
        public static final String SCHEDULE_CREATOR = "schedule_creator";
        /**
         * 是否是创建者
         **/
        public static final String SCHEDULE_CREATFLAG = "schedule_creatflag";
        /**
         * 附件的url,用分号隔开
         **/
        public static final String SCHEDULE_ADDITION = "schedule_addition";

    }
}
