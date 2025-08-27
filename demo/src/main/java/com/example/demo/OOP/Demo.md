# Object-oriented programming

## [1. Nêu ra các tính chất quan trọng của hướng đối tượng](#tính-đóng-gói-encapsulation)

## [2. Access modifier trong java có những loại nào ? Nêu đặc điểm của từng loại](#1-access-modifiers)

## [3. Phân biệt class và instance](#class-vs-instance)

## [4. Phân biệt Abstract và Interface , Nêu trường hợp sử dụng cụ thể. Nếu 2 interface hoặc 1 abstract và 1 interface có 1 function cùng tên, có thể cùng hoặc khác kiểu trả về cùng được kế thừa bởi một class, chuyện gì sẽ xảy ra?](#abstract-vs-interface)

## [5. Thế nào là Overriding và Overloading](#overloading)

## [6. Một function có access modifier là private or static có thể overriding được không?](#overriding)

## [7. Một phương thức final có thể kế thừa được không ?](#tính-kế-thừa-inheritance)

## [8. Phân biệt hai từ khóa This và Super](#this-vs-super)

## [Chú thích](#chú-thích)

Trong Java, cần khẳng định OOP không phải là một danh sách các "tính năng" (như class, extends, private). Đó là một mô hình tư duy để giải quyết sự phức tạp trong việc xây dựng phần mềm. Nguồn gốc của nó (từ Simula và Smalltalk) là nỗ lực mô phỏng thế giới thực, nơi các thực thể độc lập (đối tượng) có trạng thái và hành vi riêng, tương tác với nhau thông qua các thông điệp. Mục tiêu cuối cùng là quản lý sự phụ thuộc (dependency management) và kiểm soát sự phức tạp (complexity control) khi hệ thống phát triển.

## Class

> Class đóng nhiều vai trò cùng lúc. Nó là một thành phần của hệ thống kiểu, cũng là nơi để chưa đựng hành vi và là một metadata cho JVM

Khi được nạp bởi ClassLoader, class được biểu diễn trong bộ nhớ như một metadata. Nó định nghĩa cấu trúc của đối tượng (tên và kiểu của các trường) và hành vi (mã bytecode của các phương thức). Class tồn tại trong Metaspace (Java 8+) hoặc PermGen (Java 7 trở về trước) và có vòng đời từ khi được nạp đến khi ClassLoader bị hủy.

Đối với mỗi class, JVM sẽ tạo ra một bản sao duy nhất trong Metaspace, bất kể có bao nhiêu instance được tạo ra từ class đó. Điều này giúp tiết kiệm bộ nhớ và cho phép chia sẻ hành vi giữa các instance.

### This
> Từ khóa `this` trong Java là một tham chiếu đặc biệt bên trong một phương thức hoặc constructor, nó trỏ đến instance hiện tại của lớp. `this` được sử dụng để phân biệt giữa các biến instance và các biến cục bộ hoặc tham số có cùng tên, cũng như để gọi các phương thức hoặc constructor khác trong cùng một lớp.

Thông thường this được dùng trong constructor đế phân biệt tham số truyền vào với các biến instance của lớp, một ứng dụng khác là Constructor Chaining - gọi constructor này từ constructor khác trong cùng một lớp để tránh lặp lại mã nguồn, điều kiện là câu lệnh this phải là câu lệnh đầu tiên trong constructor:
```
public class Rectangle {
    private int width, height;

    // Constructor chính
    public Rectangle(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // Constructor thứ hai tạo ra một hình vuông
    public Rectangle(int side) {
        this(side, side); // Gọi constructor Rectangle(int, int)
    }

    // Constructor mặc định
    public Rectangle() {
        this(1, 1); // Gọi constructor Rectangle(int, int)
    }
}
```

Ngoài ra nó cũng có thể trả về instance hiện tại từ một phương thức:
```
public class Calculator {
    private int result = 0;

    public Calculator add(int value) {
        this.result += value;
        return this; // Trả về chính đối tượng Calculator
    }

    public Calculator subtract(int value) {
        this.result -= value;
        return this;
    }
}
```

