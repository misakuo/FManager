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

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;

import net.sodaless.fmanager.util.MyCellEditor;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.TWaverUtil;

/**
 * ��̬������ģ�飬�ṩ��̬���Ĳ�ѯ����ɾ����
 * @author Misaku
 */
public class FlowManager extends JFrame implements ActionListener {

	/**
	 * ���л�
	 */
	private static final long serialVersionUID = 1L;
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
	private JButton push = new JButton("Push");
	private Choice sw = new Choice();
	private JSONObject reply;
	private flowEntryTable table = new flowEntryTable(17,2);
	private Choice actions = new Choice();
	private JTextField value = new JTextField(20);
	
	/**
	 * Ĭ�Ϲ�����
	 */
	public FlowManager()
	{
		logger = Logger.getLogger(getClass());
		cn = this.getContentPane();
		cn.setLayout(new BorderLayout());
		initMenu();
		initStatusBar();
		ts.dispose();
		ts.getTable().setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableColumn col = ts.getTable().getColumnModel().getColumn(2);
		col.setPreferredWidth(295);
		//ts.getTable().setEnabled(false);//�����������Ϊ���ɱ༭
		cn.add(menu,BorderLayout.NORTH);
		cn.add(ts.getPanel(),BorderLayout.CENTER);
		cn.add(statusBar,BorderLayout.SOUTH);
	}
	
	/**
	 * ��ʼ���˵���
	 */
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
	
	/**
	 * ��ʼ��״̬��
	 */
	private void initStatusBar()
	{
		statusBar.add(l);
	}
	
	/**
	 * ������̬����Ŀ��Ϣ��õĻظ��н������ض�switch�ڵ�����Ϣ�������䰴��Ŀ��ӵ��б���
	 * @param sw
	 * @throws JSONException
	 */
	private void parseReply(String sw) throws JSONException
	{
		String name="",priority,match,action="";
		@SuppressWarnings("unchecked")
		Iterator<String> it = reply.getJSONObject(sw).keys();
		while(it.hasNext())
		{
			name = it.next().toString();
			match = matchFilter(reply.getJSONObject(sw).getJSONObject(name).getJSONObject("match"));
			priority = String.valueOf(reply.getJSONObject(sw).getJSONObject(name).getInt("priority"));
			if(!reply.getJSONObject(sw).getJSONObject(name).isNull("actions"))
			{
				action = reply.getJSONObject(sw).getJSONObject(name).getJSONArray("actions").toString();
			}
			String[] row = {name,priority,match,action};
			ts.addRow(row);
		}
	}
	
