public class NewType {
	public Type cint (int integer) {
		Type cint = new Type();
		cint.ident = 0;
		cint.constructorType = 0;
		cint.values = new Object[] {(Object) new Integer(integer)};
		return cint;
	}

	public Type cbool (boolean bool) {
		Type cbool = new Type();
		cbool.ident = 1;
		cbool.constructorType = bool ? 1 : 0;
		cbool.values = new Object[0];
		return cbool;
	}
}
