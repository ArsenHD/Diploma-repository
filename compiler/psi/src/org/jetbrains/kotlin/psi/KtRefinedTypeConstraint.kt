/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.getChildOfType
import org.jetbrains.kotlin.psi.stubs.KotlinRefinedTypeConstraintStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KtRefinedTypeConstraint: KtElementImplStub<KotlinRefinedTypeConstraintStub> {
    constructor(node: ASTNode): super(node)
    constructor(stub: KotlinRefinedTypeConstraintStub): super(stub, KtStubElementTypes.CONTRACT_EFFECT)
}

fun KtRefinedTypeConstraint.getCallableReferenceExpression(): KtCallableReferenceExpression = getChildOfType()!!