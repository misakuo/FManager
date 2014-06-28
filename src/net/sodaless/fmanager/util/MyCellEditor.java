/**
 * 
 */
package net.sodaless.fmanager.util;

import java.util.Iterator;

import javax.swing.JComboBox;



import net.sodaless.fmanager.Main;

/**
 * @author Misaku
 *
 */
public class MyCellEditor {

	/**
	 * –Ú¡–ªØ
	 */
	public static JComboBox<String> switchlist = new JComboBox<String>();
	public static JComboBox<Boolean> active = new JComboBox<Boolean>();
	public static JComboBox<Integer> inport = new JComboBox<Integer>();
	public static JComboBox<String> ethtype = new JComboBox<String>();
	public static JComboBox<String> protocol = new JComboBox<String>();
	public static JComboBox<String> dl_type = new JComboBox<String>();
	public static JComboBox<String> nw_protocol = new JComboBox<String>();
	public static JComboBox<String> fwaction = new JComboBox<String>();
	
	public static void initComboBox()
	{
		String[] etype = {"0x0800(IP)","0x0806(ARP)","0x8035(RARP)","0x814C(SNMP)","0x86DD(IPv6)","0x880B(PPP)","0x8847(MPLS-unicast)","0x8848(MPLS-multicast)","0x88CC(LLDP)"};
		String[] proto = {"TCP","UDP"};
		String[] dltype = {"ARP","IPv4"};
		String[] nwp = {"TCP","UDP","ICMP"};
		String[] action = {"ALLOW","DENY"};
		
		Iterator<String> it = Main.swSet.iterator();
		while(it.hasNext())
		{
			switchlist.addItem(it.next());
		}
		
		active.addItem(true);
		active.addItem(false);
		
		ethtype = new JComboBox<String>(etype);
		protocol = new JComboBox<String>(proto);
		
		dl_type = new JComboBox<String>(dltype);
		nw_protocol = new JComboBox<String>(nwp);
		fwaction = new JComboBox<String>(action);
		
	}
	
	public MyCellEditor()
	{
		initComboBox();
	}
	
	public static void clear()
	{
		switchlist.removeAllItems();
		active.removeAllItems();
		inport.removeAllItems();
		ethtype.removeAllItems();
		protocol.removeAllItems();
	}
	
}