	/**
	 * ������õ�������Ŀƥ������й��ˣ��˳�����ƥ�䣨ֵΪ�գ�����Ŀ
	 * @param match ����õ�������Ŀƥ����
	 * @return
	 * @throws JSONException
	 */
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
		logger.debug(sw.getSelectedItem() + " vaild match entry: " + r);
		return r;
		
	}
	
	/**
	 * ��ʾ��̬����ӽ��棬�����û����벢�·��û����������Ϣ
	 */
	private void showPathEditor()
	{
		MyCellEditor.initComboBox();
		JFrame f = new JFrame("Flow Entry Editor");
		Image img = Toolkit.getDefaultToolkit().getImage(f.getClass().getResource("/flow.png"));
		f.setIconImage(img);
		Container cn = f.getContentPane();
		table.setRowHeight(20);
		TableColumn firstColumn = table.getColumnModel().getColumn(0);
		firstColumn.setPreferredWidth(150);
		firstColumn.setMaxWidth(150);
		firstColumn.setMinWidth(150);
		TableColumn secondColumn = table.getColumnModel().getColumn(1);
		secondColumn.setPreferredWidth(400);
		JPanel bar = new JPanel();
		
		bar.add(actions);
		bar.add(value);
		bar.add(push);
		initActions(actions);
		initFlowHeader(table);
		cn.add(table,BorderLayout.CENTER);
		cn.add(bar,BorderLayout.SOUTH);
		
		push.addActionListener(this);
		
		f.pack();
		f.setVisible(true);
		
	}
	
	/**
	 * ��ʼ����̬����ӽ���ĸ�ƥ��������
	 * @param t
	 */
	private void initFlowHeader(JTable t)
	{
		ArrayList<String> field = new ArrayList<String>(Arrays.asList("switch","name","priority","active","ingress-port","src-mac","dst-mac","vlan-id","vlan-priority","ether-type","tos-bits","protocol","src-ip","dst-ip","src-port","dst-port","wildcards"));
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
	 * ��ʼ����̬����ӽ����Action�б�
	 * @param c
	 */
	private void initActions(Choice c)
	{
		ArrayList<String> actions = new ArrayList<String>(Arrays.asList("output","enqueue","strip-vlan","set-vlan-id","set-vlan-priority","set-src-mac","set-dst-mac","set-tos-bits","set-src-ip","set-dst-ip","set-src-port","set-dst-port"));
		Iterator<String> it = actions.iterator();
		while(it.hasNext())
		{
			c.add(it.next());
		}
	}
	
	/**
	 * ���û��������Ϣ��ϳ�һ����̬����Ŀ���ȴ��·�
	 * @param t �����û�����ı�����
	 * @return
	 * @throws JSONException
	 */
	private JSONObject flowBuilder(JTable t) throws JSONException
	{
		JSONObject flow = new JSONObject();
		
		for(int i=0;i<=16;i++)
		{
			if(t.getValueAt(i, 1) != null)
			{
				if(i==9)
				{
					String s = t.getValueAt(9, 1).toString();
					String[] split = s.split("\\(");
					flow.put(t.getValueAt(9, 0).toString(), split[0]);
				}
				else
				{
					flow.put(t.getValueAt(i, 0).toString(),t.getValueAt(i, 1).toString());
			
				}
			}
		}
		flow.put("actions", actions.getSelectedItem() + "=" + value.getText());
		logger.debug("Flow entry: " + flow);
		return flow;
	}
	
	/**
	 * ��Ϣ������
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
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
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == add)
		{
			MyCellEditor.clear();
			showPathEditor();
		}
		
		if(e.getSource() == del)
		{
			String flowname;
			flowname = ts.getTable().getValueAt(ts.getTable().getSelectedRow(), 0).toString();
			String param = "{\"name\":\"" + flowname + "\"}";
			logger.debug("Delete entry: " + param);
			try {
				String r = ConnectionService.doDelete(ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/staticflowentrypusher/json"), param);
				ts.getModel().removeRow(ts.getTable().getSelectedRow());
				logger.info(r);
				l.setText("Entry " + flowname + " in switch " + sw.getSelectedItem() + "deleted" );
			} catch (IOException e1) {
			e1.printStackTrace();
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
				e1.printStackTrace();
			}
		}
		
		if(e.getSource() == push)
		{
			String address = ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/staticflowentrypusher/json");
			try {
				String re = "";
				re = ConnectionService.doPost(address, flowBuilder(table).toString());
				l.setText(re);
				logger.debug(re);
			} catch (JSONException | IOException e1) {
				e1.printStackTrace();
			}
		}

	}
	
	/**
	 * ����������switch��ѯ�������REST API
	 * @param sw ����switch��dpid
	 * @return
	 */
	private String apiBuilder(String sw)
	{
		String r = "";
		r = "/wm/staticflowentrypusher/list/" + sw + "/json";
		return r;
	}
	
	/**
	 * ��ձ���ڵ�������Ŀ
	 */
	private void clearTable()
	{
		int num = ts.getTable().getRowCount();
		
		for(int i=0;i<num;i++)
		{
			ts.getModel().removeRow(i);
		}
	}
	
	/**
	 * ������ģ�����ڣ����𴴽�����
	 */
	public static void showWindow()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		FlowManager window = new FlowManager();
		window.setSize(540, 200);
		Image img = Toolkit.getDefaultToolkit().getImage(window.getClass().getResource("/flow.png"));
		window.setIconImage(img);
		window.setTitle("Flow Manager");
		window.setVisible(true);
		TWaverUtil.centerWindow(window);
		window.logger.info("Flow Manager launched");
	}

}

/**
 * �ڲ��࣬������һ�������������༭��
 * @author Misaku
 *
 */
class flowEntryTable extends JTable
{
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getClass());

	/**
	 * Ĭ�Ϲ�����������һ���к��зֱ�Ϊrow��col�ı��
	 * @param row
	 * @param col
	 */
	public flowEntryTable(int row,int col)
	{
		super(row,col);
	}
	
	/**
	 * ����row��col��Ӧ�ĵ�Ԫ���Ƿ�ɱ༭
	 */
	public boolean isCellEditable(int row, int column){
		return column==0 ? false:true;
	}

	/**
	 * Ϊ�ض���Ԫ�񷵻ض��Ƶı༭��
	 */
	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		if (row == 0 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.switchlist);
		}
		if(row == 3 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.active);
		}
		if(row == 4 && column == 1)
		{
			if(this.getValueAt(0, 1) == null)
			{
				JOptionPane.showMessageDialog(null,"ERROR: Plese select switch first!","ERROR",JOptionPane.ERROR_MESSAGE);
				logger.warn("Please select the switch first!");
			}
			ArrayList<Integer> portlist = Main.swPort.get(this.getValueAt(0, 1));
			Iterator<Integer> it = portlist.iterator();
			while(it.hasNext())
			{
				MyCellEditor.inport.addItem(it.next());
			}
			MyCellEditor.inport.removeItem(65534);
			return new DefaultCellEditor(MyCellEditor.inport);
		}
		if(row == 9 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.ethtype);
		}
		if(row == 11 && column == 1)
		{
			return new DefaultCellEditor(MyCellEditor.protocol);
		}
		return super.getCellEditor(row, column);
	}
}
