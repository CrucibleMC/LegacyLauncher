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

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.Type;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IsAssignableFromCachingTypeHierarchyReader extends TypeHierarchyReader {
    final LaunchClassLoader loader;
    private final ConcurrentMap<TypeAssignability, Boolean> isAssignableFromCache;
    private final TypeHierarchyReader baseReader;

    public IsAssignableFromCachingTypeHierarchyReader(LaunchClassLoader loader, TypeHierarchyReader baseReader) {
        super(loader);
        this.loader = loader;
        this.baseReader = baseReader;
        this.isAssignableFromCache =  new ConcurrentHashMap<TypeAssignability, Boolean>();
    }
    
    @Override
    public TypeHierarchy hierarchyOf(Type t) {
        return baseReader.hierarchyOf(t);
    }
    
    @Override
    public Type getSuperClass(Type t) {
        return baseReader.getSuperClass(t);
    }
    
    @Override
    public boolean isInterface(Type t) {
        return baseReader.isInterface(t);
    }
    
    @Override
    public boolean isAssignableFrom(final Type t, final Type u) {
        TypeAssignability assignability = new TypeAssignability(t, u);
        if (!isAssignableFromCache.containsKey(assignability)) {
            isAssignableFromCache.put(assignability, baseReader.isAssignableFrom(t, u));
        }
        return isAssignableFromCache.get(assignability);
    }
    
    private static class TypeAssignability {
        private final Type toType, fromType;
        private final int hashCode;
        
        TypeAssignability(Type toType, Type fromType) {
            this.toType = toType;
            this.fromType = fromType;
            this.hashCode = calculateHashCode();
        }

        private int calculateHashCode() {
            int result = toType.hashCode();
            result = 31 * result + fromType.hashCode();
            return result;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (getClass() != obj.getClass()) {
                return false;
            }
            
            TypeAssignability other = (TypeAssignability) obj;
            return toType.equals(other.toType) && fromType.equals(other.fromType);
        }
        
    }
    
}
