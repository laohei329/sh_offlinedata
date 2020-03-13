package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.user;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.AmUserManage;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.AmUserManageDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.ExpAmUserManage;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.ExpAmUserManageDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//导出用户管理应用模型
public class ExpAmUserManageAppMod {
	private static final Logger logger = LogManager.getLogger(ExpAmUserManageAppMod.class.getName());
	
	//用户管理应用模型
	private AmUserManageAppMod aumAppMod = new AmUserManageAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expAmUserManage/";
	//导出列名数组
	String[] colNames = {"用户名", "姓名", "角色", "账号类型", "单位", "手机", "状态", "创建人", "创建日期", "最后登录时间"};	
	
	//变量数据初始化	
	String userName = null;	
	String fullName = null;	
	String roleName = null;	
	String userOrg = null;	
	String accountType = null;	
	String userStatus = null;
	String expFileUrl = "test1.txt";

	//模拟数据函数
	private ExpAmUserManageDTO SimuDataFunc() {
		ExpAmUserManageDTO eaumDto = new ExpAmUserManageDTO();
		//设置返回数据
		eaumDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpAmUserManage expAmUserManage = new ExpAmUserManage();
		//赋值
		expAmUserManage.setUserName(userName);
		expAmUserManage.setFullName(fullName);
		expAmUserManage.setRoleName(roleName);
		expAmUserManage.setUserOrg(userOrg);
		expAmUserManage.setAccountType(accountType);
		expAmUserManage.setUserStatus(userStatus);
		expAmUserManage.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		eaumDto.setExpAmUserManage(expAmUserManage);
		//消息ID
		eaumDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return eaumDto;
	}
	
