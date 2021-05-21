/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa

import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.ConeClassErrorType
import org.jetbrains.kotlin.fir.types.ConeKotlinType
import org.jetbrains.kotlin.types.SmartcastStability
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

// --------------------------------------- Variables ---------------------------------------

data class Identifier(
    val symbol: FirBasedSymbol<*>,
    val dispatchReceiver: DataFlowVariable?,
    val extensionReceiver: DataFlowVariable?
) {
    override fun toString(): String {
        val callableId = (symbol as? FirCallableSymbol<*>)?.callableId
        return "[$callableId, dispatchReceiver = $dispatchReceiver, extensionReceiver = $extensionReceiver]"
    }
}

sealed class DataFlowVariable(private val variableIndexForDebug: Int) {
    final override fun toString(): String {
        return "d$variableIndexForDebug"
    }
}

enum class PropertyStability(val impliedSmartcastStability: SmartcastStability?) {
    // Immutable and no custom getter or local.
    // Smartcast is definitely safe regardless of usage.
    STABLE_VALUE(SmartcastStability.STABLE_VALUE),

    // Open or custom getter.
    // Smartcast is always unsafe regardless of usage.
    PROPERTY_WITH_GETTER(SmartcastStability.PROPERTY_WITH_GETTER),

    // Protected / public member value from another module.
    // Smartcast is always unsafe regardless of usage.
    ALIEN_PUBLIC_PROPERTY(SmartcastStability.ALIEN_PUBLIC_PROPERTY),

    // Smartcast may or may not be safe, depending on whether there are concurrent writes to this local variable.
    LOCAL_VAR(null),

    // Mutable member property of a class or object.
    // Smartcast is always unsafe regardless of usage.
    MUTABLE_PROPERTY(SmartcastStability.MUTABLE_PROPERTY),

    // Delegated property of a class or object.
    // Smartcast is always unsafe regardless of usage.
    DELEGATED_PROPERTY(SmartcastStability.DELEGATED_PROPERTY),
}

class RealVariable(
    val identifier: Identifier,
    val isThisReference: Boolean,
    val explicitReceiverVariable: DataFlowVariable?,
    variableIndexForDebug: Int,
    val stability: PropertyStability,
) : DataFlowVariable(variableIndexForDebug) {
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    private val _hashCode by lazy {
        31 * identifier.hashCode() + (explicitReceiverVariable?.hashCode() ?: 0)
    }

    override fun hashCode(): Int {
        return _hashCode
    }
}

class RealVariableAndInfo(
    val variable: RealVariable,
    val originalType: ConeKotlinType?,
    val originalConstraints: Set<FirFunctionSymbol<*>>?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RealVariableAndInfo

        if (variable != other.variable) return false
        if (originalType != other.originalType) return false
        if (originalConstraints != other.originalConstraints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = variable.hashCode()
        result = 31 * result + originalType.hashCode()
        result = 31 * result + originalConstraints.hashCode()
        return result
    }
}

class SyntheticVariable(val fir: FirElement, variableIndexForDebug: Int) : DataFlowVariable(variableIndexForDebug) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SyntheticVariable

        return fir isEqualsTo other.fir
    }

    override fun hashCode(): Int {
        // hack for enums
        return if (fir is FirResolvedQualifier) {
            31 * fir.packageFqName.hashCode() + fir.classId.hashCode()
        } else {
            fir.hashCode()
        }
    }
}

private infix fun FirElement.isEqualsTo(other: FirElement): Boolean {
    if (this !is FirResolvedQualifier || other !is FirResolvedQualifier) return this == other
    if (packageFqName != other.packageFqName) return false
    if (classId != other.classId) return false
    return true
}

// --------------------------------------- Facts ---------------------------------------

sealed interface Statement<T : Statement<T>> {
    fun invert(): T
    val variable: DataFlowVariable
}

interface AdditiveStatement<T : AdditiveStatement<T>> : Statement<T> {
    operator fun plus(other: T): T
}

interface MutableStatement<T : Statement<T>> : Statement<T> {
    operator fun plusAssign(other: T)
}

interface PersistentStatement<T : Statement<T>> : Statement<T>

