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

import com.github.javaparser.ast.CompilationUnit;

import java.util.Objects;
import java.util.Set;


public class JavaFile {

    private final String path;
    private final String absolutePath;
    private final Set<Class> classes;
    private final CompilationUnit compilationUnit;

    public JavaFile(CompilationUnit cu, String path, String absolutePath, Set<Class> classes) {
        this.compilationUnit = cu;
        this.path = path;
        this.absolutePath = absolutePath;
        this.classes = classes;
    }
   

    public String getPath() {
        return path;
    } 

    public Set<Class> getClasses() {
        return this.classes;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public String getClassNames() {
        StringBuilder classesAsStringBuilder = new StringBuilder();
        for (Class aClass : this.getClasses()) {
            classesAsStringBuilder.append(aClass.getQualifiedName()).append(",");
        }
        String classesAsString = classesAsStringBuilder.toString();
        return classesAsString.isEmpty() ? "" : classesAsString.substring(0, classesAsString.length() -1);
    }

    public CompilationUnit getCompilationUnit() {
        return compilationUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaFile javaFile = (JavaFile) o;
        return Objects.equals(path, javaFile.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        return this.getPath();
    }
}
