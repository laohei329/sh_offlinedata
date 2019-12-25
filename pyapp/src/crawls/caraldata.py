import json
import requests
import urllib3
import time
from xlwt import Workbook

# page:表示页面       sale_c2_id：表示各个商品列表的参数

url = 'https://mallapi.yunshanmeicai.com/api/commodity/detail'
book = Workbook(encoding='utf-8')
sheet1 = book.add_sheet('1', cell_overwrite_ok=True)

# 蔬菜

def get_index(base_url, ssu_id, t):
    data = {
        '_ENV_': {
            'app_version': '2.6.4.1',
            'bssid': '35:78:43:44:76:50',
            'device_id': 'fa68b0b7-05a7-315c-94a8-7570b29a3199',
            'device_name': 'MuMu',
            'distribute_channel': 'huawei',
            'imei': '008796758452640',
            'lat': '31.222282',
            'lng': '121.462717',
            'mac': '02:00:00:00:00:00',
            'mno': '',
            'net': 'WIFI',
            'os_version': '6.0.1',
            'real_device_id': '008796758452640',
            'sn': '',
            'source': 'android',
            'ssid': '5xCDvPbtw'
        },
        'sku_id' : '',
        'ssu_id' : ssu_id,
        'area_id': '417',
        'city_id': '2',
        'password': '9bhMDENxZ1icSYcWEOU4Pg==',
        'phone': '15901669839',
        'registration_id': '160a3797c8485cf650a',
        'tickets': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiMmYxM2Q5NWY2NWUwMTg1YTJjMjFiNzY4ODliNmUwNmItMzptYWxsIiwiaWF0IjoxNTYxOTUyOTMyMTQyfQ.LK0CY3JAPnJLKMIjqFxZ32WLUSXc6fI4Gm-9-FCBY_W4Qz8kH7LtDeZ-v6tnIp7pGcYCsR-82lmvfNKrL1XBaU3xMpRzWl_Ikh94SGc09CNR7MH2tOkgQifquHG_5dDKSWz_zyzmZgwNJsVFsz1MOd8mXqdr9QUc0Y3E4ftACtc',
        'time_stamp': t,
        'utoken': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiMmYxM2Q5NWY2NWUwMTg1YTJjMjFiNzY4ODliNmUwNmItMzptYWxsIiwiaWF0IjoxNTYxOTUyOTMyMTQyfQ.LK0CY3JAPnJLKMIjqFxZ32WLUSXc6fI4Gm-9-FCBY_W4Qz8kH7LtDeZ-v6tnIp7pGcYCsR-82lmvfNKrL1XBaU3xMpRzWl_Ikh94SGc09CNR7MH2tOkgQifquHG_5dDKSWz_zyzmZgwNJsVFsz1MOd8mXqdr9QUc0Y3E4ftACtc'
    }
    urllib3.disable_warnings()

    headear_app = {
        'User-Agent': 'Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36'
    }

    response = requests.post(base_url, data=json.dumps(data),
                             headers=headear_app)

    result_json = json.loads(response.text)
    return result_json


