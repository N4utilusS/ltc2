package main;

import java.io.FileWriter;
import java.io.IOException;

public class LLVM {
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
			System.out.println("== LLVM ==\n" + str + "\n====");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void varDecl(String name, Image image){
		
		if (image.digitAfter > 0){
			this.writeToLLFile("%" + name + " = alloca float");
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
	
	void writeHeader(){
		//this.writeToLLFile("declare i32 @getchar ()");
		this.writeToLLFile("declare i32 (i8*, ...)* @printf(i8*, ...) nounwind");
		
		
	}
	
	void writeReadInt(){
		this.writeToLLFile("declare i32 @getchar ()");
		this.writeToLLFile("define i32 @readInt() {\n" +
				"entry:\n" +
				"%res = alloca i32\n" +
				"%digit = alloca i32\n" +
				"store i32 0, i32* %res\n" +
				"br label %read\n" +
				"read:\n" +
				"%0 = call i32 @getchar()\n" +
				"%1 = sub i32 %0, 48\n" +
				"store i32 %1, i32* %digit\n" +
				"%2 = icmp ne i32 %0, 10\n" +
				"br i1 %2, label %save, label %exit\n" +
				"save:\n" +
				"%3 = load i32* %res\n" +
				"%4 = load i32* %digit\n" +
				"%5 = mul i32 %3, 10\n" +
				"%6 = add i32 %5, %4\n" +
				"store i32 %6, i32* %res\n" +
				"br label %read\n" +
				"exit:\n" +
				"%7 = load i32* %res\n" +
				"ret i32 %7\n" +
				"}");
	}
}