Hay truyền instance hiện tại làm tham số cho một phương thức khác:
```
public class EventSource {
    public void register(EventListener listener) {
        listener.onEvent(this); // Truyền chính EventSource này vào
    }
}
```
## Instance

> Một instance (object) là sự hiện thực hóa của một class tại thời điểm chạy. Nó là một thực thể cụ thể, tồn tại trong bộ nhớ

Vai trò của instance trong quản lý trạng thái:
- Chức năng nổi bật của 1 instance là cấp phát và quản lý vùng nhớ riêng trên Heap để lưu trữ các giá trị cụ thể cho các fields non-static đã được định nghĩa trong class. Mỗi instance có một không gian trạng thái độc lập

- Mỗi instance được tạo ra trên Heap đều có một định danh duy nhất của nó trong suốt vòng đời của nó, thường là địa chỉ ô nhớ của nó, điều này khiến chúng được phân biệt với các instance khác mặc dù chúng cùng trạng thái

Vai trò của instance trong thực thi hành vi:
- Instance là chủ thể thực thi hành vi được định nghĩa trong class thông qua các phương thức. Khi một phương thức được gọi, tham chiếu đến instance đó (con trỏ **this**) được ngầm truyền vào phương thức.
- Instance có thể tương tác với các instance khác thông qua việc gọi phương thức trên chúng, điều này tạo nên mạng lưới các đối tượng tương tác trong hệ thống
- Instance có thể thay đổi trạng thái của chính nó thông qua các phương thức, điều này làm cho chúng trở nên động và có thể phản ứng với các sự kiện trong hệ thống


## Class vs Instance

|                   |                              Class                              |                      Instance                      |
| :---------------: | :-------------------------------------------------------------: | :------------------------------------------------: |
|    **Bản thể**    |                           Trừu tượng                            |                       Vật lý                       |
|   **Hoạt động**   |                   Tại compile time và runtime                   |                    Tại runtime                     |
| **Vị trí bộ nhớ** |                         Metaspace, Heap                         |                        Heap                        |
|   **Số lượng**    |                 Một định nghĩa cho ClassLoader                  |              0, 1 hoặc nhiều thể hiện              |
|     **State**     | Định nghĩa cấu trúc của trạng thái (tên và kiểu của các trường) |   Chứa đựng các giá trị cụ thể cho trạng thái đó   |
|   **Behavior**    |      Định nghĩa hành vi (chứa mãbytecode của phương thức)       | Thực thi hành vi (là chủ thể gọi các phương thức)  |
|   **Vòng đời**    |       Tồn tại từ khi được nạp đến khi ClassLoader bị hủy        | Tồn tại từ lúc được khởi tạo đến khi bị GC thu hồi |

## Tính đóng gói (Encapsulation)

> Một module có thể che giấu những quyết định thiết kế có thể thay đổi trong tương lai, thông tin cần che giấu (đóng gói) không chỉ là dữ liệu, mà còn là sự tồn tại của nó, cấu trúc và thuật toán xử lý nó.

Mục đích:

- Che giấu thông tin, ngăn chặn việc truy cập trực tiếp đối tượng từ bên ngoài, đảm bảo tính vẹn toàn của đối tượng
- Dễ bảo trì và nâng cấp khi thay đổi cấu trúc bên trong module

### 1. Access Modifiers

> > Cấp độ truy cập là công cụ thiết kế cơ bản, quyết định điều gì là công khai và điều gì cần che giấu với các mức độ khác nhau

Phân loại:

