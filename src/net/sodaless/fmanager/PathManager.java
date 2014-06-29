/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

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
 * 路径管理模块，提供路径的计算和下发服务
 * @author Misaku
 */
public class PathManager extends JFrame implements ActionListener {

	/**
	 * last update:2014-6-18
	 */
	private static final long serialVersionUID = 2346794804456463815L;
	
	private Logger logger;
	private JPanel menu = new JPanel(new GridLayout(15,6,10,10));
	private TopologyVisualizationServices tvs = new TopologyVisualizationServices(Main.currentTopo);
	private JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,menu,tvs.getTopoPanel());
	private JButton algorithm = new JButton(Main.lang.PMloadalog);
	private JButton calc = new JButton(Main.lang.PMcalc);
	private JButton push =new JButton(Main.lang.PMpush);
	private JCheckBox autopush = new JCheckBox(Main.lang.PMautopush);
	private JCheckBox twoway = new JCheckBox(Main.lang.PMsymmetricpath);
	private JLabel empty = new JLabel();
	private JLabel srcname = new JLabel(Main.lang.PMsrc);
	private Choice srcvalue = new Choice();
	private JLabel dstname = new JLabel(Main.lang.PMdst);
	private Choice dstvalue = new Choice();
	private JLabel bw = new JLabel(Main.lang.PMbw);
	private JTextField bwvalue = new JTextField();
	private JButton commit = new JButton(Main.lang.PMcommit);
	private JButton add = new JButton(Main.lang.PMadd);
	private JButton reset = new JButton(Main.lang.PMreset);
	
	private String alogpath = "";
	private String flowcommit = "";
	private JSONObject network = new JSONObject();
	private ArrayList<HashMap<Integer,String>> pathSet = new ArrayList<HashMap<Integer,String>>();//解析出的路径信息集合
	private TableService ts;
	private TableService pathtable;
	private boolean tablecreated = false;
	private int flownum = 0;
	
	/**
	 * 默认构造器
	 */
	public PathManager()
	{
		if(Main.online == false)
		{
			tvs.refreshDisable();
		}
		
		network = Main.currentTopo;
		
		logger = Logger.getLogger(getClass());
		getContentPane().add(split, BorderLayout.CENTER);
		split.setDividerLocation(160);
		
		menu.add(algorithm);
		menu.add(calc);
		menu.add(push);
		menu.add(autopush);
		menu.add(twoway);
		menu.add(empty);
		menu.add(srcname);
		menu.add(srcvalue);
		menu.add(dstname);
		menu.add(dstvalue);
		menu.add(bw);
		menu.add(bwvalue);
		menu.add(add);
		menu.add(commit);
		menu.add(reset);
		
		algorithm.addActionListener(this);
		calc.addActionListener(this);
		push.addActionListener(this);
		add.addActionListener(this);
		commit.addActionListener(this);
		reset.addActionListener(this);
		
		for(String value : Main.hostSet)
		{
			srcvalue.add(value);
			dstvalue.add(value);
		}
		
	}
	
	/**
	 * 显示路径管理模块窗口，包括拓扑区域和侧边栏
	 */
	public static void showWindow()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		//创建窗体
		PathManager window = new PathManager();
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/path.png"));
		window.setIconImage(img);
		window.setBounds(500, 500, 1000, 600);
		window.setTitle("Path Viewer");
		TWaverUtil.centerWindow(window);
		window.setVisible(true);
		window.logger.info("Path Manager launched");
	}
	
	/**
	 * 重置拓扑信息，流信息和路径信息
	 * @throws JSONException
	 * @throws IOException
	 */
	private void reset() throws JSONException, IOException
	{
		String mode = null;
		if(Main.online)
		{
			mode = "online";
			network = TopologyService.getTopo();
		}
		else
		{
			mode = "offline";
			network = Main.currentTopo;
		}
		flownum = 0;
		flowcommit = "";
		tablecreated = false;
		pathSet.clear();
		if(ts != null)
		{
			ts.dispose();
		}
		if(pathtable != null)
		{
			pathtable.dispose();
		}
		logger.info("Current mode is " + mode + ", the context is initialized");
	}
	
	/**
	 * 用已选择的路由算法计算路径
	 */
	private void pathCalc()
	{
		Runtime rn=Runtime.getRuntime(); 
		Process p=null; 
		try{ 
			logger.debug("Info to algo process: " + network);
			p=rn.exec(alogpath+" "+network);//相当于执行"alog.exe links"

			InputStream out = p.getInputStream();
			InputStream err = p.getErrorStream();
			
			BufferedReader output = new BufferedReader(new InputStreamReader(out));
			BufferedReader error = new BufferedReader(new InputStreamReader(err));
			StringBuilder sb = new StringBuilder();
			String line = null,getoutput = null;
			
			logger.info("Thr error stream of algo process： " + error.readLine());
			try{
				while((line = output.readLine())!=null)
				{
					sb.append(line);
				}
			}catch (IOException ioe) {
				ioe.printStackTrace();
			}finally{
				try{
					out.close();
				}catch(IOException ie) {
					ie.printStackTrace();
				}
			}
			
			getoutput = sb.toString();
			logger.info("The output stream of algo process: " + getoutput);
			if(!getoutput.equals("error"))
			{
				JSONArray pathjson = new JSONArray(getoutput);
				
				/**
				 * 将算法模块输出（pathjson）转换成map，存入path中
				 * pathjson形如[{"0":"192.168.3.13"},{"1":"00:00:00:e0:4c:50:5f:29"},{"2":"00:00:00:e0:4c:43:32:68"},{"3":"192.168.1.140"}]
				 */				
				for(int a=0;a<pathjson.length();a++)//遍历输出json，将每一条路径信息put入array
				{
					HashMap<Integer,String> path = new HashMap<Integer,String>();//解析出的单条路径信息
					JSONArray pathelement = new JSONArray(pathjson.getJSONArray(a).toString());
					for(int i=0;i<pathelement.length();i++)
					{
						//路径信息放入hashmap
						path.put(i,pathelement.getJSONObject(i).getString(String.valueOf(i)));
					}
					
					if(autopush.isSelected())
					{
						toFlows(path,a);
						if(twoway.isSelected())
							{
								symmetricPath(path,a);
							}
					}
					
					pathSet.add(path);
					String[] pathentry = {String.valueOf(a),path.get(0),path.get(path.size()-1),path.toString()};
					pathtable.addRow(pathentry);
					//FIXME: 将路径列表每一行的第一个单元格背景色设置为和该行路径一样的颜色
					//pathtable.getTable().getCellRenderer(0, a).getTableCellRendererComponent(pathtable.getTable(), 0, false, false, 0, a).setBackground(selectColor(a));
					logger.info("Path entry" + a + ": " + path );
				}
			}
			else
			{
				logger.error("Unexpected error on algo process");
			}
			}catch(Exception ex){ 
			ex.printStackTrace();
			}
			
	}
	
	/**
	 * 将路径信息集合转换成单条路径并依次下发
	 * @param path
	 * @param color
	 * @throws JSONException
	 */
	private void toFlows(HashMap<Integer,String> path,int color) throws JSONException
	{
		if(!(path.get(1).equals("No Path!")))
		{
			JSONObject flow = new JSONObject();
			String src=null,dst=null,flowname=null;
			src = path.get(0).toString();
			dst = path.get((path.size()-1)).toString();
			flowname = src + "to" +dst;
			linkHighLight(src,path.get(1).toString(),color);
			
			logger.info("Path src: " + src + " path dst: " + dst);	
			for(int i=1;i<(path.size()-1);i++)
			{
				String nextHop = path.get(i+1).toString();
				String currentNode = path.get(i).toString();
					
				flow.put("switch", path.get(i).toString());
				flow.put("name", flowname);
				flow.put("priority", 32768);
				flow.put("ether-type", "0x0800");
				flow.put("src-ip", src);
				flow.put("dst-ip", dst);
				flow.put("active", true);
				flow.put("actions", "output="+findOutputPort(path.get(i).toString(),nextHop));
				
				linkHighLight(currentNode,nextHop,color);
				if(Main.online)
				{
					pushFlows(flow);
				}
				logger.info("Flow entry pushed: " + flow.toString());
			}
				
		}
	}
	
	/**
	 * 给定一个link上的左端点，找出其与link相连的端口。
	 * @param left
	 * @param right
	 * @return
	 * @throws JSONException
	 */
	private int findOutputPort(String left,String right) throws JSONException
	{
		int length=0,port=0;
		
		length = network.getJSONArray("links").length();
		for(int i=0;i<length;i++)
		{
			String l = network.getJSONArray("links").getJSONObject(i).getString("left");
			String r = network.getJSONArray("links").getJSONObject(i).getString("right");
			if(l.equals(left) && r.equals(right))
			{
				port = network.getJSONArray("links").getJSONObject(i).getInt("left-port");
				break;
			}
			else if(l.equals(right) && r.equals(left))
			{
				port = network.getJSONArray("links").getJSONObject(i).getInt("right-port");
			}
		}
		return port;
	}
	
	/**
	 * 为路径信息集合中的每一条路径生成一条与其对称的路径并下发
	 * @param path
	 * @param color
	 * @throws JSONException
	 */
	private void symmetricPath (HashMap<Integer,String> path,int color) throws JSONException
	{
		if(!(path.get(1).equals("No Path!")))
		{	
			JSONObject flow = new JSONObject();
			String src=null,dst=null,flowname=null;
			dst = path.get(0).toString();
			src = path.get((path.size()-1)).toString();
			flowname = src + "to" + dst;
			linkHighLight(src,path.get((path.size()-2)).toString(),color);
			
			logger.info("Symmetric path src: " + src + " dst: " + dst);
			
			for(int i=(path.size()-1);i>0;i--)
			{
				String nextHop = path.get(i-1).toString();
				String currentNode = path.get(i).toString();
				
				flow.put("switch", path.get(i).toString());
				flow.put("name", flowname);
				flow.put("priority", 32768);
				flow.put("ether-type", "0x0800");
				flow.put("src-ip", src);
				flow.put("dst-ip", dst);
				flow.put("active", true);
				flow.put("actions", "output="+findOutputPort(path.get(i).toString(),nextHop));
				
				linkHighLight(currentNode,nextHop,color);
				if(Main.online)
				{
					pushFlows(flow);
				}
				logger.info("Flow entry pushed: " + flow.toString());
			}
		}
	}
	
	/**
	 * 下发流到switch
	 * @param flow
	 */
	private void pushFlows(JSONObject flow)
	{
		String f = flow.toString();
		String address = ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/staticflowentrypusher/json");
		try {
			String reply = ConnectionService.doPost(address, f);
			logger.info(reply);
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	/**
	 * 将结点current和next之间的路径用颜色c进行高亮
	 * @param current
	 * @param next
	 * @param c
	 */
	private void linkHighLight(String current,String next,int c)
	{
		TDataBox pbox = tvs.getDataBox();
		TNetwork pnetwork = tvs.getNetwork();
		Link link;
		Color color=null;
		color = selectColor(c);
		String a = current+"-"+next;
		String b = next+"-"+current;
		
		pbox.removeElementByID(a);
		pbox.removeElementByID(b);
		link = new Link(a,(Node)pbox.getElementByID(current),(Node)pbox.getElementByID(next));
		link.putLinkColor(color);
		link.setName("PATH "+ c);
		pbox.addElement(link);
		pnetwork.updateUI();
	}
	
	/**
	 * 返回c对应的颜色值
	 * @param c
	 * @return
	 */
	private Color selectColor(int c)
	{
		Color color = null;
		switch (c)
		{
			case 0:
				color = Color.GREEN;
				break;
			case 1:
				color = Color.CYAN;
				break;
			case 2:
				color = Color.MAGENTA;
				break;
			case 3:
				color = Color.ORANGE;
				break;
			case 4:
				color = Color.BLUE;
				break;
			case 5:
				color = Color.PINK;
				break;
			case 6:
				color = Color.YELLOW;
				break;
			default:
				color = Color.BLACK;
		}
		return color;
	}
	
	/**
	 * 显示一个待添加的流列表
	 */
	private void showFlowSet()
	{
		String[] title = {"#","src","dst","bandwidth"};
		ts = new TableService(title);
		ts.setIconImage(Toolkit.getDefaultToolkit().getImage(ts.getClass().getResource("/path.png")));
		ts.setTitle("流条目列表");
	}
	
	/**
	 * 显示一个计算出的路径集合列表
	 */
	private void showPathSet()
	{
		String[] title = {"#","src","dst","Path"};
		pathtable = new TableService(title);
		pathtable.setIconImage(Toolkit.getDefaultToolkit().getImage(ts.getClass().getResource("/path.png")));
		pathtable.setTitle("路径列表");
	}
	
	/**
	 * 消息监听器
	 */
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getSource() == algorithm)
		{
			if(srcvalue.getSelectedItem().length()<3 || dstvalue.getSelectedItem().length()<3)
			{
				JOptionPane.showMessageDialog(null,"请输入流信息","ERROR",JOptionPane.ERROR_MESSAGE);
			}
			else {
			JFileChooser fc = new JFileChooser();
			fc.showDialog(null,"选择算法程序");
			alogpath = fc.getSelectedFile().getPath();
			logger.info("Algorithm program path: " + alogpath);
			alogpath = alogpath.replaceAll("\\\\", "/");
			alogpath = "\""+alogpath+"\"";
			}
		}
		
		if(e.getSource() == calc)
		{
			showPathSet();
			pathCalc();
		}
		
		if(e.getSource() == push)
		{
			try {
				for(int i=0;i<pathSet.size();i++)
				{
					HashMap<Integer,String> path = new HashMap<Integer,String>();
					path = pathSet.get(i);
					//System.out.println("PATH EXPORT"+i+":"+path);
					toFlows(path,i);
					if(twoway.isSelected())
					{
						symmetricPath(path,i);
					}
					path.clear();
				}
			} catch (JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == add)
		{
			if(srcvalue.getSelectedItem().equals(dstvalue.getSelectedItem()))
			{
				JOptionPane.showMessageDialog(null,"源和目的不能为同一结点","ERROR",JOptionPane.ERROR_MESSAGE);
			}
			else if(bwvalue.getText().isEmpty())
			{
				JOptionPane.showMessageDialog(null,"带宽值不能为空","ERROR",JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				String flowsinfo = "{";
				flowsinfo = flowsinfo + "\"src\":\"" + srcvalue.getSelectedItem() + "\",";
				flowsinfo = flowsinfo + "\"dst\":\"" + dstvalue.getSelectedItem() + "\",";
				flowsinfo = flowsinfo + "\"bandwidth\":" + bwvalue.getText() + "},";
		
				flowcommit += flowsinfo;
				logger.info("add flow entry: " + flowsinfo);
				flownum++;
				
				if(!tablecreated)
				{
					showFlowSet();
					tablecreated = true;
				}
				
				String[] row = {String.valueOf(flownum),srcvalue.getSelectedItem(),dstvalue.getSelectedItem(),bwvalue.getText()};
				ts.addRow(row);
			}
		}
		
		if(e.getSource() == commit)
		{
			flowcommit = flowcommit.substring(0,(flowcommit.length()-1));
			flowcommit = "[" + flowcommit + "]";
			try {
				network.put("flows", new JSONArray(flowcommit));
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			logger.info("Commit flows: " + flowcommit);
			logger.debug("Info to Algorithm program: " + network);
		}
		
		if(e.getSource() == reset)
		{
			try {
				reset();
			} catch (JSONException | IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
	}

}
