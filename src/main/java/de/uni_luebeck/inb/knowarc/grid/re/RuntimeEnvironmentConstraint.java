/* Part of the KnowARC Janitor Use-case processor for taverna
 *  written 2007-2010 by Hajo Nils Krabbenhoeft and Steffen Moeller
 *  University of Luebeck, Institute for Neuro- and Bioinformatics
 *  University of Luebeck, Institute for Dermatolgy
 *
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this package; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package de.uni_luebeck.inb.knowarc.grid.re;
 
import java.util.Collection;
import java.util.Iterator;

/**
 * When use cases present a runtime environment, then they don't want to express
 * that these would provide such, but that these request such, i.e. they constrain
 * lists of potentially eligible queues. 
 * @author Steffen Moeller
 * @since 2008
 */
public class RuntimeEnvironmentConstraint extends RuntimeEnvironment {
	
	
	/**
	 *  Identifies the relation between runtime environments to be tested
	 */
	protected String relation;
	
	/**
	 * Accessor function for relation
	 */
	public String getRelation() {
		return this.relation;
	}
	
	/**
	 * Tests of a relation is supported
	 * @param relation
	 * @return true iff in <, > , <=, >=, =
	 */
	public static boolean acceptedRelation(String relation) {
		if ((null == relation) || relation.equals("")) {
			return false;
		}
		return relation.equals("=")||relation.equals(">=")||relation.equals("<=")||relation.equals(">")||relation.equals("<");
	}
	
	/**
	 * If there is no relation specified, presume >=
	 */
	public static String DefaultRelation=new String(">=");
	/**
	 * Constructor
	 * @param id - expects the name of the runtime environment together with the version to which the
	 * @param relation - relates to (">","<","=",">=","<=")
	 */
	public RuntimeEnvironmentConstraint(String id, String relation) {
		super(id);
		if (null == relation || relation.equals("")) {
			relation=RuntimeEnvironmentConstraint.DefaultRelation;
		}
		if (relation.equals("==")) {
			relation="=";
		}
		else if (relation.equals("=<")) {
			relation="<=";
		}
		else if (!RuntimeEnvironmentConstraint.acceptedRelation(relation)) {
			System.err.println("Unknown relation '"+relation+"', presuming '"+RuntimeEnvironmentConstraint.DefaultRelation+"'");
			relation=RuntimeEnvironmentConstraint.DefaultRelation;
		}
		this.relation=relation;
	}
	
	/**
	 * Perfoms test if the RuntimeEnvironment (RE) passed as argument fulfills the requirements set by the constraint.
	 * @param re - RE to test
	 * @return true iff the RE fulfills this REconstraint.
	 */
	public boolean isFulfilledBy(RuntimeEnvironment re) {
		//System.err.println(re.getID()+" " + this.getRelation() + " "+this.getID() + " ?");
		if (this.getRelation().equals("=")) {
			//System.err.println("=");
			return re.getID().equals(this.getID());
		}
		if (!re.getName().equals(this.getName())) {
			//System.err.println("Name match failed");
			return false;
		}
		int c = RuntimeEnvironment.compareVersions(re.getVersion(),this.getVersion());
		//System.err.println("c="+c);
		if (this.getRelation().equals(">")) return c>0;
		if (this.getRelation().equals(">=")) return c>=0;
		if (this.getRelation().equals("<=")) return c<=0;
		if (this.getRelation().equals("<")) return c<0;
		throw new RuntimeException("Unknown/untreated releation '"+this.getRelation()+"'");
	}
	
	/**
	 * Iterates over all the RuntimeEnvironments passed as argument.
	 * @param REs - list of RuntimeEnvironments, mostly those offered at a particular queue
	 * @return true iff any RE among the REs passed as argument are fulfilling the constraint
	 */
	public boolean isFulfilledByAtLeastOneIn(Collection<RuntimeEnvironment> REs) {
		boolean fulfilled = false;
		Iterator<RuntimeEnvironment> i = REs.iterator();
		while(i.hasNext() && !fulfilled) {
			RuntimeEnvironment r = i.next();
			fulfilled = this.isFulfilledBy(r);
		}
		return fulfilled;
	}
	/**
	 * For testing purposes
	 */
	public static void main(String argv[]) {
		try {
			if (argv[0].equals("--help") || argv.length != 3) {
				System.err.println("Expecting arguments (<|>|=|<=|>=) runtime1-version runtime2-version");
			}
			else {
				RuntimeEnvironmentConstraint r1 = new RuntimeEnvironmentConstraint(argv[1], argv[0]);
				RuntimeEnvironment r2 = new RuntimeEnvironment(argv[2]);
				System.out.println("r1.getName(): "+r1.getName());
				System.out.println("r1.getVersion(): "+r1.getVersion());
				System.out.println("r1.getRelation(): "+r1.getRelation());
				System.out.println("r2.getName(): "+r2.getName());
				System.out.println("r2.getVersion(): "+r2.getVersion());
				System.out.println("r1.isFulfilledBy(r2): "+String.valueOf(r1.isFulfilledBy(r2)));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
