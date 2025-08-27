package com.example.demo.OOP;


abstract class Abstract1 {
    public void method1() {
        System.out.println("Abstract1 method1");
    }
    public final void method2(){
        System.out.println("Abstract1 method2");
    }

}
interface Interface1 {
     void method1();
}
interface Interface2 {
     void method1();
}

//TH1
class Code extends Abstract1 implements  Interface2 {

    public static void main(String[] args) {
        Code code = new Code();
        code.method1(); // Abstract1 method1
        code.method2(); // Abstract1 method2

    }
}
//TH2
class Code2 implements Interface1, Interface2 {

    @Override
    public void method1() {
        System.out.println("Code2 method1");
    }

    public static void main(String[] args) {
        Code2 code2 = new Code2();
        code2.method1(); // Code2 method1

    }
}
