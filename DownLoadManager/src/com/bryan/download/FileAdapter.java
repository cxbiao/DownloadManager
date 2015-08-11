package com.bryan.download;

import java.util.List;

import com.bryan.download.domain.FileInfo;
import com.bryan.download.services.DownloadService;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter{

	
	private Context context;
	private List<FileInfo> fileList;
	
	public FileAdapter(Context context,List<FileInfo> data){
		this.context=context;
		this.fileList=data;
	}
	@Override
	public int getCount() {
		return fileList.size();
	}

	@Override
	public Object getItem(int position) {
		return fileList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder viewHolder=null;
		if(convertView==null){
			convertView=View.inflate(context, R.layout.listitem, null);
			viewHolder=new ViewHolder();
			viewHolder.mTvFileName=(TextView) convertView.findViewById(R.id.tvFileName);
			viewHolder.mProgressBar=(ProgressBar) convertView.findViewById(R.id.progressBar);
			viewHolder.mTvProgress=(TextView) convertView.findViewById(R.id.tvProgress);
			viewHolder.mBtnStart=(Button) convertView.findViewById(R.id.btnStart);
			viewHolder.mBtnStop=(Button) convertView.findViewById(R.id.btnStop);
		    convertView.setTag(viewHolder);
		}else{
            viewHolder=(ViewHolder) convertView.getTag();
		}
		final FileInfo fileInfo=fileList.get(position);
		viewHolder.mTvFileName.setText(fileInfo.getFileName());
		viewHolder.mProgressBar.setProgress((int)fileInfo.getFinished());
		viewHolder.mBtnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(context,DownloadService.class);
				intent.setAction(DownloadService.ACTION_START);
				intent.putExtra("fileInfo", fileInfo);
				context.startService(intent);
				
			}
		});
		viewHolder.mBtnStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(context,DownloadService.class);
				intent.setAction(DownloadService.ACTION_STOP);
				intent.putExtra("fileInfo", fileInfo);
				context.startService(intent);
				
			}
		}); 
		return convertView;
	}
	
	/**
	 * 更新进度条
	 * @author bryan
	 *
	 */
	public void updateProgress(int id,double progress){
		FileInfo fileInfo=fileList.get(id);
		fileInfo.setFinished((long)progress);
		notifyDataSetChanged();
		
	}
	
	static class ViewHolder{
		TextView mTvFileName;
		ProgressBar mProgressBar;
		TextView mTvProgress;
		Button mBtnStart;
		Button mBtnStop;
	}

}