sealed interface ProvableStatement<T : AdditiveStatement<T>> : AdditiveStatement<T>

interface MutableProvableStatement<T : ProvableStatement<T>> : ProvableStatement<T>, MutableStatement<T>

interface PersistentProvableStatement<T : ProvableStatement<T>> : ProvableStatement<T>, PersistentStatement<T>

/*
 * Examples:
 * d == Null
 * d != Null
 * d == True
 * d == False
 */
data class OperationStatement(override val variable: DataFlowVariable, val operation: Operation) : Statement<OperationStatement> {
    override fun invert(): OperationStatement {
        return OperationStatement(variable, operation.invert())
    }

    override fun toString(): String {
        return "$variable $operation"
    }
}

abstract class ConstraintStatement : ProvableStatement<ConstraintStatement> {
    abstract override val variable: RealVariable
    abstract val satisfiedConstraints: Set<FirFunctionSymbol<*>>
    abstract val unsatisfiedConstraints: Set<FirFunctionSymbol<*>>

    override fun toString(): String {
        val satisfiedPredicates = satisfiedConstraints.toPredicateNames()
        val unsatisfiedPredicates = satisfiedConstraints.toPredicateNames()
        return "$variable: $satisfiedPredicates, $unsatisfiedPredicates"
    }

    val isEmpty: Boolean
        get() = satisfiedConstraints.isEmpty() && unsatisfiedConstraints.isEmpty()

    val isNotEmpty: Boolean
        get() = !isEmpty

    private fun Set<FirFunctionSymbol<*>>.toPredicateNames(): String =
        map { "${it.callableId}" }.joinToString(prefix = "[", postfix = "]") { it }
}

internal fun ConstraintStatement.toMutableConstraintStatement(): MutableConstraintStatement =
    MutableConstraintStatement(
        variable,
        LinkedHashSet(satisfiedConstraints),
        LinkedHashSet(unsatisfiedConstraints)
    )

data class MutableConstraintStatement(
    override val variable: RealVariable,
    override val satisfiedConstraints: MutableSet<FirFunctionSymbol<*>> = linkedSetOf(),
    override val unsatisfiedConstraints: MutableSet<FirFunctionSymbol<*>> = linkedSetOf()
) : ConstraintStatement(), MutableProvableStatement<ConstraintStatement> {
    override fun plus(other: ConstraintStatement): ConstraintStatement {
        return MutableConstraintStatement(
            variable,
            LinkedHashSet(satisfiedConstraints).apply { addAll(other.satisfiedConstraints) },
            LinkedHashSet(unsatisfiedConstraints).apply { addAll(other.unsatisfiedConstraints) }
        )
    }

    override fun plusAssign(other: ConstraintStatement) {
        satisfiedConstraints += other.satisfiedConstraints
        unsatisfiedConstraints += other.unsatisfiedConstraints
    }

    override fun invert(): ConstraintStatement {
        return MutableConstraintStatement(
            variable,
            LinkedHashSet(unsatisfiedConstraints),
            LinkedHashSet(satisfiedConstraints)
        )
    }
}

abstract class TypeStatement : ProvableStatement<TypeStatement> {
    abstract override val variable: RealVariable
    abstract val exactType: Set<ConeKotlinType>
    abstract val exactNotType: Set<ConeKotlinType>

    abstract val isEmpty: Boolean
    val isNotEmpty: Boolean get() = !isEmpty

    override fun toString(): String {
        return "$variable: $exactType, $exactNotType"
    }
}

internal fun TypeStatement.toMutableTypeStatement(): MutableTypeStatement =
    MutableTypeStatement(
        variable,
        LinkedHashSet(exactType),
        LinkedHashSet(exactNotType)
    )

operator fun TypeStatement.plus(other: TypeStatement?): TypeStatement = other?.let { this + other } ?: this

