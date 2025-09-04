# Thread

## [1. Process là gì ?](#process)

## [2. Thread là gì ?](#thread)

## [3. Có bao nhiêu cách để tạo 1 thread trong java ? Khác biệt giữa việc sử dụng các cách đó gì ?](#thread)

## 4. Thế nào là multi thread ? Sử dụng multi thread mang lại ưu nhược điểm gì ?

## 5. Làm thế nào để biết được 1 thread, multi thread đã hoàn thành hay chưa?

## 6. Có giới hạn việc tạo ra bao nhiêu thread trong 1 ứng dụng java hay không?

[Chú thích](#chú-thích)

## Process

## Thread

Có 2 cách để tạo 1 thread trong Java, trong đó có 1 cách trực tiếp và 1 cách gián tiếp

- Tạo bằng Thread class : Việc extends class không bắt buộc ta phải override hàm run của super class

```
class SomeThing extends Thread {
@Override 
public void run(){}
...
```

- Triển khai qua Runnable: Vì là dạng triển khai của Interface nên bắt buộc method run() phải được ghi đè. 1 instance
  của Thread sẽ được tạo ngay sau đó và chứa method này

```
class SomeThing implements Runnable {
@Override 
public void run (){}
...
// C1
SomeThing something = new SomeThing();
Thread thread = new Thread (something);

//C2
Thread thread = new Thread( () -> {} ); // lambda expression

```

Có thể nhìn thấy ưu điểm vượt trội nằm ở việc triển khai 1 interface Runnable hơn là việc kế thừa từ một Thread vì
implements hỗ trợ đa triển khai trong khi kế thừa chỉ hỗ trợ đơn kế thừa

### Chú thích


