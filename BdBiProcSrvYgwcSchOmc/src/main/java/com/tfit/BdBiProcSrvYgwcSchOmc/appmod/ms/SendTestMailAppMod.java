package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.ms;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdMailSrvDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTHttpRspVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ms.SendTestMailDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.SendMailAcceUtils;

//发送测试邮件应用模型
public class SendTestMailAppMod {
	private static final Logger logger = LogManager.getLogger(SendTestMailAppMod.class.getName());
	
	@Autowired
	ObjectMapper objectMapper = new ObjectMapper();
	
	//发送邮件方法
	int sendMailMethod = 1;
	
	//发送邮件
	private boolean sendMail(TEduBdMailSrvDo tebmsDo, String mailTitle, String mailCont, boolean isShowDebug) {
		boolean retFlag = false;
        Properties properties = new Properties();
        String strInfo = "", strVal = "";
        properties.put("mail.transport.protocol", "smtp");                                // 连接协议
        strVal = tebmsDo.getSendServer();
        strInfo += ("发件主机名：" + strVal + "\n");
        properties.put("mail.smtp.host", strVal);                                         // 主机名
        strVal = String.valueOf(tebmsDo.getSendSrvPortNo());
        strInfo += ("端口号：" + strVal + "\n");
        properties.put("mail.smtp.port", strVal);                                         // 端口号
        properties.put("mail.smtp.auth", "true");
        if(tebmsDo.getSendSrvPort() != null) {
        	if(tebmsDo.getSendSrvPort() == 0) {
        		strInfo += ("端口：" + "SSL" + "\n");
        		properties.put("mail.smtp.ssl.enable", "true");                           // 设置是否使用ssl安全连接 ---一般都使用
        	}
        	else if(tebmsDo.getSendSrvPort() == 1) {
        		strInfo += ("端口：" + "STARTTLS" + "\n");
        		properties.put("mail.smtp.starttls.enable", "true");                      // 设置是否使用starttls安全连接 ---一般都使用
        	}
        }
        else
        	properties.put("mail.smtp.ssl.enable", "true");                               // 设置是否使用ssl安全连接 ---一般都使用
        if(isShowDebug)
        	properties.put("mail.debug", "true");                                         // 设置是否显示debug信息 true 会在控制台显示相关信息
        logger.info(strInfo);
        // 得到回话对象
        Session session = Session.getInstance(properties);
        // 获取邮件对象
        Message message = new MimeMessage(session);
        // 设置发件人邮箱地址
        try {
			message.setFrom(new InternetAddress(tebmsDo.getEmail()));
			// 设置收件人邮箱地址 
	        //message.setRecipients(Message.RecipientType.TO, new InternetAddress[]{new InternetAddress(tebmsDo.getEmail()), new InternetAddress("xxx@qq.com"), new InternetAddress("xxx@qq.com")});   //多个收件人
	        message.setRecipient(Message.RecipientType.TO, new InternetAddress(tebmsDo.getEmail()));//一个收件人
	        // 设置邮件标题
	        message.setSubject(mailTitle);
	        // 设置邮件内容
	        message.setText(mailCont);
	        // 得到邮差对象
	        Transport transport = null;
			try {
				transport = session.getTransport();
				// 连接自己的邮箱账户
		        try {
		        	transport.connect(tebmsDo.getEmail(), tebmsDo.getPassword());
		        	// 发送邮件
			        transport.sendMessage(message, message.getAllRecipients());
			        transport.close();
			        retFlag = true;
				} catch (MessagingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}// 密码为QQ邮箱开通的stmp服务后得到的客户端授权码
			} catch (NoSuchProviderException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}
		} catch (AddressException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}        
        
        return retFlag;
    }
	
	//添加账号应用模型函数
  	public IOTHttpRspVO appModFunc(String token, String strBodyCont, Db1Service db1Service, Db2Service db2Service, int[] codes) {
  		IOTHttpRspVO normResp = null;
  		//按参数形式处理
  		if(token != null && db2Service != null) {
  			SendTestMailDTO stmDto = null;
  			String mailCont = null;
  			//将json子串转成对象
  			try {
  				if(strBodyCont != null) {
  					if(!strBodyCont.isEmpty()) {
  						stmDto = objectMapper.readValue(strBodyCont, SendTestMailDTO.class);
  						if(stmDto != null)
  							mailCont = stmDto.getMailCont();
  					}
  				}
			} catch (JsonParseException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} catch (IOException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			} 
  			if(mailCont == null)
				mailCont = "您好，这是一封测试邮件！";
  			if(mailCont != null) {  	    	   
  				//从数据源ds1的数据表t_edu_bd_user中查找用户信息以授权码token
  	  			TEduBdUserDo tebuDo = db2Service.getBdUserInfoByToken(token);
  	  			if(tebuDo.getId() != null) {
  	  				//查询邮件服务记录以用户名
  	  				TEduBdMailSrvDo tebmsDo = db2Service.getMailSrvInfoByUserName(tebuDo.getUserAccount());
  	  				if(tebmsDo != null) {
  	  					boolean flag = false;
  	  					//发送邮件
  	  					if(sendMailMethod == 0)
  	  						flag = sendMail(tebmsDo, mailCont, mailCont, true);
  	  					else if(sendMailMethod == 1) {
  	  						String[] amFileNames = new String[2], amOutNames = new String[2], rcvMailUsers = new String[2];
  	  						amFileNames[0] = SpringConfig.tomcatSrvDirs[0] + "/" + "tomcat.png";
  	  						amOutNames[0] = "附件1.png";
  	  						amFileNames[1] = SpringConfig.tomcatSrvDirs[0] + "/" + "index.jsp";
  	  						amOutNames[1] = "附件2.jsp";
  	  						rcvMailUsers[0] = tebmsDo.getEmail();
  	  						rcvMailUsers[1] = "185601074@qq.com";
  	  						flag = SendMailAcceUtils.sendMail(tebmsDo, mailCont, mailCont, amFileNames, amOutNames, rcvMailUsers, false);
  	  					}
  	  					else
  	  						flag = sendMail(tebmsDo, mailCont, mailCont, true);
  	  				    if(flag) {
  	  				    	normResp = AppModConfig.getNormalResp(null);
  	  				    	logger.info("邮件发送成功！");
  	  				    }
  	  				    else {
  	  				    	codes[0] = 2037;
  	  				    	logger.info("邮件发送失败！");
  	  				    }
  	  				}
  	  				else {
  	  					codes[0] = 2013;
  	  					logger.info("查询数据记录失败！");
  	  				}
  	  			}
  	  			else {
  	  				codes[0] = 2013;
  	  				logger.info("查询数据记录失败！");
  	  			}
  			}
  		}
  		else {
  			codes[0] = 2017;
  			logger.info("访问接口参数非法！");
  		}
  		
  		return normResp;
  	}
}
