/**
 * 
 */
package net.sodaless.fmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 通信服务类，提供HTTP GET/POST/DELETE和数据格式化服务
 * @author Misaku
 * @date 2014-06-17
 */
public class ConnectionService {

	private static boolean ConnectionSuccess = false;
	
	/**
	 * 初始化连接成功状态为false
	 */
	private static void initConnectionSuccess()
	{
		setConnectionSuccess(false);
	}
	
	/**
	 * 判断连接是否成功
	 * @return connectionSuccess
	 */
	public static boolean isConnectionSuccess() {
		return ConnectionSuccess;
	}

	/**
	 * 设置标志状态
	 * @param connectionSuccess 要设置的 connectionSuccess
	 */
	private static void setConnectionSuccess(boolean connectionSuccess) {
		ConnectionSuccess = connectionSuccess;
	}
	
	/**
	 * 将给定的IP、port、api拼装成url字符串
	 * @param ip
	 * @param port
	 * @param api
	 * @return
	 */
	public static String addressBuilder(String ip,String port,String api)
	{
		String address = "";
		address = "http://" + ip + ":" + port + api;
		
		return address;
	}
	
	/**
	 * 向给定address地址发起GET请求并读取返回值
	 * @param address url地址
	 * @return 返回值
	 * @throws IOException
	 */
	public static String doGet(String address) throws IOException
	{
		initConnectionSuccess();
		String reply = "";
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.connect();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		reply = rd.readLine();
		conn.disconnect();
		setConnectionSuccess(true);
		return reply;
	}
	
	/**
	 * 向给定address地址发起POST请求，写入数据并读取返回值
	 * @param address url地址
	 * @param param 要写入的数据
	 * @return 返回值
	 * @throws IOException
	 */
	public static String doPost(String address,String param) throws IOException
	{
		initConnectionSuccess();
		String reply = "";
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(param);
		wr.flush();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		reply = rd.readLine();
		wr.close();
		rd.close();
		conn.disconnect();
		setConnectionSuccess(true);
		
		return reply;
	}
	
	/**
	 * 向给定address地址发起DELETE请求，写入数据并读取返回值
	 * @param address url地址
	 * @param param 要写入的数据
	 * @return 返回值
	 * @throws IOException
	 */
	public static String doDelete(String address,String param) throws IOException
	{
		initConnectionSuccess();
		String reply = "";
		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("DELETE");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		wr.write(param);
		wr.flush();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		reply = rd.readLine();
		wr.close();
		rd.close();
		conn.disconnect();
		setConnectionSuccess(true);
		
		return reply;
	}

	/**
	 * 将字符串转换为JSONObject
	 * @param s 待转换的字符串
	 * @return 转换后的JSONObject
	 * @throws JSONException
	 */
	public static JSONObject toJSONObject(String s) throws JSONException
	{
		JSONObject j = new JSONObject(s);
		return j;
	}
	
	/**
	 * 将字符串转换为JSONArray
	 * @param s 待转换的字符串
	 * @return 转换后的JSONArray
	 * @throws JSONException
	 */
	public static JSONArray toJSONArray(String s) throws JSONException
	{
		JSONArray j = new JSONArray(s);
		return j;
	}
	
}
