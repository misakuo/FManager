package net.sodaless.fmanager.util;

import java.awt.Cursor;  
import java.awt.FlowLayout;  
import java.awt.event.MouseAdapter;  
import java.awt.event.MouseEvent;  
import java.io.IOException;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
  
/** 
 * 超链接按钮。 
 *  
 * @author Elvis 
 */  
public class LinkButton extends JLabel {  
 private static final long serialVersionUID = 1L;  
 private String text;    
 private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);  //建立一个光标为手指类型的鼠标
  
 public LinkButton(final String url) {  
  addMouseListener(new MouseAdapter() {  
   public void mouseClicked(MouseEvent e) {  
    Runtime rt = Runtime.getRuntime();  
    try {  
     String cmd = "rundll32 url.dll,FileProtocolHandler " + url;  
     rt.exec(cmd);  
    } catch (IOException e1) {  
     e1.printStackTrace();  
    }  
    setClickedText();  
   }  
  
   //鼠标移入标签时,设置文本样式事件
   public void mouseEntered(MouseEvent e) {  
    setHandCursor(); 
    setMoveInText(); 
   }  
     
   //鼠标移出标签时,设置文本样式事件
   public void mouseExited(MouseEvent e) {  
    setDefaultCursor();
    setMoveOutText();  
   }  
  });  
 }  
  
 //设置初始样式
 public void setText(String text) {  
  String content = "<html><font color=blue>" + text  + "</font></html>";  
  this.text = text;  
  super.setText(content);  
 }  

 //设置鼠标单击样式  
 private void setClickedText() {  
  String content = "<html><font color=green><u>" + text  + "</u></font></html>";  
  super.setText(content);  
 }  

//设置鼠标移入样式
private void setMoveInText(){
String content="<html><font color=red><u>"+text+"</u></font></html>";
super.setText(content);
}

//设置鼠标移出样式
private void setMoveOutText(){
String content="<html><font color=blue>"+text+"</font></html>";
super.setText(content);
}
 
 //设置光标为手指
 private void setHandCursor() {  
  this.setCursor(handCursor);  
 }  
 //设置光标为默认
 private void setDefaultCursor() {  
  this.setCursor(null);  
 }  
 public static void main(String[] args) {  
  JFrame f = new JFrame();  
  f.setSize(400, 400);  
  f.setLayout(new FlowLayout());  
  LinkButton btn = new LinkButton("http://www.baidu.com");  
  btn.setText("BAIDU");  
  f.add(btn);  
  f.setVisible(true);  
  f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
 }  
}  