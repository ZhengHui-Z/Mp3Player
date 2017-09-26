package top.codeplus.play;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSlider;

import top.codeplus.ui.ProgressSliderUI;
import top.codeplus.ui.VolumeSliderUI;

/**
 * 控制声音，播放进程的面板
 * @author jaheim
 *
 */
@SuppressWarnings("serial")
public class ControllePane extends JPanel {
	private JSlider progressBar, volumeBar;
	private JButton previous, start, stop, next;

	public ControllePane(int width, int height) {
		setPreferredSize(new Dimension(width, height));
		
		setLayout(new BorderLayout());
		
		//音量与进度条
		JPanel panel01 = new JPanel();
		panel01.setLayout(new BorderLayout());
		progressBar = new JSlider(JSlider.HORIZONTAL, 0, 100, 1);
		progressBar.setUI(new ProgressSliderUI());
		panel01.add(progressBar, BorderLayout.CENTER);
		volumeBar = new JSlider(JSlider.HORIZONTAL, 0, 100, 80);
		volumeBar.setUI(new VolumeSliderUI());
		volumeBar.setPreferredSize(new Dimension(80, 20));
		panel01.add(volumeBar, BorderLayout.EAST);
		add(panel01, BorderLayout.NORTH);
		
		//上一曲，播放，停止，下一曲
		JPanel panel02 = new JPanel();
		panel02.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
		previous = createJButton("Previous", "/top/codeplus/resources/back.png");
		panel02.add(previous);
		start = createJButton("Start/Pause", "/top/codeplus/resources/begin.png");
		panel02.add(start);
		stop = createJButton("Stop", "/top/codeplus/resources/pause.png");
		panel02.add(stop);
		next = createJButton("Next", "/top/codeplus/resources/go.png");
		panel02.add(next);
		add(panel02);
	}
	
	//创建一个按钮
	public JButton createJButton(String txt, String iconPath) {
		Icon icon = new ImageIcon(getClass().getResource(iconPath));
		JButton button = new JButton("");
		button.setIcon(icon);
		button.setToolTipText(txt);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setOpaque(false);//设置控件透明
		button.setEnabled(true);
		
		return button;
	}
/*	//set A button
	public JButton setJButton(String txt, String iconPath) {
		Icon icon1 = new ImageIcon(getClass().getResource(iconPath));
		Icon icon2 = new ImageIcon(getClass().getResource(iconPath));
		JButton button = new JButton("");
		button.setIcon(icon);
		button.setToolTipText(txt);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder());
		button.setOpaque(false);//设置控件透明
		button.setEnabled(true);
		
		return button;
	}*/
	
	// 返回播放进度条
	public JSlider getProgressBar() {
		return progressBar;
	}
	
	// 返回音量控制条
	public JSlider getVolumeBar() {
		return volumeBar;
	}
	
	public JButton getStartButton() {
		return start;
	}
	
	public JButton getStopButton() {
		return stop;
	}
	
	public JButton getNextButton() {
		return next;
	}
	
	public JButton getPreviousButton() {
		return previous;
	}
}
