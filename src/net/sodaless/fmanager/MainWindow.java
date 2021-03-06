/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import twaver.TWaverUtil;

/**
 * 在线模式主窗口，也是其他在线服务的入口
 * @author Misaku
 * @date 2014-06-18
 */
public class MainWindow extends JFrame implements ActionListener{
	
	/**
	 * last update:2014-6-18
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger;
	private JPanel menuPanel = new JPanel(new GridLayout(17,1,5,5));
	private TopologyVisualizationServices tvs = new TopologyVisualizationServices(Main.currentTopo);
	private JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,menuPanel,tvs.getTopoPanel());
	private JButton pm = new JButton(Main.lang.MWpm);
	private JButton fm = new JButton(Main.lang.MWfm);
	private JButton fw = new JButton(Main.lang.MWfw);
	private JButton lb = new JButton(Main.lang.MWlb);
	private JButton qos = new JButton(Main.lang.MWqos);
	private JButton tm = new JButton(Main.lang.MWtm);
	
	/**
	 * 默认构造器
	 */
	public MainWindow()
	{
		logger = Logger.getLogger(getClass());
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		getContentPane().add(split, BorderLayout.CENTER);
		split.setDividerLocation(120);
				
		menuPanel.add(pm);
		menuPanel.add(fm);
		menuPanel.add(fw);
		menuPanel.add(lb);
		//menuPanel.add(qos); //待添加
		menuPanel.add(tm);
		
		pm.addActionListener(this);
		fm.addActionListener(this);
		fw.addActionListener(this);
		lb.addActionListener(this);
		qos.addActionListener(this);
		tm.addActionListener(this);
	}
	
	/**
	 * 展示主窗口，包括拓扑区域和侧边栏各功能区域
	 */
	public static void showWindow()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		
		MainWindow window = new MainWindow();
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/icon.png"));
		window.setIconImage(img);
		window.setSize(1000, 600);
		window.setTitle(Main.lang.MWTitle);
		TWaverUtil.centerWindow(window);
		window.setVisible(true);
		window.logger.info("Online mode launched");
	}

	/**
	 * 消息监听器，处理事件，打开对应窗口
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自动生成的方法存根
		
		if(e.getSource() == pm)
		{
			this.setExtendedState(ICONIFIED);
			PathManager.showWindow();
		}
		
		if(e.getSource() == fm)
		{
			this.setExtendedState(ICONIFIED);
			FlowManager.showWindow();
		}
		
		if(e.getSource() == fw)
		{
			this.setExtendedState(ICONIFIED);
			FireWallManager.showWindow();
		}
		
		if(e.getSource() == lb)
		{
			this.setExtendedState(ICONIFIED);
			LoadBalanceManager.showWindow();
		}
		
		if(e.getSource() == tm)
		{
			this.setExtendedState(ICONIFIED);
			TrafficMonitor.showWindow();
		}
		
	}

}
