from urllib.parse import urlencode
import requests
from pyquery import PyQuery as pq
import xlrd
from xlutils.copy import copy
import datetime
import time
import os
import xlwt
import pyhdfs

url = 'http://www.shncp.com/Price_Info2.aspx'


def get_index(base_url, page, date):
    data = {
        'page': page,
        'col_id': 0,
        'keystr': '',
        'sdate': date
    }
    data = urlencode(data)
    response = requests.get(base_url, params=data)
    page_str = response.text
    return page_str


if __name__ == '__main__':
    date_str = time.strftime("%Y-%m-%d")
    n = datetime.date(*map(int, date_str.split('-')))
    x = datetime.timedelta(days=-1)
    date_old = (n + x).strftime('%Y-%m-%d')
    year = str(datetime.date(*map(int, date_str.split('-'))).year)
    date = year + '-' + str(datetime.date(*map(int, date_str.split('-'))).month) + '-' + str(
        datetime.date(*map(int, date_str.split('-'))).day)
    fs = pyhdfs.HdfsClient(hosts=['172.16.10.17:50070', '172.16.10.36:50070', '172.16.10.51:50070'], user_name='ops')
    if os.path.exists('/opt/crawldata/' + year + '_shnong.xls') == True:
        workbook = xlrd.open_workbook('/opt/crawldata/' + year + '_shnong.xls')  # 打开工作簿
        sheets = workbook.sheet_names()  # 获取所有表格
        worksheet = workbook.sheet_by_name(sheets[0])  # 获取工作簿中所有表格中的的第一个表格
        rows_old = worksheet.nrows  # 获取表格中已存在数据的行数
        new_workbook = copy(workbook)  # 将xlrd对象拷贝转化为xlwt对象
        new_worksheet = new_workbook.get_sheet(0)  # 获取转化后工作簿中的第一个表格

        for page in range(1, 20):
            page_str = get_index(url, page, date_str)
            doc = pq(page_str)
            print(doc('.font-black > td').text())
            page_data = doc('.font-black > td').text().split(date)
            size = len(page_data)
            for it in range(0, size - 1):
                j = 0
                new_worksheet.write(rows_old, j, date)
                print(it)
                item_data = page_data[it].strip().split(' ')
                for item in item_data:
                    print(rows_old, j)
                    print(item)
                    new_worksheet.write(rows_old, j + 1, item)
                    j += 1
                rows_old += 1
            new_workbook.save('/opt/crawldata/' + year + '_shnong.xls')
        fs.delete('/crawldata/上农批发/' + year + '_shnong.xls')
        fs.copy_from_local('/opt/crawldata/' + year + '_shnong.xls', '/crawldata/上农批发/' + year + '_shnong.xls')
    else:
        workbook = xlwt.Workbook()
        sheet = workbook.add_sheet('1')
        i = 1
        sheet.write(0, 0, '日期')
        sheet.write(0, 1, '分类')
        sheet.write(0, 2, '品名')
        sheet.write(0, 3, '产地')
        sheet.write(0, 4, '最高价(元/kg)')
        sheet.write(0, 5, '平均价(元/kg)')

        for page in range(1, 20):
            page_str = get_index(url, page, date_str)
            doc = pq(page_str)
            page_data = doc('.font-black > td').text().split(date)
            size = len(page_data)
            for it in range(0, size - 1):
                j = 0
                sheet.write(i, j, date)
                print(it)
                item_data = page_data[it].strip().split(' ')
                for item in item_data:
                    print(i, j)
                    print(item)
                    sheet.write(i, j + 1, item)
                    j += 1
                i += 1
            workbook.save('/opt/crawldata/' + year + '_shnong.xls')
        fs.copy_from_local('/opt/crawldata/' + year + '_shnong.xls', '/crawldata/上农批发/' + year + '_shnong.xls')
