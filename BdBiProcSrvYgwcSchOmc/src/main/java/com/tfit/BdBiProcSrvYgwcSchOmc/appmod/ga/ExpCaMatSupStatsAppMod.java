package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.ga;

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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.ExpCaMatSupStats;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.ExpCaMatSupStatsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaMatSupStats;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaMatSupStatsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//导出综合分析原料供应统计列表应用模型
public class ExpCaMatSupStatsAppMod {
	private static final Logger logger = LogManager.getLogger(ExpCaMatSupStatsAppMod.class.getName());
	
	//综合分析原料供应统计列表应用模型
	private CaMatSupStatsAppMod cmssAppMod = new CaMatSupStatsAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expCaMatSupStats/";
	//导出列名数组
	String[] colNames = {"排序", "标准名称", "原料类别", "分类", "实际数量"};	
	
	//变量数据初始化	
	String startUseDate = "2018-09-03";
	String endUseDate = "2018-09-04";
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String schName = null;
	String matClassify = null;
	String matCategory = null;
	String expFileUrl = "test1.txt";
	
	//模拟数据函数
	private ExpCaMatSupStatsDTO SimuDataFunc() {
		ExpCaMatSupStatsDTO ecmssDto = new ExpCaMatSupStatsDTO();
		//设置返回数据
		ecmssDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpCaMatSupStats expCaMatSupStats = new ExpCaMatSupStats();
		//赋值
		expCaMatSupStats.setStartUseDate(startUseDate);
		expCaMatSupStats.setEndUseDate(endUseDate);
		expCaMatSupStats.setDistName(distName);
		expCaMatSupStats.setPrefCity(prefCity);
		expCaMatSupStats.setProvince(province);
		expCaMatSupStats.setSchName(schName);
		expCaMatSupStats.setMatClassify(matClassify);
		expCaMatSupStats.setMatCategory(matCategory);
		expCaMatSupStats.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		ecmssDto.setExpCaMatSupStats(expCaMatSupStats);
		//消息ID
		ecmssDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ecmssDto;
	}
	
	//生成导出EXCEL文件
	public boolean expCaMatSupStatsExcel(String pathFileName, List<CaMatSupStats> dataList, String colNames[]) { 
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
				row.createCell(0).setCellValue(dataList.get(i).getSn());                                                       //排序
				row.createCell(1).setCellValue(dataList.get(i).getStandardName());                                             //标准名称
				row.createCell(2).setCellValue(AppModConfig.matCategoryIdToNameMap.get(dataList.get(i).getMatCategory()));     //原料类别
				row.createCell(3).setCellValue(dataList.get(i).getMatClassify());                                              //原料分类
				row.createCell(4).setCellValue(dataList.get(i).getActualQuan() + " kg");                                       //实际数量
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
	
	//导出综合分析原料供应统计列表模型函数
	public ExpCaMatSupStatsDTO appModFunc(String token, String startUseDate, String endUseDate, String distName, String prefCity, String province, String schType, String schName, String matClassify, String matCategory, String matStdName, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		ExpCaMatSupStatsDTO ecmssDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (startUseDate == null || endUseDate == null) {   // 按照当天日期获取数据
				startUseDate = BCDTimeUtil.convertNormalDate(null);
				endUseDate = startUseDate;
			}
			CaMatSupStatsDTO pdlDto = cmssAppMod.appModFunc(token, startUseDate, endUseDate, distName, prefCity, province, schType, schName, matClassify, matCategory, matStdName, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
			if(pdlDto != null) {
				int i, totalCount = pdlDto.getPageInfo().getPageTotal();
				int pageCount = 0;
				List<CaMatSupStats> expExcelList = new ArrayList<>();
				if(totalCount % pageSize == 0)
					pageCount = totalCount/pageSize;
				else
					pageCount = totalCount/pageSize + 1;
				//第一页数据
				if(pdlDto.getCaMatSupStats() != null) {
					expExcelList.addAll(pdlDto.getCaMatSupStats());			
				}
				//后续页数据
				for(i = curPageNum+1; i <= pageCount; i++) {
					strCurPageNum = String.valueOf(i);
					CaMatSupStatsDTO curGpoDto = cmssAppMod.appModFunc(token, startUseDate, endUseDate, distName, prefCity, province, schType, schName, matClassify, matCategory, matStdName, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
					if(curGpoDto.getCaMatSupStats() != null) {
						expExcelList.addAll(curGpoDto.getCaMatSupStats());
					}
				}
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.base_dir + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expCaMatSupStatsExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					//移动文件到其他目录
					AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
					ecmssDto = new ExpCaMatSupStatsDTO();
					ExpCaMatSupStats expCaMatSupStats = new ExpCaMatSupStats();
					//时戳
					ecmssDto.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expCaMatSupStats.setStartUseDate(startUseDate);
					expCaMatSupStats.setEndUseDate(endUseDate);
					expCaMatSupStats.setDistName(distName);
					expCaMatSupStats.setPrefCity(prefCity);
					expCaMatSupStats.setProvince(province);
					expCaMatSupStats.setSchName(schName);
					expCaMatSupStats.setMatClassify(matClassify);
					expCaMatSupStats.setMatCategory(matCategory);
					expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expCaMatSupStats.setExpFileUrl(expFileUrl);
					ecmssDto.setExpCaMatSupStats(expCaMatSupStats);
					//消息ID
					ecmssDto.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			ecmssDto = SimuDataFunc();
		}

		return ecmssDto;
	}
}
