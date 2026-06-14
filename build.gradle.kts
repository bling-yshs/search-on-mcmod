
plugins {
    id("com.gtnewhorizons.gtnhconvention")
}

version = providers.gradleProperty("modVersion").get()

tasks.withType<Jar>().configureEach {
    archiveBaseName.set(
        "${providers.gradleProperty("modId").get()}-forge-${providers.gradleProperty("minecraftVersion").get()}")
    archiveVersion.set(project.version.toString())
}

tasks.register("printModVersion") {
    group = "help"
    description = "打印模组版本号，用于 CI/CD 工作流"

    val projectVersion = project.version
    doLast {
        println(projectVersion)
    }
}
