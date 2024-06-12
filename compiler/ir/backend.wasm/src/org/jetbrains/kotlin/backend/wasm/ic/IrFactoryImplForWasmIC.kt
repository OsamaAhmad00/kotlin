/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ic

import org.jetbrains.kotlin.backend.wasm.WasmCompilerWithIC
import org.jetbrains.kotlin.backend.wasm.WasmCompilerWithICForTesting
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.backend.js.WholeWorldStageController
import org.jetbrains.kotlin.ir.backend.js.ic.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.AbstractIrFactoryImplForIC
import org.jetbrains.kotlin.ir.util.IdSignature
import java.io.File
import java.util.*

open class WasmICContext(protected val allowIncompleteImplementations: Boolean) : PlatformDependentICContext {
    override fun createIrFactory(): IrFactory =
        IrFactoryImplForWasmIC(WholeWorldStageController())

    override fun createCompiler(mainModule: IrModuleFragment, configuration: CompilerConfiguration): IrCompilerICInterface =
        WasmCompilerWithIC(mainModule, configuration, allowIncompleteImplementations)

    override fun createSrcFileArtifact(srcFilePath: String, fragments: IrProgramFragments?, astArtifact: File?): SrcFileArtifact =
        WasmSrcFileArtifact(srcFilePath, fragments as? WasmIrProgramFragments, astArtifact)

    override fun createModuleArtifact(
        moduleName: String,
        fileArtifacts: List<SrcFileArtifact>,
        artifactsDir: File?,
        forceRebuildJs: Boolean,
        externalModuleName: String?,
    ): ModuleArtifact =
        WasmModuleArtifact(moduleName, fileArtifacts.map { it as WasmSrcFileArtifact }, artifactsDir, forceRebuildJs, externalModuleName)
}

class WasmICContextForTesting(allowIncompleteImplementations: Boolean) : WasmICContext(allowIncompleteImplementations) {
    override fun createCompiler(mainModule: IrModuleFragment, configuration: CompilerConfiguration): IrCompilerICInterface =
        WasmCompilerWithICForTesting(mainModule, configuration, allowIncompleteImplementations)
}

class IrFactoryImplForWasmIC(stageController: StageController) : AbstractIrFactoryImplForIC(stageController) {
    private val declarationToSignature = WeakHashMap<IrDeclaration, IdSignature>()

    override fun <T : IrDeclaration> T.register(): T {
        val parentSig = stageController.currentDeclaration?.let { declarationSignature(it) } ?: return this

        stageController.createSignature(parentSig)?.let { declarationToSignature[this] = it }

        return this
    }

    override fun declarationSignature(declaration: IrDeclaration): IdSignature? {
        when (declaration) {
            is IrFunction, is IrProperty, is IrClass, is IrField, is IrEnumEntry, is IrValueParameter -> Unit
            else -> return null
        }
        return declarationToSignature[declaration]
            ?: declaration.symbol.signature
            ?: declaration.symbol.privateSignature
            ?: error("Can't retrieve a signature for $declaration")
    }
}
