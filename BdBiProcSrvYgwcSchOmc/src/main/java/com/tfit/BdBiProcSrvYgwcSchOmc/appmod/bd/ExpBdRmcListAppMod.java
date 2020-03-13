package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.bd;

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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.BdRmcList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.BdRmcListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.ExpBdRmcList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.ExpBdRmcListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

//导出基础数据团餐公司列表应用模型
public class ExpBdRmcListAppMod {
	private static final Logger logger = LogManager.getLogger(ExpBdRmcListAppMod.class.getName());
	
	//基础数据团餐公司列表应用模型
	private BdRmcListAppMod brlAppMod = new BdRmcListAppMod();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	//报表文件资源路径
	String repFileResPath = "/expBdRmcList/";
	//导出列名数组
	String[] colNames = {"企业名称", "所在地", "详细地址", "电子邮箱", "联系人", "手机号", "质量负责人", "联系电话", "关联项目点", "证照名称", "证照图片", "经营单位", "许可证号", "发证日期", "有效日期", "统一社会信用代码", "证照图片", "经营单位", "经营范围", "注册地址", "详细地址", "注册资本", "法人代表", "发证机关", "发证日期", "有效日期", "食品卫生许可证", "证照图片", "经营单位", "许可证号", "发证日期", "有效日期", "运输许可证", "证照图片", "经营单位", "许可证号", "发证日期", "有效日期", "IOS认证证书", "证照图片", "经营单位", "许可证号", "发证日期", "有效日期"};
	
	//变量数据初始化
	String compName = null;
	String distName = null;
	String prefCity = null;
	String province = "上海市";
	String contact = null;
	String fblNo = null;
	String uscc = null;
	String regCapital = null;	
	String expFileUrl = "test1.txt";

	//模拟数据函数
	private ExpBdRmcListDTO SimuDataFunc() {
		ExpBdRmcListDTO ebrlDto = new ExpBdRmcListDTO();
		//设置返回数据
		ebrlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		ExpBdRmcList expBdRmcList = new ExpBdRmcList();
		//赋值
		expBdRmcList.setCompName(compName);
		expBdRmcList.setDistName(distName);
		expBdRmcList.setPrefCity(prefCity);
		expBdRmcList.setProvince(province);
		expBdRmcList.setContact(contact);
		expBdRmcList.setFblNo(fblNo);
		expBdRmcList.setUscc(uscc);
		expBdRmcList.setRegCapital(regCapital);
		expBdRmcList.setExpFileUrl(SpringConfig.repfile_srvdn+repFileResPath+expFileUrl);
		//设置模拟数据
		ebrlDto.setExpBdRmcList(expBdRmcList);
		//消息ID
		ebrlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		//消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ebrlDto;
	}
	
