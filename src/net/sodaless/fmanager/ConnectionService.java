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
 * ͨ�ŷ����࣬�ṩHTTP GET/POST/DELETE�����ݸ�ʽ������
 * @author Misaku
 * @date 2014-06-17
 */
public class ConnectionService {

	private static boolean ConnectionSuccess = false;
	
	/**
	 * ��ʼ�����ӳɹ�״̬Ϊfalse
	 */
	private static void initConnectionSuccess()
	{
		setConnectionSuccess(false);
	}
	
	/**
	 * �ж������Ƿ�ɹ�
	 * @return connectionSuccess
	 */
	public static boolean isConnectionSuccess() {
		return ConnectionSuccess;
	}

	/**
	 * ���ñ�־״̬
	 * @param connectionSuccess Ҫ���õ� connectionSuccess
	 */
	private static void setConnectionSuccess(boolean connectionSuccess) {
		ConnectionSuccess = connectionSuccess;
	}
	
	/**
	 * ��������IP��port��apiƴװ��url�ַ���
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
	 * �����address��ַ����GET���󲢶�ȡ����ֵ
	 * @param address url��ַ
	 * @return ����ֵ
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
	 * �����address��ַ����POST����д�����ݲ���ȡ����ֵ
	 * @param address url��ַ
	 * @param param Ҫд�������
	 * @return ����ֵ
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
	 * �����address��ַ����DELETE����д�����ݲ���ȡ����ֵ
	 * @param address url��ַ
	 * @param param Ҫд�������
	 * @return ����ֵ
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
	 * ���ַ���ת��ΪJSONObject
	 * @param s ��ת�����ַ���
	 * @return ת�����JSONObject
	 * @throws JSONException
	 */
	public static JSONObject toJSONObject(String s) throws JSONException
	{
		JSONObject j = new JSONObject(s);
		return j;
	}
	
	/**
	 * ���ַ���ת��ΪJSONArray
	 * @param s ��ת�����ַ���
	 * @return ת�����JSONArray
	 * @throws JSONException
	 */
	public static JSONArray toJSONArray(String s) throws JSONException
	{
		JSONArray j = new JSONArray(s);
		return j;
	}
	
}
