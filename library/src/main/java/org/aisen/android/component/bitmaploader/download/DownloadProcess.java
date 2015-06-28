package org.aisen.android.component.bitmaploader.download;

import android.os.Handler;
import android.os.Message;

public abstract class DownloadProcess {
	private Handler mHandler;

	public DownloadProcess() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 0:
					receiveLength(Long.parseLong(msg.obj.toString()));
					break;
				case 1:
					receiveProgress(Long.parseLong(msg.obj.toString()));
					break;
				case 2:
					Object[] tag = (Object[]) msg.obj;
					prepareDownload(tag[0].toString());
					break;
				case 3:
					finishedDownload((byte[]) msg.obj);
					break;
				case 4:
					downloadFailed((Exception) msg.obj);
					break;
				}
			}
		};
	}

	public void sendLength(int length) {
		Message msg = mHandler.obtainMessage();
		msg.what = 0;
		msg.obj = length;
		msg.sendToTarget();
	}

	public void sendProgress(long progress) {
		Message msg = mHandler.obtainMessage();
		msg.what = 1;
		msg.obj = progress;
		msg.sendToTarget();
	}

	void sendPrepareDownload(String url) {
		Message msg = mHandler.obtainMessage();
		msg.what = 2;
		msg.obj = new Object[] { url };
		msg.sendToTarget();
	}

	public void sendFinishedDownload(byte[] bytes) {
		Message msg = mHandler.obtainMessage();
		msg.what = 3;
		msg.obj = bytes;
		msg.sendToTarget();
	}

	void sendException(Exception e) {
		Message msg = mHandler.obtainMessage();
		msg.what = 4;
		msg.obj = e;
		msg.sendToTarget();
	}

	public void receiveLength(long length) {;
	
	}

	public void receiveProgress(long progressed) {
		
	};

	public void prepareDownload(String url) {;
	
	}

	public void finishedDownload(byte[] bytes) {;
	
	}

	public void downloadFailed(Exception e) {;
	
	}

}