	//生成导出EXCEL文件
	public boolean expBdRmcListExcel(String pathFileName, List<BdRmcList> dataList, String colNames[]) { 
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
				row.createCell(0).setCellValue(dataList.get(i).getCompName());                                           //企业名称
				row.createCell(1).setCellValue(AppModConfig.distIdToNameMap.get(dataList.get(i).getLocation()));         //所在地
				row.createCell(2).setCellValue(dataList.get(i).getDetailAddr());                                         //详细地址
				row.createCell(3).setCellValue(dataList.get(i).getEmail());                                              //电子邮箱
				row.createCell(4).setCellValue(dataList.get(i).getContact());                                            //联系人
				row.createCell(5).setCellValue(dataList.get(i).getMobilePhone());                                        //手机号
				row.createCell(6).setCellValue(dataList.get(i).getQaLeader());                                           //质量负责人
				row.createCell(7).setCellValue(dataList.get(i).getContactNo());                                          //联系电话
				row.createCell(8).setCellValue(dataList.get(i).getRelPpNum());                                           //关联项目点
				row.createCell(9).setCellValue(dataList.get(i).getLicName());                                            //证照名称
				row.createCell(10).setCellValue(dataList.get(i).getLicPic());                                            //证照图片
				row.createCell(11).setCellValue(dataList.get(i).getLicOptUnit());                                        //经营单位
				row.createCell(12).setCellValue(dataList.get(i).getLicNo());                                             //许可证号
				row.createCell(13).setCellValue(dataList.get(i).getLicIssueDate());                                      //发证日期
				row.createCell(14).setCellValue(dataList.get(i).getLicValidDate());                                      //有效日期
				row.createCell(15).setCellValue(dataList.get(i).getUscc());                                              //统一社会信用代码
				row.createCell(16).setCellValue(dataList.get(i).getUsccPic());                                           //证照图片
				row.createCell(17).setCellValue(dataList.get(i).getUsccOptUnit());                                       //经营单位
				row.createCell(18).setCellValue(dataList.get(i).getOptScope());                                          //经营范围
				row.createCell(19).setCellValue(dataList.get(i).getRegAddr());                                           //注册地址
				row.createCell(20).setCellValue(dataList.get(i).getUdetailAddr());                                       //详细地址
				row.createCell(21).setCellValue(dataList.get(i).getRegCapital());                                        //注册资本
				row.createCell(22).setCellValue(dataList.get(i).getCorpRepName());                                       //法人代表
				row.createCell(23).setCellValue(dataList.get(i).getUcssIssueAuth());                                     //发证机关
				row.createCell(24).setCellValue(dataList.get(i).getUcssIssueDate());                                     //发证日期
				row.createCell(25).setCellValue(dataList.get(i).getUcssValidDate());                                     //有效日期
				row.createCell(26).setCellValue(dataList.get(i).getFhlName());                                           //食品卫生许可证
				row.createCell(27).setCellValue(dataList.get(i).getFhlPic());                                            //证照图片
				row.createCell(28).setCellValue(dataList.get(i).getFhlOptUnit());                                        //经营单位
				row.createCell(29).setCellValue(dataList.get(i).getFhlNo());                                             //许可证号
				row.createCell(30).setCellValue(dataList.get(i).getFhlIssueDate());                                      //发证日期
				row.createCell(31).setCellValue(dataList.get(i).getFhlValidDate());                                      //有效日期
				row.createCell(32).setCellValue(dataList.get(i).getTplName());                                           //运输许可证
				row.createCell(33).setCellValue(dataList.get(i).getTplNo());                                             //证照图片
				row.createCell(34).setCellValue(dataList.get(i).getTplOptUnit());                                        //经营单位
				row.createCell(35).setCellValue(dataList.get(i).getTplNo());                                             //许可证号
				row.createCell(36).setCellValue(dataList.get(i).getTplIssueDate());                                      //发证日期				
				row.createCell(36).setCellValue(dataList.get(i).getTplValidDate());                                      //有效日期
				row.createCell(36).setCellValue(dataList.get(i).getIosName());                                           //IOS认证证书
				row.createCell(36).setCellValue(dataList.get(i).getIosPic());                                            //证照图片
				row.createCell(36).setCellValue(dataList.get(i).getIosOptUnit());                                        //经营单位
				row.createCell(36).setCellValue(dataList.get(i).getIosNo());                                             //许可证号
				row.createCell(36).setCellValue(dataList.get(i).getIosIssueDate());                                      //发证日期
				row.createCell(36).setCellValue(dataList.get(i).getIosValidDate());                                      //有效日期
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
	
	//导出基础数据团餐公司列表模型函数
	public ExpBdRmcListDTO appModFunc(String token, String compName, String distName, String prefCity, String province, String contact, String fblNo, String uscc, String regCapital, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		ExpBdRmcListDTO ebrlDto = null;
		if (isRealData) { // 真实数据
			String strCurPageNum = String.valueOf(curPageNum), strPageSize = String.valueOf(pageSize);
			BdRmcListDTO brlDto = brlAppMod.appModFunc(token, compName, distName, prefCity, province, contact, fblNo, uscc, regCapital, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
			if(brlDto != null) {
				int i, totalCount = brlDto.getPageInfo().getPageTotal();
				int pageCount = 0;
				List<BdRmcList> expExcelList = new ArrayList<>();
				if(totalCount % pageSize == 0)
					pageCount = totalCount/pageSize;
				else
					pageCount = totalCount/pageSize + 1;
				//第一页数据
				if(brlDto.getBdRmcList() != null) {
					expExcelList.addAll(brlDto.getBdRmcList());
				}
				//后续页数据
				for(i = curPageNum+1; i <= pageCount; i++) {
					strCurPageNum = String.valueOf(i);
					BdRmcListDTO curPdlDto = brlAppMod.appModFunc(token, compName, distName, prefCity, province, contact, fblNo, uscc, regCapital, strCurPageNum, strPageSize, db1Service, db2Service, saasService);
					if(curPdlDto.getBdRmcList() != null) {
						expExcelList.addAll(curPdlDto.getBdRmcList());
					}
				}
				//生成导出EXCEL文件
				String repFileName = repFileResPath + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];
				String pathFileName = SpringConfig.base_dir + repFileName;
				logger.info("导出文件路径：" + pathFileName);
				boolean flag = expBdRmcListExcel(pathFileName, expExcelList, colNames);
				if(flag) {
					//移动文件到其他目录
					AppModConfig.moveFileToOtherFolder(pathFileName, SpringConfig.tomcatSrvDirs[1] + repFileResPath);
					ebrlDto = new ExpBdRmcListDTO();
					ExpBdRmcList expBdRmcList = new ExpBdRmcList();
					//时戳
					ebrlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
					//导出信息
					expBdRmcList.setCompName(compName);
					expBdRmcList.setDistName(distName);
					expBdRmcList.setPrefCity(prefCity);
					expBdRmcList.setProvince(province);
					expBdRmcList.setContact(contact);
					expBdRmcList.setFblNo(fblNo);
					expBdRmcList.setUscc(uscc);
					expBdRmcList.setRegCapital(regCapital);
					expFileUrl = SpringConfig.repfile_srvdn + repFileName;
					logger.info("导出文件URL：" + expFileUrl);
					expBdRmcList.setExpFileUrl(expFileUrl);
					ebrlDto.setExpBdRmcList(expBdRmcList);
					//消息ID
					ebrlDto.setMsgId(AppModConfig.msgId);
					AppModConfig.msgId++;
					// 消息id小于0判断
					AppModConfig.msgIdLessThan0Judge();
				}
			}
		} else { // 模拟数据
			// 模拟数据函数
			ebrlDto = SimuDataFunc();
		}

		return ebrlDto;
	}
}
