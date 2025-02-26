/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package shaded.org.mutabilitydetector.asm.typehierarchy;

import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Type.getType;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.BOOLEAN_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.BYTE_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.CHAR_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.DOUBLE_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.FLOAT_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.INT_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.LONG_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.SHORT_HIERARCHY;
import static shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchy.VOID_HIERARCHY;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

/**
 * Discovers basic type information without loading classes.
 * 
 * Each of the methods have semantics equivalent to those found on {@link Class}
 * 
 * 
 * This implementation performs zero caching. Either of the results of reading
 * from the underlying class files, or the results of computations. As a result
 * the implementation is likely to be significantly poorer than using equivalent
 * methods from {@link Class}. Users are expected to decorate or subclass with a
 * caching strategy which best suits your environment or application.
 * 
 * @author Graham Allan
 * 
 */
public class TypeHierarchyReader {
    final LaunchClassLoader loader;

    public TypeHierarchyReader(LaunchClassLoader loader) {
        this.loader = loader;
    }

    /**
     * Returns true if the given {@link Type} represents an interface.
     * 
     * @see Class#isInterface()
     */
    public boolean isInterface(final Type t) {
        return hierarchyOf(t).isInterface();
    }

    /**
     * <p> Returns a {@link Type} representing the superclass of the given
     * {@link Type} <code>t</code>. </p> <p> Semantically equivalent to
     * {@link Class#getSuperclass()}. That is, with {@link Class}es X and Y
     * where <code>X.getSuperclass()</code> returns <code>Y</code>, and the
     * {@link Type}s <code>tX</code> and <code>tY</code> representing
     * <code>X</code> and <code>Y</code> respectively,
     * <code>this.getSuperClass(tX)</code> returns <code>tY</code>. It matches
     * {@link Class#getSuperclass()}'s behaviour for primitive, array and void
     * types, where the given {@link Type}'s have the equivalent
     * {@link Type#getSort()} values. </p>
     * 
     * @see Class#getSuperclass()
     * @see Type#getSort()
     */
    public Type getSuperClass(final Type t) {
        return hierarchyOf(t).getSuperClass();
    }

    /**
     * <p> Determines if the {@link Type} <code>to</code> is the same class, a
     * superclass, or superinterface of the given {@link Type}
     * <code>from</code>. Semantically equivalent to
     * {@link Class#isAssignableFrom(Class)}, where <code>to</code> represents
     * <code>this</code> {@link Class} object. That is, with any given
     * {@link Class}es X and Y, and {@link Type}s tX and tY, the following
     * statements will always be true: <br> <code>X.isAssignableFrom(Y) ==
     * this.isAssignableFrom(tX, tY)</code> </p>
     * 
     * Note that, in accordance with {@link Class#isAssignableFrom(Class)}, this
     * method does not return true for {@link Type}'s representing primitives
     * that can be converted (i.e. widened or narrowed) to represent the same
     * type.
     * 
     * @return true if instances of from can be legally assigned to instances of
     *         to, else false
     */
    public boolean isAssignableFrom(Type to, Type from) {
        return hierarchyOf(to).isAssignableFrom(hierarchyOf(from), this);
    }

    /**
     * Obtains the {@link TypeHierarchy} for the given {@link Type} t.
     * <br>
     * This method represents a suitable point for caching {@link TypeHierarchy} results.
     */
    public TypeHierarchy hierarchyOf(Type t) {
        try {
            switch (t.getSort()) {
                case Type.BOOLEAN:
                    return BOOLEAN_HIERARCHY;
                case Type.BYTE:
                    return BYTE_HIERARCHY;
                case Type.CHAR:
                    return CHAR_HIERARCHY;
                case Type.SHORT:
                    return SHORT_HIERARCHY;
                case Type.INT:
                    return INT_HIERARCHY;
                case Type.LONG:
                    return LONG_HIERARCHY;
                case Type.FLOAT:
                    return FLOAT_HIERARCHY;
                case Type.DOUBLE:
                    return DOUBLE_HIERARCHY;
                case Type.VOID:
                    return VOID_HIERARCHY;
                case Type.ARRAY:
                    return TypeHierarchy.hierarchyForArrayOfType(t);
                case Type.OBJECT:
                    return obtainHierarchyOf(reader(t));
                default:
                    throw new Error("Programmer error: received a type whose getSort() wasn't matched.");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a {@link ClassReader} instance which has read the class file
     * represented by the {@link Type} t. <br> The default implementation
     * returns a {@link ClassReader} which will attempt to read the class file
     * as specified by {@link ClassReader#ClassReader(String)} This method
     * represents a suitable point for subclasses to provide their own
     * configured {@link ClassReader}. For example, a {@link ClassReader} which
     * has been constructed with an alternate {@link InputStream}.
     * 
     * @see ClassReader#ClassReader(String)
     * @see ClassReader#ClassReader(InputStream)
     */
    protected ClassReader reader(Type t) throws IOException {
        String name = t.getInternalName().replace('/','.');
        String untransformedName = loader.untransformName(name);
        String transformedName = loader.transformName(name);
        byte[] transformedClass = loader.runTransformers(untransformedName, transformedName, loader.getClassBytes(untransformedName));
        if (transformedClass == null) {
            //System.out.printf("name:%s\nuname:%s\ntname:%s\n",name,untransformedName,transformedName);
            return new ClassReader(t.getInternalName());
        }
        return new ClassReader(transformedClass);
    }

    protected TypeHierarchy obtainHierarchyOf(ClassReader reader) {
        boolean isInterface = (reader.getAccess() & ACC_INTERFACE) != 0;
        return new TypeHierarchy(Type.getObjectType(reader.getClassName()),
            reader.getSuperName() == null || isInterface
                ? null
                : Type.getObjectType(reader.getSuperName()),
            interfacesTypesFrom(reader.getInterfaces()),
            isInterface);
    }

    private List<Type> interfacesTypesFrom(String[] interfaces) {
        Type[] interfaceTypes = new Type[interfaces.length];

        for (int i = 0; i < interfaces.length; i++) {
            interfaceTypes[i] = Type.getObjectType(interfaces[i]);
        }
        return Arrays.asList(interfaceTypes);
    }

}
