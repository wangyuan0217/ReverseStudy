
## 目录
- [目录](#目录)
- [pyenv-win](#pyenv-win)
  - [下载](#下载)
  - [环境变量](#环境变量)
  - [基本命令](#基本命令)
- [Root Su](#root-su)
  - [Magisk安装](#magisk安装)
  - [Lsposed](#lsposed)
  - [magisk隐藏](#magisk隐藏)
  - [KernelSu](#kernelsu)
- [Frida](#frida)
  - [安装](#安装)
  - [启动](#启动)
  - [基本使用](#基本使用)
  - [代码块收集](#代码块收集)
- [Objection](#objection)
  - [安装](#安装-1)
  - [使用](#使用)
- [r0capture](#r0capture)
  - [使用](#使用-1)
- [r0tracer](#r0tracer)
  - [使用](#使用-2)
- [jnitrace](#jnitrace)
  - [安装](#安装-2)
  - [使用](#使用-3)
- [lamda](#lamda)
  - [安装](#安装-3)
  - [访问](#访问)
  - [抓包](#抓包)
- [ecapture](#ecapture)
- [Shizuku](#shizuku)
- [LsPatch](#lspatch)
- [Pixel 手机相关](#pixel-手机相关)
  - [解决时间同步](#解决时间同步)
  - [网络感叹号](#网络感叹号)
- [ADB](#adb)
  - [基本](#基本)
  - [am](#am)
  - [pm](#pm)
  - [pull \& push](#pull--push)
  - [logcat](#logcat)
  - [debuggable](#debuggable)
- [Smali](#smali)
- [Tips](#tips)


<!-- dex2c + 函数分离 + np控制流混淆 + 360加固 -->

## pyenv-win

### 下载
首先下载 https://github.com/pyenv-win/pyenv-win/archive/master.zip 并解压到本地

### 环境变量
* PYENV, PYENV_HOME ,PYENV_ROOT
  变量值都是：D:\pyenv-win-master\pyenv-win\
* path新增  %PYENV%\bin  和   %PYENV%\shims
* 测试成功  pyenv --version

### 基本命令
```bash
# 设置代理
# set HTTP_PROXY=127.0.0.1:7890
# set HTTPS_PROXY=127.0.0.1:7890
pyenv install -l
pyenv install 3.9.9
pyenv uninstall 3.5.2
pyenv local 3.5.2
pyenv global 3.5.2
pyenv version
pyenv versions
pip ...
```


## Root Su

### Magisk安装
* 解锁BL
* 下载对应的ROM，提取boot.img(解压没有的话用 https://mrzzoxo.lanzoub.com/b02plgdpi 提取)
* 安装magisk-app https://github.com/topjohnwu/Magisk/releases
* 在magisk-app中选择对应的boot.img进行修补，生成magisk_patched-版本_随机.img
* 手机重启到fastboot, 执行 fastboot flash boot magisk_patched_xxx.img；然后fastboot reboot即可完成
* (补救)如果刷入模块不兼容导致不能开机，fastboot flash boot boot.img

### Lsposed
* 下载 https://github.com/LSPosed/LSPosed/releases/latest
* Magisk – 设置 – 开启 Zygisk
* 打开magisk-app 从本地刷入并重启；lsposed中创建快捷方式;

### magisk隐藏
* 随机包名
* Shamiko:隐藏root；https://github.com/LSPosed/LSPosed.github.io/releases
  勾选zygisk，并从magisk刷入重启；
  - whitelist 在/data/adb/shamiko下新建文件whitelist重启即可；全局生效；
  - blacklist 默认；并结合 magisk-设置-配置排查列表 中配置完成对指定应用生效；
* 隐藏应用列表 https://github.com/Dr-TSNG/Hide-My-Applist/releases
  1. 在lsposed中勾选生效
  2. 模板管理 - 创建黑名单模块(要隐藏的app)
  3. 应用管理 - 选择app来隐藏对应的黑名单模板；
  4. 检测 - https://github.com/Dr-TSNG/ApplistDetector/releases/latest

### KernelSu
* https://github.com/tiann/KernelSU
* https://github.com/msnx/KernelSU-Pixel4XL



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
#需要root
adb push frida-server /data/local/tmp/
adb shell "chmod 755 /data/local/tmp/frida-server"
```

### 启动
```bash
# root
# ./frida-server &
adb shell "/data/local/tmp/frida-server &"
# 后续新开一个shell...

# 关闭服务
ps | grep frida
kill -9 进程id

# 端口转发(可选)
adb forward tcp:27042 tcp:27042
adb forward tcp:27043 tcp:27043

#版本
frida --version 

# 查看手机启动的进程
frida-ps -Uai
```

### 基本使用

```bash
# Spawn(-f)
frida -U -f com.xx.messenger -l hook.js

# Attach模式
frida -U com.android.chrome -l chrome.js 
```

### [代码块收集](/Frida/frida-note.md)


## Objection

### 安装
```bash
pip install -U objection
```

### 使用
```bash
objection -g com.xxx explore
objection -g com.xxx explore --startup-command "你要执行的oc命令"


android hooking get current_activity
#搜索类
android hooking search classes asvid.github.io.fridaapp
#搜索类的方法
android hooking search methods asvid.github.io.fridaapp MainActivity
#列出类的声明方法及其参数
android hooking list class_methods asvid.github.io.fridaapp.MainActivity

#hook 方法
android hooking watch class_method asvid.github.io.fridaapp.MainActivity.sum --dump-args --dump-backtrace --dump-return

#改变方法的返回值(只支持bool类型)
android hooking set return_value 包.类.方法  true

#sslpinning
android sslpinning disable  #屏蔽SSL 校验, 使得ssl pinning失效


memory list modules  # 查看内存中加载的库
memory list exports libssl.so  #查看库的导出函数
memory list exports libart.so --json /root/libart.json #将结果保存到json文件中
memory search --string --offsets-only #搜索内存
memory search "00 00 00 00 00 00 00"
```


## r0capture

### 使用
```bash
python r0capture.py -U 应用名称(frida新版本)/包名(frida老版本) -v -p log.pcap
```


## r0tracer
精简版 objection + Wallbreaker

### 使用
```bash
# 推荐使用Frida14版本，并且将日志使用-o参数进行输出保存
frida -U -f com.r0ysue.example -l r0tracer.js  --no-pause -o log.txt
```


## jnitrace

### 安装
```bash
pip install jnitrace
```

### 使用
```bash
#需先运行frida-server
jnitrace -l libxxxxxx.so com.xxxx.xxxx
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
startmitm.exe localhost
startmitm.exe 192.168.1.2
```

只抓具体应用
```bash
python -u startmitm.py 192.168.1.2:com.some.package
或
startmitm.exe 192.168.1.2:com.xxx.xxx
```

改包
```bash
python -u startmitm.py 192.168.1.2 -s a_http_modify.py
```


## ecapture

eCapture(旁观者): 基于eBPF技术实现TLS加密的明文捕获，无需CA证书。
https://github.com/gojue/ecapture

[下载二进制包](https://github.com/gojue/ecapture/releases)直接使用（需要root)

```
安装
adb push ecapture /data/local/tmp/
adb shell chmod 777 /data/local/tmp/ecapture
启动
adb shell: su
cd /data/local/tmp/
./ecapture tls
```


## Shizuku

Shizuku 可以帮助普通应用借助一个由 app_process 启动的 Java 进程直接以 adb 或 root 特权使用系统 API。

https://github.com/RikkaApps/Shizuku

https://shizuku.rikka.app/zh-hans/introduction/


## LsPatch
LSPosed框架的免Root 实现, 通过在目标APK中插入dex等整合Xposed API
https://github.com/LSPosed/LSPatch




## Pixel 手机相关

### 解决时间同步
```bash
adb shell "settings put global ntp_server pool.ntp.org"
```

### 网络感叹号
```bash
adb shell settings put global captive_portal_server www.google.cn
adb shell settings put global captive_portal_https_url https://www.google.cn/generate_204
adb shell settings put global captive_portal_mode 0

# 或者 adb shell
settings put global captive_portal_http_url https://www.google.cn/generate_204
settings put global captive_portal_https_url https://www.google.cn/generate_204
settings put global ntp_server 1.hk.pool.ntp.org
```


## ADB

更多请参考 [CCommand](https://github.com/xbdcc/CCommand)

### 基本

```bash
adb install -r(覆盖安装) xx.apk

#adb 查看当前activity
adb shell dumpsys window | findstr mCurrentF

```

### am
am命令是一个重要的调试工具，主要用于启动或停止服 务、发送广播、启动Activity等。在逆向过程中，往往在需要以 Debug模式启动App时会使用这个命令。 对应命令格式为adb shell am start-activity -D -N <包名>/<类 名>。

```bash
adb shell am start-activity -D -N com.example.network/.MainActivity
```

### pm

```bash
# 列出所 有安装的APK包名。
adb shell pm list packages

#adb 搜索手机上的包名 
adb shell pm list packages | findstr com.xx
#或者frida
frida-ps -Uai | findstr 包名
frida-ps -Uai | Select-String 包名/APP名
#或者objection
android hooking get current_activity
```

### pull & push

```bash
# 从手机中拉取信息到本地电脑上  本机路径为空则到当前文件夹下
adb pull <手机路径>   <本机路径>  

# 从本地电脑推送信息到手机上
adb push <本机路径>  <手机路径>  
```

### logcat

```bash
# 根据关键词过滤
adb logcat -e keyword

# 根据包名过滤
adb logcat *|find "com.kj.avde"

# 根据Tag过滤
adb logcat -s System.out

# 存储日志到本地（需先创建好log.txt）
adb logcat | findstr "com.kj.avd">D:\log.txt
```

### debuggable

- 重启失效

```bash
adb shell
su 
magisk resetprop ro.debuggable 1
stop;start; #一定要通过该方式重启
```


- 永久生效

先在magisk中安装 https://github.com/Magisk-Modules-Repo/MagiskHidePropsConf/releases 然后重启手机；


```bash
adb shell
props
# 选择 Edit MagiskHide props
# 在选择 ro.debuggable
# 最后查看 getprop ro.debuggable
# 测试重启查看 getprop ro.debuggable
```



## Smali

基本类型和基本语法

....



## Tips

- #### du -h * 查看此目录所有文件大小
- #### frida 代码提示：npm install --save @types/frida-gum
- #### grep -ril "MainActivity" .
  查看此目录脱下来的dex哪个包含指定字符串,可以查看脱下来的壳哪个是正确的。
- #### jadx -e xxx.apk 导出一个gradle项目

