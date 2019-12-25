import json

import redis as redis
import requests
import urllib3
import time
from hdfs.client import Client
from xlwt import Workbook
import pyhdfs
import random


url = 'https://mallapi.yunshanmeicai.com/api/search/getsearchlistbyc2'
url2 = 'https://mallapi.yunshanmeicai.com/api/commodity/detail'
book = Workbook(encoding='utf-8')
sheet1 = book.add_sheet('1', cell_overwrite_ok=True)


# 蔬菜
def sale_id(sale_c1_id):
    if (sale_c1_id == '1'):
        sale_c2_id = {'9': '叶菜类', '10': '茄果类', '11': '根茎类', '12': '豆类', '13': '葱姜蒜', '14': '根茎类', '16': '特菜'}
    elif (sale_c1_id == '2'):
        sale_c2_id = {'218': '苹果', '219': '梨', '220': '柑橘橙柚', '221': '香蕉', '222': '葡提', '224': '瓜', '226': '桃李',
                      '227': '菠萝'}
    elif (sale_c1_id == '3'):
        sale_c2_id = {'401': '品牌鲜猪肉', '410': '品牌牛羊肉', '17': '猪分割肉', '171': '猪五花肉', '25': '猪排骨类', '30': '猪副产品',
                      '204': '肉馅/丝/片', '205': '整鸡', '206': '分割鸡产品', '207': '鸡副产品', '186': '鸡腿', '185': '鸡胸',
                      '22': '鸭肉类',
                      '413': '鲜牛肉类', '416': '冻牛肉类', '20': '羊肉类', '212': '调理类', '213': '熟食类', '214': '腌制类', '260': '半成品'}
    elif (sale_c1_id == '338'):
        sale_c2_id = {'341': '熟食类', '344': '香肠火腿', '347': '禽料类包', '350': '肉类料包', '356': '其它料包', '371': '鲜面制品',
                      '362': '包子馒头',
                      '365': '粉面水饺', '368': '油条饼类', '374': '米糕制品', '377': '其它面点', '383': '其它', '359': '酒店菜',
                      '380': '汤粥类'}
    elif (sale_c1_id == '302'):
        sale_c2_id = {'311': '鱼糜火锅类', '314': '肉糜火锅类', '317': '火锅辅料', '332': '西快小吃', '326': '速冻蔬菜', '323': '西式料理',
                      '320': '烘焙原料',
                      '329': '调味酱汁', '377': '其它面点', '335': '其它'}
    elif (sale_c1_id == '4'):
        sale_c2_id = {'47': '鲜活/冰鲜', '386': '虾/虾制品', '208': '冷冻鱼/蛙类', '168': '海带/海蜇', '168': '海带/海蜇', '389': '贝壳蟹类',
                      '392': '水产干货',
                      '166': '鱿鱼/海参', '395': '韩餐日料', '398': '应季热销', '165': '虾蟹蛙贝', '377': '其它面点', '71': '其它冻货'}
    elif (sale_c1_id == '137'):
        sale_c2_id = {'23': '鲜蛋', '170': '加工蛋', '199': '豆制品', '200': '新鲜面条', '201': '鲜制面点', '348': '烘焙面包'}
    elif (sale_c1_id == '6'):
        sale_c2_id = {'33': '食用油', '32': '大米', '34': '面粉', '35': '杂粮', '37': '烘焙佐料'}
    elif (sale_c1_id == '7'):
        sale_c2_id = {'187': '碳酸饮料', '188': '茶饮料', '189': '果汁', '55': '牛奶乳品', '54': '饮用水', '190': '功能饮料', '44': '啤酒',
                      '50': '白酒',
                      '196': '冲调饮品', '162': '休闲食品'}
    elif (sale_c1_id == '5'):
        sale_c2_id = {'242': '美菜自有品牌', '42': '调味品', '195': '调味酱', '194': '调味汁', '46': '酱油醋', '40': '干货', '45': '粉丝粉条',
                      '43': '腌菜罐头', '44': '西餐专区'}
    elif (sale_c1_id == '8'):
        sale_c2_id = {'192': '一次性用品', '59': '纸品湿巾', '58': '清洁消毒', '60': '厨房用具', '698': '纸品湿巾', '702': '园形餐盒',
                      '706': '方形餐盒', '710': '多格餐盒',
                      '714': '纸碗餐盒', '718': '酱料杯', '722': '筷子餐具包', '730': '刀叉勺', '734': '杯子吸管', '738': '塑料袋',
                      '742': '锡纸保鲜膜', '746': '清洁工具',
                      '758': '家电五金', '762': '文体办公', '766': '软百服饰'}
    elif (sale_c1_id == '140'):
        sale_c2_id = {'175': '肉类料包', '178': '预制菜'}

    return sale_c2_id

