package com.bryan.download.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.bryan.download.domain.ThreadInfo;
/**
 * 数据库操作类
 * @author bryan
 *
 */
public class ThreadDao {

	private DBHelper dBHelper;
	
	public ThreadDao(Context context){
		dBHelper=DBHelper.getInstance(context);
	}
	/**
	 * 插入线程信息
	 * @param threadInfo
	 */
	public synchronized void insertThread(ThreadInfo threadInfo) {
		SQLiteDatabase db=dBHelper.getWritableDatabase();
		db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values (?,?,?,?,?)",
			new Object[]{threadInfo.getId(),threadInfo.getUrl(),threadInfo.getStart(),threadInfo.getEnd(),threadInfo.getFinished()});
	    db.close();
	}

	 /**
	    * 删除线程
	    * @param url
	    */
	public synchronized void deleteThread(String url) {
		SQLiteDatabase db=dBHelper.getWritableDatabase();
		db.execSQL("delete from thread_info where url=?",
			new Object[]{url});
	    db.close();
		
	}
	 /**
	    * 更新线程下载信息
	    * @param url
	    * @param thread_id
	    * @param finished
	   */
	public synchronized void updateThread(String url, long thread_id, long finished) {
		SQLiteDatabase db=dBHelper.getWritableDatabase();
		db.execSQL("update thread_info set finished=? where url=? and thread_id=?",
			new Object[]{finished,url,thread_id});
	    db.close();
		
	}
	 /**
	    * 查询文件的线程信息
	    * @param url
	    * @return
	    */
	public List<ThreadInfo> getThreads(String url) {
		SQLiteDatabase db=dBHelper.getReadableDatabase();
		List<ThreadInfo> list=new ArrayList<ThreadInfo>();
		Cursor cursor=db.rawQuery("select * from thread_info where url=?", new String[]{url});
		while (cursor.moveToNext()) {
			ThreadInfo thread=new ThreadInfo();
			thread.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			thread.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			thread.setStart(cursor.getLong(cursor.getColumnIndex("start")));
			thread.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
			thread.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
		    list.add(thread);
		}
		cursor.close();
	    db.close();
	    return list;
	    
	}
	 /**
	    * 线程的信息是否存在
	    * @param url
	    * @param thread_id
	    * @return
	    */
	public boolean isExists(String url, long thread_id) {
		SQLiteDatabase db=dBHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select * from thread_info where url=? and thread_id=?", new String[]{url,thread_id+""});
		boolean exists=cursor.moveToNext();
		cursor.close();
	    db.close();
		return exists;
	}
	/**
	 * 获得每个任务的下载进度
	 */
	public long getFinisedByUrl(String url){
		long finished=0;
		SQLiteDatabase db=dBHelper.getReadableDatabase();
		Cursor cursor=db.rawQuery("select sum(finished) from thread_info where url=?", new String[]{url});
		if(cursor.moveToNext()){
			finished=cursor.getLong(0);
		}
		return finished;
	}

}
