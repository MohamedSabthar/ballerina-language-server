package io.ballerina.flowmodelgenerator.core.model.node;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.flowmodelgenerator.core.model.*;
import io.ballerina.modelgenerator.commons.CommonUtils;
import io.ballerina.modelgenerator.commons.FunctionData;
import io.ballerina.modelgenerator.commons.FunctionDataBuilder;
import io.ballerina.modelgenerator.commons.ModuleInfo;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VectorKnowledgeBaseBuilder extends CallBuilder{
    public static final String LABEL = "Vector Knowledge Base";
    public static final String DESCRIPTION = "vector knowledge-bases available in the flow";

    public static final String VECTOR_KNOWLEDGE_BASE_NAME = "Vector knowledge-base name";
    public static final String VECTOR_KNOWLEDGE_BASE_PROVIDER_NAME_DOC = VECTOR_KNOWLEDGE_BASE_NAME;
    public static final String CHECK_ERROR_DOC = "Terminate on error";

    @Override
    public void setConcreteConstData() {
        metadata().label(LABEL);
        codedata().node(NodeKind.VECTOR_KNOWLEDGE_BASE);
    }

    @Override
    protected NodeKind getFunctionNodeKind() {
        return NodeKind.VECTOR_KNOWLEDGE_BASE;
    }

    @Override
    protected FunctionData.Kind getFunctionResultKind() {
        return FunctionData.Kind.VECTOR_KNOWLEDGE_BASE;
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        sourceBuilder
                .token().keyword(SyntaxKind.FINAL_KEYWORD).stepOut()
                .newVariable();

        sourceBuilder.token()
                .keyword(SyntaxKind.NEW_KEYWORD)
                .stepOut()
                .functionParameters(sourceBuilder.flowNode,
                        Set.of(Property.VARIABLE_KEY, Property.TYPE_KEY, Property.SCOPE_KEY,
                                Property.CHECK_ERROR_KEY));


        sourceBuilder.textEdit();
        sourceBuilder.acceptImport();

        return sourceBuilder.build();
    }

    @Override
    public void setConcreteTemplateData(NodeBuilder.TemplateContext context) {
        Codedata codedata = context.codedata();
        FunctionData functionData;

        FunctionDataBuilder functionDataBuilder = new FunctionDataBuilder()
                .parentSymbolType(codedata.object())
                .name(codedata.symbol())
                .moduleInfo(new ModuleInfo(codedata.org(), codedata.packageName(), codedata.module(),
                        codedata.version()))
                .lsClientLogger(context.lsClientLogger())
                .functionResultKind(FunctionData.Kind.VECTOR_KNOWLEDGE_BASE)

                .userModuleInfo(moduleInfo);

        functionData = functionDataBuilder.build();
        metadata()
                .label(functionData.packageName())
                .description(functionData.description())
                .icon(CommonUtils.generateIcon(functionData.org(), functionData.packageName(),
                        functionData.version()));
        codedata()
                .node(NodeKind.VECTOR_KNOWLEDGE_BASE)
                .org(functionData.org())
                .module(functionData.moduleName())
                .packageName(functionData.packageName())
                .object(functionData.name())
                .version(functionData.version())
                .symbol("init");

        setParameterProperties(functionData);

        if (CommonUtils.hasReturn(functionData.returnType())) {
            setReturnTypeProperties(functionData, context, VECTOR_KNOWLEDGE_BASE_NAME, VECTOR_KNOWLEDGE_BASE_PROVIDER_NAME_DOC, false);
        }

        properties()
                .scope(Property.GLOBAL_SCOPE)
                .checkError(true, CHECK_ERROR_DOC, false);
    }
}
