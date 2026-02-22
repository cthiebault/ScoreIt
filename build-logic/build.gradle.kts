plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.plugins.android.application.get().let {
        "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}"
    })
}
