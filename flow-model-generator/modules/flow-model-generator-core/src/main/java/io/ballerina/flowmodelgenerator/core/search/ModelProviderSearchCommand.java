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

package io.ballerina.flowmodelgenerator.core.search;

import com.google.gson.reflect.TypeToken;
import io.ballerina.flowmodelgenerator.core.LocalIndexCentral;
import io.ballerina.flowmodelgenerator.core.model.*;
import io.ballerina.modelgenerator.commons.SearchResult;
import io.ballerina.projects.Project;
import io.ballerina.tools.text.LineRange;

import java.lang.reflect.Type;
import java.util.*;


/**
 * Handles the search command for connectors.
 *
 * @since 1.0.0
 */
public class ModelProviderSearchCommand extends SearchCommand {
    private static List<Item> modelProviders;

    public ModelProviderSearchCommand(Project project, LineRange position, Map<String, String> queryMap) {
        super(project, position, queryMap);
    }

    @Override
    protected List<Item> defaultView() {
        return this.getModelProviders();
    }

    @Override
    protected List<Item> search() {
        List<Item> modelProviders = this.getModelProviders();
        if (modelProviders.isEmpty()) {
            return modelProviders;
        }
        Category modelProviderCategory = (Category) modelProviders.getFirst();
        List<Item> availableProvider = modelProviderCategory.items();
        List<Item> filteredProviders = availableProvider.stream().filter(p -> p instanceof AvailableNode availableNode && availableNode.codedata().module().contains(query)).toList();
        modelProviderCategory.items().removeAll(filteredProviders);
        modelProviderCategory.items().addAll(filteredProviders);
        return List.of(modelProviderCategory);
    }

    @Override
    protected Map<String, List<SearchResult>> fetchPopularItems() {
        return Collections.emptyMap();
    }

    private List<Item> getModelProviders() {
        if (modelProviders == null) {
            modelProviders = LocalIndexCentral.getInstance().getModelProviders();
        }
        return modelProviders;
    }
}
