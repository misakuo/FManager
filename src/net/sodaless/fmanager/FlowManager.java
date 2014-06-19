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
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Misaku
 *
 */
public class FlowManager extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4561313732259115602L;
	private Logger logger;
	private String[] s = {"Name","Priority","Match","Action"};
	private Container cn;
	private JPanel menu = new JPanel();
	private JPanel statusBar = new JPanel();
	private TableService ts = new TableService(s);
	private JLabel l = new JLabel("Status");
	private JButton query = new JButton("Query");
	private JButton add = new JButton("Add");
	private JButton del = new JButton("Delete");
	private JButton delall = new JButton("Delele All");
	private Choice sw = new Choice();
	private JSONObject reply;
	
	public FlowManager()
	{
		logger = Logger.getLogger(getClass());
		cn = this.getContentPane();
		cn.setLayout(new BorderLayout());
		initMenu();
		initStatusBar();
		ts.dispose();
		ts.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		//ts.getTable().setEnabled(false);//设置整个表格为不可编辑
		cn.add(menu,BorderLayout.NORTH);
		cn.add(ts.getPanel(),BorderLayout.CENTER);
		cn.add(statusBar,BorderLayout.SOUTH);
		this.pack();
		this.setVisible(true);
	}
	
	private void initMenu()
	{
		menu.add(sw);
		menu.add(query);
		menu.add(add);
		menu.add(del);
		menu.add(delall);
		for(String e : Main.swSet)
		{
			sw.add(e);
		}
		query.addActionListener(this);
		add.addActionListener(this);
		del.addActionListener(this);
		delall.addActionListener(this);
	}
	
	private void initStatusBar()
	{
		statusBar.add(l);
	}
	
	private void parseReply(String sw) throws JSONException
	{
		String name="",priority,match,action;
		@SuppressWarnings("unchecked")
		Iterator<String> it = reply.getJSONObject(sw).keys();
		while(it.hasNext())
		{
			name = it.next().toString();
			match = matchFilter(reply.getJSONObject(sw).getJSONObject(name).getJSONObject("match"));
			priority = String.valueOf(reply.getJSONObject(sw).getJSONObject(name).getInt("priority"));
			action = reply.getJSONObject(sw).getJSONObject(name).getJSONArray("actions").toString();
		
			String[] row = {name,priority,match,action};
			ts.addRow(row);
		}
	}
	
	private String matchFilter(JSONObject match) throws JSONException
	{
		String r = "";
		if(!match.getString("dataLayerDestination").equals("00:00:00:00:00:00"))
		{
			r = r + "dst-mac:" + match.getString("dataLayerDestination");
		}
		if(!match.getString("dataLayerSource").equals("00:00:00:00:00:00"))
		{
			r = r + " src-mac:" + match.getString("dataLayerSource");
		}
		if(!match.getString("dataLayerType").equals("0x0000"))
		{
			r = r + " ether-type:" + match.getString("dataLayerType");
		}
		if(match.getInt("dataLayerVirtualLan") != -1)
		{
			r = r + " vlan-id:" + match.getInt("dataLayerVirtualLan");
		}
		if(match.getInt("dataLayerVirtualLanPriorityCodePoint") != 0)
		{
			r = r + " vlan-priority:" + match.getInt("dataLayerVirtualLanPriorityCodePoint");
		}
		if(match.getInt("inputPort") != 0)
		{
			r= r + " ingress-port" + match.getInt("inputPort");
		}
		if(!match.getString("networkDestination").equals("0.0.0.0"))
		{
			r = r + " dst-ip" + match.getString("networkDestination");
		}
		if(match.getInt("networkProtocol") != 0)
		{
			r = r + " protocol:" + match.getInt("networkProtocol");
		}
		if(!match.getString("networkSource").equals("0.0.0.0"))
		{
			r = r + " src-ip:" + match.getString("networkSource");
		}
		if(match.getInt("networkTypeOfService") != 0)
		{
			r = r + " tos-bits:" + match.getInt("networkTypeOfService");
		}
		if(match.getInt("transportDestination") != 0)
		{
			r = r + " dst-port:" + match.getInt("transportDestination");
		}
		if(match.getInt("transportSource") != 0)
		{
			r = r + " src-port:" + match.getInt("transportSource");
		}
		logger.debug(sw.getSelectedItem() + "vaild match entry: " + r);
		return r;
		
	}
	
	/* （非 Javadoc）
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自动生成的方法存根
		if(e.getSource() == query)
		{
			clearTable();			
			String dpid = sw.getSelectedItem();
			String address = ConnectionService.addressBuilder(Main.ip, Main.port, apiBuilder(dpid));
			try {
				reply = new JSONObject(ConnectionService.doGet(address));
				if(reply.isNull(dpid))
				{
					l.setText("Flowtable in " + dpid + " is empty!");
				}
				else
				{
					parseReply(dpid);
					l.setText("Query flow entry in switch " + dpid + " successed");
				}
				logger.debug("Query result: " + ConnectionService.doGet(address));
				
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == add)
		{
			
		}
		
		if(e.getSource() == del)
		{
			if(ts.getTable().hasFocus())
			{
				String flowname;
				flowname = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 0).toString();
				String param = "{\"name\":\"" + flowname + "\"}";
				try {
					String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/staticflowentrypusher/json"), param);
					ts.getModel().removeRow(ts.getTable().getSelectedRow());
					logger.info(r);
					l.setText("Entry " + flowname + " in switch " + sw.getSelectedItem() + "deleted" );
				} catch (IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
				}
			}
		}
		
		if(e.getSource() == delall)
		{
			clearTable();
			try {
				ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/staticflowentrypusher/clear/all/json"));
				logger.info("All static flow entry has deleted");
				l.setText("All static flow entry has deleted");
			} catch (IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}

	}
	
	private String apiBuilder(String sw)
	{
		String r = "";
		r = "/wm/staticflowentrypusher/list/" + sw + "/json";
		return r;
	}
	
	private void clearTable()
	{
		int num = ts.getTable().getRowCount();
		
		for(int i=0;i<num;i++)
		{
			ts.getModel().removeRow(i);
		}
	}
	
	public static void showWindow()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		FlowManager window = new FlowManager();
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/path.png"));
		window.setIconImage(img);
		window.setTitle("Flow Manager");
		window.logger.info("Flow Manager launched");
	}

}
