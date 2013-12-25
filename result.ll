@.str = private unnamed_addr constant [3 x i8] c"%f\00", align 1
@.str1 = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str2 = private unnamed_addr constant [3 x i8] c"%s\00", align 1
declare i32 @printf(i8*, ...)
declare i32 @scanf(i8*, ...)
declare i32 @putchar(i32)

@a = global i18 42
@b = global i18 0
@c = global i18 0
define i32 @main () nounwind ssp uwtable {
entry:
%1 = alloca i18
%2 = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @.str1, i32 0, i32 0), i18* %1)
%3 = load i18* %1
store i18 %3, i18* @a

%4 = alloca i18
%5 = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @.str1, i32 0, i32 0), i18* %4)
%6 = load i18* %4
store i18 %6, i18* @b

br label %L0

L0:
%7 = load i18* @b

%8 = i5 0

%9 = sext i18 %7 to i19
%10 = zext i5 %8 to i19
%11 = icmp eq i19 %9, %10

%12 = zext i1 %11 to i5
%13 = icmp ne i5 %12, 0

br i1 %13, label %L2, label %L1

L1:
call void @find()
br label %L0

L2:
call i32 @putchar(i32 39)
call i32 @putchar(i32 118)
call i32 @putchar(i32 97)
call i32 @putchar(i32 108)
call i32 @putchar(i32 101)
call i32 @putchar(i32 117)
call i32 @putchar(i32 114)
call i32 @putchar(i32 58)
call i32 @putchar(i32 39)
call i32 @putchar(i32 10)
%14 = load i18* @a

%15 = call i32 (i8*, ...)* @printf(i8* getelementptr inbounds ([3 x i8]* @.str1, i32 0, i32 0), i18 %14)
ret i32 0
}

define void @find(){
entry:
%16 = load i18* @c

store i18 %16, i18* @b

br label %L0

L0:
%17 = load i18* @a

%18 = load i18* @b

%19 = sext i18 %17 to i19
%20 = sext i18 %18 to i19
%21 = icmp slt i19 %19, %20

%22 = zext i1 %21 to i5
%23 = icmp ne i5 %22, 0

br i1 %23, label %L2, label %L1

L1:
call void @diff()
br label %L0

L2:
%24 = load i18* @a

store i18 %24, i18* @b

%25 = load i18* @c

store i18 %25, i18* @a

}

define void @diff(){
entry:
%26 = load i18* @b

%27 = load i18* @a

%28 = sext i18 %27 to i21
%29 = sext i18 %26 to i21
%30 = sub i21 %28, %29

%31 = trunc i21 %30 to i18
store i18 %31, i18* @a

}

