/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.*;
import twaver.network.TNetwork;

/**
 * �����ṩ������ʾ����
 * @author Misakuo
 * @date 2014-6-16
 *
 */
public class TopologyVisualizationServices {

	private TDataBox box = new TDataBox();
	private TNetwork network = new TNetwork(box);
	private JPanel panel = new JPanel(new BorderLayout());
	private JSONObject topo = new JSONObject();
	private ArrayList<String> nodes = new ArrayList<String>();
	private ArrayList<String> oldnodes = new ArrayList<String>();
	private JButton refresh = new JButton(Main.lang.VTSrefresh);
	private Logger logger;
	
	/**
	 * Ĭ�Ϲ��캯��
	 */
	public TopologyVisualizationServices()
	{
		addButton();
		try {
			countNodes();
			createTopo();
		} catch (JSONException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		panel.add(network, BorderLayout.CENTER);
		logger = Logger.getLogger(this.getClass());
	}
	
	/**
	 * ���캯��
	 * @param topoinfo
	 */
	public TopologyVisualizationServices(JSONObject topoinfo)
	{
		addButton();
		topo = topoinfo;
		try {
			countNodes();
			createTopo();
		} catch (JSONException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		} catch (IOException e) {
			// TODO �Զ����ɵ� catch ��
			e.printStackTrace();
		}
		panel.add(network, BorderLayout.CENTER);
		logger = Logger.getLogger(this.getClass());
	}
	
	/**
	 * ���ˢ�°�ť��������
	 */
	private void addButton()
	{
		network.getToolbar().add(refresh);
		refresh.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == refresh)
				{
					try {
						refresh();
					} catch (JSONException | IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}
	
	/**
	 * ����������Ϣ
	 * @param topoinfo ������Ϣ����
	 * @throws JSONException
	 * @throws IOException 
	 */
	public void setTopo(JSONObject topoinfo) throws JSONException, IOException
	{
		topo = topoinfo;
		countNodes();
	}
	
	/**
	 * ��JPanel����������ͼ
	 * @throws IllegalArgumentException
	 * @throws JSONException
	 */
	private void createTopo() throws IllegalArgumentException, JSONException
	{
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).toString().length() == 23)//DPID����Ϊ23��Ϊ�̶�ֵ��IP���ȿɱ䡣
			{
				//������ɽ������λ�ã��ڴ��巶Χ��
				double x = 30+Math.random()*650;
				double y = 30+Math.random()*450;
				
				Node switcher = new Node(nodes.get(i).toString());//��IP/DPID��Ϊ���ID
				switcher.setImage("/switch.png");
				switcher.setName(nodes.get(i).toString());
				switcher.setLocation(x, y);
				box.addElement(switcher);
			}
			else
			{
				double x = 30+Math.random()*650;
				double y = 30+Math.random()*450;
				
				Node host = new Node(nodes.get(i).toString());
				host.setImage("/host.png");
				host.setName(nodes.get(i).toString());
				host.setLocation(x, y);
				box.addElement(host);
			}
		}
		
