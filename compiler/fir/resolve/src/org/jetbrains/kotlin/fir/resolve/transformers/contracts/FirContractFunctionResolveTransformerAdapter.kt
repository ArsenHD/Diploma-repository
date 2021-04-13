/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.contracts

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.resolve.transformers.AdapterForResolveProcessor
import org.jetbrains.kotlin.fir.resolve.transformers.FirTransformerBasedResolveProcessor
import org.jetbrains.kotlin.fir.visitors.FirTransformer

@OptIn(AdapterForResolveProcessor::class)
class FirContractFunctionResolveProcessor(session: FirSession, scopeSession: ScopeSession) : FirTransformerBasedResolveProcessor(session, scopeSession) {
    override val transformer = FirContractFunctionResolveTransformerAdapter(session, scopeSession)
}

@AdapterForResolveProcessor
class FirContractFunctionResolveTransformerAdapter(session: FirSession, scopeSession: ScopeSession) : FirTransformer<Nothing?>() {
    private val transformer = FirContractFunctionResolveTransformer(session, scopeSession)
    override fun <E : FirElement> transformElement(element: E, data: Nothing?): E {
        return element
    }

    override fun transformFile(file: FirFile, data: Nothing?): FirDeclaration {
        return file.transform(transformer, ResolutionMode.ContextIndependent)
    }
}