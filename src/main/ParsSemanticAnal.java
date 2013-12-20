package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static main.LexicalUnit.*;

public class ParsSemanticAnal {

	private Scanner cobolScanner;
	private Symbol<String> currentToken;
	private boolean pID = false;
	private String programID = null;
	private Symbol<String> previousToken;
	private List<Symbol<String>> usedLabels;
	private Map<String,Symbol<?>> tableOfSymbols;
	private Image currentimage = null;

	public ParsSemanticAnal(){
		this.usedLabels = new ArrayList<Symbol<String>>();
		//this.cobolScanner = new Scanner(System.in);
		try {
			this.cobolScanner = new Scanner(new FileInputStream(new File("entree.txt")));
			this.tableOfSymbols = cobolScanner.getTableOfSymbols();
			currentToken = this.cobolScanner.next_token();
			PROGRAM();

			// Check if the labels used are defined.

			for (Symbol<String> s : this.usedLabels){
				if (!tableOfSymbols.containsKey(s.getValue()))
					throw new Exception("LINE: " + s.get(Symbol.LINE) + "\nUse of undefined label: " + s.getValue() + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			//System.out.println("PROBLEME: " + e.getMessage());
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

	public Scanner getScanner(){
		return this.cobolScanner;
	}

	private void PROGRAM() throws Exception {
		IDENT(); ENV(); DATA(); PROC(); match(EOF);
	}

	private void DATA() throws Exception {
		match(DATA);
		match(DIVISION);
		END_INST();
		match(WORKING_STORAGE);
		match(SECTION);
		END_INST();
		VAR_LIST();
	}

	private void VAR_LIST() throws Exception {
		switch(currentToken.unit){
		case INTEGER : 
			VAR_DECL();
			VAR_LIST();
			break;
		case PROCEDURE : 
			break;
		default: syntax_error("Missing: INTEGER or PROCEDURE"); break;
		}
	}

	private void VAR_DECL() throws Exception {
		LEVEL();
		match(IDENTIFIER);
		match(IMAGE);
		VD_FACT();
	}

	private void VD_FACT() throws Exception {
		switch(currentToken.unit){
		case END_OF_INSTRUCTION : 
			END_INST();
			break;
		case VALUE :
			match(VALUE);
			VD_VALUE();
			END_INST();
			break;
		default: syntax_error("Missing: END_OF_INSTRUCTION or VALUE"); break;
		}
	}

	private void LEVEL() throws Exception {
		match(INTEGER);
	}

	private void ENV() throws Exception {
		match(ENVIRONMENT);
		match(DIVISION);
		END_INST();
		match(CONFIGURATION);
		match(SECTION);
		END_INST();
		match(SOURCE_COMPUTER);
		match(DOT);
		WORDS();
		END_INST();
		match(OBJECT_COMPUTER);
		match(DOT);
		WORDS();
		END_INST();
	}

	private void IDENT() throws Exception {
		match(IDENTIFICATION);
		match(DIVISION);
		END_INST();
		match(PROGRAM_ID);
		match(DOT);
		match(IDENTIFIER);
		// Save program id:
		this.programID = previousToken.getValue();
		END_INST();
		match(AUTHOR);
		match(DOT);
		WORDS();
		END_INST();
		match(DATE_WRITTEN);
		match(DOT);
		WORDS();
		END_INST();

	}

	private void WORDS() throws Exception { 
		WORD();
		WORDS_REC();	
	}

	private void WORDS_REC() throws Exception {
		switch(currentToken.unit){
		case IDENTIFIER :
		case INTEGER :
			WORD();
			WORDS_REC();
			break;
		case END_OF_INSTRUCTION : 
			break;
		default: syntax_error("Missing: IDENTIFIER or END_OF_INSTRUCTION"); break;
		}
	}

	private void END_INST() throws Exception {
		match(END_OF_INSTRUCTION);	
	}

	private void PROC() throws Exception {
		match(PROCEDURE);
		match(DIVISION);
		END_INST();
		match(IDENTIFIER);
		match(SECTION);
		END_INST();
		LABELS();
		match(END);
		match(PROGRAM);
		match(IDENTIFIER);
		// Check with the saved id if the same:
		if (!this.programID.equals(previousToken.getValue()))	// Si �a ne correspond pas.
			this.semantic_error("ProgramID do not match: " + this.programID + " is not " + previousToken.getValue());
		match(DOT);
	}

	private void LABELS() throws Exception {
		LABEL();
		END_INST();
		INSTRUCTION_LIST();
		LABELS_REC();
	}

	private void LABELS_REC() throws Exception {
		switch(currentToken.unit){
		case IDENTIFIER : 
			LABEL();
			END_INST();
			INSTRUCTION_LIST();
			LABELS_REC();
			break;
		case END : 
			break;
		default: syntax_error("Missing: IDENTIFIER or END"); break;
		}
	}

	private void INSTRUCTION_LIST() throws Exception {
		switch(currentToken.unit){
		case MOVE :
		case COMPUTE :
		case ADD :
		case SUBTRACT :
		case MULTIPLY:
		case DIVIDE:
		case PERFORM:
		case ACCEPT:
		case DISPLAY:
		case STOP:
		case IF:
			INSTRUCTION();
			INSTRUCTION_LIST();
			break;
		case IDENTIFIER :
		case END:
		case ELSE:
		case END_IF:
			break;
		default: syntax_error("Missing: STOP or IF or MOVE or COMPUTE or ADD or MULTIPLY or DIVIDE or PERFORM or ACCEPT or DISPLAY or SUBTRACT or END or IDENTIFIER or ELSE or END_IF"); break;
		}
	}

	private void INSTRUCTION() throws Exception {
		switch(currentToken.unit){
		case MOVE :
		case COMPUTE:
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case DIVIDE:
			ASSIGNATION();
			break;
		case IF : 
			IF();
			break;
		case PERFORM : 
			CALL();
			break;
		case ACCEPT : 
			READ();
			break;
		case DISPLAY : 
			WRITE();
			break;
		case STOP : 
			match(STOP);
			match(RUN);
			END_INST();
			break;
		default: syntax_error("Missing: MOVE or COMPUTE or ADD or SUBTRACT or MULTIPLY or DIVIDE or IF or PERFORM or ACCEPT or DISPLAY or STOP"); break;
		}
	}

	private void WRITE() throws Exception {
		match(DISPLAY);
		WRITE_FACT();
	}

	private void WRITE_FACT() throws Exception {
		switch(currentToken.unit){
		case IDENTIFIER : 
		case INTEGER:
		case NOT:
		case MINUS_SIGN:
		case LEFT_PARENTHESIS:
		case TRUE:
		case FALSE:
			EXPRESSION();
			END_INST();
			break;
		case STRING:
			match(STRING);
			END_INST();
			break;
		default: syntax_error("Missing: IDENTIFIER or INTEGER or NOT or MINUS_SIGN or LEFT_PARENTHESIS or STRING or TRUE or FALSE"); break;
		}
	}

	private void READ() throws Exception {
		match(ACCEPT);
		match(IDENTIFIER);
		// Check if declared variable.
		if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
			this.semantic_error("Use of undefined variable: " + previousToken.getValue());
		END_INST();
	}

	private void CALL() throws Exception {
		match(PERFORM);
		match(IDENTIFIER);
		// Save the label id for further check.
		if (!this.usedLabels.contains(previousToken))
			this.usedLabels.add(previousToken);
		CALL_FACT();
	}

	private void CALL_FACT() throws Exception {
		switch(currentToken.unit){
		case UNTIL : 
			match(UNTIL);
			LexicalUnit l = EXPRESSION(); checkLogicalExpression(l);
			END_INST();
			break;
		case END_OF_INSTRUCTION:
			END_INST();
			break;
		default: syntax_error("Missing: UNTIL or END_OF_INSTRUCTION"); break;
		}
	}

	private void IF() throws Exception {
		switch(currentToken.unit){
		case IF : 
			match(IF);
			LexicalUnit l = EXPRESSION(); checkLogicalExpression(l);
			match(THEN);
			INSTRUCTION_LIST();
			IF_END();
			break;
		default: syntax_error("Missing: IF"); break;
		}
	}
	
	private void checkLogicalExpression(LexicalUnit l) throws Exception{
		if(l != INTEGER)
			throw new Exception("Expected logical expression after if. Learn to code.");
	}

	private void IF_END() throws Exception {
		switch(currentToken.unit){
		case ELSE : 
			match(ELSE);
			INSTRUCTION_LIST();
			match(END_IF);
			break;
		case END_IF:
			match(END_IF);
			break;
		default: syntax_error("Missing: ELSE or END_IF"); break;
		}
	}

	private void ASSIGNATION() throws Exception {
		LexicalUnit l = null;
		Symbol<?> s = null;
		switch(currentToken.unit){
		case MOVE : 
			match(MOVE);
			l = EXPRESSION();
			match(TO);
			match(IDENTIFIER);
			// Check if declared variable.
			if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			s = this.previousToken;
			checkAssignationCompatibility(s, l);
			END_INST();
			break;
		case COMPUTE :
			match(COMPUTE);
			match(IDENTIFIER);
			// Check if declared variable.
			if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			s = this.previousToken;
			match(EQUALS_SIGN);
			l = EXPRESSION();
			checkAssignationCompatibility(s, l);
			END_INST();
			break;
		case ADD:
			match(ADD);
			l = EXPRESSION();
			match(TO);
			match(IDENTIFIER);
			// Check if declared variable.
			if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			s = this.previousToken;
			checkAssignationCompatibility(s, l);
			END_INST();
			break;
		case SUBTRACT:
			match(SUBTRACT);
			l = EXPRESSION();
			match(FROM);
			match(IDENTIFIER);
			// Check if declared variable.
			if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			s = this.previousToken;
			checkAssignationCompatibility(s, l);
			END_INST();
			break;
		case MULTIPLY:
			match(MULTIPLY);
			ASSING_END();
			END_INST();
			break;
		case DIVIDE:
			match(DIVIDE);
			ASSING_END();
			END_INST();	
		default: syntax_error("Missing: MOVE or COMPUTE or ADD or SUBTRACT or MULTIPLY or DIVIDE"); break;
		}
	}

	private void ASSING_END() throws Exception {
		EXPRESSION();
		match(COMMA);
		EXPRESSION();
		match(GIVING);
		match(IDENTIFIER);
		// Check if declared variable.
		if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
			this.semantic_error("Use of undefined variable: " + previousToken.getValue());
	}

	private LexicalUnit EXPRESSION() throws Exception {
		LexicalUnit l = CONDITION();
		l = EXPRESSION_REC(l);
		return l;
	}

	private LexicalUnit EXPRESSION_REC(LexicalUnit l) throws Exception {
		switch(currentToken.unit){
		case OR :
			match(OR);
			LexicalUnit lu1 = CONDITION();
			l = EXPRESSION_REC(resultType(l, Operator.OR, lu1));
			break;
		case END_OF_INSTRUCTION : 
		case TO:
		case GIVING:
		case COMMA:
		case RIGHT_PARENTHESIS:
		case THEN:
		case FROM:
			break;
		default: syntax_error("Missing: OR or END_OF_INSTRUCTION or TO or GIVING or COMMA or RIGHT_PARENTHESIS or THEN or FROM"); break;
		}
		return l;
	}

	private LexicalUnit CONDITION() throws Exception {
		LexicalUnit l = SUBCONDITION();
		l = CONDITION_REC(l);
		return l;
	}

	private LexicalUnit CONDITION_REC(LexicalUnit l) throws Exception {
		switch(currentToken.unit){
		case AND : 
			match(AND);
			LexicalUnit lu1 = SUBCONDITION();
			l = CONDITION_REC(resultType(l, Operator.AND, lu1));
			break;
		case END_OF_INSTRUCTION:
		case TO:
		case GIVING:
		case COMMA:
		case OR:
		case RIGHT_PARENTHESIS:
		case THEN:
		case FROM:
			break;
		default: syntax_error("Missing: AND or END_OF_INSTRUCTION or TO or GIVING or COMMA or OR or RIGHT_PARENTHESIS or THEN or FROM"); break;
		}
		return l;
	}

	private LexicalUnit SUBCONDITION() throws Exception {
		LexicalUnit l = VALUE();
		l = SUBCON_FACT(l);
		return l;
	}

	private LexicalUnit SUBCON_FACT(LexicalUnit l) throws Exception {
		switch(currentToken.unit){
		case LOWER_THAN : 
		case GREATER_THAN:
		case LOWER_OR_EQUALS:
		case GREATER_OR_EQUALS:
		case EQUALS_SIGN:
			Operator o = COMP_OP();
			LexicalUnit lu1 = VALUE();
			l = resultType(l, o, lu1);
			break;
		case END_OF_INSTRUCTION:
		case TO:
		case GIVING:
		case COMMA:
		case OR:
		case AND:
		case RIGHT_PARENTHESIS:
		case THEN:
		case FROM:
			break;
		default: syntax_error("Missing: LOWER_THAN or GREATER_THAN or LOWER_OR_EQUALS or GREATER_OR_EQUALS or EQUALS_SIGN or END_OF_INSTRUCTION or TO or GIVING or COMMA or OR or AND or RIGHT_PARENTHESIS or THEN or FROM"); break;
		}
		return l;
	}

	private Operator COMP_OP() throws Exception {
		Operator o = null;
		switch(currentToken.unit){
		case GREATER_THAN :
			match(GREATER_THAN);
			o = Operator.GREATER_THAN;
			break;
		case LOWER_THAN : 
			match(LOWER_THAN);
			o = Operator.LOWER_THAN;
			break;
		case LOWER_OR_EQUALS:
			match(LOWER_OR_EQUALS);
			o = Operator.LOWER_OR_EQUALS;
			break;
		case GREATER_OR_EQUALS:
			match(GREATER_OR_EQUALS);
			o = Operator.GREATER_OR_EQUALS;
			break;
		case EQUALS_SIGN:
			match(EQUALS_SIGN);
			o = Operator.EQUALS;
			break;
		default: syntax_error("Missing: GREATER_THAN or LOWER_THAN or LOWER_OR_EQUALS or GREATER_OR_EQUALS or EQUALS_SIGN"); break;
		}
		return o;
	}

	private LexicalUnit VALUE() throws Exception {
		LexicalUnit l = TERM();
		l = VALUE_REC(l);
		return l;
	}

	private LexicalUnit TERM() throws Exception {
		LexicalUnit l = FACTOR();
		l = TERM_REC(l);
		return l;
	}

	private LexicalUnit TERM_REC(LexicalUnit l) throws Exception {
		switch(currentToken.unit){
		case ASTERISK :
		case SLASH:
			Operator o = MUL_DIV();
			LexicalUnit lu1 = FACTOR();
			l = TERM_REC(resultType(lu1, o, l));
			break;
		case END_OF_INSTRUCTION:
		case TO:
		case GIVING:
		case COMMA:
		case OR:
		case AND:
		case RIGHT_PARENTHESIS:
		case MINUS_SIGN:
		case PLUS_SIGN:
		case LOWER_THAN : 
		case GREATER_THAN:
		case LOWER_OR_EQUALS:
		case GREATER_OR_EQUALS:
		case EQUALS_SIGN:
		case THEN:
		case FROM:
			break;
		default: syntax_error("Missing: ASTERISK or SLASH or END_OF_INSTRUCTION or TO or GIVING or COMMA or OR or AND or RIGHT_PARENTHESIS or MINUS_SIGN or PLUS_SIGN or LOWER_THAN or GREATER_THAN or LOWER_OR_EQUALS or GREATER_OR_EQUALS or EQUALS_SIGN or THEN or FROM"); break;
		}
		return l;
	}

	private Operator MUL_DIV() throws Exception {
		Operator o = null;
		switch(currentToken.unit){
		case ASTERISK : 
			match(ASTERISK);
			o = Operator.MULTIPLY;
			break;
		case SLASH : 
			match(SLASH);
			o = Operator.DIVIDE;
			break;
		default: syntax_error("Missing: ASTERISK or SLASH"); break;
		}
		return o;
	}

	private LexicalUnit FACTOR() throws Exception {
		LexicalUnit l = null;
		switch(currentToken.unit){
		case NOT : 
			match(NOT);
			l = NUMBER();
			l = resultType(l, Operator.NOT, l);
			break;
		case MINUS_SIGN : 
			match(MINUS_SIGN);
			l = NUMBER();
			l = resultType(l, Operator.UN_MINUS, l);
			break;
		case IDENTIFIER:
		case INTEGER:
		case LEFT_PARENTHESIS:
		case TRUE:
		case FALSE:
			l = NUMBER();
			break;			
		default: syntax_error("Missing: NOT or MINUS_SIGN or IDENTIFIER or INTEGER or LEFT_PARENTHESIS or TRUE or FALSE"); break;
		}
		return l;
	}

	private LexicalUnit NUMBER() throws Exception {
		LexicalUnit l = null;

		switch(currentToken.unit){
		case LEFT_PARENTHESIS:
			match(LEFT_PARENTHESIS);
			l = EXPRESSION();
			match(RIGHT_PARENTHESIS);
			break;
		case IDENTIFIER:
			match(IDENTIFIER);
			// Check if declared variable.
			if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			l = previousToken.unit;
			break;
		case INTEGER:
			match(INTEGER);
			l = INTEGER;
			break;
		case TRUE:
			match(TRUE);
			l = INTEGER;
			break;
		case FALSE:
			match(FALSE);
			l = INTEGER;
			break;
		default: syntax_error("Missing: LEFT_PARENTHESIS or IDENTIFIER or INTEGER or TRUE or FALSE"); break;
		}
		return l;
	}

	private LexicalUnit VALUE_REC(LexicalUnit l) throws Exception {
		
		switch(currentToken.unit){
		case PLUS_SIGN :
		case MINUS_SIGN:
			Operator o = PLUS_MINUS();
			LexicalUnit lu1 = TERM();
			l = VALUE_REC(resultType(l, o, lu1));
			break;
		case END_OF_INSTRUCTION:
		case TO:
		case GIVING:
		case COMMA:
		case OR:
		case AND:
		case RIGHT_PARENTHESIS:
		case LOWER_THAN : 
		case GREATER_THAN:
		case LOWER_OR_EQUALS:
		case GREATER_OR_EQUALS:
		case EQUALS_SIGN:
		case THEN:
		case FROM:
			break;
		default: syntax_error("Missing: PLUS_SIGN or MINUS_SIGN or END_OF_INSTRUCTION or TO or GIVING or COMMA or OR or AND or RIGHT_PARENTHESIS or LOWER_THAN or GREATER_THAN or LOWER_OR_EQUALS or GREATER_OR_EQUALS or EQUALS_SIGN or THEN or FROM"); break;
		}
		return l;
	}

	private Operator PLUS_MINUS() throws Exception {
		Operator o = null;
		switch(currentToken.unit){
		case MINUS_SIGN :
			match(MINUS_SIGN);
			o = Operator.MINUS;
			break;
		case PLUS_SIGN :
			match(PLUS_SIGN);
			o = Operator.PLUS;
			break;
		default: syntax_error("Missing: MINUS_SIGN or PLUS_SIGN"); break;
		}
		return o;
	}

	private void LABEL() throws Exception {
		match(IDENTIFIER);
	}

	private void WORD() throws Exception {
		switch(currentToken.unit){
		case IDENTIFIER : 
			match(IDENTIFIER);
			break;
		case INTEGER : 
			match(INTEGER);
			break;
		default: syntax_error("Missing: IDENTIFIER or INTEGER"); break;
		}
	}

	private void match(LexicalUnit lexicalUnit) throws Exception{
		if(currentToken.unit != lexicalUnit){
			syntax_error("missing:" +  lexicalUnit);	// Si ce n'est pas le type qu'on attendait...
		}
		else{

			System.out.println(lexicalUnit + ": " + currentToken.getValue());
			previousToken = currentToken;
			currentToken = cobolScanner.next_token();
		}
	}

	private void syntax_error(String message) throws Exception {
		throw new Exception("LINE:" + currentToken.get(Symbol.LINE) + "\n" + message + "\n" + "before: " + currentToken.getValue() + "\n");
	}

	private void semantic_error(String message) throws Exception {
		throw new Exception("LINE:" + previousToken.get(Symbol.LINE) + "\n" + message + "\n" + "before: " + currentToken.getValue() + "\n");
	}

	private LexicalUnit resultType(LexicalUnit lu1, Operator op, LexicalUnit lu2) throws Exception {
		LexicalUnit l = LexicalUnit.resultType(lu1, op, lu2);
		
		if (l == null)	// null means no existing compatibility.
			throw new Exception("Type incompatibility on line " + previousToken.get(Symbol.LINE) + ": " + lu1 + " and " + lu2 + " are not compatible." +
					"Learn to code.");
		
		return l;
	}
	
	private void checkAssignationCompatibility(Symbol<?> rec, LexicalUnit exp) throws Exception{
		// Check basic compatibility:
		LexicalUnit.checkAssignationCompatibility(rec.unit, exp);
		
		// Check images:
	}
}
