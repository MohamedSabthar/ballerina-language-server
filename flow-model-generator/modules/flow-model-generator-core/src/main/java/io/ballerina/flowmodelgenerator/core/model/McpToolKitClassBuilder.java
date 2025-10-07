/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.ballerina.flowmodelgenerator.core.model;

import io.ballerina.compiler.syntax.tree.SyntaxInfo;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.ballerina.flowmodelgenerator.core.Constants.Ai;
import static io.ballerina.flowmodelgenerator.core.model.NodeKind.MCP_TOOL_KIT_CLASS;

/**
 * Represents a Generated McpToolKitClass node in the flow model.
 *
 * @since 1.3.1
 */
public class McpToolKitClassBuilder extends NodeBuilder {
    private static final String TOOL_KIT_NAME_PROPERTY = "toolKitName";
    private static final String TOOL_KIT_NAME_PROPERTY_LABEL = "MCP Toolkit Name";
    private static final String TOOL_KIT_NAME_DESCRIPTION = "Name of the MCP toolkit";
    private static final String TOOL_KIT_DEFAULT_CLASS_NAME = "McpToolKit";
    private static final String PERMITTED_TOOLS_PROPERTY = "permittedTools";
    private static final String PERMITTED_TOOLS_PROPERTY_LABEL = "List of permitted tools from the MCP server";
    private static final String PERMITTED_TOOLS_PROPERTY_DESCRIPTION = "Permitted List of MCP tools";

    @Override
    public void setConcreteConstData() {
        codedata().node(MCP_TOOL_KIT_CLASS);
    }

    @Override
    public void setConcreteTemplateData(TemplateContext context) {
        properties().custom().metadata()
                .label(TOOL_KIT_NAME_PROPERTY_LABEL).description(TOOL_KIT_NAME_DESCRIPTION).stepOut()
                .typeConstraint(Property.GLOBAL_SCOPE).value(TOOL_KIT_DEFAULT_CLASS_NAME)
                .type(Property.ValueType.IDENTIFIER).editable().stepOut()
                .addProperty(TOOL_KIT_NAME_PROPERTY);

        properties().custom().metadata()
                .label(PERMITTED_TOOLS_PROPERTY_LABEL).description(PERMITTED_TOOLS_PROPERTY_DESCRIPTION).stepOut()
                .typeConstraint(Property.GLOBAL_SCOPE).value(new ArrayList<String>())
                .type(Property.ValueType.MULTIPLE_SELECT).editable().stepOut()
                .addProperty(PERMITTED_TOOLS_PROPERTY);
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        Property permittedToolsProperty = sourceBuilder.flowNode.properties().get(PERMITTED_TOOLS_PROPERTY);
        Property toolKitNameProperty = sourceBuilder.flowNode.properties().get(TOOL_KIT_NAME_PROPERTY);

        sourceBuilder.acceptImport(Ai.BALLERINA_ORG, Ai.MCP_PACKAGE);
        sourceBuilder.acceptImport(Ai.BALLERINA_ORG, Ai.AI_PACKAGE);

        List<String> permittedTools = ((List<?>) permittedToolsProperty.value()).stream()
                .filter(String.class::isInstance).map(String.class::cast).toList();

        String toolKitName = String.valueOf(toolKitNameProperty.value());
        String sourceCode = generateMcpToolKitClassSource(toolKitName, permittedTools);
        sourceBuilder.token().source(sourceCode).skipFormatting();
        return sourceBuilder.textEdit().build();
    }

    private String generateMcpToolKitClassSource(String className, List<String> permittedTools) {
        Map<String, String> toolMapping = generatePermittedToolsMapping(permittedTools);
        String permittedToolsMappingConstructorExp = toolMapping.entrySet().stream()
                .map(e -> "                \"" + e.getKey() + "\" : self." + e.getValue())
                .collect(Collectors.joining(",\n"));

        String toolFunctions = toolMapping.values().stream().map(this::getToolMethodSignature)
                .collect(Collectors.joining("\n"));

        return String.format(
                "isolated class %s {%n" +
                        "    *ai:McpBaseToolKit;%n" +
                        "    private final mcp:StreamableHttpClient mcpClient;%n" +
                        "    private final readonly & ai:ToolConfig[] tools;%n" +
                        "%n" +
                        "    public isolated function init(string serverUrl," +
                        " mcp:Implementation info = {name: \"MCP\", version: \"1.0.0\"},%n" +
                        "        *mcp:StreamableHttpClientTransportConfig config) returns ai:Error? {%n" +
                        "        final map<ai:FunctionTool> permittedTools = {%n" +
                        "%s%n" +
                        "        };" +
                        "%n" +
                        "        do {%n" +
                        "            self.mcpClient = check new mcp:StreamableHttpClient(serverUrl, config);%n" +
                        "            self.tools = check ai:getPermittedMcpToolConfigs(self.mcpClient, info," +
                        " permittedTools).cloneReadOnly();%n" +
                        "        } on fail error e {%n" +
                        "            return error ai:Error(\"Failed to initialize MCP toolkit\", e);%n" +
                        "        }%n" +
                        "    }%n" +
                        "%n" +
                        "    public isolated function getTools() returns ai:ToolConfig[] => self.tools;%n" +
                        "%n" +
                        "%s" +
                        "}%n",
                className, permittedToolsMappingConstructorExp, toolFunctions
        );
    }

    private String getToolMethodSignature(String toolName) {
        return String.format("    @ai:AgentTool%n"
                + "    public isolated function %s(mcp:CallToolParams params) returns mcp:CallToolResult|error {%n"
                + "        return self.mcpClient->callTool(params);%n"
                + "    }%n", toolName);
    }


    private Map<String, String> generatePermittedToolsMapping(List<String> permittedTools) {
        return permittedTools.stream().filter(tool -> tool != null && !tool.isBlank())
                .collect(Collectors.toMap(name -> name, this::toMethodName));
    }

    private String toMethodName(String input) {
        String[] parts = input.split("\\s+");
        StringBuilder sb = new StringBuilder(parts[0].toLowerCase(Locale.UK));
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)
                        .toLowerCase(Locale.UK));
            }
        }
        String methodName = sb.toString();

        if (!Character.isJavaIdentifierStart(methodName.charAt(0))) {
            methodName = "_" + methodName;
        }
        methodName = methodName.chars()
                .mapToObj(c -> Character.isJavaIdentifierPart(c) ? String.valueOf((char) c) : "_")
                .collect(Collectors.joining());

        // Avoid keywords and reserved/predefined member names
        Set<String> predefinedMembers = Set.of("init", "getTools", "mcpClient", "tools");
        if (SyntaxInfo.isKeyword(methodName) || predefinedMembers.contains(methodName)) {
            methodName = "'" + methodName;
        }
        return methodName;
    }
}
