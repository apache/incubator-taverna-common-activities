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

package org.apache.taverna.wsdl.parser.schema;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Generic Tree Node implementation class that is compatible with MutableTreeNode 
 * Swing interface. The class is used as a replacement of DefaultMutableTreeNode to 
 * remove Swing references from the package. It may be used as an implementation though
 * {@code
 * MyMutableTreeNode extends XSNode<TreeNode, MutableTreeNode> implements MutableTreeNode
 * }.
 * 
 * @author Dmitry Repchevsky
 */

public class XSNode <T, V extends T>  {

    private V parent;
    private Object userObject;
    private final List<V> children;
    
    public XSNode() {
        children = new ArrayList();
    }
    
    public void insert(V child, int index) {
        if (child instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)child;
            XSNode oldParent = node.getParent();
            if (oldParent != null) {
                oldParent.remove(node);
            }
            node.setParent(this);
        }
        children.add(index, child);
    }

    public void remove(int index) {
        V child = children.remove(index);
        if (child instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)child;
            node.setParent(null);
        }
    }

    public void remove(V child) {
        if (children.remove(child) && child instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)child;
            node.setParent(null);
        }
    }

    public Object getUserObject() {
        return userObject;
    }
    
    public void setUserObject(Object object) {
        this.userObject = object;
    }

    public void removeFromParent() {
        if (parent instanceof XSNode) {
            XSNode<XSNode,XSNode> node = (XSNode)parent;
            node.remove(this);
        }
    }

    public void setParent(V newParent) {
        this.parent = newParent;
    }

    public V getChildAt(int childIndex) {
        return children.get(childIndex);
    }

    public int getChildCount() {
        return children.size();
    }

    public V getParent() {
        return parent;
    }

    public int getIndex(T node) {
        return children.indexOf(node);
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Enumeration children() {
        return Collections.enumeration(children);
    }
    
    public void removeAllChildren() {
        for (int i = getChildCount()-1; i >= 0; i--) {
            remove(i);
        }
    }
}
