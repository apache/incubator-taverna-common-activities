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

package org.apache.taverna.activities.wsdl;

import java.io.IOException;
import java.util.Map;

import org.apache.taverna.activities.wsdl.xmlsplitter.XMLInputSplitterActivity;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.UnknownOperationException;

/**
 * Interface for an activity such as {@link WSDLActivity} and
 * {@link XMLInputSplitterActivity} that can provide {@link TypeDescriptor}s for
 * its inputs.
 *
 * @author Stian Soiland-Reyes
 *
 * @param <ActivityBeanType> The configuration bean type of the activity
 */
public interface InputPortTypeDescriptorActivity {

	/**
	 * Provides access to the TypeDescriptor for a given input port name.
	 * <br>
	 * This TypeDescriptor represents the Type defined in the schema for this Activities
	 * WSDL.
	 *
	 * @param portName
	 * @return the TypeDescriptor, or null if the portName is not recognised.
	 * @throws UnknownOperationException if the operation this Activity is associated with doesn't exist.
	 * @throws IOException
	 *
	 * @see TypeDescriptor
	 * @see #getTypeDescriptorsForInputPorts()
	 * @see #getTypeDescriptorForOutputPort(String)
	 */
	public abstract TypeDescriptor getTypeDescriptorForInputPort(String portName)
			throws UnknownOperationException, IOException;

	/**
	 * Return TypeDescriptor for a all input ports.
	 * <p>
	 * This TypeDescriptor represents the Type defined in the schema for this Activities
	 * WSDL.
	 *
	 * @param portName
	 * @return A {@link Map} from portname to {@link TypeDescriptor}
	 * @throws UnknownOperationException if the operation this Activity is associated with doesn't exist.
	 * @throws IOException If the WSDL or some of its dependencies could not be read
	 *
	 * @see TypeDescriptor
	 * @see #getTypeDescriptorForInputPort(String)
	 * @see #getTypeDescriptorsForOutputPorts()
	 */
	public abstract Map<String, TypeDescriptor> getTypeDescriptorsForInputPorts()
			throws UnknownOperationException, IOException;

}