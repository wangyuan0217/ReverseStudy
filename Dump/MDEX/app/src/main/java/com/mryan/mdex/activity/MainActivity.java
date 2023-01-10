package com.mryan.mdex.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.mryan.mdex.R;
import com.mryan.mdex.adpater.ApplicationInfoAdapter;
import com.mryan.mdex.constract.MainConstract;
import com.mryan.mdex.presenter.MainPresenter;
import com.mryan.mdex.until.AppInfo;
import com.mryan.mdex.until.PropertiesUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.leefeng.promptlibrary.PromptDialog;


public class MainActivity extends AppCompatActivity implements MainConstract.MainView, ServiceConnection {
    private MainPresenter mainPresenter;
    private ListView mLVAppInfo;
    //存放第三方应用信息
    private List<AppInfo> mListsAppInfo;
    private PackageManager pm;
    private TextView mSetting;
    private ApplicationInfoAdapter applicationInfoAdapter;
    //下拉按钮
    private SwipeRefreshLayout mTabLayout2SkillSf;
    private List<ApplicationInfo> listAppcations;
    //Properties存储
    private PropertiesUtil mProp;
    private PromptDialog promptDialog;
    //检测模块
    private Button vcm_false_intent;
    private Button vcm_true_intent;
    private Dialog dialogMoudleTrue;
    private Dialog dialogMoudleFalse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onTRANSLUCENT();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && MainActivity.this.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            MainActivity.this.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
        }
        init();
        onClick();
    }

    private void onClick() {
        mSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogAbout(MainActivity.this);
            }
        });
        mSetting.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", "com.mryan.mdex", null);
                intent.setData(uri);
                startActivity(intent);
                return false;
            }
        });
        mTabLayout2SkillSf.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mainPresenter.RefreshAppInFo();//刷新
            }
        });
        mLVAppInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //配置弹窗
                showDialog(MainActivity.this, position);
            }
        });

    }


    @SuppressLint("SdCardPath")
    private void init() {
        mainPresenter = new MainPresenter(this);
        mLVAppInfo = findViewById(R.id.lv_main_showappinfo);
        mSetting = findViewById(R.id.setting);
        mTabLayout2SkillSf = findViewById(R.id.sf_main_skill);
        mListsAppInfo = new ArrayList<>();
        //SwipeRefreshLayout 监听
        mTabLayout2SkillSf.setColorScheme(R.color.hblue);
        // 获得PackageManager对象
        pm = this.getPackageManager();
        //模块检测
        if (!isModuleActive()) {
            showDialogCheckMoudleFalse(MainActivity.this);
            vcm_false_intent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String xposedPkgName = "de.robv.android.xposed.installer";
                    Intent intent = getPackageManager().getLaunchIntentForPackage(xposedPkgName);
                    if (intent == null) {
                        Snackbar.make(getWindow().getDecorView(), "检测到未安装Xposed环境！", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(getWindow().getDecorView(), "跳转中！！！！", Snackbar.LENGTH_SHORT).show();
                        startActivity(intent);
                    }
                }
            });
        } else {
            showDialogCheckMoudleTrue(MainActivity.this);
            vcm_true_intent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogMoudleTrue.cancel();
                }
            });

        }
        mainPresenter.loadAllApp(getApplicationContext());

    }


    @Override
    public void onLoadAllApp(Context context) {
        ScaleAnimation s = new ScaleAnimation(0, 1, 0, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        s.setDuration(100);
        LayoutAnimationController f = new LayoutAnimationController(s, 1f);
        f.setOrder(LayoutAnimationController.ORDER_NORMAL);/*顺序*/
        mLVAppInfo.setLayoutAnimation(f);
        mListsAppInfo = queryAppInfo();
        applicationInfoAdapter = new ApplicationInfoAdapter(context, mListsAppInfo);
        mLVAppInfo.setAdapter(applicationInfoAdapter);
    }


    /**
     * 弹窗检测模块激活 false
     *
     * @param context
     */
    private void showDialogCheckMoudleFalse(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialogMoudleFalse = builder.create();
        View viewCheckMoudleFalse = LayoutInflater.from(context).inflate(R.layout.checkmoudlefalse, null);
        dialogMoudleFalse.show();
        dialogMoudleFalse.setCancelable(true);
        Window window = dialogMoudleFalse.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setContentView(viewCheckMoudleFalse);
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        vcm_false_intent = viewCheckMoudleFalse.findViewById(R.id.btn_checkfalse_intent);

    }


    /**
     * 弹窗检测模块激活 true
     *
     * @param context
     */
    @SuppressLint("InflateParams")
    private void showDialogCheckMoudleTrue(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialogMoudleTrue = builder.create();
        View viewCheckMoudleTrue = LayoutInflater.from(context).inflate(R.layout.checkmoudletrue, null);
        dialogMoudleTrue.show();
        dialogMoudleTrue.setCancelable(true);
        Window window = dialogMoudleTrue.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setContentView(viewCheckMoudleTrue);
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        vcm_true_intent = viewCheckMoudleTrue.findViewById(R.id.btn_checktrue_intent);
    }


    /**
     * 点击MDEX关于的弹窗
     *
     * @param context
     */
    private void showDialogAbout(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        @SuppressLint("InflateParams") View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.aboutdialog, null);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setContentView(v);
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        CardView clear = v.findViewById(R.id.tv_about_clearProperties);
        //跳转xposed框架
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String xposedPkgName = "de.robv.android.xposed.installer";
                Intent intent = getPackageManager().getLaunchIntentForPackage(xposedPkgName);
                if (intent == null) {
                    Snackbar.make(getWindow().getDecorView(), "检测到未安装Xposed环境！", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(getWindow().getDecorView(), "跳转中！！！！", Snackbar.LENGTH_SHORT).show();
                    startActivity(intent);
                }
            }
        });
    }

    /**
     * 点击listview的弹窗
     *
     * @param context
     * @param position
     */
    private void showDialog(final Context context, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        String appName = mListsAppInfo.get(position).getAppName();
        String pkgName = mListsAppInfo.get(position).getPkgName();
        @SuppressLint("InflateParams") View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.customdialog, null);
        final Dialog dialog = builder.create();
        dialog.show();
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setContentView(v);
        window.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        //跳转
        CardView btnintent = v.findViewById(R.id.btn_intent);
        TextView textView = v.findViewById(R.id.select_appname);
        textView.setText(appName);
        //默认选择此应用
        String mainActivity = null;
        if (pkgName != null) {
            mainActivity = findMainActivity(pkgName);
        }
        if (mainActivity != null && pkgName != null) {
            mProp = PropertiesUtil.getInstance().init();
            Snackbar.make(getWindow().getDecorView(), "已选择：" + appName + "     请点击DUMPDEX    \n" + "脱壳后自动复制DEX输出路径到剪贴板", Snackbar.LENGTH_SHORT).show();
            // Properties存储包名 app名 启动类
            mProp.writeString("pkgName", pkgName);
            mProp.writeString("mainactivity", mainActivity);
            mProp.writeString("appName", appName);
            mProp.commit();

        }
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Uri uri = Uri.fromParts("package", mListsAppInfo.get(position).getPkgName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });
        btnintent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isModuleActive()) {
                    Snackbar.make(getWindow().getDecorView(), "模块未激活！！！激活模块后食用！！！", Snackbar.LENGTH_LONG).show();
                } else {
                    //跳转
                    String pkgName = mListsAppInfo.get(position).getPkgName();
                    Intent intent = getPackageManager().getLaunchIntentForPackage(pkgName);
                    if (intent == null) {
                        Toast.makeText(MainActivity.this, "打开失败", Toast.LENGTH_LONG).show();

                    } else {
                        promptDialog = new PromptDialog(MainActivity.this);
                        promptDialog.showLoading("脱壳中！");

                        //DEX输出路径复制到剪贴板
                        //获取剪贴板管理器：
                        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        // 创建普通字符型ClipData
                        ClipData mClipData = ClipData.newPlainText("path", "/data/data/" + pkgName + "/dump");
                        // 将ClipData内容放到系统剪贴板里。
                        cm.setPrimaryClip(mClipData);
                        startActivity(intent);
                        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                        am.restartPackage(getPackageName());
                        dialog.cancel();
                    }
                }
            }
        });


    }


    /**
     * 获取Properties存储配置文件
     *
     * @return
     */
    private List<String> getPropertiesXdata() {
        List<String> list = new ArrayList<>();
        //利用Properties读取app指定信息
        PropertiesUtil mProp = PropertiesUtil.getInstance().init();
        mProp.open();
        String pkgName = mProp.readString("pkgName", "");
        String mainactivity = mProp.readString("mainactivity", "");
        String appName = mProp.readString("appName", "");
        list.add(pkgName);
        list.add(mainactivity);
        list.add(appName);
        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (promptDialog != null) {
            promptDialog.dismiss();
        }
    }


    private String findMainActivity(String pkgName) {
        String className = null;
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return null;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = getPackageManager()
                .queryIntentActivities(resolveIntent, 0);
        if (resolveinfoList != null) {
            try {
                ResolveInfo resolveinfo = resolveinfoList.iterator().next();
                if (resolveinfo != null) {
                    className = resolveinfo.activityInfo.name;
                }
            } catch (Exception e) {
                Snackbar.make(getWindow().getDecorView(), "此应用不支持DUMP", Snackbar.LENGTH_LONG).show();
            }
        }
        return className;
    }

    @Override
    public void onTRANSLUCENT() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            // Translucent status bar
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }

    @Override
    public void onRefreshAppInFo() {
        mainPresenter.loadAllApp(MainActivity.this);
        mTabLayout2SkillSf.setRefreshing(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * 获得所有启动Activity的信息
     */
    public List<AppInfo> queryAppInfo() {

        // 查询所有已经安装的应用程序
        listAppcations = pm
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        Collections.sort(listAppcations,
                new ApplicationInfo.DisplayNameComparator(pm));// 排序
        List<AppInfo> appInfos = new ArrayList<AppInfo>(); // 保存过滤查到的AppInfo
        appInfos.clear();
        for (ApplicationInfo app : listAppcations) {
            //非系统程序
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                appInfos.add(getAppInfo(app));
            }
            //本来是系统程序，被用户手动更新后，该系统程序也成为第三方应用程序了
            else if ((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                appInfos.add(getAppInfo(app));
            }
        }
        /*System.out.println(appInfos);*/
        return appInfos;
    }

    // 构造一个AppInfo对象 ，并赋值
    private AppInfo getAppInfo(ApplicationInfo app) {
        AppInfo appInfo = new AppInfo();
        appInfo.setAppName((String) app.loadLabel(pm));
        appInfo.setAppIcon(app.loadIcon(pm));
        appInfo.setPkgName(app.packageName);
        return appInfo;
    }


    /**
     * 检测模块是否激活
     *
     * @return
     */
    private boolean isModuleActive() {
        return false;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}