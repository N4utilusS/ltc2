package main;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static main.LexicalUnit.*;

public class Parser {

	private Scanner cobolScanner;
	private Symbol<String> currentToken;
	private boolean pID = false;
	private String programID = null;
	private Symbol<String> previousToken;
	private List<Symbol<String>> usedLabels;
	private Map<String,Symbol<?>> tableOfSymbols;
	private LLVM llvm;
	private boolean acceptEncountered = false;

	public Parser(){
		this.llvm = new LLVM("result.ll");
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
			
			// Write the read function for int if the accept is encountered.
			if (this.acceptEncountered)
				this.llvm.writeReadInt();
			
		} catch (IOException e) {
			e.printStackTrace();
			//System.out.println("PROBLEME: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		finally{
			llvm.close();
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
		// LLVM ----------
		llvm.writeMain();
		// ---------
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
		Type t = null;
		Symbol<String> s = null;

		LEVEL();
		match(IDENTIFIER); s = this.previousToken;
		match(IMAGE);
		// Use the image to give a type to the symbol:
		s.setTypeWithImage();
		// LLVM -------
		Type varT = (Type) s.get(Symbol.TYPE);
		llvm.varDecl(s.getValue(), varT.image);
		// --------
		t = VD_FACT();
		checkAssignationCompatibility(s, t);
	}

	private Type VD_FACT() throws Exception {
		Type t = null;
		switch(currentToken.unit){
		case END_OF_INSTRUCTION : 
			END_INST();
			break;
		case VALUE :
			match(VALUE);
			t = VD_VALUE();
			END_INST();
			break;
		default: syntax_error("Missing: END_OF_INSTRUCTION or VALUE"); break;
		}
		return t;
	}

