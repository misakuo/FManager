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
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sodaless.fmanager.util.TrafficChart;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.TWaverUtil;

/**
 * @author Misaku
 *
 */
public class TrafficMonitor extends JFrame implements ActionListener,Runnable {

	
	/**
	 * 
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
	
	public static void showWindow()
	{
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
		window.logger.info("Traffic Monitor launched");
	}
	
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

	@Override
	public void run() {
		doFlush();
	}
	
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
	
	public static JSONObject getAggregate()
	{
		return status;
	}
	
}
