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


public final class CodeRange {

    private final Integer startLine;
    private final Integer endLine;

    public CodeRange (Integer startLine, Integer endLine) {
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public Integer getStartLine() {
        return startLine;
    }

    public Integer getEndLine() {
        return endLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeRange codeRange = (CodeRange) o;
        return Objects.equals(Math.abs(startLine - endLine), Math.abs(codeRange.startLine - codeRange.endLine));
    }

    @Override
    public int hashCode() {   
        return Objects.hash(startLine, endLine);
    }

    @Override
    public String toString() {
        return "[" + this.getStartLine() + "," + this.getEndLine() + "]";
    }
}
