/*******************************************************************************
 * Copyright (C) 2021-2022 UoM - University of Macedonia
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package callgraph.infrastructure.entities;

import java.util.Objects;


public class MethodDecl {

    private final String filePath;
    private final String packageName;
    private final String simpleName;
    private final String qualifiedName;
    private final CodeRange codeRange;

    public MethodDecl(String filePath, String packageName, String simpleName, String qualifiedName, CodeRange codeRange) {
        this.filePath = filePath;
        this.packageName = packageName;
        this.simpleName = simpleName;
        this.qualifiedName = qualifiedName;
        this.codeRange = codeRange;
    } 
    
   
    

    public String getSimpleName() {
        return simpleName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public CodeRange getCodeRange() {
        return codeRange;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodDecl that = (MethodDecl) o;
        return Objects.equals(qualifiedName, that.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }

    @Override
    public String toString() {
        return this.getQualifiedName();
    }
}
