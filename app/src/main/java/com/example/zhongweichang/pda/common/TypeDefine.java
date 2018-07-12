package com.example.zhongweichang.pda.common;

/**
 * Created by zhongweichang on 2018/7/10.
 */

public abstract class TypeDefine {
    public static final int MSG_NET_ERROR = 0X11;   //连接服务器失败
    public static final int MSG_NET_NORMAL = 0X12;  //连接服务器正常
    public static final int MSG_SVR_RESPOND = 0X13; //响应服务器消息
    public static final int MSG_CLI_RESQUEST = 0X14;   //客户端请求数据
}
