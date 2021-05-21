/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import com.google.common.collect.ArrayListMultimap
import kotlinx.collections.immutable.*
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeInferenceContext
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.utils.addIfNotNull
import java.util.*
import kotlin.collections.LinkedHashSet

data class PersistentConstraintStatement(
    override val variable: RealVariable,
    override val satisfiedConstraints: PersistentSet<FirFunctionSymbol<*>> = persistentSetOf(),
    override val unsatisfiedConstraints: PersistentSet<FirFunctionSymbol<*>> = persistentSetOf()
) : ConstraintStatement(), PersistentProvableStatement<ConstraintStatement> {

    override fun plus(other: ConstraintStatement): PersistentConstraintStatement {
        return PersistentConstraintStatement(
            variable,
            satisfiedConstraints + other.satisfiedConstraints,
            unsatisfiedConstraints + other.unsatisfiedConstraints
        )
    }

    override fun invert(): ConstraintStatement {
        return PersistentConstraintStatement(variable, unsatisfiedConstraints, satisfiedConstraints)
    }
}

data class PersistentTypeStatement(
    override val variable: RealVariable,
    override val exactType: PersistentSet<ConeKotlinType>,
    override val exactNotType: PersistentSet<ConeKotlinType>
) : TypeStatement(), PersistentProvableStatement<TypeStatement> {
    override operator fun plus(other: TypeStatement): PersistentTypeStatement {
        return PersistentTypeStatement(
            variable,
            exactType + other.exactType,
            exactNotType + other.exactNotType
        )
    }

    override val isEmpty: Boolean
        get() = exactType.isEmpty() && exactNotType.isEmpty()

    override fun invert(): PersistentTypeStatement {
        return PersistentTypeStatement(variable, exactNotType, exactType)
    }
}

typealias PersistentApprovedTypeStatements = PersistentMap<RealVariable, PersistentTypeStatement>
typealias PersistentApprovedConstraints = PersistentMap<RealVariable, PersistentConstraintStatement>
typealias PersistentImplications = PersistentMap<DataFlowVariable, PersistentList<Implication>>

class PersistentFlow : Flow {
    val previousFlow: PersistentFlow?
    var approvedTypeStatements: PersistentApprovedTypeStatements
    var approvedConstraints: PersistentApprovedConstraints
    var logicStatements: PersistentImplications
    val level: Int
    var approvedTypeStatementsDiff: PersistentApprovedTypeStatements = persistentHashMapOf()
    var approvedConstraintsDiff: PersistentApprovedConstraints = persistentHashMapOf()
    var updatedAliasDiff: PersistentSet<RealVariable> = persistentSetOf()

    /*
     * val x = a
     * val y = a
     *
     * directAliasMap: { x -> a, y -> a}
     * backwardsAliasMap: { a -> [x, y] }
     */
    override var directAliasMap: PersistentMap<RealVariable, RealVariableAndInfo>
    override var backwardsAliasMap: PersistentMap<RealVariable, PersistentList<RealVariable>>

    override var assignmentIndex: PersistentMap<RealVariable, Int>

    constructor(previousFlow: PersistentFlow) {
        this.previousFlow = previousFlow
        approvedTypeStatements = previousFlow.approvedTypeStatements
        approvedConstraints = previousFlow.approvedConstraints
        logicStatements = previousFlow.logicStatements
        level = previousFlow.level + 1

        directAliasMap = previousFlow.directAliasMap
        backwardsAliasMap = previousFlow.backwardsAliasMap
        assignmentIndex = previousFlow.assignmentIndex
    }

    constructor() {
        previousFlow = null
        approvedTypeStatements = persistentHashMapOf()
        approvedConstraints = persistentHashMapOf()
        logicStatements = persistentHashMapOf()
        level = 1

        directAliasMap = persistentMapOf()
        backwardsAliasMap = persistentMapOf()
        assignmentIndex = persistentMapOf()
    }

    override fun getTypeStatement(variable: RealVariable): TypeStatement? {
        return approvedTypeStatements[variable]
    }

    override fun getRefinedTypePredicates(variable: RealVariable): ConstraintStatement? {
        return approvedConstraints[variable]
    }

