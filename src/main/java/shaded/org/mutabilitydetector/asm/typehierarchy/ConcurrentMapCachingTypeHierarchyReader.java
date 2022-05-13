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
package shaded.org.mutabilitydetector.asm.typehierarchy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.Type;


public class ConcurrentMapCachingTypeHierarchyReader extends TypeHierarchyReader {
    final LaunchClassLoader loader;
    private final TypeHierarchyReader baseReader;
    private final ConcurrentMap<Type, TypeHierarchy> typeHierarchyCache;

    public ConcurrentMapCachingTypeHierarchyReader(LaunchClassLoader loader, TypeHierarchyReader baseReader, ConcurrentMap<Type, TypeHierarchy> initiallyEmptyCache) {
        super(loader);
        this.loader = loader;
        this.baseReader = baseReader;
        this.typeHierarchyCache = initiallyEmptyCache;
    }

    public ConcurrentMapCachingTypeHierarchyReader(TypeHierarchyReader baseReader, LaunchClassLoader loader) {
    	this(loader, baseReader, new ConcurrentHashMap<Type, TypeHierarchy>());
    }
    
    @Override
    public TypeHierarchy hierarchyOf(final Type t) {
        if (!typeHierarchyCache.containsKey(t)) {
            typeHierarchyCache.put(t, baseReader.hierarchyOf(t));
        }
        return typeHierarchyCache.get(t);
    }
}
