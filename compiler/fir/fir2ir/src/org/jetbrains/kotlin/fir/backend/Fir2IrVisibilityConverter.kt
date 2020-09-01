/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend

import org.jetbrains.kotlin.fir.Visibilities
import org.jetbrains.kotlin.fir.Visibility
import org.jetbrains.kotlin.descriptors.DescriptorVisibility as OldVisibility
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities as OldVisibilities

abstract class Fir2IrVisibilityConverter {
    object Default : Fir2IrVisibilityConverter() {
        override fun convertPlatformVisibility(visibility: Visibility): OldVisibility {
            error("Unknown visibility: $this")
        }
    }

    fun convertToOldVisibility(visibility: Visibility): OldVisibility {
        return when (visibility) {
            Visibilities.Private -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE
            Visibilities.PrivateToThis -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PRIVATE_TO_THIS
            Visibilities.Protected -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PROTECTED
            Visibilities.Internal -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.INTERNAL
            Visibilities.Public -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.PUBLIC
            Visibilities.Local -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.LOCAL
            Visibilities.InvisibleFake -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.INVISIBLE_FAKE
            Visibilities.Unknown -> org.jetbrains.kotlin.descriptors.DescriptorVisibilities.UNKNOWN
            else -> convertPlatformVisibility(visibility)
        }
    }

    protected abstract fun convertPlatformVisibility(visibility: Visibility): OldVisibility
}