    override fun getImplications(variable: DataFlowVariable): Collection<Implication> {
        return logicStatements[variable] ?: emptyList()
    }

    override fun getVariablesInTypeStatements(): Collection<RealVariable> {
        return approvedTypeStatements.keys
    }

    override fun removeOperations(variable: DataFlowVariable): Collection<Implication> {
        return getImplications(variable).also {
            if (it.isNotEmpty()) {
                logicStatements -= variable
            }
        }
    }
}

abstract class PersistentLogicSystem(context: ConeInferenceContext) : LogicSystem<PersistentFlow>(context) {
    override fun createEmptyFlow(): PersistentFlow {
        return PersistentFlow()
    }

    override fun forkFlow(flow: PersistentFlow): PersistentFlow {
        return PersistentFlow(flow)
    }

    override fun joinFlow(flows: Collection<PersistentFlow>): PersistentFlow {
        return foldFlow(
            flows,
            mergeTypeStatements = { statements -> this.or(statements).takeIf { it.isNotEmpty } },
            mergeConstraints = { constraints -> this.or(constraints).takeIf { it.isNotEmpty } }
        )
    }

    override fun unionFlow(flows: Collection<PersistentFlow>): PersistentFlow {
        return foldFlow(
            flows,
            mergeTypeStatements = this::and,
            mergeConstraints = this::and
        )
    }

    private inline fun foldFlow(
        flows: Collection<PersistentFlow>,
        mergeTypeStatements: (Collection<TypeStatement>) -> MutableTypeStatement?,
        mergeConstraints: (Collection<ConstraintStatement>) -> MutableConstraintStatement?
    ): PersistentFlow {
        if (flows.isEmpty()) return createEmptyFlow()
        flows.singleOrNull()?.let { return it }

        val aliasedVariablesThatDontChangeAlias = computeAliasesThatDontChange(flows)

        val commonFlow = flows.reduce(::lowestCommonFlow)

        val variables = flows.flatMap { it.approvedTypeStatements.keys + it.approvedConstraints.keys }.toSet()
        for (variable in variables) {
            val typeInfo = mergeTypeStatements(flows.map { it.getApprovedTypeStatements(variable, commonFlow) }) //?: continue
            if (typeInfo != null) {
                removeTypeStatementsAboutVariable(commonFlow, variable)
            }
            val constraintsInfo = mergeConstraints(flows.map { it.getApprovedConstraints(variable, commonFlow) }) //?: continue
            if (constraintsInfo != null) {
                removeConstraintStatementsAboutVariable(commonFlow, variable)
            }
            if (typeInfo == null && constraintsInfo == null) continue
            val thereWereReassignments = variable.hasDifferentReassignments(flows)
            if (thereWereReassignments) {
                removeLogicStatementsAboutVariable(commonFlow, variable)
                removeAliasInformationAboutVariable(commonFlow, variable)
            }
            typeInfo?.let { commonFlow.addApprovedTypeStatements(it) }
            constraintsInfo?.let { commonFlow.addApprovedConstraintStatements(it) }
        }

        commonFlow.addVariableAliases(aliasedVariablesThatDontChangeAlias)
        return commonFlow
    }

    private fun RealVariable.hasDifferentReassignments(flows: Collection<PersistentFlow>): Boolean {
        val firstIndex = flows.first().assignmentIndex[this] ?: -1
        for (flow in flows) {
            val index = flow.assignmentIndex[this] ?: -1
            if (index != firstIndex) return true
        }
        return false
    }

    private fun computeAliasesThatDontChange(
        flows: Collection<PersistentFlow>
    ): MutableMap<RealVariable, RealVariableAndInfo> {
        val flowsSize = flows.size
        val aliasedVariablesThatDontChangeAlias = mutableMapOf<RealVariable, RealVariableAndInfo>()

        flows.flatMapTo(mutableSetOf()) { it.directAliasMap.keys }.forEach { aliasedVariable ->
            val originals = flows.map { it.directAliasMap[aliasedVariable] ?: return@forEach }
            if (originals.size != flowsSize) return@forEach
            val firstOriginal = originals.first()
            if (originals.all { it == firstOriginal }) {
                aliasedVariablesThatDontChangeAlias[aliasedVariable] = firstOriginal
            }
        }

        return aliasedVariablesThatDontChangeAlias
    }

