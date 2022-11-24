package domain;

import com.github.javaparser.ast.CompilationUnit;

import java.util.Objects;
import java.util.Set;

public class JavaFile {
    private String path;
    private String absolutePath;
    private Set<Class> classes;
    private CompilationUnit compilationUnit;

    public JavaFile(CompilationUnit cu, String path, String absolutePath, Set<Class> classes) {
        this.compilationUnit = cu;
        this.path = path;
        this.absolutePath = absolutePath;
        this.classes = classes;
    }  
     
    public JavaFile(String path){
        this.path=path;
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