	//生成导出EXCEL文件
	public boolean expAmUserManageExcel(String pathFileName, List<AmUserManage> dataList, String colNames[]) { 
		boolean retFlag = true;
		Workbook wb = null;
        String excelPath = pathFileName, fileType = "";
        File file = new File(excelPath);
        Sheet sheet = null;
        int idx1 = excelPath.lastIndexOf(".xls"), idx2 = excelPath.lastIndexOf(".xlsx");
        if(idx1 != -1)
        	fileType = excelPath.substring(idx1+1);
        else if(idx2 != -1)
        	fileType = excelPath.substring(idx2+1);
        //创建工作文档对象   
        if (!file.exists()) {      //excel文件不存在
            if (fileType.equals("xls")) {
                wb = new HSSFWorkbook();                
            } else if(fileType.equals("xlsx")) {  
            	wb = new XSSFWorkbook();
            } else {
            	retFlag = false;
            }
            //创建sheet对象   
            if(retFlag) {
            	sheet = (Sheet) wb.createSheet("sheet1");  
            	OutputStream outputStream = null;
				try {
					outputStream = new FileOutputStream(excelPath);
				} catch (FileNotFoundException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				if(outputStream != null) {
					try {
						wb.write(outputStream);
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						outputStream.flush();
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						outputStream.close();
					} catch (IOException e) {
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
				}
				else
					retFlag = false;
            }
            
        } 
        else {       //excel文件已存在
            if (fileType.equals("xls")) {  
                wb = new HSSFWorkbook();                  
            } else if(fileType.equals("xlsx")) { 
                wb = new XSSFWorkbook();                  
            } else {  
            	retFlag = false;
            }  
        }
        //创建sheet对象   
        if (sheet == null && retFlag) {
            sheet = (Sheet) wb.createSheet("sheet1");  
        }
        //写excel文件数据
        if(sheet != null && retFlag) {
        	int startRowIdx = 0;
        	String[] colVals = new String[colNames.length];
			// 添加样式
			Row row = null;
			Cell cell = null;
			// 创建第一行
			row = (Row) sheet.createRow(startRowIdx);
			for (int i = 0; i < colNames.length; i++) {
				cell = row.createCell(i);
				try {
					logger.info(colNames[i] + " ");
					colVals[i] = new String(colNames[i].getBytes(), "utf-8");
					cell.setCellValue(colNames[i]);
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			// 循环写入行数据
			startRowIdx++;
			for (int i = 0; i < dataList.size(); i++) {
				row = (Row) sheet.createRow(i + startRowIdx);
				row.createCell(0).setCellValue(dataList.get(i).getUserName());                                           //用户名
				row.createCell(1).setCellValue(dataList.get(i).getFullName());                                           //姓名
				row.createCell(2).setCellValue(dataList.get(i).getRoleName());                                           //角色
				row.createCell(3).setCellValue(dataList.get(i).getAccountType());                                        //账号类型
				row.createCell(4).setCellValue(dataList.get(i).getUserOrg());                                            //单位
				row.createCell(5).setCellValue(dataList.get(i).getMobPhone());                                           //手机
				row.createCell(6).setCellValue(dataList.get(i).getUserStatus());                                         //状态
				row.createCell(7).setCellValue(dataList.get(i).getCreator());                                            //创建人
				row.createCell(8).setCellValue(dataList.get(i).getCreateTime());                                         //创建日期
				row.createCell(9).setCellValue(dataList.get(i).getLastLoginTime());                                      //最后登录时间
			}
			// 创建文件流
			OutputStream stream = null;
			try {
				stream = new FileOutputStream(excelPath);
			} catch (FileNotFoundException e) {
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}

			if (stream != null) {
				// 写入数据
				try {
					wb.write(stream);
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				// 关闭文件流
				try {
					stream.close();
				} catch (IOException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			} else
				retFlag = false;
        }
        
        return retFlag;
    }
	
	//导出用户管理模型函数
	public ExpAmUserManageDTO appModFunc(String token, String userName, String fullName, String roleName, String userOrg, String accountType, String userStatus, Db2Service db2Service, int[] codes) {
		ExpAmUserManageDTO eaumDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			AmUserManageDTO aumDto = aumAppMod.appModFunc(token, userName, fullName, roleName, userOrg, accountType, userStatus, strCurPageNum, strPageSize, db2Service, codes);
			if(aumDto != null) {
				int i, totalCount = aumDto.getPageInfo().getPageTotal();
				int pageCount = 0;
				List<AmUserManage> expExcelList = new ArrayList<>();
				if(totalCount % pageSize == 0)
					pageCount = totalCount/pageSize;
				else
					pageCount = totalCount/pageSize + 1;
				//第一页数据
				if(aumDto.getAmUserManage() != null) {
					expExcelList.addAll(aumDto.getAmUserManage());
				}
				//后续页数据
				for(i = curPageNum+1; i <= pageCount; i++) {
					strCurPageNum = String.valueOf(i);
					AmUserManageDTO curPdlDto = aumAppMod.appModFunc(token, userName, fullName, roleName, userOrg, accountType, userStatus, strCurPageNum, strPageSize, db2Service, codes);
					if(curPdlDto.getAmUserManage() != null) {
						expExcelList.addAll(curPdlDto.getAmUserManage());
					}
				}
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.base_dir + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expAmUserManageExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					//移动文件到其他目录
					AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
					eaumDto = new ExpAmUserManageDTO();
					ExpAmUserManage expAmUserManage = new ExpAmUserManage();
					//时戳
					eaumDto.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expAmUserManage.setUserName(userName);
					expAmUserManage.setFullName(fullName);
					expAmUserManage.setRoleName(roleName);
					expAmUserManage.setUserOrg(userOrg);
					expAmUserManage.setAccountType(accountType);
					expAmUserManage.setUserStatus(userStatus);
					expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expAmUserManage.setExpFileUrl(expFileUrl);
					eaumDto.setExpAmUserManage(expAmUserManage);
					//消息ID
					eaumDto.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			eaumDto = SimuDataFunc();
		}

		return eaumDto;
	}
}