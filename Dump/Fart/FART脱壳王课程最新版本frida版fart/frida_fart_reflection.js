//typed by hanbingle,just for fun!!
//email:edunwu@gmail.com
//Android 7,7.1,8,8.1 版本
/*使用说明
首先拷贝fart.so和fart64.so到/data/app目录下，并使用chmod 777 设置好权限,然后就可以使用了。
该frida版fart是利用反射的方式实现的函数粒度的脱壳，与使用hook方式实现的方法不同,可以使用spawn和attach两种方式使用。
使用方式1、以spawn方式启动app，等待app进入Activity界面后，执行fart()函数即可
使用方式2、app启动后，使用frida -UF直接attach上进程，执行fart()函数即可
高级用法：可以调用dumpclass(classname),传入要处理的类名，只完成对某一个类下的所有函数的CodeItem完成dump，效率更高，dump下来的类函数的所有CodeItem在含有类名的bin文件中。
* */
var addrGetDexFile = null;
var funcGetDexFile = null;
//android8.0,8.1
var addrGetObsoleteDexCache = null;
var addrGetCodeItemLength = null;
var funcGetCodeItemLength = null;
var addrBase64_encode = null;
var funcBase64_encode = null;
var addrFreeptr = null;
var funcFreeptr = null;
//默认直接保存到sd卡目录下，因此需要授予sd卡读写权限
var savepath = "/sdcard";
var version = 0;
var dexfilemaps = {};

function DexFile(start, size) {
    this.start = start;
    this.size = size;
}

function ArtMethod(dexfile, artmethodptr) {
    this.dexfile = dexfile;
    this.artmethodptr = artmethodptr;
}

function getHandle(object) {
    var handle = null;
    try {
        handle = object.$handle;
    } catch (e) {
    }
    if (handle == null) {
        try {
            handle = object.$h;
        } catch (e) {
        }

    }
    if (handle == null) {
        try {
            handle = object.handle;
        } catch (e) {
        }

    }
    return handle;
}

function dumpcodeitem(methodname, artmethodobj, fileflag) {
    if (artmethodobj != null) {
        var dexfileobj = artmethodobj.dexfile;
        var dexfilebegin = dexfileobj.start;
        var dexfilesize = dexfileobj.size;
        var dexfile_path = savepath + "/" + dexfilesize + "_" + Process.getCurrentThreadId() + ".dex";
        var dexfile_handle = null;
        try {
            dexfile_handle = new File(dexfile_path, "r");
            if (dexfile_handle && dexfile_handle != null) {
                dexfile_handle.close()
            }

        } catch (e) {
            dexfile_handle = new File(dexfile_path, "a+");
            if (dexfile_handle && dexfile_handle != null) {
                var dex_buffer = ptr(dexfilebegin).readByteArray(dexfilesize);
                dexfile_handle.write(dex_buffer);
                dexfile_handle.flush();
                dexfile_handle.close();
                console.error("[dumpdex]:", dexfile_path);
            }
        }
        var artmethodptr = artmethodobj.artmethodptr;
        var dex_code_item_offset_ = Memory.readU32(ptr(artmethodptr).add(8));
        var dex_method_index_ = Memory.readU32(ptr(artmethodptr).add(12));
        if (dex_code_item_offset_ != null && dex_code_item_offset_ > 0) {
            var dir = savepath;
            var file_path = dir + "/" + dexfilesize + "_ins_" + Process.getCurrentThreadId() + "_" + fileflag + ".bin";
            var file_handle = new File(file_path, "a+");
            if (file_handle && file_handle != null) {
                var codeitemstartaddr = ptr(dexfilebegin).add(dex_code_item_offset_);
                var codeitemlength = funcGetCodeItemLength(ptr(codeitemstartaddr));
                if (codeitemlength != null & codeitemlength > 0) {
                    Memory.protect(ptr(codeitemstartaddr), codeitemlength, 'rwx');
                    var base64lengthptr = Memory.alloc(8);
                    var arr = [0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00];
                    Memory.writeByteArray(base64lengthptr, arr);
                    var base64ptr = funcBase64_encode(ptr(codeitemstartaddr), codeitemlength, ptr(base64lengthptr));
                    var b64content = ptr(base64ptr).readCString(base64lengthptr.readInt());
                    funcFreeptr(ptr(base64ptr));
                    var content = "{name:" + methodname + ",method_idx:" + dex_method_index_ + ",offset:" + dex_code_item_offset_ + ",code_item_len:" + codeitemlength + ",ins:" + b64content + "};";
                    file_handle.write(content);
                    file_handle.flush();
                    file_handle.close();
                    console.warn("[dump codeitem success]" + methodname);
                }

            } else {
                console.log("openfile failed,filepath:", file_path);
            }
        }


    }

}

function readuleb128string(uleb128ptr) {
    var num = ptr(uleb128ptr).readU8();
    var uleb128length = 1;
    while (num >= 128) {
        uleb128length = uleb128length + 1;
        num = ptr(uleb128ptr).add(0x1).readU8();
    }
    return ptr(uleb128ptr).add(uleb128length).readUtf8String();

}

