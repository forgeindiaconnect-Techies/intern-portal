import re
import urllib.request
import urllib.parse
import http.cookiejar

cj = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cj))
resp = opener.open('http://localhost:8080/login')
data = resp.read().decode('utf-8')
match = re.search(r'name="_csrf" value="([^"]+)"', data)
print('FOUND', bool(match))
if not match:
    raise SystemExit('CSRF token not found')

token = match.group(1)
body = urllib.parse.urlencode({'username': 'admin', 'password': 'admin123', '_csrf': token}).encode('utf-8')
req = urllib.request.Request('http://localhost:8080/login', data=body)
req.add_header('Content-Type', 'application/x-www-form-urlencoded')
try:
    r2 = opener.open(req)
    print('POST STATUS', r2.getcode())
    print('URL', r2.geturl())
    print(r2.read(500).decode('utf-8', 'ignore'))
except urllib.error.HTTPError as e:
    print('HTTPERR', e.code)
    print(e.read(500).decode('utf-8', 'ignore'))
