import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import callgraph.InvestigatorFacade;
import callgraph.infrastructure.entities.MethodCallSet;
import domain.Class;

import domain.JavaFile;
import domain.MethodOfLibrary;
import domain.Project;
import utils.Commands;

public class Main {
	
	public static Project project=new Project("C:\\Users\\kolid\\eclipse-workspace\\JavaTest"); 
	
    public static List<String> allMethodsCalled = new ArrayList<>();

    public static void main(String[] args) throws IOException {
    	
        //ToDo
        //Clone from url ???
    	
    	
    	Commands.getJarDependenciesForInitParsing(project.getClonePath());  
        getMethodsCalled();
        allMethodsCalled.forEach(System.out::println);
    

        //ToDo
        /*
        DONE run "mvn clean" in cmd
        DONE run "mvn dependency:copy-dependencies -Dclassifier=sources  -DexcludeTransitive" in cmd
        DONE foreach library in ./target/dependency
           ? jar xf nameofjar.jar
           ? parse
        	DONE  and get all method declarations (Done in Main2.java -> Main2.methodsOfLibrary)
        	DONE  foreach method
        	DONE check if it exists in our list of methods -> Main  .allMethodsCalled
        	DONE   	if exists
                    start call-graph from there
                    InvestigatorFacade facade = new InvestigatorFacade(dirOfProject, fileOfMethod, methodDeclaration);
                    Set<MethodCallSet> methodCallSets = facade.start();
         */
        
        
        
       // List all files of Target & jar xf nameofjar.jar?
        Path path = Paths.get("C:\\Users\\kolid\\eclipse-workspace\\JavaTest\\target\\dependency");
        try (Stream <Path> subPaths = Files.walk(path,1)) {
        	 List<String> allFiles = subPaths
        			 .map(Objects::toString)
        			 .collect(Collectors.toList());
        System.out.println(allFiles);
        	 
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        //get all method declarations
        Main2.main(args);
        List<MethodOfLibrary> methodsOfFile= new ArrayList<>();
        methodsOfFile = Main2.getMethodsOfLibrary();
       // System.out.println(methodsOfFile);
        
        //ONLY THE METHOD DECLARATIONS
        // for (int i=0;i<methodsOfFile.size();i++) {
    	  // System.out.println(methodsOfFile.get(i).getMethodDeclaration());
    	
        // check if it exists in our list of methods
         for(MethodOfLibrary j: methodsOfFile) {
        	for (String i : allMethodsCalled){
        	if( j.toString().contains(i)) {
        		//CALLGRAPH
        		InvestigatorFacade facade = new InvestigatorFacade("C:\\Users\\kolid\\eclipse-workspace\\JavaTest\\target\\dependency",
        					j.getFilePath(), j.getMethodDeclaration());
                Set<MethodCallSet> methodCallSets = facade.start();}
        	}
         }
         
    }

    private static void getMethodsCalled() {
        //Setup Parser and SymbolSolver
        ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(Paths.get(project.getClonePath()));
        List<SourceRoot> sourceRoots = projectRoot.getSourceRoots();
        try {
            createSymbolSolver(project.getClonePath());
        } catch (IllegalStateException e) {
            return;
        }
        if (createFileSet(sourceRoots) == 0) {
           System.err.println("No classes could be identified! Exiting...");
            return;
        }

        //For all files parse with visitor
        sourceRoots
                .forEach(sourceRoot -> {
                    System.out.println("Analysing Source Root: " + sourceRoot.getRoot().toString() );
                    try {
                        List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
                        parseResults
                                .stream()
                                .filter(res -> res.getResult().isPresent())
                                .filter(f -> !f.getResult().get().getStorage().get().getPath().toString().contains(".mvn\\wrapper"))
                                .forEach(res -> {
                                    analyzeCompilationUnit(res.getResult().get());
                                });
                    } catch (Exception ignored) {
                    }
                });
        System.out.println();
    }

    private static void analyzeCompilationUnit(CompilationUnit compilationUnit) {
        VoidVisitor<List<String>> methodCall = new MethodCall();
        methodCall.visit(compilationUnit,allMethodsCalled);
    }

    private static class MethodCall extends VoidVisitorAdapter<List<String>> {
        @Override
        public void visit(MethodCallExpr n, List<String> collector) {
            super.visit(n, collector);
            try {
                collector.add(n.resolve().getQualifiedSignature());
            } catch (Throwable t) {
                System.out.println("Problem with: "+n.getName() +"  ---"+n.resolve().getQualifiedSignature());
            }
        }
    }

    //Create Symbol Solver
    private static void createSymbolSolver(String projectDir) {
        TypeSolver javaParserTypeSolver = new JavaParserTypeSolver(new File(projectDir));
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(javaParserTypeSolver);
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration
                .setSymbolResolver(symbolSolver)
                .setAttributeComments(false).setDetectOriginalLineSeparator(true);
        StaticJavaParser
                .setConfiguration(parserConfiguration);
    }

    //Find all Java files and add to List
    
    private static int createFileSet(List<SourceRoot> sourceRoots) {
        try {
            sourceRoots
                    .forEach(sourceRoot -> {
                        try {
                            sourceRoot.tryToParse()
                                    .stream()
                                    .filter(res -> res.getResult().isPresent())
                                    .filter(cu -> cu.getResult().get().getStorage().isPresent())
                                    .filter(f -> !f.getResult().get().getStorage().get().getPath().toString().contains(".mvn\\wrapper"))
                                    .forEach(cu -> {
                                        try {
                                            project.getJavaFiles().add(
                                                    new JavaFile(cu.getResult().get(), cu.getResult().get().getStorage().get().getPath().toString().replace("\\", "/").replace(project.getClonePath(), "").substring(1),
                                                            cu.getResult().get().getStorage().get().getPath().toString().replace("\\", "/"),
                                                            cu.getResult().get().findAll(ClassOrInterfaceDeclaration.class)
                                                                    .stream()
                                                                    .filter(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getFullyQualifiedName().isPresent())
                                                                    .map(classOrInterfaceDeclaration -> classOrInterfaceDeclaration.getFullyQualifiedName().get())
                                                                    .map(Class::new)
                                                                    .collect(Collectors.toSet())));
                                        } catch (Throwable ignored) {
                                        }
                                    });
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }
        return project.getJavaFiles().size();
    }

}
