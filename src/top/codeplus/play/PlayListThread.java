package top.codeplus.play;

import java.io.File;
import java.io.IOException;

import javax.swing.JSlider;

import jmp123.decoder.IAudio;
import top.codeplus.utli.ProcessBarTimer;

public class PlayListThread extends Thread {
	private volatile boolean interrupted;
	private PlayBack playback;
	private ListPane playlist;
	private JSlider progressBar;
	private JSlider volumeBar;
	private long startFrames; // 记录当前的开始帧位置
	private int times; // 显示当前开始的进度条次数
	private boolean isLeap; // 用于指示是否为跳过播放的
	private long currentFrames; // 总的帧数
	private float frameDuration; // 每一帧的时长"秒"
	private ProcessBarTimer processShow; // 显示进度条的一个进程

	public PlayListThread(ListPane playlist, IAudio audio, JSlider progressBar, JSlider volumeBar) {
		this.playlist = playlist;
		playback = new PlayBack(audio);
		setName("playlist_thread");
		this.progressBar = progressBar;
		this.volumeBar = volumeBar;
		startFrames = 0;
	}

	public synchronized void pause() {
		playback.pause();
		processShow.pause();
	}

	public synchronized void playNext() {
		playlist.setNextIndex(playlist.getCurrentIndex() + 1);
		playback.stop();
		processShow.stop();
	}

	// 上一曲
	public synchronized void playPrevious() {
		playlist.setNextIndex(playlist.getCurrentIndex() - 1);
		playback.stop();
		processShow.stop();
	}

	// 双击后播放列表某一条目后被调用
	public synchronized void startPlay(int idx) {
		playlist.setNextIndex(idx);
		playback.stop();
		if (processShow != null) {
			processShow.stop();
		}
	}

	// 设定起启播放点
	public void setStartFrame(long startFrame, int times) {
		this.startFrames = startFrame;
		this.times = times;
		isLeap = true;
	}

	// 让进度条停止显示播放进程
	public void stopShowProcess() {
		processShow.stop();
	}

	// 返回进程条的刷新率
	public int rateOfRefresh() {
		return ProcessBarTimer.DELAY;
	}

	/**
	 * 终止此播放线程。
	 */
	public synchronized void interrupt() {
		interrupted = true;
		super.interrupt();
		playback.stop();
	}

	public ListPane getPlayList() {
		return playlist;
	}

	public void removeSelectedItem() {
		int index = playlist.getCurrentIndex();
		int select = playlist.getSelectedIndex();
		playlist.removeItem(select);
		// 如果删除的正是正在播放的则停止
		if (select == index) {
			playback.stop();
		}
	}

	// 删除选中文件
	public void deleteSelectedItem() {
		PlayListItem item = playlist.getPlayListItem(playlist.getSelectedIndex());
		String filename = item.getPath();
		// 从列表中删除
		removeSelectedItem();
		try {
			System.out.println(filename);
			File file = new File(filename);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {

		}
	}

	// 总的帧频数
	public long getSumFrames() {
		return currentFrames;
	}

	// 每一帧的时间长度
	public float getFrameDuration() {
		return frameDuration;
	}

	// 设置播放模式
	public void setPlayMode(int playMode) {
		playlist.setPlayMode(playMode);
	}

	@Override
	public void run() {
		// Runtime rt = Runtime.getRuntime();
		String filename;
		int curIndex;
		// float freeMemory, totalMemory; // VM
		while (!interrupted) {
			if ((curIndex = playlist.getNextIndex()) == -1)
				break;

			playlist.setSelectedIndex(curIndex);
			PlayListItem item = playlist.getPlayListItem(curIndex);
			filename = item.getPath();

			try {
				if (playback.open(filename, item.toString())) {

					currentFrames = playback.getHeader().getTrackFrames();
					frameDuration = playback.getHeader().getFrameDuration();
					processShow = new ProcessBarTimer((long) playback.getHeader().getDuration(), progressBar);
					processShow.setTimes(times);
					processShow.start();
					playback.start(startFrames, currentFrames, volumeBar);
				} else
					item.enable(playlist.isInterrupted());
			} catch (IOException e) {
				// 如果打开网络文件时被用户中断，会抛出异常。
				e.printStackTrace();
				item.enable(playlist.isInterrupted());
			} finally {
				playback.close();
				if (!isLeap) {
					startFrames = 0;
					times = 0;
					processShow.stop();
				} else {
					isLeap = false;
				}
			}
		}
	}
}
