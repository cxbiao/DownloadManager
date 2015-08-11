package com.bryan.download.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.bryan.download.db.ThreadDao;
import com.bryan.download.db.ThreadDao;
import com.bryan.download.domain.FileInfo;
import com.bryan.download.domain.ThreadInfo;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class DownloadTask {
	
	private Context mContext;
	private FileInfo mFileInfo;
	private ThreadDao mDao;
	private long mFinished=0;
	public boolean isPause=false;
	private int mThreadCount=1;   //线程数量
	private List<DownloadThread> mThreadList;   //线程集合
	public static ExecutorService executorService=Executors.newCachedThreadPool(); 
	private Semaphore mSemaphore=new Semaphore(1);
	public DownloadTask(Context mContext, FileInfo mFileInfo,int threadCount) {
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount=threadCount;
		mDao=new ThreadDao(mContext);
	}
	
	public void download(){
		//读取数据库的线程信息
		List<ThreadInfo> threadInfos=mDao.getThreads(mFileInfo.getUrl());
		if(threadInfos.size()==0){
			//获得每个线程下载长度
			long length=mFileInfo.getLength()/mThreadCount;
			for(int i=0;i<mThreadCount;i++){
				ThreadInfo threadInfo=new ThreadInfo(i,mFileInfo.getUrl(),length*i,(i+1)*length-1,0);
			    //最后一个可能除不尽
				if(i==mThreadCount-1){
			    	threadInfo.setEnd(mFileInfo.getLength());
			    }
				//添加到线程集合
				threadInfos.add(threadInfo);
				//向数据库插入线程信息
				mDao.insertThread(threadInfo);
			}
		}else{
			mFinished=mDao.getFinisedByUrl(mFileInfo.getUrl());
		}
		mThreadList=new ArrayList<DownloadThread>();
		//启动多个线程进行下载
		for(ThreadInfo info:threadInfos){
			DownloadThread thread=new DownloadThread(info);
			//thread.start();
			DownloadTask.executorService.execute(thread);
			mThreadList.add(thread);
			
		}
	}
	
	
	private synchronized void checkAllThreadsFinished() {
		boolean allFinished=true;
		for(DownloadThread thread:mThreadList){
			if(!thread.isFinished){
				allFinished=false;
				break;
			}
		}
		if(allFinished){
			//删除线程信息
			mDao.deleteThread(mFileInfo.getUrl());
			//发送广播通知UI下载任务结束
			Intent intent=new Intent(DownloadService.ACTION_FINISH);
		    intent.putExtra("fileInfo", mFileInfo);
		    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
		  
		}
	}
	/**
	 * 下载线程
	 * @author bryan
	 *
	 */
	class DownloadThread extends Thread{
		private ThreadInfo mThreadInfo;
        public boolean isFinished=false;  //标识线程是否结束
		public DownloadThread(ThreadInfo mThreadInfo) {
			this.mThreadInfo = mThreadInfo;
		}
		
		@Override
		public void run() {
			
			
			HttpURLConnection conn=null;
			RandomAccessFile raf=null;
			InputStream inputStream=null;
			
			try {
				URL url=new URL(mThreadInfo.getUrl());
				conn=(HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setRequestMethod("GET");
				//设置下载位置
				long start=mThreadInfo.getStart()+mThreadInfo.getFinished();
				conn.setRequestProperty("Range", "bytes="+start+"-"+mThreadInfo.getEnd());
				//设置文件 写入位置
				File file=new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
				raf=new RandomAccessFile(file, "rwd");
				raf.seek(start);
				Intent intent=new Intent(DownloadService.ACTION_UPDATE);
				//mFinished+=mThreadInfo.getFinished();
				//开始下载
				if(conn.getResponseCode()==HttpURLConnection.HTTP_PARTIAL){
					//读取数据
					inputStream=conn.getInputStream();
					byte[] buf=new byte[1024*4];
					int len=-1;
					long time=System.currentTimeMillis();
					while((len=inputStream.read(buf))!=-1){
						//写入文件
						raf.write(buf, 0, len);
						//把下载进度发送广播
						mSemaphore.acquire();
						mFinished+=len;
						mSemaphore.release();
						mThreadInfo.setFinished(mThreadInfo.getFinished()+len);
						//每2000ms发送一次，减少UI刷新频率
						//下载10+M的文件时会出现进度度响应慢，甚至最后不响应 
						//界面还是有点卡 暂时无法解决
						if(System.currentTimeMillis()-time>2000){
							time=System.currentTimeMillis();
							intent.putExtra("finished", (mFinished+0.0)/mFileInfo.getLength()*100);
							intent.putExtra("id", mFileInfo.getId());
							LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
						}
						//在下载暂停时，保存下载进度
						if(isPause){
							mDao.updateThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
						   return;
						}
						
					}
					//标识线程执行完毕
					isFinished=true;
					
					checkAllThreadsFinished();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				
				try {
					conn.disconnect();
					inputStream.close();
					raf.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
		}
		
	}

}
