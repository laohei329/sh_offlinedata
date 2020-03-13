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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.ExpMatConfirmDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.ExpMatConfirmDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.MatConfirmDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.MatConfirmDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//导出用料确认详情列表应用模型
public class ExpMatConfirmDetsAppMod {
	private static final Logger logger = LogManager.getLogger(ExpMatConfirmDetsAppMod.class.getName());
	
	//用料确认详情列表应用模型
	private MatConfirmDetsAppMod mcdAppMod = new MatConfirmDetsAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expMatConfirmDets/";
	//导出列名数组
	String[] colNames = {"用料日期", "用料类别", "项目点名称", "总校/分校", "分校数量", "关联总校", "所属", "主管部门", "所属区", "食品经营许可证主体", "所在地", "学校学制", "办学性质", "供餐模式", "团餐公司", "是否确认"};
		
	//变量数据初始化
	String startDate = "2018-10-30";
	String endDate = "2018-10-30";
	String ppName = null;
	String rmcName = null;
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String confirmFlag = null;
	String schType = null;
	String schProp = null;
	String optMode = null;
	String sendFlag = null;
	String expFileUrl = "test1.txt";
	
	//模拟数据函数
	private ExpMatConfirmDetsDTO SimuDataFunc() {
		ExpMatConfirmDetsDTO emcdDto = new ExpMatConfirmDetsDTO();
		//设置返回数据
		emcdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpMatConfirmDets expMatConfirmDets = new ExpMatConfirmDets();
		//赋值
		expMatConfirmDets.setStartDate(startDate);
		expMatConfirmDets.setEndDate(endDate);
		expMatConfirmDets.setPpName(ppName);
		expMatConfirmDets.setRmcName(rmcName);
		expMatConfirmDets.setDistName(distName);
		expMatConfirmDets.setPrefCity(prefCity);
		expMatConfirmDets.setProvince(province);
		expMatConfirmDets.setConfirmFlag(confirmFlag);
		expMatConfirmDets.setSchType(schType);
		expMatConfirmDets.setSchProp(schProp);
		expMatConfirmDets.setOptMode(optMode);
		expMatConfirmDets.setSendFlag(sendFlag);
		expMatConfirmDets.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		emcdDto.setExpMatConfirmDets(expMatConfirmDets);
		//消息ID
		emcdDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return emcdDto;
	}
	
