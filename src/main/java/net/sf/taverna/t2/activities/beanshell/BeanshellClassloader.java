/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.beanshell;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import net.sf.taverna.raven.repository.Artifact;
import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.raven.repository.Repository;
import net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader;

public class BeanshellClassloader extends URLClassLoader {
	private List<Artifact> artifacts;
	private Repository repo;

	public BeanshellClassloader(List<Artifact> artifacts) {
		super(new URL[] {});
		this.artifacts = artifacts;
		if (this.getClass().getClassLoader() instanceof LocalArtifactClassLoader) {

			repo = ((LocalArtifactClassLoader) this.getClass()
					.getClassLoader()).getRepository();
			for (Artifact a : artifacts) {
				repo.addArtifact(a);
			}
			repo.update();
		}
	}

	@Override
	protected Class<?> findClass(String classname) throws ClassNotFoundException {
		if (repo!=null) {
			for (Artifact a : artifacts) {
				ClassLoader cl;
				try {
					cl = repo.getLoader(a, null);
					Class<?> result = cl.loadClass(classname);
					return result;
				} catch (ArtifactNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ArtifactStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					
				}
			}
			throw new ClassNotFoundException("No class found for:"+classname);
			
		}
		else {
			return this.getClass().getClassLoader().loadClass(classname);
		}
	}
	
	
}
