public class NewType {
	public static Type cint (int integer) {
		Type cint = new Type();
		cint.ident = 0;
		cint.constructorType = 0;
		cint.values = new Object[] {(Object) new Integer(integer)};
		return cint;
	}

	public static Type cbool (int bool) {
		Type cbool = new Type();
		cbool.ident = 1;
		cbool.constructorType = bool;
		cbool.values = new Object[] {(Object) new Integer(bool)};
		return cbool;
	}
}
