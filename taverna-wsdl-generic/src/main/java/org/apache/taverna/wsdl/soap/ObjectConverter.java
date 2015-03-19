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

package org.apache.taverna.wsdl.soap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * This class replicates the behaviour of data conversion when using DataThingFactory.bake in Taverna 1.
 * </p>
 * <p>
 * In particular it deals with the conversion of arrays to ArrayList
 * </p>
 * @author Stuart Owen
 * 
 *
 */
public class ObjectConverter {

	/**
	 * Converts an Object into an appropriate type, in particular recursively converting [] arrays to List<?>'s.<br>
	 * 
	 * This method is a copy of convertObject in DataThingFactory from Taverna 1
	 * @param theObject
	 * @return
	 */
	public static Object convertObject(Object theObject) {

		if (theObject == null) {
			return null;
		}
		// If an array type...
		Class<?> theClass = theObject.getClass();
		if (theClass.isArray()) {
			// Special case for byte[]
			if (theObject instanceof byte[]) {
				// System.out.println("Found a byte[], returning it.");
				return theObject;
			} //extra primitive object checks for those fun edge cases!
			else if (theObject instanceof int[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((int[])theObject).length;i++) {
					Object a = ((int[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else if (theObject instanceof short[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((short[])theObject).length;i++) {
					Object a = ((short[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else if (theObject instanceof long[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((long[])theObject).length;i++) {
					Object a = ((long[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else if (theObject instanceof float[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((float[])theObject).length;i++) {
					Object a = ((float[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else if (theObject instanceof double[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((double[])theObject).length;i++) {
					Object a = ((double[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else if (theObject instanceof boolean[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((boolean[])theObject).length;i++) {
					Object a = ((boolean[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else if (theObject instanceof char[]){
				List<Object> l = new ArrayList<Object>();
				for (int i = 0; i<((char[])theObject).length;i++) {
					Object a = ((char[])theObject)[i];
					l.add(convertObject(a));
				}
				return l;
			} else {
				// For all other arrays, create a new
				// List and iterate over the array,
				// unpackaging the item and recursively
				// putting it into the new List after
				// conversion				
				
				// System.out.println("Found an array length
				// "+theArray.length+", repacking as List...");
				
				List<Object> l = new ArrayList<Object>();				
				Object[] theArray = (Object[]) theObject;
				for (int i = 0; i < theArray.length; i++) {
					l.add(convertObject(theArray[i]));
				}
				return l;
			}
		}
		// If a collection, iterate over it and copy
		if (theObject instanceof Collection) {
			if (theObject instanceof List) {
				// System.out.println("Re-packing a list...");
				List<Object> l = new ArrayList<Object>();
				for (Iterator<?> i = ((List<?>) theObject).iterator(); i.hasNext();) {
					l.add(convertObject(i.next()));
				}
				return l;
			} else if (theObject instanceof Set) {
				// System.out.println("Re-packing a set...");
				Set<Object> s = new HashSet<Object>();
				for (Iterator<?> i = ((Set<?>) theObject).iterator(); i.hasNext();) {
					s.add(convertObject(i.next()));
				}
				return s;
			}
		}
		// If a number then return the string representation for it
		if (theObject instanceof Number) {
			// System.out.println("Found a number, converting it to a
			// string...");
			return theObject.toString();
		}
		// Otherwise just return the object
		// System.out.println("Found a "+theObject.getClass().getName()+",
		// returning it");
		return theObject;
	}
}
