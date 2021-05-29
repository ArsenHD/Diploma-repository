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

class PersistentFlow : Flow<PersistentFlow> {
    val previousFlow: PersistentFlow?
    val approvedTypeStatements: PersistentApprovedTypeStatements
    val approvedConstraints: PersistentApprovedConstraints
    val logicStatements: PersistentImplications
    val level: Int
    val approvedTypeStatementsDiff: PersistentApprovedTypeStatements
    val approvedConstraintsDiff: PersistentApprovedConstraints
    val updatedAliasDiff: PersistentSet<RealVariable>

    /*
     * val x = a
     * val y = a
     *
     * directAliasMap: { x -> a, y -> a}
     * backwardsAliasMap: { a -> [x, y] }
     */
    override val directAliasMap: PersistentMap<RealVariable, RealVariableAndInfo>
    override val backwardsAliasMap: PersistentMap<RealVariable, PersistentList<RealVariable>>

    override val assignmentIndex: PersistentMap<RealVariable, Int>

    val flowId: Long

    companion object {
        var nextId: Long = 0
            get() = field++
    }

    constructor(previousFlow: PersistentFlow, increaseLevel: Boolean = true) {
        this.previousFlow = previousFlow
        approvedTypeStatements = previousFlow.approvedTypeStatements
        approvedConstraints = previousFlow.approvedConstraints
        logicStatements = previousFlow.logicStatements
        level = if (increaseLevel) previousFlow.level + 1 else previousFlow.level

        directAliasMap = previousFlow.directAliasMap
        backwardsAliasMap = previousFlow.backwardsAliasMap
        assignmentIndex = previousFlow.assignmentIndex

        approvedTypeStatementsDiff = persistentHashMapOf()
        approvedConstraintsDiff = persistentHashMapOf()
        updatedAliasDiff = persistentSetOf()
        flowId = nextId
    }

    fun copy(
        previousFlow: PersistentFlow? = this.previousFlow,
        approvedTypeStatements: PersistentApprovedTypeStatements = this.approvedTypeStatements,
        approvedConstraints: PersistentApprovedConstraints = this.approvedConstraints,
        logicStatements: PersistentImplications = this.logicStatements,
        level: Int = this.level,
        approvedTypeStatementsDiff: PersistentApprovedTypeStatements = this.approvedTypeStatementsDiff,
        approvedConstraintsDiff: PersistentApprovedConstraints = this.approvedConstraintsDiff,
        updatedAliasDiff: PersistentSet<RealVariable> = this.updatedAliasDiff,
        directAliasMap: PersistentMap<RealVariable, RealVariableAndInfo> = this.directAliasMap,
        backwardsAliasMap: PersistentMap<RealVariable, PersistentList<RealVariable>> = this.backwardsAliasMap,
        assignmentIndex: PersistentMap<RealVariable, Int> = this.assignmentIndex
    ): PersistentFlow {
        return PersistentFlow(
            previousFlow,
            approvedTypeStatements,
            approvedConstraints,
            logicStatements,
            level,
            approvedTypeStatementsDiff,
            approvedConstraintsDiff,
            updatedAliasDiff,
            directAliasMap,
            backwardsAliasMap,
            assignmentIndex,
            this.flowId
        )
    }

    fun updateLogicStatements(new: () -> PersistentImplications): PersistentFlow {
        return this.copy(
            logicStatements = new()
        )
    }

    fun updateApprovedTypeStatements(new: () -> PersistentApprovedTypeStatements): PersistentFlow {
        return this.copy(
            approvedTypeStatements = new()
        )
    }

    fun updateApprovedTypeStatementsDiff(new: () -> PersistentApprovedTypeStatements): PersistentFlow {
        return this.copy(
            approvedTypeStatementsDiff = new()
        )
    }

    fun updateApprovedConstraints(new: () -> PersistentApprovedConstraints): PersistentFlow {
        return this.copy(
            approvedConstraints = new()
        )
    }

    fun updateDirectAliasMap(new: () -> PersistentMap<RealVariable, RealVariableAndInfo>): PersistentFlow {
        return this.copy(
            directAliasMap = new()
        )
    }

    fun updateBackwardsAliasMap(new: () -> PersistentMap<RealVariable, PersistentList<RealVariable>>): PersistentFlow {
        return this.copy(
            backwardsAliasMap = new()
        )
    }

    fun updateAssignmentIndex(new: () -> PersistentMap<RealVariable, Int>): PersistentFlow {
        return this.copy(
            assignmentIndex = new()
        )
    }

    fun addAliasToUpdatedAliasDiff(alias: RealVariable): PersistentFlow {
        return this.copy(
            updatedAliasDiff = updatedAliasDiff + alias
        )
    }

