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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.TWaverUtil;

/**
 * @author Misaku
 * 负载均衡服务管理器，可对负载均衡策略各条目进行增删操作
 *
 */
public class LoadBalanceManager extends JFrame implements ActionListener {

	/**
	 *序列化 
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getClass());
	private Container cn;
	private String[] s = {"VIP","Pool","Members"};
	private TableService ts = new TableService(s);
	private JPanel menu = new JPanel();
	private JPanel statusBar = new JPanel();
	private JLabel l = new JLabel();
	private JButton query = new JButton("Query");
	private JButton add = new JButton("Add");
	private JButton delvip = new JButton("Del VIP");
	private JButton delpool = new JButton("Del Pool");
	private JButton delmem = new JButton("Del Members");
	private Choice poollist = new Choice();
	private Choice memlist = new Choice();
	private final String VIP = "/quantum/v1.0/vips/";
	private final String POOLS = "/quantum/v1.0/pools/";
	private final String MEMBERS = "/quantum/v1.0/members/";
	private JSONArray spools = new JSONArray();
	private JSONArray smembers = new JSONArray();
	private JButton pv = new JButton("Push VIP");
    private JButton pp = new JButton("Push pools");
    private JButton pm = new JButton("Push members");
    private JTable tvip = new JTable(5,2);
    private JTable tpool = new JTable(4,2);
    private JTable tmem = new JTable(4,2);
	
    /**
     * 默认构造器
     */
	public LoadBalanceManager()
	{
		cn = this.getContentPane();
		cn.setLayout(new BorderLayout());
		initMenu();
		initStatusBar();
		initTableListener();
		ts.dispose();
		ts.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn col = ts.getTable().getColumnModel().getColumn(2);
		col.setPreferredWidth(295);
		cn.add(menu,BorderLayout.NORTH);
		cn.add(ts.getPanel(),BorderLayout.CENTER);
		cn.add(statusBar,BorderLayout.SOUTH);
	}
	
	/**
	 * 初始化管理器窗口的菜单
	 */
	private void initMenu()
	{
		menu.add(query);
		menu.add(add);
		menu.add(delvip);
		menu.add(poollist);
		menu.add(delpool);
		menu.add(memlist);
		menu.add(delmem);
		
		query.addActionListener(this);
		add.addActionListener(this);
		delvip.addActionListener(this);
		delpool.addActionListener(this);
		delmem.addActionListener(this);
		pv.addActionListener(this);
		pp.addActionListener(this);
		pm.addActionListener(this);		
	}
	
	/**
	 * 初始化管理器窗口的状态栏
	 */
	private void initStatusBar()
	{
		l.setText("Load Balance");
		statusBar.add(l);
	}
	
	/**
	 * 初始化管理器窗口的表格控件，并为空间添加消息监听器并实现消息处理逻辑
	 */
	private void initTableListener()
	{
		ts.getTable().getSelectionModel().addListSelectionListener(new ListSelectionListener(){

			@Override
			public void valueChanged(ListSelectionEvent e) {
				int row = ts.getTable().getSelectedRow();
				String p = ts.getTable().getValueAt(row, 1).toString();
				String m = ts.getTable().getValueAt(row, 2).toString();
				String[] psplit = p.split(" ");
				String[] msplit = m.split(" ");
				poollist.removeAll();
				memlist.removeAll();
				for(int i=0;i<psplit.length;i++)
				{
					if(psplit[i].startsWith("id"))
					{
						String[] sp = psplit[i].split(":");
						poollist.add(sp[1]);
						logger.debug("pool:" + sp[1]);
					}
				}
				
				for(int j=0;j<msplit.length;j++)
				{
					if(msplit[j].startsWith("id"))
					{
						String[] mp = msplit[j].split(":");
						memlist.add(mp[1]);
						logger.debug("member:" + mp[1]);
					}
				}
			
			}
			
		});
	}
	
