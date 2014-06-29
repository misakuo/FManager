/**
 * 
 */
package net.sodaless.fmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;

import net.sodaless.fmanager.util.*;

/**
 * 程序入口类
 * @author Misaku
 */
public class Main {
	
	public static boolean online = true;
	public static Language lang;
	public static String ip;
	public static String port;
	public static JSONObject currentTopo = new JSONObject();
	public static HashSet<String> hostSet = new HashSet<String>();
	public static HashSet<String> swSet = new HashSet<String>();
	public static HashMap<String,ArrayList<Integer>> swPort = new HashMap<String,ArrayList<Integer>>();
	
	/**
	 * 选择语言类型(ZH_CN/EN_US)
	 * @param zh_cn
	 */
	public static void selectLanguage(boolean zh_cn)
	{
		if(zh_cn == true)
		{
			lang = new LanguageZH_CN();
		}
		else
		{
			lang = new LanguageEN_US();
		}
	}

	/**
	 * 程序的main方法
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws JSONException, IOException {

		ModeSelection.start();
	}

}
