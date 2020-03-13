package com.tfit.BdBiProcSrvYgwcSchOmc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.CreatorDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SupplierIdDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TBaseMaterialDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TBaseMaterialSupplierDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TBaseMaterialTypeDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduCaterTypeDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduMenuGroupDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduMenuTemplateDishesDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSchoolSupplierDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProCategoryDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProDishesDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProLicenseDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProRecyclerSupplierDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProRecyclerWasteDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProReserveSampleDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProSupplierDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProWarningDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TSaasPackageDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.CreatorDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.SupplierIdDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TBaseMaterialDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TBaseMaterialSupplierDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TBaseMaterialTypeDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TEduCaterTypeDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TEduMenuGroupDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TEduMenuTemplateDishesDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TEduSchoolSupplierDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProCategoryDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProDishesDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProLicenseDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProRecyclerSupplierDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProRecyclerWasteDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProReserveSampleDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProSupplierDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TProWarningDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.TSaasPackageDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;

@Service
public class SaasServiceImpl implements SaasService {
	@Autowired
	SupplierIdDoMapper siMapper;
	
	@Autowired
	CreatorDoMapper creatorMapper;
	
	@Autowired
	TProSupplierDoMapper tpsMapper;
	
	@Autowired
	TEduSchoolSupplierDoMapper tessMapper;
	
	@Autowired
	TProWarningDoMapper tpwMapper;
	
	@Autowired
	TProRecyclerWasteDoMapper tprwMapper;
	
	@Autowired
	TEduMenuGroupDoMapper temgMapper;
	
	@Autowired
	TProRecyclerSupplierDoMapper tprsMapper;
	
	@Autowired
	TProReserveSampleDoMapper tpresMapper;
	
	@Autowired
	TSaasPackageDoMapper tspMapper;
	
	@Autowired
	TBaseMaterialTypeDoMapper tbmtMapper;
	
	@Autowired
	TBaseMaterialDoMapper tbmMapper;
	
	@Autowired
	TEduMenuTemplateDishesDoMapper temtdMapper;
	
	@Autowired
	TProDishesDoMapper tpdMapper;
	
	@Autowired
	TProCategoryDoMapper tpcMapper;
	
	@Autowired
	TEduCaterTypeDoMapper tectMapper;
	
	@Autowired
	TBaseMaterialSupplierDoMapper tbmsMapper;
	
	@Autowired
	TProLicenseDoMapper tplMapper;
	
	//获取所有供应商
	public List<SupplierIdDo> getAllSupplierId() {
		return siMapper.getAllSupplierId();
	}
	
	//以供应商获取创建者
	public List<CreatorDo> getCreatorBySupplierId(String strSupplierId) {
		return creatorMapper.getCreatorBySupplierId(strSupplierId);
	}
	
	//从表t_pro_supplier中获取供应商id和供应商名称
	public List<TProSupplierDo> getIdSupplierIdName() {
		return tpsMapper.getIdSupplierIdName();
	}
	
	//从表t_pro_supplier中获取团餐公司id和团餐公司名称
	public List<TProSupplierDo> getRmcIdName() {
		return tpsMapper.getRmcIdName();
	}
	
	//从表t_pro_supplier中获取原料供应商id和原料供应商名称
	public List<TProSupplierDo> getAllMatSupplierIdName() {
		return tpsMapper.getAllMatSupplierIdName();
	}
	
	//从表t_edu_school_supplier中获取供应商id和学校名称
    public List<TEduSchoolSupplierDo> getSupplierIdSchoolName() {
    	return tessMapper.getSupplierIdSchoolName();
    }
    
    //学校名称映射团餐公司名称
    public Map<String, String> getSchoolNameToRmcNameMap() {
    	Map<String, String> schNameToSupplerNameMap = new HashMap<>(), schNameToSupplerIdMap = new HashMap<>(), SupplerIdToSupplierNameMap = new HashMap<>();
    	//获取团餐公司id和学校名称并映射
    	List<TEduSchoolSupplierDo> tesDoList = getSupplierIdSchoolName();
    	if(tesDoList != null) {
    		for(int i = 0; i < tesDoList.size(); i++)
    			schNameToSupplerIdMap.put(tesDoList.get(i).getSchoolName(), tesDoList.get(i).getSupplierId());
    	}
    	//获取团餐公司id和团餐公司名称并映射
    	List<TProSupplierDo> tpsDoList = getRmcIdName();
    	if(tpsDoList != null) {
    		for(int i = 0; i < tpsDoList.size(); i++) {
    			SupplerIdToSupplierNameMap.put(tpsDoList.get(i).getId(), tpsDoList.get(i).getSupplierName());
    		}
    	}
    	//计算学校名称映射团餐公司名称
    	for(String curKey : schNameToSupplerIdMap.keySet()) {
    		String supplerId = schNameToSupplerIdMap.get(curKey);
    		String supplerName = SupplerIdToSupplierNameMap.get(supplerId);
    		schNameToSupplerNameMap.put(curKey, supplerName);
    	}
    	
    	return schNameToSupplerNameMap;
    }
    
