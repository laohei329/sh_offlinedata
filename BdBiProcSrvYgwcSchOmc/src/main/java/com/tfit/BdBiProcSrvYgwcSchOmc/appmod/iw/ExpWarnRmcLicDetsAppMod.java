package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.iw;

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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.ExpWarnRmcLicDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.ExpWarnRmcLicDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnRmcLicDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnRmcLicDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//导出证照预警团餐公司证件详情列表应用模型
public class ExpWarnRmcLicDetsAppMod {
	private static final Logger logger = LogManager.getLogger(ExpWarnRmcLicDetsAppMod.class.getName());
	
	//证照预警团餐公司证件详情列表应用模型
	private WarnRmcLicDetsAppMod wrldAppMod = new WarnRmcLicDetsAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expWarnRmcLicDets/";
	//导出列名数组
	String[] colNames = {"预警日期", "区", "团餐公司名称", "证件名称", "证件号码", "有效日期", "证件状况", "状态", "消除日期"};
	
	
	
	//变量数据初始化
	String startWarnDate = "2018-09-03";
	String endWarnDate = "2018-09-04";
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String rmcName = null;
	String licType = null;
	String licAuditStatus = null;
	String startElimDate = null;
	String endElimDate = null;
	String startValidDate = null;
	String endValidDate = null;
	String licNo = null;
	String expFileUrl = "test1.txt";

	//模拟数据函数
	private ExpWarnRmcLicDetsDTO SimuDataFunc() {
		ExpWarnRmcLicDetsDTO ewrldDto = new ExpWarnRmcLicDetsDTO();
		//设置返回数据
		ewrldDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpWarnRmcLicDets expWarnRmcLicDets = new ExpWarnRmcLicDets();
		//赋值
		expWarnRmcLicDets.setStartWarnDate(startWarnDate);
		expWarnRmcLicDets.setEndWarnDate(endWarnDate);
		expWarnRmcLicDets.setDistName(distName);
		expWarnRmcLicDets.setPrefCity(prefCity);
		expWarnRmcLicDets.setProvince(province);
		expWarnRmcLicDets.setRmcName(rmcName);
		expWarnRmcLicDets.setLicType(licType);
		expWarnRmcLicDets.setLicAuditStatus(licAuditStatus);
		expWarnRmcLicDets.setStartElimDate(startElimDate);
		expWarnRmcLicDets.setEndElimDate(endElimDate);
		expWarnRmcLicDets.setStartValidDate(startValidDate);
		expWarnRmcLicDets.setEndValidDate(endValidDate);
		expWarnRmcLicDets.setLicNo(licNo);
		expWarnRmcLicDets.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		ewrldDto.setExpWarnRmcLicDets(expWarnRmcLicDets);
		//消息ID
		ewrldDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ewrldDto;
	}
	
	//生成导出EXCEL文件
	public boolean expWarnRmcLicDetsExcel(String pathFileName, List<WarnRmcLicDets> dataList, String colNames[]) { 
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
				row.createCell(0).setCellValue(dataList.get(i).getWarnDate());                                           //预警日期
				row.createCell(1).setCellValue(AppModConfig.distIdToNameMap.get(dataList.get(i).getDistName()));         //区
				row.createCell(2).setCellValue(dataList.get(i).getRmcName());                                            //学校学制
				row.createCell(3).setCellValue(dataList.get(i).getLicName());                                            //证件名称
				row.createCell(4).setCellValue(dataList.get(i).getLicNo());                                              //证件号码
				row.createCell(5).setCellValue(dataList.get(i).getValidDate());                                          //有效日期
				row.createCell(6).setCellValue(dataList.get(i).getLicStatus());                                          //证件状况
				row.createCell(7).setCellValue(AppModConfig.licAudStatusIdToNameMap.get(dataList.get(i).getLicAuditStatus()));                                     //状态
				row.createCell(8).setCellValue(dataList.get(i).getElimDate());                                           //消除日期
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
	
	//导出证照预警团餐公司证件详情列表模型函数
	public ExpWarnRmcLicDetsDTO appModFunc(String token, String startWarnDate, String endWarnDate, String distName, String prefCity, String province, String rmcName, String licType, String licStatus, String licAuditStatus, String startElimDate, String endElimDate, String startValidDate, String endValidDate, String licNo, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		ExpWarnRmcLicDetsDTO ewrldDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (startWarnDate == null || endWarnDate == null) {   // 按照当天日期获取数据
				startWarnDate = BCDTimeUtil.convertNormalDate(null);
				endWarnDate = startWarnDate;
			}
			WarnRmcLicDetsDTO wrldDto = wrldAppMod.appModFunc(token, startWarnDate, endWarnDate, distName, prefCity, province, rmcName, licType, licStatus, licAuditStatus, startElimDate, endElimDate, startValidDate, endValidDate, licNo, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
			if(wrldDto != null) {
				int i, totalCount = wrldDto.getPageInfo().getPageTotal();
				int pageCount = 0;
				List<WarnRmcLicDets> expExcelList = new ArrayList<>();
				if(totalCount % pageSize == 0)
					pageCount = totalCount/pageSize;
				else
					pageCount = totalCount/pageSize + 1;
				//第一页数据
				if(wrldDto.getWarnRmcLicDets() != null) {
					expExcelList.addAll(wrldDto.getWarnRmcLicDets());
				}
				//后续页数据
				for(i = curPageNum+1; i <= pageCount; i++) {
					strCurPageNum = String.valueOf(i);
					WarnRmcLicDetsDTO curPdlDto = wrldAppMod.appModFunc(token, startWarnDate, endWarnDate, distName, prefCity, province, rmcName, licType, licStatus, licAuditStatus, startElimDate, endElimDate, startValidDate, endValidDate, licNo, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
					if(curPdlDto.getWarnRmcLicDets() != null) {
						expExcelList.addAll(curPdlDto.getWarnRmcLicDets());
					}
				}
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.base_dir + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expWarnRmcLicDetsExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					//移动文件到其他目录
					AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
					ewrldDto = new ExpWarnRmcLicDetsDTO();
					ExpWarnRmcLicDets expWarnRmcLicDets = new ExpWarnRmcLicDets();
					//时戳
					ewrldDto.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expWarnRmcLicDets.setStartWarnDate(startWarnDate);
					expWarnRmcLicDets.setEndWarnDate(endWarnDate);
					expWarnRmcLicDets.setDistName(distName);
					expWarnRmcLicDets.setPrefCity(prefCity);
					expWarnRmcLicDets.setProvince(province);
					expWarnRmcLicDets.setRmcName(rmcName);
					expWarnRmcLicDets.setLicType(licType);
					expWarnRmcLicDets.setLicAuditStatus(licAuditStatus);
					expWarnRmcLicDets.setStartElimDate(startElimDate);
					expWarnRmcLicDets.setEndElimDate(endElimDate);
					expWarnRmcLicDets.setStartValidDate(startValidDate);
					expWarnRmcLicDets.setEndValidDate(endValidDate);
					expWarnRmcLicDets.setLicNo(licNo);
					expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expWarnRmcLicDets.setExpFileUrl(expFileUrl);
					ewrldDto.setExpWarnRmcLicDets(expWarnRmcLicDets);
					//消息ID
					ewrldDto.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			ewrldDto = SimuDataFunc();
		}

		return ewrldDto;
	}
}
