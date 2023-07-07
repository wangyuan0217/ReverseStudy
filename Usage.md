
## pyenv-win

### 下载
首先下载 https://github.com/pyenv-win/pyenv-win/archive/master.zip
并解压到本地

### 环境变量
* PYENV, PYENV_HOME ,PYENV_ROOT
变量值都是：D:\pyenv-win-master\pyenv-win\
* path新增  %PYENV%\bin  和   %PYENV%\shims
* 测试成功  pyenv --version

### 基本命令
```bash
pyenv install -l
pyenv install 3.9.9
pyenv uninstall 3.5.2
pyenv local 3.5.2
pyenv global 3.5.2
pyenv version
pyenv versions
pip ...
```



## Frida  

### 安装
```bash
# 安装最新frida
pip install frida-tools
# 或
pip install frida==12.8.0
pip install frida-tools==5.3.0

# adb shell查看手机CPU 架构类型
adb shell getprop ro.product.cpu.abi

# 根据系统架构在https://github.com/frida/frida/releases 下载对应的frida-server
adb push frida-server /data/local/tmp/
adb shell "chmod 755 /data/local/tmp/frida-server"
```

### 启动
```bash
# ./frida-server &
adb shell "/data/local/tmp/frida-server &"
# 后续新开一个shell...

# 关闭服务
ps | grep frida
kill -9 进程id


# 端口转发
adb forward tcp:27042 tcp:27042
adb forward tcp:27043 tcp:27043

# 查看手机启动的进程
frida-ps -U

#版本
frida --version 
```

### 基本使用

```bash
# -f 表示Spawn模式
frida -U -f com.xx.messenger -l hook.js --no-pause 

# Attach模式
frida -U com.android.chrome -l chrome.js 
```

### [代码块收集](/Frida/frida-note.md) 


## objection

### 安装
```bash
pip install -U objection
```

### 使用
```bash
objection -d -g package_name explore

# 查看内存中加载的module
memory list modules

# 查看库的导出函数
memory list exports libssl.so

# 内存堆搜索实例
android heap search instances 类名

# 调用实例的方法
android heap execute 实例ID 实例方法

# 查看当前可用的activity或者service
android hooking list activities/services

# 直接启动activity或者服务
android intent launch_activity/launch_service activity/服务

# 列出内存中所有的类
android hooking list classes

# 在内存中所有已加载的类中搜索包含特定关键词的类
android hooking search classes display

# 内存中搜索指定类的所有方法 
android hooking list class_methods 类名

# 在内存中所有已加载的类的方法中搜索包含特定关键词的方法
android hooking search methods display

hook类的方法（hook类里的所有方法/具体某个方法）
android hooking watch class 类名
android hooking watch class 类名 --dump-args --dump-backtrace --dump-return
android hooking watch class_method 方法名
# 可以直接hook到所有重载
android hooking watch class_method xxx.MainActivity.fun --dump-args --dump-backtrace --dump-return

```


## r0capture

```bash
python r0capture.py -U com.everysing.lysn -v -p lysn.pcap

python r0capture.py -U 韵达快递超市 -v -p yunda.pcap
```


## r0tracer
精简版 objection + Wallbreaker

```bash
# 推荐使用Frida14版本，并且将日志使用-o参数进行输出保存
frida -U -f com.r0ysue.example -l r0tracer.js  --no-pause -o saveLog5.txt


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

网络需设置为桥接模式

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

```bash
apktweak --embed --apk xxxx.apk --target assets/libjiagu.so
```



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