def get_index2(base_url, ssu_id, t):
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
        'tickets': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiYzJlM2NiOGYyMTJjNjk1ZWIwZGI3YzZiYzJhMTRjMGQtMzptYWxsIiwiaWF0IjoxNTcwODYyNzY3NzUwfQ.oL2aDHYXQUEkhkrH2DQ8T5oIPEK-ICBac6m7ZrrXAxFO6QCf4uloXJpRfRDVvduRw3hZUCEuxYw7fG9MWnpXSACdw16YzUfj7lhGXugxPioCtTedg69VgAcKOL5bIHcF6qMZ7XZyT02igSr_TwkGhvAJFgorGbdPxOtPO7XU_eA',
        'time_stamp': t,
        'utoken': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiYzJlM2NiOGYyMTJjNjk1ZWIwZGI3YzZiYzJhMTRjMGQtMzptYWxsIiwiaWF0IjoxNTcwODYyNzY3NzUwfQ.oL2aDHYXQUEkhkrH2DQ8T5oIPEK-ICBac6m7ZrrXAxFO6QCf4uloXJpRfRDVvduRw3hZUCEuxYw7fG9MWnpXSACdw16YzUfj7lhGXugxPioCtTedg69VgAcKOL5bIHcF6qMZ7XZyT02igSr_TwkGhvAJFgorGbdPxOtPO7XU_eA'
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
    res = get_index2(url2, ssu_id, t)
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
        'tickets': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiYzJlM2NiOGYyMTJjNjk1ZWIwZGI3YzZiYzJhMTRjMGQtMzptYWxsIiwiaWF0IjoxNTcwODYyNzY3NzUwfQ.oL2aDHYXQUEkhkrH2DQ8T5oIPEK-ICBac6m7ZrrXAxFO6QCf4uloXJpRfRDVvduRw3hZUCEuxYw7fG9MWnpXSACdw16YzUfj7lhGXugxPioCtTedg69VgAcKOL5bIHcF6qMZ7XZyT02igSr_TwkGhvAJFgorGbdPxOtPO7XU_eA',
        'time_stamp': t,
        'utoken': 'jwt:eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjI5MDU5NTUzIiwianRpIjoiYzJlM2NiOGYyMTJjNjk1ZWIwZGI3YzZiYzJhMTRjMGQtMzptYWxsIiwiaWF0IjoxNTcwODYyNzY3NzUwfQ.oL2aDHYXQUEkhkrH2DQ8T5oIPEK-ICBac6m7ZrrXAxFO6QCf4uloXJpRfRDVvduRw3hZUCEuxYw7fG9MWnpXSACdw16YzUfj7lhGXugxPioCtTedg69VgAcKOL5bIHcF6qMZ7XZyT02igSr_TwkGhvAJFgorGbdPxOtPO7XU_eA'
    }
    urllib3.disable_warnings()

    headear_app = {
        'User-Agent': 'Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/52.0.2743.100 Mobile Safari/537.36'
    }

    s =requests.session()
    s.keep_alive =False
    ips= proxy()
    ip = random.choice(ips)
    print(ip)
    response = requests.post(base_url, data=json.dumps(data),
                             headers=headear_app,proxies={'http':ip})

    result_json = json.loads(response.text)
    return result_json

def proxy():
    redis_db = redis.Redis(host='172.16.1.183', port='6379')
    a = redis_db.zrevrange("proxies", 0, 100)
    ip_list = []
    for x in a:
        ip_list.append(str(x).split("'")[1])
    return ip_list


if __name__ == '__main__':
    #sale_c1 = {'1': '新鲜蔬菜', '2': '时令水果', '3': '肉禽类', '338': '方便速食', '302': '加工调理', '4': '新鲜水产', '137': '蛋品豆面',
    #           '6': '米面粮油', '7': '休闲酒饮', '5': '调料干货', '8': '餐厨用品', '140': '方便菜'}
    sale_c1 = {'1': '新鲜蔬菜', '2': '时令水果', '3': '肉禽类', '4': '新鲜水产', '137': '蛋品豆面',
               '6': '米面粮油', '7': '休闲酒饮', '5': '调料干货'}
    fs = pyhdfs.HdfsClient(hosts=['172.16.10.17:50070', '172.16.10.36:50070', '172.16.10.51:50070'], user_name='ops')
    date = time.strftime("%Y-%m-%d")
    i = 1
    for k in sale_c1.keys():
        sale_c1_name = sale_c1[k]
        sale_c2 = sale_id(k)
        for k2 in sale_c2.keys():
            time.sleep(2)
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
    fs.copy_from_local('/opt/crawldata/' + str(date) + '_app.xlsx', '/crawldata/app/' + str(date) + '_app.xlsx')