		for(int i=0;i<topo.getJSONArray("links").length();i++)
		{
			JSONObject link = topo.getJSONArray("links").getJSONObject(i);
			String left = link.getString("left");
			String right = link.getString("right");
			int bandwidth = link.getInt("bandwidth");
			//����link�����
			Link linkelement = new Link(left+"-"+right,(Node)box.getElementByName(left),(Node)box.getElementByName(right));
			//System.out.println("linkID:"+link.getID());
			linkelement.setName("bandwidth:"+String.valueOf(bandwidth)+" Mbps");
			box.addElement(linkelement);
		}
	}
	
	/**
	 * ����������Ϣ
	 * @throws JSONException
	 * @throws IOException
	 */
	public void updateTopo() throws JSONException, IOException
	{
		topo = TopologyService.getTopo();
		logger.debug("Topo Updated: "+topo);
		logger.info("Topo Updated");
	}
	
	/**
	 * ���½���б�
	 * @throws JSONException
	 * @throws IOException
	 */
	public void updateNodes() throws JSONException, IOException
	{
		updateTopo();
		nodes.clear();
		countNodes();
	}
	
	/**
	 * ͳ�������еĽ������������IP/DPID�����б���
	 * @throws JSONException
	 * @throws IOException 
	 */
	private void countNodes() throws JSONException, IOException
	{
		int length = topo.getJSONArray("links").length();
		for(int i=0;i<length;i++)
		{
			String left = topo.getJSONArray("links").getJSONObject(i).getString("left");
			String right = topo.getJSONArray("links").getJSONObject(i).getString("right");
			if(!nodes.contains(left))
			{
				nodes.add(left);
			}
			if(!nodes.contains(right))
			{
				nodes.add(right);
			}
		}
	}
	
	/**
	 * ˢ�����ˣ��Ƴ�ʧЧ�Ľ�����·����������Ľ�����·�����ಿ�ֱ��ֲ���
	 * @throws JSONException
	 * @throws IOException
	 */
	private void refresh() throws JSONException, IOException
	{
		oldnodes.clear();
		oldnodes.addAll(nodes);
		updateNodes();
		removeNonexistent();
		addNew();
		network.updateUI();		
	}
	
	/**
	 * �Ƴ�ʧЧ�Ľ�����·
	 * @throws JSONException
	 */
	private void removeNonexistent() throws JSONException
	{
		String linkID=null;
		for(int i =0;i<oldnodes.size();i++)
		{
			if(!(nodes.contains(oldnodes.get(i))))//����ɽ���б������½���б��в�������Ԫ�أ���ɾ��֮��
			{
				box.removeElementByID(oldnodes.get(i).toString());
				if(oldnodes.get(i).length() == 23)
				{
					logger.info("Switch: "+oldnodes.get(i).toString()+" is removed");
				}
				else
				{
					logger.info("Host: "+oldnodes.get(i).toString()+" is removed");
				}
				
				linkID = oldnodes.get(i) + "-" + searchRightNode(oldnodes.get(i).toString());
				box.removeElementByID(linkID);
				logger.info("Link: "+linkID+" is removed");
			}
			network.updateUI();
			
		}
	}
	
	/**
	 * �����Ը������Ϊ�������·�ϵ��ҽ�����Ʋ�����
	 * @param l ��������������
	 * @return
	 * @throws JSONException
	 */
	private String searchRightNode(String l) throws JSONException
	{
		JSONArray links = topo.getJSONArray("links");
		String r=null;
		
		for(int i=0;i<links.length();i++)
		{
			String left = links.getJSONObject(i).getString("left");
			String right = links.getJSONObject(i).getString("right");
			if(left.equals(l))
			{
				r = right;
			}
		}
		return r;
	}
	
	/**
	 * ��������Ľ�����·
	 * @throws JSONException
	 */
	private void addNew() throws JSONException
	{
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).toString().length() == 23 && !(oldnodes.contains(nodes.get(i))))
			{
				//������ɽ������λ�ã��ڴ��巶Χ��
				double x = 30+Math.random()*650;
				double y = 30+Math.random()*450;
				
				Node switcher = new Node(nodes.get(i).toString());//��IP/DPID��Ϊ���ID
				switcher.setImage("/switch.png");
				switcher.setName(nodes.get(i).toString());
				switcher.setLocation(x, y);
				box.addElement(switcher);
				logger.info("Switch: "+nodes.get(i).toString()+" is added");
				
				String right = searchRightNode(nodes.get(i).toString());
				int bandwidth = searchBandwidth(nodes.get(i).toString());
				
				Link link = new Link(nodes.get(i)+"-"+right,switcher,(Node)box.getElementByID(right));
				link.setName("bandwidth:"+String.valueOf(bandwidth)+" Mbps");
				box.addElement(link);
				logger.info("Link: "+nodes.get(i)+"-"+right+" is added");
				
			}
			else if(!(oldnodes.contains(nodes.get(i))))
			{
				double x = 30+Math.random()*650;
				double y = 30+Math.random()*450;
				
				Node host = new Node(nodes.get(i).toString());
				host.setImage("/host.png");
				host.setName(nodes.get(i).toString());
				host.setLocation(x, y);
				box.addElement(host);
				logger.info("Host: "+nodes.get(i).toString()+" is added");
				
				String right = searchRightNode(nodes.get(i).toString());
				//System.out.println(right);
				int bandwidth = searchBandwidth(nodes.get(i).toString());
				
				Link link = new Link(nodes.get(i)+"-"+right,host,(Node)box.getElementByID(right));
				link.setName("bandwidth:"+String.valueOf(bandwidth)+" Mbps");
				box.addElement(link);
				logger.info("Link: "+nodes.get(i)+"-"+right+" is added");
			}
		}
	}
	
	/**
	 * ���Ҹ���������·�Ĵ�������
	 * @param l
	 * @return
	 * @throws JSONException
	 */
	private int searchBandwidth(String l) throws JSONException
	{
		JSONArray links = topo.getJSONArray("links");
		int bw=0;
		for(int i=0;i<links.length();i++)
		{
			//��ȡ������·��Ϣ
			String left = links.getJSONObject(i).getString("left");
			int bandwidth = links.getJSONObject(i).getInt("bandwidth");
			
			//������·����
			
			if(left.equals(l))
			{
				bw = bandwidth;
			}
		}
		return bw;
	}
	
	/**
	 * �����������ڵ�JPanel
	 * @return
	 */
	public JPanel getTopoPanel()
	{
		return panel;
	}
	
	/**
	 * ��������������TNetwork����
	 * @return
	 */
	public TNetwork getNetwork()
	{
		return network;
	}
	
	/**
	 * ��������������DataBox����
	 * @return
	 */
	public TDataBox getDataBox()
	{
		return box;
	}
	
	/**
	 * ����ˢ�°�ť
	 */
	public void refreshDisable()
	{
		refresh.setEnabled(false);
	}
	
	/**
	 * �ṩȫ�ֵ������ػ湦�ܣ�����������
	 * FIXME: ���񲻹���
	 * @throws JSONException
	 * @throws IOException
	 */
	public void globalRedraw() throws JSONException, IOException
	{
		box = new TDataBox();
		network = new TNetwork();
		countNodes();
		createTopo();
		network.updateUI();
		panel.add(network);
		panel.updateUI();
	}
	
	/**
	 * ���ڲ���
	 * @param args
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void main(String[] args) throws JSONException, IOException
	{
		JFrame f = new JFrame();
		TopologyVisualizationServices tvs = new TopologyVisualizationServices(TopologyService.getTopo());
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(tvs.getTopoPanel());
		f.setVisible(true);
	}
}