    private fun PersistentFlow.addVariableAliases(
        aliasedVariablesThatDontChangeAlias: MutableMap<RealVariable, RealVariableAndInfo>
    ) {
        for ((alias, underlyingVariable) in aliasedVariablesThatDontChangeAlias) {
            addLocalVariableAlias(this, alias, underlyingVariable)
        }
    }

    private fun PersistentFlow.addApprovedConstraintStatements(
        info: MutableConstraintStatement
    ) {
        approvedConstraints = approvedConstraints.addConstraintStatement(info)
        if (previousFlow != null) {
            approvedConstraintsDiff = approvedConstraintsDiff.addConstraintStatement(info)
        }
    }

    private fun PersistentFlow.addApprovedTypeStatements(
        info: MutableTypeStatement
    ) {
        approvedTypeStatements = approvedTypeStatements.addTypeStatement(info)
        if (previousFlow != null) {
            approvedTypeStatementsDiff = approvedTypeStatementsDiff.addTypeStatement(info)
        }
    }

    override fun addLocalVariableAlias(flow: PersistentFlow, alias: RealVariable, underlyingVariable: RealVariableAndInfo) {
        removeLocalVariableAlias(flow, alias)
        flow.directAliasMap = flow.directAliasMap.put(alias, underlyingVariable)
        flow.backwardsAliasMap = flow.backwardsAliasMap.put(
            underlyingVariable.variable,
            { persistentListOf(alias) },
            { variables -> variables + alias }
        )
    }

    /**
     * For example, consider `var b = a`. In this case, `b` is an alias of `a`. In other words, `a` is the original variable of `b`.
     *
     * For back alias, consider the following.
     * ```
     * var b = a
     * var c = b
     * ```
     * Here, if the current `alias` references `b`, `c` is a back alias of `b`. So if one calls this method with `b`, we must also
     * remove aliasing between `b` and `c`. But before removing aliasing, we need to copy any statements that apply to `b` to `c`.
     */
    override fun removeLocalVariableAlias(flow: PersistentFlow, alias: RealVariable) {
        val backAliases = flow.backwardsAliasMap[alias] ?: emptyList()
        for (backAlias in backAliases) {
            flow.logicStatements[alias]?.let { it ->
                val newStatements = it.map { it.replaceVariable(alias, backAlias) }
                val replacedStatements =
                    flow.logicStatements[backAlias]?.let { existing -> existing + newStatements } ?: newStatements.toPersistentList()
                flow.logicStatements = flow.logicStatements.put(backAlias, replacedStatements)
            }
            flow.approvedTypeStatements[alias]?.let { it ->
                val newStatements = it.replaceVariable(alias, backAlias)
                val replacedStatements =
                    flow.approvedTypeStatements[backAlias]?.let { existing -> existing + newStatements } ?: newStatements
                flow.approvedTypeStatements = flow.approvedTypeStatements.put(backAlias, replacedStatements.toPersistent())
            }
            flow.approvedTypeStatementsDiff[alias]?.let { it ->
                val newStatements = it.replaceVariable(alias, backAlias)
                val replacedStatements =
                    flow.approvedTypeStatementsDiff[backAlias]?.let { existing -> existing + newStatements } ?: newStatements
                flow.approvedTypeStatementsDiff = flow.approvedTypeStatementsDiff.put(backAlias, replacedStatements.toPersistent())
            }
        }

        val original = flow.directAliasMap[alias]?.variable
        if (original != null) {
            flow.directAliasMap = flow.directAliasMap.remove(alias)
            val variables = flow.backwardsAliasMap.getValue(original)
            flow.backwardsAliasMap = flow.backwardsAliasMap.put(original, variables - alias)
        }
        flow.backwardsAliasMap = flow.backwardsAliasMap.remove(alias)
        for (backAlias in backAliases) {
            flow.directAliasMap = flow.directAliasMap.remove(backAlias)
        }
    }

