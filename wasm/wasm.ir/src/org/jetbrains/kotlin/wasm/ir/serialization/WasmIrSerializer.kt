/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir.serialization

import org.jetbrains.kotlin.wasm.ir.WasmFunction
import org.jetbrains.kotlin.wasm.ir.WasmFunctionType
import org.jetbrains.kotlin.wasm.ir.WasmGlobal
import org.jetbrains.kotlin.wasm.ir.WasmTypeDeclaration

interface WasmIrSerializer {
    fun serialize(func: WasmFunction)

    fun serialize(global: WasmGlobal)

    fun serialize(funcType: WasmFunctionType)

    fun serialize(typeDecl: WasmTypeDeclaration)
}