package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.im;

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
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.ExpPpGsPlanOpts;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.ExpPpGsPlanOptsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpGsPlanOpts;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpGsPlanOptsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//3.2.42.	导出项目点配货计划操作列表应用模型
public class ExpPpGsPlanOptsAppMod {
	private static final Logger logger = LogManager.getLogger(ExpPpGsPlanOptsAppMod.class.getName());
	
	//3.2.41.	项目点配货计划操作列表
	private PpGsPlanOptsAppMod ppGsPlanOptsAppMod = new PpGsPlanOptsAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expPpGsPlanOpts/";
	//导出列名数组
	String[] colNames = {"配货日期", "所在地", "学校学制", "项目点", "地址", "项目联系人", "手机", "配货计划数量",
			"验收状态", "已验收数量", "未验收数量", "已指派状态", "已指派数量", "未指派数量", "配送状态", "已配送数量", "未配送数量"};	
	//变量数据初始化
	String startDate = "2018-09-03";
	String endDate = "2018-09-04";
	String ppName = null;
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String acceptStatus = null;
	String assignStatus = null;
	String schType = null;
	String dispStatus = null;
	String expFileUrl = "test1.txt";
	
	//模拟数据函数
	private ExpPpGsPlanOptsDTO SimuDataFunc() {
		ExpPpGsPlanOptsDTO eppGsPlanOptsDTO = new ExpPpGsPlanOptsDTO();
		//设置返回数据
		eppGsPlanOptsDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpPpGsPlanOpts expPpGsPlanOpts = new ExpPpGsPlanOpts();
		//赋值
		expPpGsPlanOpts.setStartDate(startDate);
		expPpGsPlanOpts.setEndDate(endDate);
		expPpGsPlanOpts.setPpName(ppName);
		expPpGsPlanOpts.setDistName(distName);
		expPpGsPlanOpts.setPrefCity(prefCity);
		expPpGsPlanOpts.setProvince(province);
		expPpGsPlanOpts.setAcceptStatus(acceptStatus);
		expPpGsPlanOpts.setAssignStatus(assignStatus);
		expPpGsPlanOpts.setDispStatus(dispStatus);
		expPpGsPlanOpts.setSchType(schType);
		
		
		expPpGsPlanOpts.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		eppGsPlanOptsDTO.setExpPpGsPlanOpts(expPpGsPlanOpts);
		//消息ID
		eppGsPlanOptsDTO.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return eppGsPlanOptsDTO;
	}
	
