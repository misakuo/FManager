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
 * 用于提供拓扑显示服务
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
	 * 默认构造函数
	 */
	public TopologyVisualizationServices()
	{
		addButton();
		try {
			countNodes();
			createTopo();
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		panel.add(network, BorderLayout.CENTER);
		logger = Logger.getLogger(this.getClass());
	}
	
	/**
	 * 构造函数
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
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		panel.add(network, BorderLayout.CENTER);
		logger = Logger.getLogger(this.getClass());
	}
	
	/**
	 * 添加刷新按钮到工具条
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
	 * 设置拓扑信息
	 * @param topoinfo 拓扑信息对象
	 * @throws JSONException
	 * @throws IOException 
	 */
	public void setTopo(JSONObject topoinfo) throws JSONException, IOException
	{
		topo = topoinfo;
		countNodes();
	}
	
	/**
	 * 在JPanel内生成拓扑图
	 * @throws IllegalArgumentException
	 * @throws JSONException
	 */
	private void createTopo() throws IllegalArgumentException, JSONException
	{
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).toString().length() == 23)//DPID长度为23且为固定值，IP长度可变。
			{
				//随机生成结点坐标位置，在窗体范围内
				double x = 30+Math.random()*650;
				double y = 30+Math.random()*450;
				
				Node switcher = new Node(nodes.get(i).toString());//用IP/DPID作为结点ID
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
			//生成link并添加
			Link linkelement = new Link(left+"-"+right,(Node)box.getElementByName(left),(Node)box.getElementByName(right));
			//System.out.println("linkID:"+link.getID());
			linkelement.setName("bandwidth:"+String.valueOf(bandwidth)+" Mbps");
			box.addElement(linkelement);
		}
	}
	
	/**
	 * 更新拓扑信息
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
	 * 更新结点列表
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
	 * 统计拓扑中的结点个数并将结点IP/DPID存入列表中
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
	 * 刷新拓扑，移除失效的结点和链路，添加新增的结点和链路，其余部分保持不变
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
	 * 移除失效的结点的链路
	 * @throws JSONException
	 */
	private void removeNonexistent() throws JSONException
	{
		String linkID=null;
		for(int i =0;i<oldnodes.size();i++)
		{
			if(!(nodes.contains(oldnodes.get(i))))//如果旧结点列表中有新结点列表中不包含的元素，则删除之。
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
	 * 查找以给定结点为左结点的链路上的右结点名称并返回
	 * @param l 给定的左结点名称
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
	 * 添加新增的结点和链路
	 * @throws JSONException
	 */
	private void addNew() throws JSONException
	{
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).toString().length() == 23 && !(oldnodes.contains(nodes.get(i))))
			{
				//随机生成结点坐标位置，在窗体范围内
				double x = 30+Math.random()*650;
				double y = 30+Math.random()*450;
				
				Node switcher = new Node(nodes.get(i).toString());//用IP/DPID作为结点ID
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
	 * 查找给定左结点链路的带宽并返回
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
			//获取结点和链路信息
			String left = links.getJSONObject(i).getString("left");
			int bandwidth = links.getJSONObject(i).getInt("bandwidth");
			
			//查找链路带宽
			
			if(left.equals(l))
			{
				bw = bandwidth;
			}
		}
		return bw;
	}
	
	/**
	 * 返回拓扑所在的JPanel
	 * @return
	 */
	public JPanel getTopoPanel()
	{
		return panel;
	}
	
	/**
	 * 返回拓扑容器的TNetwork对象
	 * @return
	 */
	public TNetwork getNetwork()
	{
		return network;
	}
	
	/**
	 * 返回拓扑容器的DataBox对象
	 * @return
	 */
	public TDataBox getDataBox()
	{
		return box;
	}
	
	/**
	 * 屏蔽刷新按钮
	 */
	public void refreshDisable()
	{
		refresh.setEnabled(false);
	}
	
	/**
	 * 提供全局的拓扑重绘功能，重置拓扑用
	 * FIXME: 好像不管用
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
	 * 用于测试
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
