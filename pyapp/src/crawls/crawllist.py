import json
import requests
import urllib3
import time
from hdfs.client import Client


# page:表示页面       sale_c2_id：表示各个商品列表的参数
from src.config.saleid import sale_id
from src.crawls.caraldata import crawl_detail

url = 'https://mallapi.yunshanmeicai.com/api/search/getsearchlistbyc2'


# 蔬菜


def get_index(base_url, sale_c1_id, sale_c2_id, t, page):
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
        'page': page,
        'sale_c1_id': sale_c1_id,
        'sale_c2_id': sale_c2_id,
        'area_id': '417',
        'city_id': '2',
        'password': '9bhMDENxZ1icSYcWEOU4Pg==',
        'phone': '15901669839',
        'registration_id': '160a3797c8485cf650a',
        'tickets': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiNGQ2YTQ0ODk4M2FhNzY4NmY5OTgwNTIxMzJlNWExMzQtMzptYWxsIiwiaWF0IjoxNTcwODY3OTgzNzIwfQ.CJIWrtmDT8AMZpQQ-c_Sqwl6xJ0DP64Kk4C1S5ddwXSh3HpYX13_n2jqNO12iMm70Cinkdzsw9Fl8skmuRLUqlaukJit8jcR0mVW2BiGIYwOQSJac7-n3VK1ZN0NjzcR6GUTf6wHHM7C8jL8NyLgv90rrz5jWEEzmidZqHvv9hw',
        'time_stamp': t,
        'utoken': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiNGQ2YTQ0ODk4M2FhNzY4NmY5OTgwNTIxMzJlNWExMzQtMzptYWxsIiwiaWF0IjoxNTcwODY3OTgzNzIwfQ.CJIWrtmDT8AMZpQQ-c_Sqwl6xJ0DP64Kk4C1S5ddwXSh3HpYX13_n2jqNO12iMm70Cinkdzsw9Fl8skmuRLUqlaukJit8jcR0mVW2BiGIYwOQSJac7-n3VK1ZN0NjzcR6GUTf6wHHM7C8jL8NyLgv90rrz5jWEEzmidZqHvv9hw'
    }
    urllib3.disable_warnings()

    headear_app = {
        'User-Agent': 'Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36'
    }

    response = requests.post(base_url, data=json.dumps(data),
                             headers=headear_app)

    result_json = json.loads(response.text)
    return result_json


if __name__ == '__main__':
    sale_c1 = {'1': '新鲜蔬菜', '2': '时令水果', '3': '肉禽类', '338': '方便速食', '302': '加工调理', '4': '新鲜水产', '137': '蛋品豆面',
               '6': '米面粮油', '7': '休闲酒饮', '5': '调料干货', '8': '餐厨用品', '140': '方便菜'}
    #client = Client('hdfs://172.16.10.17:9000/crawldata/app/', root=None, proxy=None, timeout=None, session=None)
    date = time.strftime("%Y-%m-%d")
    i = 1
    for k in sale_c1.keys():
        sale_c1_name = sale_c1[k]
        sale_c2 = sale_id(k)
        for k2 in sale_c2.keys():
            sale_c2_name = sale_c2[k2]
            for page in range(1, 11):
                t = str(int(round(time.time() * 1000)))
                res = get_index(url, k, k2, t, page)
                print(res)
                rows = res['data']['rows']
                for each in rows:
                    ssu_id = each['ssu_list'][0]['ssu_id']
                    crawl_detail(ssu_id, sale_c1_name, sale_c2_name, i)
                    i += 1
    #client.upload('hdfs://172.16.10.17:9000/', '/opt/crawldata/'+str(date)+'_app.xlsx',cleanup=True)