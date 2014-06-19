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
 * @author Misaku
 *
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
	 * @param args
	 * @throws IOException 
	 * @throws JSONException 
	 */
	public static void main(String[] args) throws JSONException, IOException {
		// TODO 自动生成的方法存根
		ModeSelection.start();
	}

}
