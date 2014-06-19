/**
 * 
 */
package net.sodaless.fmanager;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.json.JSONException;

/**
 * @author Misaku
 *
 */
public class ModeSelection implements ActionListener {
	
	private JFrame init = new JFrame();
	private Image img;
	private JPanel panel = new JPanel();
	private JLabel lable = new JLabel();
	private JTextField ip = new JTextField(20);
	private JTextField port = new JTextField(5);
	private JButton online = new JButton();
	private JButton offline = new JButton();
	private JCheckBox language = new JCheckBox();
	private Logger logger;
	
	public ModeSelection()
	{
		logger = Logger.getLogger(this.getClass());
		img = Toolkit.getDefaultToolkit().getImage(init.getClass().getResource("/icon.png"));
		init.setTitle(Main.lang.initFrameTitle);
		init.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		init.setIconImage(img);
		init.setBounds(500, 400, 400, 160);
		init.setResizable(false);
		lable.setText(Main.lang.initLableText);
		ip.setText("127.0.0.1");
		port.setText("8080");
		online.setText(Main.lang.initOnlineButtonText);
		offline.setText(Main.lang.initOfflineButtonText);
		language.setText(Main.lang.initCheckBoxText);
		ip.addActionListener(this);
		port.addActionListener(this);
		online.addActionListener(this);
		offline.addActionListener(this);
		language.addActionListener(this);
		panel.add(lable);
		panel.add(ip);
		panel.add(port);
		panel.add(online);
		panel.add(offline);
		panel.add(language);
		init.add(BorderLayout.CENTER,panel);
		init.setVisible(true);
	}
	
	private String getIP()
	{
		return ip.getText();
	}
	
	private String getPort()
	{
		return port.getText();
	}
	
	private boolean connectionTest() throws IOException
	{
		String address = ConnectionService.addressBuilder(Main.ip, Main.port, "/wm/core/health/json");
		logger.debug("Controller status: " + ConnectionService.doGet(address));
		if(ConnectionService.isConnectionSuccess())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void launchOnlineMode() throws IOException
	{
		if(connectionTest())
		{
			logger.info("Successed to connect to Controller in " + Main.ip + ":" +Main.port);
			try {
				TopologyService.updateTopo();
			} catch (JSONException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
			MainWindow.showWindow();
		}
		else
		{
			logger.warn("Failed to connect to Controller in " + Main.ip + ":" +Main.port);
		}
	}
	
	public static void start()
	{
		Main.selectLanguage(true);
		ModeSelection m = new ModeSelection();
		Main.ip = m.getIP();
		Main.port = m.getPort();
		m.logger.info("Set Controller IP "+ Main.ip + " and REST serverce port "+ Main.ip + ":" +Main.port);
		m.logger.info("Current Language: ZH-CN");
	}

	/* （非 Javadoc）
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == ip)
		{
			try {
				Main.ip = ip.getText();
				launchOnlineMode();
			} catch (IOException e1) {
				logger.warn("Failed to connect to Controller in " + Main.ip + ":" +Main.port);
			}
		}
		
		if(e.getSource() == port)
		{
			try {
				Main.port = port.getText();
				launchOnlineMode();
			} catch (IOException e1) {
				logger.warn("Failed to connect to Controller in " + Main.ip + ":" +Main.port);
			}
		}
		
		if(e.getSource() == online)
		{
			try {
				logger.info("Launch the online mode");
				Main.ip = ip.getText();
				Main.port = port.getText();
				launchOnlineMode();
				Main.online = true;
				init.dispose();
			} catch (IOException e1) {
				logger.warn("Failed to connect to Controller in " + Main.ip + ":" +Main.port);
			}
		}
		
		if(e.getSource() == offline)
		{
			logger.info("Launch the offline mode");
			VirtualTopology.Init();
			Main.online = false;
			init.dispose();
		}
		
		if(e.getSource() == language)
		{
			if(Main.lang.getLanguageType() == "ZH-CN")
			{
				Main.selectLanguage(false);
				init.dispose();
				ModeSelection m = new ModeSelection();
				Main.ip = m.getIP();
				Main.port = m.getPort();
				m.logger.info("Set Controller IP "+ Main.ip + " and REST serverce port "+Main.port);
				m.logger.info("Current Language: EN-US");
			}
			else
			{
				Main.selectLanguage(true);
				init.dispose();
				ModeSelection m = new ModeSelection();
				Main.ip = m.getIP();
				Main.port = m.getPort();
				m.logger.info("Set Controller IP "+ Main.ip + " and REST serverce port "+Main.port);
				m.logger.info("Current Language: ZH-CN");
			}
		}
	}
	
}
