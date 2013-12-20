package main;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

/**
This class show you how to use the Lexical Analyzer.
For this second part, I choosed to accept a file that does not respect completly
the given grammar. The modification is the following:
	A Cobol file has to end with a END_OF_INSTRUCTION <=> .\n
	and in this case, I also accept a simple dot as a END_OF_INSTRUCTION
*/
public class Main{
	public static void main(String args[])throws Exception{
		// Apply the lexical analyzer
//		Scanner cobolScanner = new Scanner(System.in);
		
		Parser parsSemanticAnal = new Parser();
		
//		Symbol lexicalUnit;
//		do{
//			lexicalUnit = cobolScanner.next_token();
//			if(lexicalUnit != null){
//				System.out.println("token: "+lexicalUnit.getValue()+" \tlexical unit: "+lexicalUnit.unit.toString());
//			}
//		}while(lexicalUnit == null || !lexicalUnit.unit.equals(LexicalUnit.EOF));
//		
		// Printing the table
		final Map<String,Symbol<?>> tableOfSymbols = parsSemanticAnal.getScanner().getTableOfSymbols();
		List<Symbol<String>> variables = new ArrayList<Symbol<String>>();
		List<Symbol<String>> labels	 = new ArrayList<Symbol<String>>();
		// we distinguish vars/labels
		for(String identifier:tableOfSymbols.keySet()){
			try{
				final Symbol<String> symbol = (Symbol<String>)tableOfSymbols.get(identifier);
				if(symbol != null){
					if(symbol.containsKey(Symbol.IMAGE)) variables.add(symbol);
					else labels.add(symbol);
				}
			}catch(ClassCastException cce){ cce.printStackTrace(); /* we just ignore remaining information from the table*/}
		}
		// we sort vars/labels
		final Comparator<Symbol<String>> sorter = new Comparator<Symbol<String>>(){
			public int compare(Symbol<String> identifier1, Symbol<String> identifier2){
				return identifier1.getValue().compareTo(identifier2.getValue());
			}
		};
		Collections.sort(variables,sorter);
		Collections.sort(labels,sorter);
		// print
		System.out.println("variables");
		for(Symbol<String> identifier:variables)
			System.out.println(identifier.getValue()+"\t"+identifier.get(Symbol.IMAGE));
		System.out.println("labels");
		for(Symbol<String> identifier:labels)
			System.out.println(identifier.getValue()+"\t"+identifier.get(Symbol.LINE));
			
	}
}
