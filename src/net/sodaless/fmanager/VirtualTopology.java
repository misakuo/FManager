/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.Link;
import twaver.Node;
import twaver.TDataBox;
import twaver.TWaverUtil;
import twaver.network.TNetwork;

/**
 * �����������ɹ��ߣ��ɹ���һ�������粢���·������ģ�����·���㷨����
 * @author Misaku
 */
public class VirtualTopology extends JFrame implements ActionListener{
	
	/**
	 * last update:2014-6-18
	 */
	private static final long serialVersionUID = 5712034601165270837L;
	private TDataBox vbox = new TDataBox("Topo");
	private TNetwork vnetwork = new TNetwork(vbox);
	private JPanel vnetworkPane = new JPanel(new BorderLayout());
	private JPanel left = new JPanel(new GridLayout(17,1,5,5));
	private JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,vnetworkPane);
	
	private JButton addHost = new JButton(Main.lang.VTaddHost);
	private JButton addSwitch = new JButton(Main.lang.VTaddSwitch);
	private JButton addLink = new JButton(Main.lang.VTaddLink);
	private JButton finish = new JButton(Main.lang.VTfinish);
	private JButton file = new JButton(Main.lang.VTfile);
	private JButton save = new JButton(Main.lang.VTsave);
	private JButton clear = new JButton(Main.lang.VTclear);
	private JButton remove = new JButton(Main.lang.VTremove);
	
	private JTextField IP = new JTextField("10.0.0.1");
	private JTextField DPID = new JTextField("00:00:00:00:00:00:00:01");
	private JTextField BW = new JTextField();
	
	private Choice linkSrc = new Choice();
	private Choice linkDst = new Choice();
	private Choice removeElement = new Choice();
	
	private int hostnum=1,swnum=1;
	private int nodesNum = 0;
	private int portnum = 0;
	private String jsonFactory = "";
	
	private JSONObject vtopo;
	private Logger logger;
	
	/**
	 * Ĭ�Ϲ�����
	 */
	public VirtualTopology()
	{
		logger = Logger.getLogger(this.getClass());
		getContentPane().add(split, BorderLayout.CENTER);
		split.setDividerLocation(160);
		
		left.add(IP);
		left.add(addHost);
		left.add(DPID);
		left.add(addSwitch);
		left.add(linkSrc);
		left.add(linkDst);
		left.add(BW);
		left.add(addLink);
		left.add(finish);
		left.add(removeElement);
		left.add(remove);
		left.add(clear);
		left.add(file);
		left.add(save);
		
		IP.setFont(new Font("Arial",Font.BOLD,15));
		
		addHost.addActionListener(this);
		addSwitch.addActionListener(this);
		addLink.addActionListener(this);
		finish.addActionListener(this);
		file.addActionListener(this);
		save.addActionListener(this);
		clear.addActionListener(this);
		remove.addActionListener(this);
		
		vnetworkPane.add(vnetwork, BorderLayout.CENTER);
	}

	/**
	 * ��������ģ����ڣ����𴴽���ͼ
	 */
	public static void Init()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {e.printStackTrace();} 
		
		VirtualTopology frame =new VirtualTopology();
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		Image img = Toolkit.getDefaultToolkit().getImage(frame.getClass().getResource("/vtopo.png"));
		frame.setIconImage(img);
		frame.setBounds(500, 500, 1000, 600);
		frame.setTitle(Main.lang.VTFrameTitle);
		TWaverUtil.centerWindow(frame);
		frame.setVisible(true);
	}
	
	/**
	 * ���ѹ����õ���������ȡ������Host���������ȫ��Host������
	 * @throws JSONException
	 */
	private void exportHost() throws JSONException
	{
		JSONArray link = vtopo.getJSONArray("links");
		for(int i=0;i<link.length();i++)
		{
			String l = link.getJSONObject(i).getString("left");
			String r = link.getJSONObject(i).getString("right");
			if(l.length() < 23)
			{
				Main.hostSet.add(l);
			}
			if(r.length() < 23)
			{
				Main.hostSet.add(r);
			}
		}
	}

	/**
	 * ��Ϣ������
	 */
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == addHost)
		{
			double x = 30+Math.random()*650;
			double y = 30+Math.random()*450;
			
			Node host = new Node(IP.getText());
			host.setImage("/vhost.png");
			host.setName(IP.getText());
			host.setLocation(x, y);
			vbox.addElement(host);
			vnetwork.updateUI();
			nodesNum++;
			linkSrc.add(IP.getText());
			linkDst.add(IP.getText());
			removeElement.add(IP.getText());
			logger.info("Host add: " + IP.getText());
			
			Main.hostSet.add(IP.getText());
			logger.debug("Current host set: " + Main.hostSet);
			
			//������һ��host��ip��ֻ����������255��host�����
			hostnum++;
			String nextip = "10.0.0." + hostnum;
			IP.setText(nextip);
			
		}
		
		if(e.getSource() == addSwitch)
		{
			double x = 30+Math.random()*650;
			double y = 30+Math.random()*450;
			
			Node switcher = new Node(DPID.getText());//��IP/DPID��Ϊ���ID
			switcher.setImage("/vswitch.png");
			switcher.setName(DPID.getText());
			switcher.setLocation(x, y);
			vbox.addElement(switcher);
			nodesNum++;
			linkSrc.add(DPID.getText());
			linkDst.add(DPID.getText());
			removeElement.add(DPID.getText());
			logger.info("Switch add: " + DPID.getText());
			
			Main.swSet.add(DPID.getText());
			Main.swPort.put(DPID.getText(),new ArrayList<Integer>());
			logger.debug("Current switch set: " + Main.swSet);
			
			//����������һ��switch��dpid��ֻ������С��255��switch�����
			swnum++;
			String nextdpid = "00:00:00:00:00:00:00:"; 
			if(Integer.toHexString(swnum).length() == 1)
			{
				nextdpid = nextdpid + "0" + Integer.toHexString(swnum);
			}
			else
			{
				nextdpid = nextdpid + Integer.toHexString(swnum);
			}
			DPID.setText(nextdpid);
		}
		
		if(e.getSource() == addLink)
		{
			String src = linkSrc.getSelectedItem();
			String dst = linkDst.getSelectedItem();
			String name = src+"-"+dst;
			
			if(src == null && dst == null)
			{
				logger.warn("Link src and dst are not exist!");
			}
			else if(src.equals(dst))
			{
				logger.warn("Link src and dst are the same element!");
			}
			else if(src.length() < 23 && dst.length() < 23)
			{
				logger.warn("Link can not between host and host!");
			}
			else if(BW.getText().length() == 0)
			{
				logger.warn("The Bandwidth is empty!");
			}
			else
			{
				Link link = new Link(name,(Node)vbox.getElementByID(src),(Node)vbox.getElementByID(dst));
				link.setName("bandwidth:"+BW.getText()+" Mbps");
				vbox.addElement(link);
				removeElement.add(name);
				portnum = portnum + 1;
				jsonFactory = jsonFactory + "{\"left\":" + "\"" + src + "\"," + "\"left-port\":" + portnum + ",\"right\":" +"\""+ dst + "\"" + ",\"right-port\":" + portnum +",\"bandwidth\":" + BW.getText() +"},"; 
				logger.info("Link add: " + name);
				if(src.length() == 23)
				{
					Main.swPort.get(src).add(portnum);
				}
				if(dst.length() == 23)
				{
					Main.swPort.get(dst).add(portnum);
				}
			}
			logger.debug("Current dpid-portset mapping" + Main.swPort);
		}
		
		if(e.getSource() == finish)
		{
			
			jsonFactory = jsonFactory.substring(0, jsonFactory.length()-1);
			jsonFactory = "{\"nodes\":" +nodesNum+",\"links\":[" + jsonFactory + "]}";
			try {
				vtopo = new JSONObject(jsonFactory);
			} catch (JSONException e1) {
				// TODO �Զ����ɵ� catch ��
				e1.printStackTrace();
			}
			Main.currentTopo = vtopo;
			logger.info("Virtual topology created");
			logger.debug("Virtual topology: " + vtopo);
			
			PathManager.showWindow();
		}
		
		if(e.getSource() == file)
		{
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Virtual Topo Files(*.vtp)", "vtp");
			fc.setFileFilter(filter);
			fc.showDialog(null,"��");
			String filepath = fc.getSelectedFile().getPath();
			try {
				FileInputStream fin = new FileInputStream(filepath);
				InputStreamReader inR = new InputStreamReader(fin);
				BufferedReader bfR  = new BufferedReader(inR);
				String in="";
				String line;
				try {
					while ((line = bfR.readLine()) != null) 
					in +=line;

				} catch (IOException e1) {
					// TODO �Զ����ɵ� catch ��
					e1.printStackTrace();
				}
				try {
					vtopo = new JSONObject(in);
					exportHost();
					logger.debug("Virtual topo details: " + vtopo);
					Main.currentTopo = vtopo;
					logger.info("Flie readin success from " + filepath);
					PathManager.showWindow();
				} catch (JSONException e1) {
					JOptionPane.showMessageDialog(null,"��ȡʧ�ܣ������ļ�����","��ȡʧ��",JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
				fin.close();
				inR.close();
				bfR.close();
			} catch (FileNotFoundException e1) {
				// TODO �Զ����ɵ� catch ��
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO �Զ����ɵ� catch ��
				e1.printStackTrace();
			}
			
		}
		
		if(e.getSource() == save)
		{
			jsonFactory = jsonFactory.substring(0, jsonFactory.length()-1);
			jsonFactory = "{\"nodes\":" +nodesNum+",\"links\":[" + jsonFactory + "]}";
			try {
				vtopo = new JSONObject(jsonFactory);
				logger.debug("Virtual topo details: " + vtopo);
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			
			Main.currentTopo = vtopo;
			
			JFileChooser fc = new JFileChooser();
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Virtual Topo Files(*.vtp)", "vtp");
			fc.setFileFilter(filter);
			fc.showDialog(null,"����");
			String filepath = fc.getSelectedFile().getPath()+".vtp";
			try{
			File tofile=new File(filepath);
			FileWriter fw=new FileWriter(tofile);
			fw.write(vtopo.toString());
			fw.flush();
			fw.close();
			JOptionPane.showMessageDialog(null,"�ļ�����ɹ�","����ɹ�",JOptionPane. INFORMATION_MESSAGE);
			logger.info("File success save to " + filepath);
			}catch (IOException e1) {
				JOptionPane.showMessageDialog(null,"�ļ�����ʧ��","����ʧ��",JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();}
		}
		
		if(e.getSource() == clear)
		{
			jsonFactory = "";
			nodesNum = 0;
			portnum = 0;
			hostnum = 1;
			swnum = 1;
			vtopo = null;
			IP.setText("10.0.0.1");
			DPID.setText("00:00:00:00:00:00:00:01");
			linkSrc.removeAll();
			linkDst.removeAll();
			vbox.clear();
			vnetwork.updateUI();
			Main.hostSet.clear();
			Main.swSet.clear();
			Main.swPort.clear();
			logger.warn("All content has been cleared");
		}
		
		if(e.getSource() == remove)
		{
			String elementID = removeElement.getSelectedItem();
			if(elementID != null)
			{
				vbox.removeElementByID(elementID);
				removeElement.remove(elementID);
				if(elementID.length() <= 23)
				{
					linkSrc.remove(elementID);
					linkDst.remove(elementID);
					Main.hostSet.remove(elementID);
					Main.swSet.remove(elementID);
					Main.swPort.remove(elementID);
				}
				logger.warn("Element " + elementID + " removed");
			}
			else
			{
				logger.warn("No elements exist");
			}
		}
		
	}

}
