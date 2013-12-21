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

	public boolean equals(Object obj){
		if (obj == this)
			return true;
		else
			if (obj instanceof Symbol<?>){
				@SuppressWarnings("unchecked")
				Symbol<?> s = (Symbol<String>) obj;	
				return(super.equals(s) && this.value.equals(s.value) && this.unit == s.unit);
			}
		return false;
	}
	
	public void setTypeWithImage() {
		LexicalUnit l = LexicalUnit.INTEGER;
		Type t = new Type();
		String image = (String) this.get(IMAGE);
		System.out.println("--------------- The string is: " + image);
		
		if (image.matches("s?9(\\([1-9][0-9]*\\))?v9(\\([1-9][0-9]*\\))?"))
			l = LexicalUnit.REAL;
		t.l = l;
		t.updateImageWithImage(image);
		
		this.put(TYPE, t);
	}
}
