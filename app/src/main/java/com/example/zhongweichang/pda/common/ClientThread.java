package com.example.zhongweichang.pda.common;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by zhongweichang on 2018/7/10.
 */

public class ClientThread implements Runnable{
    private static final String TAG = "ClientThread";
    private Socket s;
    //定义向UI线程发送消息的Handler对象
    Handler handler;
    //定义接收UI线程的Handler对象
    public Handler revHandler;
    //该线程处理Socket所使用的输入输出流
    BufferedReader br = null;
    OutputStream out = null;

    public ClientThread(Handler handler){
        this.handler = handler;
    }

    @Override
    public void run () {
        s = new Socket();
        try{
            s.connect(new InetSocketAddress("192.168.1.183", 2000), 5000);
            br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = s.getOutputStream();
            //启动一个子线程来读取服务器相应的数据
            new Thread(){

                @Override
                public void run(){
                    Log.d(TAG, "run: ");
                    String content = null;
                    //不断的读取Socket输入流的内容
                    try{
                        while ((content = br.readLine().toString()) != null){
                            //每当读取到来自服务器的数据之后，发送消息通知程序
                            //界面显示该数据
                            Log.d(TAG, content);
                            Message msg = new Message();
                            msg.what = TypeDefine.MSG_SVR_RESPOND;
                            msg.obj = content;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Log.d(TAG, "run: done");
                }
            }.start();

            //为当前线程初始化Looper;
            Looper.prepare();
            //创建revHandler对象
            revHandler = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    //接收到UI线程中用户输入的数据
                    if (msg.what == TypeDefine.MSG_CLI_RESQUEST){
                        //将用户输入的内容写入到网络
                        try{
                            out.write((msg.obj.toString() + "\r\n").getBytes("gbk"));

                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            //启动Looper
            Looper.loop();

        } catch (SocketTimeoutException e) {
            Message msg = new Message();
            msg.what = TypeDefine.MSG_NET_ERROR;
            msg.obj = "网络连接超时...";
            handler.sendMessage(msg);
        }
        catch (ConnectException e){
            Message msg = new Message();
            msg.what = TypeDefine.MSG_NET_ERROR;
            msg.obj = "连接服务器失败...";
            handler.sendMessage(msg);
        } catch (IOException io){
            io.printStackTrace();
        }

    }
}