	//生成导出EXCEL文件
	public boolean expMatConfirmDetsExcel(String pathFileName, List<MatConfirmDets> dataList, String colNames[]) { 
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
				row.createCell(0).setCellValue(dataList.get(i).getMatUseDate());                                              //用料日期
				row.createCell(1).setCellValue(dataList.get(i).getMatCategory());                                             //用料类别
				row.createCell(2).setCellValue(dataList.get(i).getPpName());                                                  //项目点名称
				row.createCell(3).setCellValue(dataList.get(i).getSchGenBraFlag());                                           //总校/分校
				row.createCell(4).setCellValue(dataList.get(i).getBraCampusNum());                                            //分校数量
				row.createCell(5).setCellValue(dataList.get(i).getRelGenSchName());                                           //关联总校
				row.createCell(6).setCellValue(dataList.get(i).getSubLevel());                                                //所属
				row.createCell(7).setCellValue(dataList.get(i).getCompDep());                                                 //主管部门
				row.createCell(8).setCellValue(dataList.get(i).getSubDistName());                                             //所属区
				row.createCell(9).setCellValue(dataList.get(i).getFblMb());                                                   //食品经营许可证主体
				row.createCell(10).setCellValue(AppModConfig.distIdToNameMap.get(dataList.get(i).getDistName()));              //所在地
				row.createCell(11).setCellValue(dataList.get(i).getSchType());	                                              //学校学制			
				row.createCell(12).setCellValue(dataList.get(i).getSchProp());                                                 //办学性质
				row.createCell(13).setCellValue(dataList.get(i).getOptMode());	                                              //供餐模式			
				row.createCell(14).setCellValue(dataList.get(i).getRmcName());                                                 //团餐公司
				row.createCell(15).setCellValue(AppModConfig.confirmFlagIdToNameMap.get(dataList.get(i).getConfirmFlag()));    //是否确认
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
	
	//导出用料确认详情列表模型函数
	public ExpMatConfirmDetsDTO appModFunc(String token, String startDate, String endDate, String ppName,
			String rmcName, String distName, String prefCity, String province, String subLevel,
			String compDep, String schGenBraFlag, String subDistName, String fblMb, String confirmFlag,
			String schType, String schProp, String optMode, String sendFlag, 
			String distNames,String subLevels,String compDeps,String schProps,String schTypes,
			String optModes,String subDistNames,
			Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		ExpMatConfirmDetsDTO emcdDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (startDate == null || endDate == null) {   // 按照当天日期获取数据
				startDate = BCDTimeUtil.convertNormalDate(null);
				endDate = startDate;
			}
			MatConfirmDetsDTO pddDto = mcdAppMod.appModFunc(token, startDate, endDate, ppName, 
					rmcName, distName, prefCity, province, subLevel, compDep, schGenBraFlag, 
					subDistName, fblMb, confirmFlag, schType, schProp, optMode, sendFlag, 
					distNames,subLevels,compDeps,schProps,schTypes,optModes,subDistNames,
					strCurPageNum, strPageSize, db1Service, db2Service, saasService);
			if(pddDto != null) {
				int i, totalCount = pddDto.getPageInfo().getPageTotal();
				int pageCount = 0;
				List<MatConfirmDets> expExcelList = new ArrayList<>();
				if(totalCount % pageSize == 0)
					pageCount = totalCount/pageSize;
				else
					pageCount = totalCount/pageSize + 1;
				//第一页数据
				if(pddDto.getMatConfirmDets() != null) {
					expExcelList.addAll(pddDto.getMatConfirmDets());			
				}
				//后续页数据
				for(i = curPageNum+1; i <= pageCount; i++) {
					strCurPageNum = String.valueOf(i);
					MatConfirmDetsDTO curPdlDto = mcdAppMod.appModFunc(token, startDate, endDate, 
							ppName, rmcName, distName, prefCity, province, subLevel, compDep, 
							schGenBraFlag, subDistName, fblMb, confirmFlag, schType, schProp, 
							optMode, sendFlag, 
							distNames,subLevels,compDeps,schProps,schTypes,optModes,subDistNames,
							strCurPageNum, strPageSize, 
							db1Service, db2Service, saasService);
					if(curPdlDto.getMatConfirmDets() != null) {
						expExcelList.addAll(curPdlDto.getMatConfirmDets());
					}
				}
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.base_dir + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expMatConfirmDetsExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					//移动文件到其他目录
					AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
					emcdDto = new ExpMatConfirmDetsDTO();
					ExpMatConfirmDets expMatConfirmDets = new ExpMatConfirmDets();
					//时戳
					emcdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expMatConfirmDets.setStartDate(startDate);
					expMatConfirmDets.setEndDate(endDate);
					expMatConfirmDets.setPpName(ppName);
					expMatConfirmDets.setRmcName(rmcName);
					expMatConfirmDets.setDistName(AppModConfig.distIdToNameMap.get(distName));
					expMatConfirmDets.setPrefCity(prefCity);
					expMatConfirmDets.setProvince(province);	
					expMatConfirmDets.setConfirmFlag(confirmFlag);
					expMatConfirmDets.setSchType(schType);
					expMatConfirmDets.setSchProp(schProp);
					expMatConfirmDets.setOptMode(optMode);
					expMatConfirmDets.setSendFlag(sendFlag);
					expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expMatConfirmDets.setExpFileUrl(expFileUrl);
					emcdDto.setExpMatConfirmDets(expMatConfirmDets);
					//消息ID
					emcdDto.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			emcdDto = SimuDataFunc();
		}

		return emcdDto;
	}
}
