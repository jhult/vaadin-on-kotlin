dependencies {
    compile(project(":vok-framework"))
    compile("com.github.mvysny.vokorm:vok-orm:0.16")
    testCompile("com.github.mvysny.dynatest:dynatest-engine:${ext["dynatest_version"]}")
}

val configureBintray = ext["configureBintray"] as (artifactId: String, description: String) -> Unit
configureBintray("vok-db", "VoK: A very simple persistence framework built on top of vok-orm/Sql2o")
