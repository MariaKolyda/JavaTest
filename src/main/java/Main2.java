import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import domain.MethodOfLibrary;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main2 {
    public static String project = "C:\\Users\\kolid\\eclipse-workspace\\JavaTest\\target\\dependency\\checker-qual-3.12.0";
    
    public static List<MethodOfLibrary> methodsOfLibrary= new ArrayList<>();

    public static void main(String[] args) {
        ProjectRoot projectRoot = new SymbolSolverCollectionStrategy().collect(Paths.get(project));
        List<SourceRoot> sourceRoots = projectRoot.getSourceRoots();
        try {
            createSymbolSolver(project);
        } catch (IllegalStateException e) {
            return;
        }

        sourceRoots
                .forEach(sourceRoot -> {
                    System.out.println("Analysing Source Root: " + sourceRoot.getRoot().toString() );
                    try {
                        List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
                        parseResults
                                .stream()
                                .filter(res -> res.getResult().isPresent())
                                .filter(f -> !f.getResult().get().getStorage().get().getPath().toString().contains(".mvn\\wrapper"))
                                .forEach(res -> { analyzeUnit(res.getResult().get(), res.getResult().get().getStorage().get().getPath().toString());
                                });
                    } catch (Exception ignored) {
                    }
                });
       System.out.println();

       methodsOfLibrary.forEach(System.out::println);
       
    }

    private static void analyzeUnit(CompilationUnit compilationUnit, String filePath) {
        VoidVisitor<List<MethodOfLibrary>> methodCall = new MethodCall();
        List<MethodOfLibrary> methodsOfFile= new ArrayList<>();
        methodCall.visit(compilationUnit,methodsOfFile);
        methodsOfFile.forEach(m -> {
            m.setFilePath(filePath);
        });
        methodsOfLibrary.addAll(methodsOfFile);
    }

    private static class MethodCall extends VoidVisitorAdapter<List<MethodOfLibrary>> {
        @Override
        public void visit(MethodDeclaration n, List<MethodOfLibrary> collector) {
            super.visit(n, collector);
            collector.add(new MethodOfLibrary(n,n.resolve().getQualifiedSignature()));
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

	public static List<MethodOfLibrary> getMethodsOfLibrary() {
		return methodsOfLibrary;
	}

	public static void setMethodsOfLibrary(List<MethodOfLibrary> methodsOfLibrary) {
		Main2.methodsOfLibrary = methodsOfLibrary;
	}
}
