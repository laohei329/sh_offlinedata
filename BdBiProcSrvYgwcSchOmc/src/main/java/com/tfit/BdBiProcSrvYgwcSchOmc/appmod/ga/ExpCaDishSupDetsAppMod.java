package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.ga;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaDishSupDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaDishSupDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.ExpCaDishSupDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.ExpCaDishSupDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.DbHiveService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.CommonUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.export.RelectUtil;

//导出综合分析菜品供应明细列表应用模型
public class ExpCaDishSupDetsAppMod {
	private static final Logger logger = LogManager.getLogger(ExpCaDishSupDetsAppMod.class.getName());
	
	//综合分析菜品供应明细列表应用模型
	private CaDishSupDetsAppMod cdsdAppMod = new CaDishSupDetsAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expCaDishSupDets/";
	//导出列名数组
	String[] colNames = {"用餐日期", "学校", "区", "详细地址", "学校学制", "学校性质", "供餐模式", "配送类型", "餐别", "菜品名称", "分类", "份数", "菜单名称", "团餐公司"};
	int methodIndex =2;	
	//变量数据初始化
	String repastStartDate = "2018-09-03";
	String repasEndDate = "2018-09-04";
	String schName = null;
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String dishName = null;
	String rmcName = null;
	String caterType = null;
	String schType = null;
	String schProp = null;
	String optMode = null;
	String menuName = null;
	String expFileUrl = "test1.txt";	
	
	//模拟数据函数
	private ExpCaDishSupDetsDTO SimuDataFunc() {
		ExpCaDishSupDetsDTO ecdsdDto = new ExpCaDishSupDetsDTO();
		//设置返回数据
		ecdsdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpCaDishSupDets expCaDishSupDets = new ExpCaDishSupDets();
		//赋值
		expCaDishSupDets.setRepastStartDate(repastStartDate);
		expCaDishSupDets.setRepasEndDate(repasEndDate);
		expCaDishSupDets.setSchName(schName);
		expCaDishSupDets.setDistName(distName);
		expCaDishSupDets.setPrefCity(prefCity);
		expCaDishSupDets.setProvince(province);
		expCaDishSupDets.setDishName(dishName);
		expCaDishSupDets.setRmcName(rmcName);
		expCaDishSupDets.setCaterType(caterType);
		expCaDishSupDets.setSchType(schType);
		expCaDishSupDets.setSchProp(schProp);
		expCaDishSupDets.setOptMode(optMode);
		expCaDishSupDets.setMenuName(menuName);		
		expCaDishSupDets.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		ecdsdDto.setExpCaDishSupDets(expCaDishSupDets);
		//消息ID
		ecdsdDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ecdsdDto;
	}
	
