/**
 * 
 */
package net.sodaless.fmanager.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.sodaless.fmanager.TrafficMonitor;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import twaver.Element;
import twaver.Node;
import twaver.TUIManager;
import twaver.TWaverConst;
import twaver.TaskAdapter;
import twaver.TaskScheduler;
import twaver.chart.LineChart;

/**
 * 用于展示流量情况的动态图表
 * @author Misaku
 */
public class TrafficChart extends JFrame{

	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getLogger(getClass());
	private LineChart Chart = new LineChart();
	private Element e = new Node();
	private int last=0;
	private final String dpid;
	private final String port;
	private final String type;
	private static String title = "";
	private static JPanel panel = new JPanel();
	private boolean firstrun = true;
	private boolean log = false;
	private String logname;
	
	/**
	 * 默认构造器器
	 * @param d 交换机的dpid
	 * @param p 交换机指定端口
	 * @param t 要监控的流量类型
	 * @param l 是否生成流量日志
	 */
	public TrafficChart(String d,String p,String t,boolean l)
	{	
		try { 
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
		} catch (Exception e) {e.printStackTrace();} 
		
		dpid = d;
		port = p;
		type = t;
		log = l;
		title = dpid + "-" + port + " " + type;
		e.setName(t);
		e.putChartColor(Color.green.darker());
		Chart.getDataBox().addElement(e);
		
		Font font = TUIManager.getDefault10SizeFont();
		Chart.setBackground(Color.BLACK);
    	Chart.setForeground(Color.WHITE);
        Chart.getTitleLabel().setFont(font);
        Chart.setYAxisTextFont(font);
        Chart.setLegendFont(font);
    	Chart.setLineType(TWaverConst.LINE_TYPE_AREA);
        Chart.setYAxisTextColor(Color.white);
        Chart.setLegendOrientation(TWaverConst.LABEL_ORIENTATION_RIGHT);
        Chart.setLegendLayout(TWaverConst.LEGEND_LAYOUT_VERTICAL);
        Chart.setHighlightBackground(new Color(0, 255, 0, 128));
        Chart.setHighlightForeground(Color.YELLOW);
        Chart.setEnableYTranslate(true);
        Chart.setEnableYZoom(true);
        Chart.setAntialias(false);
        Chart.setLowerLimit(0);
        Chart.setBackgroundVisible(true);
        Chart.setXScaleLineVisible(true);
        Chart.setFixedValueCount(10);
        
        panel.setLayout(new BorderLayout());
        panel.add(Chart, BorderLayout.CENTER);
        
        if(log == true)
        {
        	newLog();
        }
        
        Image img = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/traffic.png"));
		this.setIconImage(img);
        this.setSize(new Dimension(750, 460));
        this.add(panel);
        this.setVisible(true);
        this.setTitle(title);        
	}
	
	/**
	 * 绘制流量图表，每秒更新一次
	 */
	private void drawChart()
	{
		TaskScheduler.getInstance().register(new TaskAdapter() {
            public void run(long clock) {
                int total = 1024*100;
                Chart.setYAxisText("Tot: " + NumberFormat.getInstance().format(total) + "Byte");
                Chart.setUpperLimit(total);

                int traffic;
				try {
					traffic = getPortAggregate(dpid,port,type);
					Chart.setTitle(type + ": " + NumberFormat.getInstance().format(traffic) + "Byte/s");
	                e.addChartValue(traffic);
	              
				} catch (JSONException e) {
					e.printStackTrace();
				}
            }
            public int getInterval() {
                return 1000;
            }
        });
	}
	
	/**
	 * 获取指定交换机指定端口指定类型的流量信息并返回
	 * @param dpid
	 * @param port
	 * @param type
	 * @return
	 * @throws JSONException
	 */
	private int getPortAggregate(String dpid,String port,String type) throws JSONException
	{
		int traffic=0;
		JSONObject sw = new JSONObject();
		sw = TrafficMonitor.getAggregate();
		for(int i=0;i<sw.getJSONArray(dpid).length();i++)
		{
			if(sw.getJSONArray(dpid).getJSONObject(i).getInt("portNumber") == Integer.parseInt(port))
			{
				traffic = sw.getJSONArray(dpid).getJSONObject(i).getInt(type);
			}
		}
		
		if(firstrun == true)
		{
			last = traffic;
			traffic = 0;
		}
		else
		{
			int tmp=traffic;
			traffic = traffic - last;
			last = tmp;
		}
		
		if(log == true)
		{
			toLog(String.valueOf(traffic));
		}
		
		firstrun = false;
		return traffic;
	}
	
	/**
	 * 图表模块的入口
	 * @param d
	 * @param p
	 * @param t
	 * @param l
	 */
	public static void showChart(String d,String p,String t,boolean l)
	{
		TrafficChart tc = new TrafficChart(d,p,t,l);
		tc.drawChart();
	}
	
	/**
	 * 在当前工作目录下创建一个新的日志文件
	 */
	private void newLog()
	{
		java.text.SimpleDateFormat  formatter=new  java.text.SimpleDateFormat("yyyyMMdd-HH-mm-ss");
		logname = System.getProperty("user.dir") + "\\log_" + formatter.format(new Date()) + ".txt";
		logger.info("Log file " + logname + " created");
	}
	
	/**
	 * 将str的内容写入日志文件
	 * @param str
	 */
	private void toLog(String str)
	{
		java.text.SimpleDateFormat  formatter=new  java.text.SimpleDateFormat("mm:ss:SSS");
		String t = formatter.format(new Date());
		str = t + " " + str + "\r\n";
		File log =new File(logname);
		FileWriter writer;
		try {
			writer = new FileWriter(log, true);
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