class MutableTypeStatement(
    override val variable: RealVariable,
    override val exactType: MutableSet<ConeKotlinType> = linkedSetOf(),
    override val exactNotType: MutableSet<ConeKotlinType> = linkedSetOf()
) : TypeStatement(), MutableProvableStatement<TypeStatement> {
    override fun plus(other: TypeStatement): MutableTypeStatement = MutableTypeStatement(
        variable,
        LinkedHashSet(exactType).apply { addAll(other.exactType) },
        LinkedHashSet(exactNotType).apply { addAll(other.exactNotType) }
    )

    override val isEmpty: Boolean
        get() = exactType.isEmpty() && exactType.isEmpty()

    override fun invert(): MutableTypeStatement {
        return MutableTypeStatement(
            variable,
            LinkedHashSet(exactNotType),
            LinkedHashSet(exactType)
        )
    }

    override operator fun plusAssign(other: TypeStatement) {
        exactType += other.exactType
        exactNotType += other.exactNotType
    }

    fun copy(): MutableTypeStatement = MutableTypeStatement(variable, LinkedHashSet(exactType), LinkedHashSet(exactNotType))
}

class Implication(
    val condition: OperationStatement,
    val effect: Statement<*>
) {
    override fun toString(): String {
        return "$condition -> $effect"
    }
}

fun Implication.invertCondition(): Implication = Implication(condition.invert(), effect)

// --------------------------------------- Aliases ---------------------------------------

typealias TypeStatements = Map<RealVariable, TypeStatement>
typealias ConstraintStatements = Map<RealVariable, ConstraintStatement>
typealias ProvableStatements = Map<RealVariable, ProvableStatement<*>>
typealias MutableTypeStatements = MutableMap<RealVariable, MutableTypeStatement>
typealias MutableConstraintStatements = MutableMap<RealVariable, MutableConstraintStatement>
typealias MutableProvableStatements = MutableMap<RealVariable, MutableProvableStatement<*>>

typealias MutableOperationStatements = MutableMap<RealVariable, MutableTypeStatement>

fun MutableTypeStatements.addStatement(variable: RealVariable, statement: TypeStatement) {
    put(variable, statement.asMutableStatement()) { it.apply { this += statement } }
}

fun MutableConstraintStatements.addStatement(variable: RealVariable, statement: ConstraintStatement) {
    put(variable, statement.asMutableStatement()) { it.apply { this += statement } }
}

fun MutableTypeStatements.mergeTypeStatements(other: TypeStatements) {
    other.forEach { (variable, info) ->
        addStatement(variable, info)
    }
}

fun MutableConstraintStatements.mergeConstraintStatements(other: ConstraintStatements) {
    other.forEach { (variable, info) ->
        addStatement(variable, info)
    }
}

// --------------------------------------- DSL ---------------------------------------

infix fun DataFlowVariable.eq(constant: Boolean?): OperationStatement {
    val condition = when (constant) {
        true -> Operation.EqTrue
        false -> Operation.EqFalse
        null -> Operation.EqNull
    }
    return OperationStatement(this, condition)
}

infix fun DataFlowVariable.notEq(constant: Boolean?): OperationStatement {
    val condition = when (constant) {
        true -> Operation.EqFalse
        false -> Operation.EqTrue
        null -> Operation.NotEqNull
    }
    return OperationStatement(this, condition)
}

infix fun OperationStatement.implies(effect: Statement<*>): Implication = Implication(this, effect)

infix fun RealVariable.typeEq(type: ConeKotlinType): TypeStatement =
    if (type !is ConeClassErrorType) {
        MutableTypeStatement(this, linkedSetOf<ConeKotlinType>().apply { this += type }, HashSet())
    } else {
        MutableTypeStatement(this)
    }

infix fun RealVariable.typeNotEq(type: ConeKotlinType): TypeStatement =
    if (type !is ConeClassErrorType) {
        MutableTypeStatement(this, linkedSetOf(), LinkedHashSet<ConeKotlinType>().apply { this += type })
    } else {
        MutableTypeStatement(this)
    }

// --------------------------------------- Utils ---------------------------------------

@OptIn(ExperimentalContracts::class)
fun DataFlowVariable.isSynthetic(): Boolean {
    contract {
        returns(true) implies (this@isSynthetic is SyntheticVariable)
    }
    return this is SyntheticVariable
}

@OptIn(ExperimentalContracts::class)
fun DataFlowVariable.isReal(): Boolean {
    contract {
        returns(true) implies (this@isReal is RealVariable)
    }
    return this is RealVariable
}
