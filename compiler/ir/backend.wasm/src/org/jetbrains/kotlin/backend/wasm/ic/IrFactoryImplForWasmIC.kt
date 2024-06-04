/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ic

import org.jetbrains.kotlin.backend.wasm.WasmCompilerWithIC
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.backend.js.WholeWorldStageController
import org.jetbrains.kotlin.ir.backend.js.ic.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.dump
import java.io.File
import java.util.*

class WasmICContext(
    private val allowIncompleteImplementations: Boolean,
    private val skipLocalNames: Boolean = false,
    private val skipSourceLocations: Boolean = false,
) : PlatformDependentICContext {
    override fun createIrFactory(): IrFactory =
        IrFactoryImplForWasmIC(WholeWorldStageController())

    override fun createCompiler(mainModule: IrModuleFragment, configuration: CompilerConfiguration): IrCompilerICInterface =
        WasmCompilerWithIC(mainModule, configuration, allowIncompleteImplementations)

    override fun createSrcFileArtifact(srcFilePath: String, fragments: IrProgramFragments?, astArtifact: File?): SrcFileArtifactBase =
        WasmSrcFileArtifact(srcFilePath, fragments as? WasmIrProgramFragments, astArtifact, skipLocalNames, skipSourceLocations)

    override fun createModuleArtifact(
        moduleName: String,
        fileArtifacts: List<SrcFileArtifactBase>,
        artifactsDir: File?,
        forceRebuildJs: Boolean,
        externalModuleName: String?,
    ): ModuleArtifactBase =
        WasmModuleArtifact(moduleName, fileArtifacts.map { it as WasmSrcFileArtifact }, artifactsDir, forceRebuildJs, externalModuleName)
}

class IrFactoryImplForWasmIC(stageController: StageController) : IrFactory(stageController), IdSignatureRetriever {
    private val declarationToSignature = WeakHashMap<IrDeclaration, IdSignature>()

    override fun <T : IrDeclaration> T.declarationCreated(): T {
        val parentSig = stageController.currentDeclaration?.let { declarationSignature(it) } ?: return this

        stageController.createSignature(parentSig)?.let { declarationToSignature[this] = it }

        return this
    }

    override fun declarationSignature(declaration: IrDeclaration): IdSignature? {
        when (declaration) {
            is IrFunction, is IrProperty, is IrClass, is IrField -> Unit
            else -> return null
        }

        val signature = declarationToSignature[declaration]
            ?: declaration.symbol.signature
            ?: declaration.symbol.privateSignature
        if (signature != null) return signature

        val unknownDeclaration = IdSignature.ScopeLocalDeclaration(declaration.dump().hashCode(), "UNKNOWN")
        declarationToSignature[declaration] = unknownDeclaration
        return unknownDeclaration
    }
}