- **public**: Phạm vi truy cập rộng nhất (toàn project). Public là "cánh cửa" duy nhất mà bên ngoài nên sử dụng để tương tác với một đối tượng đóng gói, biến hay phương thức, class public ít thay đổi một cách tùy tiện. Tuy nhiên, việc lạm dụng public có thể phơi bày chi tiết triển khai của đối tượng và tạo ra tight coupling
- **protected**: Phạm vi truy cập bởi các lớp trong cùng package và các subclass ngay cả khi chúng ở khác package. Ứng dụng kinh điển của nó là triển khai chi tiết hành vi của lớp cha (hành vi của lớp cha là khung). Tuy nhiên tight coupling, bản chất yếu hơn private khiến protected cần sử dụng thận trọng
- **default**: Phạm vi truy cập là các lớp khác trong package. Ứng dụng mạnh với package-private - cho phép xây dựng tập hợp các lớp liên kết chặt chẽ với nhau mà không cần phơi bày các tương tác nội bộ của chúng ra bên ngoài
- **private**: Phạm vi truy cập trong lớp đó, lớp con cũng không thể truy cập trực tiếp. Vì không phụ thuộc nên thành phần private có thể tự do thay đổi mà không phá vỡ các phần khác của hệ thống.

## Tính kế thừa (Inheritance)

> Kế thừa cho phép một lớp con mới thừa hưởng lại thuộc tính và phương thức của lớp đã tồn tại (super-class). Mối quan hệ này được gọi là IS-A

Mục đích:

- Tính tái sử dụng cao: không cần viết lại các thuộc tính và phương thức của lớp cha
- Cấu trúc, tổ chức dễ dàng: Kế thừa tạo ra một hệ thống cây phân cấp tự nhiên giữa các lớp, việc quản lý và hiểu chương trình cao hơn

Có 2 loại kế thừa:

- Kế thừa interface (một lớp implements 1 interface): Là nền tảng cho tính đa hình và loose coupling (thiết kế lỏng lẻo)
- Kế thừa triển khai (một lớp extends từ 1 lớp khác): Tái sử dụng mã nguồn, nhưng sự thay đổi lớp cha có thể phá vỡ lớp con, đồng thời tạo ra liên kết chặt chẽ giữa 2 lớp (tight coupling)

Nguyên lý thiết kế hiện đại luôn ưu tiên HAS-A hơn do sự linh hoạt, cho phép thay đổi hành vi lúc chạy và tránh được hệ thống phân cấp phức tạp

Phương thức final có thể được kế thừa nhưng không thể Override. Nó đảm bảo rằng hành vi quan trọng không bị thay đổi bởi các lớp con, giúp duy trì tính nhất quán và an toàn trong hệ thống.

![ques](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/Screenshot%202025-08-27%20091951.png)

### Super
> Từ khóa `super` trong Java là một tham chiếu đặc biệt bên trong một lớp con, nó trỏ đến lớp cha trực tiếp của lớp con đó. 

Về lý thuyết, constructor lớp con có trách nhiệm gọi đến constructor lớp cha, nếu không gọi thì trình biên dịch sẽ tự động gọi đến constructor không tham số của lớp cha. Tuy nhiên, nếu lớp cha không có constructor mặc định (không tham số), thì lớp con phải gọi rõ ràng một constructor cụ thể của lớp cha bằng cách sử dụng `super(...)` với các tham số phù hợp. Câu lệnh `super(...)` phải là câu lệnh đầu tiên trong constructor của lớp con.

Khi một lớp con ghi đè một phương thức của lớp cha, đôi khi nó vẫn cần gọi đến phiên bản gốc của phương thức đó trong lớp cha để mở rộng chức năng thay vì thay thế hoàn toàn:
```
class Animal {
    public void move() {
        System.out.println("Animal is moving");
    }
}

class Horse extends Animal {
    @Override
    public void move() {
        super.move(); // Gọi phiên bản của Animal
        System.out.println("Horse is galloping"); // Thêm hành vi mới
    }
}
// Output:
// Animal is moving
// Horse is galloping
```

