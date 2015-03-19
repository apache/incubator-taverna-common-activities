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

package org.apache.taverna.wsdl.parser;

/**
 * @author Dmitry Repchevsky
 */

public enum WSRF_RPOperation {
    
    GetResourcePropertyDocument("http://docs.oasis-open.org/wsrf/rpw-2/GetResourcePropertyDocument/GetResourcePropertyDocumentRequest"),
    GetResourceProperty("http://docs.oasis-open.org/wsrf/rpw-2/GetResourceProperty/GetResourcePropertyRequest"),
    GetMultipleResourceProperties("http://docs.oasis-open.org/wsrf/rpw-2/GetMultipleResourceProperties/GetMultipleResourcePropertiesRequest"),
    QueryResourceProperties("http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest"),
    QueryExpressionDialect("http://docs.oasis-open.org/wsrf/rpw-2/QueryResourceProperties/QueryResourcePropertiesRequest"),
    PutResourcePropertyDocument("http://docs.oasis-open.org/wsrf/rpw-2/PutResourcePropertyDocument/PutResourcePropertyDocumentRequest"),
    SetResourceProperties("http://docs.oasis-open.org/wsrf/rpw-2/SetResourceProperties/SetResourcePropertiesRequest"),
    InsertResourceProperties("http://docs.oasis-open.org/wsrf/rpw-2/InsertResourceProperties/InsertResourcePropertiesRequest"),
    UpdateResourceProperties("http://docs.oasis-open.org/wsrf/rpw-2/UpdateResourceProperties/UpdateResourcePropertiesRequest"),
    DeleteResourceProperties("http://docs.oasis-open.org/wsrf/rpw-2/DeleteResourceProperties/DeleteResourcePropertiesRequest");
    
    public final String SOAP_ACTION;
    
    private WSRF_RPOperation(String req_action) {
        SOAP_ACTION = req_action;
    }
}
