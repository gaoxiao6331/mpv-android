# mpv-android 文件作用树

范围说明：本文件覆盖 `git ls-files` 中的已跟踪文件和这些文件所在目录；本地缓存、IDE 配置、构建输出和 `local.properties` 等未跟踪机器文件不列入。

- `mpv-android/`
  作用：项目根目录，包含 Android 版 mpv 播放器的应用代码、原生桥接、构建脚本、发布文档和商店元数据。
  - `.github/`
    作用：GitHub 协作与自动化配置。
    - `ISSUE_TEMPLATE/`
      作用：GitHub Issue 模板目录，用于规范用户提交的问题类型。
      - `1_bug_report.md`
        作用：Bug 报告模板，要求提供 Android/mpv-android 版本、复现步骤、logcat 和样例文件等排障信息。
      - `2_feature_request.md`
        作用：功能请求模板，引导用户描述期望行为和使用场景。
      - `3_question.md`
        作用：提问模板，用于非 Bug、非功能请求的问题。
      - `config.yml`
        作用：Issue 模板配置，关闭空白 Issue 入口，强制用户选择模板。
    - `workflows/`
      作用：GitHub Actions 工作流目录。
      - `build.yml`
        作用：CI 构建流程；安装 JDK 和原生依赖，缓存/构建 native prefix，编译 mpv 与 Android APK，并上传 debug APK 产物。
  - `.gitignore`
    作用：忽略构建输出、IDE 文件、Gradle/Kotlin 缓存、本地配置和原生库输出目录。
  - `LICENSE`
    作用：项目许可证文本。
  - `README.md`
    作用：项目简介、功能列表、下载入口和源码构建入口说明。
  - `build.gradle`
    作用：顶层 Gradle 配置，声明 Android Gradle Plugin、Kotlin 插件版本、仓库源和 Gradle Wrapper 版本。
  - `gradle.properties`
    作用：全局 Gradle 属性，启用 AndroidX、配置缓存和 JVM 内存参数。
  - `gradlew`
    作用：Unix/macOS Gradle Wrapper 启动脚本，用于调用仓库指定版本的 Gradle。
  - `settings.gradle`
    作用：Gradle 项目设置，目前只包含 `:app` 应用模块。
  - `app/`
    作用：Android 应用模块，包含 Kotlin/Java、JNI、资源、Manifest 和模块级构建配置。
    - `build.gradle`
      作用：应用模块构建配置；定义 SDK、版本号、`default`/`api29` flavors、ABI split、native 库完整性检查、versionCode 映射和 AndroidX/Material 依赖。
    - `src/`
      作用：Android 源集目录，按 flavor、build type 和 main 源集组织代码与资源。
      - `api29/`
        作用：`api29` flavor 的补充配置，用于旧 targetSdk 构建。
        - `AndroidManifest.xml`
          作用：为 `api29` flavor 声明传统外部存储读写权限。
      - `default/`
        作用：默认 flavor 的补充配置。
        - `AndroidManifest.xml`
          作用：为默认 flavor 声明存储/媒体权限；旧存储权限限制到 Android 12L 及以下，并在 Android 13+ 使用媒体读取权限。
      - `debug/`
        作用：debug 构建专用源集，提供调试入口和调试资源。
        - `AndroidManifest.xml`
          作用：注册 debug 专用的 Intent 测试页和编解码器信息页。
        - `java/`
          作用：debug 构建专用 Kotlin 代码。
          - `is/`
            作用：Java/Kotlin 包名根目录。
            - `xyz/`
              作用：项目包命名空间目录。
              - `mpv/`
                作用：debug 工具页面代码目录。
                - `CodecInfoActivity.kt`
                  作用：列出设备 MediaCodec 编解码器、硬件/软件/厂商标记、MIME 类型和 profile 信息，辅助排查硬解能力。
                - `IntentTestActivity.kt`
                  作用：构造并发起播放 Intent，测试 URI、字幕、硬解模式、标题和起播位置等外部调用参数。
        - `res/`
          作用：debug 构建专用资源。
          - `layout/`
            作用：debug 工具页面布局。
            - `activity_codec_info.xml`
              作用：编解码器信息页布局，包含筛选开关和信息文本区域。
            - `activity_intent_test.xml`
              作用：Intent 测试页布局，包含 URI 输入、选项开关、位置滑块和结果显示。
          - `values/`
            作用：debug 专用基础资源。
            - `strings.xml`
              作用：debug 工具页面使用的字符串资源。
      - `main/`
        作用：应用主源集，包含正式代码、资源、JNI 和资产。
        - `AndroidManifest.xml`
          作用：主应用 Manifest；声明播放 Activity、启动页、设置页、后台播放服务、通知按钮接收器、媒体 Intent 过滤器、PiP/多窗口/Leanback 支持和网络/前台服务权限。
        - `assets/`
          作用：打包进 APK 的静态资产目录。
          - `cacert.pem`
            作用：CA 根证书集合，运行时复制到应用文件目录，供 mpv 网络播放时进行 TLS 证书校验。
        - `java/`
          作用：主 Java/Kotlin 源码目录。
          - `is/`
            作用：包名根目录。
            - `xyz/`
              作用：项目包命名空间目录。
              - `filepicker/`
                作用：通用文件/文档选择器实现，被 mpv 专用选择器复用。
                - `AbstractFilePickerFragment.java`
                  作用：文件选择器抽象基类；管理 RecyclerView、路径跳转、加载器回调、父目录项、文件/目录 ViewHolder 和选择回调接口。
                - `DividerItemDecoration.java`
                  作用：RecyclerView 分隔线装饰器，用于文件列表行之间绘制 divider。
                - `DocumentPickerFragment.java`
                  作用：基于 Android Storage Access Framework 的文档树选择器，支持 DocumentsContract 列目录、判断目录、过滤和父路径映射。
                - `FileItemAdapter.java`
                  作用：文件列表 RecyclerView 适配器，额外插入 `..` 头部项以返回父目录。
                - `FilePickerFragment.java`
                  作用：基于 `java.io.File` 的本地/外部存储选择器，处理存储权限、隐藏文件、文件过滤和目录监听。
                - `LogicHandler.java`
                  作用：文件选择器后端接口，抽象目录判断、路径转换、加载器、ViewHolder 创建与绑定等行为。
              - `mpv/`
                作用：应用核心播放、界面、设置、文件选择和工具代码。
                - `BackgroundPlaybackService.kt`
                  作用：后台播放前台服务；保持播放进程存活并维护媒体通知、封面图、播放/暂停/上一首/下一首状态。
                - `BaseMPVView.kt`
                  作用：最小 mpv SurfaceView 封装；负责创建/初始化/销毁 libmpv、绑定 Surface、设置 VO，并在 Surface 可用后执行 `loadfile`。
                - `DecimalPickerDialog.kt`
                  作用：十进制数值选择对话框，提供输入框和加减按钮，并把数值限制在指定范围内。
                - `FilePickerActivity.kt`
                  作用：文件选择 Activity；协调旧文件选择器、系统文档选择器、文档树选择、URL 输入、过滤开关和选择结果返回。
                - `KeyMapping.java`
                  作用：Android 按键码到 mpv 按键名的映射表，支持键盘、遥控器、方向键和媒体键输入。
                - `MPVActivity.kt`
                  作用：播放器主 Activity；处理 Intent 解析、播放控制、手势、按键、UI 显隐、音频焦点、后台播放、PiP、媒体会话、播放列表、轨道选择和 mpv 事件更新。
                - `MPVDocumentPickerFragment.java`
                  作用：mpv 专用文档树选择器；给文档项绑定缩略图，点击选择文件，长按选择目录。
                - `MPVFilePickerFragment.java`
                  作用：mpv 专用文件系统选择器；支持自定义根目录、ActionBar 路径显示、缩略图加载、点击选文件和长按选目录。
                - `MPVLib.kt`
                  作用：JNI/native libmpv 包装层；加载 `libmpv` 和 `libplayer`，暴露命令、属性、选项、Surface、缩略图接口，并分发 mpv 事件和日志观察者回调。
                - `MPVView.kt`
                  作用：播放器 View 的业务封装；读取偏好设置并转为 mpv 选项，观察属性，管理音频/字幕轨、播放列表、章节、播放速度、循环、随机和硬解切换。
                - `MainActivity.kt`
                  作用：应用启动 Activity，承载主屏 Fragment，并应用 Material You 动态主题。
                - `MainScreenFragment.kt`
                  作用：启动主屏；提供打开文件、打开 URL、设置入口、恢复上次选择和 debug 菜单入口。
                - `NotificationButtonReceiver.kt`
                  作用：后台播放通知按钮广播接收器，把播放/暂停、上一项、下一项动作转成 mpv 命令。
                - `PickerDialog.kt`
                  作用：数值选择对话框的统一接口，供滑块、速度、十进制选择器共用。
                - `PlaylistDialog.kt`
                  作用：播放列表对话框；显示当前 playlist、支持选择条目、添加文件/URL、切换随机和循环模式。
                - `SliderPickerDialog.kt`
                  作用：通用滑块式数值选择器，把整数 SeekBar 进度映射到指定浮点范围。
                - `SpeedPickerDialog.kt`
                  作用：播放速度选择器，使用非线性映射让常见速度档位更易选择。
                - `SubDelayDialog.kt`
                  作用：字幕延迟调整对话框，通过两个时间输入/增减控件计算并修改 subtitle delay。
                - `SubTrackDialog.kt`
                  作用：字幕轨选择对话框，支持主字幕和第二字幕轨切换，并避免同一轨同时作为主/副字幕。
                - `TouchGestures.kt`
                  作用：触控手势识别器；将拖动、双击等动作转换为 seek、音量、亮度或自定义命令变化。
                - `Utils.kt`
                  作用：通用工具集合；复制资产、生成 fontconfig 配置、查找真实路径、格式化时间、读取亮度、列存储卷、处理元数据/播放状态、创建 URL 对话框和处理窗口 insets。
                - `VideoThumbnailLoader.java`
                  作用：异步视频缩略图加载器；为文件/文档选择器加载媒体缩略图、目录图标并管理缓存与后台任务。
                - `preferences/`
                  作用：设置页 Activity 和自定义 Preference 实现。
                  - `AboutActivity.kt`
                    作用：关于/版本信息页面；通过 mpv 日志观察者收集版本输出并显示给用户。
                  - `ConfigEditDialogPreference.kt`
                    作用：可编辑配置文件的 Preference，对应 `mpv.conf`、`input.conf` 等文本配置编辑对话框。
                  - `InterpolationDialogPreference.kt`
                    作用：视频插值自定义设置项，组合插值开关和 video-sync 模式，并确保相关设置保持一致。
                  - `PreferenceActivity.kt`
                    作用：设置 Activity；承载各分类 PreferenceFragment，处理 Toolbar 返回、Fragment 导航和偏好变更后的界面更新。
                  - `ScalerDialogPreference.kt`
                    作用：缩放滤镜自定义设置项，编辑 scaler 名称及 param1/param2 参数。
        - `jni/`
          作用：C++ JNI 层，连接 Kotlin `MPVLib` 与 libmpv/native Android Surface。
          - `Android.mk`
            作用：NDK build 配置；按 ABI 引入预编译 FFmpeg/libmpv 动态库，并编译项目自己的 `libplayer` JNI 库。
          - `Application.mk`
            作用：NDK 应用配置；根据可用 prefix 决定 ABI，设置 API 21、`c++_shared` 和 flexible page size 支持。
          - `event.cpp`
            作用：mpv 事件线程实现；等待 mpv event/log/property，并通过 JNI 调回 `MPVLib` 静态回调方法。
          - `event.h`
            作用：事件线程启动/停止函数声明。
          - `globals.h`
            作用：声明全局 `mpv_handle *g_mpv`，供 JNI 源文件共享当前 mpv 实例。
          - `jni_utils.cpp`
            作用：初始化并缓存 Java 类、构造器和 `MPVLib` 回调方法 ID，降低事件回调时的 JNI 查找成本。
          - `jni_utils.h`
            作用：JNI 函数命名宏、缓存变量声明和工具函数声明。
          - `log.cpp`
            作用：JNI/native 日志辅助实现，把 native 日志写入 Android logcat。
          - `log.h`
            作用：日志宏、错误终止函数和 mpv 初始化检查宏声明。
          - `main.cpp`
            作用：`MPVLib` 的核心 JNI 实现；创建/初始化/销毁 mpv、设置 Android context、启动事件线程并执行 mpv 命令。
          - `property.cpp`
            作用：mpv 选项和属性 JNI 封装，提供 int/double/bool/string 的 get/set/observe。
          - `render.cpp`
            作用：Android Surface 绑定实现，把 native window 传给 mpv 的 `wid` 并在 Surface 销毁时解绑。
          - `thumbnail.cpp`
            作用：调用 mpv 截图/缩略图命令并把结果转换成 Android `Bitmap`。
        - `jniLibs`
          作用：指向 `libs/` 的符号链接；Gradle 从该路径收集按 ABI 生成的 native `.so` 库。
        - `res/`
          作用：Android 主资源目录。
          - `drawable/`
            作用：矢量图标、Logo 和简单形状资源。
            - `alpha_darken.xml`
              作用：半透明深色背景，用于选中/强调某些按钮区域。
            - `ic_audiotrack_black_24dp.xml`
              作用：音轨按钮图标。
            - `ic_file_open_48dp.xml`
              作用：打开文件入口图标。
            - `ic_filter_alt_24dp.xml`
              作用：文件列表过滤开关图标。
            - `ic_folder_white_48dp.xml`
              作用：白色文件夹图标。
            - `ic_link_48dp.xml`
              作用：打开 URL/链接入口图标。
            - `ic_lock_24dp.xml`
              作用：锁定播放器 UI 的图标。
            - `ic_lock_open_24dp.xml`
              作用：解锁播放器 UI 的图标。
            - `ic_mpv_symbolic.xml`
              作用：通知栏等小尺寸场景使用的 mpv 符号图标。
            - `ic_pause_black_24dp.xml`
              作用：暂停按钮图标。
            - `ic_picture_in_picture_24dp.xml`
              作用：进入画中画按钮图标。
            - `ic_play_arrow_black_24dp.xml`
              作用：播放按钮图标。
            - `ic_repeat_24dp.xml`
              作用：播放列表循环模式图标。
            - `ic_repeat_one_24dp.xml`
              作用：单项循环模式图标。
            - `ic_sd_card_white_24dp.xml`
              作用：外部存储/SD 卡入口图标。
            - `ic_settings_black_24dp.xml`
              作用：播放器或菜单中的设置图标。
            - `ic_settings_black_48dp.xml`
              作用：主屏/大尺寸设置入口图标。
            - `ic_shuffle_24dp.xml`
              作用：播放列表随机播放图标。
            - `ic_skip_next_black_24dp.xml`
              作用：下一项播放按钮图标。
            - `ic_skip_previous_black_24dp.xml`
              作用：上一项播放按钮图标。
            - `ic_subtitles_black_24dp.xml`
              作用：字幕轨按钮图标。
            - `mpv_logo.xml`
              作用：应用主 Logo 矢量图。
            - `mpv_monochrome.xml`
              作用：单色版 mpv 图标，适配部分系统图标场景。
            - `nnf_ic_folder_black_48dp.xml`
              作用：文件选择器目录项使用的黑色文件夹图标。
            - `round_code_24.xml`
              作用：开发者/代码类设置入口的圆角 Material 图标。
            - `round_gesture_24.xml`
              作用：手势设置入口图标。
            - `round_info_24.xml`
              作用：关于页面入口图标。
            - `round_palette_24.xml`
              作用：UI/主题类设置入口图标。
            - `round_play_arrow_24.xml`
              作用：通用播放圆角图标。
            - `round_settings_24.xml`
              作用：通用设置圆角图标。
            - `round_video_settings_24.xml`
              作用：视频设置入口图标。
          - `layout/`
            作用：竖屏/默认布局 XML。
            - `activity_about.xml`
              作用：关于页面默认布局，显示 Logo、版本/日志等内容。
            - `activity_filepicker.xml`
              作用：文件选择 Activity 容器布局，放置 FragmentContainerView 和辅助文本。
            - `activity_main.xml`
              作用：启动页 Activity 的 Fragment 容器。
            - `conf_editor.xml`
              作用：配置文件编辑对话框布局，提供多行文本编辑区域。
            - `dialog_advanced_menu.xml`
              作用：播放器高级菜单布局，包含外部音轨/字幕、比例、延迟、画面参数、PiP、方向等操作。
            - `dialog_decimal.xml`
              作用：十进制数值调整对话框布局，带主/副输入行和加减按钮。
            - `dialog_playlist.xml`
              作用：播放列表对话框布局，包含列表、添加文件/URL、随机和循环按钮。
            - `dialog_playlist_item.xml`
              作用：播放列表单行条目布局。
            - `dialog_slider.xml`
              作用：滑块数值选择器布局，显示当前值、重置按钮和 SeekBar。
            - `dialog_top_menu.xml`
              作用：播放器顶部菜单布局，包含音轨/字幕/速度/解码器/播放列表等快捷入口。
            - `dialog_track.xml`
              作用：轨道选择对话框布局，包含主/副字幕切换按钮和 RecyclerView。
            - `dialog_track_item.xml`
              作用：轨道选择列表的单个可勾选条目。
            - `fragment_filepicker_choice.xml`
              作用：文件选择方式选择页布局，提供 URL、系统文档、本地文件等入口。
            - `fragment_main_screen.xml`
              作用：应用启动主屏布局，显示 Logo 和打开文件/URL/设置等按钮。
            - `interpolation_pref.xml`
              作用：插值设置自定义 Preference 对话框布局。
            - `material_preferences_switch.xml`
              作用：替换 SwitchPreferenceCompat 的开关控件布局，使设置页开关更贴近 Material 风格。
            - `nnf_filepicker_listitem_checkable.xml`
              作用：文件选择器中文件项布局，带图标和名称。
            - `nnf_filepicker_listitem_dir.xml`
              作用：文件选择器中目录项布局，带目录图标和名称。
            - `nnf_fragment_filepicker.xml`
              作用：文件选择器列表 Fragment 布局，核心为 RecyclerView。
            - `player.xml`
              作用：播放器主界面布局，包含 `MPVView`、标题、进度条、播放控制、菜单按钮、统计文本、手势提示和锁屏控件。
            - `scaler_pref.xml`
              作用：缩放滤镜设置对话框布局，包含滤镜下拉和参数输入。
            - `version_dialog.xml`
              作用：版本信息/日志文本展示对话框布局。
          - `layout-land/`
            作用：横屏专用布局覆盖目录。
            - `activity_about.xml`
              作用：关于页面横屏布局，优化宽屏下 Logo 与文本排列。
          - `menu/`
            作用：Android menu 资源目录。
            - `menu_filepicker.xml`
              作用：文件选择器菜单，提供过滤切换等 ActionBar 菜单项。
          - `mipmap-anydpi-v26/`
            作用：Android 8+ 自适应图标资源目录。
            - `mpv_launcher_icon.xml`
              作用：自适应启动图标定义，组合前景图和背景色。
          - `mipmap-hdpi/`
            作用：hdpi 密度启动图资源。
            - `mpv_launcher_foreground.png`
              作用：hdpi 自适应图标前景位图。
            - `mpv_launcher_icon.png`
              作用：hdpi 传统启动图标位图。
          - `mipmap-mdpi/`
            作用：mdpi 密度启动图资源。
            - `mpv_launcher_foreground.png`
              作用：mdpi 自适应图标前景位图。
            - `mpv_launcher_icon.png`
              作用：mdpi 传统启动图标位图。
          - `mipmap-xhdpi/`
            作用：xhdpi 密度启动图和电视横幅资源。
            - `ic_banner.png`
              作用：Android TV/Leanback 使用的应用横幅图。
            - `mpv_launcher_foreground.png`
              作用：xhdpi 自适应图标前景位图。
            - `mpv_launcher_icon.png`
              作用：xhdpi 传统启动图标位图。
          - `mipmap-xxhdpi/`
            作用：xxhdpi 密度启动图资源。
            - `mpv_launcher_foreground.png`
              作用：xxhdpi 自适应图标前景位图。
            - `mpv_launcher_icon.png`
              作用：xxhdpi 传统启动图标位图。
          - `mipmap-xxxhdpi/`
            作用：xxxhdpi 密度启动图资源。
            - `mpv_launcher_foreground.png`
              作用：xxxhdpi 自适应图标前景位图。
            - `mpv_launcher_icon.png`
              作用：xxxhdpi 传统启动图标位图。
          - `values/`
            作用：默认语言和基础资源目录。
            - `arrays.xml`
              作用：选项数组资源，包含比例、后台播放、屏幕方向、scaler、deband、video-sync、统计模式和手势选项。
            - `attrs.xml`
              作用：自定义 View/Preference 属性定义，包括 scaler、interpolation、配置编辑器和文件选择器样式属性。
            - `colors.xml`
              作用：应用主色、按钮/SeekBar tint、透明遮罩和文件选择器分隔色资源。
            - `mpv_launcher_background.xml`
              作用：自适应启动图标背景色资源。
            - `strings.xml`
              作用：默认英文字符串，覆盖播放器 UI、菜单、设置项、文件选择器和对话框文案。
            - `styles.xml`
              作用：应用主题和样式定义，包括文件选择器主题、播放器主题、设置页 Material3 主题和 SwitchPreference 覆盖样式。
          - `values-ca/`
            作用：加泰罗尼亚语本地化资源。
            - `arrays.xml`
              作用：加泰罗尼亚语数组显示文本。
            - `strings.xml`
              作用：加泰罗尼亚语界面字符串。
          - `values-es/`
            作用：西班牙语本地化资源。
            - `arrays.xml`
              作用：西班牙语数组显示文本。
            - `strings.xml`
              作用：西班牙语界面字符串。
          - `values-et/`
            作用：爱沙尼亚语本地化资源。
            - `arrays.xml`
              作用：爱沙尼亚语数组显示文本。
            - `strings.xml`
              作用：爱沙尼亚语界面字符串。
          - `values-fa/`
            作用：波斯语本地化资源。
            - `arrays.xml`
              作用：波斯语数组显示文本。
            - `strings.xml`
              作用：波斯语界面字符串。
          - `values-it/`
            作用：意大利语本地化资源。
            - `arrays.xml`
              作用：意大利语数组显示文本。
            - `strings.xml`
              作用：意大利语界面字符串。
          - `values-ja/`
            作用：日语本地化资源。
            - `arrays.xml`
              作用：日语数组显示文本。
            - `strings.xml`
              作用：日语界面字符串。
          - `values-nb-rNO/`
            作用：挪威书面语本地化资源。
            - `arrays.xml`
              作用：挪威书面语数组显示文本。
            - `strings.xml`
              作用：挪威书面语界面字符串。
          - `values-pl/`
            作用：波兰语本地化资源。
            - `arrays.xml`
              作用：波兰语数组显示文本。
            - `strings.xml`
              作用：波兰语界面字符串。
          - `values-pt-rBR/`
            作用：巴西葡萄牙语本地化资源。
            - `arrays.xml`
              作用：巴西葡萄牙语数组显示文本。
            - `strings.xml`
              作用：巴西葡萄牙语界面字符串。
          - `values-ru/`
            作用：俄语本地化资源。
            - `arrays.xml`
              作用：俄语数组显示文本。
            - `strings.xml`
              作用：俄语界面字符串。
          - `values-tr/`
            作用：土耳其语本地化资源。
            - `arrays.xml`
              作用：土耳其语数组显示文本。
            - `strings.xml`
              作用：土耳其语界面字符串。
          - `values-uk/`
            作用：乌克兰语本地化资源。
            - `arrays.xml`
              作用：乌克兰语数组显示文本。
            - `strings.xml`
              作用：乌克兰语界面字符串。
          - `values-zh-rCN/`
            作用：简体中文本地化资源。
            - `arrays.xml`
              作用：简体中文数组显示文本。
            - `strings.xml`
              作用：简体中文界面字符串。
          - `xml/`
            作用：非布局 XML 配置资源目录。
            - `locales_config.xml`
              作用：Android 13+ 应用语言列表配置，声明支持的 locale。
            - `pref_advanced.xml`
              作用：高级设置页面，包含 gpu-next 和 `mpv.conf`/`input.conf` 编辑入口。
            - `pref_developer.xml`
              作用：开发者设置页面，包含统计显示、忽略音频焦点和 OpenGL debug。
            - `pref_general.xml`
              作用：通用设置页面，包含默认路径、语言、动态主题、硬解、后台播放、保存进度和新 Intent 行为。
            - `pref_gestures.xml`
              作用：触控手势设置页面，配置滑动、双击和自定义手势说明。
            - `pref_ui.xml`
              作用：用户界面设置页面，配置方向、标题显示、底部控制、弹窗播放和 playlist 退出确认。
            - `pref_video.xml`
              作用：视频设置页面，配置 scaler、deband、插值、tscale 和低质量解码。
            - `preferences_root.xml`
              作用：设置首页目录，链接到各分类 Fragment 和关于页面。
  - `buildscripts/`
    作用：原生依赖、libmpv 和 Android APK 的下载/交叉编译脚本集合。
    - `.gitignore`
      作用：忽略 `deps`、`sdk`、`prefix` 和下载缓存包等构建脚本产物。
    - `README.md`
      作用：原生依赖下载、构建、单组件重建、logcat 和 Android Studio 使用说明。
    - `buildall.sh`
      作用：原生构建总入口；选择 ABI，设置 NDK 工具链和 Meson cross file，按依赖树递归构建目标并列出 APK 输出。
    - `download.sh`
      作用：下载准备总入口，依次运行 SDK/NDK 下载和第三方源码下载脚本。
    - `include/`
      作用：构建脚本共用函数、版本和环境配置。
      - `ci.sh`
        作用：CI 专用流程；导出缓存 key、安装 SDK/依赖、构建或恢复 prefix、编译 mpv 与 app。
      - `depinfo.sh`
        作用：集中定义 SDK/NDK/第三方库版本、依赖关系和 CI prefix 缓存标识。
      - `download-deps.sh`
        作用：下载或 clone mbedtls、dav1d、FFmpeg、freetype、fribidi、harfbuzz、unibreak、libxml2、fontconfig、libass、Lua、libplacebo 和 mpv 源码。
      - `download-sdk.sh`
        作用：安装主机依赖，下载 Android commandline tools、SDK platform/build-tools、NDK 和 gas-preprocessor。
      - `path.sh`
        作用：检测 Linux/macOS、设置核心数、GNU 工具名和 `ANDROID_HOME`。
    - `scripts/`
      作用：各 native 依赖和最终应用的单独构建脚本。
      - `dav1d.sh`
        作用：用 Meson/Ninja 交叉编译 AV1 解码库 dav1d。
      - `ffmpeg.sh`
        作用：为 Android 交叉编译共享版 FFmpeg，启用 JNI、MediaCodec、mbedtls、dav1d、libxml2，并裁剪编码器/设备/程序等不需要组件。
      - `fontconfig.sh`
        作用：用 Meson/Ninja 交叉编译 fontconfig，供字幕字体匹配使用。
      - `freetype2.sh`
        作用：用 Meson/Ninja 交叉编译 FreeType 字体渲染库。
      - `fribidi.sh`
        作用：用 Meson/Ninja 交叉编译 FriBidi 双向文字处理库。
      - `harfbuzz.sh`
        作用：用 Meson/Ninja 交叉编译 HarfBuzz 字形 shaping 库，并关闭测试/文档/部分可选功能。
      - `libass.sh`
        作用：用 autotools/make 交叉编译静态 libass，启用 fontconfig 和 libunibreak。
      - `libplacebo.sh`
        作用：用 Meson/Ninja 交叉编译 libplacebo，禁用 Vulkan/demo，并修正 pkg-config 链接 flags。
      - `libxml2.sh`
        作用：用 Meson/Ninja 交叉编译精简 libxml2，供 FFmpeg/fontconfig 等组件使用。
      - `lua.sh`
        作用：交叉编译 Lua 5.2 静态库，添加 Android/Bionic 兼容宏并生成 pkg-config 文件。
      - `mbedtls.sh`
        作用：交叉编译 mbedTLS，按 x86/非 x86 调整 AESNI 配置并跳过测试。
      - `mpv-android.sh`
        作用：最终应用构建脚本；检测各 ABI prefix，运行 `ndk-build` 生成 `libplayer`，执行 Gradle assemble/bundle，并在配置签名变量时签名 APK/AAB。
      - `mpv.sh`
        作用：用 Meson/Ninja 交叉编译 libmpv 共享库，启用 Lua/libmpv，关闭命令行播放器和 manpage。
      - `unibreak.sh`
        作用：用 configure/make 交叉编译静态 libunibreak。
  - `docs/`
    作用：项目 GitHub Pages/发布辅助文档目录。
    - `default.css`
      作用：文档站页面的基础 CSS 样式。
    - `index.html`
      作用：文档站首页，链接 Intent 规范和隐私政策。
    - `intent.html`
      作用：外部应用调用 mpv-android 的 Intent 规范文档，包括 URI/MIME、extras、示例和结果 Intent。
    - `prepare_artifacts.sh`
      作用：发布前整理产物，把签名 APK/AAB、debug object 和符号 ZIP 复制到 `output/github` 与 `output/googleplay`。
    - `print_releasenotes.sh`
      作用：根据当前源码和 `buildscripts/deps` 中的依赖 commit/version 打印发布说明依赖列表。
    - `privacy.html`
      作用：应用隐私政策页面。
    - `release_process.md`
      作用：正式发版步骤清单，覆盖版本号、全架构构建、测试、产物准备、GitHub Release 和 Google Play 发布。
  - `fastlane/`
    作用：应用商店元数据目录，供 fastlane/Google Play/F-Droid 等发布流程复用。
    - `metadata/`
      作用：fastlane 元数据根目录。
      - `android/`
        作用：Android 商店描述、标题、截图和图标元数据。
        - `en-US/`
          作用：英语商店元数据。
          - `full_description.txt`
            作用：英语完整商店描述。
          - `short_description.txt`
            作用：英语短描述。
          - `title.txt`
            作用：英语应用标题。
          - `images/`
            作用：英语商店图片资源。
            - `icon.png`
              作用：512x512 商店图标。
            - `tvBanner.png`
              作用：Android TV 商店横幅图。
            - `phoneScreenshots/`
              作用：手机截图目录。
              - `1.jpg`
                作用：第一张手机商店截图。
              - `2.png`
                作用：第二张手机商店截图。
              - `3.png`
                作用：第三张手机商店截图。
            - `tenInchScreenshots/`
              作用：10 英寸平板截图目录。
              - `1.jpg`
                作用：第一张平板商店截图。
              - `2.png`
                作用：第二张平板商店截图。
              - `3.png`
                作用：第三张平板商店截图。
        - `es-ES/`
          作用：西班牙语商店元数据。
          - `full_description.txt`
            作用：西班牙语完整商店描述。
          - `short_description.txt`
            作用：西班牙语短描述。
        - `et-EE/`
          作用：爱沙尼亚语商店元数据。
          - `full_description.txt`
            作用：爱沙尼亚语完整商店描述。
          - `short_description.txt`
            作用：爱沙尼亚语短描述。
        - `fa-IR/`
          作用：波斯语商店元数据。
          - `full_description.txt`
            作用：波斯语完整商店描述。
          - `short_description.txt`
            作用：波斯语短描述。
          - `title.txt`
            作用：波斯语应用标题。
        - `it-IT/`
          作用：意大利语商店元数据。
          - `full_description.txt`
            作用：意大利语完整商店描述。
          - `short_description.txt`
            作用：意大利语短描述。
        - `nl-NL/`
          作用：荷兰语商店元数据。
          - `full_description.txt`
            作用：荷兰语完整商店描述。
          - `short_description.txt`
            作用：荷兰语短描述。
        - `pl-PL/`
          作用：波兰语商店元数据。
          - `full_description.txt`
            作用：波兰语完整商店描述。
          - `short_description.txt`
            作用：波兰语短描述。
        - `ru-RU/`
          作用：俄语商店元数据。
          - `full_description.txt`
            作用：俄语完整商店描述。
          - `short_description.txt`
            作用：俄语短描述。
        - `tr-TR/`
          作用：土耳其语商店元数据。
          - `full_description.txt`
            作用：土耳其语完整商店描述。
          - `short_description.txt`
            作用：土耳其语短描述。
        - `zh-CN/`
          作用：简体中文商店元数据。
          - `full_description.txt`
            作用：简体中文完整商店描述。
          - `short_description.txt`
            作用：简体中文短描述。
  - `gradle/`
    作用：Gradle Wrapper 支持文件目录。
    - `wrapper/`
      作用：Gradle Wrapper 二进制和版本配置目录。
      - `gradle-wrapper.jar`
        作用：Gradle Wrapper 启动器二进制，用于自动下载并运行指定 Gradle。
      - `gradle-wrapper.properties`
        作用：Gradle Wrapper 配置，指定 Gradle 9.3.1 bin 发行版下载地址和校验/缓存位置。
