package com.rework.joss.persistence.convention;



public class SqlArgTypeSetter {
	
	private final Object[] args;

	private final int[] argTypes;
	
	public SqlArgTypeSetter(){
		this.args = new Object[0];
		this.argTypes = new int[0];
	}
	/**
	 * Create a new SqlArgTypeSetter for the given arguments.
	 * @param args the arguments to set
	 * @param argTypes the corresponding SQL types of the arguments
	 */
	public SqlArgTypeSetter(Object[] args, Integer[] argTypes) {
		if ((args != null && argTypes == null) || (args == null && argTypes != null) ||
				(args != null && args.length != argTypes.length)) {
			throw new RuntimeException("args and argTypes parameters must match");
		}
		int[] tempArgTypes = new int[argTypes.length];
		for(int i = 0; i < argTypes.length; i++){
			tempArgTypes[i] = argTypes[i].intValue();
		}
		this.argTypes = tempArgTypes;
		this.args = args;
	}

	public Object[] getArgs() {
		return args;
	}

	public int[] getArgTypes() {
		return argTypes;
	}
}
