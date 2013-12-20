package main;
import static main.Operator.*;

public enum LexicalUnit{
	IDENTIFICATION(1),
	DIVISION(2),
	PROGRAM_ID(3),
	AUTHOR(4),
	DOT(5),
	END_OF_INSTRUCTION(6),
	DATE_WRITTEN(7),
	ENVIRONMENT(8),
	CONFIGURATION(9),
	SECTION(10),
	SOURCE_COMPUTER(11),
	OBJECT_COMPUTER(12),
	DATA(13),
	WORKING_STORAGE(14),
	VALUE(15),
	PROCEDURE(16),
	END(17),
	PROGRAM(18),
	STOP(19),
	RUN(20),
	MOVE(21),
	TO(22),
	FROM(23),
	BY(24),
	COMPUTE(25),
	ADD(26),
	SUBTRACT(27),
	MULTIPLY(28),
	DIVIDE(29),
	GIVING(30),
	NOT(31),
	TRUE(32),
	FALSE(33),
	AND(34),
	OR(35),
	IF(36),
	ELSE(37),
	END_IF(38),
	PERFORM(39),
	UNTIL(40),
	ACCEPT(41),
	DISPLAY(42),
	COMMA(43),
	PIC(44),
	LEFT_PARENTHESIS(45),
	RIGHT_PARENTHESIS(46),
	MINUS_SIGN(47),
	PLUS_SIGN(48),
	EQUALS_SIGN(49),
	ASTERISK(50),
	SLASH(51),
	LOWER_THAN(52),
	GREATER_THAN(53),
	LOWER_OR_EQUALS(54),
	GREATER_OR_EQUALS(55),
	REAL(56),
	INTEGER(57),
	STRING(58),
	IMAGE(59),
	IDENTIFIER(60),
	EOF(61),
	THEN(62)
	;

	public final int SYMBOL_ID;
	private static LexicalUnit[][][] types;
	private static int[][] assComTab;
	static final int NC = 0;	// Not compatible
	static final int SC = 1;	// Semi-compatible, cast may be needed
	static final int C = 2;		// Fully compatible

	private LexicalUnit(final int uniqueIdentifier){
		SYMBOL_ID = uniqueIdentifier;
	}

	public static LexicalUnit convert(String rawText){
		LexicalUnit result = null;
		try{
			result = LexicalUnit.valueOf(rawText.toUpperCase().replaceAll("-","_"));
		}catch(IllegalArgumentException iae){
			/* nothing to do, let "result" initilized as a null object */
		}
		if(result != null) return result;
		if(rawText.isEmpty()) return null;
		if(rawText.length()<2){//1
			switch(rawText.charAt(0)){
			case '.': return LexicalUnit.DOT;
			case ',': return LexicalUnit.COMMA;
			case '(': return LexicalUnit.LEFT_PARENTHESIS;
			case ')': return RIGHT_PARENTHESIS;
			case '-': return LexicalUnit.MINUS_SIGN;
			case '+': return LexicalUnit.PLUS_SIGN;
			case '=': return LexicalUnit.EQUALS_SIGN;
			case '*': return LexicalUnit.ASTERISK;
			case '/': return LexicalUnit.SLASH;
			case '<': return LexicalUnit.LOWER_THAN;
			case '>': return LexicalUnit.GREATER_THAN;
			}
		}else if(rawText.length()<3 && rawText.charAt(1)=='='){
			switch(rawText.charAt(0)){
			case '<': return LexicalUnit.LOWER_OR_EQUALS;
			case '>': return LexicalUnit.GREATER_OR_EQUALS;
			}
		}
		return null;
	}
	public static LexicalUnit convert(final int uniqueIdentifier){
		for(LexicalUnit unit:LexicalUnit.values())
			if(unit.SYMBOL_ID == uniqueIdentifier)
				return unit;
		return null;
	}

	private static void setResultingType(LexicalUnit lu1, Operator op, LexicalUnit lu2, LexicalUnit res){
		if (lu1.SYMBOL_ID > lu2.SYMBOL_ID)
			types[lu2.SYMBOL_ID-1][lu1.SYMBOL_ID-1][op.ordinal()] = res;	// So we only need to fill the tab for lu1-lu2 and not also for lu2-lu1 (same thing).

		else
			types[lu1.SYMBOL_ID-1][lu2.SYMBOL_ID-1][op.ordinal()] = res;
	}

	/**
	 * Returns the type of the combination of the 2 operands with the given operator.
	 * If only one operand for the operator, repeat it in the second field.
	 * We chose to implement it with an array in java, instead of a HashMap. The amount of memory used may be higher, but we thought the speed would increase.
	 * We suppose the operators are commutative for the type.
	 * 
	 * @param lu1 operand 1 type
	 * @param op Operator, @see Operator
	 * @param lu2 operand 2 type
	 * @return The resulting type.
	 * @throws Exception
	 */
	public static LexicalUnit resultType(LexicalUnit lu1, Operator op, LexicalUnit lu2) throws Exception {

		if (types == null){
			int length = values().length;
			Operator[] opTab = Operator.values();
			types = new LexicalUnit[length][length][opTab.length];
			
			// INTEGER (the combinations giving INTEGERs)
			for (int i = 0; i < opTab.length; ++i){	// Define the resulting type of operations.
				setResultingType(INTEGER, opTab[i], INTEGER, INTEGER);
				if (i <= 7)
					setResultingType(REAL, opTab[i], REAL, INTEGER);
				if (i >= 1 && i <= 7)
					setResultingType(REAL, opTab[i], INTEGER, INTEGER);
			}
			
			// REAL
			for (int i = 8; i < opTab.length; ++i){
				if (i >= 9)
					setResultingType(REAL, opTab[i], INTEGER, REAL);
				setResultingType(REAL, opTab[i], REAL, REAL);
			}
		}
		
		LexicalUnit lu = null;

		if (lu1 != null && lu2 != null){

			if(lu1.SYMBOL_ID > lu2.SYMBOL_ID)
				lu = types[lu2.SYMBOL_ID-1][lu1.SYMBOL_ID-1][op.ordinal()];
			else
				lu = types[lu1.SYMBOL_ID-1][lu2.SYMBOL_ID-1][op.ordinal()];
		}
		else if (lu1 != null)
			lu = lu1;
		else if (lu2 != null)
			lu = lu2;
		
		return lu;
	}

	/**
	 * Checks the variable type with the assigned expression type.
	 * @param rec
	 * @param exp
	 * @return The level of compatibility.
	 */
	public static int checkAssignationCompatibility(LexicalUnit rec, LexicalUnit exp) {
		if (assComTab == null){
			int length = values().length;
			assComTab = new int[length][length];
			
			
			assComTab[REAL.SYMBOL_ID-1][INTEGER.SYMBOL_ID-1] = SC;	// This is because of the images.
			assComTab[INTEGER.SYMBOL_ID-1][REAL.SYMBOL_ID-1] = SC;	// "
			assComTab[INTEGER.SYMBOL_ID-1][INTEGER.SYMBOL_ID-1] = SC;	// "
			assComTab[REAL.SYMBOL_ID-1][REAL.SYMBOL_ID-1] = SC;	// "
		}
		System.out.println("---------- The type: " + rec);
		
		return (exp != null) ? assComTab[rec.SYMBOL_ID-1][exp.SYMBOL_ID-1] : C;
	}

}
