# Android多渠道(平台)打包

[TOC]

*前言：*
开发中难免会遇到这样的两个需求：  

 - 国内有n个Android市场，根据不同市场打包出渠道名不同的APK来统计下载量；    
 - 同一个APK要给自己公司旗下不同的代理商使用，功能基本相同，但是图标等资源有较大差异，又或者签名也不同、服务器地址不同等等；  

遇到这样的需求怎么做呢，难道要新建一个或者多个工程吗，或者在同一个工程上修改再一次次的打包吗？直觉告诉我们肯定有更简便的方法的，而且Android Studio和Gradle这么智能，所以我们就来一步步探究下。

-------------------

## 一、认识Gradle

> Gradle是一个集合了Apache Ant和Apache Maven概念的项目自动化构建工具。它使用一种基于Groovy的特定领域语言(DSL)来声明项目设置。  
  
Gradle配置文件在项目中展示如下：
![Gradle](http://img.blog.csdn.net/20170720153638722?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

build.gradle(***Project***: xxx)：代表项目的基础配置文件。
build.gradle(***Module***: xxx)：表示该模块的配置文件，也是项目最主要的配置文件。
setting.gradle(Project Settings)：全局的项目配置文件，里面主要声明一些需要加入gradle的Module。

接下来我们主要看下build.gradle(Module: xxx)文件：

```
apply plugin: 'com.android.application'

android {
    // 编译SDK的版本
    compileSdkVersion 25

    // buildTools版本
    buildToolsVersion "25.0.2"

    // 默认的配置
    defaultConfig {
        // 应用的包名
        applicationId "com.cooloongwu.multichannel"
        minSdkVersion 14
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // 是否进行代码混淆
            minifyEnabled false

            // 混淆文件的位置
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

// 依赖项目
dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:25.3.1'
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
    testCompile 'junit:junit:4.12'
}
```
项目建好后，基本配置就这么多，下面我们一步步增加内容来实现多渠道（平台）的打包过程。  

## 二、多渠道打包配置
### 1.	配置多渠道

在android标签中配置productFlavors，比如我的是天气应用，要给三个平台使用day，night，cold（没啥别的意思就是这么整的），那么分别进行如下配置，如果不同渠道的包名不同的话可以添加***applicationId***进行修改，通过***manifestPlaceholders***进行渠道名的配置。然后在AndroidManifest.xml的application标签中配置meta-data即可。

```
//多渠道打包配置，signingConfig可配置不同签名，在AndroidManifest.xml的application标签中配置meta-data即可，如下所示：
    //<meta-data
    //android:name="CHANNEL"
    //android:value="${CHANNEL_VALUE}" />

    productFlavors {
        day {
            applicationId = "com.cooloongwu.multichannel.day"
            manifestPlaceholders = [
                    CHANNEL_VALUE: "day"
            ]
            //signingConfig signingConfigs.day
        }

        night {
            applicationId = "com.cooloongwu.multichannel.night"
            manifestPlaceholders = [
                    CHANNEL_VALUE: "night"
            ]
            //signingConfig signingConfigs.night
        }

        cold {
            applicationId = "com.cooloongwu.multichannel.cold"
            manifestPlaceholders = [
                    CHANNEL_VALUE: "cold"
            ]
            //signingConfig signingConfigs.cold
        }

```
上面是直接在gradle中配置的，当然也可以在File→Profile Structure中配置，如下图所示：

![Flavors](http://img.blog.csdn.net/20170720154408266?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

配置好后点击Sync Project with Gradle Files同步项目，然后在Android Studio左下角的Build Variants中就多了你配置的渠道，如下所示：

![Variant](http://img.blog.csdn.net/20170720154515753?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

当你选中其中一个比如coldDebug时，那么你点击“Run App”只会打包并运行这么一个coldDebug版本的，要想所有的一次性打包出来的话，可以见下文“打包”。

### 2.	自定义渠道Apk名字

根据不同的渠道名生成带渠道名前缀的Apk方便上传到不同的市场，需在android标签中配置applicationVariants.all，代码如下：

```
//自定义打包发布apk名字的格式：xxx_multichannel_1.2.1.apk
    applicationVariants.all { variant ->
        variant.outputs.each { output ->
            def outputFile = output.outputFile
            if (variant.buildType.name == 'release') {
                def fileName = "${variant.productFlavors[0].name}_multichannel_${defaultConfig.versionName}.apk"
                output.outputFile = new File(outputFile.parent, fileName)
            }
        }
}

```

### 3.	不同渠道使用不同的签名
在android标签中配置signingConfigs，代码如下：

```
//多渠道签名配置
    signingConfigs {
        // day签名
        day{
            keyAlias day //别名
            keyPassword 'xxx'//别名密码
            storeFile file('D:/Key/day/ day.jks')//签名文件路径
            storePassword 'xxx'//签名密码
        }
        //night签名
        night{
            keyAlias night//别名
            keyPassword 'xxx'//别名密码
            storeFile file('D:/Key/night/night.jks')//签名文件路径
            storePassword 'xxx'//签名密码
        }
        //cold签名
        cold{
            keyAlias cold//别名
            keyPassword 'xxx'//别名密码
            storeFile file('D:/Key/cold/cold.jks')//签名文件路径
            storePassword 'xxx'//签名密码
        }
}

```

然后在productFlavors标签中各个渠道下，添加相应的签名配置即可，例如：***signingConfig signingConfigs.day***。

### 4.	打包
按照上述配置配置完后，点击右侧栏中的Gradle，然后依次展开MultiChannel→:app→Tasks→build，可以看到有下方这些选项了：

![Gradle](http://img.blog.csdn.net/20170720154746854?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

assemble以及其他各种assembleXXX顾名思义就是打包出相应的程序了，我们双击assembleRelease（打包所有渠道的Release版本），打包完后可以在***MultiChannel\app\build\outputs\apk***文件夹下看到相应的三个APK文件，并且文件名也自动生成好了：

## 三、进阶开发配置
### 1.	配置不同的资源
如果每个APK的应用名称需要定义的话，那么可以在productFlavors下每个渠道标签下配置：resValue "string", "app_name", " XXX"。如果在这里配置了，那么在strings.xml中就不再需要配置了，否则会提示冲突并报错。
也可以在app→src下建立相应的文件夹，比如我这里建立了day文件夹，文件夹中内容同main文件夹类似，表示day这个渠道的应用会使用这个文件夹下的资源文件等，在strings.xml中配置不同的应用名称即可。
同理应用图标等图片各种资源也是这样的用法，如下图所示：

![res](http://img.blog.csdn.net/20170720155011754?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### 2.	配置不同的代码
这里要说的是类似微信登录、分享等的回调机制。这种回调机制需要固定的“包名+类名”来实现。来看下微信分享接受返回值的要求，如下图所示：

![CallBack](http://img.blog.csdn.net/20170720155103452?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

这时问题就来了，你更改了包名，但是代码中的这处回调却没有，所以要在src下建立相应的目录及回调类，其中几点要求：

 1. 新建的目录层级要跟你在gradle文件中productFlavors下定义的applicationId一致。例如我这里是day，day的applicationId = "com.cooloongwu.multichannel.day"，一定要注意层级，千万不可出错；
 
![day](http://img.blog.csdn.net/20170720155220621?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

 2.当你复制WXEntryActivity.java文件到新目录后，记得要***更改包名***；

![packageName](http://img.blog.csdn.net/20170720155408568?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

3.在gradle中配置AppID等信息时，请注意***字符串写法***，如下图所示（当然这些信息不应直接暴漏出来，我这里只作为演示Demo使用）；

![Key](http://img.blog.csdn.net/20170720155452565?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvdTAxMDk3NjIxMw==/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

### 3.	版本号管理
每次发布新版前忘记更改版本号怎么办，当然我们可以使用gradle来定义啊！可以参考http://www.jianshu.com/p/a1b0bc453319。
下面主要罗列两种方式吧（还是有缺陷，以后有好的方式再修改吧）：

1、直接使用Git提交次数来定义VersionCode

```
def cmd_code = 'git rev-list HEAD --first-parent --count'
def gitVersionCode = cmd_code.execute().text.trim().toInteger()
```

2、使用日期和Git提交次数来定义VersionCode和VersionName

```
//（其实版本名这个东西还是需要靠手动控制，毕竟定版本信息还是需要靠人来决定，这个只是保证你每次提交代码后会更新下编译的版本号，版本名你可以自己定义，伪代码在下面了）
ext.majorCode = 1       //主版本号，手动修改
ext.minorCode = 0       //次版本号，手动修改
ext.revisionCode = 1    //修订版本号，手动修改
//ext.revisionDescriptionCMD = 'git describe --always'
//ext.tempRevisionDescription = revisionDescriptionCMD.execute().getText().trim()
//ext.revisionDescription = (tempRevisionDescription == null || (tempRevisionDescription).size() == 0) ? new Date().format("yyMMddhhss") : (tempRevisionDescription).substring((tempRevisionDescription).size() - 6)

ext.compileCodeCMD = 'git rev-list HEAD --count'
ext.compileCode = compileCodeCMD.execute().getText().trim().toInteger()//编译版本号，自动修改，按照Git提交次数生成

然后在gradle文件中定义两个方法来获取版本号和版本名称。
def getVersionCode(boolean isRelease) {
    // 正式环境
    if (isRelease) {
        majorCode * 1000000 + minorCode * 10000 + revisionCode + compileCode
    }
    // debug环境
    else {
        //直接返回一个日期
        Integer.parseInt(new Date().format("yyMMddhhss"))
    }
}

def getMyVersionName() {
    majorCode + "." + minorCode + "." + revisionCode + "." + compileCode
}

```

最后在自定义打包apk文件名的代码中根据release或者debug来判断使用不同的版本名称：

```
//自定义打包发布apk名字的格式：xxx_multichannel_1.2.1.apk
    applicationVariants.all { variant ->
        if (variant.buildType.name == 'release') {
            variant.mergedFlavor.versionCode = getVersionCode(true)
        } else {
            variant.mergedFlavor.versionCode = getVersionCode(false)
        }
        variant.outputs.each { output ->
            def outputFile = output.outputFile
            if (variant.buildType.name == 'release') {
                def fileName = "${variant.productFlavors[0].name}_multichannel_${defaultConfig.versionName}.apk"
                output.outputFile = new File(outputFile.parent, fileName)
            }
        }
    }

```