    private fun Implication.replaceVariable(from: RealVariable, to: RealVariable): Implication {
        return Implication(condition.replaceVariable(from, to), effect.replaceVariable(from, to))
    }

    private fun <T : Statement<T>> Statement<T>.replaceVariable(from: RealVariable, to: RealVariable): T {
        val statement = when (this) {
            is OperationStatement -> if (variable == from) copy(variable = to) else this
            is PersistentTypeStatement -> if (variable == from) copy(variable = to) else this
            is MutableTypeStatement -> if (variable == from) MutableTypeStatement(to, exactType, exactNotType) else this
            else -> throw IllegalArgumentException("unknown type of statement $this")
        }
        @Suppress("UNCHECKED_CAST")
        return statement as T
    }


    @OptIn(DfaInternals::class)
    private fun PersistentFlow.getApprovedTypeStatements(variable: RealVariable, parentFlow: PersistentFlow): TypeStatement {
        return getApprovedStatements(
            variable,
            parentFlow,
            initResult = { realVar -> MutableTypeStatement(realVar) },
            obtainStatements = { flow, realVar -> flow.approvedTypeStatements[realVar] },
            withOriginalValue = { result, value ->
                MutableTypeStatement(
                    result.variable,
                    LinkedHashSet(result.exactType).also { it.addIfNotNull(value.originalType) },
                    LinkedHashSet(result.exactNotType)
                )
            }
        )
    }

    @OptIn(DfaInternals::class)
    private fun PersistentFlow.getApprovedConstraints(variable: RealVariable, parentFlow: PersistentFlow): ConstraintStatement {
        return getApprovedStatements(
            variable,
            parentFlow,
            initResult = { realVar -> MutableConstraintStatement(realVar) },
            obtainStatements = { flow, realVar -> flow.approvedConstraints[realVar] },
            withOriginalValue = { result, value ->
                MutableConstraintStatement(
                    result.variable,
                    LinkedHashSet(result.satisfiedConstraints).also { constraints -> value.originalConstraints?.let { constraints += it } },
                    LinkedHashSet(result.unsatisfiedConstraints)
                )
            }
        )
    }

    @OptIn(DfaInternals::class)
    private fun <T : AdditiveStatement<T>> PersistentFlow.getApprovedStatements(
        variable: RealVariable,
        parentFlow: PersistentFlow,
        initResult: (RealVariable) -> T,
        obtainStatements: (PersistentFlow, RealVariable) -> T?,
        withOriginalValue: (T, RealVariableAndInfo) -> T
    ): T {
        var flow = this
        var result = initResult(variable)
        val variableUnderAlias = directAliasMap[variable]
        if (variableUnderAlias == null) {
            // get approved type statement even though the starting flow == parent flow
            if (flow == parentFlow) {
                obtainStatements(flow, variable)?.let {
                    result += it
                }
            } else {
                while (flow != parentFlow) {
                    obtainStatements(flow, variable)?.let {
                        result += it
                    }
                    flow = flow.previousFlow!!
                }
            }
        } else {
            result = withOriginalValue(result, variableUnderAlias)
            obtainStatements(flow, variableUnderAlias.variable)?.let {
                result += it
            }
        }
        return result
    }

    override fun addConstraintStatement(flow: PersistentFlow, statement: ConstraintStatement) {
        if (statement.isEmpty) return
        addStatement(
            flow,
            statement,
            updateStatements = { approvedConstraints = approvedConstraints.addConstraintStatement(statement) },
            updateDiff = { approvedConstraintsDiff = approvedConstraintsDiff.addConstraintStatement(statement) }
        )
    }

    override fun addTypeStatement(flow: PersistentFlow, statement: TypeStatement) {
        if (statement.isEmpty) return
        addStatement(
            flow,
            statement,
            updateStatements = { approvedTypeStatements = approvedTypeStatements.addTypeStatement(statement) },
            updateDiff = { approvedTypeStatementsDiff = approvedTypeStatementsDiff.addTypeStatement(statement) }
        )
    }

