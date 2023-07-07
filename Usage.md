
## pyenv-win

[安装教程](https://my.oschina.net/zhenggao/blog/5585534)


## r0capture

```bash
python r0capture.py -U com.everysing.lysn -v -p lysn.pcap

python r0capture.py -U 韵达快递超市 -v -p yunda.pcap
```


## lamda

### 安装
  
```bash
adb push arm64-v8a.tar.gz-install.sh /data/local/tmp
adb shell
su
cd /data/local/tmp
sh x86_64.sh
```
或者直接magisk安装(带自启动)

### 访问 
```bash
ip:65000
ip:65000/fs/
```

### 抓包
```bash
python -u startmitm.py 192.168.1.2
或 
startmitm.exe 192.168.1.2
```

只抓具体应用
```bash
python -u startmitm.py 192.168.1.2:com.some.package
```

改包
```bash
python -u startmitm.py 192.168.1.2 -s a_http_modify.py
```


## TweakMe

过签 360 (so是指加固的壳的so，也就是app第一个加载的so)

apktweak --embed --apk xxxx.apk --target assets/libjiagu.so



## Pixel 手机相关

解决时间同步 
```bash
adb shell "settings put global ntp_server pool.ntp.org"
```

网络感叹号  
```bash
adb shell settings put global captive_portal_server www.google.cn
adb shell settings put global captive_portal_https_url https://www.google.cn/generate_204
adb shell settings put global captive_portal_mode 0
```


