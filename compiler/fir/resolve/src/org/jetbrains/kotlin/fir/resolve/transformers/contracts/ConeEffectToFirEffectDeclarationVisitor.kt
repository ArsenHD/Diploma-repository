/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.contracts

import org.jetbrains.kotlin.fir.contracts.FirEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.builder.buildEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.description.*
import org.jetbrains.kotlin.fir.contracts.toFirEffectDeclaration
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirBodyResolveTransformer
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.util.OperatorNameConventions

private data class VisitorData(
    val transformer: FirBodyResolveTransformer,
    val argumentMapping: Map<Int, FirExpression>
)

private class ConeEffectToFirEffectDeclarationVisitor(
    private val effectExtractor: ConeEffectExtractor
) : ConeContractDescriptionVisitor<FirExpression?, VisitorData>() {
    override fun visitConditionalEffectDeclaration(
        conditionalEffect: ConeConditionalEffectDeclaration,
        data: VisitorData
    ): FirExpression? {
        val effectDeclaration = conditionalEffect.effect.accept(this, data) as? FirEffectDeclaration
        val condition = conditionalEffect.condition
            .accept(this, data)
            ?.accept(effectExtractor, null) as? ConeBooleanExpression
        return if (effectDeclaration != null && condition != null) {
            buildEffectDeclaration {
                this.effect = ConeConditionalEffectDeclaration(effectDeclaration.effect, condition)
            }
        } else {
            null
        }
    }

    override fun visitReturnsEffectDeclaration(
        returnsEffect: ConeReturnsEffectDeclaration,
        data: VisitorData
    ): FirExpression? {
        return returnsEffect.toFirEffectDeclaration()
    }

    override fun visitCallsEffectDeclaration(callsEffect: ConeCallsEffectDeclaration, data: VisitorData): FirExpression? {
        val parameter = callsEffect.valueParameterReference
            .accept(this, data)
            ?.accept(effectExtractor, null) as? ConeValueParameterReference
        return if (parameter != null) {
            return buildEffectDeclaration {
                effect = ConeCallsEffectDeclaration(parameter, callsEffect.kind)
            }
        } else {
            null
        }
    }

    override fun visitConstantDescriptor(constantReference: ConeConstantReference, data: VisitorData): FirExpression? {
        return when (constantReference) {
            ConeBooleanConstantReference.TRUE -> buildConstExpression(null, ConstantValueKind.Boolean, true)
            ConeBooleanConstantReference.FALSE -> buildConstExpression(null, ConstantValueKind.Boolean, false)
            ConeConstantReference.NULL -> createConstNull()
            else -> null
        }
    }

    override fun visitLogicalBinaryOperationContractExpression(
        binaryLogicExpression: ConeBinaryLogicExpression,
        data: VisitorData
    ): FirExpression? {
        val leftExpression = binaryLogicExpression.left.accept(this, data) ?: return null
        val rightExpression = binaryLogicExpression.right.accept(this, data) ?: return null
        return buildBinaryLogicExpression {
            leftOperand = leftExpression
            rightOperand = rightExpression
            kind = binaryLogicExpression.kind
        }
    }

    override fun visitLogicalNot(logicalNot: ConeLogicalNot, data: VisitorData): FirExpression? {
        val explicitReceiver = logicalNot.arg.accept(this, data) ?: return null
        return buildFunctionCall {
            calleeReference = buildSimpleNamedReference { name = OperatorNameConventions.NOT }
            this.explicitReceiver = explicitReceiver
        }
    }

    override fun visitIsInstancePredicate(isInstancePredicate: ConeIsInstancePredicate, data: VisitorData): FirExpression? {
        val argument = isInstancePredicate.arg.accept(this@ConeEffectToFirEffectDeclarationVisitor, data) ?: return null
        return buildTypeOperatorCall {
            argumentList = buildUnaryArgumentList(argument)
            operation = if (isInstancePredicate.isNegated) {
                FirOperation.NOT_IS
            } else {
                FirOperation.IS
            }
            conversionTypeRef = buildResolvedTypeRef { type = isInstancePredicate.type }
        }
    }

    override fun visitIsNullPredicate(isNullPredicate: ConeIsNullPredicate, data: VisitorData): FirExpression? {
        val argument = isNullPredicate.arg.accept(this, data) ?: return null
        return buildEqualityOperatorCall {
            operation = if (isNullPredicate.isNegated) {
                FirOperation.NOT_EQ
            } else {
                FirOperation.EQ
            }
            argumentList = buildBinaryArgumentList(argument, createConstNull())
        }
    }

    override fun visitSatisfiesPredicate(satisfiesEffect: ConeSatisfiesPredicate, data: VisitorData): FirExpression? {
        val variable = satisfiesEffect.value.accept(this, data) ?: return null

        val satisfiesCall = buildFunctionCall {
            calleeReference = buildSimpleNamedReference {
                name = Name.identifier("satisfies")
            }
            explicitReceiver = variable
            argumentList = buildArgumentList {
                val references = satisfiesEffect.predicateReferences
                if (references.size == 1) {
                    arguments += references.single()
                } else {
                    arguments += buildFunctionCall {
                        calleeReference = buildSimpleNamedReference {
                            name = Name.identifier("arrayOf")
                        }
                        argumentList = buildArgumentList {
                            arguments += references
                        }
                    }
                }
            }
        }

        val session = data.transformer.session
        val resolvedContractCall: FirFunctionCall =
            wrapEffectsInContractCall(session, listOf(satisfiesCall))
                .contractCall
                .transform(data.transformer, ResolutionMode.ContextIndependent)

        return (resolvedContractCall.argument as? FirLambdaArgumentExpression)
            ?.let { it.expression as? FirAnonymousFunctionExpression }
            ?.anonymousFunction
            ?.body
            ?.let { it.statements[0] as? FirFunctionCall }
    }

    override fun visitValueParameterReference(valueParameterReference: ConeValueParameterReference, data: VisitorData): FirExpression? {
        return data.argumentMapping[valueParameterReference.parameterIndex]
    }

    private fun createConstNull(): FirConstExpression<*> = buildConstExpression(null, ConstantValueKind.Null, null)
}

fun ConeEffectDeclaration.buildContractEffectFir(
    effectExtractor: ConeEffectExtractor,
    transformer: FirBodyResolveTransformer,
    argumentMapping: Map<Int, FirExpression>
): FirExpression? {
    return this.accept(ConeEffectToFirEffectDeclarationVisitor(effectExtractor), VisitorData(transformer, argumentMapping))
}
