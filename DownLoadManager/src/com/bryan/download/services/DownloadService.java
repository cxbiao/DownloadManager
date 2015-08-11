package com.bryan.download.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpStatus;

import com.bryan.download.domain.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class DownloadService extends Service{

	//开始下载
	public static final String ACTION_START="ACTION_START";
	//暂停下载
	public static final String ACTION_STOP="ACTION_STOP";
	//结束下载
	public static final String ACTION_FINISH="ACTION_FINISH";
	//更新UI
	public static final String ACTION_UPDATE="ACTION_UPDATE";
	private static final String TAG="DownloadService";
	public static final String DOWNLOAD_PATH=
		 Environment.getExternalStorageDirectory().getAbsolutePath()
		 +"/downloads/";
	private static final int MSG_INIT=0;
	//下载任务集合
	private Map<Integer, DownloadTask> mTasks=new LinkedHashMap<Integer, DownloadTask>();
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		//获得参数
		if(intent!=null){
			if(ACTION_START.equals(intent.getAction())){
				FileInfo fileInfo=(FileInfo) intent.getSerializableExtra("fileInfo");
				Log.i(TAG, "start:"+fileInfo.toString());
				DownloadTask.executorService.execute(new InitThread(fileInfo));
				//new InitThread(fileInfo).start();
			}else if(ACTION_STOP.equals(intent.getAction())){
				//暂停下载
				FileInfo fileInfo=(FileInfo) intent.getSerializableExtra("fileInfo");
				Log.i(TAG, "stop:"+fileInfo.toString());
				//从集合中取出下载任务
				DownloadTask task=mTasks.get(fileInfo.getId());
				if(task!=null){
					task.isPause=true;
				}
					
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	Handler mHandler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo=(FileInfo)msg.obj;
				Log.i(TAG, "init:"+fileInfo.toString());
				//启动下载任务
				DownloadTask task=new DownloadTask(DownloadService.this, fileInfo,3);
				task.download();
				mTasks.put(fileInfo.getId(), task);
				break;

			default:
				break;
			}
		};
	};
	
	/**
	 * 初始化子线程
	 */
	class InitThread extends Thread {
		private FileInfo mFileInfo;

		public InitThread(FileInfo mFileInfo) {
			this.mFileInfo = mFileInfo;
		}
		@Override
		public void run() {
			HttpURLConnection conn=null;
			RandomAccessFile raf=null;
			try {
				//连接网络文件
				URL url=new URL(mFileInfo.getUrl());
				conn=(HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				int length=-1;
				if(conn.getResponseCode()==HttpURLConnection.HTTP_OK){
					//获得文件长度
					length=conn.getContentLength();
				}
				if(length<=0){
					return;
				}
				File dir=new File(DOWNLOAD_PATH);
				if(!dir.exists()){
					dir.mkdir();
				}
			    //在本地创建文件
				File file=new File(dir,mFileInfo.getFileName());
				//rwd 读写删除
				raf=new RandomAccessFile(file, "rwd");
				raf.setLength(length);
				//设置文件长度，断点下载
				mFileInfo.setLength(length);
				mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();
				
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					conn.disconnect();
					raf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

}
