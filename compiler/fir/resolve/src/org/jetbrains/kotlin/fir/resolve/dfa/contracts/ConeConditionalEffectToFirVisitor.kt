/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa.contracts

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.contracts.description.*
import org.jetbrains.kotlin.fir.declarations.FirContractFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.diagnostics.ConeSimpleDiagnostic
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.*
import org.jetbrains.kotlin.fir.expressions.impl.FirNoReceiverExpression
import org.jetbrains.kotlin.fir.references.builder.buildErrorNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildResolvedNamedReference
import org.jetbrains.kotlin.fir.references.builder.buildSimpleNamedReference
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.providers.getClassDeclaredFunctionSymbols
import org.jetbrains.kotlin.fir.resolve.symbolProvider
import org.jetbrains.kotlin.fir.symbols.impl.ConeClassLikeLookupTagImpl
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.ConstantValueKind
import org.jetbrains.kotlin.util.OperatorNameConventions

private class ConeConditionalEffectToFirVisitor(
    val valueParametersMapping: Map<Int, FirExpression>,
    val substitutor: ConeSubstitutor,
    val session: FirSession
) : ConeContractDescriptionVisitor<FirExpression?, Nothing?>() {
    override fun visitConditionalEffectDeclaration(conditionalEffect: ConeConditionalEffectDeclaration, data: Nothing?): FirExpression? {
        return conditionalEffect.condition.accept(this, data)
    }

    override fun visitConstantDescriptor(constantReference: ConeConstantReference, data: Nothing?): FirExpression? {
        return when (constantReference) {
            ConeBooleanConstantReference.TRUE -> buildConstExpression(null, ConstantValueKind.Boolean, true)
            ConeBooleanConstantReference.FALSE -> buildConstExpression(null, ConstantValueKind.Boolean, false)
            ConeConstantReference.NULL -> createConstNull()
            else -> null
        }
    }

    override fun visitLogicalBinaryOperationContractExpression(
        binaryLogicExpression: ConeBinaryLogicExpression,
        data: Nothing?
    ): FirExpression? {
        val leftExpression = binaryLogicExpression.left.accept(this, data) ?: return null
        val rightExpression = binaryLogicExpression.right.accept(this, data) ?: return null
        return buildBinaryLogicExpression {
            leftOperand = leftExpression
            rightOperand = rightExpression
            kind = binaryLogicExpression.kind
        }
    }

    override fun visitLogicalNot(logicalNot: ConeLogicalNot, data: Nothing?): FirExpression? {
        val explicitReceiver = logicalNot.arg.accept(this, data) ?: return null
        return buildFunctionCall {
            calleeReference = buildSimpleNamedReference { name = OperatorNameConventions.NOT }
            this.explicitReceiver = explicitReceiver
            origin = FirFunctionCallOrigin.Operator
        }
    }

    override fun visitIsInstancePredicate(isInstancePredicate: ConeIsInstancePredicate, data: Nothing?): FirExpression? {
        val argument = isInstancePredicate.arg.accept(this@ConeConditionalEffectToFirVisitor, data) ?: return null
        return buildTypeOperatorCall {
            argumentList = buildUnaryArgumentList(argument)
            operation = if (isInstancePredicate.isNegated) {
                FirOperation.NOT_IS
            } else {
                FirOperation.IS
            }
            conversionTypeRef = buildResolvedTypeRef { type = substitutor.substituteOrSelf(isInstancePredicate.type) }
        }
    }

    override fun visitIsNullPredicate(isNullPredicate: ConeIsNullPredicate, data: Nothing?): FirExpression? {
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

    override fun visitSatisfiesPredicate(
        satisfiesEffect: ConeSatisfiesPredicate,
        data: Nothing?
    ): FirExpression? {
        val argument = satisfiesEffect.value.accept(this, data) ?: return null

        return buildFunctionCall {
            val functionName = Name.identifier("satisfies")
            val session = session
            val classId = ClassId.fromString("kotlin.contracts/ContractBuilder")
            calleeReference = session.symbolProvider.getClassDeclaredFunctionSymbols(classId, functionName)
                .firstOrNull()?.let { functionSymbol ->
                    buildResolvedNamedReference {
                        name = functionName
                        resolvedSymbol = functionSymbol
                    }
                } ?: buildErrorNamedReference {
                    diagnostic = ConeSimpleDiagnostic("Could not find function 'satisfies' in interface kotlin.contracts/ContractBuilder")
                }
            explicitReceiver = argument
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
                            arguments += buildVarargArgumentsExpression {
                                varargElementType = buildResolvedTypeRef {
                                    type = ConeClassLikeTypeImpl(
                                        lookupTag = ConeClassLikeLookupTagImpl(ClassId.fromString("kotlin/reflect/KFunction")),
                                        typeArguments = emptyArray(),
                                        isNullable = false
                                    )
                                }
                                arguments += references
                            }
                        }
                    }
                }
            }
        }
    }

    override fun visitValueParameterReference(valueParameterReference: ConeValueParameterReference, data: Nothing?): FirExpression? {
        return valueParametersMapping[valueParameterReference.parameterIndex]
    }

    private fun createConstNull(): FirConstExpression<*> = buildConstExpression(null, ConstantValueKind.Null, null)
}

fun ConeConditionalEffectDeclaration.buildContractFir(
    argumentMapping: Map<Int, FirExpression>,
    substitutor: ConeSubstitutor,
    session: FirSession
): FirExpression? {
    return condition.accept(ConeConditionalEffectToFirVisitor(argumentMapping, substitutor, session), null)
}

fun createArgumentsMapping(qualifiedAccess: FirQualifiedAccess): Map<Int, FirExpression>? {
    val argumentsMapping = mutableMapOf<Int, FirExpression>()
    qualifiedAccess.extensionReceiver.takeIf { it != FirNoReceiverExpression }?.let { argumentsMapping[-1] = it }
        ?: qualifiedAccess.dispatchReceiver.takeIf { it != FirNoReceiverExpression }?.let { argumentsMapping[-1] = it }
    when (qualifiedAccess) {
        is FirFunctionCall -> {
            val function = qualifiedAccess.toResolvedCallableSymbol()?.fir
            return when (function) {
                // order is important here, because FirContractFunction inherits from FirSimpleFunction
                is FirContractFunction -> getArgumentsToParametersMapping(function, qualifiedAccess, argumentsMapping)
                is FirSimpleFunction -> getArgumentsToParametersMapping(function, qualifiedAccess, argumentsMapping)
                else -> null
            }
        }
        is FirVariableAssignment -> {
            argumentsMapping[0] = qualifiedAccess.rValue
        }
    }
    return argumentsMapping
}

private fun getArgumentsToParametersMapping(
    function: FirFunction,
    functionCall: FirFunctionCall,
    argumentsMapping: MutableMap<Int, FirExpression>
): Map<Int, FirExpression>? {
    val parameterToIndex = function.valueParameters.mapIndexed { index, parameter -> parameter to index }.toMap()
    val callArgumentMapping = functionCall.argumentMapping ?: return null
    for (argument in functionCall.arguments) {
        argumentsMapping[parameterToIndex.getValue(callArgumentMapping.getValue(argument))] = argument.unwrapArgument()
    }
    return argumentsMapping
}

