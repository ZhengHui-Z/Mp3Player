package top.codeplus.play;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class PlayWindow extends JFrame {
	private File currentDirectory; // 当前的文件
	private SpectrumPane spectrumPane; // 频率面板
	private ListPane listPane; // 歌曲面板
	private ControllePane controllePanel; // 控制面板
	private PlayListThread playlistThread; // 播放线程
	private JPopupMenu popupMenu; // 弹出菜单
	private JFrame jFrame;

	public PlayWindow() {
		/*************** 面板左部分 **********************************/
		JRootPane rootPaneleft = new JRootPane();
		rootPaneleft.setLayout(new BorderLayout());
		rootPaneleft.setOpaque(false);
		// 左侧上部选择按钮
		JPanel JPanelfile = new JPanel(new GridLayout(1, 3));
		JButton Butfile = getJButton("file", "openFile", "/top/codeplus/resources/min.png");
		JButton Butfolder = getJButton("file", "openFolder", "/top/codeplus/resources/min.png");
		JButton Butabout = getJButton("about", "about", "/top/codeplus/resources/min.png");
		JPanelfile.add(Butfile);
		JPanelfile.add(Butfolder);
		JPanelfile.add(Butabout);
		JPanelfile.setVisible(true);
		JPanelfile.setSize(rootPaneleft.getWidth(), 200);
		// 左侧下部歌曲列表
		// 歌曲列表面板
		listPane = new ListPane(getWidth(), 220);
		listPane.getMusicList().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
					playlistThread.startPlay(listPane.getMusicList().locationToIndex(e.getPoint()));
					startPlaylistThread();
				} else if (e.getButton() == MouseEvent.BUTTON3) {
					int index = listPane.getMusicList().locationToIndex(e.getPoint());
					listPane.getMusicList().setSelectedIndex(index);
					initPopupMenu();
					popupMenu.show(listPane.getMusicList(), e.getX(), e.getY());
				}
			}
		});
		// 模式选择
		JPanel jPanelmod = new JPanel(new GridLayout(1, 3));
		JButton jButtonmod1 = getJButton("mod1", "mod1", "/top/codeplus/resources/1.png");
		JButton jButtonmod2 = getJButton("mod2", "mod2", "/top/codeplus/resources/2.png");
		JButton jButtonmod3 = getJButton("mod3", "mod3", "/top/codeplus/resources/3.png");
		jPanelmod.add(jButtonmod1);
		jPanelmod.add(jButtonmod2);
		jPanelmod.add(jButtonmod3);
		jPanelmod.setVisible(true);
		rootPaneleft.add(JPanelfile, BorderLayout.NORTH);
		rootPaneleft.add(listPane, BorderLayout.CENTER);
		rootPaneleft.add(jPanelmod, BorderLayout.SOUTH);
		/*********************************************************/
		/*************** 面板右部分 **********************************/
		JRootPane rootPaneright = new JRootPane();
		rootPaneright.setLayout(new BorderLayout());
		rootPaneright.setVisible(true);
		rootPaneright.setSize(166, 666);
		// rootPaneleft.setOpaque(false);
		// 频谱
		spectrumPane = new SpectrumPane(41000, 386, 133);
		rootPaneright.add(spectrumPane, BorderLayout.NORTH);
		// 歌词面板
		Icon icon = new ImageIcon(this.getClass().getResource("/top/codeplus/resources/bk.png"));
		JLabel jLabellrc = new JLabel(icon);
		jLabellrc.setLayout(new GridLayout(3, 1));
		jLabellrc.setSize(rootPaneright.getWidth(), 400);
		jLabellrc.setVisible(true);
		jLabellrc.setOpaque(false);
		rootPaneright.add(jLabellrc, BorderLayout.CENTER);
		// 控制面板
		controllePanel = new ControllePane(rootPaneright.getWidth(), 100);
		rootPaneright.add(controllePanel, BorderLayout.SOUTH);
		controllePanel.getProgressBar().addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (playlistThread != null && listPane.getCount() != 0) {
					playlistThread.stopShowProcess();
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (playlistThread != null && listPane.getCount() != 0) {
					long countFrame = playlistThread.getSumFrames();// 总的帧频数
					float frameDuration = playlistThread.getFrameDuration() * 1000;// 每一帧的时间长度ms
					int rate = playlistThread.rateOfRefresh();// 返回进程条的刷新率
					int currentValue = controllePanel.getProgressBar().getValue();// 进度条当前值
					long startFrams = (long) (currentValue / 100.0f * countFrame);
					int times = (int) (frameDuration * startFrams / rate);
					int currentPlayeIndex = listPane.getCurrentIndex();// 当前正在播放的文件的列表索引。
					playlistThread.setStartFrame(startFrams, times);
					playlistThread.startPlay(currentPlayeIndex);
				}
			}
		});
		// 上一曲
		controllePanel.getPreviousButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playlistThread.playPrevious();
				controllePanel.getProgressBar().setValue(0);
			}
		});

		// 暂停\播放
		controllePanel.getStartButton().addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				playlistThread.pause(); // 播放暂停
			}
		});

		// 停止
		controllePanel.getStopButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playlistThread.interrupt();
				controllePanel.getProgressBar().setValue(0);
			}
		});

		// 下一曲
		controllePanel.getNextButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				playlistThread.playNext();
				controllePanel.getProgressBar().setValue(0);
			}
		});
		// 如果存在列表文件则将其打开
		File listFile = new File(ListPane.listFile);
		if (listFile.exists()) {
			listPane.openM3U(listFile.getAbsolutePath());
			startPlaylistThread();
		}
		/********************** 竖直分割线 ****************************/
		JSplitPane jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, rootPaneleft, rootPaneright);
		jSplitPane.setDividerLocation(266);
		jSplitPane.setEnabled(false);
		jSplitPane.setBackground(Color.WHITE);
		/**********************************************************/
		//Icon jFrameIcon = new ImageIcon(this.getClass().getResource("/top/codeplus/resources/music.png"));
		//Image jFrameIcon=Toolkit.getDefaultToolkit().createImage("/top/codeplus/resources/music.png");
		jFrame = new JFrame("Mp3Player");
		jFrame.add(jSplitPane);
		jFrame.setSize(666, 666);
		jFrame.setVisible(true);
		jFrame.setResizable(false);
		//jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("/top/codeplus/resources/music.png"));
		jFrame.setLocationRelativeTo(null);
		jFrame.addWindowListener(new MyListener());
		/**********************************************************/
	}

	public class MyListener implements WindowListener {

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("test1");
		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub
			System.out.println("test2");
		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			// TODO Auto-generated method stub
			listPane.saveM3U();
			System.out.println("窗口正在被关闭");
			System.exit(0);
		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

	}

	// 模式选择
	public void mod1() {
		if (playlistThread != null && listPane.getCount() != 0)
			playlistThread.setPlayMode(1);
	}

	public void mod2() {
		if (playlistThread != null && listPane.getCount() != 0)
			playlistThread.setPlayMode(2);
	}

	public void mod3() {
		if (playlistThread != null && listPane.getCount() != 0)
			playlistThread.setPlayMode(3);
	}

	// 初始化弹出菜单
	private JPopupMenu initPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			JMenuItem remove = new JMenuItem("移除");
			remove.addActionListener(new ActionListener() {

				@Override
				// 删除播放列表中的歌曲
				public void actionPerformed(ActionEvent e) {
					removeSelectFile();
				}

			});
			JMenuItem delete = new JMenuItem("彻底删除");
			delete.addActionListener(new ActionListener() {
				// 删除文件
				public void actionPerformed(ActionEvent e) {
					deleteSelectFile();
				}
			});
			JMenuItem openContain = new JMenuItem("文件路径");
			openContain.addActionListener(new ActionListener() {
				// 打开所在文件
				public void actionPerformed(ActionEvent e) {
					openContainFolder();
				}
			});
			popupMenu.add(remove);
			popupMenu.add(delete);
			popupMenu.addSeparator();
			popupMenu.add(openContain);
		}
		Component[] comps = popupMenu.getComponents();
		if (listPane.getSelectedIndex() != -1 && listPane.getCount() != 0) {
			comps[0].setEnabled(true);
			comps[1].setEnabled(true);
			comps[3].setEnabled(true);
		} else {
			comps[0].setEnabled(false);
			comps[1].setEnabled(false);
			comps[3].setEnabled(false);
		}

		return popupMenu;

	}

	// 得到一个按钮
	private JButton getJButton(String hit, final String method, String path) {
		JButton button = new JButton();
		Icon closeIcon = new ImageIcon(this.getClass().getResource(path));
		button.setToolTipText(hit);
		button.setIcon(closeIcon);
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorder(BorderFactory.createEmptyBorder());

		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {
					PlayWindow.this.getClass().getDeclaredMethod(method).invoke(PlayWindow.this);
				} catch (Exception e) {
					System.out.println(e.toString());
				}
			}
		});
		return button;
	}

	// 开始播放线程
	private void startPlaylistThread() {
		if (listPane.getCount() == 0) {
			return;
		}

		if (playlistThread == null || playlistThread.isAlive() == false) {
			playlistThread = new PlayListThread(listPane, spectrumPane, controllePanel.getProgressBar(),
					controllePanel.getVolumeBar());
			playlistThread.start();
		}

		controllePanel.getPreviousButton().setEnabled(true);
		controllePanel.getStartButton().setEnabled(true);
		controllePanel.getStopButton().setEnabled(true);
		controllePanel.getNextButton().setEnabled(true);
	}

	// 打开文件
	public void openFile() {
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(true);
		jfc.removeChoosableFileFilter(jfc.getChoosableFileFilters()[0]);
		// jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);

		FileNameExtensionFilter filterMP3 = new FileNameExtensionFilter("Mp3 files(*.mp3)", "mp3");
		jfc.addChoosableFileFilter(filterMP3);

		jfc.addChoosableFileFilter(new FileNameExtensionFilter("VCD,DVD files (*.dat,*.vob)", "dat", "vob"));

		FileNameExtensionFilter filterM3u = new FileNameExtensionFilter("Music list(*.m3u)", "m3u");
		jfc.addChoosableFileFilter(filterM3u);

		jfc.setFileFilter(filterMP3);

		jfc.setCurrentDirectory(currentDirectory);

		int f = jfc.showOpenDialog(this);
		if (f == JFileChooser.APPROVE_OPTION) {
			File[] files = jfc.getSelectedFiles();
			int i;
			String strPath = jfc.getSelectedFile().getPath();
			if (jfc.getFileFilter().equals(filterM3u)) {
				listPane.openM3U(strPath);
			} else {
				for (i = 0; i < files.length; i++)
					listPane.append(files[i].getName(), files[i].getPath());
			}
		}
		currentDirectory = jfc.getCurrentDirectory();

		startPlaylistThread();
	}

	// 打开文件夹openFile、openFolder
	public void openFolder() {
		JFileChooser jfc = new JFileChooser();
		jfc.setMultiSelectionEnabled(false);

		jfc.removeChoosableFileFilter(jfc.getChoosableFileFilters()[0]);
		// 仅文件夹
		jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		jfc.setCurrentDirectory(currentDirectory);

		int f = jfc.showOpenDialog(this);
		if (f == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile() && files[i].getName().endsWith("mp3")) {
						listPane.append(files[i].getName(), files[i].getPath());
					}
				}
			}
		}

		currentDirectory = jfc.getCurrentDirectory();
		startPlaylistThread();
	}

	// 移除选中的选项
	public void removeSelectFile() {
		if (playlistThread != null && listPane.getCount() != 0) {
			playlistThread.removeSelectedItem();
			if (listPane.getCount() == 0) {
			}
		}
	}

	// 删除选中的文件
	public void deleteSelectFile() {
		if (playlistThread != null && listPane.getCount() != 0) {
			String filename = listPane.getPlayListItem(listPane.getSelectedIndex()).toString();
			int option = JOptionPane.showConfirmDialog(this, "你确定要从硬盘上删除 '" + filename + "' ?", "delete",
					JOptionPane.YES_NO_OPTION);
			if (option == JOptionPane.YES_OPTION) {
				playlistThread.deleteSelectedItem();
				if (listPane.getCount() == 0) {
				}
			}
		}
	}

	// 打开文件所在目录
	public void openContainFolder() {
		if (listPane.getCount() != 0) {
			String path = listPane.getPlayListItem(listPane.getSelectedIndex()).getPath();
			File file = new File(path);

			if (file.exists()) {
				JFileChooser jfc = new JFileChooser();
				jfc.setMultiSelectionEnabled(true);
				jfc.removeChoosableFileFilter(jfc.getChoosableFileFilters()[0]);
				FileNameExtensionFilter filterMP3 = new FileNameExtensionFilter("Mp3 files(*.mp3)", "mp3");
				jfc.addChoosableFileFilter(filterMP3);

				jfc.addChoosableFileFilter(new FileNameExtensionFilter("VCD,DVD files (*.dat,*.vob)", "dat", "vob"));

				FileNameExtensionFilter filterM3u = new FileNameExtensionFilter("Music list(*.m3u)", "m3u");
				jfc.addChoosableFileFilter(filterM3u);

				jfc.setFileFilter(filterMP3);
				jfc.setCurrentDirectory(file.getParentFile());
				jfc.setSelectedFile(file);
				jfc.showOpenDialog(this);
			}
		}
	}

	// 关于
	public void about() {
		JOptionPane.showMessageDialog(this, "版权所有禁止传播：@淘宝店-猿知猿味https://shop163053114.taobao.com", "about",
				JOptionPane.INFORMATION_MESSAGE);
		JOptionPane.showMessageDialog(this, "个人博客：https://codeplus.top", "about",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void main(String[] args) {
		new PlayWindow();
	}

}
