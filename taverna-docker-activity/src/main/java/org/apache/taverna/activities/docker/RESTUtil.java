/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.docker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.security.cert.CertificateException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class RESTUtil {

    /**
     * Http header name for content type
     */
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Http content type value for JSON messages.
     */
    private static final String JSON_CONTENT_TYPE = "application/json";

    /**
     * Logger
     */
    private static Logger LOG = Logger.getLogger(RESTUtil.class);


    public static boolean createContainer(DockerConfig dockerConfig) {
        try {
            URL url = new URL(dockerConfig.getCreateContainerURL());
            org.apache.http.conn.ssl.SSLSocketFactory factory = new org.apache.http.conn.ssl.SSLSocketFactory(SSLContext.getDefault());
            Scheme https = new Scheme(DockerConfig.PROTOCOL,factory , url.getPort());
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(https);
            ClientConnectionManager connectionManager = new SingleClientConnManager(null, schemeRegistry);
            Map<String,String> headers = new HashMap<String,String>();
            headers.put(CONTENT_TYPE, JSON_CONTENT_TYPE);
            DockerHttpResponse response = doPost(connectionManager,dockerConfig.getCreateContainerURL(), headers, dockerConfig.getCreateContainerPayload());
            if(response.getStatusCode() == 201){
                JsonNode node = getJson(response.getBody());
                LOG.info(String.format("Successfully created Docker container id: %s ", getDockerId(node)));
                return true;
            }

        } catch (MalformedURLException e1) {
            LOG.error(String.format("Malformed URL encountered. This can be due to invalid URL parts. " +
                            "Docker Host=%s, Port=%d and Resource Path=%s",
                    dockerConfig.getContainerHost(),
                    dockerConfig.getRemoteAPIPort(),
                    DockerConfig.CREATE_CONTAINER_RESOURCE_PATH), e1);
        } catch (NoSuchAlgorithmException e2) {
            LOG.error("Failed to create SSLContext for invoking the REST service over https.", e2);
        } catch (IOException e3) {
            LOG.error("Error occurred while reading the docker http response", e3);
        }
        return false;
    }

    private static DockerHttpResponse doPost(ClientConnectionManager connectionManager, String url, Map<String, String> headers, JsonNode payload) {
        DefaultHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        DockerHttpResponse dockerResponse = null;
        HttpPost httpPost = null;
        try {
            httpPost = new HttpPost(url);
            HttpEntity entity = new StringEntity(payload.toString());
            httpPost.setEntity(entity);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(new BasicHeader(entry.getKey(), entry.getValue()));
            }
            httpClient = new DefaultHttpClient(connectionManager, null);
            response = httpClient.execute(httpPost);
            if (response != null) {
                dockerResponse = new DockerHttpResponse(response.getAllHeaders(), response.getStatusLine().getStatusCode(),readBody(response.getEntity()).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Failed to complete Http POST invocation", e);
            dockerResponse = new DockerHttpResponse(new Header[]{new BasicHeader(
                    CONTENT_TYPE, JSON_CONTENT_TYPE)},
                    500,
                    "{\"error\":\"internal server error\", \"message\":\""+ e.getMessage() +"\"}");
        } finally {
            if(httpPost != null){
              httpPost.releaseConnection();
            }
            if (httpClient != null) {
                httpClient.close();
            }
            if (response != null) {
                try {
                    response.close();
                } catch (IOException ignore) {}
            }
        }
      return dockerResponse;
    }

    private static StringBuilder readBody(HttpEntity entity) throws IOException {
        String charset = null;
        String contentType = entity.getContentType().getValue().toLowerCase();
        String[] contentTypeParts = contentType.split(";");
        for (String contentTypePart : contentTypeParts) {
            contentTypePart = contentTypePart.trim();
            if (contentTypePart.startsWith("charset=")) {
                charset = contentTypePart.substring("charset=".length());
            }
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), charset != null ? charset : "UTF-8"));
        String str;
        StringBuilder responseBuilder = new StringBuilder();
        while ((str = reader.readLine()) != null) {
            responseBuilder.append(str + "\n");
         }
        return responseBuilder;
    }

    private static JsonNode getJson(String s) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(s);
    }

    private static String getDockerId(JsonNode node){
        String dockerId = null;
        Iterator<JsonNode> itr = node.elements();
        while(itr.hasNext()){
            JsonNode child = itr.next();
            if("id".equalsIgnoreCase(child.textValue())){
                dockerId =  child.textValue();
                break;
            }
        }
        return dockerId;
    }

}
