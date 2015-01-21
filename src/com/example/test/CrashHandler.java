package com.example.test;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

/**
 * 在Application中统一捕获异常，下次打开时上传到服务器
 * @author chaimg
 *
 */
public class CrashHandler implements UncaughtExceptionHandler {
	/**
	 * 是否开启日志输出，在debug状态下开启
	 * 在release状态下关闭以提示程序性能
	 */
	public static final boolean DEBUG = true;
	/**
	 * 系统默认的UncaughtException处理类
	 */
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	private static CrashHandler INSTANCE;
	private Context mContext;
	private HashMap<String, String> infos = new HashMap<String, String>();
	private static final String TAG = "CrashHandler";
	// 用于格式化日期,作为日志文件名的一部分
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
	private String mEmailSubject = null;
	private String mFilePath =  null;
	private CrashHandler(){}
	
	public static CrashHandler getInstance() {  
	       if (INSTANCE == null) {  
	           INSTANCE = new CrashHandler();  
	       }  
	       return INSTANCE;  
	  } 
    /** 
     * 初始化,注册Context对象, 
     * 获取系统默认的UncaughtException处理器, 
     * 设置该CrashHandler为程序的默认处理器 
     * @param ctx 
     */ 
    public void init(Context ctx) {  
    	mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();  
        Thread.setDefaultUncaughtExceptionHandler(this); 
        collectDeviceInfo(mContext);
    }
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {  
            //如果用户没有处理则让系统默认的异常处理器来处理  
            mDefaultHandler.uncaughtException(thread, ex);  
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            try {  
                Thread.sleep(3000);  
            } catch (InterruptedException e) {  
            }  
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(0);  
        }  
	}
	/** 
     * 自定义错误处理,收集错误信息 
     * 发送错误报告等操作均在此完成. 
     * 开发者可以根据自己的情况来自定义异常处理逻辑 
     * @return 
     * true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */ 
	private boolean handleException(final Throwable ex){
		if(ex == null){
			return false;
		}
		final StackTraceElement[] stack = ex.getStackTrace();
		mEmailSubject = "【" + getAppName() + "】 手机厂商:"
				+ android.os.Build.MANUFACTURER + "-手机型号:"
				+ android.os.Build.MODEL + ",错误报告.";
        final String message = ex.getMessage();
        final String header = getLogHeader();
        new Thread() {  
            @Override 
            public void run() {  
                Looper.prepare();  
//                Toast.makeText(mContext, "程序出错啦:" + message, Toast.LENGTH_LONG).show();  
//                可以只创建一个文件，以后全部往里面append然后发送，这样就会有重复的信息，个人不推荐
                String fileName = "roamWiFiCrash.log";  
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                	 File file = new File(Environment.getExternalStorageDirectory(), fileName);
                	 mFilePath = file.getPath();
                     try {
                         FileOutputStream fos = new FileOutputStream(file,true);
                         fos.write(formatter.format(new Date()).getBytes());
                         fos.write(header.getBytes());
                         fos.write(message.getBytes());
                         for (int i = 0; i < stack.length; i++) {
                             fos.write(stack[i].toString().getBytes());
                         }
                         fos.flush();
                         fos.close();
//                         if (Tools.isNetAvailable(mContext)) {
//                         	  sendEmail();
//     					}
                     } catch (Exception e) {
                     	
                     }
				}
               
                Looper.loop();  
            }  
   
        }.start();
        return false;
	}
		/**
	      * 收集设备参数信息
	      * 
	      * @param ctx
	      */
	public void collectDeviceInfo(Context ctx) {
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				String versionName = pi.versionName == null ? "null": pi.versionName;
				String versionCode = pi.versionCode + "";
				infos.put("versionName", versionName);
				infos.put("versionCode", versionCode);
			}
		} catch (NameNotFoundException e) {

			if (DEBUG) {
				Log.e(TAG, "an error occured when collect package info", e);
			}
		}
		Field[] fields = Build.class.getDeclaredFields();
		for (Field field : fields) {
			try {
				field.setAccessible(true);
				infos.put(field.getName(), field.get(null).toString());
				if (DEBUG) {
					Log.d(TAG, field.getName() + " : " + field.get(null));
				}
			} catch (Exception e) {
				if (DEBUG) {
					Log.e(TAG, "an error occured when collect crash info", e);
				}
			}
		}
	}
	/***
	 * 获取错误日志的头信息，包括设备信息版本等
	 * @return
	 */
	private String getLogHeader() {
		StringBuffer sb = new StringBuffer();
		for (Map.Entry<String, String> entry : infos.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(key + "=" + value + "\n");
		}
		return sb.toString();
	}
	
	private String getAppName(){
		ApplicationInfo android = mContext.getApplicationInfo();
		return mContext.getString(android.labelRes);
	}
	
	/**
	 * 发送邮件的方法
	 * @return
	 */
	   private void  sendEmail(){  
		   //获取帐号密码  
		   	LogEmailAuth pass = new LogEmailAuth(); 
		   	//获取邮箱信息
		   	LogEmailInfor emailInfor = new LogEmailInfor();
		   	emailInfor.setEmailSubject(mEmailSubject);
		   	
	        Properties props = new Properties(); 
	        props.put("mail.smtp.protocol", "smtp");  
	        props.put("mail.smtp.auth", "true");//设置要验证  
	        props.put("mail.smtp.host", emailInfor.getEmailHost());//设置host  
	        props.put("mail.smtp.port", emailInfor.getEmailPort());  //设置端口  
	        
	        Session session = Session.getInstance(props, pass); //获取验证会话  
	        try  
	        {  
	            //配置发送及接收邮箱  
	            InternetAddress fromAddress, toAddress;  
	            /**
	             * 这个地方需要改成自己的邮箱, 一个发送邮箱， 一个接收邮箱, 都填成自己的就行
	             */
	            fromAddress = new InternetAddress(emailInfor.getEmailAdd(), emailInfor.getEmailFrom());  
	            toAddress   = new InternetAddress(emailInfor.getEmailAdd(), emailInfor.getEmailCome());
	            /**
	             * 一下内容是：发送邮件时添加附件
	             */
	            MimeBodyPart attachPart = new MimeBodyPart();  
	            FileDataSource fds = new FileDataSource(mFilePath); //打开要发送的文件  
	            
	            attachPart.setDataHandler(new DataHandler(fds)); 
	            attachPart.setFileName(fds.getName()); 
	            MimeMultipart allMultipart = new MimeMultipart("mixed"); //附件  
	            allMultipart.addBodyPart(attachPart);//添加  
	            //配置发送信息  
	            MimeMessage message = new MimeMessage(session);  
//	            message.setContent("test", "text/plain"); 
	            message.setContent(allMultipart); //发邮件时添加附件
	            message.setSubject(emailInfor.getEmailSubject());  
	            message.setFrom(fromAddress);  
	            message.addRecipient(javax.mail.Message.RecipientType.TO, toAddress);  
	            message.saveChanges();  
	            //连接邮箱并发送  
	            Transport transport = session.getTransport("smtp");  
	            /**
	             * 这个地方需要改称自己的账号和密码
	             */
	            transport.connect(emailInfor.getEmailHost(), emailInfor.getEmailAdd(), emailInfor.getEmailPass()); 
	            Transport.send(message);
	            transport.close(); 
	           
	            deleteFile();
	        } catch (Exception e) {
	        	//将此异常向上抛出，此时CrashHandler就能够接收这里抛出的异常并最终将其存放到txt文件中
	        	throw new RuntimeException();
	        }
	    } 
	   
	   /**
	    * 发送完把文件删了
	    */
	   public void deleteFile(){
		   File file = new File(mFilePath);
           if (file.isFile()) {
				file.delete();
			}
	   }

}