    // Use only for TypeStatement and ConstraintStatement
    private fun addStatement(
        flow: PersistentFlow,
        statement: Statement<*>,
        updateStatements: PersistentFlow.() -> Unit,
        updateDiff: PersistentFlow.() -> Unit
    ) {
        val variable = statement.variable as? RealVariable
        with (flow) {
            updateStatements()
            if (previousFlow != null) {
                updateDiff()
            }
            if (variable?.isThisReference == true) {
                processUpdatedReceiverVariable(flow, variable)
            }
        }
    }

    override fun addImplication(flow: PersistentFlow, implication: Implication) {
        if ((implication.effect as? TypeStatement)?.isEmpty == true) return
        if (implication.condition == implication.effect) return
        with(flow) {
            val variable = implication.condition.variable
            val existingImplications = logicStatements[variable]
            logicStatements = if (existingImplications == null) {
                logicStatements.put(variable, persistentListOf(implication))
            } else {
                logicStatements.put(variable, existingImplications + implication)
            }
        }
    }

    override fun removeTypeStatementsAboutVariable(flow: PersistentFlow, variable: RealVariable) {
        flow.approvedTypeStatements -= variable
        flow.approvedTypeStatementsDiff -= variable
    }

    override fun removeConstraintStatementsAboutVariable(flow: PersistentFlow, variable: RealVariable) {
        flow.approvedConstraints -= variable
        flow.approvedConstraintsDiff -= variable
    }

    override fun removeLogicStatementsAboutVariable(flow: PersistentFlow, variable: DataFlowVariable) {
        flow.logicStatements -= variable
        var newLogicStatements = flow.logicStatements
        for ((key, implications) in flow.logicStatements) {
            val implicationsToDelete = mutableListOf<Implication>()
            implications.forEach { implication ->
                if (implication.effect.variable == variable) {
                    implicationsToDelete += implication
                }
            }
            if (implicationsToDelete.isEmpty()) continue
            val newImplications = implications.removeAll(implicationsToDelete)
            newLogicStatements = if (newImplications.isNotEmpty()) {
                newLogicStatements.put(key, newImplications)
            } else {
                newLogicStatements.remove(key)
            }
        }
        flow.logicStatements = newLogicStatements
    }

    override fun removeAliasInformationAboutVariable(flow: PersistentFlow, variable: RealVariable) {
        val existedAlias = flow.directAliasMap[variable]?.variable
        if (existedAlias != null) {
            flow.directAliasMap = flow.directAliasMap.remove(variable)
            val updatedBackwardsAliasList = flow.backwardsAliasMap.getValue(existedAlias).remove(variable)
            flow.backwardsAliasMap = if (updatedBackwardsAliasList.isEmpty()) {
                flow.backwardsAliasMap.remove(existedAlias)
            } else {
                flow.backwardsAliasMap.put(existedAlias, updatedBackwardsAliasList)
            }
        }
    }

    override fun translateVariableFromConditionInStatements(
        flow: PersistentFlow,
        originalVariable: DataFlowVariable,
        newVariable: DataFlowVariable,
        shouldRemoveOriginalStatements: Boolean,
        filter: (Implication) -> Boolean,
        transform: (Implication) -> Implication?
    ) {
        with(flow) {
            val statements = logicStatements[originalVariable]?.takeIf { it.isNotEmpty() } ?: return
            val newStatements = statements.filter(filter).mapNotNull {
                val newStatement = OperationStatement(newVariable, it.condition.operation) implies it.effect
                transform(newStatement)
            }.toPersistentList()
            if (shouldRemoveOriginalStatements) {
                logicStatements -= originalVariable
            }
            logicStatements = logicStatements.put(newVariable, logicStatements[newVariable]?.let { it + newStatements } ?: newStatements)
        }
    }

