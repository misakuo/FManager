/**
 * 
 */
package net.sodaless.fmanager.util;

/**
 * @author Misaku
 *
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
	
	
	public Language()
	{
		LanguageType = "ZH-CN";
	}
	
	public Language(String lang)
	{
		LanguageType = lang;
	}
	
	public String getLanguageType()
	{
		return LanguageType;
	}
	
	protected void setLanguageType(String lang)
	{
		LanguageType = lang;
	}
	
	
	
}
