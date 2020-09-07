/****************************************************************************************
 *                                                                                      *
 *  * Copyright (C)  2020  Shabinder Singh                                              *
 *  *                                                                                   *
 *  * This program is free software: you can redistribute it and/or modify              *
 *  * it under the terms of the GNU General Public License as published by              *
 *  * the Free Software Foundation, either version 3 of the License, or                 *
 *  * (at your option) any later version.                                               *
 *  *                                                                                   *
 *  * This program is distributed in the hope that it will be useful,                   *
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of                    *
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                     *
 *  * GNU General Public License for more details.                                      *
 *  *                                                                                   *
 *  * You should have received a copy of the GNU General Public License                 *
 *  * along with this program.  If not, see <https://www.gnu.org/licenses/>.            *
 *                                                                                      *
 ****************************************************************************************/

plugins {
    kotlin("jvm") version "1.4.0"
    id("com.github.johnrengelman.shadow") version "6.0.0"
    id ("com.heroku.sdk.heroku-gradle") version "2.0.0"
    kotlin("plugin.serialization") version "1.4.0"
    idea
    application
}
heroku {
    appName = "wacebot"
    includes = mutableListOf(
        "build/libs/wace-1.0-all.jar"
    )
    isIncludeBuildDir = false
    jdkVersion = "1.8"
    processTypes = mutableMapOf<String,String>(
        "worker" to "java -jar build/libs/wace-1.0-all.jar"
    )
}

group = "com.shabinder.wace"
version = "1.0"

application {
    mainClassName = "com.shabinder.wace.MainKt"
}
repositories {
    maven(url = "https://jitpack.io")
    mavenCentral()
    jcenter()
}
dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin","kotlin-reflect","1.4.0")
    implementation("org.jetbrains.kotlinx","kotlinx-serialization-core","1.0.0-RC")
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot","telegram","5.0.0")
    implementation("org.jetbrains.exposed", "exposed-core", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.24.1")
    implementation("org.postgresql", "postgresql", "42.2.2")
    implementation("org.slf4j", "slf4j-nop", "1.7.30")
}
tasks {
    withType<Jar> {
        manifest {
            attributes(
                    mapOf(
                            "Main-Class" to application.mainClassName
                    )
            )
        }
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
    register("stage") {//Heroku Deployment
        dependsOn("build")
    }
}
