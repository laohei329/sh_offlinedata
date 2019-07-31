package com.tfit.BdBiProcSrvYgwcSchOmc.dto;

public class PageInfo {

    public PageInfo() {

    }

    public PageInfo(int curPageNum, int pageTotal) {
        this.curPageNum = curPageNum;
        this.pageTotal = pageTotal;
    }

    int pageTotal;
    int curPageNum;

    public int getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(int pageTotal) {
        this.pageTotal = pageTotal;
    }

    public int getCurPageNum() {
        return curPageNum;
    }

    public void setCurPageNum(int curPageNum) {
        this.curPageNum = curPageNum;
    }
}