	private Type VD_VALUE() throws Exception {
		Type t = new Type();
		switch(currentToken.unit){
		case INTEGER:
			match(INTEGER);
			t.l = INTEGER;
			t.updateImage(this.previousToken.getValue());
			break;
		case REAL:
			match(REAL);
			t.l = REAL;
			t.updateImage(this.previousToken.getValue());
			break;
		default: syntax_error("Missing: INTEGER or REAL"); break;
		}
		return t;
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
		// Check if declared variable. TODO Check for the type of the id in code gen ?
		if (!this.tableOfSymbols.containsKey(previousToken.getValue()))
			this.semantic_error("Use of undefined variable: " + previousToken.getValue());
		END_INST();
		// Set the acceptEncountered to true, so the read llvm function is written in the file.
		this.acceptEncountered = true;
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
			Type t = EXPRESSION(); checkLogicalExpression(t);
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
			Type t = EXPRESSION(); checkLogicalExpression(t);
			match(THEN);
			INSTRUCTION_LIST();
			IF_END();
			break;
		default: syntax_error("Missing: IF"); break;
		}
	}

	private void checkLogicalExpression(Type t) throws Exception{
		if(t.l != INTEGER && t.l != REAL)
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
		Type t = null;
		Symbol<?> s = null;
		switch(currentToken.unit){
		case MOVE : 
			match(MOVE);
			t = EXPRESSION();
			match(TO);
			match(IDENTIFIER);
			// Check if declared variable.
			if ((s = this.tableOfSymbols.get(previousToken.getValue())) == null)
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			checkAssignationCompatibility(s, t);
			END_INST();
			break;
		case COMPUTE :
			match(COMPUTE);
			match(IDENTIFIER);
			// Check if declared variable.
			if ((s = this.tableOfSymbols.get(previousToken.getValue())) == null)
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			match(EQUALS_SIGN);
			t = EXPRESSION();
			checkAssignationCompatibility(s, t);
			END_INST();
			break;
		case ADD:
			match(ADD);
			t = EXPRESSION();
			match(TO);
			match(IDENTIFIER);
			// Check if declared variable.
			if ((s = this.tableOfSymbols.get(previousToken.getValue())) == null)
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			t = resultType((Type) s.get(Symbol.TYPE), Operator.PLUS, t);
			checkAssignationCompatibility(s, t);
			END_INST();
			break;
		case SUBTRACT:
			match(SUBTRACT);
			t = EXPRESSION();
			match(FROM);
			match(IDENTIFIER);
			// Check if declared variable.
			if ((s = this.tableOfSymbols.get(previousToken.getValue())) == null)
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			t = resultType((Type) s.get(Symbol.TYPE), Operator.MINUS, t);
			checkAssignationCompatibility(s, t);
			END_INST();
			break;
		case MULTIPLY:
			match(MULTIPLY);
			ASSING_END(Operator.MULTIPLY);
			END_INST();
			break;
		case DIVIDE:
			match(DIVIDE);
			ASSING_END(Operator.DIVIDE);
			END_INST();
		default: syntax_error("Missing: MOVE or COMPUTE or ADD or SUBTRACT or MULTIPLY or DIVIDE"); break;
		}
	}

	private void ASSING_END(Operator o) throws Exception {
		Type t1, t2, t;
		Symbol<?> s;
		t1 = EXPRESSION();
		match(COMMA);
		t2 = EXPRESSION();
		match(GIVING);
		match(IDENTIFIER);
		// Check if declared variable.
		if ((s = this.tableOfSymbols.get(previousToken.getValue())) == null)
			this.semantic_error("Use of undefined variable: " + previousToken.getValue());
		t = resultType(t1, o, t2);
		checkAssignationCompatibility(s, t);
	}

	private Type EXPRESSION() throws Exception {
		Type t = CONDITION();
		t = EXPRESSION_REC(t);
		return t;
	}

	private Type EXPRESSION_REC(Type t) throws Exception {
		switch(currentToken.unit){
		case OR :
			match(OR);
			Type t1 = CONDITION();
			t = EXPRESSION_REC(resultType(t, Operator.OR, t1));
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
		return t;
	}

	private Type CONDITION() throws Exception {
		Type t = SUBCONDITION();
		t = CONDITION_REC(t);
		return t;
	}

	private Type CONDITION_REC(Type t) throws Exception {
		switch(currentToken.unit){
		case AND : 
			match(AND);
			Type t1 = SUBCONDITION();
			t = CONDITION_REC(resultType(t, Operator.AND, t1));
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
		return t;
	}

	private Type SUBCONDITION() throws Exception {
		Type t = VALUE();
		t = SUBCON_FACT(t);
		return t;
	}

	private Type SUBCON_FACT(Type t) throws Exception {
		switch(currentToken.unit){
		case LOWER_THAN : 
		case GREATER_THAN:
		case LOWER_OR_EQUALS:
		case GREATER_OR_EQUALS:
		case EQUALS_SIGN:
			Operator o = COMP_OP();
			Type t1 = VALUE();
			t = resultType(t, o, t1);
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
		return t;
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

	private Type VALUE() throws Exception {
		Type t = TERM();
		t = VALUE_REC(t);
		return t;
	}

	private Type TERM() throws Exception {
		Type t = FACTOR();
		t = TERM_REC(t);
		return t;
	}

	private Type TERM_REC(Type t) throws Exception {
		switch(currentToken.unit){
		case ASTERISK :
		case SLASH:
			Operator o = MUL_DIV();
			Type t1 = FACTOR();
			t = TERM_REC(resultType(t, o, t1));
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
		return t;
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

	private Type FACTOR() throws Exception {
		Type t = null;
		switch(currentToken.unit){
		case NOT : 
			match(NOT);
			t = NUMBER();
			t = resultType(t, Operator.NOT, t);
			break;
		case MINUS_SIGN : 
			match(MINUS_SIGN);
			t = NUMBER();
			t = resultType(t, Operator.UN_MINUS, t);
			break;
		case IDENTIFIER:
		case INTEGER:
		case LEFT_PARENTHESIS:
		case TRUE:
		case FALSE:
			t = NUMBER();
			break;			
		default: syntax_error("Missing: NOT or MINUS_SIGN or IDENTIFIER or INTEGER or LEFT_PARENTHESIS or TRUE or FALSE"); break;
		}
		return t;
	}

	private Type NUMBER() throws Exception {
		Type t = new Type();

		switch(currentToken.unit){
		case LEFT_PARENTHESIS:
			match(LEFT_PARENTHESIS);
			t = EXPRESSION();
			match(RIGHT_PARENTHESIS);
			break;
		case IDENTIFIER:
			match(IDENTIFIER);
			// Check if declared variable.
			Symbol<?> s;
			if ((s = this.tableOfSymbols.get(previousToken.getValue())) == null)
				this.semantic_error("Use of undefined variable: " + previousToken.getValue());
			t = (Type) s.get(Symbol.TYPE);
			break;
		case INTEGER:
			match(INTEGER);
			// Update the type:
			t.l = INTEGER;
			t.updateImage(this.previousToken.getValue());
			break;
		case REAL:
			match(REAL);
			// Update the type:
			t.l = REAL;
			t.updateImage(this.previousToken.getValue());
			break;
		case TRUE:
			match(TRUE);
			t.l = INTEGER;
			t.updateImage("1");
			break;
		case FALSE:
			match(FALSE);
			t.l = INTEGER;
			t.updateImage("0");
			break;
		default: syntax_error("Missing: LEFT_PARENTHESIS or IDENTIFIER or INTEGER or TRUE or FALSE"); break;
		}
		return t;
	}

	private Type VALUE_REC(Type t) throws Exception {

		switch(currentToken.unit){
		case PLUS_SIGN :
		case MINUS_SIGN:
			Operator o = PLUS_MINUS();
			Type t1 = TERM();
			t = VALUE_REC(resultType(t, o, t1));
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
		return t;
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

	private Type resultType(Type t1, Operator op, Type t2) throws Exception {
		LexicalUnit l = LexicalUnit.resultType(t1.l, op, t2.l);

		if (l == null)	// null means no existing compatibility.
			throw new Exception("Type incompatibility on line " + previousToken.get(Symbol.LINE) + ": " + t1.l + " and " + t2.l + " are not compatible." +
					" Learn to code.");
		
		Type t = new Type();
		t.l = l;
		
		switch (op){
		case NOT:
		case AND:
		case OR:
		case LOWER_THAN : 
		case GREATER_THAN:
		case LOWER_OR_EQUALS:
		case GREATER_OR_EQUALS:
		case EQUALS:
			t.image.digitBefore = 1;
			break;
		case PLUS:
			t.image.digitBefore = Math.max(t1.image.digitBefore, t2.image.digitBefore) + 1;
			t.image.digitAfter = Math.max(t1.image.digitAfter, t2.image.digitAfter);
			t.image.signed = t1.image.signed || t2.image.signed;
			break;
		case MINUS:
			t.image.digitBefore = Math.max(t1.image.digitBefore, t2.image.digitBefore) + ((t2.image.signed) ? 1 : 0);
			t.image.digitAfter = Math.max(t1.image.digitAfter, t2.image.digitAfter);
			t.image.signed = true;
			break;
		case MULTIPLY:
			t.image.digitBefore = t1.image.digitBefore + t2.image.digitBefore;
			t.image.digitAfter = t1.image.digitAfter + t2.image.digitAfter;
			t.image.signed = t1.image.signed || t2.image.signed;
			break;
		case DIVIDE:
			t.image.digitBefore = t1.image.digitBefore + t2.image.digitAfter;
			t.image.digitAfter = t2.image.digitBefore + t1.image.digitAfter;
			t.image.signed = t1.image.signed || t2.image.signed;
			break;
		case UN_MINUS:
			t.image.digitBefore = t1.image.digitBefore;
			t.image.digitAfter = t1.image.digitAfter;
			t.image.signed = true;
			break;
		}
		
		
		return t;
	}

	private void checkAssignationCompatibility(Symbol<?> rec, Type exp) throws Exception{
		
		if (exp == null) return;	// In case there is no expression.
		
		// Check basic compatibility:
		int compLevel = LexicalUnit.checkAssignationCompatibility(((Type) rec.get(Symbol.TYPE)).l, exp.l);

		if (compLevel == NC){
			throw new Exception("Type incompatibility for assignation on line " + previousToken.get(Symbol.LINE) + ": " + rec.getValue() + " and " + exp + " are not compatible." +
					" Learn to code.");
		}
		else if (compLevel == SC){	// Cast may be needed.
			// Check images:

			Type recType = (Type) rec.get(Symbol.TYPE);
			if (exp.image.signed && !recType.image.signed)
				System.out.println("Warning: On line: " + this.previousToken.get(Symbol.LINE) + " : The unsigned variable shouldn't be assigned to a signed expression.");

			if (exp.image.digitBefore > recType.image.digitBefore)
				System.out.println("Warning: On line: " + this.previousToken.get(Symbol.LINE) + " : The integer part of the expression may be truncated (smaller image).");

			if (exp.image.digitAfter > recType.image.digitAfter)
				System.out.println("Warning: On line: " + this.previousToken.get(Symbol.LINE) + " : The decimal part of the expression may be truncated (smaller image).");


		}	// No cast needed.

	}
}
