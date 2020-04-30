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

import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
import org.apache.camel.*;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.support.ExchangeHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.camel.component.qute.QuteConstants.QUTE_RESOURCE_URI;
import static org.apache.camel.component.qute.QuteConstants.QUTE_TEMPLATE;

/**
 * Represents a Qute endpoint.
 */
@UriEndpoint(firstVersion = "3.3.0", scheme = "qute", title = "Qute Template", syntax="qute:resourceUri", producerOnly = true, label = "transformation")
public class QuteEndpoint extends ResourceEndpoint {

    private Engine engine;

    public QuteEndpoint() {
    }

    public QuteEndpoint(String endpointUri, QuteComponent component, String resourceUri) {
        super(endpointUri, component, resourceUri);
    }

    public Producer createProducer() throws Exception {
        return new QuteProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("Consumer not supported");
    }

    @Override
    public ExchangePattern getExchangePattern() {
        return ExchangePattern.InOut;
    }

    @Override
    protected void onExchange(Exchange exchange) throws Exception {
        String content = exchange.getIn().getHeader(QUTE_TEMPLATE, String.class);
        if (content != null) {
            exchange.getIn().removeHeader(QUTE_TEMPLATE);
            process(exchange, content);
        } else {
            String newResourceUri = exchange.getIn().getHeader(QUTE_RESOURCE_URI, String.class);
            InputStream inputStream;
            if (newResourceUri != null) {
                exchange.getIn().removeHeader(QUTE_RESOURCE_URI);
                inputStream = loadResource(newResourceUri);
            } else {
                inputStream =  getResourceAsInputStream();
            }
            String contentFromURI = getCamelContext().getTypeConverter().convertTo(String.class, exchange, inputStream);
            process(exchange, contentFromURI);
        }
    }

    private void process(Exchange exchange, String content) throws IOException {
        // load includes if exists
        java.util.regex.Pattern regex = Pattern.compile("\\{#include(.*?)\\}");
        Matcher regexMatcher = regex.matcher(content);
        while (regexMatcher.find()) {
            String templateName = regexMatcher.group(1).trim();
            InputStream inputStream = loadResource(templateName);
            String contentFromURI = getCamelContext().getTypeConverter().convertTo(String.class, exchange, inputStream);
            engine.putTemplate(templateName, engine.parse(contentFromURI));
        }

        // parse template
        Template template = engine.parse(content);
        Map<String, Object> variableMap = ExchangeHelper.createVariableMap(exchange);
        String result = template.data(variableMap).render();
        exchange.getIn().setBody(result);
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
