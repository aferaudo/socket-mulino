package principale;


public class Main {

	public static void main(String[] args) {
		StringBuffer result = new StringBuffer();
		result.append("7 " + "a7"+ "--------" + "d7"+ "--------" + "g7" + "\n");
		result.append("6 |--" + "b6" + "-----" + "d6" + "-----" + "f6" + "--|\n");
		result.append("5 |--|--" + "c5" + "--" + "d5" + "--" + "e5" + "--|--|\n");
		result.append("4 " + "a4" + "--" + "b4" + "--" + "c4" + "     "
				+ "e4" + "--" + "f4" + "--" + "g4" + "\n");
		result.append("3 |--|--" + "c3" + "--" + "d3" + "--" + "e3" + "--|--|\n");
		result.append("2 |--" + "b2" + "-----" + "d2" + "-----" + "f2" + "--|\n");
		result.append("1 " + "a1" + "--------" + "d1" + "--------" + "g1" + "\n");
		result.append("  a  b  c  d  e  f  g\n");
		
		System.out.println(result);
		String prova = "(-1, 10, 11)   ";
		System.out.println(JavaInterfaceServer.toSend(prova));
	}
}
