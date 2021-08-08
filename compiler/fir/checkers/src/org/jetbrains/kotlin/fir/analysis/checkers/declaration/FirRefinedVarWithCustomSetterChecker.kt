/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.declaration

import org.jetbrains.kotlin.fir.FirSourceElement
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.util.toRefinedType
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.diagnostics.reportOn
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertySetter

object FirRefinedVarWithCustomSetterChecker : FirPropertyChecker() {
    override fun check(declaration: FirProperty, context: CheckerContext, reporter: DiagnosticReporter) {
        val session = declaration.symbol.moduleData.session
        val setter = declaration.setter ?: return
        if (setter is FirDefaultPropertySetter) return
        declaration.returnTypeRef.toRefinedType(session) ?: return
        // here it is known that the given property has a custom setter and has a refined type
        reporter.reportError(declaration.source, context)
    }

    private fun DiagnosticReporter.reportError(source: FirSourceElement?, context: CheckerContext) {
        reportOn(source, FirErrors.REFINED_VAR_WITH_CUSTOM_SETTER, context)
    }
}