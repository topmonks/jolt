/*
 * Copyright 2014 Topmonks, s r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt;

/**
 * @author Michal Nikodim (michal.nikodim@topmonks.com)
 */
public class FuncParser {
    private final String className;
    private final String methodName;
    private final String[] params;

    public FuncParser(String function) {
        String f = function.replaceAll(" ", "");
        int leftParenthesis = f.indexOf("(");
        int rightParenthesis = f.indexOf(")");
        if (leftParenthesis != -1) {
            String p = f.substring(leftParenthesis + 1, rightParenthesis);
            params = p.split("\\,");
            f = f.substring(0, leftParenthesis);
        } else {
            params = new String[0];
        }
        int lastDot = f.lastIndexOf(".");
        className = f.substring(0, lastDot);
        methodName = f.substring(lastDot + 1);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getParams() {
        return params;
    }
}
