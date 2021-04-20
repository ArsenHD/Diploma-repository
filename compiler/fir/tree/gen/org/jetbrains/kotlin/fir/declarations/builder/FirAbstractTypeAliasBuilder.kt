/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations.builder

import org.jetbrains.kotlin.fir.FirModuleData
import org.jetbrains.kotlin.fir.FirSourceElement
import org.jetbrains.kotlin.fir.builder.FirAnnotationContainerBuilder
import org.jetbrains.kotlin.fir.builder.FirBuilderDsl
import org.jetbrains.kotlin.fir.declarations.DeprecationsPerUseSite
import org.jetbrains.kotlin.fir.declarations.FirDeclarationAttributes
import org.jetbrains.kotlin.fir.declarations.FirDeclarationOrigin
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.declarations.FirTypeAlias
import org.jetbrains.kotlin.fir.declarations.FirTypeParameter
import org.jetbrains.kotlin.fir.declarations.builder.FirTypeParametersOwnerBuilder
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.visitors.*
import org.jetbrains.kotlin.name.Name

/*
 * This file was generated automatically
 * DO NOT MODIFY IT MANUALLY
 */

@FirBuilderDsl
interface FirAbstractTypeAliasBuilder : FirTypeParametersOwnerBuilder, FirAnnotationContainerBuilder {
    abstract override var source: FirSourceElement?
    abstract override val typeParameters: MutableList<FirTypeParameter>
    abstract override val annotations: MutableList<FirAnnotationCall>
    abstract var moduleData: FirModuleData
    abstract var resolvePhase: FirResolvePhase
    abstract var origin: FirDeclarationOrigin
    abstract var attributes: FirDeclarationAttributes
    abstract var deprecation: DeprecationsPerUseSite?
    abstract var status: FirDeclarationStatus
    abstract var name: Name
    abstract var symbol: FirTypeAliasSymbol
    abstract var expandedTypeRef: FirTypeRef
    override fun build(): FirTypeAlias
}
