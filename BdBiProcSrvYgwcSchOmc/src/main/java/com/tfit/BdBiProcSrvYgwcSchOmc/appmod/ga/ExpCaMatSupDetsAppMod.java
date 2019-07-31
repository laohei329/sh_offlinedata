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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaMatSupDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaMatSupDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.ExpCaMatSupDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.ExpCaMatSupDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//导出综合分析原料供应明细列表应用模型
public class ExpCaMatSupDetsAppMod {
	private static final Logger logger = LogManager.getLogger(ExpCaMatSupDetsAppMod.class.getName());
	
	//综合分析原料供应明细列表应用模型
	private CaMatSupDetsAppMod cmsdAppMod = new CaMatSupDetsAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expCaMatSupDets/";
	//导出列名数组
	String[] colNames = {"用料日期", "配货批次号", "学校", "区", "详细地址", "学校学制", "学校性质", "供餐模式", "配送类型", "团餐公司", "物料名称", "标准名称", "原料类别", "数量", "换算关系", "换算数量", "批号", "生产日期", "保质期", "供应商", "是否验收", "验收数量", "验收比例", "配货单图片", "检疫证图片", "验收日期"};
		
	//变量数据初始化
	String startUseDate = "2018-09-03";
	String endUseDate = "2018-09-04";
	String schName = null;
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String matName = null;
	String rmcName = null;
	String supplierName = null;
	String distrBatNumber = null;
	String schType = null;
	String acceptStatus = null;
	String optMode = null;
	String expFileUrl = "test1.txt";
	
	//模拟数据函数
	private ExpCaMatSupDetsDTO SimuDataFunc() {
		ExpCaMatSupDetsDTO ecmsdDto = new ExpCaMatSupDetsDTO();
		//设置返回数据
		ecmsdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpCaMatSupDets expCaMatSupDets = new ExpCaMatSupDets();
		//赋值
		expCaMatSupDets.setStartUseDate(startUseDate);
		expCaMatSupDets.setEndUseDate(endUseDate);
		expCaMatSupDets.setSchName(schName);
		expCaMatSupDets.setDistName(distName);
		expCaMatSupDets.setPrefCity(prefCity);
		expCaMatSupDets.setProvince(province);
		expCaMatSupDets.setMatName(matName);
		expCaMatSupDets.setRmcName(rmcName);
		expCaMatSupDets.setSupplierName(supplierName);
		expCaMatSupDets.setDistrBatNumber(distrBatNumber);
		expCaMatSupDets.setSchType(schType);
		expCaMatSupDets.setAcceptStatus(acceptStatus);
		expCaMatSupDets.setOptMode(optMode);
		expCaMatSupDets.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		ecmsdDto.setExpCaMatSupDets(expCaMatSupDets);
		//消息ID
		ecmsdDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ecmsdDto;
	}
	
