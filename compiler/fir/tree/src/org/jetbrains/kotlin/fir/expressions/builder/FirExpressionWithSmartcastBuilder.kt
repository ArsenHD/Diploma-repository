/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.expressions.builder

import org.jetbrains.kotlin.fir.expressions.FirExpressionWithSmartcast
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.impl.FirExpressionWithSmartcastImpl
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.types.SmartcastStability

class FirExpressionWithSmartcastBuilder {
    lateinit var originalExpression: FirQualifiedAccessExpression
    lateinit var smartcastType: FirTypeRef
    lateinit var typesFromSmartCast: Collection<ConeKotlinType>
    lateinit var smartcastStability: SmartcastStability

    fun build(): FirExpressionWithSmartcast {
        return FirExpressionWithSmartcastImpl(originalExpression, smartcastType, typesFromSmartCast, smartcastStability, listOf())
    }
}

inline fun buildExpressionWithSmartcast(init: FirExpressionWithSmartcastBuilder.() -> Unit): FirExpressionWithSmartcast {
    return FirExpressionWithSmartcastBuilder().apply(init).build()
}