package net.sodaless.fmanager.util;

import java.awt.Cursor;  
import java.awt.FlowLayout;  
import java.awt.event.MouseAdapter;  
import java.awt.event.MouseEvent;  
import java.io.IOException;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
  
/** 
 * �����Ӱ�ť�� 
 *  
 * @author Elvis 
 */  
public class LinkButton extends JLabel {  
 private static final long serialVersionUID = 1L;  
 private String text;    
 private Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);  //����һ�����Ϊ��ָ���͵����
  
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
  
   //��������ǩʱ,�����ı���ʽ�¼�
   public void mouseEntered(MouseEvent e) {  
    setHandCursor(); 
    setMoveInText(); 
   }  
     
   //����Ƴ���ǩʱ,�����ı���ʽ�¼�
   public void mouseExited(MouseEvent e) {  
    setDefaultCursor();
    setMoveOutText();  
   }  
  });  
 }  
  
 //���ó�ʼ��ʽ
 public void setText(String text) {  
  String content = "<html><font color=blue>" + text  + "</font></html>";  
  this.text = text;  
  super.setText(content);  
 }  

 //������굥����ʽ  
 private void setClickedText() {  
  String content = "<html><font color=green><u>" + text  + "</u></font></html>";  
  super.setText(content);  
 }  

//�������������ʽ
private void setMoveInText(){
String content="<html><font color=red><u>"+text+"</u></font></html>";
super.setText(content);
}

//��������Ƴ���ʽ
private void setMoveOutText(){
String content="<html><font color=blue>"+text+"</font></html>";
super.setText(content);
}
 
 //���ù��Ϊ��ָ
 private void setHandCursor() {  
  this.setCursor(handCursor);  
 }  
 //���ù��ΪĬ��
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