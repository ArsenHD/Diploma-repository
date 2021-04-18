/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.stubs.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.jetbrains.kotlin.psi.KtRefinedTypeConstraint
import org.jetbrains.kotlin.psi.stubs.KotlinRefinedTypeConstraintStub
import org.jetbrains.kotlin.psi.stubs.elements.KtRefinedTypeConstraintElementType

class KotlinRefinedTypeConstraintStubImpl(
    parent: StubElement<out PsiElement>?,
    elementType: KtRefinedTypeConstraintElementType
) : KotlinPlaceHolderStubImpl<KtRefinedTypeConstraint>(parent, elementType), KotlinRefinedTypeConstraintStub {
}