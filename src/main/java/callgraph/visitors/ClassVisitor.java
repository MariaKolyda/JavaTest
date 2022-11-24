/*******************************************************************************
 * Copyright (C) 2021-2022 UoM - University of Macedonia
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package callgraph.visitors;

import callgraph.infrastructure.entities.MethodCallSet;
import callgraph.infrastructure.entities.Project;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import callgraph.infrastructure.entities.*;
import callgraph.infrastructure.entities.Class;

import java.util.*;
import java.util.stream.Collectors;


public class ClassVisitor extends VoidVisitorAdapter<Void> {


    private final Set<MethodCallSet> methodCallSets;
    private final String filePath;
    private final MethodDeclaration startingMethod;

    private final Project project;

    public ClassVisitor(Project project, Set<MethodCallSet> methodCallSets, String filePath, MethodDeclaration method) {
        this.project = project;
        this.methodCallSets = methodCallSets;
        this.filePath = filePath;
        this.startingMethod = method;
    }

    
    @Override
    public void visit(ClassOrInterfaceDeclaration javaClass, Void arg) {
        if (this.project.getJavaFiles().stream().anyMatch(javaFile -> javaFile.getPath().equals(this.filePath))) {
            if (javaClass.getFullyQualifiedName().isPresent()) {
                if (Objects.isNull(this.startingMethod)) {
                    javaClass.getMethods().forEach(this::startMethodCallSetCreation);
                } else {
                    Optional<MethodDeclaration> methodDeclarationOptional = javaClass.getMethods().stream().filter(methodDeclaration -> methodDeclaration.equals(this.startingMethod)).findFirst();
                    methodDeclarationOptional.ifPresent(this::startMethodCallSetCreation);
                }
                aggregateMethodCallSets();
            }
        }
    }

    /**
     * Aggregates the method call sets (final step)
     */
    private void aggregateMethodCallSets() {
        this.methodCallSets.forEach(methodCallSet -> addMethodCallSetToCollection(methodCallSet, new HashSet<>(methodCallSet.getMethodCalls())));
    }

    /**
     * Starts the process of method call set creation
     *
     * @param methodDeclaration the starting method declaration
     */
    private void startMethodCallSetCreation(MethodDeclaration methodDeclaration) {
        try {
            ResolvedMethodDeclaration resolvedMethodDeclaration = methodDeclaration.resolve();
            Optional<Range> methodRangeOptional = methodDeclaration.getRange();
            methodRangeOptional.ifPresent(range -> createMethodCallSets(methodDeclaration, new MethodCallSet(new MethodDecl(methodDeclaration.findCompilationUnit().get().getStorage().get().getPath().toString().replace("\\", "/").replace(project.getClonePath(), "").substring(1), resolvedMethodDeclaration.getPackageName(), methodDeclaration.getName().asString(), resolvedMethodDeclaration.getQualifiedName(), new CodeRange(range.begin.line, range.end.line)))));
        } catch (UnsolvedSymbolException ignored) {
        }
    }

    /**
     * Adds a method call set to the collection of method call sets
     *
     * @param methodCallSet the (simple) name of the method we are searching
     * @param methodCalls   the method's invocations (MethodDecl objects)
     */
    private void addMethodCallSetToCollection(MethodCallSet methodCallSet, Set<MethodDecl> methodCalls) {
        for (MethodDecl methodInvoked : methodCallSet.getMethodCalls()) {
            MethodCallSet invokedMethodCallSet = findMethodByName(methodInvoked.getQualifiedName());
            if (Objects.isNull(invokedMethodCallSet))
                continue;
            if (methodCalls.containsAll(invokedMethodCallSet.getMethodCalls()))
                return;
            methodCalls.addAll(invokedMethodCallSet.getMethodCalls());
            addMethodCallSetToCollection(invokedMethodCallSet, methodCalls);
        }
    }

    /**
     * Searches for a method call set, given a method name (Simple name)
     *
     * @param methodName the (simple) name of the method we are searching
     * @return the method's call set (MethodCallSet object)
     */
    private MethodCallSet findMethodByName(String methodName) {
        for (MethodCallSet methodCallSet : this.methodCallSets)
            if (methodCallSet.getMethod().getSimpleName().equals(methodName))
                return methodCallSet;
        return null;
    }

    /**
     * Stringifies a list of MethodCallExpr objects (to qualified names)
     *
     * @param methodCallExpressions a list with MethodCallExpr objects
     * @return a list with the string representations of invoked methods
     */
    private List<String> convertMethodCallExprToString(List<MethodCallExpr> methodCallExpressions) {
        List<String> methodCallExpressionsStr = new ArrayList<>();
        for (MethodCallExpr methodCallExpression : methodCallExpressions) {
            try {
                methodCallExpressionsStr.add(methodCallExpression.resolve().getQualifiedName());
            } catch (Throwable ignored) {
            }
        }
        return methodCallExpressionsStr;
    }

    /**
     * Recursive method creating the method call sets
     *
     * @param method        the method we are creating the call set for
     * @param methodCallSet method's call set
     */
    private void createMethodCallSets(MethodDeclaration method, MethodCallSet methodCallSet) {

        List<String> methodCallExpressionsStr = convertMethodCallExprToString(method.findAll(MethodCallExpr.class));
        if (methodCallExpressionsStr.isEmpty() || new HashSet<>(methodCallSet.getMethodCalls().stream().map(MethodDecl::getQualifiedName).collect(Collectors.toList())).containsAll(methodCallExpressionsStr))
            return;

        for (MethodCallExpr methodCallExpr : method.findAll(MethodCallExpr.class)) {
            JavaFile jf;
            ResolvedMethodDeclaration resolvedMethodCallExpression;
            try {
                resolvedMethodCallExpression = methodCallExpr.resolve();
                jf = this.withinAnalysisBounds(resolvedMethodCallExpression.getPackageName() + "." + resolvedMethodCallExpression.getClassName());
            } catch (Throwable t) {
                continue;
            }
            if (Objects.nonNull(jf)) {
                CompilationUnit cu = findCompilationUnit(jf.getAbsolutePath());
                if (Objects.isNull(cu))
                    continue;
                MethodDeclaration invokedMethodDeclaration = getMethodDeclarationInCompilationUnitByName(cu, methodCallExpr.getName().asString());
                if (Objects.nonNull(invokedMethodDeclaration)) {
                    try {
                        if (invokedMethodDeclaration.equals(method.asMethodDeclaration()))
                            continue;
                        Optional<Range> invokedMethodRangeOptional = invokedMethodDeclaration.getRange();
                        invokedMethodRangeOptional.ifPresent(range -> methodCallSet.addMethodCall(new MethodDecl(invokedMethodDeclaration.findCompilationUnit().get().getStorage().get().getPath().toString().replace("\\", "/").replace(project.getClonePath(), "").substring(1), resolvedMethodCallExpression.getPackageName(), methodCallExpr.getNameAsString(), resolvedMethodCallExpression.getQualifiedName(), new CodeRange(range.begin.line, range.end.line))));
                        createMethodCallSets(invokedMethodDeclaration, methodCallSet);
                    } catch (UnsolvedSymbolException ignored) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
        this.methodCallSets.add(methodCallSet);
    }

    /**
     * Finds the method declaration in a compilation unit by (simple) name
     *
     * @param compilationUnit the compilation unit we are searching
     * @param methodSimpleName method's simple name
     * @return the MethodDeclaration object if exists, null otherwise
     */
    private MethodDeclaration getMethodDeclarationInCompilationUnitByName(CompilationUnit compilationUnit, String methodSimpleName) {
        for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : compilationUnit.findAll(ClassOrInterfaceDeclaration.class)) {
            Optional<MethodDeclaration> methodOptional;
            if (classOrInterfaceDeclaration.isInterface()) {
                return getMethodImplementation(classOrInterfaceDeclaration, methodSimpleName);
            } else {
                methodOptional = classOrInterfaceDeclaration.findAll(MethodDeclaration.class).stream().filter(methodDeclaration -> methodDeclaration.getName().asString().equals(methodSimpleName)).findFirst();
                return methodOptional.orElse(null);
            }
        }
        return null;
    }

    /**
     * Searches & returns the MethodDeclaration object of the method's
     * implementation.
     *
     * @param interfaceDeclaration the interface declaration object
     * @param methodSimpleName method's simple name
     *
     * @return the implementation of the MethodDeclaration object if exists, null otherwise
     */
    private MethodDeclaration getMethodImplementation(ClassOrInterfaceDeclaration interfaceDeclaration, String methodSimpleName) {
        try {
            String implementationPath = InterfaceImplementations.getImplementedTypesByInterface(interfaceDeclaration.getFullyQualifiedName().get());
            CompilationUnit cu = findCompilationUnit(implementationPath);
            Optional<MethodDeclaration> methodOptional = cu.findAll(MethodDeclaration.class).stream().filter(methodDeclaration -> methodDeclaration.getName().asString().equals(methodSimpleName)).findFirst();
            return methodOptional.orElse(null);
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * Finds the compilation unit that corresponds to a Java source file
     *
     * @param fileAbsolutePath the path of the file we are referring to
     * @return the compilation unit (CompilationUnit object)
     */
    private CompilationUnit findCompilationUnit(String fileAbsolutePath) {
        Optional<JavaFile> javaFileOptional = this.project.getJavaFiles().stream().filter(javaFile -> javaFile.getAbsolutePath().equals(fileAbsolutePath)).findFirst();
        return javaFileOptional.map(JavaFile::getCompilationUnit).orElse(null);
    }

    /**
     * Checks if a class is part of the analysis (user defined)
     *
     * @param name the name of the class we are referring to
     * @return the java file (JavaFile object) which contains the class
     */
    private JavaFile withinAnalysisBounds(String name) {
        Optional<JavaFile> javaFileOptional = this.project.getJavaFiles().stream().filter(javaFile -> javaFile.getClasses().contains(new Class(name))).findFirst();
        return javaFileOptional.orElse(null);
    }
}
