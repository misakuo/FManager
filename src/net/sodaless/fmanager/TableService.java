/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Enumeration;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

/**
 * @author Misaku
 *
 */
public class TableService extends JFrame {

	/**
	 * last update:2014-06-18
	 */
	private static final long serialVersionUID = 6313340183417593662L;
	
	private JTable table;
	private DefaultTableModel tm;
	private Container cn;
	private JScrollPane sc;
	private DefaultTableCellRenderer r = new DefaultTableCellRenderer();
	
	public TableService()
	{
		cn = getContentPane();
		table = new JTable();
		table.setDefaultRenderer(getClass(), r);
		initTable();
	}
	
	public TableService(String[] title)
	{
		cn = getContentPane();
		initTableModel(title);
		table = new JTable(tm);
		table.setDefaultRenderer(getClass(), r);
		initTable();
	}
	
	public JTable getTable()
	{
		return table;
	}
	
	public JScrollPane getPanel()
	{
		return sc;
	}
	
	private void initTableModel(String[] s)
	{
		String[] title = s;
		String[][] data = {};
		tm = new DefaultTableModel(data,title);
		
	}
	
	private void initTable()
	{
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		
		table.setIntercellSpacing(new Dimension(1,1)); 
		table.setGridColor(Color.BLACK);
		table.setPreferredScrollableViewportSize(new Dimension(400,80));
		sc=new JScrollPane(table) ; 
		cn.setLayout(new BorderLayout()); 
		cn.add(sc,BorderLayout.CENTER);
		this.setVisible(true); 
		this.pack();
	}
	
	public void addRow(String[] row)
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(row);
		FitTableColumns(table);
	}
	
	public void FitTableColumns(JTable myTable)
	{
	    JTableHeader header = myTable.getTableHeader();
	    int rowCount = myTable.getRowCount();
	    Enumeration<TableColumn> columns = myTable.getColumnModel().getColumns();
	    while(columns.hasMoreElements())
	    {
	        TableColumn column = (TableColumn)columns.nextElement();
	        int col = header.getColumnModel().getColumnIndex(column.getIdentifier());
	        int width = (int)myTable.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(myTable, column.getIdentifier(), false, false, -1, col).getPreferredSize().getWidth();                
	        for(int row = 0; row<rowCount; row++)
	         {
	             int preferedWidth = (int)myTable.getCellRenderer(row, col).getTableCellRendererComponent(myTable,
	             myTable.getValueAt(row, col), false, false, row, col).getPreferredSize().getWidth();
	             width = Math.max(width, preferedWidth);
	         }
	         header.setResizingColumn(column);
	         column.setWidth(width + myTable.getIntercellSpacing().width + 5);
	     }
	}
	
}


