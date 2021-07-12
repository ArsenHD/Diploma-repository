/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.cfa

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.diagnostics.reportOn
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirRefinedType
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.dfa.*
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.CFGNode
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.ControlFlowGraph
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.ControlFlowGraphVisitor
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.FunctionCallNode
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.toSymbol

object FirRefinedTypeConstraintsChecker : FirControlFlowChecker() {
    override fun analyze(graph: ControlFlowGraph, reporter: DiagnosticReporter, context: CheckerContext) {
        val function = graph.declaration as? FirFunction ?: return
        val graphRef = function.controlFlowGraphReference as FirControlFlowGraphReferenceImpl
        val dataFlowInfo = graphRef.dataFlowInfo ?: return

        val incorrectArguments = mutableListOf<IncorrectArgument>()
        graph.traverse(
            TraverseDirection.Forward,
            UnsatisfiedConstraintsFinder(),
            UnsatisfiedConstraintsContext(dataFlowInfo, incorrectArguments)
        )

        for (incorrectArgument in incorrectArguments) {
            // TODO: what to do in case when there is no source?
            val source = incorrectArgument.argument.source ?: continue
            reporter.reportOn(source, FirErrors.UNSATISFIED_REFINED_TYPE_CONSTRAINTS, context)
        }
    }
}

data class UnsatisfiedConstraintsContext(
    val dataFlowInfo: DataFlowInfo,
    val incorrectArguments: MutableList<IncorrectArgument>
)

sealed class IncorrectArgument {
    abstract val argument: FirQualifiedAccessExpression
}

class IncorrectValueParameter(
    override val argument: FirQualifiedAccessExpression
) : IncorrectArgument()

class IncorrectProperty(
    override val argument: FirQualifiedAccessExpression
) : IncorrectArgument()

class IncorrectFunctionCall(
    override val argument: FirFunctionCall
) : IncorrectArgument()

class UnsatisfiedConstraintsFinder : ControlFlowGraphVisitor<Unit, UnsatisfiedConstraintsContext>() {
    override fun visitNode(node: CFGNode<*>, data: UnsatisfiedConstraintsContext) {}

    @OptIn(SymbolInternals::class)
    override fun visitFunctionCallNode(node: FunctionCallNode, data: UnsatisfiedConstraintsContext) {
        val functionCall = node.fir
        val argumentMapping = functionCall.resolvedArgumentMapping ?: return
        val symbol = node.fir.toResolvedCallableSymbol() as? FirNamedFunctionSymbol
        val function = symbol?.fir ?: return
        val flow = data.dataFlowInfo.flowOnNodes[node] as? PersistentFlow ?: return
        val variableStorage = data.dataFlowInfo.variableStorage
        val session = function.moduleData.session
        val expectedConstraints = functionCall.arguments
            .mapNotNull {
                val param = argumentMapping[it]
                val refinedType = param?.returnTypeRef?.toRefinedType(session)
                val constraints = refinedType?.constraints?.toPredicates()?.toSet() ?: return@mapNotNull null
                it to constraints
            }
        for ((arg, expected) in expectedConstraints) {
            when (arg) {
                is FirFunctionCall -> {
                    val calledFunction = arg.toResolvedCallableSymbol()?.fir ?: continue
                    val refinedType = calledFunction.returnTypeRef.toRefinedType(session) ?: continue
                    val constraints = refinedType.constraints
                        .toPredicates()
                        .toSet()
                    if (constraints != expected) {
                        data.incorrectArguments += IncorrectFunctionCall(arg)
                    }
                }
                is FirQualifiedAccessExpression -> {
                    val variable = arg.toResolvedCallableSymbol()?.fir ?: continue
                    val type = variable.returnTypeRef.toRefinedType(session)
                    val constraintsFromType = type?.constraints
                        ?.mapNotNull { it.toResolvedCallableSymbol() as? FirFunctionSymbol<*> }
                        ?.toSet()
                        ?: emptySet()
                    val dataFlowVariable = variableStorage.getVariable(variable, flow) ?: continue // TODO: do we miss anything if we continue here?
                    val constraints = when (dataFlowVariable) {
                        is RealVariable -> {
                            val constraintsFromFlow = flow.approvedConstraints[dataFlowVariable]?.satisfiedConstraints ?: emptySet()
                            constraintsFromType union constraintsFromFlow
                        }
                        is SyntheticVariable -> constraintsFromType
                    }
                    if (constraints != expected) {
                        data.incorrectArguments += IncorrectProperty(arg)
                    }
                }
            }
        }
    }

    @OptIn(SymbolInternals::class)
    private fun FirTypeRef.toRefinedType(session: FirSession): FirRefinedType? {
        return coneType.toSymbol(session)?.fir as? FirRefinedType
    }

    private fun List<FirCallableReferenceAccess>.toPredicates(): List<FirFunctionSymbol<*>> {
        return mapNotNull { it.toResolvedCallableSymbol() as? FirFunctionSymbol<*> }
    }
}