	//生成导出EXCEL文件
	public boolean expCaMatSupDetsExcel(String pathFileName, List<CaMatSupDets> dataList, String colNames[]) { 
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
				row.createCell(0).setCellValue(dataList.get(i).getMatUseDate());                                         //用料日期
				row.createCell(1).setCellValue(dataList.get(i).getDistrBatNumber());                                     //配货批次号
				row.createCell(2).setCellValue(dataList.get(i).getSchName());                                            //学校
				row.createCell(3).setCellValue(AppModConfig.distIdToNameMap.get(dataList.get(i).getDistName()));         //区
				row.createCell(4).setCellValue(dataList.get(i).getDetailAddr());                                         //详细地址
				row.createCell(5).setCellValue(dataList.get(i).getSchType());                                            //学校学制
				row.createCell(6).setCellValue(dataList.get(i).getSchProp());                                            //学校性质
				row.createCell(7).setCellValue(dataList.get(i).getOptMode());                                            //供餐模式
				row.createCell(8).setCellValue(dataList.get(i).getDispType());                                           //配送类型
				row.createCell(9).setCellValue(dataList.get(i).getRmcName());                                            //团餐公司               
				row.createCell(10).setCellValue(dataList.get(i).getMatName());                                            //物料名称
				row.createCell(11).setCellValue(dataList.get(i).getStandardName());                                       //标准名称
				row.createCell(12).setCellValue(dataList.get(i).getMatClassify());                                        //原料类别
				row.createCell(13).setCellValue(dataList.get(i).getQuantity());                                           //数量
				row.createCell(14).setCellValue(dataList.get(i).getCvtRel());                                            //换算关系
				row.createCell(15).setCellValue(dataList.get(i).getCvtQuantity());                                       //换算数量
				row.createCell(16).setCellValue(dataList.get(i).getBatNumber());                                         //批号
				row.createCell(17).setCellValue(dataList.get(i).getProdDate());                                          //生产日期
				row.createCell(18).setCellValue(dataList.get(i).getQaGuaPeriod());                                       //保质期
				row.createCell(19).setCellValue(dataList.get(i).getSupplierName());                                      //供应商
				row.createCell(20).setCellValue(AppModConfig.acceptStatusIdToNameMap.get(dataList.get(i).getAcceptStatus()));                                      //是否验收
				row.createCell(21).setCellValue(dataList.get(i).getAcceptNum());                                         //验收数量
				row.createCell(22).setCellValue(dataList.get(i).getAcceptRate() + "%");                                        //验收比例
				row.createCell(23).setCellValue(dataList.get(i).getGsBillPicUrl());                                      //配货单图片
				row.createCell(24).setCellValue(dataList.get(i).getQaCertPicUrl());                                      //检疫证图片
				row.createCell(25).setCellValue(dataList.get(i).getAcceptDate());                                        //验收日期
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
	
	//导出综合分析原料供应明细列表模型函数
	public ExpCaMatSupDetsDTO appModFunc(String token, String startUseDate, String endUseDate, String schName, String distName, String prefCity, String province, String matName, String rmcName, String supplierName, String distrBatNumber, String schType, String acceptStatus, String optMode, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		ExpCaMatSupDetsDTO ecmsdDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (startUseDate == null || endUseDate == null) {   // 按照当天日期获取数据
				startUseDate = BCDTimeUtil.convertNormalDate(null);
				endUseDate = startUseDate;
			}
			CaMatSupDetsDTO cmsdDto = cmsdAppMod.appModFunc(token, startUseDate, endUseDate, schName, distName, prefCity, province, matName, rmcName, supplierName, distrBatNumber, schType, acceptStatus, optMode, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
			if(cmsdDto != null) {
				int i, totalCount = cmsdDto.getPageInfo().getPageTotal();
				int pageCount = 0;
				List<CaMatSupDets> expExcelList = new ArrayList<>();
				if(totalCount % pageSize == 0)
					pageCount = totalCount/pageSize;
				else
					pageCount = totalCount/pageSize + 1;
				//第一页数据
				if(cmsdDto.getCaMatSupDets() != null) {
					expExcelList.addAll(cmsdDto.getCaMatSupDets());
				}
				//后续页数据
				for(i = curPageNum+1; i <= pageCount; i++) {
					strCurPageNum = String.valueOf(i);
					CaMatSupDetsDTO curPdlDto = cmsdAppMod.appModFunc(token, startUseDate, endUseDate, schName, distName, prefCity, province, matName, rmcName, supplierName, distrBatNumber, schType, acceptStatus, optMode, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
					if(curPdlDto.getCaMatSupDets() != null) {
						expExcelList.addAll(curPdlDto.getCaMatSupDets());
					}
				}
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.base_dir + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expCaMatSupDetsExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					//移动文件到其他目录
					AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
					ecmsdDto = new ExpCaMatSupDetsDTO();
					ExpCaMatSupDets expCaMatSupDets = new ExpCaMatSupDets();
					//时戳
					ecmsdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expCaMatSupDets.setStartUseDate(startUseDate);
					expCaMatSupDets.setEndUseDate(endUseDate);
					expCaMatSupDets.setSchName(schName);
					expCaMatSupDets.setDistName(distName);
					expCaMatSupDets.setPrefCity(prefCity);
					expCaMatSupDets.setProvince(province);
					expCaMatSupDets.setMatName(matName);
					expCaMatSupDets.setRmcName(rmcName);
					expCaMatSupDets.setSupplierName(supplierName);
					expCaMatSupDets.setDistrBatNumber(distrBatNumber);
					expCaMatSupDets.setSchType(schType);
					expCaMatSupDets.setAcceptStatus(acceptStatus);
					expCaMatSupDets.setOptMode(optMode);
					expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expCaMatSupDets.setExpFileUrl(expFileUrl);
					ecmsdDto.setExpCaMatSupDets(expCaMatSupDets);
					//消息ID
					ecmsdDto.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			ecmsdDto = SimuDataFunc();
		}

		return ecmsdDto;
	}
}
