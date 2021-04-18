/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.KotlinPlaceHolderStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KtRefinedTypeConstraintList : KtElementImplStub<KotlinPlaceHolderStub<KtRefinedTypeConstraintList>> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: KotlinPlaceHolderStub<KtRefinedTypeConstraintList>) : super(stub, KtStubElementTypes.REFINED_TYPE_CONSTRAINT_LIST)
}

fun KtRefinedTypeConstraintList.getExpressions(): List<KtExpression> =
    getStubOrPsiChildrenAsList(KtStubElementTypes.REFINED_TYPE_CONSTRAINT)
        .map {
            it.getExpression()
        }
