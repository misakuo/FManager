/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
	private final String VIP = "/quantum/v1.0/vips/";
	private final String POOLS = "/quantum/v1.0/pools/";
	private final String MEMBERS = "/quantum/v1.0/members/";
	private JSONArray spools = new JSONArray();
	private JSONArray smembers = new JSONArray();
	
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
	
	private void initMenu()
	{
		menu.add(query);
		menu.add(add);
		menu.add(delvip);
		menu.add(delpool);
		menu.add(delmem);
		
		query.addActionListener(this);
		add.addActionListener(this);
		delvip.addActionListener(this);
		delpool.addActionListener(this);
		delmem.addActionListener(this);
		
	}
	
	private void initStatusBar()
	{
		l.setText("Load Balance");
		statusBar.add(l);
	}
	
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
				for(int i=0;i<psplit.length;i++)
				{
					if(psplit[i].startsWith("id"))
					{
						logger.debug(psplit[i]);
					}
				}
				
				for(int j=0;j<msplit.length;j++)
				{
					if(msplit[j].startsWith("id"))
					{
						logger.debug(msplit[j]);
					}
				}
			
			}
			
		});
	}
	
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
	
	private void clearTable()
	{
		//FIXME: 在为JTable对象添加ListSelectionListener且选中表格某一行之后清空表格会出现数组越界的错误。
		int num = ts.getTable().getRowCount();
		for(int i=0;i<num;i++)
		{
			ts.getModel().removeRow(i);
		}
	}
	
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
			vip += " address:" + vj.getJSONObject(i).getString("address");
			vip += " protocol:" + vj.getJSONObject(i).getString("protocol");
			vip += " port:" + vj.getJSONObject(i).getString("port");
			
			for(int j=0;j<pj.length();j++)
			{
				if(pj.getJSONObject(i).getString("vipId").equals(vj.getJSONObject(i).getString("id")))
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
					mem += " address:" + mj.getJSONObject(k).getString("address");
					mem += " port:" + mj.getJSONObject(k).getString("port");
				}
			}
			String[] row = {vip,pools,mem};
			ts.addRow(row);
		}
		logger.info("Query load balance entry success");
	}
	
	private void delVIP(String vipid) throws IOException, JSONException
	{
		queryPM();
		String api = "/quantum/v1.0/vips/" + vipid;
		String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, api),null);
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
		for(int j=0;j<smembers.length();j++)
		{
			if(smembers.getJSONObject(j).getString("vipId").equals(vipid))
			{
				delMember(smembers.getJSONObject(j).getString("id"));
			}
		}
		logger.info("Delete vip completed");
		l.setText("Vip id=" + vipid + " and it's pools & members deleted");
	}
	
	private void delPool(String poolid) throws IOException, JSONException
	{
		 String api = "/quantum/v1.0/pools/" + poolid;
		 String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, api),null);
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
		logger.info("Delete pools completed");
		l.setText("Pool id=" + poolid + "and it's members deleted");
	}
	
	private void delMember(String memid) throws IOException
	{
		String api = "/quantum/v1.0/members/" + memid;
		String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, api),null);
		if(r.equals("0"))
		{
			logger.info("Delete members id=" + memid + " successed");
		}
		logger.info("Delete members completed");
		l.setText("Members id=" + memid + "deleted");
	}
	
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
	
	/* （非 Javadoc）
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
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
			
		}

		if(e.getSource() == delvip)
		{
			String vip;
			vip = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 0).toString();
		}
		
		if(e.getSource() == delpool)
		{
			String pool;
			pool = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 1).toString();
		}
		
		if(e.getSource() ==delmem)
		{
			String mem;
			mem = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 0).toString();
		}
	}

}
