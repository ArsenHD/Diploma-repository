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
import org.jetbrains.kotlin.fir.declarations.builder.FirAbstractTypeAliasBuilder
import org.jetbrains.kotlin.fir.declarations.builder.FirDeclarationBuilder
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
class FirRefinedTypeBuilder : FirDeclarationBuilder, FirAbstractTypeAliasBuilder, FirAnnotationContainerBuilder {
    override var source: FirSourceElement? = null
    override lateinit var moduleData: FirModuleData
    override var resolvePhase: FirResolvePhase = FirResolvePhase.RAW_FIR
    override lateinit var origin: FirDeclarationOrigin
    override var attributes: FirDeclarationAttributes = FirDeclarationAttributes()
    override var deprecation: DeprecationsPerUseSite? = null
    override lateinit var status: FirDeclarationStatus
    override val typeParameters: MutableList<FirTypeParameter> = mutableListOf()
    override lateinit var name: Name
    override lateinit var symbol: FirTypeAliasSymbol
    override lateinit var expandedTypeRef: FirTypeRef
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

@OptIn(ExperimentalContracts::class)
inline fun buildRefinedTypeCopy(original: FirRefinedType, init: FirRefinedTypeBuilder.() -> Unit): FirRefinedType {
    contract {
        callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)
    }
    val copyBuilder = FirRefinedTypeBuilder()
    copyBuilder.source = original.source
    copyBuilder.moduleData = original.moduleData
    copyBuilder.resolvePhase = original.resolvePhase
    copyBuilder.origin = original.origin
    copyBuilder.attributes = original.attributes.copy()
    copyBuilder.deprecation = original.deprecation
    copyBuilder.status = original.status
    copyBuilder.typeParameters.addAll(original.typeParameters)
    copyBuilder.name = original.name
    copyBuilder.symbol = original.symbol
    copyBuilder.expandedTypeRef = original.expandedTypeRef
    copyBuilder.annotations.addAll(original.annotations)
    copyBuilder.constraints.addAll(original.constraints)
    return copyBuilder.apply(init).build()
}
