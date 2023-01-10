//typed by hanbingle,just for fun!!
//email:edunwu@gmail.com
//Android 7,7.1,8,8.1 版本
/*使用说明
首先拷贝fart.so和fart64.so到/data/app目录下，并使用chmod 777 设置好权限,然后就可以使用了。
该frida版fart是使用hook的方式实现的函数粒度的脱壳，仅仅是对类中的所有函数进行了加载，但依然可以解决绝大多数的抽取保护
需要以spawn方式启动app，等待app进入Activity界面后，执行fart()函数即可。如app包名为com.example.test,则
frida -U -f com.example.test -l frida_fart_hook.js --no-pause，然后等待app进入主界面,执行fart()
高级用法：如果发现某个类中的函数的CodeItem没有dump下来，可以调用dumpclass(classname),传入要处理的类名，完成对该类下的所有函数体的dump,dump下来的函数体会追加到bin文件当中。
 */
var ishook_libart = false;
var addrLoadMethod = null;
var addrGetCodeItemLength = null;
var funcGetCodeItemLength = null;
var addrBase64_encode = null;
var funcBase64_encode = null;
var addrFreeptr = null;
var funcFreeptr = null;
var savepath = "/sdcard";
var version = 0;
var dex_maps = {};
var artmethod_maps = {};

function DexFile(start, size) {
    this.start = start;
    this.size = size;
}

function ArtMethod(dexfile, artmethodptr) {
    this.dexfile = dexfile;
    this.artmethodptr = artmethodptr;
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

function hookart() {
    getversion();
    if (ishook_libart === true) {
        return;
    }
    var module_libext = null;
    if (Process.arch === "arm64") {
        module_libext = Module.load("/data/app/fart64.so");
    } else if (Process.arch === "arm") {
        module_libext = Module.load("/data/app/fart.so");
    }
    if (module_libext != null) {
        addrGetCodeItemLength = module_libext.findExportByName("GetCodeItemLength");
        funcGetCodeItemLength = new NativeFunction(addrGetCodeItemLength, "int", ["pointer"]);
        addrBase64_encode = module_libext.findExportByName("Base64_encode");
        funcBase64_encode = new NativeFunction(addrBase64_encode, "pointer", ["pointer", "int", "pointer"]);
        addrFreeptr = module_libext.findExportByName("Freeptr");
        funcFreeptr = new NativeFunction(addrFreeptr, "void", ["pointer"]);
    }
    var symbols = Module.enumerateSymbolsSync("libart.so");
    for (var i = 0; i < symbols.length; i++) {
        var symbol = symbols[i];
        //_ZN3art11ClassLinker10LoadMethodERKNS_7DexFileERKNS_21ClassDataItemIteratorENS_6HandleINS_6mirror5ClassEEEPNS_9ArtMethodE
        if (symbol.name.indexOf("ClassLinker") >= 0
            && symbol.name.indexOf("LoadMethod") >= 0
            && symbol.name.indexOf("DexFile") >= 0
            && symbol.name.indexOf("ClassDataItemIterator") >= 0
            && symbol.name.indexOf("ArtMethod") >= 0) {
            addrLoadMethod = symbol.address;
            break;
        }
    }

    if (addrLoadMethod != null) {
        Interceptor.attach(addrLoadMethod, {
            onEnter: function (args) {
                //android 7.0 7.1
                if (version == 24 || version == 25) {
                    this.dexfileptr = args[2];
                    this.artmethodptr = args[5];
                }
                //android 8.0 8.1
                if (version == 26 || version == 27) {
                    this.dexfileptr = args[1];
                    this.artmethodptr = args[4];
                }
            },
            onLeave: function (retval) {
                var dexfilebegin = null;
                var dexfilesize = null;
                if (this.dexfileptr != null) {
                    dexfilebegin = ptr(this.dexfileptr).add(Process.pointerSize * 1).readPointer();
                    dexfilesize = ptr(this.dexfileptr).add(Process.pointerSize * 2).readU32();
                    var dexfile_path = savepath + "/" + dexfilesize + "_loadMethod.dex";
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
                    var dexfileobj = new DexFile(dexfilebegin, dexfilesize);
                    if (dex_maps[dexfilebegin] == undefined) {
                        dex_maps[dexfilebegin] = dexfilesize;
                        console.warn("[got a dex]addr:" + dexfilebegin + "---size:" + dexfilesize);
                    }
                }

                if (this.artmethodptr != null) {
                    var artmethodobj = new ArtMethod(dexfileobj, this.artmethodptr);
                    if (artmethod_maps[this.artmethodptr] == undefined) {
                        artmethod_maps[this.artmethodptr] = artmethodobj;
                    }
                }
            }
        });
    }
    ishook_libart = true;
}

function dumpcodeitem(artmethodobj) {
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
            var file_path = dir + "/" + dexfilesize + "_ins_" + Process.getCurrentThreadId() + ".bin";
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
                    var content = "{name:ooxx,method_idx:" + dex_method_index_ + ",offset:" + dex_code_item_offset_ + ",code_item_len:" + codeitemlength + ",ins:" + b64content + "};";
                    file_handle.write(content);
                    file_handle.flush();
                    file_handle.close();
                }

            } else {
                console.log("openfile failed,filepath:", file_path);
            }
        }


    }

}

function dumpall() {
    console.log("start dump all CodeItem.......");
    for (var artmethodptr in artmethod_maps) {
        var artmethodobj = artmethod_maps[artmethodptr];
        try {
            dumpcodeitem(artmethodobj);
        } catch (e) {
            console.log("error", e);
        }

    }
    console.log("end dump all CodeItem.......");
}

function dumpclass(classname) {
    if (Java.available) {
        Java.perform(function () {
            Java.enumerateClassLoadersSync().forEach(function (loader) {
                try {
                    var loadclass = loader.findClass(classname);
                    console.warn("success loadclass->" + loadclass);
                } catch (e) {
                    //console.log("error", e);
                }
            })
            dumpall();
        });
    }
}

function replaceAll(str, find, replace) {
    return str.replace(new RegExp(find, 'g'), replace);
}

function fart() {
    if (Java.available) {
        Java.perform(function () {
            var loaderarray = [];
            Java.enumerateClassLoadersSync().forEach(function (loader) {
                loaderarray.push(loader);
            })
            dumpall();
            //上面是利用被动调用进行函数粒度的dump，对app正常运行过程中自己加载的所有类函数进行dump
            for (var dexptr in dex_maps) {
                var dexfilesize = dex_maps[dexptr];
                console.warn("[dealwith dex]"+dexfilesize);
                try {
                    var classnamelist = getClassNameList(ptr(dexptr), dexfilesize);
                    classnamelist.forEach(function (classname) {
                        //Lorg/xml/sax/helpers/XMLReaderFactory;
                        classname = replaceAll(classname, "/", ".");
                        classname = replaceAll(classname, ";", "");
                        classname = classname.substring(1);
                        loaderarray.forEach(function (loader) {
                            try {
                                //console.log("try loadclass->" + classname);
                                var loadclass = loader.findClass(classname);
                                console.warn("success loadclass->" + loadclass);
                            } catch (e) {
                            }
                        })
                    })
                } catch (e) {
                    console.log(e);
                }


            }
            dumpall();
        });
    }
}

setImmediate(hookart);