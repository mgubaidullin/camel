/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.qute;

import java.util.Map;

import io.quarkus.qute.Engine;
import org.apache.camel.Endpoint;

import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.annotations.Component;
import org.apache.camel.support.DefaultComponent;
import org.apache.camel.util.ObjectHelper;

/**
 * Represents the component that manages {@link engine}.
 * URI pattern: {@code qute:template}
 */
@Component("qute")
public class QuteComponent extends DefaultComponent {

    @Metadata(label = "advanced")
    private Engine engine = Engine.builder().addDefaults().build();

    public QuteComponent() {
    }

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (ObjectHelper.isEmpty(remaining)) {
            throw new IllegalArgumentException("Application must be configured on endpoint using syntax qute:template");
        }
        QuteEndpoint endpoint = new QuteEndpoint(uri, this, remaining);
        endpoint.setEngine(getEngine());
        setProperties(endpoint, parameters);
        return endpoint;
    }

    public Engine getEngine() {
        return engine;
    }

    /**
     * To use a custom {@link engine}
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
