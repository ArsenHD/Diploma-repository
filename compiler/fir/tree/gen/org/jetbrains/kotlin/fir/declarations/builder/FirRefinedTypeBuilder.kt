/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.builder

import kotlin.contracts.*
import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSourceElement
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.declarations.DeprecationsPerUseSite
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirRefinedType
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirRefinedTypeImpl
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.name.Name

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
class FirRefinedTypeBuilder : FirAnnotationContainerBuilder {
    override var source: FirSourceElement? = null
    lateinit var moduleData: FirModuleData
    var resolvePhase: FirResolvePhase = FirResolvePhase.RAW_FIR
    lateinit var origin: FirDeclarationOrigin
    var attributes: FirDeclarationAttributes = FirDeclarationAttributes()
    var deprecation: DeprecationsPerUseSite? = null
    lateinit var status: FirDeclarationStatus
    val typeParameters: MutableList<FirTypeParameter> = mutableListOf()
    lateinit var name: Name
    lateinit var symbol: FirTypeAliasSymbol
    lateinit var expandedTypeRef: FirTypeRef
    override val annotations: MutableList<FirAnnotationCall> = mutableListOf()
    val constraints: MutableList<FirCallableReferenceAccess> = mutableListOf()

    override fun build(): FirRefinedType {
        return FirRefinedTypeImpl(
            source,
            moduleData,
            resolvePhase,
            origin,
            attributes,
            deprecation,
            status,
            typeParameters,
            name,
            symbol,
            expandedTypeRef,
            annotations,
            constraints,
        )
    }

}

@OptIn(ExperimentalContracts::class)
inline fun buildRefinedType(init: FirRefinedTypeBuilder.() -> Unit): FirRefinedType {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    return FirRefinedTypeBuilder().apply(init).build()
}
