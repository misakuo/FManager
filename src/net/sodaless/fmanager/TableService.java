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
 * 自定义表格服务
 * @author Misaku
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
	
	/**
	 * 默认构造器
	 */
	public TableService()
	{
		cn = getContentPane();
		table = new JTable();
		table.setDefaultRenderer(getClass(), r);
		initTable();
	}
	
	/**
	 * 带表头初始化参数的构造器
	 * @param title 表头
	 */
	public TableService(String[] title)
	{
		cn = getContentPane();
		initTableModel(title);
		table = new JTable(tm);
		table.setDefaultRenderer(getClass(), r);
		initTable();
	}
	
	/**
	 * 获取组件中的表格对象
	 * @return
	 */
	public JTable getTable()
	{
		return table;
	}
	
	/**
	 * 获取组件中的JPanel对象
	 * @return
	 */
	public JScrollPane getPanel()
	{
		return sc;
	}
	
	/**
	 * 获取组件中的Model对象
	 * @return
	 */
	public DefaultTableModel getModel()
	{
		return tm;
	}
	
	/**
	 * 初始化表格的Modal对象
	 * @param s
	 */
	private void initTableModel(String[] s)
	{
		String[] title = s;
		String[][] data = {};
		tm = new DefaultTableModel(data,title);
		
	}
	
	/**
	 * 初始化表格对象
	 */
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
	
	/**
	 * 将row中的信息添加到表格中
	 * @param row
	 */
	public void addRow(String[] row)
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.addRow(row);
		FitTableColumns(table);
	}
	
	/**
	 * 根据表格内容自动调整列宽
	 * @param myTable
	 */
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


