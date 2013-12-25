package main;

import java.io.FileWriter;
import java.io.IOException;

public class LLVM {
	private FileWriter fw;
	private long counter = -1;
	private long labelCounter = 0;
	private boolean mainFooterDone = false;

	LLVM(String path){
		try {
			this.fw = new FileWriter(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	long getCounter(){
		return counter;
	}



	void writeToLLFile(String str){
		try {
			this.fw.write(str + "\n");
			//System.out.println("== LLVM ==\n" + str + "\n====");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void varDecl(String name, Image image, String value){

		if (value == null){
			if (image.digitAfter > 0){
				this.writeToLLFile("@" + name + " = global float 0");
			}
			else{	// INTEGER
				int nbBit = (int) Math.ceil(image.digitBefore/Math.log10(2)) + 1;
				this.writeToLLFile("@" + name + " = global i" + nbBit + " 0");
			}
		}
		else{
			if (image.digitAfter > 0){
				this.writeToLLFile("@" + name + " = global float " + value);
			}
			else{	// INTEGER
				int nbBit = (int) Math.ceil(image.digitBefore/Math.log10(2)) + 1;
				this.writeToLLFile("@" + name + " = global i" + nbBit + " " + value);
			}
		}
	}

	void writeMainHeader(){
		this.writeToLLFile("define i32 @main () nounwind ssp uwtable {\nentry:");
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
		this.writeToLLFile("@.str = private unnamed_addr constant [3 x i8] c\"%f\\00\", align 1\n" +
				"@.str1 = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1\n" +
				"declare i32 @printf(i8*, ...)\n" +
				"declare i32 @scanf(i8*, ...)\n" +
				"declare i32 @putchar(i32)\n");


	}

	void wDisplayString(String str){
		for (int i = 0; i < str.length(); ++i)
			this.writeToLLFile("%" + ++this.counter + " = call i32 @putchar(i32 " + (int) str.charAt(i) + ")");
		this.writeToLLFile("%" + ++this.counter + " = call i32 @putchar(i32 10)");
	}

	void wDisplay(Type t){
		if (t.image.digitAfter > 0)
			this.writeToLLFile("%" + ++this.counter + " = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([3 x i8]* @.str, i32 0, i32 0), float %" + t.LLVMTempId + ")");
		else{
			int numberBit = (int) Math.ceil(t.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([3 x i8]* @.str1, i32 0, i32 0), i" + numberBit + " %" + t.LLVMTempId + ")");
		}
	}

	long wAccept(Type t){
		if (t.image.digitAfter > 0)
			this.writeToLLFile("%" + ++this.counter + " = alloca float\n" +
					"%" + ++this.counter + " = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @.str, i32 0, i32 0), float* %" + (this.counter-1) + ")\n" +
					"%" + ++this.counter + " = load float* %" + (this.counter-2));
		else{
			int numberBit = (int) Math.ceil(t.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = alloca i" + numberBit + "\n" +
					"%" + ++this.counter + " = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @.str1, i32 0, i32 0), i" + numberBit + "* %" + (this.counter-1) + ")\n" +
					"%" + ++this.counter + " = load i" + numberBit + "* %" + (this.counter-2));
		}
		return this.counter;
	}



	/*	void writeReadInt(){
		this.writeToLLFile("declare i32 @getchar ()");
		this.writeToLLFile("define i32 @readInt(i32 %amount) {\n" +
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
				"" +
				"br label %read\n" +
				"exit:\n" +
				"%7 = load i32* %res\n" +
				"ret i32 %7\n" +
				"}");
	}*/

	void wPerfUntilHeader(){
		this.writeToLLFile("br label %L" + this.labelCounter + "\n\n" +
				"L" + this.labelCounter + ":");
	}

	void wPerfUntilFooter(long id, String labelName){
		this.writeToLLFile("br i1 %" + id + ", label %L" + (this.labelCounter+2) + ", label %L" + (this.labelCounter+1) + "\n\n" +
				"L" + (this.labelCounter+1) + ":\n" +
				"call void @" + labelName + "()\n" +
				"br label %L" + this.labelCounter + "\n\n" +
				"L" + (this.labelCounter+2) + ":");
	}

	void wPerf(String name){
		this.writeToLLFile("call void @" + name + "()");
	}

	void wIf(long id){
		this.writeToLLFile("br i1 %" + id + ", label %L" + this.labelCounter + ", label %L" + (this.labelCounter+1) + "\n\n" +
				"L" + this.labelCounter + ":");
	}

	void wElse(){
		this.writeToLLFile("br label %L" + (this.labelCounter+2) + "\n\nL" + (this.labelCounter+1) + ":");
	}

	void wEndIf(){
		this.writeToLLFile("br label %L" + (this.labelCounter+2) + "\n\nL" + (this.labelCounter+2) + ":");
		this.labelCounter += 3;
	}

	long wLogicalExpRes(Type t){


		if (t.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp one float %" + t.LLVMTempId + ", 0\n");
		}
		else{
			int nbBit = (int) Math.ceil(t.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = icmp ne i" + nbBit + " %" + t.LLVMTempId + ", 0\n");
		}

		return this.counter;
	}

	void wLabelHeader(String name){
		this.writeToLLFile("define void @" + name + "(){\nentry:");
		this.counter = -1;
	}

	void wLabelFooter(){
		if (this.mainFooterDone){
			this.writeToLLFile("ret void\n}\n");
		}
		else{
			this.mainFooterDone  = true;
			this.writeToLLFile("ret i32 0\n}\n");
		}
	}

	void w28(Symbol<?> s, Type exp){	// Assignation

		Type recType = (Type) s.get(Symbol.TYPE);

		int numberBitV = (int) Math.ceil(recType.image.digitBefore/Math.log10(2)) + 1;
		int numberBitE = (int) Math.ceil(exp.image.digitBefore/Math.log10(2)) + 1;

		if (recType.image.digitAfter == 0 && exp.image.digitAfter == 0){	// INTEGER <-- INTEGER

			if (numberBitV > numberBitE){
				if (recType.image.signed)
					this.writeToLLFile("%" + ++this.counter + " = sext i" + numberBitE + " %" + exp.LLVMTempId + " to i" + numberBitV);
				else
					this.writeToLLFile("%" + ++this.counter + " = zext i" + numberBitE + " %" + exp.LLVMTempId + " to i" + numberBitV);
			}
			else if (numberBitV < numberBitE){
				this.writeToLLFile("%" + ++this.counter + " = trunc i" + numberBitE + " %" + exp.LLVMTempId + " to i" + numberBitV);
			}

			this.writeToLLFile("store i" + numberBitV + " %" + this.counter + ", i" + numberBitV + "* @" + s.getValue() + "\n");
		}
		else if (recType.image.digitAfter > 0 && exp.image.digitAfter == 0){	// REAL <-- INTEGER
			if (exp.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + numberBitE + " %0 to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + numberBitE + " %0 to float");

			this.writeToLLFile("store float %" + this.counter + ", float* @" + s.getValue() + "\n");
		}
		else if (recType.image.digitAfter == 0 && exp.image.digitAfter > 0){	// INTEGER <-- REAL
			if (recType.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = fptosi float %" + exp.LLVMTempId + " to i" + numberBitV);
			else
				this.writeToLLFile("%" + ++this.counter + " = fptoui float %" + exp.LLVMTempId + " to i" + numberBitV);

			this.writeToLLFile("store i" + numberBitV + " %" + this.counter + ", i" + numberBitV + "* @" + s.getValue() + "\n");
		}
		else{	// REAL <-- REAL
			this.writeToLLFile("store float %" + exp.LLVMTempId + ", float* @" + s.getValue() + "\n");
		}
	}

	long w36(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp one float %" + t1.LLVMTempId + ", 0");
		}
		else{
			int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = icmp ne i" + nbBit1 + " %" + t1.LLVMTempId + ", 0");
		}

		if (t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp one float %" + t2.LLVMTempId + ", 0");
		}
		else{
			int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = icmp ne i" + nbBit2 + " %" + t2.LLVMTempId + ", 0");
		}

		this.writeToLLFile("%" + ++this.counter + " = or i1 %" + (this.counter-2) + ", %" + (this.counter-1));

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit + "\n");

		return this.counter;
	}

	long w39(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp one float %" + t1.LLVMTempId + ", 0");
		}
		else{
			int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = icmp ne i" + nbBit1 + " %" + t1.LLVMTempId + ", 0");
		}

		if (t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp one float %" + t2.LLVMTempId + ", 0");
		}
		else{
			int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = icmp ne i" + nbBit2 + " %" + t2.LLVMTempId + ", 0");
		}

		this.writeToLLFile("%" + ++this.counter + " = and i1 %" + (this.counter-2) + ", %" + (this.counter-1));

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit + "\n");

		return this.counter;
	}

	long w42(Type t1, Operator o, Type t2, Type rT){
		long c = 0;

		switch(o){
		case GREATER_THAN :
			c = w42GT(t1, t2, rT);
			break;
		case LOWER_THAN : 
			c = w42LT(t1, t2, rT);
			break;
		case LOWER_OR_EQUALS:
			c = w42LE(t1, t2, rT);
			break;
		case GREATER_OR_EQUALS:
			c = w42GE(t1, t2, rT);
			break;
		case EQUALS:
			c = w42E(t1, t2, rT);
			break;
		}

		return c;
	}

	long w42E(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;
		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp oeq float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId);
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp oeq float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp oeq float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{

			int max = Math.max(nbBit1, nbBit2) + 1;	// Add a supp. bit to allow differentiation between u/s.

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);

			this.writeToLLFile("%" + ++this.counter + " = icmp eq i" + max + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit);

		return this.counter;
	}

	long w42GE(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;
		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp oge float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId);
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp oge float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp oge float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{

			int max = Math.max(nbBit1, nbBit2) + 1;	// Add a supp. bit to allow differentiation between u/s.

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);

			this.writeToLLFile("%" + ++this.counter + " = icmp sge i" + max + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit);

		return this.counter;
	}

	long w42LE(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;
		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp ole float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId);
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp ole float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp ole float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{

			int max = Math.max(nbBit1, nbBit2) + 1;	// Add a supp. bit to allow differentiation between u/s.

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);

			this.writeToLLFile("%" + ++this.counter + " = icmp sle i" + max + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit);

		return this.counter;
	}

