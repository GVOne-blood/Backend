package com.example.demo.DesignPattern;

public class Factory {
    public static void main(String[] args) {
        Bank bank = BankFactory.getBank(BankType.MB);
        System.out.println(bank.getBankName());
        Bank bank2 = BankFactory.getBank(BankType.VIETCOM);
        System.out.println(bank2.getBankName());
    }
}

// Product
interface Bank { 

     String getBankName();
}

// Concrete Product
class MBbank implements Bank {
    @Override
    public String getBankName() {
        return "MBbank here";
    }
}

// Concrete Product
class Vietcombank implements Bank {
    @Override
    public String getBankName() {
        return "Vietcombank here";
    }
}

// Concrete Product
class TPbank implements Bank {
    @Override
    public String getBankName() {
        return "TPbank here";
    }
}

// Factory
enum BankType {
    MB, VIETCOM, TP
}

// Factory
abstract class BankFactory {
    private BankFactory() {
    }

    // Factory Method
    public static final Bank getBank(BankType type) {
        return switch (type) {
            case MB -> new MBbank();
            case VIETCOM -> new Vietcombank();
            case TP -> new TPbank();
            default -> throw new IllegalArgumentException("No such bank");
        };
    }

}
