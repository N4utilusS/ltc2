@a = global i18 0
@b = global i18 0
@c = global i18 0
define i32 @main () nounwind ssp uwtable {
entry:
%1 = alloca float
%2 = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @.str, i32 0, i32 0), float* %1)
%3 = alloca float
%4 = call i32 (i8*, ...)* @scanf(i8* getelementptr inbounds ([3 x i8]* @.str, i32 0, i32 0), float* %3)
br label %L0
L0:
%5 = load i18* @b
%6 = i5 0
%7 = sext i18 %5 to i19
%8 = zext i5 %6 to i19
%9 = icmp eq i19 %7, %8

%10 = zext i1 %9 to i5
%11 = icmp ne i5 %10, 0
br i1 %11, label %L2, label %L1
L1:call void @find()br label %L0
L2:
%12 = load i18* @a
ret i32 0
}
define void @find(){
entry:
%13 = load i18* @c
store i18 %12, i18* @b
br label %L0
L0:
%14 = load i18* @a
%15 = load i18* @b
%16 = sext i18 %14 to i19
%17 = sext i18 %15 to i19
%18 = icmp slt i19 %16, %17

%19 = zext i1 %18 to i5
%20 = icmp ne i5 %19, 0
br i1 %20, label %L2, label %L1
L1:call void @diff()br label %L0
L2:
%21 = load i18* @a
store i18 %20, i18* @b
%22 = load i18* @c
store i18 %21, i18* @a
}
define void @diff(){
entry:
%23 = load i18* @b
%24 = load i18* @a
%25 = sext i18 %24 to i21
%26 = sext i18 %23 to i21
%27 = sub i21 %25, %26

%28 = trunc i21 %27 to i18
store i18 %27, i18* @a
}
declare i32 @getchar ()
define i32 @readInt() {
entry:
%res = alloca i32
%digit = alloca i32
store i32 0, i32* %res
br label %read
read:
%0 = call i32 @getchar()
%1 = sub i32 %0, 48
store i32 %1, i32* %digit
%2 = icmp ne i32 %0, 10
br i1 %2, label %save, label %exit
save:
%3 = load i32* %res
%4 = load i32* %digit
%5 = mul i32 %3, 10
%6 = add i32 %5, %4
store i32 %6, i32* %res
br label %read
exit:
%7 = load i32* %res
ret i32 %7
}