function getClassNameList(dexfilebegin, dexfilesize) {
    var classnamelist = [];
    var type_size = ptr(dexfilebegin).add(0x40).readU32();
    var offset = ptr(dexfilebegin).add(0x44).readU32();
    var type_ids_offset = ptr(dexfilebegin).add(offset);
    var string_size = ptr(dexfilebegin).add(0x38).readU32();
    offset = ptr(dexfilebegin).add(0x3c).readU32();
    var strings_offset = ptr(dexfilebegin).add(offset);
    offset = ptr(dexfilebegin).add(0x64).readU32();
    var class_def_off = ptr(dexfilebegin).add(offset);
    var class_def_size = ptr(dexfilebegin).add(0x60).readU32();
    console.log(dexfilesize + "---class size:" + class_def_size + "--classdefoff:" + offset);
    for (var i = 0; i < class_def_size; i++) {
        var class_def_item = ptr(class_def_off).add(i * 32);
        var class_idx = ptr(class_def_item).readU32();
        var class_data_off = ptr(class_def_item).add(0x18).readU32();
        var descriptor = ptr(type_ids_offset).add(class_idx * 4).readU32();
        offset = ptr(strings_offset).add(descriptor * 4).readU32();
        var stringoffset = ptr(dexfilebegin).add(offset);
        var classname = readuleb128string(stringoffset);
        classnamelist.push(classname);
    }
    return classnamelist;

}

function getversion() {
    Java.perform(function () {
        var ver = Java.use('android.os.Build$VERSION');
        version = ver.SDK_INT.value;
        console.log("Version: " + version);
    })
}

function init() {
    console.log("go into init," + "Process.arch:" + Process.arch);
    getversion();
    var module_libext = null;
    if (Process.arch === "arm64") {
        module_libext = Module.load("/data/app/fart64.so");
    } else if (Process.arch === "arm") {
        module_libext = Module.load("/data/app/fart.so");
    }
    if (module_libext != null) {
        //android 7.0 7.1
        if (version == 24 || version == 25) {
            addrGetDexFile = module_libext.findExportByName("GetDexFile7");
            funcGetDexFile = new NativeFunction(addrGetDexFile, "pointer", ["pointer"]);

        }
        //android 8.0,8.1
        if (version == 26 || version == 27) {
            addrGetDexFile = module_libext.findExportByName("GetDexFile8");
            funcGetDexFile = new NativeFunction(addrGetDexFile, "pointer", ["pointer", "pointer"]);
        }

        addrGetCodeItemLength = module_libext.findExportByName("GetCodeItemLength");
        funcGetCodeItemLength = new NativeFunction(addrGetCodeItemLength, "int", ["pointer"]);
        addrBase64_encode = module_libext.findExportByName("Base64_encode");
        funcBase64_encode = new NativeFunction(addrBase64_encode, "pointer", ["pointer", "int", "pointer"]);
        addrFreeptr = module_libext.findExportByName("Freeptr");
        funcFreeptr = new NativeFunction(addrFreeptr, "void", ["pointer"]);
    }
    //android 8.0,8.1
    if (version == 26 || version == 27) {
        var symbols = Module.enumerateSymbolsSync("libart.so");
        for (var i = 0; i < symbols.length; i++) {
            var symbol = symbols[i];
            //_ZN3art9ArtMethod19GetObsoleteDexCacheEv
            if (symbol.name.indexOf("ArtMethod") >= 0 && symbol.name.indexOf("GetObsoleteDexCache") >= 0) {
                addrGetObsoleteDexCache = symbol.address;
                break;
            }
        }
    }
}

function dealwithmethod(classname, method) {
    var jnienv = Java.vm.getEnv();
    var ArtMethodptr = jnienv.fromReflectedMethod(ptr(getHandle(method)));
    var DexFileptr = null;
    //android 7.0 7.1
    if (version == 24 || version == 25) {
        DexFileptr = funcGetDexFile(ptr(ArtMethodptr));
    }
    //android 8.0,8.1
    if (version == 26 || version == 27) {
        DexFileptr = funcGetDexFile(ptr(ArtMethodptr), ptr(addrGetObsoleteDexCache));
    }

    if (DexFileptr != null) {
        var dexfilebegin = Memory.readPointer(ptr(DexFileptr).add(Process.pointerSize * 1));
        var dexfilesize = Memory.readU32(ptr(DexFileptr).add(Process.pointerSize * 2));
        var dexfileobj = new DexFile(dexfilebegin, dexfilesize);
        if (ArtMethodptr != null) {
            var artmethodobj = new ArtMethod(dexfileobj, ArtMethodptr);
            dumpcodeitem(classname + "->" + method.toString(), artmethodobj, 'all');
        }
    }
}