    override fun approveStatementsInsideFlow(
        flow: PersistentFlow,
        approvedStatement: OperationStatement,
        shouldForkFlow: Boolean,
        shouldRemoveSynthetics: Boolean
    ): PersistentFlow {
        val approvedFacts = approveOperationStatementsInternal(
            flow,
            approvedStatement,
            initialStatements = null,
            shouldRemoveSynthetics
        )

        val resultFlow = if (shouldForkFlow) forkFlow(flow) else flow
        if (approvedFacts.isEmpty) return resultFlow

        val updatedReceivers = mutableSetOf<RealVariable>()
        approvedFacts.asMap().forEach { (variable, infos) ->
            var resultTypeInfo = PersistentTypeStatement(variable, persistentSetOf(), persistentSetOf())
            var resultConstraintInfo = PersistentConstraintStatement(variable, persistentSetOf(), persistentSetOf())
            for (info in infos) {
                when (info) {
                    is TypeStatement -> resultTypeInfo += info
                    is ConstraintStatement -> resultConstraintInfo += info
                    else -> error("Only TypeStatement and ConstraintStatement are expected, but got: ${info::class}")
                }
            }
            if (variable.isThisReference) {
                updatedReceivers += variable
            }
            addTypeStatement(resultFlow, resultTypeInfo)
            addConstraintStatement(resultFlow, resultConstraintInfo)
        }

        updatedReceivers.forEach {
            processUpdatedReceiverVariable(resultFlow, it)
        }

        return resultFlow
    }

    private fun approveOperationStatementsInternal(
        flow: PersistentFlow,
        approvedStatement: OperationStatement,
        initialStatements: Collection<Implication>?,
        shouldRemoveSynthetics: Boolean
    ): ArrayListMultimap<RealVariable, ProvableStatement<*>> {
        val approvedFacts: ArrayListMultimap<RealVariable, ProvableStatement<*>> = ArrayListMultimap.create()
        val approvedStatements = LinkedList<OperationStatement>().apply { this += approvedStatement }
        approveOperationStatementsInternal(flow, approvedStatements, initialStatements, shouldRemoveSynthetics, approvedFacts)
        return approvedFacts
    }

    private fun approveOperationStatementsInternal(
        flow: PersistentFlow,
        approvedStatements: LinkedList<OperationStatement>,
        initialStatements: Collection<Implication>?,
        shouldRemoveSynthetics: Boolean,
        approvedFacts: ArrayListMultimap<RealVariable, ProvableStatement<*>>,
    ) {
        if (approvedStatements.isEmpty()) return
        val approvedOperationStatements = mutableSetOf<OperationStatement>()
        var firstIteration = true
        while (approvedStatements.isNotEmpty()) {
            @Suppress("NAME_SHADOWING")
            val approvedStatement: OperationStatement = approvedStatements.removeFirst()
            // Defense from cycles in facts
            if (!approvedOperationStatements.add(approvedStatement)) {
                continue
            }
            val statements = initialStatements?.takeIf { firstIteration }
                ?: flow.logicStatements[approvedStatement.variable]?.takeIf { it.isNotEmpty() }
                ?: continue
            if (shouldRemoveSynthetics && approvedStatement.variable.isSynthetic()) {
                flow.logicStatements -= approvedStatement.variable
            }
            for (statement in statements) {
                if (statement.condition == approvedStatement) {
                    when (val effect = statement.effect) {
                        is OperationStatement -> approvedStatements += effect
                        is TypeStatement -> approvedFacts.put(effect.variable, effect)
                        is ConstraintStatement -> approvedFacts.put(effect.variable, effect)
                    }
                }
            }
            firstIteration = false
        }
    }

    private fun <T : MutableProvableStatement<E>, E : ProvableStatement<E>> putStatementTo(
        destination: MutableMap<RealVariable, T>,
        variable: RealVariable,
        statement: T
    ) {
        destination.put(variable, statement) {
            @Suppress("UNCHECKED_CAST")
            it += statement as E
            it
        }
    }

    override fun approveStatementsTo(
        typeDestination: MutableTypeStatements,
        constraintDestination: MutableConstraintStatements,
        flow: PersistentFlow,
        approvedStatement: OperationStatement,
        statements: Collection<Implication>
    ) {
        val approveOperationStatements =
            approveOperationStatementsInternal(flow, approvedStatement, statements, shouldRemoveSynthetics = false)
        approveOperationStatements.asMap().forEach { (variable, infos) ->
            for (info in infos) {
                when (val mutableInfo = info.asMutableStatement()) {
                    is MutableTypeStatement -> putStatementTo(typeDestination, variable, mutableInfo)
                    is MutableConstraintStatement -> putStatementTo(constraintDestination, variable, mutableInfo)
                    else -> throw IllegalArgumentException("Unknown ProvableStatement type: ${mutableInfo::class}")
                }
            }
        }
    }

