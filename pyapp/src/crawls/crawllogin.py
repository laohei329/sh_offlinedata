import json
import requests
import urllib3


# 模拟登陆的代码。拿取token值

url = 'https://mallapi.yunshanmeicai.com/api/auth/loginbypassword'

#salt_sign=4242AFE0EA7102AE45AC370283308355,47,1561446616033
# 加密函数

def get_index(base_url):
    data = {
        'phone': '15901669839',
        'password': '9bhMDENxZ1icSYcWEOU4Pg==',
        'device_id': 'fa68b0b7-05a7-315c-94a8-7570b29a3199',
        'source': 'android'
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
    print(get_index(url))

