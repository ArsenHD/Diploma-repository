/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.frontend.api.symbols

import org.jetbrains.kotlin.idea.frontend.api.symbols.markers.*
import org.jetbrains.kotlin.idea.frontend.api.types.KtType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaGetter

public object DebugSymbolRenderer {
    private fun StringBuilder.appendLine(indent: Int, line: String) {
        appendLine(line.prependIndent(" ".repeat(indent)))
    }

    private fun StringBuilder.append(indent: Int, value: String) {
        append(value.prependIndent(" ".repeat(indent)))
    }

    public fun render(symbol: KtSymbol): String = buildString {
        val klass = symbol::class
        appendLine("${klass.simpleName}:")
        klass.members.filterIsInstance<KProperty<*>>().sortedBy { it.name }.forEach { property ->
            if (property.name in ignoredPropertyNames) return@forEach
            val getter = property.javaGetter ?: return@forEach
            val value = try {
                getter.invoke(symbol)
            } catch (e: InvocationTargetException) {
                "Could not render due to ${e.cause}"
            }
            val stringValue = renderValue(value)
            appendLine(INDENT, "${property.name}: $stringValue")
        }
    }

    private fun renderValue(value: Any?): String = when (value) {
        null -> "null"
        is String -> value
        is Boolean -> value.toString()
        is Long -> value.toString()
        is Name -> value.asString()
        is FqName -> value.asString()
        is ClassId -> value.asString()
        is CallableId -> value.toString()
        is Enum<*> -> value.name
        is List<*> -> if (value.isEmpty()) "[]" else buildString {
            appendLine("[")
            value.forEach {
                appendLine(INDENT, renderValue(it))
            }
            append("]")
        }
        is KtType -> value.asStringForDebugging()
        is KtSymbol -> {
            val symbolTag = when (value) {
                is KtClassLikeSymbol -> renderValue(value.classIdIfNonLocal ?: "<local>/${value.name}")
                is KtFunctionSymbol -> renderValue(value.callableIdIfNonLocal ?: "<local>/${value.name}")
                is KtConstructorSymbol -> "<constructor>"
                is KtNamedSymbol -> renderValue(value.name)
                is KtPropertyGetterSymbol -> "<getter>"
                is KtPropertySetterSymbol -> "<setter>"
                else -> TODO(value::class.toString())
            }
            "${value::class.simpleName!!}($symbolTag)"
        }
        is KtSimpleConstantValue<*> -> renderValue(value.value)
        is KtNamedConstantValue -> "${renderValue(value.name)} = ${renderValue(value.expression)}"
        is KtAnnotationCall -> buildString {
            append(renderValue(value.classId))
            appendLine(value.arguments.joinToString(prefix = "(", postfix = ")") { renderValue(it) })
            // TODO: perhaps we want to render `psi` for all other cases as well.
            append(INDENT, "psi: ${renderValue(value.psi)}")
        }
        is KtTypeAndAnnotations -> "${renderValue(value.annotations)} ${renderValue(value.type)}"
        else -> value::class.simpleName!!
    }

    private val ignoredPropertyNames = setOf("firRef", "psi", "token", "builder")

    private const val INDENT = 2
}
