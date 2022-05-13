/*
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package shaded.org.mutabilitydetector.asm;

import net.minecraft.launchwrapper.LaunchClassLoader;
import shaded.org.mutabilitydetector.asm.typehierarchy.TypeHierarchyReader;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;



/**
 * Overrides {@link ClassWriter} to provide an implementation that prevents loading classes.
 * <br>
 * Delegates to an underlying {@link TypeHierarchyReader} to perform the
 * necessary visiting of class files to find the information required for
 * verification.
 *
 * The default implementation of {@link TypeHierarchyReader} will attempt to
 * read classes using a {@link ClassReader} which reads class files using
 * {@link ClassLoader#getSystemResourceAsStream(String)}. Unlike with native
 * classloading, there is no caching used in this verifier, which will almost
 * certainly degrade performance. To maintain performance, supply an alternative
 * {@link TypeHierarchyReader} which can using a caching strategy best suited to
 * your environment.
 *
 * @see ClassReader#ClassReader(String)
 * @see ClassWriter
 * @see Type
 * @see TypeHierarchyReader
 *
 *
 */
public class NonClassloadingClassWriter extends ClassWriter {

    /**
     * Used to obtain hierarchy information used in verification.
     */
    protected final TypeHierarchyReader typeHierarchyReader;
    private final LaunchClassLoader loader;
    /**
     * Constructor which chooses a naive {@link TypeHierarchyReader}.
     */
    public NonClassloadingClassWriter(int flags, LaunchClassLoader loader) {
        super(flags);
        this.loader = loader;
        this.typeHierarchyReader = new TypeHierarchyReader(this.loader);
    }

    /**
     * Constructor which chooses a naive {@link TypeHierarchyReader}.
     */
    public NonClassloadingClassWriter(ClassReader classReader, int flags, LaunchClassLoader loader) {
        this(classReader, flags, new TypeHierarchyReader(loader), loader);
    }

    /**
     * Constructor which uses the given {@link TypeHierarchyReader} to obtain
     * hierarchy information for given {@link Type}s.
     */
    public NonClassloadingClassWriter(ClassReader classReader, int flags, TypeHierarchyReader typeHierarchyReader, LaunchClassLoader loader) {
        super(classReader, flags);
        this.typeHierarchyReader = typeHierarchyReader;
        this.loader = loader;
    }

    /**
     * An implementation consistent with {@link ClassWriter#getCommonSuperClass(String, String)}
     * that does uses {@link TypeHierarchyReader} to avoid loading classes.
     */
    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        Type c = Type.getObjectType(type1);
        Type d = Type.getObjectType(type2);

        if (typeHierarchyReader.isAssignableFrom(c, d)) {
            return type1;
        }
        if (typeHierarchyReader.isAssignableFrom(d, c)) {
            return type2;
        }
        if (typeHierarchyReader.isInterface(c) || typeHierarchyReader.isInterface(d)) {
            return "java/lang/Object";
        } else {
            do {
                c = typeHierarchyReader.getSuperClass(c);
            } while (!typeHierarchyReader.isAssignableFrom(c, d));
            return c.getInternalName();
        }
    }
}
