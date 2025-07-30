> [无名杀](https://github.com/libnoname/noname)是优秀的`HTML单机三国杀`，游戏实现方式本质上来说是一个 网页+“浏览器”，通过能够在不同平台的浏览器运行无名杀网页，从而组成了各个平台的版本，版本的区别仅仅体现在浏览器的差异。 无名杀有强大的DIY功能，可以与网友讨论交流，设计自己喜欢的武将，目前支持联机功能。有着自己原创的模式，以及三国杀的众多玩法。

无名杀增强版是在其基础上开发的`android 应用程序`。
## 关于
程序开源代码：[https://github.com/zhaiyanqi/noname-plus](https://github.com/zhaiyanqi/noname-plus)，有任何程序上的问题可以到github上提 [issues](https://github.com/zhaiyanqi/noname-plus/issues) 给我反馈。

本项目将要与[noname-shijian-android](https://github.com/nonameShijian/noname-shijian-android)进行合并，并且重新创建项目，所以本项目将会进入停止更新的状态。仅当遇到恶性bug时会进行修复

## 功能说明

#### 共存
- 无名杀增强版能够和原版共存，卸载、清除数据等操作不会影响到原有的游戏内容。
- 安装后的应用名称为`noname`

#### 版本管理
无名杀作为网页游戏，一直使用的是游戏内更新的方式，但是经常遇到GitHub访问不通畅、更新失败导致游戏无法运行的情况，重新下载费时费力，增强版提供了以下几种游戏资源和版本的管理方式。
- **从QQ群文件、文件管理器等直接导入资源、更新包到程序，便捷更新。**
- **通过增强版更新游戏主体资源，提供多个更新源避免网络不通畅的情况，即使更新失败也可重新下载，下载完成后可正常进入游戏页面。**
- （仅限于懒人包）**内置完整资源**
- **多版本切换，数据互不干扰**


#### 联机服务器
#### **只有Android版**

无名杀增强版在`WebView`的基础上，结合`Android原生`开放的能力，实现了版本管理、资源下载、手机建立联机服务器等多种功能能力，用于解决原程序的`android版本`容易崩溃、无法创建联机服务器等问题。


## 更新日志

#### 版本1.3.3
更新日期：2025年7月30日
1. 修改了一个联机地址
2. 修复安卓8以下联机设置服务器闪退的bug
3. 修复联机服务器传输数据的bug
4. www/js/index.js中新增了加载game/importmap.js，旧版本需要使用新app的请在对应位置创建同名空文件
5. 本项目将要与[noname-shijian-android](https://github.com/nonameShijian/noname-shijian-android)进行合并，并且重新创建项目，所以本项目将会进入停止更新的状态。仅当遇到恶性bug时会进行修复

#### 版本1.3.2
将公共的Api和升级Webview内核操作封装到NoameCore模块中，使所有App可以共用相同功能

#### 版本1.3.1
更新日期：2024年10月27日
1. 取消了对file协议的兼容（其他APP也将陆续取消对file协议的兼容）
2. 添加了截图接口window.NonameAndroidBridge.captureScreen(文件名)，会把截图保存到DCIM/应用包名文件夹内(本应用包名为com.widget.noname.cola)
3. 补充缺失的跨域配置

#### 版本1.3.0
更新日期：2024年6月7日
更新内容：增加对https版本的支持，修复解压zip的中文乱码，可以使用chrome作为本应用的webview实现

#### 版本1.2.0
更新日期：2021年某月某日
更新内容：忘了

# 克隆本项目
git clone --recursive https://github.com/zhaiyanqi/noname-plus.git

## 创建安卓项目
先按教程全局安装cordova环境(本项目用的是cordova12)

然后安装项目依赖

```
npm i cordova@12 -g
npm i
```

创建安卓项目: 
```
cordova platform add android@13
```

在platforms\android\settings.gradle`改为`:
```gradle
apply from: "cdv-gradle-name.gradle"
include ":"
include ":CordovaLib"
include ":app"
include ':Common'
include ':NonameUI'
include ':FunctionServer'
include ':FunctionVersion'
include ':FunctionAbout'
include ':FunctionGameShell'
include ':FunctionLibrary'
include ':NonameCore'
```

在platforms\android\repositories.gradle
和
在platforms\android\app\repositories.gradle
`改为`:
```gradle
ext.repos = {
    google()
    mavenCentral()
    jcenter()
    maven { url "https://oss.jfrog.org/libs-snapshot" }
    maven { url 'https://jitpack.io' }
    maven { url 'https://maven.aliyun.com/repository/public/' }
}
```

在这个块(android块)上面添加:
```gradle
def generateTime() {
    return new Date().format("yyyy-MM-dd")
}
android { ... }
```

在platforms\android\app\build.gradle的android块中修改sourceSets块为:
```gradle
sourceSets {
    main {
        java.srcDirs += 'src/main/kotlin'
        jniLibs.srcDirs = ['libs']
    }
}
```

在platforms\android\app\build.gradle的android块中修改buildFeatures块为:
```gradle
buildFeatures {
    buildConfig true
    //noinspection DataBindingWithoutKapt
    dataBinding true
}
```

在sourceSets块下面添加:
```gradle
android.applicationVariants.all {
    variant ->
        variant.outputs.all {
            if (buildType.name == 'release') {
                outputFileName = "无名杀增强版v${variant.versionName}(${generateTime()}).ApK"
            }
        }
}

aaptOptions {
    // 表示不让aapt压缩的文件后缀
    noCompress "apk"
}
```

在platforms\android\app\build.gradle的dependencies`改为`:
```gradle
dependencies {
    // .......
    // SUB-PROJECT DEPENDENCIES END

    implementation fileTree(dir: 'src/main/libs', include: '*.jar')
    implementation project(":Common")
    implementation project(":FunctionLibrary")
    implementation project(':FunctionServer')
    implementation project(":NonameUI")
    implementation project(":FunctionVersion")
    implementation project(":FunctionGameShell")
    implementation project(":FunctionAbout")
    implementation project(":NonameCore")
    
	implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'com.android.support.constraint:constraint-layout:2.0.4'

    implementation 'com.tencent:mmkv:1.2.11'
    implementation "androidx.viewpager2:viewpager2:1.0.0"
    implementation 'com.guolindev.permissionx:permissionx:1.6.1'
    //noinspection GradleDependency
    implementation 'com.alibaba:fastjson:1.1.72.android'
    implementation 'com.github.bumptech.glide:glide:4.12.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
    implementation 'androidx.appcompat:appcompat:1.3.0'
    implementation 'com.github.li-xiaojun:XPopup:2.7.0'
    implementation 'org.greenrobot:eventbus:3.3.1'
    implementation 'com.google.android.material:material:1.4.0'
    implementation 'androidx.recyclerview:recyclerview:1.2.1'
    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
    implementation 'io.reactivex.rxjava3:rxjava:3.1.3'
    implementation 'com.squareup.okhttp3:okhttp:4.9.0'
    implementation 'com.github.hzy3774:AndroidP7zip:v1.7.2'
    implementation 'org.java-websocket:Java-WebSocket:1.5.2'
}
```

修改platforms\android\app\src\main\AndroidManifest.xml的代码
```xml
<activity ....>
    <intent-filter android:label="@string/launcher_name">
        <!-- 删除这两行 -->
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
        <!-- 添加这两行 -->
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

修改platforms\android\CordovaLib\build.gradle的dependencies添加以下代码
```
implementation 'com.tencent:mmkv:1.2.11'
implementation project(':Common')
```

然后打开`最新版`Android Studio进行安卓开发
