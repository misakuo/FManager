/**
 * 
 */
package net.sodaless.fmanager.util;

/**
 * 语言类的父类
 * @author Misaku
 */
public class Language {
	
	private String LanguageType = "";
	public String VTSrefresh = "";
	public String initCheckBoxText = "";
	public String initFrameTitle = "";
	public String initLableText = "";
	public String initOnlineButtonText = "";
	public String initOfflineButtonText = "";
	public String VTFrameTitle = "";
	public String VTaddHost = "";
	public String VTaddSwitch = "";
	public String VTaddLink = "";
	public String VTfinish = "";
	public String VTfile = "";
	public String VTsave = "";
	public String VTclear = "";
	public String VTremove = "";
	public String MWTitle = "";
	public String MWpm = "";
	public String MWfm = "";
	public String MWfw = "";
	public String MWlb = "";
	public String MWqos = "";
	public String MWtm = "";
	public String PMTitle = "";
	public String PMloadalog = "";
	public String PMcalc = "";
	public String PMpush = "";
	public String PMautopush = "";
	public String PMsymmetricpath = "";
	public String PMsrc = "";
	public String PMdst = "";
	public String PMbw = "";
	public String PMcommit = "";
	public String PMadd = "";
	public String PMreset = "";
	
	/**
	 * 默认构造器，初始化语言为中文
	 */
	public Language()
	{
		LanguageType = "ZH-CN";
	}
	
	/**
	 * 构造器，设置语言类型为lang
	 * @param lang
	 */
	public Language(String lang)
	{
		LanguageType = lang;
	}
	
	/**
	 * 获取当前语言类型
	 * @return
	 */
	public String getLanguageType()
	{
		return LanguageType;
	}
	
	protected void setLanguageType(String lang)
	{
		LanguageType = lang;
	}
	
	
	
}
