
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

tasks.register("printModVersion") {
    group = "help"
    description = "打印模组版本号，用于 CI/CD 工作流"

    val projectVersion = project.version
    doLast {
        println(projectVersion)
    }
}