function dumpmethod(classname, method) {
    //console.warn("start dump method:" + classname + "-->" + method.toString());
    var jnienv = Java.vm.tryGetEnv();
    var ArtMethodptr = jnienv.fromReflectedMethod(ptr(getHandle(method)));
    var DexFileptr = null;
    //android 7.0 7.1
    if (version == 24 || version == 25) {
        DexFileptr = funcGetDexFile(ptr(ArtMethodptr));
    }
    //android 8.0,8.1
    if (version == 26 || version == 27) {
        DexFileptr = funcGetDexFile(ptr(ArtMethodptr), ptr(addrGetObsoleteDexCache));
    }
    if (DexFileptr != null) {
        var dexfilebegin = Memory.readPointer(ptr(DexFileptr).add(Process.pointerSize * 1));
        var dexfilesize = Memory.readU32(ptr(DexFileptr).add(Process.pointerSize * 2));
        if (dexfilemaps[dexfilebegin] == undefined) {
            dexfilemaps[dexfilebegin] = dexfilesize;
            console.warn("[got a dex]" + dexfilesize);
        }
        //console.log("got a dex:" + dexfilesize + "---begin:" + dexfilebegin);
        var dexfileobj = new DexFile(dexfilebegin, dexfilesize);
        if (ArtMethodptr != null) {
            var artmethodobj = new ArtMethod(dexfileobj, ArtMethodptr);
            dumpcodeitem(classname + "->" + method.toString(), artmethodobj, classname);
        }
    } else {

    }
    //console.warn("end dump method:" + classname + "-->" + method.toString());

}

function replaceAll(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace);
}

function dumpclass(classname) {
    if (Java.available) {
        Java.perform(function () {
            Java.enumerateClassLoadersSync().forEach(function (loader) {
                try {
                    var loadclass = loader.findClass(classname);
                    console.error("success loadclass->" + loadclass);
                    var methods = loadclass.getDeclaredConstructors();
                    if (methods != null) {
                        methods.forEach(function (method) {
                            dumpmethod(classname, method);
                        })
                    }
                    methods = loadclass.getDeclaredMethods();
                    if (methods != null) {
                        methods.forEach(function (method) {
                            dumpmethod(classname, method);
                        })
                    }
                } catch (e) {
                    //console.log("error", e);
                }
            })
        });
    }
}

function fart() {
    if (Java.available) {
        Java.perform(function () {
            Java.choose("dalvik.system.DexFile", {
                onMatch: function (dexfileobj) {
                    console.log("find a DexFile instance->" + dexfileobj);
                    var Array = Java.use("java.lang.reflect.Array");
                    var mCookie = dexfileobj.mCookie.value;
                    var mInternalCookie = dexfileobj.mInternalCookie.value;
                    console.log(mCookie + "---" + mInternalCookie);
                    if (mInternalCookie != null) {
                        var size = Array.getLength(mInternalCookie);
                        for (var i = 0; i < size; i++) {
                            var longvalue = Array.getLong(mInternalCookie, i);
                            var dexfileptr = ptr(longvalue + "");
                            try {
                                var dexfilebegin = ptr(dexfileptr).add(Process.pointerSize * 1).readPointer();
                                if (dexfilemaps[dexfilebegin] == undefined) {
                                    var dexfilesize = ptr(dexfileptr).add(Process.pointerSize * 2).readU32();
                                    dexfilemaps[dexfilebegin] = dexfilesize;
                                    console.warn("[got a dex]" + dexfilesize);
                                }
                            } catch (e) {

                            }
                        }
                    }
                    if (mCookie != null) {
                        var size = Array.getLength(mCookie);
                        for (var i = 0; i < size; i++) {
                            var longvalue = Array.getLong(mCookie, i);
                            var dexfileptr = ptr(longvalue + "");
                            try {
                                if (dexfilemaps[dexfileptr] == undefined) {
                                    var dexfilebegin = ptr(dexfileptr).add(Process.pointerSize * 1).readPointer();
                                    var dexfilesize = ptr(dexfileptr).add(Process.pointerSize * 2).readU32();
                                    dexfilemaps[dexfilebegin] = dexfilesize;
                                    console.warn("[got a dex]" + dexfilesize);
                                }
                            } catch (e) {

                            }
                        }
                    }
                }, onComplete: function () {
                    console.log("search DexFile over");
                }
            });
            console.warn(JSON.stringify(dexfilemaps));
            for (var dexptr in dexfilemaps) {
                var dexfilesize = dexfilemaps[dexptr];
                console.warn("[dealwith dex]" + dexfilesize);
                try {
                    var classnamelist = getClassNameList(ptr(dexptr), dexfilesize);
                    classnamelist.forEach(function (classname) {
                        classname = replaceAll(classname, "/", ".");
                        classname = replaceAll(classname, ";", "");
                        classname = classname.substring(1);
                        dumpclass(classname);
                    })
                } catch (e) {
                    console.log(e);
                }

            }
        });
    }
}

setImmediate(init);