package io.ballerina.flowmodelgenerator.core.model.node;

import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.flowmodelgenerator.core.model.*;
import io.ballerina.modelgenerator.commons.CommonUtils;
import io.ballerina.modelgenerator.commons.FunctionData;
import io.ballerina.modelgenerator.commons.FunctionDataBuilder;
import io.ballerina.modelgenerator.commons.ModuleInfo;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ModelProviderBuilder extends CallBuilder {
    public static final String MODEL_PROVIDER_NAME = "Model Provider Variable name";
    public static final String MODEL_PROVIDER_NAME_DOC = MODEL_PROVIDER_NAME;
    public static final String CHECK_ERROR_DOC = "Terminate on error";

    public static final String LABEL = "Model Provider";
    public static final String DESCRIPTION = "The model-provider used in the flow to connect to an LLM";

    @Override
    public void setConcreteConstData() {
        metadata().label(LABEL);
        codedata().node(NodeKind.MODEL_PROVIDER).symbol("init");
    }

    @Override
    protected NodeKind getFunctionNodeKind() {
        return NodeKind.MODEL_PROVIDER;
    }

    @Override
    protected FunctionData.Kind getFunctionResultKind() {
        return FunctionData.Kind.CLASS_INIT;
    }

    @Override
    public Map<Path, List<TextEdit>> toSource(SourceBuilder sourceBuilder) {
        if (sourceBuilder.flowNode.codedata().object().equals("getDefaultModelProvider")) {
            sourceBuilder
                    .token().keyword(SyntaxKind.FINAL_KEYWORD).stepOut()
                    .newVariable();

            var flowNode = getFlowNode(sourceBuilder);

            var module = sourceBuilder.flowNode.codedata().module();
            String methodCallPrefix = (module != null) ? module.substring(module.lastIndexOf('.') + 1) + ":" : "";
            String methodCall = methodCallPrefix + "getDefaultModelProvider";

            sourceBuilder.token()
                    .keyword(SyntaxKind.CHECK_KEYWORD)
                    .name(methodCall)
                    .stepOut()
                    .functionParameters(flowNode,
                            Set.of(Property.VARIABLE_KEY, Property.TYPE_KEY, Property.SCOPE_KEY,
                                    Property.CHECK_ERROR_KEY));


            sourceBuilder.textEdit();
            sourceBuilder.acceptImport();
            return sourceBuilder.build();
        }
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

    private static FlowNode getFlowNode(SourceBuilder sourceBuilder) {
        var codedata = new Codedata(
                sourceBuilder.flowNode.codedata().node(), sourceBuilder.flowNode.codedata().org(), sourceBuilder.flowNode.codedata().module(),
                sourceBuilder.flowNode.codedata().packageName(), null, "getDefaultModelProvider",
                sourceBuilder.flowNode.codedata().version(), sourceBuilder.flowNode.codedata().lineRange(), sourceBuilder.flowNode.codedata().sourceCode(), sourceBuilder.flowNode.codedata().parentSymbol(),
                sourceBuilder.flowNode.codedata().resourcePath(),  sourceBuilder.flowNode.codedata().id(), sourceBuilder.flowNode.codedata().isNew(), sourceBuilder.flowNode.codedata().isGenerated(),
                sourceBuilder.flowNode.codedata().inferredReturnType());
        return new FlowNode(sourceBuilder.flowNode.id(), sourceBuilder.flowNode.metadata(), codedata
                , sourceBuilder.flowNode.returning(), sourceBuilder.flowNode.branches(), sourceBuilder.flowNode.properties(),
                sourceBuilder.flowNode.diagnostics(), sourceBuilder.flowNode.flags());
    }

    @Override
    public void setConcreteTemplateData(TemplateContext context) {
        Codedata codedata = context.codedata();
//        if (codedata.symbol().equals("getDefaultModelProvider")) {
//            metadata()
//                    .label(codedata.packageName())
//                    .description("");
//            codedata()
//                    .node(NodeKind.MODEL_PROVIDER)
//                    .org(codedata.org())
//                    .module(codedata.module())
//                    .packageName(codedata.packageName())
//                    .version(codedata.packageName())
//                    .version(codedata.version())
//                    .symbol("getDefaultModelProvider");
//
//            setReturnTypeProperties(functionData, context, MODEL_PROVIDER_NAME, MODEL_PROVIDER_NAME_DOC, false);
//            properties()
//                    .scope(Property.GLOBAL_SCOPE)
//                    .checkError(true, CHECK_ERROR_DOC, false);
//            return;
//        }
        // TODO: getDefaultModelProvider

        FunctionData functionData;

        FunctionDataBuilder functionDataBuilder = new FunctionDataBuilder()
                .parentSymbolType(codedata.object())
                .name(codedata.symbol())
                .moduleInfo(new ModuleInfo(codedata.org(), codedata.packageName(), codedata.module(),
                        codedata.version()))
                .lsClientLogger(context.lsClientLogger())
                .functionResultKind(FunctionData.Kind.MODEL_PROVIDER)

                .userModuleInfo(moduleInfo);

        functionData = functionDataBuilder.build();
        metadata()
                .label(functionData.packageName())
                .description(functionData.description())
                .icon(CommonUtils.generateIcon(functionData.org(), functionData.packageName(),
                        functionData.version()));
        codedata()
                .node(NodeKind.MODEL_PROVIDER)
                .org(functionData.org())
                .module(functionData.moduleName())
                .packageName(functionData.packageName())
                .object(functionData.name())
                .version(functionData.version())
                .symbol(codedata.symbol());

        setParameterProperties(functionData);

        if (CommonUtils.hasReturn(functionData.returnType())) {
            setReturnTypeProperties(functionData, context, MODEL_PROVIDER_NAME, MODEL_PROVIDER_NAME_DOC, false);
        }

        properties()
                .scope(Property.GLOBAL_SCOPE)
                .checkError(true, CHECK_ERROR_DOC, false);
    }

}