	long w42LT(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;
		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp olt float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId);
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp olt float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp olt float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{

			int max = Math.max(nbBit1, nbBit2) + 1;	// Add a supp. bit to allow differentiation between u/s.

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);

			this.writeToLLFile("%" + ++this.counter + " = icmp slt i" + max + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit);

		return this.counter;
	}

	long w42GT(Type t1, Type t2, Type rT) {

		int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;
		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp ogt float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId);
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp ogt float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fcmp ogt float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{

			int max = Math.max(nbBit1, nbBit2) + 1;	// Add a supp. bit to allow differentiation between u/s.

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + max);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + max);

			this.writeToLLFile("%" + ++this.counter + " = icmp sgt i" + max + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		this.writeToLLFile("%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit);

		return this.counter;
	}

	long w45(Type t1, Operator o, Type t2, Type rT){
		long c = 0;

		switch(o){
		case PLUS:
			c = w45Plus(t1, t2, rT);
			break;
		case MINUS:
			c = w45Minus(t1, t2, rT);
			break;
		}
		return c;
	}

	long w45Plus(Type t1, Type t2, Type rT){

		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fadd float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId + "\n");
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fadd float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fadd float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{
			int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + nbBit);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + nbBit);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + nbBit);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + nbBit);

			this.writeToLLFile("%" + ++this.counter + " = add i" + nbBit + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		return this.counter;

	}

	long w45Minus(Type t1, Type t2, Type rT){

		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fsub float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId + "\n");
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fsub float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fsub float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{
			int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;

			if (nbBit > nbBit1){
				if (t1.image.signed)
					this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + nbBit);
				else
					this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + nbBit);
				t1.LLVMTempId = this.counter;
			}

			if (nbBit > nbBit2){
				if (t2.image.signed)
					this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + nbBit);
				else
					this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + nbBit);
				t2.LLVMTempId = this.counter;
			}

			this.writeToLLFile("%" + ++this.counter + " = sub i" + nbBit + " %" + t1.LLVMTempId + ", %" + t2.LLVMTempId + "\n");
		}

		return this.counter;

	}

	long w48(Type t1, Operator o, Type t2, Type rT){
		long c = 0;

		switch(o){
		case MULTIPLY:
			c = w48Mult(t1, t2, rT);
			break;
		case DIVIDE:
			c = w48Div(t1, t2, rT);
			break;
		}
		return c;
	}

	long w48Mult(Type t1, Type t2, Type rT){	// Multiply

		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fmul float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId + "\n");
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fmul float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fmul float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{
			int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + nbBit);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit1 + " %" + t1.LLVMTempId + " to i" + nbBit);

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + nbBit);
			else
				this.writeToLLFile("%" + ++this.counter + " = zext i" + nbBit2 + " %" + t2.LLVMTempId + " to i" + nbBit);

			this.writeToLLFile("%" + ++this.counter + " = mul i" + nbBit + " %" + (this.counter-2) + ", %" + (this.counter-1) + "\n");
		}

		return this.counter;
	}

	long w48Div(Type t1, Type t2, Type rT){	// Division

		int nbBit1 = (int) Math.ceil(t1.image.digitBefore/Math.log10(2)) + 1;
		int nbBit2 = (int) Math.ceil(t2.image.digitBefore/Math.log10(2)) + 1;

		if (t1.image.digitAfter > 0 && t2.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fdiv float %" + t1.LLVMTempId + ", %" + t2.LLVMTempId + "\n");
		}
		else if (t1.image.digitAfter > 0 && t2.image.digitAfter == 0){
			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fdiv float %" + t1.LLVMTempId + ", %" + (this.counter-1) + "\n");
		}
		else if (t1.image.digitAfter == 0 && t2.image.digitAfter > 0){
			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fdiv float %" + (this.counter-1) + ", %" + t2.LLVMTempId + "\n");
		}
		else{
			int nbBit = (int) Math.ceil(rT.image.digitBefore/Math.log10(2)) + 1;

			if (t1.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit1 + " %" + t1.LLVMTempId + " to float");

			if (t2.image.signed)
				this.writeToLLFile("%" + ++this.counter + " = sitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");
			else
				this.writeToLLFile("%" + ++this.counter + " = uitofp i" + nbBit2 + " %" + t2.LLVMTempId + " to float");

			this.writeToLLFile("%" + ++this.counter + " = fdiv float %" + (this.counter-2) + ", %" + (this.counter-1));

			if (t1.image.signed || t2.image.signed){
				this.writeToLLFile("%" + ++this.counter + " = fptosi float " + (this.counter-1) + " to i" + nbBit + "\n");
			}
			else{
				this.writeToLLFile("%" + ++this.counter + " = fptoui float " + (this.counter-1) + " to i" + nbBit + "\n");
			}
		}

		return this.counter;
	}

	long w50(Type t){	// Not

		int nbBit = (int) Math.ceil(1/Math.log10(2)) + 1;

		if (t.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fcmp une float %" + t.LLVMTempId + ", 0.000000e+00\n"
					+ "%" + ++this.counter + " = xor i1 %" + (this.counter-1) + ", true\n"
					+ "%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit + "\n");
		}
		else{
			int nbBit2 = (int) Math.ceil(t.image.digitBefore/Math.log10(2)) + 1;

			this.writeToLLFile("%" + ++this.counter + " = icmp ne i" + nbBit2 + " %" + t.LLVMTempId + ", 0\n"
					+ "%" + ++this.counter + " = xor i1 %" + (this.counter-1) + ", true\n"
					+ "%" + ++this.counter + " = zext i1 %" + (this.counter-1) + " to i" + nbBit + "\n");
		}
		return this.counter;
	}

	long w51(Type t){	// - number

		if (t.image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = fsub float -0.000000e+00, %" + t.LLVMTempId + "\n");
		}
		else{
			int nbBit = (int) Math.ceil(t.image.digitBefore/Math.log10(2)) + 1;

			this.writeToLLFile("%" + ++this.counter + " = sub i" + nbBit + " 0, %" + t.LLVMTempId + "\n");
		}

		return this.counter;
	}

	long w54(String name, Image image){

		if (image.digitAfter > 0){
			this.writeToLLFile("%" + ++this.counter + " = load float* @" + name + "\n");
		}
		else{
			int nbBit = (int) Math.ceil(image.digitBefore/Math.log10(2)) + 1;
			this.writeToLLFile("%" + ++this.counter + " = load i" + nbBit + "* @" + name + "\n");
		}

		return this.counter;
	}

	long w55(String number, Image image){

		number = number.replaceFirst("\\+", "");
		int nbBit = (int) Math.ceil(image.digitBefore/Math.log10(2)) + 1;
		this.writeToLLFile("%" + ++this.counter + " = add i" + nbBit + " 0, " + number + "\n");
		return this.counter;
	}

	long w56(String number){

		number = number.replaceFirst("\\+", "");
		this.writeToLLFile("%" + ++this.counter + " = fadd float 0, " + number + "\n");
		return this.counter;
	}

	long w57(){
		int nbBit = (int) Math.ceil(1/Math.log10(2)) + 1;
		this.writeToLLFile("%" + ++this.counter + " = i" + nbBit + " 1\n");
		return this.counter;
	}

	long w58(){
		int nbBit = (int) Math.ceil(1/Math.log10(2)) + 1;
		this.writeToLLFile("%" + ++this.counter + " = i" + nbBit + " 0\n");
		return this.counter;
	}


}
