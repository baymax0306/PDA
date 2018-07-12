package com.example.zhongweichang.pda.activity;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.zhongweichang.pda.R;
import com.example.zhongweichang.pda.common.ClientThread;
import com.example.zhongweichang.pda.common.Constants;
import com.example.zhongweichang.pda.common.EventMsg;
import com.example.zhongweichang.pda.common.TypeDefine;
import com.example.zhongweichang.pda.service.SocketService;
import com.joker.annotation.PermissionsDenied;
import com.joker.annotation.PermissionsGranted;
import com.joker.annotation.PermissionsRationale;
import com.joker.api.Permissions4M;
import com.orhanobut.logger.Logger;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import sysu.zyb.panellistlibrary.AbstractPanelListAdapter;
import sysu.zyb.panellistlibrary.PanelListLayout;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_SCAN = 1;
    private PanelListLayout pl_root;
    private ListView lv_content;
    private AbstractPanelListAdapter adapter;
    private List<List<String>> contentList = new ArrayList<>();
    private List<Integer> itemWidthList = new ArrayList<>();
    private List<String> rowDataList = new ArrayList<>();

    @BindView(R.id.editText_barcode)
    EditText barcode;
    @BindView(R.id.editText_medname)
    EditText medname;

    private String host  = "192.168.1.183";
    private int port = 2000;
    Handler handler;
    ClientThread clientThread;

    //申请权限
    String permissions[] = {Manifest.permission.CAMERA};
    private static final int REQUEST_CAMERA_CODE = 1;
    private ServiceConnection sc;
    public SocketService socketService;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();

        initRowDataList();
        initContentDataList();
        initItemWidthList();

        initAdapter();
        pl_root.setAdapter(adapter);

        // 注意：
        // 如果你决定自己实现自己的Column，而不是使用默认的1，2，3。。。
        // 请注意更新contentList时手动更新columnList
        //initSocket();
        addListener();

        bindSocketService();

        /*register EventBus*/
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        Logger.d("进入加药界面！");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

           /*unregister EventBus*/
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        unbindService(sc);
        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        stopService(intent);
       }


    private void bindSocketService () {
        /*通过binder拿到service*/
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected (ComponentName name, IBinder service) {
                SocketService.SocketBinder binder = (SocketService.SocketBinder)service;
                socketService = binder.getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };


        Intent intent = new Intent(getApplicationContext(), SocketService.class);
        bindService(intent, sc, BIND_AUTO_CREATE);
    }

    private void addListener () {
        //申请权限
        Permissions4M.get(MainActivity.this)
        // 是否强制弹出权限申请对话框，建议为 true
        .requestForce(true)
        // 申请权限
        .requestPermissions(Manifest.permission.CAMERA)
        // 权限码
        .requestCodes(REQUEST_CAMERA_CODE)
        // 如果需要使用 @PermissionNonRationale 注解的话，建议添加如下一行
        // 返回的 intent 是跳转至**系统设置页面**
        //.requestPageType(Permissions4M.PageType.MANAGER_PAGE)
        // 返回的 intent 是跳转至**手机管家页面**
        .requestPageType(Permissions4M.PageType.ANDROID_SETTING_PAGE)
        .request();

    }


    //------------------------------------------------------------------------------------
    //授权成功时回调
    @PermissionsGranted({REQUEST_CAMERA_CODE})
    public void granted(int code) {
        switch (code) {
            case REQUEST_CAMERA_CODE:
                //Toast.makeText(this, "权限申请成功", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    //授权失败时回调
    @PermissionsDenied({REQUEST_CAMERA_CODE})
    public void denied(int code) {
        switch (code) {
            case REQUEST_CAMERA_CODE:
                Toast.makeText(this, "权限申请失败", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    //二次授权时回调，用于解释为何需要此权限
    @PermissionsRationale({REQUEST_CAMERA_CODE})
    public void rationale(int code) {
        switch (code) {
            case REQUEST_CAMERA_CODE:
                Toast.makeText(this, "请开启相机权限", Toast.LENGTH_SHORT).show();
                break;
           }
    }


    private void initSocket () {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                switch (msg.what)
                {
                    case TypeDefine.MSG_SVR_RESPOND:
                        medname.setText(msg.obj.toString());
                        break;

                    case TypeDefine.MSG_NET_ERROR:
                        setTitle(msg.obj.toString());
                        break;
                }
            }
        };

        clientThread = new ClientThread(handler);
        //客户端启动clientThread线程创建网络连接、读取来自服务器的数据
        new Thread(clientThread).start();
    }


    private void initAdapter(){
        adapter = new AbstractPanelListAdapter(this, pl_root, lv_content) {
            @Override
            protected BaseAdapter getContentAdapter() {
                return null;
            }
        };
        adapter.setInitPosition(10);
        adapter.setSwipeRefreshEnabled(true);
        adapter.setRowDataList(rowDataList);// must have
        adapter.setTitle("序号");// optional
        adapter.setTitleWidth(0);
        adapter.setOnRefreshListener(new CustomRefreshListener());// optional
        adapter.setContentDataList(contentList);// must have
        adapter.setItemWidthList(itemWidthList);// must have
        adapter.setItemHeight(25);// optional, dp
    }

    private void initView() {

        pl_root = findViewById(R.id.id_pl_root);
        lv_content = findViewById(R.id.id_lv_content);

        //设置listView为多选模式，长按自动触发
        //lv_content.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //lv_content.setMultiChoiceModeListener(new MultiChoiceModeCallback());

        //listView的点击监听
        lv_content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "你选中的position为：" + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class CustomRefreshListener implements SwipeRefreshLayout.OnRefreshListener {
        @Override
        public void onRefresh() {
            // you can do sth here, for example: make a toast:
            Toast.makeText(MainActivity.this, "custom SwipeRefresh listener", Toast.LENGTH_SHORT).show();
            // don`t forget to call this
            adapter.getSwipeRefreshLayout().setRefreshing(false);
        }
    }

    /**
     * 生成一份横向表头的内容
     *
     * @return List<String>
     */
    private void initRowDataList() {
        rowDataList.add("地址");
        rowDataList.add("当前量");
        rowDataList.add("需加量");
        rowDataList.add("加药状态");
    }

    /**
     * 初始化content数据
     */
    private void initContentDataList() {
        for (int i = 1; i <= 50; i++) {
            List<String> data = new ArrayList<>();
            data.add("第" + i + "行第一个");
            data.add("第" + i + "行第二个");
            data.add("第" + i + "行第三个");
            data.add("第" + i + "行第四个");
            contentList.add(data);
        }
    }

    /**
     * 初始化 content 部分每一个 item 的每个数据的宽度
     */
    private void initItemWidthList() {
        itemWidthList.add(100);
        itemWidthList.add(100);
        itemWidthList.add(100);
        itemWidthList.add(100);
    }

    /**
     * 更新content数据源
     */
    private void changeContentDataList() {
        contentList.clear();
        for (int i = 1; i < 500; i++) {
            List<String> data = new ArrayList<>();
            data.add("第" + i + "第一个");
            data.add("第" + i + "第二个");
            data.add("第" + i + "第三个");
            data.add("第" + i + "第四个");
            contentList.add(data);
        }
    }

    /**
     * 插入一个数据
     */
    private void insertData() {
        List<String> data = new ArrayList<>();
        data.add("插入1");
        data.add("插入2");
        data.add("插入3");
        data.add("插入4");
        contentList.add(5, data);
    }

    /**
     * 删除一个数据
     */
    private void removeData() {
        contentList.remove(10);
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //menu.add(0, 0, 0, "设置");
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case 0:
//                //listView.setSelectedPosition(32);
//                //Toast.makeText(this, "设置", Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
//                startActivity(intent);
//                break;
//        }
        return true;
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //扫描二维码
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null){
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                barcode.setText(content);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permissions4M.onRequestPermissionsResult(MainActivity.this, requestCode, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick({R.id.button_scan, R.id.button_querymed})
    public void click(View view){
        switch (view.getId()) {
            case R.id.button_scan:
                //PermissionHelper.getInstance().checkPermissions(permissions, MainActivity.this);
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE_SCAN);
                break;

            case R.id.button_querymed:
                try{
                    //当用户点击按钮后，将用户输入的数据封装成Message，然后发送给子线程Handler

//                    Message m = new Message();
//                    m.what = TypeDefine.MSG_CLI_RESQUEST;
//                    m.obj = "5";
//                    clientThread.revHandler.sendMessage(m);
//
//
//                    Message msg = new Message();
//                    msg.what = TypeDefine.MSG_CLI_RESQUEST;
//                    msg.obj = barcode.getText().toString();
//                    clientThread.revHandler.sendMessage(msg);
                    socketService.sendOrder(barcode.getText().toString());

                } catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handlerServerMsg(EventMsg msg){
        if (msg.getTag().equals(Constants.SERVER_MSG)){
            medname.setText(msg.getData());
        }

    }

}
