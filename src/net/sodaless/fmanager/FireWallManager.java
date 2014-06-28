/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import net.sodaless.fmanager.util.LinkButton;
import net.sodaless.fmanager.util.MyCellEditor;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.TWaverUtil;

/**
 * @author Misaku
 *
 */
public class FireWallManager extends JFrame implements ActionListener {

	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger;
	private Container cn;
	private String[] title = {"RuleID","Priority","Match","Action"};
	private JPanel menu = new JPanel();
	private JPanel status = new JPanel();
	private JLabel l = new JLabel("status");
	private TableService ts = new TableService(title);
	private JButton enable = new JButton("Enable");
	private JButton disable = new JButton("Disable");
	private JButton stat = new JButton("Status");
	private JButton query = new JButton("Query");
	private JButton mask = new JButton("subnet-mask");
	private JButton add = new JButton("Add");
	private JButton del = new JButton("Delete");
	private JButton push = new JButton("Push Rule");
	private LinkButton link = new LinkButton("http://docs.projectfloodlight.org/display/floodlightcontroller/Firewall+REST+API");
	private RulesEditor rules = new RulesEditor(12,2);
	
	public FireWallManager()
	{
		logger = Logger.getLogger(getClass());
		cn = this.getContentPane();
		cn.setLayout(new BorderLayout());
		ts.dispose();
		ts.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn col = ts.getTable().getColumnModel().getColumn(2);
		col.setPreferredWidth(357);
		initMenu();
		initStatusBar();
		String api = "/wm/firewall/module/status/json";
		try {
			String r = ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, api));
			JSONObject re = new JSONObject(r);
			l.setText(re.getString("result"));
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		cn.add(menu,BorderLayout.NORTH);
		cn.add(ts.getPanel(),BorderLayout.CENTER);
		cn.add(status,BorderLayout.SOUTH);
	}
	
	private void initMenu()
	{
		menu.add(stat);
		menu.add(enable);
		menu.add(disable);
		menu.add(query);
		menu.add(mask);
		menu.add(add);
		menu.add(del);
		
		stat.addActionListener(this);
		enable.addActionListener(this);
		disable.addActionListener(this);
		query.addActionListener(this);
		mask.addActionListener(this);
		add.addActionListener(this);
		del.addActionListener(this);		
	}
	
	private void initStatusBar()
	{
		status.add(l);
		l.setFont(new Font("alias", Font.BOLD, 15));
		l.setText("Status:");
	}
	
	public static void showWindow()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		FireWallManager window = new FireWallManager();
		window.setSize(600, 250);
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/firewall.png"));
		window.setIconImage(img);
		window.setTitle("FireWall Manager");
		window.setVisible(true);
		TWaverUtil.centerWindow(window);
		window.logger.info("FireWall Manager launched");
	}
	
	private void showRulesEditor()
	{
		clearTable();
		MyCellEditor.initComboBox();
		JFrame f = new JFrame("FireWall Rules Editor");
		Image img = Toolkit.getDefaultToolkit().getImage(f.getClass().getResource("/firewall.png"));
		f.setIconImage(img);
		Container cn = f.getContentPane();
		rules.setRowHeight(20);
		TableColumn firstColumn = rules.getColumnModel().getColumn(0);
		firstColumn.setPreferredWidth(150);
		firstColumn.setMaxWidth(150);
		firstColumn.setMinWidth(150);
		TableColumn secondColumn = rules.getColumnModel().getColumn(1);
		secondColumn.setPreferredWidth(400);
		JPanel bar = new JPanel();
		link.setSize(20, 10);
		link.setText("See help   ");
		bar.add(link);
		bar.add(push);
		initRulesHeader();
		cn.add(rules,BorderLayout.CENTER);
		cn.add(bar,BorderLayout.SOUTH);
		f.setSize(520, 312);
		f.setVisible(true);
		//f.pack();
		
		push.addActionListener(this);
	}
	
	private void initRulesHeader()
	{
		ArrayList<String> field = new ArrayList<String>(Arrays.asList("switchid","priority","src-inport","src-mac","dst-mac","dl-type","src-ip","dst-ip","nw-proto","tp-src","tp-dst","action"));
		int rowNum = 0;
		Iterator<String> it = field.iterator();
		while(it.hasNext())
		{
			//logger.debug(it.next());
			rules.setValueAt(it.next(), rowNum, 0);
			rowNum++;
		}
	}
	
	private String matchFilter(JSONObject match) throws JSONException
	{
		String r = "";
		if(!match.getString("dpid").equals("ff:ff:ff:ff:ff:ff:ff:ff"))
		{
			r = r + " dpid:" + match.getString("dpid");
		}
		if(match.getInt("in_port") != 0)
		{
			r = r + " dpid:" + match.getString("in_port");
		}
		if(!match.getString("dl_src").equals("00:00:00:00:00:00"))
		{
			r = r + " src-mac:" + match.getString("dl_src");
		}
		if(!match.getString("dl_dst").equals("00:00:00:00:00:00"))
		{
			r = r + " dst-mac:" + match.getString("dl_dst");
		}
		if(match.getInt("dl_type") != 0)
		{
			r = r + " dl_type:" + match.getInt("dl_type");
		}
		if(!match.getString("nw_src_prefix").equals("0.0.0.0"))
		{
			r = r + " src-ip:" + match.getString("nw_src_prefix") + "/" + match.getInt("nw_src_maskbits");
		}
		if(!match.getString("nw_dst_prefix").equals("0.0.0.0"))
		{
			r = r + " dst-ip:" + match.getString("nw_dst_prefix") + "/" + match.getInt("nw_dst_maskbits");
		}
		if(match.getInt("nw_proto") != 0)
		{
			r = r + " protocol:" + match.getInt("nw_proto");
		}
		if(match.getInt("tp_src") != 0)
		{
			r = r + " src-port:" + match.getInt("tp_src");
		}
		if(match.getInt("tp_dst") != 0)
		{
			r = r + " dst-port:" + match.getInt("tp_dst");
		}
		
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
	
	/* （非 Javadoc）
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO 自动生成的方法存根
		if(e.getSource() == stat)
		{
			String api = "/wm/firewall/module/status/json";
			try {
				String r = ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, api));
				JSONObject re = new JSONObject(r);
				l.setText("FireWall Status: " + re.getString("result"));
				logger.debug("FireWall status response: " + r);
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == enable)
		{
			String api = "/wm/firewall/module/enable/json";
			try {
				String r = ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, api));
				JSONObject re = new JSONObject(r);
				l.setText("FireWall: "+re.getString("status") + ", " + re.getString("details"));
				logger.debug("FireWall enable operating response: " + r);
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == disable)
		{
			String api = "/wm/firewall/module/disable/json";
			try {
				String r = ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, api));
				JSONObject re = new JSONObject(r);
				l.setText("FireWall: "+re.getString("status") + ", " + re.getString("details"));
				logger.debug("FireWall disable operating response: " + r);
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == query)
		{
			clearTable();
			String api = "/wm/firewall/rules/json";
			String r;
			try {
				r = ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, api));
				JSONArray re = new JSONArray(r);
				parseReply(re);
				l.setText("Query firewall rules success");
				logger.debug("Querry firewall rules prsponse: " + r);
			} catch (IOException | JSONException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}	
		}
		
		if(e.getSource() == mask)
		{
			String api = "/wm/firewall/module/subnet-mask/json";
			try {
				String r = ConnectionService.doGet(ConnectionService.addressBuilder(Main.ip, Main.port, api));
				l.setText("Subnet-mask: " + r);
				logger.debug("FireWall query subnet-mask response: " + r);
			} catch (IOException e1) {
				// TODO 自动生成的 catch 块
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == add)
		{
			showRulesEditor();
		}
		
		if(e.getSource() == del)
		{
			String id;
			id = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 0).toString();
			String param = "{\"ruleid\":\"" + id + "\"}";
			logger.debug("Delete rule: " + param);
			try {
				String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/firewall/rules/json"), param);
				ts.getModel().removeRow(ts.getTable().getSelectedRow());
				logger.info(r);
				l.setText("FireWall entry " + id + " deleted" );
			} catch (IOException e1) {
			e1.printStackTrace();
			}
		}
		
		if(e.getSource() == push)
		{
			String address = ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/firewall/rules/json");
			try {
				String re = "";
				re = ConnectionService.doPost(address, ruleBuilder(rules).toString());
				l.setText(re);
				logger.debug(re);
			} catch (JSONException | IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @param r
	 * @throws JSONException 
	 */
	private void parseReply(JSONArray r) throws JSONException 
	{
		String id="",por="",match="",action="";
		for(int i=0;i<r.length();i++)
		{
			id = r.getJSONObject(i).getString("ruleid");
			por = String.valueOf(r.getJSONObject(i).getInt("priority"));
			match = matchFilter(r.getJSONObject(i));
			action = r.getJSONObject(i).getString("action");
			
			String[] row = {id,por,match,action};
			ts.addRow(row);
		}		
	}

	private JSONObject ruleBuilder(JTable t) throws JSONException
	{
		JSONObject rule = new JSONObject();
		
		for(int i=0;i<12;i++)
		{
			if(t.getValueAt(i, 1) != null)
			{
				rule.put(t.getValueAt(i, 0).toString(),t.getValueAt(i, 1).toString());
			}
		}
		logger.debug("FireWall rule entry: " + rule);
		return rule;
	}
}

class RulesEditor extends JTable
{
	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getClass());

	public RulesEditor(int row,int col)
	{
		super(row,col);
	}
	
	public boolean isCellEditable(int row, int column)
	{
		return column==0 ? false:true;
	}
	
	public TableCellEditor getCellEditor(int row, int column) {
		if (row == 0 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.switchlist);
		}
		if(row == 2 && column == 1)
		{
			if(this.getValueAt(0,1) == null)
			{
				JOptionPane.showMessageDialog(null,"ERROR: Plese select switch-id first!","ERROR",JOptionPane.ERROR_MESSAGE);
				logger.warn("Please select the switch-id first!");
			}
			else
			{
				ArrayList<Integer> portlist = Main.swPort.get(this.getValueAt(0, 1));
				Iterator<Integer> it = portlist.iterator();
				while(it.hasNext())
				{
					MyCellEditor.inport.addItem(it.next());
				}
				MyCellEditor.inport.removeItem(65534);
				return new DefaultCellEditor(MyCellEditor.inport);
			}
		}
		if(row == 5 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.dl_type);
		}
		if(row == 8 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.nw_protocol);
		}
		if(row == 11 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.fwaction);
		}
		return super.getCellEditor(row, column);
	}
}
