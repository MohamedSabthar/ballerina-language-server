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

public class EmbeddingProviderBuilder extends CallBuilder {
    public static final String LABEL = "Embedding Provider";
    public static final String DESCRIPTION = "Embedding model providers available in the flow";

    public static final String EMBEDDING_PROVIDER_NAME = "Embedding Provider Variable name";
    public static final String EMBEDDING_PROVIDER_NAME_DOC = EMBEDDING_PROVIDER_NAME;
    public static final String CHECK_ERROR_DOC = "Terminate on error";

    @Override
    public void setConcreteConstData() {
        metadata().label(LABEL);
        codedata().node(NodeKind.EMBEDDING_PROVIDER);
    }

    @Override
    protected NodeKind getFunctionNodeKind() {
        return NodeKind.EMBEDDING_PROVIDER;
    }

    @Override
    protected FunctionData.Kind getFunctionResultKind() {
        return FunctionData.Kind.EMBEDDING_PROVIDER;
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        sourceBuilder
                .token().keyword(SyntaxKind.FINAL_KEYWORD).stepOut()
                .newVariable();

        sourceBuilder.token()
                .keyword(SyntaxKind.CHECK_KEYWORD)
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
                .functionResultKind(FunctionData.Kind.EMBEDDING_PROVIDER)

                .userModuleInfo(moduleInfo);

        functionData = functionDataBuilder.build();
        metadata()
                .label(functionData.packageName())
                .description(functionData.description())
                .icon(CommonUtils.generateIcon(functionData.org(), functionData.packageName(),
                        functionData.version()));
        codedata()
                .node(NodeKind.EMBEDDING_PROVIDER)
                .org(functionData.org())
                .module(functionData.moduleName())
                .packageName(functionData.packageName())
                .object(functionData.name())
                .version(functionData.version())
                .symbol("init");

        setParameterProperties(functionData);

        if (CommonUtils.hasReturn(functionData.returnType())) {
            setReturnTypeProperties(functionData, context, EMBEDDING_PROVIDER_NAME, EMBEDDING_PROVIDER_NAME_DOC, false);
        }

        properties()
                .scope(Property.GLOBAL_SCOPE)
                .checkError(true, CHECK_ERROR_DOC, false);
    }

}
