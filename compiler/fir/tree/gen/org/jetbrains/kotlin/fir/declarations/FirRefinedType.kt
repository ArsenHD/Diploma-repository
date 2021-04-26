/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSourceElement
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.fir.visitors.*

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

abstract class FirRefinedType : FirTypeAlias() {
    abstract override val source: FirSourceElement?
    abstract override val moduleData: FirModuleData
    abstract override val resolvePhase: FirResolvePhase
    abstract override val origin: FirDeclarationOrigin
    abstract override val attributes: FirDeclarationAttributes
    abstract override val deprecation: DeprecationsPerUseSite?
    abstract override val status: FirDeclarationStatus
    abstract override val typeParameters: List<FirTypeParameter>
    abstract override val name: Name
    abstract override val symbol: FirTypeAliasSymbol
    abstract override val expandedTypeRef: FirTypeRef
    abstract override val annotations: List<FirAnnotationCall>
    abstract val constraints: List<FirCallableReferenceAccess>

    override fun <R, D> accept(visitor: FirVisitor<R, D>, data: D): R = visitor.visitRefinedType(this, data)

    @Suppress("UNCHECKED_CAST")
    override fun <E: FirElement, D> transform(transformer: FirTransformer<D>, data: D): E = 
        transformer.transformRefinedType(this, data) as E

    abstract override fun replaceResolvePhase(newResolvePhase: FirResolvePhase)

    abstract override fun replaceDeprecation(newDeprecation: DeprecationsPerUseSite?)

    abstract override fun replaceExpandedTypeRef(newExpandedTypeRef: FirTypeRef)

    abstract override fun <D> transformStatus(transformer: FirTransformer<D>, data: D): FirRefinedType

    abstract override fun <D> transformTypeParameters(transformer: FirTransformer<D>, data: D): FirRefinedType

    abstract override fun <D> transformExpandedTypeRef(transformer: FirTransformer<D>, data: D): FirRefinedType

    abstract override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirRefinedType

    abstract fun <D> transformConstraints(transformer: FirTransformer<D>, data: D): FirRefinedType
}
