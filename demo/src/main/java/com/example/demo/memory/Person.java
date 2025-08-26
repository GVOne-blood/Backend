package com.example.demo.memory;

public class Person {

    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public void processUserData(int userId) { // userId được đẩy vào stack
    double score = 9.5; // score được cấp phát trên stack
    String userName = "ExperiencedDev"; // Biến tham chiếu 'userName' được cấp phát trên stack
                                        // Đối tượng String "ExperiencedDev" thì nằm trên Heap (hoặc String Pool)
} // Khi phương thức kết thúc, toàn bộ stack frame (userId, score, userName) bị hủy.
    
public void createObjects() {
    // 1. Khai báo tham chiếu 'p1' trên Stack.
    // 2. new Person(...) -> JVM tìm một vùng trống trên HEAP đủ để chứa đối tượng Person.
    // 3. Gán địa chỉ của đối tượng trên Heap cho tham chiếu 'p1' trên Stack.
    Person p1 = new Person("Alice", 30);

    // Tương tự cho p2
    Person p2 = new Person("Bob", 25);
    
    // Mảng cũng là đối tượng và được cấp phát trên Heap
    int[] numbers = new int[100]; // 100 * 4 bytes được cấp phát trên Heap
}
    public static void main(String[] args) {
        
    }
    
}
