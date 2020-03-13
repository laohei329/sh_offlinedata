package com.tfit.BdBiProcSrvYgwcSchOmc.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;

import com.alibaba.fastjson.JSON;

/**
 * 常用的公共的逻辑处理方法
 * @author Administrator
 *
 */
public class CommonUtil {
	
	/**
	 * String 格式["",""……] 转换成List
	 * @param objects
	 * @return
	 */
	public static List<Object> changeStringToList(String objects) {
		List<Object> distNamesList = null;
		if(StringUtils.isNotEmpty(objects)) {
			distNamesList = (List<Object>)JSON.parse(objects);
		}
		return distNamesList;
	}	
	
	
	/**
	 * 文件流写入文件
	 * @param retFlag
	 * @param wb
	 * @param excelPath
	 * @return
	 */
	public static boolean commonExportExcel(boolean retFlag, Workbook wb, String excelPath) {
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
		return retFlag;
	}
	
	/**
	 * 根据开始日期、结束日期，获取开始日期和结束日期的年、月
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 开始年份、开始月份、结束年份、结束月份的数组
	 */
	public static String[]  getYearMonthByDate(String startDate, String endDate) {
		
		String[] yearMonths = new String [4];
		String [] startDates = startDate.split("-");
    	String [] endDates = endDate.split("-");
    	
    	String startYearTemp = "";
    	String startMonthTemp = "";
    	if(startDates.length>=2) {
    		startYearTemp = startDates[0];
    		startMonthTemp = startDates[1];
    	}else {
    		startYearTemp = BCDTimeUtil.convertNormalDate(null).split("-")[0];
    		startMonthTemp = BCDTimeUtil.convertNormalDate(null).split("-")[1];
    	}
    	
    	
    	String endYearTemp = "";
    	String endMonthTemp = "";
    	if(endDates.length>=2) {
    		endYearTemp = endDates[0];
    		endMonthTemp = endDates[1];
    	}else {
    		endYearTemp = BCDTimeUtil.convertNormalDate(null).split("-")[0];
    		endMonthTemp = BCDTimeUtil.convertNormalDate(null).split("-")[1];
    	}
    	
    	if(startYearTemp.indexOf("0")==0) {
    		startYearTemp = startYearTemp.replaceFirst("0", "");
    	}
    	
    	if(endYearTemp.indexOf("0")==0) {
    		endYearTemp.replaceFirst("0", "");
    	}
    	
    	if(startMonthTemp.indexOf("0")==0) {
    		startMonthTemp =startMonthTemp.replaceFirst("0", "");
    	}
    	
    	if(endMonthTemp.indexOf("0")==0) {
    		endMonthTemp = endMonthTemp.replaceFirst("0", "");
    	}
    	
    	yearMonths[0]=startYearTemp;
    	yearMonths[1]=startMonthTemp;
    	yearMonths[2]=endYearTemp;
    	yearMonths[3]=endMonthTemp;
    	
    	return yearMonths;
	}

	/**
	 * 指定日期加制定天数（返回值为yyyy-MM-dd格式）
	 * @param date yyyy-MM-dd格式时间
	 * @param addDay 增加的天数
	 * @return
	 */
	public static String dateAddDay(String date, int addDay) {
		String endDateAddOne;
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		endDateAddOne = df.format(new Date());
		try {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(df.parse(date));
			calendar.add(Calendar.DAY_OF_MONTH, addDay);//加一天
			endDateAddOne = df.format(calendar.getTime());
		} catch (ParseException e1) {
		}
		return endDateAddOne;
	}

	/**
	 * 获取指定开始年份、月份、结束年份月份之间所有的年份月份结合
	 * 每个值的格式为：year_startMonth_endMonth
	 * @param startYear 开始年份
	 * @param startMonth 开始月份
	 * @param endYear 结束年份
	 * @param endMonth 结束月份
	 * @return
	 */
	public static List<String> getYearMonthList(String startYear, String startMonth, String endYear, String endMonth) {
		//获取年份+月份集合，方便查询
		int iStartYear = Integer.parseInt(startYear);
		int iEndYear = Integer.parseInt(endYear);
		int iStartMonth = Integer.parseInt(startMonth);
		int iEndMonth = Integer.parseInt(endMonth);
		
		//year_startMonth_endMonth
		List<String> listYearMonth = new ArrayList<>();
		String strYearMonth = "";
		for(int year =iStartYear;year<=iEndYear;year++) {
			//年份和开始月份
			if(year ==iStartYear) {
				//第一年，开始月份等于开始时间的月份
				strYearMonth = year+"_"+iStartMonth;
			}else if (year > iStartYear) {
				//非第一年，开始月份等于1
				strYearMonth = year+"_"+1;
			}
			
			//结束月份
			if(year == iEndYear) {
				//如果是最后一年，则结束月份等于结束日期的月份
				strYearMonth +="_"+iEndMonth;
			}else {
				//如果不是最后一年，则结束月份为12月
				strYearMonth +="_"+12;
			}
			
			if(strYearMonth!=null && !"".equals(strYearMonth) && strYearMonth.split("_").length>=3) {
				listYearMonth.add(strYearMonth);
			}
		}
		return listYearMonth;
	}
	
	/**
	 * 判断是否是整数（包含负数）
	 * @param str
	 * @return
	 */
	public static boolean isInteger(String str){
	    Pattern pattern = Pattern.compile("-?[0-9]+?");
	    Matcher isNum = pattern.matcher(str);
	    if( !isNum.matches() ){
	        return false;
	    }
	    return true;
	}
	
	/**
	 * 判断是否是数字（包含小数、整数、正数、负数）
	 * @param str
	 * @return
	 */
	public static boolean isNumeric(String str){
	    Pattern pattern = Pattern.compile("-?[0-9]+(.[0-9]+)?");
	    Matcher isNum = pattern.matcher(str);
	    if( !isNum.matches() ){
	        return false;
	    }
	    return true;
	}


}
