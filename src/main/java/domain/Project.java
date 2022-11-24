package domain;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Project {
    private String url;
    private String clonePath;
    private Set<JavaFile> javaFiles;

    public Project(String url, String clonePath) {
        this.url = url;
        this.clonePath = clonePath;
    }

    public Project(String clonePath){
        this.clonePath=clonePath;
        this.javaFiles= ConcurrentHashMap.newKeySet();
    }

    public String getUrl() {
        return url;
    }

    public String getClonePath() {
        return clonePath;
    }

    public Set<JavaFile> getJavaFiles() {
        return javaFiles;
    }
}
