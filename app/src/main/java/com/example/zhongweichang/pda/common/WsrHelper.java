package com.example.zhongweichang.pda.common;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

/**
 * Created by zhongweichang on 2018/7/12.
 */

public class WsrHelper {
    private static final String TAG = "WsrHelper";
    private String ip;      //webIP
    private String port;    //webPort

    // WSDL文档的URL，192.168.17.156为PC的ID地址
    private String serviceUrl = null; //"http://192.168.1.183:8008/PDAService/Service?wsdl";
    //soap action
    private String SOAP_ACTION="http://tempuri.org/IService1/AppRequest";
    // 定义调用的WebService方法名
    private String methodName = "AppRequest";

    SoapObject request = null;
    HttpTransportSE ht = null;

    public WsrHelper(String ip, String port){
        this.ip = ip;
        this.port = port;
        serviceUrl = "http://" + ip + ":" + port + "/PDAService/Service?wsdl";
        request = new SoapObject("http://tempuri.org/", methodName);
        ht = new HttpTransportSE(serviceUrl);
    }


    public Object Invoke(Object param){
        Object result = null;
        try{
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            request.addProperty("xml", param);
            envelope.bodyOut = request;
            envelope.dotNet = true;
            ht.call(SOAP_ACTION, envelope);
            if (envelope.getResponse() != null){
                SoapObject soapObject = (SoapObject)envelope.bodyIn;
                result = soapObject.getProperty(0);
            }
        }
        catch (Exception e){
            Log.e(TAG, "Invoke: ", e);
            result = "login&";  //登录失败
        }
        return result;
    }
}


//    // WSDL文档的URL，192.168.17.156为PC的ID地址
//    String serviceUrl = "http://192.168.1.183:8008/PDAService/Service?wsdl";
//    //soap action
//    String SOAP_ACTION="http://tempuri.org/IService1/AppRequest";
//
//    // 定义调用的WebService方法名
//    String methodName = "AppRequest";
//    // 第1步：创建SoapObject对象，并指定WebService的命名空间和调用的方法名
//    SoapObject request = new SoapObject("http://tempuri.org/", methodName);
//// 第2步：设置WebService方法的参数
//            request.addProperty("xml", "爱你哈哈，AppRequest!");
//                    // 第3步：创建SoapSerializationEnvelope对象，并指定WebService的版本
//                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER10);
//                    // 设置bodyOut属性
//                    envelope.bodyOut = request;
//                    // 设置是否调用的是dotNet开发的WebService
//                    envelope.dotNet = true;
//
//                    // 第4步：创建HttpTransportSE对象，并指定WSDL文档的URL
//                    HttpTransportSE ht = new HttpTransportSE(serviceUrl);
//                    try
//                    {
//                    // 第5步：调用WebService
//                    ht.call(SOAP_ACTION, envelope);
//                    if (envelope.getResponse() != null)
//                    {
//                    // 第6步：使用getResponse方法获得WebService方法的返回结果
//                    SoapObject soapObject = (SoapObject)envelope.bodyIn;
//                    // 通过getProperty方法获得Product对象的属性值
//                    result = soapObject.getProperty(0).toString();
//                    }
//                    else {
//                    result = "error.";
//                    }
//                    }
//                    catch (Exception e)
//                    {
//                    Log.e(TAG, "onViewClicked: ", e);
//                    }
//
//                    if (result != null)
//                    Log.d(TAG, result);
//                    return result;