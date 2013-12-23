define i32 @main () {
%a = alloca i18

%1 = sext i8 %0 to i18
store i18 %1, i18* %a
%b = alloca i18
%c = alloca i18
%1 = load i18* %b
%2 = load i18* %a
%3 = load i18* %c

store i18 %1, i18* %b
%4 = load i18* %a
%5 = load i18* %b
%6 = load i18* %a

store i18 %1, i18* %b
%7 = load i18* %c

store i18 %1, i18* %a
%8 = load i18* %b

%1 = trunc i21 %0 to i18
store i18 %1, i18* %a
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
