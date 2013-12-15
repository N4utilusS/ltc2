package main;

import java.util.HashMap;

public class Symbol<ValueType> extends HashMap<String,Object>{
	public static final String
		LINE		= "LINE",
		COLUMN	= "COLUMN",
		CONTENT	= "CONTENT",
		IMAGE		= "IMAGE",
		TYPE = "TYPE";
		
	private ValueType value;
	public final LexicalUnit unit;
	
	public Symbol(final LexicalUnit typeOfUnit){
		this.unit = typeOfUnit;
	}
	public ValueType getValue(){
		return value;
	}
	public void setValue(ValueType value){
		this.value = value;
	}
}
