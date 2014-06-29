/**
 * 
 */
package net.sodaless.fmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.*;

/**
 * 用于与Floodlight的REST API交互并获取信息，为其他模块提供基础服务
 * @author Misakuo
 * @date 2014-6-16
 *
 */
public class TopologyService {
	
	private static String topostring = "";
	private static JSONObject topojson = null;
	private static String ip = "127.0.0.1";
	private static String port = "8080";
	private static URL features = null;
	private static URL links = null;
	private static URL device = null;
	private static JSONObject fjo = null;//保存API features获取到的信息
	private static JSONArray lja = null,dja = null;//分别保存API links、device获取到的信息
	
	private static JSONArray link = new JSONArray();//topojson中links的value部分
	private static String nodes = null;//topojson中的nodes部分
	private static HashSet<String> hostSet = new HashSet<String>();
	private static HashSet<String> swSet = new HashSet<String>();
	private static HashMap<String,ArrayList<Integer>> swPort = new HashMap<String,ArrayList<Integer>>();
	
	private static Logger logger;
	
	static{
		try {
			features = new URL("http://" + ip + ":" + port + "/wm/core/switch/all/features/json");
			links = new URL("http://" + ip + ":" + port + "/wm/topology/links/json");
			device = new URL("http://" + ip + ":" + port + "/wm/device/");
			logger = Logger.getLogger(TopologyService.class);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 设置控制器IP地址
	 * @param usrip
	 */
	public static void setControllerIP(String usrip)
	{
		ip = usrip;
	}
	
	/**
	 * 设置REST服务绑定端口
	 * @param usrport
	 */
	public static void setRestServicesPort(String usrport)
	{
		port = usrport;
	}
	
	/**
	 * 返回一个表示当前最新拓扑信息的JSONObject
	 * @return
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static JSONObject getTopo() throws JSONException, IOException
	{
		link = new JSONArray();
		updateTopo();
		return topojson;
	}
	
	/**
	 * 获取控制器IP地址（默认为127.0.0.1）
	 * @return
	 */
	public static String getControllerIP()
	{
		return ip;
	}
	
	/**
	 * 获取控制器REST服务绑定的端口号（默认为8080）
	 * @return
	 */
	public static String getRestServersPort()
	{
		return port;
	}
	
	/**
	 * 更新拓扑信息
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void updateTopo() throws JSONException, IOException
	{
		hostSet.clear();
		swSet.clear();
		swPort.clear();
		fjo = new JSONObject(doConnection(features));
		parseFeatures(fjo);
		lja = new JSONArray(doConnection(links));
		parseLinks(lja);
		dja = new JSONArray(doConnection(device));
		parseDevice(dja);
		countNodes();
		buildString();
		buildJSON();
		Main.currentTopo = topojson;
		Main.hostSet = hostSet;
		Main.swSet = swSet;
		Main.swPort = swPort;
		logger.info("Data from Main class is updated");
	}
	
	/**
	 * 获取当前拓扑中所有Host的IP地址的集合
	 * @return
	 */
	public static HashSet<String> getHostSet()
	{
		return hostSet;
	}
	
	/**
	 * 获取当前拓扑中所有交换机的DPID的集合
	 * @return
	 */
	public static HashSet<String> getSWSet()
	{
		return swSet;
	}
	
	/**
	 * 获取当前拓扑中所有交换机自身DPID到其端口号列表的映射
	 * @return
	 */
	public static HashMap<String,ArrayList<Integer>> getSWPortSet()
	{
		return swPort;
	}
	
	/**
	 * 对目标url发起GET请求并将读取到的内容放入字符串中返回
	 * @param url 目标url
	 * @return 发起GET请求后得到的返回信息
	 * @throws IOException
	 */
	private static String doConnection(URL url) throws IOException
	{
		String s = null;

		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		s = rd.readLine();
		rd.close();
		con.disconnect();
		
		return s;
	}
	
	/**
	 * 从API:/wm/core/switch/all/features/json返回的信息中解析出交换机dpid到其端口列表的映射并存入表中
	 * @param f 调用API后返回的JSONObject
	 * @throws JSONException
	 */
	private static void parseFeatures(JSONObject f) throws JSONException
	{
		@SuppressWarnings("unchecked")
		Iterator<String> it = f.keys();
		while(it.hasNext())
		{
			ArrayList<Integer> portList = new ArrayList<Integer>();
			String key = it.next().toString();
			for(int i=0;i<f.getJSONObject(key).getJSONArray("ports").length();i++)
			{
				portList.add(f.getJSONObject(key).getJSONArray("ports").getJSONObject(i).getInt("portNumber"));
			}
			swPort.put(key, portList);
		}
	}
	
	/**
	 * 从API:/wm/topology/links/json返回的信息中解析出链路信息并将链路信息put到链路集合中
	 * @param l 调用API后返回的JSONArray
	 * @throws JSONException
	 */
	private static void parseLinks(JSONArray l) throws JSONException
	{
		String s = null;
		int srcbw=0,dstbw=0,linkbw=0;
		for(int i=0;i<l.length();i++)
		{
			JSONObject j = l.getJSONObject(i);
			srcbw = toBandwidth(getCurrentFeatures(j.getString("src-switch"),j.getInt("src-port")));
			dstbw = toBandwidth(getCurrentFeatures(j.getString("dst-switch"),j.getInt("dst-port")));
			linkbw = (srcbw >= dstbw ? dstbw : srcbw);

			s = "{\"left\":\""+j.getString("src-switch")+"\",\"left-port\":"+j.getInt("src-port")+",\"right\":\""+j.getString("dst-switch")+"\",\"right-port\":"+j.getInt("dst-port")+",\"bandwidth\":"+linkbw+"}"; 
			JSONObject element = new JSONObject(s);
			link.put(element);
			swSet.add(j.getString("src-switch"));
			swSet.add(j.getString("dst-switch"));
		}
	}
	
	/**
	 * 从API:/wm/device/返回的信息中解析出链路信息并将链路信息put到链路集合中
	 * @param d 调用API后返回的JSONArray
	 * @throws JSONException
	 */
	private static void parseDevice(JSONArray d) throws JSONException
	{
		String s = null;
		int linkbw=0;
		for(int i=0;i<d.length();i++)
		{
			if(d.getJSONObject(i).getJSONArray("attachmentPoint").length() > 0 && d.getJSONObject(i).getJSONArray("ipv4").length() > 0)
			{
				JSONObject j = d.getJSONObject(i).getJSONArray("attachmentPoint").getJSONObject(0);
				linkbw = toBandwidth(getCurrentFeatures(j.getString("switchDPID"),j.getInt("port")));
				s = "{\"left\":\""+d.getJSONObject(i).getJSONArray("ipv4").getString(0)+"\",\"left-port\":"+-1+",\"right\":\""+d.getJSONObject(i).getJSONArray("attachmentPoint").getJSONObject(0).getString("switchDPID")+"\",\"right-port\":"+d.getJSONObject(i).getJSONArray("attachmentPoint").getJSONObject(0).getInt("port")+",\"bandwidth\":"+linkbw+"}";
				JSONObject element = new JSONObject(s);
				link.put(element);
				hostSet.add(d.getJSONObject(i).getJSONArray("ipv4").getString(0));
			}
		}
	}
	
	/**
	 * 查找给定交换机上给定端口的带宽值并返回
	 * @param sw 指定交换机
	 * @param portNum 指定端口号
	 * @return 带宽值（未经转换）
	 * @throws JSONException
	 */
	private static int getCurrentFeatures(String sw,int portNum) throws JSONException
	{
		JSONArray ports = fjo.getJSONObject(sw).getJSONArray("ports");
		int currentFeatures = 0;
		for(int i=0;i<ports.length();i++)
		{
			if(ports.getJSONObject(i).getInt("portNumber") == portNum)
			{
				currentFeatures = ports.getJSONObject(i).getInt("currentFeatures");
			}
		}
		return currentFeatures;
	}
	
	/**
	 * 将未经转换的带宽值转换为以Mbps为单位的带宽值并返回
	 * @param currentFeatures 未经转换的带宽值
	 * @return 带宽值
	 */
	private static int toBandwidth(int currentFeatures)
	{
		int tmp = currentFeatures & 0x7f;
		int bw = 0;
		switch(tmp)
		{
		case 1:
			bw = 10;//10Mbps
			break;
		case 2:
			bw = 10;//10Mbps FDX
			break;
		case 4:
			bw = 100;//100Mbps
			break;
		case 8:
			bw = 100;//100Mbps FDX
			break;
		case 16:
			bw = 1024;//1Gbps
			break;
		case 32:
			bw = 1024;//1Gbps FDX
			break;
		case 64:
			bw = 10240;//10Gbps FDX
			break;
		}
		return bw;
	}
	
	/**
	 * 统计当前拓扑中的结点个数（未获取到IP地址的Host不计入结点个数中，但孤立的Switch会被统计在内）
	 * @throws JSONException
	 */
	private static void countNodes() throws JSONException
	{
		int hosts=0,switchs=0,all=0;
		//遍历device数组，未分配到IP地址的host（即挂载点为空的）不计入设备总数中
		for(int i=0;i<dja.length();i++)
		{
			if(dja.getJSONObject(i).getJSONArray("attachmentPoint").length() > 0 && dja.getJSONObject(i).getJSONArray("ipv4").length() > 0)
			{
				hosts++;
			}
		}
		
		switchs = fjo.length();
		all = hosts + switchs;
		nodes = "\"nodes\":"+all;
	}
	
	/**
	 * 将各部分信息拼装成表示拓扑信息的字符串
	 */
	private static void buildString()
	{
		topostring = "{\"links\":"+link+','+nodes+"}";
	}
	
	/**
	 * 将包含拓扑信息的字符串格式化为JSONObject
	 * @throws JSONException
	 */
	private static void buildJSON() throws JSONException
	{
		topojson = new JSONObject(topostring);
	}
	
	/**
	 * 以字符串方式返回拓扑信息（仍为JSON格式）
	 * @return
	 */
	public String getTopoString()
	{
		return topostring;
	}
	
	/**
	 * 用于测试
	 * @param args
	 * @throws JSONException
	 * @throws IOException
	 */
	public static void main(String[] args) throws JSONException, IOException
	{
		updateTopo();
		logger.debug("host Set: " + hostSet.toString());
		logger.debug("switch Set: " + swSet.toString());
		logger.debug("switch map to it's ports: " + swPort.toString());
		logger.debug("current topo: " + topojson);
	}
	
}
