# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/zhangshaowen/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
-keepattributes SourceFile,LineNumberTable

-dontwarn com.google.**

-dontwarn com.android.**


-dontwarn javax.swing.JFrame
-dontwarn dalvik.system.DalvikLogHandler
-dontwarn android.compat.**
-dontwarn javax.swing.event.TreeSelectionListener
-dontwarn java.lang.ClassValue
-dontwarn org.mockito.internal.creation.bytebuddy.MockMethodDispatcher
-dontwarn javax.swing.tree.TreeModel
-dontwarn javax.swing.JPanel
-dontwarn org.apache.harmony.dalvik.ddmc.ChunkHandler
-dontwarn libcore.net.event.NetworkEventListener
-dontwarn org.apache.tools.ant.taskdefs.PumpStreamHandler
-dontwarn org.jmock.core.Constraint
-dontwarn org.yaml.snakeyaml.constructor.**
-dontwarn dalvik.system.SocketTagger
-dontwarn dalvik.system.**
-dontwarn libcore.net.NetworkSecurityPolicy
-dontwarn dalvik.system.**
-dontwarn java.lang.instrument.ClassFileTransformer
-dontwarn dalvik.system.ThreadPrioritySetter
-dontwarn libcore.io.ForwardingOs
-dontwarn java.awt.event.WindowAdapter
-dontwarn org.apache.tools.ant.taskdefs.LogOutputStream
-dontwarn org.apache.tools.ant.Task
-dontwarn dalvik.system.**
-dontwarn dalvik.system.**
-dontwarn org.yaml.snakeyaml.constructor.Constructor

