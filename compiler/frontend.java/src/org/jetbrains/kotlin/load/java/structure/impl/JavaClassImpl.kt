/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.load.java.structure.impl

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiTypeParameter
import com.intellij.psi.search.SearchScope
import org.jetbrains.kotlin.asJava.KtLightClassMarker
import org.jetbrains.kotlin.asJava.isSyntheticValuesOrValueOfMethod
import org.jetbrains.kotlin.descriptors.DescriptorVisibility
import org.jetbrains.kotlin.load.java.structure.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtPsiUtil

class JavaClassImpl(psiClass: PsiClass) : JavaClassifierImpl<PsiClass>(psiClass), VirtualFileBoundJavaClass, JavaAnnotationOwnerImpl, JavaModifierListOwnerImpl {
    init {
        assert(psiClass !is PsiTypeParameter) { "PsiTypeParameter should be wrapped in JavaTypeParameter, not JavaClass: use JavaClassifier.create()" }
    }

    override val innerClassNames: Collection<Name>
        get() = psi.innerClasses.mapNotNull { it.name?.takeIf(Name::isValidIdentifier)?.let(Name::identifier) }

    override fun findInnerClass(name: Name): JavaClass? {
        return psi.findInnerClassByName(name.asString(), false)?.let(::JavaClassImpl)
    }

    override val fqName: FqName?
        get() {
            val qualifiedName = psi.qualifiedName
            return if (qualifiedName == null) null else FqName(qualifiedName)
        }

    override val name: Name
        get() = KtPsiUtil.safeName(psi.name)

    override val isInterface: Boolean
        get() = psi.isInterface

    override val isAnnotationType: Boolean
        get() = psi.isAnnotationType

    override val isEnum: Boolean
        get() = psi.isEnum

    override val outerClass: JavaClassImpl?
        get() {
            val outer = psi.containingClass
            return if (outer == null) null else JavaClassImpl(outer)
        }

    override val typeParameters: List<JavaTypeParameter>
        get() = typeParameters(psi.typeParameters)

    override val supertypes: Collection<JavaClassifierType>
        get() = classifierTypes(psi.superTypes)

    override val methods: Collection<JavaMethod>
        get() {
            assertNotLightClass()
            // We apply distinct here because PsiClass#getMethods() can return duplicate PSI methods, for example in Lombok (see KT-11778)
            // Return type seems to be null for example for the 'clone' Groovy method generated by @AutoClone (see EA-73795)
            return methods(
                psi.methods.filter { method ->
                    !method.isConstructor && method.returnType != null && !(isEnum && isSyntheticValuesOrValueOfMethod(method))
                }
            ).distinct()
        }

    override val fields: Collection<JavaField>
        get() {
            assertNotLightClass()
            return fields(psi.fields.filter {
                // ex. Android plugin generates LightFields for resources started from '.' (.DS_Store file etc)
                Name.isValidIdentifier(it.name)
            })
        }

    override val constructors: Collection<JavaConstructor>
        get() {
            assertNotLightClass()
            // See for example org.jetbrains.plugins.scala.lang.psi.light.ScFunctionWrapper,
            // which is present in getConstructors(), but its isConstructor() returns false
            return constructors(psi.constructors.filter { method -> method.isConstructor })
        }

    override fun hasDefaultConstructor() = !isInterface && constructors.isEmpty()

    override val isAbstract: Boolean
        get() = JavaElementUtil.isAbstract(this)

    override val isStatic: Boolean
        get() = JavaElementUtil.isStatic(this)

    override val isFinal: Boolean
        get() = JavaElementUtil.isFinal(this)

    override val visibility: DescriptorVisibility
        get() = JavaElementUtil.getVisibility(this)

    override val lightClassOriginKind: LightClassOriginKind?
        get() = (psi as? KtLightClassMarker)?.originKind

    override val virtualFile: VirtualFile?
        get() =  psi.containingFile?.virtualFile

    override fun isFromSourceCodeInScope(scope: SearchScope): Boolean = psi.containingFile.virtualFile in scope

    override fun getAnnotationOwnerPsi() = psi.modifierList

    private fun assertNotLightClass() {
        val psiClass = psi
        if (psiClass !is KtLightClassMarker) return

        val message = "Querying members of JavaClass created for $psiClass of type ${psiClass::class.java} defined in file ${psiClass.containingFile?.virtualFile?.canonicalPath}"
        LOGGER.error(message)
    }

    companion object {
        private val LOGGER = Logger.getInstance(JavaClassImpl::class.java)
    }
}
