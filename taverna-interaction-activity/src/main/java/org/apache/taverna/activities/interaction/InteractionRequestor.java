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

package org.apache.taverna.activities.interaction;

import java.util.Map;

/**
 * @author alanrw
 * 
 */
public interface InteractionRequestor {

	String getRunId();

	Map<String, Object> getInputData();

	void fail(String string);

	void carryOn();

	String generateId();
	
	// The path to whatever requested the interaction
	String getPath();
	
	// The number of times whatever requested the interaction has requested one
	Integer getInvocationCount();

	InteractionActivityType getPresentationType();

	InteractionType getInteractionType();

	String getPresentationOrigin();

	void receiveResult(Map<String, Object> resultMap);

}