### This vs Super
|              |                            This                            |                           Super                           |
|:------------:| :-------------------------------------------------------: | :-------------------------------------------------------: |
| **Ý nghĩa**  | Tham chiếu đến instance hiện tại của lớp | Tham chiếu đến lớp cha trực tiếp của lớp con |
| **Mục đích** | Phân biệt biến instance với biến cục bộ hoặc tham số có cùng tên, gọi các phương thức hoặc constructor khác trong cùng một lớp | Gọi constructor của lớp cha, gọi phương thức bị ghi đè trong lớp cha |
| **Bối cảnh** | Sử dụng trong constructor, phương thức của cùng một lớp | Sử dụng trong constructor, phương thức của lớp con |
| **Bản chất tham chiếu** | Trỏ đến instance hiện tại | Trỏ đến lớp cha trực tiếp |


## Tính đa hình (Encapsulation)

> Đa hình cho phép một đối tượng có thể thực hiện một hành động theo nhiều cách khác nhau, thể hiện qua **Overloading** và **Overriding**

Mục đích:

- Cung cấp khả năng mở rộng chương trình

Đa hình về mặt kiến trúc là cơ chế cho phép sự đảo ngược phụ thuộc (Dependency Inversion Principle - DIP). Giữa một module cấp cao và một module cấp thấp, module cao tương tác với một interface trừu tượng, lúc runtime, module cấp thấp sẽ được "tiêm" (inject) vào để thực hiện hành vi. Đây là cơ chế cốt lõi của Dependency Injection - trái tim của Spring framework.

### Overloading
> Method Overloading (còn gọi là đa hình tĩnh hay đa hình tại thời điểm biên dịch - compile-time polymorphism) là khả năng định nghĩa nhiều phương thức có cùng tên trong cùng một lớp, nhưng với danh sách tham số khác nhau.

Điều kiện để overload:
- Cùng tên phương thức
- Khác kiểu tham số, số lượng tham số hoặc thứ tự tham số
- Có thể khác kiểu trả về nhưng không bắt buộc
- Phạm vi truy cập có thể khác nhau

Nạp chồng được tạo ra với mục đích tăng tính dễ đọc và dễ sử dụng của API, được ứng dụng rất phổ biến với Constructor. 
Quá trình xác định phương thức nào sẽ được gọi diễn ra tại thời điểm biên dịch(static-blinding) 
```
class Calculator {
    // Overloading phương thức add
    public int add(int a, int b) {
        System.out.println("Adding two integers");
        return a + b;
    }

    public double add(double a, double b) {
        System.out.println("Adding two doubles");
        return a + b;
    }

    public int add(int a, int b, int c) {
        System.out.println("Adding three integers");
        return a + b + c;
    }
}

public class Main {
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        calc.add(5, 10);       // Trình biên dịch quyết định gọi phiên bản (int, int)
        calc.add(3.5, 2.5);    // Trình biên dịch quyết định gọi phiên bản (double, double)
        calc.add(1, 2, 3);     // Trình biên dịch quyết định gọi phiên bản (int, int, int)
    }
}
```

### Overriding
>Method Overriding (còn gọi là đa hình động hay đa hình tại thời điểm chạy - run-time polymorphism) là một cơ chế cho phép một lớp con (subclass) cung cấp một triển khai cụ thể cho một phương thức đã được định nghĩa trong lớp cha (superclass) của nó.

Điều kiện để overriding:
- Phương thức trong lớp con phải có cùng tên, cùng kiểu trả về và cùng danh sách tham số với phương thức trong lớp cha
- Phương thức trong lớp con phải có phạm vi truy cập bằng hoặc rộng hơn phương thức trong lớp cha
- Phương thức trong lớp con không thể có phạm vi truy cập hẹp hơn
- Phương thức trong lớp con không thể ném ra ngoại lệ kiểm tra (checked exception) lớn hơn phương thức trong lớp cha
- Phương thức không thể là static hoặc final, private thì càng không 

