package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.im;

import java.util.ArrayList;
import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.SchSelModeCodesDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//学校筛选模式编码列表应用模型
public class SchSelModeCodesAppMod {
	// 是否为真实数据标识
	private static boolean isRealData = true;

	// 数组数据初始化
	String[] name_Array = { "按主管部门", "按所在地" };
	String[] code_Array = { "0", "1" };
	
	// 模拟数据函数
	private SchSelModeCodesDTO SimuDataFunc() {
		SchSelModeCodesDTO ssmcDto = new SchSelModeCodesDTO();
		// 时戳
		ssmcDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 学校筛选模式编码列表模拟数据
		List<NameCode> schSelModeCodes = new ArrayList<>();
		// 赋值
		for (int i = 0; i < name_Array.length; i++) {
			NameCode ssmc = new NameCode();
			ssmc.setName(name_Array[i]);
			ssmc.setCode(code_Array[i]);
			schSelModeCodes.add(ssmc);
		}
		// 设置数据
		ssmcDto.setSchSelModeCodes(schSelModeCodes);
		// 消息ID
		ssmcDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ssmcDto;
	}

	// 学校筛选模式编码列表模型函数
	public SchSelModeCodesDTO appModFunc(String distName, String prefCity, String province, Db1Service db1Service) {
		SchSelModeCodesDTO ssmcDto = null;
		// 省或直辖市
		if (province == null)
			province = "上海市";
		if (isRealData) { // 真实数据
			ssmcDto = new SchSelModeCodesDTO();
			// 时戳
			ssmcDto.setTime(BCDTimeUtil.convertNormalFrom(null));
			// 学校筛选模式编码列表模拟数据
			List<NameCode> schSelModeCodes = new ArrayList<>();
			// 赋值
			for (int i = 0; i < name_Array.length; i++) {
				NameCode ssmc = new NameCode();
				ssmc.setName(name_Array[i]);
				ssmc.setCode(code_Array[i]);
				schSelModeCodes.add(ssmc);
			}
			// 设置数据
			ssmcDto.setSchSelModeCodes(schSelModeCodes);
			// 消息ID
			ssmcDto.setMsgId(AppModConfig.msgId);
			AppModConfig.msgId++;
			// 消息id小于0判断
			AppModConfig.msgIdLessThan0Judge();
		} else { // 模拟数据
			// 模拟数据函数
			ssmcDto = SimuDataFunc();
		}

		return ssmcDto;
	}
}