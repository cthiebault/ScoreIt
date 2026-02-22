plugins {
    jacoco
}

(extensions.getByName("android") as com.android.build.api.dsl.CommonExtension).buildTypes {
    named("debug") {
        enableUnitTestCoverage = true
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        html.required.set(true)
        xml.required.set(true)
    }

    sourceDirectories.setFrom("${projectDir}/src/main/kotlin")

    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes"))
    )

    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}
