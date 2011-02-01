/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: TypeDescriptorTest.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/30 12:13:38 $
 *               by   $Author: sowen70 $
 * Created on 17-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.parser;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class TypeDescriptorTest extends TestCase {

	// array of strings
	public void testRetrieveSignitureForArrayDescriptor() {
		ArrayTypeDescriptor desc = new ArrayTypeDescriptor();
		desc.setName("AnArray");
		desc.setType("arrayofstring");

		BaseTypeDescriptor base = new BaseTypeDescriptor();
		base.setName("");
		base.setType("string");

		desc.setElementType(base);

		String[] names = new String[1];
		Class<?>[] types = new Class[1];

		List<TypeDescriptor> params = new ArrayList<TypeDescriptor>();
		params.add(desc);
		TypeDescriptor.retrieveSignature(params, names, types);

		assertEquals("AnArray", names[0]);
		assertEquals(String[].class, types[0]);
	}

	// array of strings, but type for array is defined as string
	// (which is logically warped, but some wsdl's describe their string arrays
	// this way).
	public void testRetrieveSignitureForArrayDescriptor3() {
		ArrayTypeDescriptor desc = new ArrayTypeDescriptor();
		desc.setName("AnArray");
		desc.setType("string");

		BaseTypeDescriptor base = new BaseTypeDescriptor();
		base.setName("");
		base.setType("string");

		desc.setElementType(base);

		String[] names = new String[1];
		Class<?>[] types = new Class[1];

		List<TypeDescriptor> params = new ArrayList<TypeDescriptor>();
		params.add(desc);
		TypeDescriptor.retrieveSignature(params, names, types);

		assertEquals("AnArray", names[0]);
		assertEquals(String[].class, types[0]);
	}

	// array of complex types
	public void testRetrieveSignitureForArrayDescriptor2() {
		ArrayTypeDescriptor desc = new ArrayTypeDescriptor();
		desc.setName("AnArray");
		desc.setType("complextype");

		ComplexTypeDescriptor complex = new ComplexTypeDescriptor();
		complex.setName("complex");
		complex.setType("complextype");

		desc.setElementType(complex);

		String[] names = new String[1];
		Class<?>[] types = new Class[1];

		List<TypeDescriptor> params = new ArrayList<TypeDescriptor>();
		params.add(desc);
		TypeDescriptor.retrieveSignature(params, names, types);

		assertEquals("AnArray", names[0]);
		assertEquals(org.w3c.dom.Element.class, types[0]);
	}

	public void testForCyclicTrue() {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("outertype");

		ComplexTypeDescriptor b = new ComplexTypeDescriptor();
		b.setName("b");
		b.setType("middletype");

		ComplexTypeDescriptor c = new ComplexTypeDescriptor();
		c.setName("c");
		c.setType("innertype");

		a.getElements().add(b);
		b.getElements().add(c);
		c.getElements().add(a);

		assertTrue("should be identified as cyclic", TypeDescriptor.isCyclic(a));
	}

	public void testForCyclicTrueWithArray() {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("outertype");

		ArrayTypeDescriptor b = new ArrayTypeDescriptor();
		b.setName("b");
		b.setType("arraytype");

		ComplexTypeDescriptor c = new ComplexTypeDescriptor();
		c.setName("c");
		c.setType("innertype");

		a.getElements().add(b);
		b.setElementType(c);
		c.getElements().add(a);

		assertTrue("should be identified as cyclic", TypeDescriptor.isCyclic(a));
	}

	public void testForCyclicFalse() {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("person");

		ComplexTypeDescriptor b = new ComplexTypeDescriptor();
		b.setName("b");
		b.setType("name");

		ComplexTypeDescriptor c = new ComplexTypeDescriptor();
		c.setName("c");
		c.setType("age");

		a.getElements().add(b);
		a.getElements().add(c);

		assertFalse("should be not identified as cyclic", TypeDescriptor
				.isCyclic(a));
	}

	public void testQNameAsString() {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setQnameFromString("{URI}localPart");
		assertEquals("URI", a.getQname().getNamespaceURI());
		assertEquals("localPart", a.getQname().getLocalPart());

		a = new ComplexTypeDescriptor();
		a.setQnameFromString("{}localPart");
		assertEquals("", a.getQname().getNamespaceURI());
		assertEquals("localPart", a.getQname().getLocalPart());
	}
	
	public void testBaseTypeKnownSigniture() {
		TypeDescriptor decimal=new BaseTypeDescriptor();
		decimal.setName("adecimal");
		decimal.setType("decimal");
		
		List<TypeDescriptor> params=new ArrayList<TypeDescriptor>();
		String [] names=new String[1];
		Class<?> [] types=new Class[1];
		params.add(decimal);
		TypeDescriptor.retrieveSignature(params, names, types);
		
		assertEquals("should only be 1 type",1,types.length);
		assertEquals("should only be 1 name",1,names.length);
		
		assertEquals("name should be adecimal","adecimal",names[0]);
		assertEquals("type should be double",Double.TYPE,types[0]);
	}
	
	public void testBaseTypeUnrecognisedSigniture() {
		TypeDescriptor date=new BaseTypeDescriptor();
		date.setName("adate");
		date.setType("date");
		
		List<TypeDescriptor> params=new ArrayList<TypeDescriptor>();
		String [] names=new String[1];
		Class<?> [] types=new Class[1];
		params.add(date);
		TypeDescriptor.retrieveSignature(params, names, types);
		
		assertEquals("should only be 1 type",1,types.length);
		assertEquals("should only be 1 name",1,names.length);
		
		assertEquals("name should be adecimal","adate",names[0]);
		assertEquals("type should be string",String.class,types[0]);
	}
	
	public void testComplex() {
		TypeDescriptor complex=new ComplexTypeDescriptor();
		complex.setName("acomplex");
		complex.setType("complextype");
		
		List<TypeDescriptor> params=new ArrayList<TypeDescriptor>();
		String [] names=new String[1];
		Class<?> [] types=new Class[1];
		params.add(complex);
		TypeDescriptor.retrieveSignature(params, names, types);
		
		assertEquals("should only be 1 type",1,types.length);
		assertEquals("should only be 1 name",1,names.length);
		
		assertEquals("name should be adecimal","acomplex",names[0]);
		assertEquals("type should be string",org.w3c.dom.Element.class,types[0]);
	}

}
