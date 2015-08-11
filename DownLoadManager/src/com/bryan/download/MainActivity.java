package com.bryan.download;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.bryan.download.domain.FileInfo;
import com.bryan.download.services.DownloadService;

public class MainActivity extends Activity {

   private ListView mLvFile;
   private FileAdapter mFileAdapter;
   private List<FileInfo> mFileList=new ArrayList<FileInfo>();
   
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mLvFile=(ListView) findViewById(R.id.listview);
		
	     /**
	      * http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk  6.6m  6971553
	      * http://dldir1.qq.com/music/clntupate/QQMusic_Setup_1174.exe 15m
            http://dldir1.qq.com/qqyy/pc/QQPlayer_Setup_39_923.exe   26m  27738144
            http://dldir1.qq.com/qqfile/qq/QQ7.5/15456/QQ7.5.exe   55m
            http://dlied6.qq.com/invc/xfspeed/qqpcmgr/download/QQPCDownload140042.exe   1.5
            http://dldir1.qq.com/invc/tt/QQBrowserSetup.exe   5
	      */
		//创建文件信息对象
		final FileInfo fileInfo1=new FileInfo(0, "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk", 
				"MobileAssistant_1.apk", 0, 0);
		final FileInfo fileInfo2=new FileInfo(1, "http://dldir1.qq.com/music/clntupate/QQMusic_Setup_1174.exe", 
				"QQMusic_Setup_1174.exe", 0, 0);
		final FileInfo fileInfo3=new FileInfo(2, "http://dldir1.qq.com/qqyy/pc/QQPlayer_Setup_39_923.exe", 
				"QQPlayer_Setup_39_923.exe", 0, 0);
		final FileInfo fileInfo4=new FileInfo(3, "http://dldir1.qq.com/qqfile/qq/QQ7.5/15456/QQ7.5.exe", 
				"QQ7.5.exe", 0, 0);
		final FileInfo fileInfo5=new FileInfo(4, "http://dlied6.qq.com/invc/xfspeed/qqpcmgr/download/QQPCDownload140042.exe", 
				"QQPCDownload140042.exe", 0, 0);
		final FileInfo fileInfo6=new FileInfo(5, "http://dldir1.qq.com/invc/tt/QQBrowserSetup.exe", 
				"QQBrowserSetup.exe", 0, 0);
		
		
	
		mFileList.add(fileInfo1);
		mFileList.add(fileInfo2);
		mFileList.add(fileInfo3);
		mFileList.add(fileInfo4);
		mFileList.add(fileInfo5);
		mFileList.add(fileInfo6);
		mFileAdapter=new FileAdapter(this, mFileList);
		mLvFile.setAdapter(mFileAdapter);
		
		
		IntentFilter filter=new IntentFilter();
		filter.addAction(DownloadService.ACTION_UPDATE);
		filter.addAction(DownloadService.ACTION_FINISH);
		LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
	}



	BroadcastReceiver mReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if(DownloadService.ACTION_UPDATE.equals(intent.getAction())){
				double percent =intent.getDoubleExtra("finished", 0);
				int id=intent.getIntExtra("id", 0);
				//Log.e("down", "id:"+id+",finished:"+percent);
				mFileAdapter.updateProgress(id, percent);
				//DecimalFormat df = new DecimalFormat("0.00"); 
			    
			}else if(DownloadService.ACTION_FINISH.equals(intent.getAction())){
				FileInfo fileInfo=(FileInfo) intent.getSerializableExtra("fileInfo");
			    mFileAdapter.updateProgress(fileInfo.getId(), 0);
			    Toast.makeText(MainActivity.this, mFileList.get(fileInfo.getId()).getFileName()+"下载完成", 0).show();
			    
			}
			
		}
	};
	

}