    fun removeVariableFromApprovedTypeStatements(variable: RealVariable): PersistentFlow {
        return this.copy(
            approvedTypeStatements = approvedTypeStatements - variable
        )
    }

    fun removeVariableFromApprovedTypeStatementsDiff(variable: RealVariable): PersistentFlow {
        return this.copy(
            approvedTypeStatementsDiff = approvedTypeStatementsDiff - variable
        )
    }

    fun removeVariableFromApprovedConstraints(variable: RealVariable): PersistentFlow {
        return this.copy(
            approvedConstraints = approvedConstraints - variable
        )
    }

    fun removeVariableFromApprovedConstraintsDiff(variable: RealVariable): PersistentFlow {
        return this.copy(
            approvedConstraintsDiff = approvedConstraintsDiff - variable
        )
    }

    fun removeVariableFromDirectAliasMap(variable: RealVariable): PersistentFlow {
        return this.copy(
            directAliasMap = directAliasMap - variable
        )
    }

    fun updateApprovedConstraintsDiff(new: () -> PersistentApprovedConstraints): PersistentFlow {
        return this.copy(
            approvedConstraintsDiff = new()
        )
    }

    fun removeVariableFromLogicStatements(variable: DataFlowVariable): PersistentFlow {
        return this.copy(
            logicStatements = logicStatements - variable
        )
    }

