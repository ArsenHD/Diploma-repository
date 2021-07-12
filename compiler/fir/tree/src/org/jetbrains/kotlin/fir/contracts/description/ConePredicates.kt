/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.contracts.description

import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeKotlinType

class ConeIsInstancePredicate(val arg: ConeValueParameterReference, val type: ConeKotlinType, val isNegated: Boolean) : ConeBooleanExpression {
    override fun <R, D> accept(contractDescriptionVisitor: ConeContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitIsInstancePredicate(this, data)

    fun negated(): ConeIsInstancePredicate =
        ConeIsInstancePredicate(arg, type, isNegated.not())
}

class ConeIsNullPredicate(val arg: ConeValueParameterReference, val isNegated: Boolean) : ConeBooleanExpression {
    override fun <R, D> accept(contractDescriptionVisitor: ConeContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitIsNullPredicate(this, data)

    fun negated(): ConeIsNullPredicate =
        ConeIsNullPredicate(arg, isNegated.not())
}

class ConeSatisfiesPredicate(
    val value: ConeValueParameterReference,
    val predicateReferences: List<FirCallableReferenceAccess>,
    val satisfiesCall: FirFunctionCall
) : ConeBooleanExpression {

    constructor(
        value: ConeValueParameterReference,
        predicate: FirCallableReferenceAccess,
        satisfiesCall: FirFunctionCall
    ) : this(value, listOf(predicate), satisfiesCall)

    override fun <R, D> accept(contractDescriptionVisitor: ConeContractDescriptionVisitor<R, D>, data: D): R =
        contractDescriptionVisitor.visitSatisfiesPredicate(this, data)

    val predicates: List<FirNamedFunctionSymbol>
        get() = predicateReferences
            .mapNotNull { it.toResolvedCallableSymbol() as? FirNamedFunctionSymbol }
}
