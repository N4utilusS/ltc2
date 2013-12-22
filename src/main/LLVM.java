package main;

import java.io.FileWriter;
import java.io.IOException;

public class LLVM {
	private int unnamedCounter = 0;
	private FileWriter fw;
	
	LLVM(String path){
			try {
				this.fw = new FileWriter(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	void writeToLLFile(String str){
		try {
			this.fw.write(str + "\n");
			System.out.println("====\n" + str + "\n====");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void varDecl(String name, Image image){
		
		if (image.digitAfter > 0){
			
		}
		else{	// INTEGER
			int nbBit = (int) Math.ceil(image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + name + " = alloca i" + nbBit);
		}
		
	}
	
	void writeMain(){
		this.writeToLLFile("define i32 @main () {");
	}

	void close() {
		try {
			this.fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
