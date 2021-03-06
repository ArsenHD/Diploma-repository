/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir

import org.jetbrains.kotlin.test.WrappedException
import org.jetbrains.kotlin.test.model.AfterAnalysisChecker
import org.jetbrains.kotlin.test.model.FrontendKinds
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.moduleStructure

class FirFailingTestSuppressor(testServices: TestServices) : AfterAnalysisChecker(testServices) {
    override fun suppressIfNeeded(failedAssertions: List<WrappedException>): List<WrappedException> {
        val testFile = testServices.moduleStructure.originalTestDataFiles.first()
        val failFile = testFile.parentFile.resolve("${testFile.nameWithoutExtension}.fail")
        val exceptionFromFir =
            failedAssertions.firstOrNull {
                when (it) {
                    is WrappedException.FromFacade -> it.facade is FirFrontendFacade
                    is WrappedException.FromHandler -> it.handler.artifactKind == FrontendKinds.FIR
                    else -> false
                }
            }
        return when {
            failFile.exists() && exceptionFromFir != null -> emptyList()
            failFile.exists() && exceptionFromFir == null -> {
                failedAssertions + AssertionError("Fail file exists but no exception was throw. Please remove ${failFile.name}").wrap()
            }
            else -> failedAssertions
        }
    }
}
