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

import java.util.HashMap;
import java.util.Map;

public final class InterfaceImplementations {
    private final static Map<String, String> implementationMap = new HashMap<>();

    public static void addImplementations(String implementedType, String implementationAbsolutePath) {
        implementationMap.put(implementedType, implementationAbsolutePath);
    }
   

    public static String getImplementedTypesByInterface(String implementedType) {
        return implementationMap.get(implementedType);
    }
}
