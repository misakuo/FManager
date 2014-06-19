/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * @author Misaku
 *
 */
public class FlowManager extends JFrame implements ActionListener {

	private Container cn;
	private JPanel menu = new JPanel();
	private JPanel statusBar = new JPanel();
	private TableService ts = new TableService();
	
	public FlowManager()
	{
		cn = this.getContentPane();
		cn.setLayout(new BorderLayout());
		initMenu();
		initStatusBar();
		ts.dispose();
		cn.add(menu,BorderLayout.NORTH);
		cn.add(ts.getPanel(),BorderLayout.CENTER);
		cn.add(statusBar,BorderLayout.SOUTH);
		this.pack();
		this.setVisible(true);
	}
	
	private void initMenu()
	{
		JButton add = new JButton("add flow");
		Choice sw = new Choice();
		menu.add(add);
		menu.add(sw);
	}
	
	private void initStatusBar()
	{
		JLabel l = new JLabel("Status");
		statusBar.add(l);
	}
	
	/* （非 Javadoc）
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO 自动生成的方法存根

	}
	
	public static void main(String[] args)
	{
		FlowManager fm = new FlowManager();
	}

}
