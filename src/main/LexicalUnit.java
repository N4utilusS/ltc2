package main;
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
	END_ID(38),
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
	EOF(61)
	;

	public final int SYMBOL_ID;
	
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
}
