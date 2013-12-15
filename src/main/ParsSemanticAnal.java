package main;
import java.io.IOException;

import static main.LexicalUnit.*;

public class ParsSemanticAnal {

	private Scanner cobolScanner;
	private Symbol currentToken;
	
	public ParsSemanticAnal(){
		this.cobolScanner = new Scanner(System.in);
		try {
			currentToken = this.cobolScanner.next_token();
			this.PROGRAM();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void PROGRAM() throws Exception {
		IDENT(); ENV(); DATA(); PROC();
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

	private void VAR_LIST() {
		// TODO Auto-generated method stub
		
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
		match(IDENTIFIER);
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
		match(IDENTIFIER);
		WORDS_REC();	
	}

	private void WORDS_REC() throws Exception {
		switch(currentToken.unit){
		case IDENTIFIER : 
			match(IDENTIFIER);
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

	private void syntax_error(String message) throws Exception {
		throw new Exception("LINE:" + currentToken.get(Symbol.LINE) + "\n" + message + "\n");
	}

	private void PROC() {
		// TODO Auto-generated method stub
		
	}
	
	private void match(LexicalUnit lexicalUnit) throws Exception{
		if(currentToken.unit != lexicalUnit){
			syntax_error("missing:" +  lexicalUnit);
		}
		else
			currentToken = cobolScanner.next_token();
	}
	
}