def crawl_detail(ssu_id,sale_c1_name,sale_c2_name,i):
    print(i)
    date=time.strftime("%Y-%m-%d")
    now_time =time.strftime("%H:%M:%S")
    sheet1.write(0, 0, '日期')
    sheet1.write(0, 1, '时间')
    sheet1.write(0, 2, '一级分类')
    sheet1.write(0, 3, '二级分类')
    sheet1.write(0, 4, '品名')
    sheet1.write(0, 5, '品牌')
    sheet1.write(0, 6, '品种')
    sheet1.write(0, 7, '规格')
    sheet1.write(0, 8, '等级')
    sheet1.write(0, 9, '大小')
    sheet1.write(0, 10, '直径')
    sheet1.write(0, 11, '菜心颜色')
    sheet1.write(0, 12, '加工工艺')
    sheet1.write(0, 13, '去皮情况')
    sheet1.write(0, 14, '外皮颜色')
    sheet1.write(0, 15, '去跟情况')
    sheet1.write(0, 16, '水洗情况')
    sheet1.write(0, 17, '是否清真')
    sheet1.write(0, 18, '肥瘦比')
    sheet1.write(0, 19, '包装方式')
    sheet1.write(0, 20, '星标')
    sheet1.write(0, 21, '水域')
    sheet1.write(0, 22, '存储方式')
    sheet1.write(0, 23, '储藏方式')
    sheet1.write(0, 24, '其他')
    sheet1.write(0, 25, '规格1')
    sheet1.write(0, 26, '单价1')
    sheet1.write(0, 27, '价格1')
    sheet1.write(0, 28, '规格2')
    sheet1.write(0, 29, '单价2')
    sheet1.write(0, 30, '价格2')
    sheet1.write(0, 31, '规格3')
    sheet1.write(0, 32, '单价3')
    sheet1.write(0, 33, '价格3')
    sheet1.write(0, 34, '规格4')
    sheet1.write(0, 35, '单价4')
    sheet1.write(0, 36, '价格4')
    sheet1.write(0, 37, '规格5')
    sheet1.write(0, 38, '单价5')
    sheet1.write(0, 39, '价格5')
    sheet1.write(0, 40, '规格6')
    sheet1.write(0, 41, '单价6')
    sheet1.write(0, 42, '价格6')
    sheet1.write(0, 43, '配送服务')
    sheet1.write(0, 44, '售后服务')
    sheet1.write(0, 45, '支付方式')

    t = str(int(round(time.time() * 1000)))
    res = get_index(url, ssu_id, t)
    print(res)
    if('service_support' in res['data']):

        service_support=res['data']['service_support']
        sku_level = res['data']['sku_level']   #规格参数
        ssu_format_list = res['data']['ssu_format_list']  #价格参数
        name = res['data']['sku']['name']  # 品名
        index = 0
        sheet1.write(i, index + 0, date)
        sheet1.write(i, index + 1, now_time)
        sheet1.write(i, index + 2, sale_c1_name)
        sheet1.write(i, index + 3, sale_c2_name)
        sheet1.write(i, index + 4, name)
        for each in sku_level:
            if (each['name'] == '品牌'):
                sheet1.write(i, index + 5, each['value'])
            elif (each['name'] == '品种'):
                sheet1.write(i, index + 6, each['value'])
            elif (each['name'] == '规格'):
                sheet1.write(i, index + 7, each['value'])
            elif (each['name'] == '等级'):
                sheet1.write(i, index + 8, each['value'])
            elif (each['name'] == '大小'):
                sheet1.write(i, index + 9, each['value'])
            elif (each['name'] == '直径'):
                sheet1.write(i, index + 10, each['value'])
            elif (each['name'] == '菜心颜色'):
                sheet1.write(i, index + 11, each['value'])
            elif (each['name'] == '加工工艺'):
                sheet1.write(i, index + 12, each['value'])
            elif (each['name'] == '去皮情况'):
                sheet1.write(i, index + 13, each['value'])
            elif (each['name'] == '外皮颜色'):
                sheet1.write(i, index + 14, each['value'])
            elif (each['name'] == '去跟情况'):
                sheet1.write(i, index + 15, each['value'])
            elif (each['name'] == '水洗情况'):
                sheet1.write(i, index + 16, each['value'])
            elif (each['name'] == '是否清真'):
                sheet1.write(i, index + 17, each['value'])
            elif (each['name'] == '肥瘦比'):
                sheet1.write(i, index + 18, each['value'])
            elif (each['name'] == '包装方式'):
                sheet1.write(i, index + 19, each['value'])
            elif (each['name'] == '星标'):
                sheet1.write(i, index + 20, each['value'])
            elif (each['name'] == '水域'):
                sheet1.write(i, index + 21, each['value'])
            elif (each['name'] == '存储方式'):
                sheet1.write(i, index + 22, each['value'])
            elif (each['name'] == '储藏方式'):
                sheet1.write(i, index + 23, each['value'])
            elif (each['name'] == '其他'):
                sheet1.write(i, index + 24, each['value'])
        for each in ssu_format_list:
            unit_price = each['ssu_price'].split('¥')[1].split('/')[0]  # 单价
            ssu_format = each['ssu_format']  # 规格
            ssu_fp = each['ssu_fp']  # 重量
            price = float(unit_price) * int(ssu_fp)  #总价
            sheet1.write(i,index+25,ssu_format)
            sheet1.write(i, index + 26, unit_price)
            sheet1.write(i, index + 27, price)
            index +=3
        for each in service_support:
            if(each['title'] == '配送服务'):
                peisong=each['content'].split('\'>')[1].split('<')[0]+each['content'].split('\'>')[2].split('<')[0]
                sheet1.write(i,  43, peisong+'由美菜配送')
            elif(each['title'] == '售后服务'):
                salefuwu=each['content'].split('\'>')[1].split('<')[0]+each['content'].split('\'>')[2].split('<')[0]
                sheet1.write(i, 44, salefuwu + '内有质量问题可退')
            elif(each['title'] == '支付方式'):
                sheet1.write(i,  45, '在线支付：支付宝、微信、余额  货到付款：现金、支付宝、微信、银行卡')
        book.save('D:\\'+str(date)+'_app.xlsx')
        i += 1




