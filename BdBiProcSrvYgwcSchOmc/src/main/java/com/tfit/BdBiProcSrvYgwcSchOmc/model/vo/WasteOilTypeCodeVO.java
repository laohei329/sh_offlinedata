package com.tfit.BdBiProcSrvYgwcSchOmc.model.vo;

import com.tfit.BdBiProcSrvYgwcSchOmc.model.enums.WasteOilType;
import lombok.Data;

/**
 * @Descritpion：废弃油脂种类编码类表
 * @author: tianfang_infotech
 * @date: 2019/1/7 15:15
 */
@Data
public class WasteOilTypeCodeVO {

    public WasteOilTypeCodeVO() {

    }

    public WasteOilTypeCodeVO(WasteOilType wasteOilType) {
        this.name = wasteOilType.getName();
        this.code = wasteOilType.getCode();
    }

    public WasteOilTypeCodeVO(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    /**
     * 类型名称
     */
    private String name;

    /**
     * 类型编码
     */
    private Integer code;
}
