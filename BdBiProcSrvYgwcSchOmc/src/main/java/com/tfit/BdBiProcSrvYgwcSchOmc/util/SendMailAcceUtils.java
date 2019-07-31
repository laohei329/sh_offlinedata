package com.tfit.BdBiProcSrvYgwcSchOmc.util;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.mail.util.MailSSLSocketFactory;
import com.tfit.BdBiProcSrvYgwcSchOmc.appmod.ms.SendTestMailAppMod;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdMailSrvDo;

/**
 * 发送邮件工具类
 * 
 * @author tp
 *
 */
public class SendMailAcceUtils {
	private static final Logger logger = LogManager.getLogger(SendTestMailAppMod.class.getName());
	
	/**
     * 发送带附件的邮件
     * 
     * @param receive
     *            收件人
     * @param subject
     *            邮件主题
     * @param msg
     *            邮件内容
     * @param filename
     *            附件地址
     * @return
     * @throws GeneralSecurityException
     */
    public static boolean sendMail(TEduBdMailSrvDo tebmsDo, String mailTitle, String mailCont, String[] amFileNames, String[] amOutNames, String[] rcvMailUsers, boolean isShowDebug) {
        boolean retFlag = false;
        // 发件人电子邮箱
        final String from = tebmsDo.getEmail();
        // 发件人电子邮箱密码
        final String pass = tebmsDo.getPassword();
        // 指定发送邮件的主机为 smtp.qq.com
        String host = tebmsDo.getSendServer();                   // 邮件服务器
        // 获取系统属性
        Properties properties = System.getProperties();
        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", host);
        //设置端口号
        properties.put("mail.smtp.port", String.valueOf(tebmsDo.getSendSrvPortNo()));                                         // 端口号
        //设置授权使能
        properties.put("mail.smtp.auth", "true");
        MailSSLSocketFactory sf = null;
		try {
			sf = new MailSSLSocketFactory();
		} catch (GeneralSecurityException e1) {
			// TODO 自动生成的 catch 块
			e1.printStackTrace();
		}
		if(sf == null)
			return retFlag;
        sf.setTrustAllHosts(true);
        //设置加密方式
        if(tebmsDo.getSendSrvPort() != null) {
        	if(tebmsDo.getSendSrvPort() == 0) {
        		properties.put("mail.smtp.ssl.enable", "true");                           // 设置是否使用ssl安全连接 ---一般都使用
        		properties.put("mail.smtp.ssl.socketFactory", sf);
        	}
        	else if(tebmsDo.getSendSrvPort() == 1) {
        		properties.put("mail.smtp.starttls.enable", "true");                      // 设置是否使用starttls安全连接 ---一般都使用
        		properties.put("mail.smtp.starttls.socketFactory", sf);
        	}
        }
        else {
        	properties.put("mail.smtp.ssl.enable", "true");     
        	properties.put("mail.smtp.ssl.socketFactory", sf);
        }        
        if(isShowDebug)
        	properties.put("mail.debug", "true");
        // 获取默认session对象
        Session session = Session.getDefaultInstance(properties, new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {        // qq邮箱服务器账户、第三方登录授权码
                return new PasswordAuthentication(from, pass);                 // 发件人邮件用户名、密码
            }
        });
        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(session);
            // Set From: 头部头字段
            message.setFrom(new InternetAddress(from));
            // Set To: 头部头字段
            // 设置收件人邮箱地址 
	        if(rcvMailUsers == null)
	        	message.setRecipient(Message.RecipientType.TO, new InternetAddress(tebmsDo.getEmail()));   //一个收件人
	        else {
	        	if(rcvMailUsers.length > 0) {
	        		InternetAddress[] ias = new InternetAddress[rcvMailUsers.length];
	        		for(int i = 0; i < rcvMailUsers.length; i++) {
	        			ias[i] = new InternetAddress(rcvMailUsers[i]);
	        		}
	        		message.setRecipients(Message.RecipientType.TO, ias);    //多个收件人
	        	}
	        	else
	        		message.setRecipient(Message.RecipientType.TO, new InternetAddress(tebmsDo.getEmail()));   //一个收件人
	        }
            // Set Subject: 主题文字
	        if(mailTitle != null)
	        	message.setSubject(mailTitle);
            // 创建消息部分
            BodyPart messageBodyPart = new MimeBodyPart();
            // 消息
            if(mailCont == null)
            	mailCont = "";
            messageBodyPart.setText(mailCont);
            // 创建多重消息
            Multipart multipart = new MimeMultipart();
            // 设置文本消息部分
            multipart.addBodyPart(messageBodyPart);            
            //发送附件部分
            if(amFileNames != null) {
            	if(amFileNames.length > 0) {
            		for(int i = 0; i < amFileNames.length; i++) {
            			// 附件部分
            			messageBodyPart = new MimeBodyPart();
            			// 设置要发送附件的文件路径
            			DataSource source = new FileDataSource(amFileNames[i]);
            			messageBodyPart.setDataHandler(new DataHandler(source));
            			// 处理附件名称中文（附带文件路径）乱码问题
            			int idx = amFileNames[i].lastIndexOf("/");
            			String fileName = amFileNames[i];
            			if(amOutNames == null) {
            				if(idx != -1) {
            					fileName = amFileNames[i].substring(idx+1, amFileNames[i].length());
            					
            				}
            			}
            			else {
            				if(amOutNames.length == amFileNames.length) {
            					fileName = amOutNames[i];            					
            				}
            				else {
            					if(idx != -1) {
                					fileName = amFileNames[i].substring(idx+1, amFileNames[i].length());                					
                				}
            				}
            			}
            			logger.info("发送附件文件" + (i+1) + "：" + fileName);
            			messageBodyPart.setFileName(MimeUtility.encodeText(fileName));
            			multipart.addBodyPart(messageBodyPart);
            		}
            	}
            }
            // 发送完整消息
            message.setContent(multipart);
            // 发送消息
            Transport.send(message);
            retFlag = true;
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        return retFlag;
    }
}