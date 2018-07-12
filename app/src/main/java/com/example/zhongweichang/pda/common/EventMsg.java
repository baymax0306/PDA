package com.example.zhongweichang.pda.common;

/**
 * Created by yzq on 2017/9/27.
 * 传递消息时使用，可以自己增加更多的参数
 */

public class EventMsg {

    private String Tag;
    private String data;

    public String getData(){
        return data;
    }
    public void setData(String data){
        this.data = data;
    }

    public String getTag() {
        return Tag;
    }
    public void setTag(String tag) {
        Tag = tag;
    }
}