    //获取证照预警类型（表名：t_pro_warning）
  	public List<TProWarningDo> getLicWarnType() {
  		return tpwMapper.getLicWarnType();
  	}
  	
  	//从表t_edu_school_supplier中获取供应商id以学校id
  	public TEduSchoolSupplierDo getSupplierIdSchoolId(String schoolId) {
    	return tessMapper.getSupplierIdSchoolId(schoolId);
    }
  	
  	//从表t_pro_supplier中获取供应商名称以供应商id
  	public TProSupplierDo getSupplierNameBySupplierId(String supplierId) {
  		return tpsMapper.getSupplierNameBySupplierId(supplierId);
  	}
  	
  	//从表t_edu_school_supplier中获取学校id和供应商id
    public List<TEduSchoolSupplierDo> getAllSupplierIdSchoolId() {
    	return tessMapper.getAllSupplierIdSchoolId();
    }
    
    //从表t_edu_school_supplier中获取主键id、学校id和供应商id
    public List<TEduSchoolSupplierDo> getAllIdSupplierIdSchoolId(Integer stat ) {
    	return tessMapper.getAllIdSupplierIdSchoolId(stat);
    }
    
    //获取回收单位ID和名称
    public List<TProRecyclerSupplierDo> getAllRecyclerIdName() {    	
  		return tprsMapper.getAllRecyclerIdName();
  	}
    
    //获取回收人名称
    public List<String> getAllRecPersonName() {
    	List<String> recPersonList = null;
    	Map<String, Integer> recPersonNameToFlagMap = new HashMap<>();
    	List<TProRecyclerSupplierDo> tprsDoList = tprsMapper.getAllRecPersonName();
    	List<TProRecyclerWasteDo> tprwDoList = tprwMapper.getAllRecPersonName();
    	if(tprsDoList != null) {
    		for(int i = 0; i < tprsDoList.size(); i++) {
    			if(!recPersonNameToFlagMap.containsKey(tprsDoList.get(i).getContacts()))
    				recPersonNameToFlagMap.put(tprsDoList.get(i).getContacts(), 1);
    		}
    	}
    	if(tprwDoList != null) {
    		for(int i = 0; i < tprwDoList.size(); i++) {
    			if(!recPersonNameToFlagMap.containsKey(tprwDoList.get(i).getContact()))
    				recPersonNameToFlagMap.put(tprwDoList.get(i).getContact(), 1);
    		}    			
    	}
    	if(recPersonNameToFlagMap.size() > 0) {
    		recPersonList = new ArrayList<>();
    		for(String curKey : recPersonNameToFlagMap.keySet()) {
    			recPersonList.add(curKey);
    		}
    	}
  		return recPersonList;
  	}
    
    //获取所有菜单组ID和名称
    public List<TEduMenuGroupDo> getAllMenuGroupIdName() {
    	return temgMapper.getAllMenuGroupIdName();
    }
    
    //获取留样单位
    public List<TProReserveSampleDo> getAllRsUnits() {
  		return tpresMapper.getAllRsUnits();
  	}
    
    //获取所有菜单组名称
    public List<TSaasPackageDo> getAllMenuGroupName() {
  		return tspMapper.getAllMenuGroupName();
  	}
    
    //获取所有物料分类
 	public List<TBaseMaterialTypeDo> getAllMatClassifyIdName() {
 		return tbmtMapper.getAllMatClassifyIdName();
 	}
 	
 	//获取所有物料名称
 	public List<TBaseMaterialDo> getAllMatNames() {
 		return tbmMapper.getAllMatNames();
 	}
 	
 	//获取所有物料名称2
 	public List<TBaseMaterialSupplierDo> getAllMatNames2() {
 		return tbmsMapper.getAllMatNames();
 	}
 	
 	//获取所有菜品类别
 	public List<TEduMenuTemplateDishesDo> getAllDishTypes() {
 		return temtdMapper.getAllDishTypes();
 	}
 	
 	//获取所有菜品名称
 	public List<TProDishesDo> getAllDishNames() {
 		return tpdMapper.getAllDishNames();
 	}
 	
 	//获取所有菜品类别2
 	public List<TProCategoryDo> getAllDishTypes2() {
 		return tpcMapper.getAllDishTypes();
 	}
 	
 	//获取所有餐别类型名称
 	public List<TEduCaterTypeDo> getAllCaterTypeNames() {
 		return tectMapper.getAllCaterTypeNames();
 	}
 	
 	//获取证照信息以供应商id和证照类型（0:餐饮服务许可证 1:食品经营许可证 2:食品流通许可证 3:食品生产许可证 4:营业执照(事业单位法人证书) 5：组织机构代码(办学许可证) 6：税务登记证 7:检验检疫合格证；8：ISO认证证书；9：身份证 10：港澳居民来往内地通行证 11：台湾居民往来内地通行证 12：其他; 13:食品卫生许可证 14:运输许可证 15:其他证件类型A 16:其他证件类型B 17:军官证 20:员工健康证；21：护照  22:A1证  23:B证  24:C证 25:A2证）
 	public TProLicenseDo getLicenseInfoBySupplierIdLicType(String supplierId, int licType) {
 		return tplMapper.getLicenseInfoBySupplierIdLicType(supplierId, licType);
 	}
 	
}