Việc quyết định phương thức sẽ được gọi diễn ra tại thời điểm runtime. JVM dựa vào kiểu đối tượng thực tế đang tham chiếu để xác định phương thức (dynamic binding)
```
class Animal {
    public void makeSound() {
        System.out.println("The animal makes a sound");
    }
}

class Dog extends Animal {
    // Overriding phương thức makeSound
    @Override // Annotation này giúp trình biên dịch kiểm tra quy tắc ghi đè
    public void makeSound() {
        System.out.println("The dog barks: Woof woof");
    }
}

class Cat extends Animal {
    @Override
    public void makeSound() {
        System.out.println("The cat meows: Meow");
    }
}

public class Main {
    public static void main(String[] args) {
        Animal myAnimal = new Animal();
        Animal myDog = new Dog(); // Biến tham chiếu kiểu Animal, đối tượng thực tế là Dog
        Animal myCat = new Cat(); // Biến tham chiếu kiểu Animal, đối tượng thực tế là Cat

        myAnimal.makeSound(); // JVM gọi phiên bản của Animal
        myDog.makeSound();    // JVM kiểm tra đối tượng thực tế là Dog -> gọi phiên bản của Dog
        myCat.makeSound();    // JVM kiểm tra đối tượng thực tế là Cat -> gọi phiên bản của Cat
    }
}
```
---
## Tính trừu tượng (Abstraction)

> Trừu tượng là quá trình ẩn đi những chi tiết triển khai phức tạp và chỉ hiển thị chức năng cần thiết, nó tập trung vào "what" thay vì "how"

Mục đích:

- Đơn giản hóa sự phức tạp: Việc chia một hệ thống thành các phần nhỏ giúp ta dễ hiểu và dễ quản lý
- Linh hoạt: Cho phép thay đổi cách thức hoạt động bên trong 1 hệ thống mà không ảnh hưởng đến các thành phần khác sử dụng nó
- Cung cấp một bộ quy tắc chung cho các lớp thực thi phải tuân theo

### Interface
>Một interface là một "hợp đồng" hoàn toàn trừu tượng, định nghĩa một tập hợp các hành vi mà một lớp phải thực hiện. Một lớp implements (thực thi) một interface, qua đó cam kết rằng nó sẽ cung cấp triển khai cho tất cả các phương thức được định nghĩa trong interface đó.

Một interface có thể chứa:
- Phương thức trừu tượng (không có phần thân) - mặc định
- Phương thức mặc định (có phần thân) - từ Java 8
- Phương thức tĩnh (có phần thân) - từ Java 8 và được gọi trực tiếp từ interface
- Biến hằng (public static final) - mặc định
```
public interface Animal {
    // Phương thức trừu tượng
    void makeSound();

    // Phương thức mặc định
    default void eat() {
        System.out.println("This animal eats food.");
    }

    // Phương thức tĩnh
    static void info() {
        System.out.println("Animals are living beings.");
    }

    // Biến hằng
    int NUMBER_OF_LEGS = 4; // public static final by default
}
```

### Abstract Class
>Một lớp trừu tượng là một lớp được khai báo với từ khóa `abstract`. Nó được thiết kế để làm lớp cơ sở (base class) cho các lớp khác kế thừa. Lớp trừu tượng không thể được khởi tạo trực tiếp bằng toán tử new.

Một lớp trừu tượng có thể chứa cả phương thức trừu tượng (không có phần thân) và phương thức đã được triển khai (có phần thân). Phương thức trừu tượng có từ khóa `abstract` bắt buộc các lớp con phải cung cấp triển khai cụ thể cho chúng, trong khi các phương thức đã triển khai có thể được sử dụng trực tiếp hoặc ghi đè bởi các lớp con.

Lớp trừu tượng cũng có thể chứa các biến instance, hằng số, và các khối khởi tạo. Tuy nhiên, nó không thể được khởi tạo trực tiếp, mà phải được kế thừa bởi một lớp con cụ thể.

```
public abstract class Animal {
    // Biến instance
    private String name;

    // Constructor
    public Animal(String name) {
        this.name = name;
    }

    // Phương thức trừu tượng
    public abstract void makeSound();

    // Phương thức đã triển khai
    public void eat() {
        System.out.println(name + " is eating.");
    }

    // Getter cho biến instance
    public String getName() {
        return name;
    }
}
```



