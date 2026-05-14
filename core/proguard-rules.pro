-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep public API
-keep class com.ae.log.AELog { *; }
-keep class com.ae.log.AELogProviderKt { *; }
-keep class com.ae.log.core.** { *; }
-keep class com.ae.log.plugins.log.model.** { *; }
-keep class com.ae.log.plugins.log.store.LogStore { *; }
-keep class com.ae.log.plugins.log.LogPlugin { *; }

# Keep plugin interfaces for consumers
-keep interface com.ae.log.core.AELogPlugin { *; }
-keep interface com.ae.log.core.UIPlugin { *; }
-keep interface com.ae.log.core.DataPlugin { *; }

# Kotlinx serialization
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keep class kotlinx.serialization.** { *; }
