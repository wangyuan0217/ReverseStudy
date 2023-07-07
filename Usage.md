
## pyenv-win

- [安装教程](https://my.oschina.net/zhenggao/blog/5585534)




## r0capture

* python r0capture.py -U com.everysing.lysn -v -p lysn.pcap

* python r0capture.py -U 韵达快递超市 -v -p yunda.pcap




## lamda

* 安装
  
adb shell

su

cd /data/local/tmp

sh x86_64.sh

或者直接magisk安装(带自启动)

* 访问 
  
ip:65000

ip:65000/fs/

* 抓包
* 
python -u startmitm.py 192.168.1.2
或 startmitm.ext 192.168.1.2

只抓具体应用
python -u startmitm.py 192.168.1.2:com.some.package

* 改包
python -u startmitm.py 192.168.1.2 -s a_http_modify.py




## TweakMe

过签 360 (so是指加固的壳的so，也就是app第一个加载的so)

apktweak --embed --apk xxxx.apk --target assets/libjiagu.so



## Pixel 手机相关

* 解决时间同步 adb shell "settings put global ntp_server pool.ntp.org"



