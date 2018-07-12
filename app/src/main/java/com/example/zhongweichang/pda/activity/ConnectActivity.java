package com.example.zhongweichang.pda.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.EventLogTags;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zhongweichang.pda.R;
import com.example.zhongweichang.pda.common.Constants;
import com.example.zhongweichang.pda.common.EventMsg;
import com.example.zhongweichang.pda.common.WsrHelper;
import com.example.zhongweichang.pda.service.SocketService;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConnectActivity extends AppCompatActivity {
    private static final String TAG = "ConnectActivity";
    @BindView(R.id.ipTv)
    EditText ipTv;
    @BindView(R.id.portTv)
    EditText portTv;
    @BindView(R.id.connectBtn)
    Button connectBtn;

    private boolean isConnectSuccess = false;
    WsrHelper wsrHelper = null;


    class WSAsyncTask extends AsyncTask{
        String result = null;
        // 在UI线程执行
        @Override
        protected void onPostExecute (Object o) {
            if (o != null && o.toString().equals("login#")) {
                Toast.makeText(ConnectActivity.this, "连接PDAService成功！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ConnectActivity.this, MainActivity.class);
                ConnectActivity.this.startActivity(intent);
                ConnectActivity.this.finish();
                Logger.d("连接PDAService成功");
            }else if (o.toString().equals("login&")){
                Toast.makeText(ConnectActivity.this, "连接PDAService失败，请检查服务状态！", Toast.LENGTH_SHORT).show();
            }
        }


        @Override
        protected Object doInBackground(Object... params){
            return wsrHelper.Invoke("login");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        ButterKnife.bind(this);


        /*register EventBus*/
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Logger.addLogAdapter(new AndroidLogAdapter());
    }


    @OnClick(R.id.connectBtn)
    public void onViewClicked() {

//        String ip = ipTv.getText().toString().trim();
//        String port = portTv.getText().toString().trim();
//
//        if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
//            Toast.makeText(this, "ip和端口号不能为空", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        /*先判断 Service是否正在运行 如果正在运行  给出提示  防止启动多个service*/
//        if (isServiceRunning("com.example.zhongweichang.pda.service.SocketService")) {
//            Toast.makeText(this, "连接服务已运行", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//
//        /*启动service*/
//        Intent intent = new Intent(getApplicationContext(), SocketService.class);
//        intent.putExtra(Constants.INTENT_IP, ip);
//        intent.putExtra(Constants.INTENT_PORT, port);
//        startService(intent);

        String ip = ipTv.getText().toString().trim();
        String port = portTv.getText().toString().trim();
        if (wsrHelper == null)
            wsrHelper = new WsrHelper(ip, port);

        // 异步执行调用WebService的任务
        new WSAsyncTask().execute();
    }


    /*连接成功的话  直接进去主页面*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void skipToMainActivity(EventMsg msg) {
        if (msg.getTag().equals(Constants.CONNET_SUCCESS)) {
            /*接收到这个消息说明连接成功*/
            isConnectSuccess = true;
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            //moveTaskToBack(true);
        }

    }


    /**
     * 判断服务是否运行
     */
    private boolean isServiceRunning(final String className) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) return false;
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName()))
                return true;
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
          /*unregister EventBus*/
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        /*如果没有连接成功  则退出的时候停止服务 */
        if (!isConnectSuccess) {
            Intent intent = new Intent(this, SocketService.class);
            stopService(intent);
        }
    }
}
