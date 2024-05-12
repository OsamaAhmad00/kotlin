/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir.serialization

import org.jetbrains.kotlin.wasm.ir.WasmFunction
import org.jetbrains.kotlin.wasm.ir.WasmFunctionType
import org.jetbrains.kotlin.wasm.ir.WasmGlobal
import org.jetbrains.kotlin.wasm.ir.WasmTypeDeclaration

interface WasmIrDeserializer {
    fun deserializeFunction(): WasmFunction

    fun deserializeGlobal(): WasmGlobal

    fun deserializeFunctionType(): WasmFunctionType

    fun deserializeTypeDeclaration(): WasmTypeDeclaration
}