    override fun collectInfoForBooleanOperator(
        leftFlow: PersistentFlow,
        leftVariable: DataFlowVariable,
        rightFlow: PersistentFlow,
        rightVariable: DataFlowVariable
    ): InfoForBooleanOperator {
        return InfoForBooleanOperator(
            leftFlow.logicStatements[leftVariable] ?: emptyList(),
            rightFlow.logicStatements[rightVariable] ?: emptyList(),
            rightFlow.approvedTypeStatementsDiff,
            rightFlow.approvedConstraintsDiff
        )
    }

    override fun getImplicationsWithVariable(flow: PersistentFlow, variable: DataFlowVariable): Collection<Implication> {
        return flow.logicStatements[variable] ?: emptyList()
    }

    override fun recordNewAssignment(flow: PersistentFlow, variable: RealVariable, index: Int) {
        flow.assignmentIndex = flow.assignmentIndex.put(variable, index)
    }

    // --------------------------------------------------------------------\
}

private fun lowestCommonFlow(left: PersistentFlow, right: PersistentFlow): PersistentFlow {
    val level = minOf(left.level, right.level)

    @Suppress("NAME_SHADOWING")
    var left = left
    while (left.level > level) {
        left = left.previousFlow!!
    }
    @Suppress("NAME_SHADOWING")
    var right = right
    while (right.level > level) {
        right = right.previousFlow!!
    }
    while (left != right) {
        left = left.previousFlow!!
        right = right.previousFlow!!
    }
    return left
}

private fun PersistentApprovedConstraints.addConstraintStatement(info: ConstraintStatement): PersistentApprovedConstraints {
    val variable = info.variable
    val existingInfo = this[variable]
    return if (existingInfo == null) {
        val persistentInfo = if (info is PersistentConstraintStatement) info else info.toPersistent()
        put(variable, persistentInfo)
    } else {
        put(variable, existingInfo + info)
    }
}

private fun PersistentApprovedTypeStatements.addTypeStatement(info: TypeStatement): PersistentApprovedTypeStatements {
    val variable = info.variable
    val existingInfo = this[variable]
    return if (existingInfo == null) {
        val persistentInfo = if (info is PersistentTypeStatement) info else info.toPersistent()
        put(variable, persistentInfo)
    } else {
        put(variable, existingInfo + info)
    }
}

private fun TypeStatement.toPersistent(): PersistentTypeStatement = PersistentTypeStatement(
    variable,
    exactType.toPersistentSet(),
    exactNotType.toPersistentSet()
)

private fun ConstraintStatement.toPersistent(): PersistentConstraintStatement = PersistentConstraintStatement(
    variable,
    satisfiedConstraints.toPersistentSet(),
    unsatisfiedConstraints.toPersistentSet()
)

fun ProvableStatement<*>.asMutableStatement(): MutableProvableStatement<*> = when (this) {
    is MutableTypeStatement -> this
    is MutableConstraintStatement -> this
    is PersistentTypeStatement -> asMutableStatement()
    is PersistentConstraintStatement -> asMutableStatement()
    else -> throw IllegalArgumentException("Unknown ProvableStatement type: ${this::class}")
}

fun TypeStatement.asMutableStatement(): MutableTypeStatement = when (this) {
    is MutableTypeStatement -> this
    is PersistentTypeStatement -> MutableTypeStatement(variable, exactType.toMutableSet(), exactNotType.toMutableSet())
    else -> throw IllegalArgumentException("Unknown TypeStatement type: ${this::class}")
}

fun ConstraintStatement.asMutableStatement(): MutableConstraintStatement = when (this) {
    is MutableConstraintStatement -> this
    is PersistentConstraintStatement -> MutableConstraintStatement(variable, satisfiedConstraints.toMutableSet(), unsatisfiedConstraints.toMutableSet())
    else -> throw IllegalArgumentException("Unknown ConstraintStatement type: ${this::class}")
}