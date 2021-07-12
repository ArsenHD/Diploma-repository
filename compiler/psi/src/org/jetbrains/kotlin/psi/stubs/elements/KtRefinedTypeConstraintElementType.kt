/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.stubs.elements

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import org.jetbrains.kotlin.psi.KtRefinedTypeConstraint
import org.jetbrains.kotlin.psi.stubs.KotlinRefinedTypeConstraintStub
import org.jetbrains.kotlin.psi.stubs.impl.KotlinRefinedTypeConstraintStubImpl

class KtRefinedTypeConstraintElementType(debugName: String, psiClass: Class<KtRefinedTypeConstraint>) :
    KtStubElementType<KotlinRefinedTypeConstraintStub, KtRefinedTypeConstraint>(debugName, psiClass, KotlinRefinedTypeConstraintStub::class.java) {
    override fun serialize(stub: KotlinRefinedTypeConstraintStub, dataStream: StubOutputStream) {
    }

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<PsiElement>?): KotlinRefinedTypeConstraintStub {
        return KotlinRefinedTypeConstraintStubImpl(parentStub, this)
    }

    override fun createStub(psi: KtRefinedTypeConstraint, parentStub: StubElement<*>?): KotlinRefinedTypeConstraintStub {
        return KotlinRefinedTypeConstraintStubImpl(parentStub, this)
    }
}