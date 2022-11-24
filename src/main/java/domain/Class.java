package domain;

import java.util.Objects;

public class Class {
    private final String qualifiedName;

    public Class(String name) {
        this.qualifiedName = name;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

   
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Class aClass = (Class) o;
        return Objects.equals(qualifiedName, aClass.qualifiedName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifiedName);
    }

    
    @Override
    public String toString() {
        return qualifiedName;
    }
}
