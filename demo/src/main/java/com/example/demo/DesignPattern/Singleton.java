package com.example.demo.DesignPattern;

public class Singleton {

    public static void main(String[] args) {
        Singleton singleton1 = new Singleton();
        Singleton singleton2 = new Singleton();
        EagerInit eager1 = EagerInit.getInstance();
        EagerInit eager2 = EagerInit.getInstance();
        System.out.println(singleton1.equals(singleton2)); // false
        System.out.println(eager1.equals(eager2)); // true
    }

}

// Eager Initialization
class EagerInit {

    private static final EagerInit instance = new EagerInit();

    private EagerInit() {
    }

    public static EagerInit getInstance() {
        return instance;
    }

}

// Lazy Initialization
class LazyInit {
    private static LazyInit instance;

    private LazyInit() {
    }

    public synchronized static LazyInit getInstance() {
        if (instance == null) {
            instance = new LazyInit();
        }
        return instance;
    }
}

// Double Check Locking
class DoubleCheckLocking {
    private static volatile DoubleCheckLocking instance;

    private DoubleCheckLocking() {
    }

    public static DoubleCheckLocking getInstance() {
        if (instance == null) {
            synchronized (DoubleCheckLocking.class) {
                if (instance == null) {
                    instance = new DoubleCheckLocking();
                }
            }
        }
        return instance;
    }
}

// Bill Pugh Singleton
class BillPughSingleton {
    private BillPughSingleton() {
    }

    // Lớp static này chỉ được nạp khi getInstance() được gọi
    private static class SingletonHelper {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }

    public static BillPughSingleton getInstance() {
        return SingletonHelper.INSTANCE;
    }

}
