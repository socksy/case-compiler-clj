public class Type {
	public int ident; //what type it is
	public int constructorType; //which data constructor is used
	public Object[] values;
	
	public Type () {
		super();
	}
	public Type (int ident, int constructorType, Object[] values) {
		this.ident = ident;
		this.constructorType = constructorType;
		this.values = values;
	}


	public String toString() {
		String tmp = "ident. " + ident + " \nconstr. "+constructorType + "\n[";
		for (Object o : values) {
			tmp += (o.toString());
			tmp += ",";
		}
		tmp += "]";
		return tmp;
	}

	public void printTypes() {
		for (Object o : values) {
			System.out.println(o.toString());
		}
	}
}
