package org.oregami.entities.datalist;

import javax.persistence.Entity;


/**
 * see http://wiki.oregami.org/display/OR/Data+List+4+-+Un-release+States
 */
@Entity
public class UnreleaseState extends BaseList {

	private static final long serialVersionUID = -6903953989551086371L;
	
	public UnreleaseState(String value) {
		super(value);
	}
	
	public UnreleaseState() {
		super("");
	}
	
	public static final String IN_DEVELOPMENT = "IN_DEVELOPMENT";
	public static final String DEVELOPMENT_CANCELLED = "DEVELOPMENT_CANCELLED";
	public static final String VAPORWARE = "VAPORWARE";

}
