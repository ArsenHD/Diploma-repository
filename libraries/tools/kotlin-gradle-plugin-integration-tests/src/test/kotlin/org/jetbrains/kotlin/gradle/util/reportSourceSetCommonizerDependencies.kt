/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.util

import org.intellij.lang.annotations.Language
import org.intellij.lang.annotations.RegExp
import org.jetbrains.kotlin.commonizer.CommonizerTarget
import org.jetbrains.kotlin.commonizer.SharedCommonizerTarget
import org.jetbrains.kotlin.commonizer.parseCommonizerTargetOrNull
import org.jetbrains.kotlin.commonizer.util.transitiveClosure
import org.jetbrains.kotlin.gradle.BaseGradleIT
import org.jetbrains.kotlin.gradle.BaseGradleIT.CompiledProject
import java.io.File
import javax.annotation.RegEx
import kotlin.test.fail

data class SourceSetCommonizerDependency(
    val sourceSetName: String,
    val target: CommonizerTarget,
    val file: File
)

data class SourceSetCommonizerDependencies(
    val sourceSetName: String,
    val dependencies: Set<SourceSetCommonizerDependency>
) {

    fun onlyCInterops(): SourceSetCommonizerDependencies {
        return SourceSetCommonizerDependencies(
            sourceSetName,
            dependencies.filter { dependency ->
                /* Cinterop dependencies are located in the project's '.gradle' directory */
                dependency.file.allParents.any { parentFile -> parentFile.name == ".gradle" }
            }.toSet()
        )
    }

    fun assertTargetOnAllDependencies(target: CommonizerTarget) {
        dependencies.forEach { dependency ->
            if (dependency.target != target) {
                fail("$sourceSetName: Expected target $target but found dependency with target ${dependency.target}\n$dependency")
            }
        }
    }

    fun assertEmpty() {
        if (dependencies.isNotEmpty()) {
            fail("$sourceSetName: Expected no dependencies in set. Found $dependencies")
        }
    }

    fun assertDependencyFilesMatches(@Language("RegExp") @RegEx @RegExp vararg fileMatchers: String?) {
        assertDependencyFilesMatches(fileMatchers.filterNotNull().map(::Regex).toSet())
    }

    fun assertDependencyFilesMatches(vararg fileMatchers: Regex?) {
        assertDependencyFilesMatches(fileMatchers.filterNotNull().toSet())
    }

    fun assertDependencyFilesMatches(fileMatchers: Set<Regex>) {
        val unmatchedDependencies = dependencies.filter { dependency ->
            fileMatchers.none { matcher -> dependency.file.absolutePath.matches(matcher) }
        }

        val unmatchedMatchers = fileMatchers.filter { matcher ->
            dependencies.none { dependency -> dependency.file.absolutePath.matches(matcher) }
        }

        if (unmatchedDependencies.isNotEmpty() || unmatchedMatchers.isNotEmpty()) {
            fail(buildString {
                appendLine("$sourceSetName: Set of commonizer dependencies does not match given 'fileMatchers'")
                if (unmatchedDependencies.isNotEmpty()) {
                    appendLine("Unmatched dependencies: $unmatchedDependencies")
                }
                if (unmatchedMatchers.isNotEmpty()) {
                    appendLine("Unmatched fileMatchers: $unmatchedMatchers")
                }
            })
        }
    }
}

fun interface WithSourceSetCommonizerDependencies {
    fun getCommonizerDependencies(sourceSetName: String): SourceSetCommonizerDependencies
}

fun BaseGradleIT.reportSourceSetCommonizerDependencies(
    project: BaseGradleIT.Project,
    vararg additionalBuildParameters: String,
    test: WithSourceSetCommonizerDependencies.(compiledProject: CompiledProject) -> Unit
) = with(project) {

    if (!projectDir.exists()) {
        setupWorkingDir()
    }

    gradleBuildScript().apply {
        appendText("\n\n")
        appendText(taskSourceCode)
        appendText("\n\n")
    }

    build(
        *(listOf(":reportCommonizerSourceSetDependencies") + additionalBuildParameters).toTypedArray(),
    ) {
        assertSuccessful()

        val dependencyReports = output.lineSequence().filter { line -> line.contains("SourceSetCommonizerDependencyReport") }.toList()

        val withSourceSetCommonizerDependencies = WithSourceSetCommonizerDependencies { sourceSetName ->
            val reportMarker = "Report[$sourceSetName]"

            val reportForSourceSet = dependencyReports.firstOrNull { line -> line.contains(reportMarker) }
                ?: fail("Missing dependency report for $sourceSetName")

            val files = reportForSourceSet.split(reportMarker, limit = 2).last().split("|#+#|").map(::File)
            val dependencies = files.mapNotNull { file ->
                val allParents = file.allParents
                if (allParents.any { it.name == "commonized" } || allParents.any { it.name == "commonizer" }) {
                    val target = parseCommonizerTargetOrNull(file.parentFile.name) as? SharedCommonizerTarget ?: return@mapNotNull null
                    SourceSetCommonizerDependency(sourceSetName, target, file)
                } else null
            }
            SourceSetCommonizerDependencies(sourceSetName, dependencies.toSet())
        }

        withSourceSetCommonizerDependencies.test(this)
    }
}

private val File.allParents: Set<File> get() = transitiveClosure(this) { listOfNotNull(parentFile) }

private const val dollar = "\$"

private val taskSourceCode = """
tasks.register("reportCommonizerSourceSetDependencies") {
    kotlin.sourceSets.withType(DefaultKotlinSourceSet::class).all {
        inputs.files(configurations.getByName(intransitiveMetadataConfigurationName))
    }

    doLast {
        kotlin.sourceSets.filterIsInstance<DefaultKotlinSourceSet>().forEach { sourceSet ->
            val configuration = configurations.getByName(sourceSet.intransitiveMetadataConfigurationName)
            val dependencies = configuration.files

            logger.quiet(
                "SourceSetCommonizerDependencyReport[$dollar{sourceSet.name}]$dollar{dependencies.joinToString("|#+#|")}"
            )
        }
    }
}
""".trimIndent()