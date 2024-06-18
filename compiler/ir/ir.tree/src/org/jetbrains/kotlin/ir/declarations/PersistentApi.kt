/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.declarations

import org.jetbrains.kotlin.ir.util.IdSignature

open class StageController(open val currentStage: Int = 0) {
    open fun <T> restrictTo(declaration: IrDeclaration, fn: () -> T): T = fn()

    open fun <T> restrictInlineBodyTo(declaration: IrDeclaration, fn: () -> T): T {
        insideInlineFunction = true
        return restrictTo(declaration, fn).also { insideInlineFunction = false }
    }

    open fun <T> withInitialIr(block: () -> T): T = block()

    // Used in JS and Wasm IC. Declarations created during lowerings need meaningful signatures.
    open fun createSignature(parentSignature: IdSignature, hash: Hash128Bits? = null): IdSignature? = null

    open val currentDeclaration: IrDeclaration? get() = null
    open var insideInlineFunction = false
}

interface IdSignatureRetriever {
    fun declarationSignature(declaration: IrDeclaration): IdSignature?
}

// TODO get rid of this and use the original one in serialization.common
data class Hash128Bits(val lowBytes: ULong, val highBytes: ULong)