	/**
	 * 负载均衡管理器的入口，负责展示窗口
	 */
	public static void showWindow()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		LoadBalanceManager window = new LoadBalanceManager();
		window.setSize(540, 200);
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/lb.png"));
		window.setIconImage(img);
		window.setTitle("Load Balance Manager");
		window.setVisible(true);
		TWaverUtil.centerWindow(window);
		window.logger.info("Load Balance Manager launched");
	}
	
	/**
	 * 清空负载均衡管理器窗口内表格的内容
	 */
	private void clearTable()
	{
		//FIXME: 在为JTable对象添加ListSelectionListener且选中表格某一行之后清空表格会出现数组越界的错误。
		int num = ts.getTable().getRowCount();
		for(int i=0;i<num;i++)
		{
			ts.getModel().removeRow(i);
		}
	}
	
	/**
	 * 解析请求API后获得的回复，从中获得负载均衡各策略字段信息，并将信息添加到对应单元格中
	 * @param v 请求vips后得到的回复
	 * @param p 请求pools后得到的回复
	 * @param m 请求members后的到的回复
	 * @throws JSONException
	 */
	private void parseReply(String v,String p,String m) throws JSONException
	{
		String vip="",pools="",mem="";
		JSONArray vj = new JSONArray(v);
		JSONArray pj = new JSONArray(p);
		JSONArray mj = new JSONArray(m);
		for(int i=0;i<vj.length();i++)
		{
			vip += " name:" + vj.getJSONObject(i).getString("name");
			vip += " id:" + vj.getJSONObject(i).getString("id");
			vip += " address:" + fromIPv4Address(Integer.parseInt(vj.getJSONObject(i).getString("address")));
			vip += " protocol:" + vj.getJSONObject(i).getString("protocol");
			vip += " port:" + vj.getJSONObject(i).getString("port");
			
			for(int j=0;j<pj.length();j++)
			{
				if(pj.getJSONObject(j).getString("vipId").equals(vj.getJSONObject(i).getString("id")))
				{
					pools += " name:" + pj.getJSONObject(j).getString("name");
					pools += " id:" + pj.getJSONObject(j).getString("id");
					pools += " pool:" + pj.getJSONObject(j).getString("pool");
				}
			}
			
			for(int k=0;k<mj.length();k++)
			{
				if(mj.getJSONObject(k).getString("vipId").equals(vj.getJSONObject(i).getString("id")) && mj.getJSONObject(k).getString("poolId").equals(pj.getJSONObject(i).getString("id")))
				{
					mem += " id:" + mj.getJSONObject(k).getString("id");
					mem += " address:" + fromIPv4Address(Integer.parseInt(mj.getJSONObject(k).getString("address")));
					mem += " port:" + mj.getJSONObject(k).getString("port");
				}
			}
			String[] row = {vip,pools,mem};
			ts.addRow(row);
			vip="";pools="";mem="";
		}
		logger.info("Query load balance entry success");
	}
	
	/**
	 * 删除给定vip_id对应的VIP条目
	 * @param vipid
	 * @throws IOException
	 * @throws JSONException
	 */
	private void delVIP(String vipid) throws IOException, JSONException
	{
		queryPM();
		String api = "/quantum/v1.0/vips/" + vipid;
		String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, api),"Don't need");
		if(r.equals("0"))
		{
			logger.info("Delete vip id=" + vipid + " successed");
		}
		for(int i=0;i<spools.length();i++)
		{
			if(spools.getJSONObject(i).getString("vipId").equals(vipid))
			{
				delPool(spools.getJSONObject(i).getString("id"));
			}
		}
		logger.info("Delete vip completed");
		l.setText("Vip id=" + vipid + " and it's pools & members deleted");
	}
	
	/**
	 * 删除给定pool_id对应的pools条目
	 * @param poolid
	 * @throws IOException
	 * @throws JSONException
	 */
	private void delPool(String poolid) throws IOException, JSONException
	{
		 String api = "/quantum/v1.0/pools/" + poolid;
		 String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, api),"Don't need");
		if(r.equals("0"))
		{
			logger.info("Delete pools id=" + poolid + " successed");
		}
		for(int i=0;i<smembers.length();i++)
		{
			if(smembers.getJSONObject(i).getString("poolId").equals(poolid))
			{
				delMember(smembers.getJSONObject(i).getString("id"));
			}
		}
		poollist.remove(poolid);
		logger.info("Delete pools completed");
		l.setText("Pool id=" + poolid + "and it's members deleted");
	}
	
	/**
	 * 删除给定member_id对应的members条目
	 * @param memid
	 * @throws IOException
	 */
	private void delMember(String memid) throws IOException
	{
		String api = "/quantum/v1.0/members/" + memid;
		String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, api),"Don't need");
		if(r.equals("0"))
		{
			logger.info("Delete members id=" + memid + " successed");
		}
		memlist.remove(memid);
		logger.info("Delete members completed");
		l.setText("Members id=" + memid + "deleted");
	}
	
	/**
	 * 查询当前的pools和members信息
	 */
	private void queryPM()
	{
		String p = ConnectionService.addressBuilder(Main.ip, Main.port, POOLS);
		String m = ConnectionService.addressBuilder(Main.ip, Main.port, MEMBERS);
		try {
			spools = new JSONArray(ConnectionService.doGet(p));
			smembers = new JSONArray(ConnectionService.doGet(m));
			
		} catch (IOException | JSONException e1) {
			l.setText("Query failed, see console log");
			e1.printStackTrace();
		}
	}
	
	/**
	 * 将int形式表示的ipv4地址转换为形如xxx.xxx.xxx.xxx的地址
	 * @param ipAddress int形式表示的地址
	 * @return
	 */
	private String fromIPv4Address(int ipAddress) {
        StringBuffer sb = new StringBuffer();
        int result = 0;
        for (int i = 0; i < 4; ++i) {
            result = (ipAddress >> ((3-i)*8)) & 0xff;
            sb.append(Integer.valueOf(result).toString());
            if (i != 3)
                sb.append(".");
        }
        return sb.toString();
    }
	
	/**
	 * 显示添加负载均条目的窗口
	 */
	private void showAddWindow()
	{
		JFrame add = new JFrame("Add Load Balance Entry");
		add.setSize(485, 410);
		add.setResizable(false);
		Image img = Toolkit.getDefaultToolkit().getImage(add.getClass().getResource("/lb.png"));
		add.setIconImage(img);
		Container c = add.getContentPane();
		c.setLayout(new BorderLayout());
		JPanel vp = new JPanel(new BorderLayout());
		JPanel ppp = new JPanel(new BorderLayout());
		JPanel mp = new JPanel(new BorderLayout());
		
	    JLabel v = new JLabel("Add VIP");
	    JLabel p = new JLabel("Add pools");
	    JLabel m = new JLabel("Add members");
		tvip.setRowHeight(20);
	    tpool.setRowHeight(20);
	    tmem.setRowHeight(20);
	    initTableHeader(tvip,1);
	    initTableHeader(tpool,2);
	    initTableHeader(tmem,3);
	    vp.add(v,BorderLayout.NORTH);
	    vp.add(tvip,BorderLayout.CENTER);
	    vp.add(pv,BorderLayout.SOUTH);
	    ppp.add(p,BorderLayout.NORTH);
	    ppp.add(tpool,BorderLayout.CENTER);
	    ppp.add(pp,BorderLayout.SOUTH);
	    mp.add(m,BorderLayout.NORTH);
	    mp.add(tmem,BorderLayout.CENTER);
	    mp.add(pm,BorderLayout.SOUTH);
	    c.add(vp,BorderLayout.NORTH);
	    c.add(ppp,BorderLayout.CENTER);
	    c.add(mp,BorderLayout.SOUTH);
	    add.setVisible(true);
	    
	}
	
	/**
	 * 初始化添加负载均衡条目窗口三个表格的信息
	 * @param t 表格名称
	 * @param i 表格序号（1、2、3）
	 */
	private void initTableHeader(JTable t,int i)
	{
		ArrayList<String> vip = new ArrayList<String>(Arrays.asList("id","name","protocol","address","port"));
		ArrayList<String> pool = new ArrayList<String>(Arrays.asList("id","name","protocol","vip_id"));
		ArrayList<String> member = new ArrayList<String>(Arrays.asList("id","address","port","pool_id"));
		
		ArrayList<String> field = new ArrayList<String>();
		switch(i)
		{
		case 1:
			field = new ArrayList<String>(vip);	
			break;
		case 2:
			field = new ArrayList<String>(pool);
			break;
		case 3:
			field = new ArrayList<String>(member);
			break;
		}
		
		
		int rowNum = 0;
		Iterator<String> it = field.iterator();
		while(it.hasNext())
		{
			//logger.debug(it.next());
			t.setValueAt(it.next(), rowNum, 0);
			rowNum++;
		}
	}
	
	/**
	 * 解析表格内容，将信息组合成VIP条目信息并下发
	 */
	private void pushVIP()
	{
		JSONObject vipentry = new JSONObject();
		for(int i=0;i<5;i++)
		{
			if(tvip.getValueAt(i,1) != null)
			{
				try {
					vipentry.put(tvip.getValueAt(i,0).toString(), tvip.getValueAt(i,1).toString());
				} catch (JSONException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		String api = ConnectionService.addressBuilder(Main.ip, Main.port, "/quantum/v1.0/vips/");
		try {
			ConnectionService.doPost(api, vipentry.toString());
			JOptionPane.showMessageDialog(null,"VIP entry pushed","Success",JOptionPane. INFORMATION_MESSAGE);
			l.setText("VIP entry pushed.");
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		logger.debug("VIP entry pushed:" + vipentry);
	}
	
	/**
	 *解析表格内容，将信息组合成Pools条目信息并下发
	 */
	private void pushPools()
	{
		JSONObject poolentry = new JSONObject();
		for(int i=0;i<4;i++)
		{
			if(tpool.getValueAt(i,1) != null)
			{
				try {
					poolentry.put(tpool.getValueAt(i,0).toString(), tpool.getValueAt(i,1).toString());
				} catch (JSONException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		String api = ConnectionService.addressBuilder(Main.ip, Main.port, "/quantum/v1.0/pools/");
		try {
			ConnectionService.doPost(api, poolentry.toString());
			JOptionPane.showMessageDialog(null,"Pools entry pushed","Success",JOptionPane. INFORMATION_MESSAGE);
			l.setText("Pools entry pushed.");
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		logger.debug("Pools entry pushed:" + poolentry);
	}
	
	/**
	 * 解析表格内容，将信息组合成Members条目信息并下发
	 */
	private void pushMembers()
	{
		JSONObject mementry = new JSONObject();
		for(int i=0;i<4;i++)
		{
			if(tmem.getValueAt(i,1) != null)
			{
				try {
					mementry.put(tmem.getValueAt(i,0).toString(), tmem.getValueAt(i,1).toString());
				} catch (JSONException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
		String api = ConnectionService.addressBuilder(Main.ip, Main.port, "/quantum/v1.0/members/");
		try {
			ConnectionService.doPost(api, mementry.toString());
			JOptionPane.showMessageDialog(null,"Members entry pushed","Success",JOptionPane. INFORMATION_MESSAGE);
			l.setText("Members entry pushed.");
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		logger.debug("Members entry pushed:" + mementry);
	}
	
	/**
	 * 消息监听器
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == query)
		{
			clearTable();
			String api1 = ConnectionService.addressBuilder(Main.ip, Main.port, VIP);
			String api2 = ConnectionService.addressBuilder(Main.ip, Main.port, POOLS);
			String api3 = ConnectionService.addressBuilder(Main.ip, Main.port, MEMBERS);
			try {
				String r1 = ConnectionService.doGet(api1);
				String r2 = ConnectionService.doGet(api2);
				String r3 = ConnectionService.doGet(api3);
				logger.debug("Response of VIP: " + r1);
				logger.debug("Response of POOLS: " + r2);
				logger.debug("Response of MEMBERS: " + r3);
				parseReply(r1,r2,r3);
				
			} catch (IOException | JSONException e1) {
				l.setText("Query failed, see console log");
				e1.printStackTrace();
			}
			l.setText("Query successed");
		}
		
		if(e.getSource() == add)
		{
			showAddWindow();
		}

		if(e.getSource() == delvip)
		{
			String vip;
			vip = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 0).toString();
			String vips[] = vip.split(" ");
			String ids[] = vips[2].split(":");
			try {
				delVIP(ids[1]);
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == delpool)
		{
			try {
				delPool(poollist.getSelectedItem());
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
			
		}
		
		if(e.getSource() ==delmem)
		{
			try {
				delMember(memlist.getSelectedItem());
			} catch (IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == pv)
		{
			pushVIP();
		}
		
		if(e.getSource() == pp)
		{
			pushPools();
		}
		
		if(e.getSource() == pm)
		{
			pushMembers();
		}
	}
}
