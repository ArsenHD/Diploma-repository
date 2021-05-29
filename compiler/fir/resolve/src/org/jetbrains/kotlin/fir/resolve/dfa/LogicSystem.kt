/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeInferenceContext
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.fir.types.canBeNull
import org.jetbrains.kotlin.fir.types.commonSuperTypeOrNull

abstract class Flow<T : Flow<T>> {
    abstract fun getTypeStatement(variable: RealVariable): TypeStatement?
    abstract fun getRefinedTypePredicates(variable: RealVariable): ConstraintStatement?
    abstract fun getImplications(variable: DataFlowVariable): Collection<Implication>
    abstract fun getVariablesInTypeStatements(): Collection<RealVariable>
    abstract fun removeOperations(variable: DataFlowVariable): Pair<T, Collection<Implication>>
    abstract fun copy(): T

    abstract val directAliasMap: Map<RealVariable, RealVariableAndInfo>
    abstract val backwardsAliasMap: Map<RealVariable, List<RealVariable>>
    abstract val assignmentIndex: Map<RealVariable, Int>
}

fun <T : Flow<T>> Flow<T>.unwrapVariable(variable: RealVariable): RealVariable {
    return directAliasMap[variable]?.variable ?: variable
}

abstract class LogicSystem<FLOW : Flow<FLOW>>(protected val context: ConeInferenceContext) {
    // ------------------------------- Flow operations -------------------------------

    abstract fun createEmptyFlow(): FLOW
    abstract fun forkFlow(flow: FLOW): FLOW
    abstract fun joinFlow(flows: Collection<FLOW>): FLOW
    abstract fun unionFlow(flows: Collection<FLOW>): FLOW

    abstract fun addConstraintStatement(flow: FLOW, statement: ConstraintStatement): FLOW
    abstract fun addTypeStatement(flow: FLOW, statement: TypeStatement): FLOW

    abstract fun addImplication(flow: FLOW, implication: Implication): FLOW

    abstract fun removeTypeStatementsAboutVariable(flow: FLOW, variable: RealVariable): FLOW
    abstract fun removeConstraintStatementsAboutVariable(flow: FLOW, variable: RealVariable): FLOW
    abstract fun removeLogicStatementsAboutVariable(flow: FLOW, variable: DataFlowVariable): FLOW
    abstract fun removeAliasInformationAboutVariable(flow: FLOW, variable: RealVariable): FLOW

    abstract fun translateVariableFromConditionInStatements(
        flow: FLOW,
        originalVariable: DataFlowVariable,
        newVariable: DataFlowVariable,
        shouldRemoveOriginalStatements: Boolean,
        filter: (Implication) -> Boolean = { true },
        transform: (Implication) -> Implication? = { it },
    ): FLOW

    abstract fun approveStatementsInsideFlow(
        flow: FLOW,
        approvedStatement: OperationStatement,
        shouldForkFlow: Boolean,
        shouldRemoveSynthetics: Boolean,
    ): FLOW

    abstract fun addLocalVariableAlias(flow: FLOW, alias: RealVariable, underlyingVariable: RealVariableAndInfo): FLOW
    abstract fun removeLocalVariableAlias(flow: FLOW, alias: RealVariable): FLOW

    abstract fun recordNewAssignment(flow: FLOW, variable: RealVariable, index: Int): FLOW

    protected abstract fun getImplicationsWithVariable(flow: FLOW, variable: DataFlowVariable): Collection<Implication>

    protected abstract fun ConeKotlinType.isAcceptableForSmartcast(): Boolean

    // ------------------------------- Callbacks for updating implicit receiver stack -------------------------------

    abstract fun processUpdatedReceiverVariable(flow: FLOW, variable: RealVariable): FLOW
    abstract fun updateAllReceivers(flow: FLOW): FLOW

    // ------------------------------- Public TypeStatement util functions -------------------------------

    data class InfoForBooleanOperator(
        val conditionalFromLeft: Collection<Implication>,
        val conditionalFromRight: Collection<Implication>,
        val typeKnownFromRight: TypeStatements,
        val constraintKnownFromRight: ConstraintStatements,
    )

