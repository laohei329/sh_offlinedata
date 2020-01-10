package com.tfit.BdBiProcSrvYgwcSchOmc.util;

import com.alibaba.fastjson.JSONObject;
import com.tfit.BdBiProcSrvYgwcSchOmc.appmod.export.ExportPdfMod;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.ApplicationUtil;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FtpUtil {
	
	private static final Logger logger = LogManager.getLogger(ExportPdfMod.class.getName());
	

	/**
	 * 文件
	 * @param pathFileName :文件地址
	 * @param os ：
	 * @param repFileResPath ：文件二级地址
	 */
	public static void ftpServer(String pathFileName, ByteArrayOutputStream os,String repFileResPath) {
		if(repFileResPath == null || "".equals(repFileResPath)) {
			repFileResPath = pathFileName.substring(0,pathFileName.lastIndexOf("/"));
			repFileResPath = repFileResPath.substring(repFileResPath.lastIndexOf("/")+1,repFileResPath.length());
		}
		logger.info("pathFileName"+pathFileName);
		logger.info("repFileResPath:"+repFileResPath);
		
		FTPClient ftp = new FTPClient();
		try {
			//获取环境参数
			Environment env = ApplicationUtil.getBean(Environment.class);
			// ftp账户信息
			String ip = env.getProperty("ftp.ip");
			String port = env.getProperty("ftp.port");
			String username = env.getProperty("ftp.username");
			String password = env.getProperty("ftp.password");

			// 连接ftp服务器
			ftp.connect(ip, Integer.parseInt(port));
			// 登录
			ftp.login(username, password);
			
			//编码
			ftp.setControlEncoding("UTF-8");
			//启动被动模式的
			ftp.enterLocalPassiveMode();
			
			// 设置上传路径
			//String path = "/data/vsftpd/ftp-user1"+ repFileResPath;
			String path = repFileResPath.substring(0,repFileResPath.length()-1);
			if(repFileResPath.lastIndexOf("/") <= 0) {
				path = repFileResPath.substring(0,repFileResPath.length());
			}
			logger.info("path:"+path);
			// 检查上传路径是否存在 如果不存在返回false
			boolean flag = ftp.changeWorkingDirectory(path);
			logger.info("flag:"+flag);
			if (!flag) {
				// 创建上传的路径 该方法只能创建一级目录，在这里如果/home/ftpuser存在则可创建image
				boolean isMakeDirectory = ftp.makeDirectory(path);
				logger.info("isMakeDirectory:"+isMakeDirectory);
			}
			// 指定上传路径
			ftp.changeWorkingDirectory(path);
			// 指定上传文件的类型 二进制文件
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			
			byte[] b = os.toByteArray();
		    ByteArrayInputStream in = new ByteArrayInputStream(b);
		    pathFileName = pathFileName.substring(pathFileName.lastIndexOf("/")+1);
		    String name = new String(pathFileName.getBytes("GBK"), "iso-8859-1");//涉及到中文问题 根据系统实际编码改变 
		    logger.info("name:"+name);
		    logger.info("pathFileName:"+pathFileName);
		    logger.info("in:"+ JSONObject.toJSONString(in));
		    boolean bResult = ftp.storeFile(name, in);
		    if(bResult) {
		    	 logger.info("文件"+pathFileName+"推送成功！");
		    }else {
		    	 logger.info("文件"+pathFileName+"推送失败！");
		    }
		} catch (Exception e) {
			 logger.info("e:"+e.getMessage());
			e.printStackTrace();
		}finally {
			try {
                //关闭文件流
                //退出
                ftp.logout();
                //断开连接
                ftp.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}
	
	/**
	 * 圖片
	 * @param pathFileName :文件地址
	 * @param os ：
	 * @param repFileResPath ：文件二级地址
	 */
	public static void ftpPicServer(String pathFileName, byte[] fileCont) {
		String repFileResPath = "";
		/*if(repFileResPath == null || "".equals(repFileResPath)) {
			repFileResPath = pathFileName;
			if(pathFileName.lastIndexOf("/") >= 0) {
				repFileResPath = pathFileName.substring(0,pathFileName.lastIndexOf("/"));
			}
			repFileResPath = repFileResPath.substring(repFileResPath.lastIndexOf("/")+1,repFileResPath.length());
		}*/
		
		if(repFileResPath == null || "".equals(repFileResPath)) {
			repFileResPath = pathFileName.substring(0,pathFileName.lastIndexOf("/"));
			repFileResPath = repFileResPath.substring(repFileResPath.lastIndexOf("/")+1,repFileResPath.length());
		}
		
		logger.info("pathFileName"+pathFileName);
		logger.info("repFileResPath:"+repFileResPath);
		logger.info("fileCont:"+fileCont.toString());
		
		FTPClient ftp = new FTPClient();
		try {
			// 连接ftp服务器
			ftp.connect("172.18.14.41", 21);
			// 登录
			ftp.login("ftp-user1", "123456");
			
			//编码
			ftp.setControlEncoding("GBK");
			//启动被动模式的
			ftp.enterLocalPassiveMode();
			
			// 设置上传路径
			//String path = "/data/vsftpd/ftp-user1"+ repFileResPath;
			String path = repFileResPath.substring(0,repFileResPath.length()-1);
			if(repFileResPath.lastIndexOf("/") <= 0) {
				path = repFileResPath.substring(0,repFileResPath.length());
			}
			logger.info("path:"+path);
			// 检查上传路径是否存在 如果不存在返回false
			boolean flag = ftp.changeWorkingDirectory(path);
			logger.info("flag:"+flag);
			if (!flag) {
				// 创建上传的路径 该方法只能创建一级目录，在这里如果/home/ftpuser存在则可创建image
				boolean isMakeDirectory = ftp.makeDirectory(path);
				logger.info("isMakeDirectory:"+isMakeDirectory);
			}
			// 指定上传路径
			ftp.changeWorkingDirectory(path);
			// 指定上传文件的类型 二进制文件
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE);
			
			String aa = "";
			for (byte b : fileCont){
				aa += b;
			}
			logger.info("aa:"+aa);
			 
		    ByteArrayInputStream in = new ByteArrayInputStream(fileCont);
		    pathFileName = pathFileName.substring(pathFileName.lastIndexOf("/")+1);
		    String name = new String(pathFileName.getBytes("GBK"), "iso-8859-1");//涉及到中文问题 根据系统实际编码改变 
		    //String name = new String(pathFileName.getBytes(),"iso-8859-1");
		    logger.info("name:"+name);
		    logger.info("pathFileName:"+pathFileName);
		    boolean bResult = ftp.storeFile(name, in);
		    if(bResult) {
		    	 logger.info("文件"+pathFileName+"推送成功！");
		    }else {
		    	 logger.info("文件"+pathFileName+"推送失败！");
		    }
		} catch (Exception e) {
			 logger.info("e:"+e.getMessage());
			e.printStackTrace();
		}finally {
			try {
                //关闭文件流
                //退出
                ftp.logout();
                //断开连接
                ftp.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
		}
	}

	/**
	 * 多個文件
	 * @param files
	 */
	public static void uploadFile(String pathFileName,MultipartFile files,String repFileResPath) {
		
		if(repFileResPath == null || "".equals(repFileResPath)) {
			repFileResPath = pathFileName.substring(0,pathFileName.lastIndexOf("/"));
			repFileResPath = repFileResPath.substring(repFileResPath.lastIndexOf("/")+1,repFileResPath.length());
		}
		logger.info("pathFileName"+pathFileName);
		logger.info("repFileResPath:"+repFileResPath);
		
		FTPClient ftpClient = new FTPClient();
		try {
			// 连接ftp服务器
			ftpClient.connect("172.18.14.41", 21);
			// 登录
			ftpClient.login("ftp-user1", "123456");
			
			//编码
			ftpClient.setControlEncoding("UTF-8");
			//启动被动模式的
			ftpClient.enterLocalPassiveMode();
			
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			
			String path = repFileResPath.substring(0,repFileResPath.length()-1);
			if(repFileResPath.lastIndexOf("/") <= 0) {
				path = repFileResPath.substring(0,repFileResPath.length());
			}
			logger.info("path:"+path);
			// 设置上传目录
			ftpClient.changeWorkingDirectory(path);
			pathFileName = pathFileName.substring(pathFileName.lastIndexOf("/")+1);
			String fileName = new String(pathFileName.getBytes(
			"utf-8"), "iso-8859-1");
			FTPFile[] fs = ftpClient.listFiles();
			if (fs != null && fs.length > 0) {
				for (int i = 0; i < fs.length; i++) {
					if (fs[i].getName().equals(fileName)) {
						ftpClient.deleteFile(fs[i].getName());
						break;
					}
				}
			}
			
			logger.info("fileName:"+fileName);
			
			OutputStream os = ftpClient.appendFileStream(fileName);
			byte[] bytes = new byte[1024];
			InputStream is = files.getInputStream();
			int c;
			// 暂未考虑中途终止的情况
			while ((c = is.read(bytes)) != -1) {
				os.write(bytes, 0, c);
			}
			os.flush();
			is.close();
			os.close();
			
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			boolean bResult = ftpClient.storeFile(fileName, in);
			logger.info("bResult:"+bResult);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * @Description: 从远程ftp服务器读取文件
	 * @Param: [fileName]
	 * @return: java.io.InputStream
	 * @Author: jianghy
	 * @Date: 2020/1/7
	 * @Time: 17:08
	 */
	public static InputStream readRemoteFile(String fileName,String ftpPath){
		logger.info("需要读取的远程的pdf的路径"+fileName);
		InputStream ins = null;
		FTPClient ftpClient = new FTPClient();
		try {
			// 连接ftp服务器
			ftpClient.connect("172.20.105.205", 21);
			// 登录
			ftpClient.login("ftp-user1", "123456");
			//编码
			ftpClient.setControlEncoding("UTF-8");
			int replyCode = ftpClient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(replyCode)) {
				logger.info("connect failed...ftp:");
			} else {
				logger.info("connect successfu...ftp:");
			}

			//启动被动模式的
			ftpClient.enterLocalPassiveMode();
			ftpClient.changeWorkingDirectory(ftpPath);
			int status = ftpClient.getReplyCode();
			logger.info("FTP status = " + status);
			// 从服务器上读取指定的文件
			ins = ftpClient.retrieveFileStream(fileName);
		} catch (Exception e) {
			logger.info("e:"+e.getMessage());
			e.printStackTrace();
		}finally {
			try {
				//关闭文件流
				//退出
				ftpClient.logout();
				//断开连接
				ftpClient.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return ins;
	}



	/**
	 * @Description: 通过url获取远程ftp上的文件
	 * @Param: [path]
	 * @return: java.io.InputStream
	 * @Author: jianghy
	 * @Date: 2020/1/10
	 * @Time: 14:07
	 */
	public static InputStream readRemoteFileFromUrl(String path) {
		URL url = null;
		InputStream is =null;
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();//利用HttpURLConnection对象,我们可以从网络中获取网页数据.
			conn.setDoInput(true);
			conn.connect();
			is = conn.getInputStream();	//得到网络返回的输入流
		} catch (IOException e) {
			e.printStackTrace();
		}
		return is;
	}
}
