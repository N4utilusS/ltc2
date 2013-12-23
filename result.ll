define i32 @main () {
%a = alloca i18
%b = alloca i18
%c = alloca i18
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