    abstract fun collectInfoForBooleanOperator(
        leftFlow: FLOW,
        leftVariable: DataFlowVariable,
        rightFlow: FLOW,
        rightVariable: DataFlowVariable,
    ): InfoForBooleanOperator

    abstract fun approveStatementsTo(
        typeDestination: MutableTypeStatements,
        constraintDestination: MutableConstraintStatements,
        flow: FLOW,
        approvedStatement: OperationStatement,
        statements: Collection<Implication>,
    ): FLOW

    /**
     * Recursively collects all TypeStatements and ConstraintStatements approved by [approvedStatement] and all predicates
     *   that has been implied by it
     *   TODO: or not recursively?
     */
    fun approveOperationStatement(flow: FLOW, approvedStatement: OperationStatement): Pair<FLOW, Collection<ProvableStatement<*>>> {
        val newFlow = flow.copy()
        val statements = getImplicationsWithVariable(newFlow, approvedStatement.variable)
        return approveOperationStatement(newFlow, approvedStatement, statements).let { it.first to it.second.values + it.third.values }
    }

    // TODO: move out common parts of this function and orForTypeStatements()
    // TODO: to some other function
    fun orForConstraintStatements(
        left: ConstraintStatements,
        right: ConstraintStatements
    ): MutableConstraintStatements {
        if (left.isNullOrEmpty() || right.isNotEmpty()) return mutableMapOf()
        val map = mutableMapOf<RealVariable, MutableConstraintStatement>()
        for (variable in left.keys.intersect(right.keys)) {
            val leftStatement = left.getValue(variable)
            val rightStatement = right.getValue(variable)
            map[variable] = or(listOf(leftStatement, rightStatement))
        }
        return map
    }

    fun orForTypeStatements(
        left: TypeStatements,
        right: TypeStatements,
    ): MutableTypeStatements {
        if (left.isNullOrEmpty() || right.isNullOrEmpty()) return mutableMapOf()
        val map = mutableMapOf<RealVariable, MutableTypeStatement>()
        for (variable in left.keys.intersect(right.keys)) {
            val leftStatement = left.getValue(variable)
            val rightStatement = right.getValue(variable)
            map[variable] = or(listOf(leftStatement, rightStatement))
        }
        return map
    }

    // ------------------------------- Util functions -------------------------------

    // TODO
    protected fun <E> Collection<Collection<E>>.intersectSets(): Set<E> {
        if (isEmpty()) return emptySet()
        val iterator = iterator()
        val result = LinkedHashSet<E>(iterator.next())
        while (iterator.hasNext()) {
            result.retainAll(iterator.next())
        }
        return result
    }

    private inline fun <T : AdditiveStatement<T>, E> manipulateStatements(
        statements: Collection<T>,
        op: (Collection<Set<E>>) -> MutableSet<E>,
        first: (T) -> Set<E>,
        second: (T) -> Set<E>,
        result: (RealVariable, MutableSet<E>, MutableSet<E>) -> T
    ): T {
        require(statements.isNotEmpty())
        statements.singleOrNull()?.let { return it }
        val variable = statements.first().variable
        require(variable is RealVariable)
        assert(statements.all { it.variable == variable })
        val positive = op.invoke(statements.map { first(it) })
        val negative = op.invoke(statements.map { second(it) })
        return result(variable, positive, negative)
    }

    private inline fun manipulateTypeStatements(
        statements: Collection<TypeStatement>,
        op: (Collection<Set<ConeKotlinType>>) -> MutableSet<ConeKotlinType>
    ): MutableTypeStatement {
        require(statements.isNotEmpty())
        statements.singleOrNull()?.let { return it as MutableTypeStatement }
        val variable = statements.first().variable
        assert(statements.all { it.variable == variable })
        val exactType = op.invoke(statements.map { it.exactType })
        val exactNotType = op.invoke(statements.map { it.exactNotType })
        return MutableTypeStatement(variable, exactType, exactNotType)
    }