    constructor(
        previousFlow: PersistentFlow?,
        approvedTypeStatements: PersistentApprovedTypeStatements,
        approvedConstraints: PersistentApprovedConstraints,
        logicStatements: PersistentImplications,
        level: Int,
        approvedTypeStatementsDiff: PersistentApprovedTypeStatements = persistentHashMapOf(),
        approvedConstraintsDiff: PersistentApprovedConstraints = persistentHashMapOf(),
        updatedAliasDiff: PersistentSet<RealVariable> = persistentSetOf(),
        directAliasMap: PersistentMap<RealVariable, RealVariableAndInfo>,
        backwardsAliasMap: PersistentMap<RealVariable, PersistentList<RealVariable>>,
        assignmentIndex: PersistentMap<RealVariable, Int>,
        flowId: Long
    ) {
        this.previousFlow = previousFlow
        this.approvedTypeStatements = approvedTypeStatements
        this.approvedConstraints = approvedConstraints
        this.logicStatements = logicStatements
        this.level = level
        this.approvedTypeStatementsDiff = approvedTypeStatementsDiff
        this.approvedConstraintsDiff = approvedConstraintsDiff
        this.updatedAliasDiff = updatedAliasDiff
        this.directAliasMap = directAliasMap
        this.backwardsAliasMap = backwardsAliasMap
        this.assignmentIndex = assignmentIndex
        this.flowId = flowId
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

        approvedTypeStatementsDiff = persistentHashMapOf()
        approvedConstraintsDiff = persistentHashMapOf()
        updatedAliasDiff = persistentSetOf()
        flowId = nextId
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

    override fun removeOperations(variable: DataFlowVariable): Pair<PersistentFlow, Collection<Implication>> {
        val implications = getImplications(variable)
        var newFlow = this.copy()
        if (implications.isNotEmpty()) {
            newFlow = newFlow.removeVariableFromLogicStatements(variable)
        }
        return newFlow to implications
    }

    override fun copy(): PersistentFlow {
        return PersistentFlow(
            previousFlow,
            approvedTypeStatements,
            approvedConstraints,
            logicStatements,
            level,
            approvedTypeStatementsDiff,
            approvedConstraintsDiff,
            updatedAliasDiff,
            directAliasMap,
            backwardsAliasMap,
            assignmentIndex,
            this.flowId
        )
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

        var commonFlow = flows.reduce(::lowestCommonFlow)

        val variables = flows.flatMap { it.approvedTypeStatements.keys + it.approvedConstraints.keys }.toSet()
        for (variable in variables) {
            val typeInfo = mergeTypeStatements(flows.map { it.getApprovedTypeStatements(variable, commonFlow) })
            if (typeInfo != null) {
                commonFlow = removeTypeStatementsAboutVariable(commonFlow, variable)
            }
            val constraintsInfo = mergeConstraints(flows.map { it.getApprovedConstraints(variable, commonFlow) })
            if (constraintsInfo != null) {
                commonFlow = removeConstraintStatementsAboutVariable(commonFlow, variable)
            }
            if (typeInfo == null && constraintsInfo == null) continue
            val thereWereReassignments = variable.hasDifferentReassignments(flows)
            if (thereWereReassignments) {
                commonFlow = removeLogicStatementsAboutVariable(commonFlow, variable)
                commonFlow = removeAliasInformationAboutVariable(commonFlow, variable)
            }
            typeInfo?.let { commonFlow = commonFlow.addApprovedTypeStatements(it) }
            constraintsInfo?.let { commonFlow = commonFlow.addApprovedConstraintStatements(it) }
        }

        commonFlow = commonFlow.addVariableAliases(aliasedVariablesThatDontChangeAlias)
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
    ): PersistentFlow {
        var newFlow = this.copy()
        for ((alias, underlyingVariable) in aliasedVariablesThatDontChangeAlias) {
            newFlow = addLocalVariableAlias(newFlow, alias, underlyingVariable)
        }
        return newFlow
    }

    private fun PersistentFlow.addApprovedConstraintStatements(
        info: MutableConstraintStatement
    ): PersistentFlow {
        var newFlow = this.updateApprovedConstraints { approvedConstraints.addConstraintStatement(info) }
        if (previousFlow != null) {
            newFlow = newFlow.updateApprovedConstraintsDiff { approvedConstraintsDiff.addConstraintStatement(info) }
        }
        return newFlow
    }

    private fun PersistentFlow.addApprovedTypeStatements(
        info: MutableTypeStatement
    ): PersistentFlow {
        var newFlow = this.updateApprovedTypeStatements { approvedTypeStatements.addTypeStatement(info) }
        if (previousFlow != null) {
            newFlow = newFlow.updateApprovedTypeStatementsDiff { approvedTypeStatementsDiff.addTypeStatement(info) }
        }
        return newFlow
    }

    override fun addLocalVariableAlias(flow: PersistentFlow, alias: RealVariable, underlyingVariable: RealVariableAndInfo): PersistentFlow {
        var newFlow = flow.copy()
        removeLocalVariableAlias(newFlow, alias)
        newFlow = newFlow.updateDirectAliasMap { newFlow.directAliasMap.put(alias, underlyingVariable) }
        newFlow = newFlow.updateBackwardsAliasMap {
            newFlow.backwardsAliasMap.put(
                underlyingVariable.variable,
                { persistentListOf(alias) },
                { variables -> variables + alias }
            )
        }
        return newFlow
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
    override fun removeLocalVariableAlias(flow: PersistentFlow, alias: RealVariable): PersistentFlow {
        var newFlow = flow.copy()
        val backAliases = newFlow.backwardsAliasMap[alias] ?: emptyList()
        for (backAlias in backAliases) {
            newFlow.logicStatements[alias]?.let { it ->
                val newStatements = it.map { it.replaceVariable(alias, backAlias) }
                val replacedStatements =
                    newFlow.logicStatements[backAlias]?.let { existing -> existing + newStatements } ?: newStatements.toPersistentList()
                newFlow = newFlow.updateLogicStatements { newFlow.logicStatements.put(backAlias, replacedStatements) }
            }
            newFlow.approvedTypeStatements[alias]?.let { it ->
                val newStatements = it.replaceVariable(alias, backAlias)
                val replacedStatements =
                    newFlow.approvedTypeStatements[backAlias]?.let { existing -> existing + newStatements } ?: newStatements
                newFlow = newFlow.updateApprovedTypeStatements { newFlow.approvedTypeStatements.put(backAlias, replacedStatements.toPersistent()) }
            }
            newFlow.approvedTypeStatementsDiff[alias]?.let { it ->
                val newStatements = it.replaceVariable(alias, backAlias)
                val replacedStatements =
                    newFlow.approvedTypeStatementsDiff[backAlias]?.let { existing -> existing + newStatements } ?: newStatements
                newFlow = newFlow.updateApprovedTypeStatementsDiff { newFlow.approvedTypeStatementsDiff.put(backAlias, replacedStatements.toPersistent()) }
            }
        }

        val original = newFlow.directAliasMap[alias]?.variable
        if (original != null) {
            newFlow = newFlow.removeVariableFromDirectAliasMap(alias)
            val variables = newFlow.backwardsAliasMap.getValue(original)
            newFlow = newFlow.updateBackwardsAliasMap { newFlow.backwardsAliasMap.put(original, variables - alias) }
        }
        newFlow = newFlow.updateBackwardsAliasMap { newFlow.backwardsAliasMap.remove(alias) }
        for (backAlias in backAliases) {
            newFlow = newFlow.updateDirectAliasMap { newFlow.directAliasMap.remove(backAlias) }
        }
        return newFlow
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
            if (flow.flowId == parentFlow.flowId) {
                obtainStatements(flow, variable)?.let {
                    result += it
                }
            } else {
                while (flow.flowId != parentFlow.flowId) {
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

    override fun addConstraintStatement(flow: PersistentFlow, statement: ConstraintStatement): PersistentFlow {
        if (statement.isEmpty) return flow.copy()
        return addStatement(
            flow,
            statement,
            updateStatements = {
                updateApprovedConstraints { approvedConstraints.addConstraintStatement(statement) }
            },
            updateDiff = {
                updateApprovedConstraintsDiff { approvedConstraintsDiff.addConstraintStatement(statement) }
            }
        )
    }

    override fun addTypeStatement(flow: PersistentFlow, statement: TypeStatement): PersistentFlow {
        if (statement.isEmpty) return flow.copy()
        return addStatement(
            flow,
            statement,
            updateStatements = {
                updateApprovedTypeStatements { approvedTypeStatements.addTypeStatement(statement) }
            },
            updateDiff = {
                updateApprovedTypeStatementsDiff { approvedTypeStatementsDiff.addTypeStatement(statement) }
            }
        )
    }

    // Use only for TypeStatement and ConstraintStatement
    private fun addStatement(
        flow: PersistentFlow,
        statement: Statement<*>,
        updateStatements: PersistentFlow.() -> PersistentFlow,
        updateDiff: PersistentFlow.() -> PersistentFlow
    ): PersistentFlow {
        var newFlow = flow.copy()
        val variable = statement.variable as? RealVariable
        newFlow = newFlow.updateStatements()
        if (newFlow.previousFlow != null) {
            newFlow = newFlow.updateDiff()
        }
        if (variable?.isThisReference == true) {
            newFlow = processUpdatedReceiverVariable(newFlow, variable)
        }
        return newFlow
    }

    override fun addImplication(flow: PersistentFlow, implication: Implication): PersistentFlow {
        var newFlow = flow.copy()
        if ((implication.effect as? TypeStatement)?.isEmpty == true) return newFlow
        if (implication.condition == implication.effect) return newFlow
        with(newFlow) {
            val variable = implication.condition.variable
            val existingImplications = logicStatements[variable]
            newFlow = newFlow.updateLogicStatements {
                if (existingImplications == null) {
                    logicStatements.put(variable, persistentListOf(implication))
                } else {
                    logicStatements.put(variable, existingImplications + implication)
                }
            }
        }
        return newFlow
    }

    override fun removeTypeStatementsAboutVariable(flow: PersistentFlow, variable: RealVariable): PersistentFlow {
        var newFlow = flow.copy()
        newFlow = newFlow.removeVariableFromApprovedTypeStatements(variable)
        newFlow = newFlow.removeVariableFromApprovedTypeStatementsDiff(variable)
        return newFlow
    }

    override fun removeConstraintStatementsAboutVariable(flow: PersistentFlow, variable: RealVariable): PersistentFlow {
        var newFlow = flow.copy()
        newFlow = newFlow.removeVariableFromApprovedConstraints(variable)
        newFlow = newFlow.removeVariableFromApprovedConstraintsDiff(variable)
        return newFlow
    }

    override fun removeLogicStatementsAboutVariable(flow: PersistentFlow, variable: DataFlowVariable): PersistentFlow {
        var newFlow = flow.copy()
        newFlow = newFlow.removeVariableFromLogicStatements(variable)
        var newLogicStatements = newFlow.logicStatements
        for ((key, implications) in newFlow.logicStatements) {
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
        newFlow = newFlow.updateLogicStatements { newLogicStatements }
        return newFlow
    }

    override fun removeAliasInformationAboutVariable(flow: PersistentFlow, variable: RealVariable): PersistentFlow {
        var newFlow = flow.copy()
        val existedAlias = newFlow.directAliasMap[variable]?.variable
        if (existedAlias != null) {
            newFlow = newFlow.removeVariableFromDirectAliasMap(variable)
            val updatedBackwardsAliasList = newFlow.backwardsAliasMap.getValue(existedAlias).remove(variable)
            newFlow = newFlow.updateBackwardsAliasMap {
                if (updatedBackwardsAliasList.isEmpty()) {
                    newFlow.backwardsAliasMap.remove(existedAlias)
                } else {
                    newFlow.backwardsAliasMap.put(existedAlias, updatedBackwardsAliasList)
                }
            }
            newFlow = newFlow.addAliasToUpdatedAliasDiff(variable)
        }
        return newFlow
    }

    override fun translateVariableFromConditionInStatements(
        flow: PersistentFlow,
        originalVariable: DataFlowVariable,
        newVariable: DataFlowVariable,
        shouldRemoveOriginalStatements: Boolean,
        filter: (Implication) -> Boolean,
        transform: (Implication) -> Implication?
    ): PersistentFlow {
        var newFlow = flow.copy()
        val statements = newFlow.logicStatements[originalVariable]?.takeIf { it.isNotEmpty() } ?: return newFlow
        val newStatements = statements.filter(filter).mapNotNull {
            val newStatement = OperationStatement(newVariable, it.condition.operation) implies it.effect
            transform(newStatement)
        }.toPersistentList()
        if (shouldRemoveOriginalStatements) {
            newFlow = newFlow.removeVariableFromLogicStatements(originalVariable)
        }
        newFlow = newFlow.updateLogicStatements {
            newFlow.logicStatements.put(newVariable, newFlow.logicStatements[newVariable]?.let { it + newStatements } ?: newStatements)
        }
        return newFlow
    }

    override fun approveStatementsInsideFlow(
        flow: PersistentFlow,
        approvedStatement: OperationStatement,
        shouldForkFlow: Boolean,
        shouldRemoveSynthetics: Boolean
    ): PersistentFlow {
        var resultFlow = flow.copy()
        val (updatedFlow, approvedFacts) = approveOperationStatementsInternal(
            resultFlow,
            approvedStatement,
            initialStatements = null,
            shouldRemoveSynthetics
        )

        resultFlow = updatedFlow

        resultFlow = if (shouldForkFlow) forkFlow(resultFlow) else resultFlow
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
            resultFlow = addTypeStatement(resultFlow, resultTypeInfo)
            resultFlow = addConstraintStatement(resultFlow, resultConstraintInfo)
        }

        updatedReceivers.forEach {
            resultFlow = processUpdatedReceiverVariable(resultFlow, it)
        }

        return resultFlow
    }

    private fun approveOperationStatementsInternal(
        flow: PersistentFlow,
        approvedStatement: OperationStatement,
        initialStatements: Collection<Implication>?,
        shouldRemoveSynthetics: Boolean
    ): Pair<PersistentFlow, ArrayListMultimap<RealVariable, ProvableStatement<*>>> {
        val approvedFacts: ArrayListMultimap<RealVariable, ProvableStatement<*>> = ArrayListMultimap.create()
        val approvedStatements = LinkedList<OperationStatement>().apply { this += approvedStatement }
        val newFlow = approveOperationStatementsInternal(flow, approvedStatements, initialStatements, shouldRemoveSynthetics, approvedFacts)
        return newFlow to approvedFacts
    }

    private fun approveOperationStatementsInternal(
        flow: PersistentFlow,
        approvedStatements: LinkedList<OperationStatement>,
        initialStatements: Collection<Implication>?,
        shouldRemoveSynthetics: Boolean,
        approvedFacts: ArrayListMultimap<RealVariable, ProvableStatement<*>>,
    ): PersistentFlow {
        var newFlow = flow.copy()
        if (approvedStatements.isEmpty()) return newFlow
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
                ?: newFlow.logicStatements[approvedStatement.variable]?.takeIf { it.isNotEmpty() }
                ?: continue
            if (shouldRemoveSynthetics && approvedStatement.variable.isSynthetic()) {
                newFlow = newFlow.removeVariableFromLogicStatements(approvedStatement.variable)
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
        return newFlow
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
    ): PersistentFlow {
        var newFLow = flow.copy()
        val (updatedFlow, approveOperationStatements) =
            approveOperationStatementsInternal(newFLow, approvedStatement, statements, shouldRemoveSynthetics = false)
        newFLow = updatedFlow
        approveOperationStatements.asMap().forEach { (variable, infos) ->
            for (info in infos) {
                when (val mutableInfo = info.asMutableStatement()) {
                    is MutableTypeStatement -> putStatementTo(typeDestination, variable, mutableInfo)
                    is MutableConstraintStatement -> putStatementTo(constraintDestination, variable, mutableInfo)
                    else -> throw IllegalArgumentException("Unknown ProvableStatement type: ${mutableInfo::class}")
                }
            }
        }
        return newFLow
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

    override fun recordNewAssignment(flow: PersistentFlow, variable: RealVariable, index: Int): PersistentFlow {
        var newFlow = flow.copy()
        newFlow = newFlow.updateAssignmentIndex { newFlow.assignmentIndex.put(variable, index) }
        return newFlow
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
    while (left.flowId != right.flowId) {
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
    is PersistentConstraintStatement -> MutableConstraintStatement(
        variable,
        satisfiedConstraints.toMutableSet(),
        unsatisfiedConstraints.toMutableSet()
    )
    else -> throw IllegalArgumentException("Unknown ConstraintStatement type: ${this::class}")
}