	//生成导出EXCEL文件
	public boolean expCaDishSupDetsExcel(String pathFileName, List<CaDishSupDets> dataList, String colNames[]) { 
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
						logger.info("异常："+e.getMessage());
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						outputStream.flush();
					} catch (IOException e) {
						logger.info("异常："+e.getMessage());
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						outputStream.close();
					} catch (IOException e) {
						logger.info("异常："+e.getMessage());
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
					logger.info("异常："+e.getMessage());
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			// 循环写入行数据
			startRowIdx++;
			for (int i = 0; i < dataList.size(); i++) {
				row = (Row) sheet.createRow(i + startRowIdx);
				row.createCell(0).setCellValue(dataList.get(i).getRepastDate());                                         //用餐日期
				row.createCell(1).setCellValue(dataList.get(i).getSchName());                                            //学校
				row.createCell(2).setCellValue(AppModConfig.distIdToNameMap.get(dataList.get(i).getDistName()));         //区
				row.createCell(3).setCellValue(dataList.get(i).getDetailAddr());                                         //详细地址
				row.createCell(4).setCellValue(dataList.get(i).getSchType());                                            //学校学制
				row.createCell(5).setCellValue(dataList.get(i).getSchProp());                                            //学校性质
				row.createCell(6).setCellValue(dataList.get(i).getOptMode());                                            //供餐模式
				row.createCell(7).setCellValue(dataList.get(i).getDispType());                                           //配送类型
				row.createCell(8).setCellValue(dataList.get(i).getCaterType());                                          //餐别
				row.createCell(9).setCellValue(dataList.get(i).getDishName());                                           //菜品名称          
				row.createCell(10).setCellValue(dataList.get(i).getDishType());                                          //分类
				row.createCell(11).setCellValue(dataList.get(i).getSupNum());                                            //份数
				row.createCell(12).setCellValue(dataList.get(i).getMenuName());                                          //菜单名称
				row.createCell(13).setCellValue(dataList.get(i).getRmcName());                                           //团餐公司
			}
			// 创建文件流
			OutputStream stream = null;
			try {
				stream = new FileOutputStream(excelPath);
			} catch (FileNotFoundException e) {
				logger.info("异常："+e.getMessage());
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}

			if (stream != null) {
				// 写入数据
				try {
					wb.write(stream);
				} catch (IOException e) {
					logger.info("异常："+e.getMessage());
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				// 关闭文件流
				try {
					stream.close();
				} catch (IOException e) {
					logger.info("异常："+e.getMessage());
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			} else
				retFlag = false;
        }
        
        return retFlag;
    }
	
	
	//生成导出EXCEL文件
	public boolean expCaDishSupDetsExcelThree(String pathFileName, List<CaDishSupDets> dataList, String colNames[]) { 
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
						logger.info("异常："+e.getMessage());
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						outputStream.flush();
					} catch (IOException e) {
						logger.info("异常："+e.getMessage());
						// TODO 自动生成的 catch 块
						e.printStackTrace();
					}
					try {
						outputStream.close();
					} catch (IOException e) {
						logger.info("异常："+e.getMessage());
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
					logger.info("异常："+e.getMessage());
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
			// 循环写入行数据
			startRowIdx++;
			for (int i = 0; i < dataList.size(); i++) {
				row = (Row) sheet.createRow(i + startRowIdx);
				row.createCell(0).setCellValue(dataList.get(i).getRepastDate());                                         //用餐日期
				row.createCell(1).setCellValue(dataList.get(i).getSchName());                                            //学校
				row.createCell(2).setCellValue(AppModConfig.distIdToNameMap.get(dataList.get(i).getDistName()));         //区
				row.createCell(3).setCellValue(dataList.get(i).getDetailAddr());                                         //详细地址
				row.createCell(4).setCellValue(dataList.get(i).getSchType());                                            //学校学制
				row.createCell(5).setCellValue(dataList.get(i).getSchProp());                                            //学校性质
				row.createCell(6).setCellValue(dataList.get(i).getOptMode());                                            //供餐模式
				row.createCell(7).setCellValue(dataList.get(i).getDispType());                                           //配送类型
				row.createCell(8).setCellValue(dataList.get(i).getCaterType());                                          //餐别
				row.createCell(9).setCellValue(dataList.get(i).getDishName());                                           //菜品名称          
				row.createCell(10).setCellValue(dataList.get(i).getDishType());                                          //分类
				row.createCell(11).setCellValue(dataList.get(i).getSupNum());                                            //份数
				row.createCell(12).setCellValue(dataList.get(i).getMenuName());                                          //菜单名称
				row.createCell(13).setCellValue(dataList.get(i).getRmcName());                                           //团餐公司
			}
			// 创建文件流
			OutputStream stream = null;
			try {
				stream = new FileOutputStream(excelPath);
			} catch (FileNotFoundException e) {
				logger.info("异常："+e.getMessage());
				// TODO 自动生成的 catch 块
				e.printStackTrace();
			}

			if (stream != null) {
				// 写入数据
				try {
					wb.write(stream);
				} catch (IOException e) {
					logger.info("异常："+e.getMessage());
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
				// 关闭文件流
				try {
					stream.close();
				} catch (IOException e) {
					logger.info("异常："+e.getMessage());
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			} else
				retFlag = false;
        }
        
        return retFlag;
    }
	
	
	//生成导出EXCEL文件(读取数据方式改变（优化，调用hive效率更高），支持6w以上导出，6w以上分多个sheet)
	public static boolean expCaDishSupDetsExcelTwo(String pathFileName, List<CaDishSupDets> dataList, String colNames[]) { 
		logger.info("进入expCaDishSupDetsExcelTwo方法");
		boolean retFlag = true;
		Workbook wb = null;
        String excelPath = pathFileName, fileType = "";
        File file = new File(excelPath);
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
            	 Map<Integer, List<CaDishSupDets>> sheetMap = RelectUtil.<CaDishSupDets>daData(dataList);
                 Set<Integer> keys = sheetMap.keySet();
                 Sheet sheet = null;
                 Row row = null;
                 Cell cell = null;
                 String[] colVals = new String[colNames.length];
         		 try {
         			int rowIndex=0;
         	        for (Iterator<Integer> iterator = keys.iterator(); iterator.hasNext();) {
         	            Integer SheetKey = iterator.next();
         	            sheet = wb.createSheet(("sheet"+(SheetKey+1)).toString());
         	            List<CaDishSupDets> sheetRows = sheetMap.get(SheetKey);
         	            rowIndex=0;
         	            
         				// 创建第一行（标题）
         				row = (Row) sheet.createRow(rowIndex++);
         				for (int i = 0; i < colNames.length; i++) {
         					cell = row.createCell(i);
         					try {
         						logger.info(colNames[i] + " ");
         						colVals[i] = new String(colNames[i].getBytes(), "utf-8");
         						cell.setCellValue(colNames[i]);
         					} catch (UnsupportedEncodingException e) {
         						logger.info("异常："+e.getMessage());
         						// TODO 自动生成的 catch 块
         						e.printStackTrace();
         					}
         				}
         				
         	            for (int i = 0, len = sheetRows.size(); i < len; i++) {
         	            	CaDishSupDets caDishSupDets = (CaDishSupDets) sheetRows.get(i);
         	            	row = sheet.createRow(rowIndex++);
         	            	//row.createCell(0).setCellValue(String.valueOf(i));
         					row.createCell(0).setCellValue(caDishSupDets.getRepastDate());                                         //用餐日期
         					row.createCell(1).setCellValue(caDishSupDets.getSchName());                                            //学校
         					row.createCell(2).setCellValue(AppModConfig.distIdToNameMap.get(caDishSupDets.getDistName()));         //区
         					row.createCell(3).setCellValue(caDishSupDets.getDetailAddr());                                         //详细地址
         					row.createCell(4).setCellValue(caDishSupDets.getSchType());                                            //学校学制
         					row.createCell(5).setCellValue(caDishSupDets.getSchProp());                                            //学校性质
         					row.createCell(6).setCellValue(caDishSupDets.getOptMode());                                            //供餐模式
         					row.createCell(7).setCellValue(caDishSupDets.getDispType());                                           //配送类型
         					row.createCell(8).setCellValue(caDishSupDets.getCaterType());                                          //餐别
         					row.createCell(9).setCellValue(caDishSupDets.getDishName());                                           //菜品名称          
         					row.createCell(10).setCellValue(caDishSupDets.getDishType());                                          //分类
         					row.createCell(11).setCellValue(caDishSupDets.getSupNum());                                            //份数
         					row.createCell(12).setCellValue(caDishSupDets.getMenuName());                                          //菜单名称
         					row.createCell(13).setCellValue(caDishSupDets.getRmcName());                                           //团餐公司
         	            }
         	        }
         		} catch (IllegalArgumentException e) {
         			logger.info("异常："+e.getMessage());
         			// TODO Auto-generated catch block
         		} catch (SecurityException e) {
         			logger.info("异常："+e.getMessage());
         			// TODO Auto-generated catch block
         			e.printStackTrace();
         		} 
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
        
        //文件流写入文件
        CommonUtil.commonExportExcel(retFlag, wb, excelPath);
        
        return retFlag;
    }
		
	//导出综合分析菜品供应明细列表模型函数
	public ExpCaDishSupDetsDTO appModFunc(String token, String repastStartDate, String repasEndDate, String schName, String distName, String prefCity, 
			String province, String dishName, String rmcName, String caterType, String schType, String schProp, String optMode, String menuName, 
			Db1Service db1Service, Db2Service db2Service, SaasService saasService,DbHiveService dbHiveService) {
		ExpCaDishSupDetsDTO ecdsdDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			if (repastStartDate == null || repasEndDate == null) {   // 按照当天日期获取数据
				repastStartDate = BCDTimeUtil.convertNormalDate(null);
				repasEndDate = repastStartDate;
			}
			//生成导出EXCEL文件
			String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
			String pathFileName = SpringConfig.base_dir + repFileName;
			logger.info("导出文件路径：" + pathFileName);
			boolean flag = false;
			if(methodIndex == 1) {
				List<CaDishSupDets> expExcelList = getDataListFromRedis(token, repastStartDate, repasEndDate, schName,
						distName, prefCity, province, dishName, rmcName, caterType, schType, schProp, optMode, menuName,
						db1Service, db2Service, saasService, dbHiveService, strCurPageNum, strPageSize);
				flag = expCaDishSupDetsExcel(pathFileName, expExcelList, colNames);
			}else if (methodIndex == 2) {
				List<CaDishSupDets> expExcelList = getDataListFromHive(token, repastStartDate, repasEndDate, schName,
						distName, prefCity, province, dishName, rmcName, caterType, schType, schProp, optMode, menuName,
						db1Service, db2Service, saasService, dbHiveService, strCurPageNum, strPageSize);
				flag = expCaDishSupDetsExcelTwo(pathFileName, expExcelList, colNames);
			}
			logger.info("flag" + flag);
			if(flag) {
				//移动文件到其他目录
				AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
				ecdsdDto = new ExpCaDishSupDetsDTO();
				ExpCaDishSupDets expCaDishSupDets = new ExpCaDishSupDets();
				//时戳
				ecdsdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
				//导出信息
				expCaDishSupDets.setRepastStartDate(repastStartDate);
				expCaDishSupDets.setRepasEndDate(repasEndDate);
				expCaDishSupDets.setSchName(schName);
				expCaDishSupDets.setDistName(distName);
				expCaDishSupDets.setPrefCity(prefCity);
				expCaDishSupDets.setProvince(province);
				expCaDishSupDets.setDishName(dishName);
				expCaDishSupDets.setRmcName(rmcName);
				expCaDishSupDets.setCaterType(caterType);
				expCaDishSupDets.setSchType(schType);
				expCaDishSupDets.setSchProp(schProp);
				expCaDishSupDets.setOptMode(optMode);
				expCaDishSupDets.setMenuName(menuName);
				expFileUrl = SpringConfig.repfile_srvdn + repFileName;
				logger.info("导出文件URL：" + expFileUrl);
				expCaDishSupDets.setExpFileUrl(expFileUrl);
				ecdsdDto.setExpCaDishSupDets(expCaDishSupDets);
				//消息ID
				ecdsdDto.setMsgId(AppModConfig.msgId);
				AppModConfig.msgId++;
				// 消息id小于0判断
				AppModConfig.msgIdLessThan0Judge();
			}else {
				
			}
		} else { // 模拟数据
			// 模拟数据函数
			ecdsdDto = SimuDataFunc();
		}

		return ecdsdDto;
	}
	
	/**
	 * 适合Redis取数的读取方法（效率更高）
	 * @param token
	 * @param repastStartDate
	 * @param repasEndDate
	 * @param schName
	 * @param distName
	 * @param prefCity
	 * @param province
	 * @param dishName
	 * @param rmcName
	 * @param caterType
	 * @param schType
	 * @param schProp
	 * @param optMode
	 * @param menuName
	 * @param db1Service
	 * @param db2Service
	 * @param saasService
	 * @param dbHiveService
	 * @param strCurPageNum
	 * @param strPageSize
	 * @return
	 */
	private List<CaDishSupDets> getDataListFromRedis(String token, String repastStartDate, String repasEndDate,
			String schName, String distName, String prefCity, String province, String dishName, String rmcName,
			String caterType, String schType, String schProp, String optMode, String menuName, Db1Service db1Service,
			Db2Service db2Service, SaasService saasService, DbHiveService dbHiveService, String strCurPageNum,
			String strPageSize) {
		List<CaDishSupDets> expExcelList = new ArrayList<>();
		CaDishSupDetsDTO cdsdDto = cdsdAppMod.appModFunc(token, repastStartDate, repasEndDate, schName, distName, prefCity, province, 
				dishName, rmcName, caterType, schType, schProp, optMode, menuName, 
				strCurPageNum, strPageSize, db1Service, db2Service, saasService,dbHiveService);
		if(cdsdDto != null) {
			int i, totalCount = cdsdDto.getPageInfo().getPageTotal();
			int pageCount = 0;
			if(totalCount % pageSize == 0)
				pageCount = totalCount/pageSize;
			else
				pageCount = totalCount/pageSize + 1;
			//第一页数据
			if(cdsdDto.getCaDishSupDets() != null) {
				expExcelList.addAll(cdsdDto.getCaDishSupDets());
			}
			//后续页数据
			for(i = curPageNum+1; i <= pageCount; i++) {
				strCurPageNum = String.valueOf(i);
				CaDishSupDetsDTO curPdlDto = cdsdAppMod.appModFunc(token, repastStartDate, repasEndDate, 
						schName, distName, prefCity, province, dishName, rmcName, caterType, 
						schType, schProp, optMode, menuName, strCurPageNum, strPageSize, 
						db1Service, db2Service, saasService,dbHiveService);
				if(curPdlDto.getCaDishSupDets() != null) {
					expExcelList.addAll(curPdlDto.getCaDishSupDets());
				}
			}
		}
		return expExcelList;
	}

	/**
	 * 适合从hive取数的读取方法（效率更高）
	 * @param token
	 * @param repastStartDate
	 * @param repasEndDate
	 * @param schName
	 * @param distName
	 * @param prefCity
	 * @param province
	 * @param dishName
	 * @param rmcName
	 * @param caterType
	 * @param schType
	 * @param schProp
	 * @param optMode
	 * @param menuName
	 * @param db1Service
	 * @param db2Service
	 * @param saasService
	 * @param dbHiveService
	 * @param strCurPageNum
	 * @param strPageSize
	 * @return
	 */
	private List<CaDishSupDets> getDataListFromHive(String token, String repastStartDate, String repasEndDate,
			String schName, String distName, String prefCity, String province, String dishName, String rmcName,
			String caterType, String schType, String schProp, String optMode, String menuName, Db1Service db1Service,
			Db2Service db2Service, SaasService saasService, DbHiveService dbHiveService, String strCurPageNum,
			String strPageSize) {
		List<CaDishSupDets> expExcelList = new ArrayList<>();
		CaDishSupDetsDTO cdsdDto = cdsdAppMod.appModFunc(token, repastStartDate, repasEndDate, schName, distName, prefCity, province, dishName, rmcName,
				caterType, schType, schProp, optMode, menuName, strCurPageNum, strPageSize, db1Service, db2Service, saasService,dbHiveService);
		if(cdsdDto != null) {
			int totalCount = cdsdDto.getPageInfo().getPageTotal();
			//int pageCount = 0;
			/*if(totalCount % pageSize == 0)
				pageCount = totalCount/pageSize;
			else
				pageCount = totalCount/pageSize + 1;
			//第一页数据
			if(cdsdDto.getCaDishSupDets() != null) {
				expExcelList.addAll(cdsdDto.getCaDishSupDets());
			}*/
			//后续页数据(取redis的写法，redis读取速度较快)
			/*for(i = curPageNum+1; i <= pageCount; i++) {
				strCurPageNum = String.valueOf(i);
				CaDishSupDetsDTO curPdlDto = cdsdAppMod.appModFunc(token, repastStartDate, repasEndDate, schName, distName, prefCity, province, 
						dishName, rmcName, caterType, schType, schProp, optMode, menuName, strCurPageNum, strPageSize, db1Service, db2Service, saasService,dbHiveService);
				if(curPdlDto.getCaDishSupDets() != null) {
					expExcelList.addAll(curPdlDto.getCaDishSupDets());
				}
			}*/
			
			//后续页数据(取hive的写法)
			strCurPageNum = String.valueOf(1);
			strPageSize = String.valueOf(totalCount);
			CaDishSupDetsDTO curPdlDto = cdsdAppMod.appModFunc(token, repastStartDate, repasEndDate, schName, distName, prefCity, province, 
					dishName, rmcName, caterType, schType, schProp, optMode, menuName, strCurPageNum, strPageSize, db1Service, db2Service, saasService,dbHiveService);
			if(curPdlDto.getCaDishSupDets() != null) {
				expExcelList = curPdlDto.getCaDishSupDets();
			}
		}
		return expExcelList;
	}
	
	public static void main(String[] args) {
		List <CaDishSupDets> expExcelList = new ArrayList<CaDishSupDets>();
    	for(int i = 0 ; i <80000;i++) {
    		CaDishSupDets caDishSupDets = new CaDishSupDets();
    		caDishSupDets.setSchName("学校"+i);
    		expExcelList.add(caDishSupDets);
    	}
    	
    	/*Map<Integer, List<CaDishSupDets>> sheetMap = RelectUtil.<CaDishSupDets>daData(expExcelList);
    	for(Map.Entry<Integer, List<CaDishSupDets>> entry: sheetMap.entrySet()) {
    		
    	}*/
    	
    	String[] colNames = {"用餐日期", "学校", "区", "详细地址", "学校学制", "学校性质", "供餐模式", "配送类型", "餐别", "菜品名称", "分类", "份数", "菜单名称", "团餐公司"};
    	expCaDishSupDetsExcelTwo("d:/eb2e530f0c364c49989cb4882700d484.xlsx", expExcelList, colNames);
	}
}