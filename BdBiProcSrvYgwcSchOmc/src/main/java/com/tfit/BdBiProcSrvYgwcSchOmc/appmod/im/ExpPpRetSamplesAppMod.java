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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.ExpPpRetSamples;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.ExpPpRetSamplesDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpRetSamples;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpRetSamplesDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//3.2.54.	导出项目点留样列表
public class ExpPpRetSamplesAppMod {
	private static final Logger logger = LogManager.getLogger(ExpPpRetSamplesAppMod.class.getName());
	
	//3.2.41.	项目点配货计划操作列表
	private PpRetSamplesAppMod ppRetSamplesAppMod = new PpRetSamplesAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expPpRetSamples/";
	//导出列名数组
	String[] colNames = {"就餐日期", "所在地", "学校学制", "项目点名称", "地址", "项目联系人", "手机",
			"是否留样", "菜品数量", "已留样菜品", "未留样菜品"};	
	//变量数据初始化
	String startDate = "2018-09-03";
	String endDate = "2018-09-04";
	String ppName = null;
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String rsFlag = null;
	String schType = null;
	String expFileUrl = "test1.txt";
	
	//模拟数据函数
	private ExpPpRetSamplesDTO SimuDataFunc() {
		ExpPpRetSamplesDTO eppGsPlanOptsDTO = new ExpPpRetSamplesDTO();
		//设置返回数据
		eppGsPlanOptsDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpPpRetSamples expPpRetSamples = new ExpPpRetSamples();
		//赋值
		expPpRetSamples.setStartDate(startDate);
		expPpRetSamples.setEndDate(endDate);
		expPpRetSamples.setPpName(ppName);
		expPpRetSamples.setDistName(distName);
		expPpRetSamples.setPrefCity(prefCity);
		expPpRetSamples.setProvince(province);
		expPpRetSamples.setRsFlag(rsFlag);
		expPpRetSamples.setSchType(schType);
		
		
		expPpRetSamples.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		eppGsPlanOptsDTO.setExpPpRetSamples(expPpRetSamples);
		//消息ID
		eppGsPlanOptsDTO.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return eppGsPlanOptsDTO;
	}
	
	//生成导出EXCEL文件
	public boolean expPpRetSamplesExcel(String pathFileName, List<PpRetSamples> dataList, String colNames[]) { 
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
				row.createCell(0).setCellValue(dataList.get(i).getRepastDate()); //就餐日期
				row.createCell(1).setCellValue(dataList.get(i).getDistName());  //所在地
				row.createCell(2).setCellValue(dataList.get(i).getSchType());   //学校学制
				row.createCell(3).setCellValue(dataList.get(i).getPpName());    //项目点
				row.createCell(4).setCellValue(dataList.get(i).getDetailAddr());//地址
				row.createCell(5).setCellValue(dataList.get(i).getProjContact());  //项目联系人
				row.createCell(6).setCellValue(dataList.get(i).getPcMobilePhone());//手机
				row.createCell(7).setCellValue(dataList.get(i).getRsFlag()==0?"否":"是"); //是否留样
				row.createCell(8).setCellValue(dataList.get(i).getDishNum()); //菜品数量
				row.createCell(9).setCellValue(dataList.get(i).getRsDishNum());            //已留样菜品
				row.createCell(10).setCellValue(dataList.get(i).getNoRsDishNum());         //未留样菜品
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
	public ExpPpRetSamplesDTO appModFunc(String token,String startDate,String endDate,String ppName,Integer rsFlag,Integer schType, 
			String distName, String prefCity, String province, String distNames,String schTypes,
			Db1Service db1Service,Db2Service db2Service) {
		ExpPpRetSamplesDTO eppGsPlanOptsDTO = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (startDate == null || endDate == null) {   // 按照当天日期获取数据
				startDate = BCDTimeUtil.convertNormalDate(null);
				endDate = startDate;
			}
			PpRetSamplesDTO ppGsPlanOptsDTO = ppRetSamplesAppMod.appModFunc(token, startDate, endDate, ppName, rsFlag, 
					schType, distName, prefCity, province,distNames,schTypes,strCurPageNum, strPageSize, 
					db1Service, null, db2Service);
			
			if(ppGsPlanOptsDTO != null) {
				int pageSizeTemp = ppGsPlanOptsDTO.getPageInfo().getPageTotal();
				List<PpRetSamples> expExcelList = new ArrayList<>();
				
				if(ppGsPlanOptsDTO.getPpRetSamples()!=null && ppGsPlanOptsDTO.getPpRetSamples().size() > 0 && 
						ppGsPlanOptsDTO.getPpRetSamples().size() <ppGsPlanOptsDTO.getPageInfo().getPageTotal() ) {
					
					ppGsPlanOptsDTO = ppRetSamplesAppMod.appModFunc(token, startDate, endDate, ppName, rsFlag, 
							schType, distName, prefCity, province,distNames,schTypes,strCurPageNum, pageSizeTemp+"", 
							db1Service, null, db2Service);
				}
				if(ppGsPlanOptsDTO !=null && ppGsPlanOptsDTO.getPpRetSamples()!=null && ppGsPlanOptsDTO.getPpRetSamples().size() > 0) {
					expExcelList.addAll(ppGsPlanOptsDTO.getPpRetSamples());	
				}
				
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.tomcatSrvDirs[1] + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expPpRetSamplesExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					eppGsPlanOptsDTO = new ExpPpRetSamplesDTO();
					ExpPpRetSamples expPpRetSamples = new ExpPpRetSamples();
					//时戳
					eppGsPlanOptsDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expPpRetSamples.setStartDate(startDate);
					expPpRetSamples.setEndDate(endDate);
					expPpRetSamples.setPpName(ppName);
					expPpRetSamples.setDistName(AppModConfig.distIdToNameMap.get(distName));
					expPpRetSamples.setPrefCity(prefCity);
					expPpRetSamples.setProvince(province);
					//是否留样标识
					if(rsFlag !=null && rsFlag==0) {
						expPpRetSamples.setRsFlag("未留样");
					}else if(rsFlag!=null && rsFlag==1) {
						expPpRetSamples.setRsFlag("已留样");
					}else {
						expPpRetSamples.setRsFlag("");
					}
					//学校类型（学制）
					if(schType != null) {
						expPpRetSamples.setSchType(AppModConfig.schTypeIdToNameMap.get(schType));
					}else {
						expPpRetSamples.setSchType("");
					}
					//导出文件URL
					String expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expPpRetSamples.setExpFileUrl(expFileUrl);
					eppGsPlanOptsDTO.setExpPpRetSamples(expPpRetSamples);
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