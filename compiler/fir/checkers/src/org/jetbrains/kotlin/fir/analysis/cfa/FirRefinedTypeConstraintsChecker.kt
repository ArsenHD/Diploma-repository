/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.cfa

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.util.toRefinedType
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.diagnostics.reportOn
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.dfa.*
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.*
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol

object FirRefinedTypeConstraintsChecker : FirControlFlowChecker() {
    override fun analyze(graph: ControlFlowGraph, reporter: DiagnosticReporter, context: CheckerContext) {
        val function = graph.declaration as? FirFunction ?: return
        val graphRef = function.controlFlowGraphReference as FirControlFlowGraphReferenceImpl
        val dataFlowInfo = graphRef.dataFlowInfo ?: return

        val incorrectArguments = mutableListOf<FirExpression>()
        graph.traverse(
            TraverseDirection.Forward,
            UnsatisfiedConstraintsFinder(),
            UnsatisfiedConstraintsContext(dataFlowInfo, incorrectArguments)
        )

        for (incorrectArgument in incorrectArguments) {
            // TODO: what to do in case when there is no source?
            val source = incorrectArgument.source ?: continue
            reporter.reportOn(source, FirErrors.UNSATISFIED_REFINED_TYPE_CONSTRAINTS, context)
        }
    }
}

data class UnsatisfiedConstraintsContext(
    val dataFlowInfo: DataFlowInfo<*>,
    val incorrectArguments: MutableList<FirExpression>
)

class UnsatisfiedConstraintsFinder : ControlFlowGraphVisitor<Unit, UnsatisfiedConstraintsContext>() {
    override fun visitNode(node: CFGNode<*>, data: UnsatisfiedConstraintsContext) {}

    override fun visitVariableDeclarationNode(node: VariableDeclarationNode, data: UnsatisfiedConstraintsContext) {
        val property = node.fir
        val session = property.symbol.moduleData.session
        val initializer = property.initializer ?: return
        val refinedType = property.returnTypeRef.toRefinedType(session) ?: return
        val constraints = refinedType.constraints.toPredicates()
        checkConstraints(initializer, constraints, session, node, data)
    }

    override fun visitVariableAssignmentNode(node: VariableAssignmentNode, data: UnsatisfiedConstraintsContext) {
        val assignment = node.fir
        val variable = assignment.lValue.toResolvedCallableSymbol() ?: return
        val session = variable.moduleData.session
        val refinedType = assignment.lValueTypeRef.toRefinedType(session) ?: return
        val constraints = refinedType.constraints.toPredicates()
        val rValue = assignment.rValue
        checkConstraints(rValue, constraints, session, node, data)
    }

    // TODO
//    override fun visitFieldInitializerExitNode(node: FieldInitializerExitNode, data: UnsatisfiedConstraintsContext) {
//        super.visitFieldInitializerExitNode(node, data)
//    }

    // TODO
//    override fun visitPropertyInitializerExitNode(node: PropertyInitializerExitNode, data: UnsatisfiedConstraintsContext) {
//        val
//    }

    @OptIn(SymbolInternals::class)
    override fun visitFunctionCallNode(node: FunctionCallNode, data: UnsatisfiedConstraintsContext) {
        val functionCall = node.fir
        val argumentMapping = functionCall.resolvedArgumentMapping ?: return
        val symbol = node.fir.toResolvedCallableSymbol() as? FirFunctionSymbol<*>
        val function = symbol?.fir ?: return
        val session = function.moduleData.session
        val expectedConstraints = functionCall.arguments
            .mapNotNull {
                val param = argumentMapping[it]
                val refinedType = param?.returnTypeRef?.toRefinedType(session)
                val constraints = refinedType?.constraints?.toPredicates() ?: return@mapNotNull null
                it to constraints
            }
        for ((arg, expected) in expectedConstraints) {
            checkConstraints(arg, expected, session, node, data)
        }
    }

    private fun List<FirCallableReferenceAccess>.toPredicates(): Set<FirFunctionSymbol<*>> {
        return mapNotNull { it.toResolvedCallableSymbol() as? FirFunctionSymbol<*> }.toSet()
    }

    @OptIn(SymbolInternals::class)
    private fun checkConstraints(
        value: FirExpression,
        expectedConstraints: Set<FirFunctionSymbol<*>>,
        session: FirSession,
        node: CFGNode<*>,
        data: UnsatisfiedConstraintsContext
    ) {
        val flow = data.dataFlowInfo.flowOnNodes[node] as? PersistentFlow ?: return
        val variableStorage = data.dataFlowInfo.variableStorage
        val constraintsFromType = value.typeRef.toRefinedType(session)
            ?.constraints
            ?.toPredicates()
            ?: emptySet()
        val constraintsFromFlow = variableStorage.getVariable(value, flow)
            ?.let {
                when (it) {
                    is RealVariable -> flow.approvedConstraints[it]?.satisfiedConstraints ?: emptySet()
                    is SyntheticVariable -> emptySet()
                }
            } ?: emptySet()
        val constraints = constraintsFromType union constraintsFromFlow
        if (expectedConstraints.any { it !in constraints }) {
            data.incorrectArguments += value
        }
    }
}