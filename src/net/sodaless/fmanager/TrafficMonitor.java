/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sodaless.fmanager.util.TrafficChart;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.TWaverUtil;

/**
 * 流量监控模块，提供指定交换机指定端口上的TX/RX流量监控服务
 * @author Misaku
 */
public class TrafficMonitor extends JFrame implements ActionListener,Runnable {

	
	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	private final String api = "/wm/core/switch/all/port/json";
	private static JSONObject status;
	private Logger logger = Logger.getLogger(getClass());
	private Container cn;
	private JPanel menu = new JPanel();
	private Choice swlist = new Choice();
	private Choice portlist = new Choice();
	private Choice type = new Choice();
	private JButton start = new JButton("Start");
	private JCheckBox log = new JCheckBox("Generate log");
	private static Thread flush;

	/**
	 * 默认构造器
	 */
	public TrafficMonitor()
	{
		cn = this.getContentPane();
		cn.setLayout(new BorderLayout());
		cn.add(menu,BorderLayout.CENTER);
		menu.add(swlist);
		menu.add(portlist);
		menu.add(type);
		menu.add(start);
		menu.add(log);
		
		start.addActionListener(this);
		this.addWindowListener(new WindowAdapter()
		{
			@SuppressWarnings("deprecation")
			public void windowClosing(WindowEvent e)//窗口正处在关闭过程中时调用
			{
				flush.stop();
			}
		});
		
		Iterator<String> it = Main.swSet.iterator();
		while(it.hasNext())
		{
			swlist.addItem(it.next());
		}
		addPort();
		
		type.addItem("receiveBytes");
		type.addItem("transmitBytes");
		swlist.addItemListener(new ItemListener(){

			@Override
			public void itemStateChanged(ItemEvent e) {
				addPort();
			}});
	}
	
	/**
	 * 流量监控器入口，初始化界面和更新信息的线程
	 */
	public static void showWindow()
	{
		if(Main.swSet.isEmpty())
		{
			JOptionPane.showMessageDialog(null,"No nodes exist","ERROR",JOptionPane.ERROR_MESSAGE);
		}
		else{
		flush = new Thread(new TrafficMonitor());
		flush.start();
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		TrafficMonitor window = new TrafficMonitor();
		window.setSize(540, 200);
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/traffic.png"));
		window.setIconImage(img);
		window.setTitle("Traffic Monitor");
		window.setVisible(true);
		TWaverUtil.centerWindow(window);
		flush = new Thread(new TrafficMonitor());
		flush.start();
		window.logger.info("Traffic Monitor launched");
		}
	}
	
	/**
	 * 消息监听器
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == start)
		{
			if(log.isSelected())
			{
				TrafficChart.showChart(swlist.getSelectedItem(),portlist.getSelectedItem(),type.getSelectedItem(),true);
			}
			else
			{
				TrafficChart.showChart(swlist.getSelectedItem(),portlist.getSelectedItem(),type.getSelectedItem(),false);
			}
		}

	}

	/**
	 * 多线程支持
	 */
	@Override
	public void run() {
		doFlush();
	}
	
	/**
	 * 更新交换机端口统计信息
	 */
	private void doFlush()
	{	
		while(true)
		{
			String address = ConnectionService.addressBuilder(Main.ip, Main.port, api);
			String r;
			try {
				r = ConnectionService.doGet(address);
				status  = new JSONObject(r);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 将已选择的交换机上的所有可用端口添加至选择框中
	 */
	private void addPort()
	{
		ArrayList<Integer> port = new ArrayList<Integer>(Main.swPort.get(swlist.getSelectedItem()));
		Iterator<Integer> it = port.iterator();
		portlist.removeAll();
		while(it.hasNext())
		{
			portlist.addItem(it.next().toString());
		}
		portlist.remove("65534");
	}
	
	/**
	 * 返回交换机的端口统计信息
	 * @return
	 */
	public static JSONObject getAggregate()
	{
		return status;
	}
	
}