	//生成导出EXCEL文件
	public boolean expPpGsPlanOptsExcel(String pathFileName, List<PpGsPlanOpts> dataList, String colNames[]) { 
		boolean retFlag = true;
		Workbook wb = null;
        String excelPath = pathFileName, fileType = "";
        File file = new File(excelPath);
        Sheet sheet = null;
        int idx1 = excelPath.lastIndexOf(".xls"), idx2 = excelPath.lastIndexOf(".xlsx");
        if(dataList.size() > AppModConfig.maxPageSize)
        	return false;
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
			//获取excell单元风格
		  	CellStyle style = AppModConfig.getExcellCellStyle(wb);
			for (int i = 0; i < colNames.length; i++) {
				cell = row.createCell(i);
				try {
					logger.info(colNames[i] + " ");
					colVals[i] = new String(colNames[i].getBytes(), "utf-8");
					cell.setCellValue(colNames[i]);
					cell.setCellStyle(style);
				} catch (UnsupportedEncodingException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			// 循环写入行数据
			startRowIdx++;
			for (int i = 0; i < dataList.size(); i++) {
				row = (Row) sheet.createRow(i + startRowIdx);		
				row.createCell(0).setCellValue(dataList.get(i).getDistrDate()); //配货日期
				row.createCell(1).setCellValue(dataList.get(i).getDistName());  //所在地
				row.createCell(2).setCellValue(dataList.get(i).getSchType());   //学校学制
				row.createCell(3).setCellValue(dataList.get(i).getPpName());    //项目点
				row.createCell(4).setCellValue(dataList.get(i).getDetailAddr());//地址
				row.createCell(5).setCellValue(dataList.get(i).getProjContact());  //项目联系人
				row.createCell(6).setCellValue(dataList.get(i).getPcMobilePhone());//手机
				row.createCell(7).setCellValue(dataList.get(i).getDistrPlanNum()); //配货计划数量
				row.createCell(8).setCellValue(dataList.get(i).getAcceptStatus()==0?"否":"是"); //验收状态
				row.createCell(9).setCellValue(dataList.get(i).getAcceptPlanNum());            //已验收数量
				row.createCell(10).setCellValue(dataList.get(i).getNoAcceptPlanNum());         //未验收数量
				row.createCell(11).setCellValue(dataList.get(i).getAssignStatus()==0?"否":"是"); //已指派状态
				row.createCell(12).setCellValue(dataList.get(i).getAssignPlanNum());       //已指派数量
				row.createCell(13).setCellValue(dataList.get(i).getNoAssignPlanNum());     //未指派数量
				row.createCell(14).setCellValue(dataList.get(i).getDispStatus()==0?"否":"是");   //配送状态
				row.createCell(15).setCellValue(dataList.get(i).getDispPlanNum());              //已配送数量
				row.createCell(16).setCellValue(dataList.get(i).getNoDispPlanNum());            //未配送数量
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
	
	//导出项目点排菜详情列表模型函数
	public ExpPpGsPlanOptsDTO appModFunc(String token,String startDate,String endDate,String ppName,
			Integer acceptStatus,Integer assignStatus, Integer dispStatus,Integer schType, String distName, 
			String prefCity, String province,String distNames,String schTypes,
			Db1Service db1Service,Db2Service db2Service) {
		ExpPpGsPlanOptsDTO eppGsPlanOptsDTO = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (startDate == null || endDate == null) {   // 按照当天日期获取数据
				startDate = BCDTimeUtil.convertNormalDate(null);
				endDate = startDate;
			}
			PpGsPlanOptsDTO ppGsPlanOptsDTO = ppGsPlanOptsAppMod.appModFunc(token, startDate, endDate, ppName, acceptStatus, 
					assignStatus, dispStatus, schType, distName, prefCity, province, distNames,schTypes,
					strCurPageNum, strPageSize, 
					db1Service, null, db2Service);
			
			if(ppGsPlanOptsDTO != null) {
				int pageSizeTemp = ppGsPlanOptsDTO.getPageInfo().getPageTotal();
				List<PpGsPlanOpts> expExcelList = new ArrayList<>();
				
				if(ppGsPlanOptsDTO.getPpGsPlanOpts()!=null && ppGsPlanOptsDTO.getPpGsPlanOpts().size() > 0 && 
						ppGsPlanOptsDTO.getPpGsPlanOpts().size() <ppGsPlanOptsDTO.getPageInfo().getPageTotal() ) {
					ppGsPlanOptsDTO = ppGsPlanOptsAppMod.appModFunc(token, startDate, endDate, ppName, acceptStatus, assignStatus,
							dispStatus, schType, distName, prefCity, province,distNames,schTypes,
							pageSizeTemp+"", strPageSize, db1Service, 
							null, db2Service);
				}
				if(ppGsPlanOptsDTO !=null && ppGsPlanOptsDTO.getPpGsPlanOpts()!=null && ppGsPlanOptsDTO.getPpGsPlanOpts().size() > 0) {
					expExcelList.addAll(ppGsPlanOptsDTO.getPpGsPlanOpts());	
				}
				
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.tomcatSrvDirs[1] + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expPpGsPlanOptsExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					eppGsPlanOptsDTO = new ExpPpGsPlanOptsDTO();
					ExpPpGsPlanOpts expPpGsPlanOpts = new ExpPpGsPlanOpts();
					//时戳
					eppGsPlanOptsDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expPpGsPlanOpts.setStartDate(startDate);
					expPpGsPlanOpts.setEndDate(endDate);
					expPpGsPlanOpts.setPpName(ppName);
					expPpGsPlanOpts.setDistName(AppModConfig.distIdToNameMap.get(distName));
					expPpGsPlanOpts.setPrefCity(prefCity);
					expPpGsPlanOpts.setProvince(province);
					//验收状态
					if(acceptStatus !=null && acceptStatus==0) {
						expPpGsPlanOpts.setAcceptStatus("未验收");
					}else if(acceptStatus!=null && acceptStatus==1) {
						expPpGsPlanOpts.setAcceptStatus("已验收");
					}else {
						expPpGsPlanOpts.setAcceptStatus("");
					}
					//指派状态
					if(assignStatus!=null && assignStatus==0) {
						expPpGsPlanOpts.setAssignStatus("未指派");
					}else if(assignStatus!=null && assignStatus==1) {
						expPpGsPlanOpts.setAssignStatus("已指派");
					}else {
						expPpGsPlanOpts.setAssignStatus("");
					}
					//配送状态
					if(dispStatus!=null && dispStatus==0) {
						expPpGsPlanOpts.setDispStatus("未配送");
					}else if(dispStatus!=null && dispStatus==1) {
						expPpGsPlanOpts.setDispStatus("已配送");
					}else {
						expPpGsPlanOpts.setDispStatus("");
					}
					//学校类型（学制）
					if(schType != null) {
						expPpGsPlanOpts.setSchType(AppModConfig.schTypeIdToNameMap.get(schType));
					}else {
						expPpGsPlanOpts.setSchType("");
					}
					//导出文件URL
					String expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expPpGsPlanOpts.setExpFileUrl(expFileUrl);
					eppGsPlanOptsDTO.setExpPpGsPlanOpts(expPpGsPlanOpts);
					//消息ID
					eppGsPlanOptsDTO.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			eppGsPlanOptsDTO = SimuDataFunc();
		}

		return eppGsPlanOptsDTO;
	}
}