    protected fun or(constraints: Collection<ConstraintStatement>): MutableConstraintStatement {
        val constraintStatement = manipulateStatements(
            constraints,
            ::orForConstraints,
            first = { it.satisfiedConstraints },
            second = { it.unsatisfiedConstraints },
            result = ::MutableConstraintStatement
        )
        return constraintStatement.toMutableConstraintStatement()
    }

    protected fun or(statements: Collection<TypeStatement>): MutableTypeStatement {
        val typeStatement = manipulateStatements(
            statements,
            ::orForTypes,
            first = { it.exactType },
            second = { it.exactNotType },
            result = ::MutableTypeStatement
        )
        return typeStatement.toMutableTypeStatement()
    }

    private fun orForConstraints(constraints: Collection<Set<FirFunctionSymbol<*>>>): MutableSet<FirFunctionSymbol<*>> {
        if (constraints.isEmpty()) return mutableSetOf()
        if (constraints.any { it.isEmpty() }) return mutableSetOf()
        val first = constraints.first()
        val commonConstraints = constraints.fold(first) { acc, element -> acc intersect element }
        return commonConstraints.toMutableSet()
    }

    private fun orForTypes(types: Collection<Set<ConeKotlinType>>): MutableSet<ConeKotlinType> {
        if (types.any { it.isEmpty() }) return mutableSetOf()
        val intersectedTypes = types.map {
            if (it.size > 1) {
                context.intersectTypes(it.toList())
            } else {
                assert(it.size == 1) { "We've already checked each set of types is not empty." }
                it.single()
            }
        }
        val result = mutableSetOf<ConeKotlinType>()
        context.commonSuperTypeOrNull(intersectedTypes)?.let {
            if (it.isAcceptableForSmartcast()) {
                result.add(it)
            } else if (!it.canBeNull) {
                result.add(context.anyType())
            }
            Unit
        }
        return result
    }

    protected fun and(constraints: Collection<ConstraintStatement>): MutableConstraintStatement {
        val constraintStatement = manipulateStatements(
            constraints,
            ::andForConstraints,
            first = { it.satisfiedConstraints },
            second = { it.unsatisfiedConstraints },
            result = ::MutableConstraintStatement
        )
        return constraintStatement.toMutableConstraintStatement()
    }

    protected fun and(statements: Collection<TypeStatement>): MutableTypeStatement {
        val typeStatement = manipulateStatements(
            statements,
            ::andForTypes,
            first = { it.exactType },
            second = { it.exactNotType },
            result = ::MutableTypeStatement
        )
        return typeStatement.toMutableTypeStatement()
    }

    private fun andForConstraints(constraints: Collection<Set<FirFunctionSymbol<*>>>): MutableSet<FirFunctionSymbol<*>> {
        return constraints.flatMapTo(mutableSetOf()) { it }
    }

    private fun andForTypes(types: Collection<Set<ConeKotlinType>>): MutableSet<ConeKotlinType> {
        return types.flatMapTo(mutableSetOf()) { it }
    }
}

fun <FLOW : Flow<FLOW>> LogicSystem<FLOW>.approveOperationStatement(
    flow: FLOW,
    approvedStatement: OperationStatement,
    statements: Collection<Implication>,
): Triple<FLOW, MutableTypeStatements, MutableConstraintStatements> {
    var newFlow = flow.copy()
    val typeStatements: MutableTypeStatements = mutableMapOf()
    val constraintStatements: MutableConstraintStatements = mutableMapOf()
    newFlow = approveStatementsTo(typeStatements, constraintStatements, newFlow, approvedStatement, statements)
    return Triple(newFlow, typeStatements, constraintStatements)
}

/*
 *  used for:
 *   1. val b = x is String
 *   2. b = x is String
 *   3. !b | b.not()   for Booleans
 */
fun <F : Flow<F>> LogicSystem<F>.replaceVariableFromConditionInStatements(
    flow: F,
    originalVariable: DataFlowVariable,
    newVariable: DataFlowVariable,
    filter: (Implication) -> Boolean = { true },
    transform: (Implication) -> Implication = { it },
): F {
    return translateVariableFromConditionInStatements(
        flow,
        originalVariable,
        newVariable,
        shouldRemoveOriginalStatements = true,
        filter,
        transform,
    )
}
