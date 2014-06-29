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
 * ������Floodlight��REST API��������ȡ��Ϣ��Ϊ����ģ���ṩ��������
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
	private static JSONObject fjo = null;//����API features��ȡ������Ϣ
	private static JSONArray lja = null,dja = null;//�ֱ𱣴�API links��device��ȡ������Ϣ
	
	private static JSONArray link = new JSONArray();//topojson��links��value����
	private static String nodes = null;//topojson�е�nodes����
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
	 * ���ÿ�����IP��ַ
	 * @param usrip
	 */
	public static void setControllerIP(String usrip)
	{
		ip = usrip;
	}
	
	/**
	 * ����REST����󶨶˿�
	 * @param usrport
	 */
	public static void setRestServicesPort(String usrport)
	{
		port = usrport;
	}
	
	/**
	 * ����һ����ʾ��ǰ����������Ϣ��JSONObject
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
	 * ��ȡ������IP��ַ��Ĭ��Ϊ127.0.0.1��
	 * @return
	 */
	public static String getControllerIP()
	{
		return ip;
	}
	
	/**
	 * ��ȡ������REST����󶨵Ķ˿ںţ�Ĭ��Ϊ8080��
	 * @return
	 */
	public static String getRestServersPort()
	{
		return port;
	}
	
	/**
	 * ����������Ϣ
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
	 * ��ȡ��ǰ����������Host��IP��ַ�ļ���
	 * @return
	 */
	public static HashSet<String> getHostSet()
	{
		return hostSet;
	}
	
	/**
	 * ��ȡ��ǰ���������н�������DPID�ļ���
	 * @return
	 */
	public static HashSet<String> getSWSet()
	{
		return swSet;
	}
	
	/**
	 * ��ȡ��ǰ���������н���������DPID����˿ں��б��ӳ��
	 * @return
	 */
	public static HashMap<String,ArrayList<Integer>> getSWPortSet()
	{
		return swPort;
	}
	
	/**
	 * ��Ŀ��url����GET���󲢽���ȡ�������ݷ����ַ����з���
	 * @param url Ŀ��url
	 * @return ����GET�����õ��ķ�����Ϣ
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
	 * ��API:/wm/core/switch/all/features/json���ص���Ϣ�н�����������dpid����˿��б��ӳ�䲢�������
	 * @param f ����API�󷵻ص�JSONObject
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
	 * ��API:/wm/topology/links/json���ص���Ϣ�н�������·��Ϣ������·��Ϣput����·������
	 * @param l ����API�󷵻ص�JSONArray
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
	 * ��API:/wm/device/���ص���Ϣ�н�������·��Ϣ������·��Ϣput����·������
	 * @param d ����API�󷵻ص�JSONArray
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
	 * ���Ҹ����������ϸ����˿ڵĴ���ֵ������
	 * @param sw ָ��������
	 * @param portNum ָ���˿ں�
	 * @return ����ֵ��δ��ת����
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
	 * ��δ��ת���Ĵ���ֵת��Ϊ��MbpsΪ��λ�Ĵ���ֵ������
	 * @param currentFeatures δ��ת���Ĵ���ֵ
	 * @return ����ֵ
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
	 * ͳ�Ƶ�ǰ�����еĽ�������δ��ȡ��IP��ַ��Host������������У���������Switch�ᱻͳ�����ڣ�
	 * @throws JSONException
	 */
	private static void countNodes() throws JSONException
	{
		int hosts=0,switchs=0,all=0;
		//����device���飬δ���䵽IP��ַ��host�������ص�Ϊ�յģ��������豸������
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
	 * ����������Ϣƴװ�ɱ�ʾ������Ϣ���ַ���
	 */
	private static void buildString()
	{
		topostring = "{\"links\":"+link+','+nodes+"}";
	}
	
	/**
	 * ������������Ϣ���ַ�����ʽ��ΪJSONObject
	 * @throws JSONException
	 */
	private static void buildJSON() throws JSONException
	{
		topojson = new JSONObject(topostring);
	}
	
	/**
	 * ���ַ�����ʽ����������Ϣ����ΪJSON��ʽ��
	 * @return
	 */
	public String getTopoString()
	{
		return topostring;
	}
	
	/**
	 * ���ڲ���
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