### Abstract vs Interface
|                     |                          Abstract Class                           |                             Interface                              |
| :-----------------: | :---------------------------------------------------------------: | :----------------------------------------------------------------: |
|     **Mục đích**       |   Cung cấp một lớp cơ sở với cả hành vi đã triển khai và trừu tượng   | Cung cấp một hợp đồng hoàn toàn trừu tượng mà các lớp phải tuân theo |
|   **Kế thừa**       | Một lớp chỉ có thể kế thừa từ một lớp trừu tượng (Java không hỗ trợ đa kế thừa) | Một lớp có thể implements nhiều interface |
| **Phương thức** | Có thể chứa cả phương thức trừu tượng (không có phần thân) và phương thức đã triển khai (có phần thân) | Mặc định tất cả phương thức là trừu tượng (không có phần thân) trừ khi được khai báo là default hoặc static (có phần thân) |
|   **Biến**    | Có thể chứa biến instance và hằng số | Chỉ có thể chứa hằng số (public static final) |
| **Trạng thái** | Có thể có trạng thái (biến instance) | Không có trạng thái (chỉ có hằng số) |
| **Khởi tạo** | Không thể khởi tạo trực tiếp, phải được kế thừa | Không thể khởi tạo trực tiếp, phải được implements |
| **Phạm vi truy cập** | Có thể có các phương thức và biến với bất kỳ phạm vi truy cập nào (private, protected, public) | Mặc định tất cả phương thức và biến là public |


Khi một class được implements bởi 2 interface có cùng 1 phương thức hoặc 1 abstract class và 1 interface có cùng phương thức
, hầu hết trong nhiều case class đó phải cung cấp một triển khai duy nhất cho phương thức đó. Nếu không, trình biên dịch sẽ báo lỗi do không rõ ràng về việc sử dụng phương thức nào. Điều này giúp tránh xung đột và đảm bảo rằng lớp con có một hành vi nhất quán.


![nah](https://github.com/GVOne-blood/Backend/blob/main/demo/src/main/resources/local/code.png)


>>Trong hệ thống phân cấp kiểu của Java, các thành viên (phương thức, trường) được kế thừa từ một lớp cha (superclass), dù là abstract hay cụ thể, luôn có độ ưu tiên cao hơn các phương thức default được kế thừa từ một interface.

TH1: 2 interface có cùng phương thức được 1 class implements -> override phương thức đó; dùng super; cả 2

TH2: 1 abstract class và 1 interface có cùng phương thức được 1 class kế thừa -> override phương thức đó; dùng super; chỉ abstract class

TH3: 1 abstract class và 1 interface nhưng method ở interface là method đã triển khai -> override phương thức đó; dùng super; chỉ interface (nói chung java ưu tiên interface hơn abstract class)

## Tính toàn năng của OOP

OOP không toàn năng, một số hạn chế phổ biến như:

- Mutable State in multithread: Cốt lõi OOP là các đối tượng đóng gói trạng thái và thay đổi nó qua thời gian. Trong môi trường đa luồng, các vấn đề như race-conditions, deadlocks,...OOP không thể giải quyết triệt để so với Lập trình Hàm (Functional Programming - FP)
- Object-Relational Impedance Mismatch: Mô hình đối tượng trong bộ nhớ là một đồ thị các đối tượng liên kết với nhau, trong khi mô hình dữ l iệu trong RDB là các bảng 2 chiều. Sự khác biệt này dẫn đến sự ra đời của các công cụ như ORM (Hibernate, JPA) để chuyển đổi qua lại
- Unnecessary Complexity: OOP có xu hướng mô hình hóa mọi thứ thành object dẫn đến việc các tác vụ đơn giản cũng được đối tượng hóa gây rườm rà. Lambda, Stream API là các yếu tố lập trình hàm được thêm để giải quyết vấn đề này.

### Chú thích
1. ClassLoader: ClassLoader trong Java là một thành phần quan trọng của JVM, chịu trách nhiệm tải các class vào bộ nhớ khi chương trình Java được thực thi. Đây là cơ chế giúp Java đạt được tính modular, bảo mật, và độc lập nền tảng.