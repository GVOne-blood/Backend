package com.example.demo.RegularExpression;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Code {
    static Pattern pattern = Pattern.compile("");
    static Matcher matcher = pattern.matcher("Hello");

    static class Cha {
        private String ten;
        protected String tou;

        public Cha() {
        }

        ;

        public void pr() {
            System.out.println("dd");
        }

        ;
    }

    static class Con extends Cha {
        private String ten = "HUA";
        private String con;

        public Con() {
            super.pr();
            System.out.println("DDD");
        }

        public void pr() {
        }

        ;

        public Con(String temp, String temo) {
            this.ten = temp;
            super.ten = temo;
        }

    }

    public static void main(String[] args) {
        Cha cha = new Cha();
        Con con = new Con();

        System.out.println(con.ten);
        con.pr();
        cha.pr();
    }
}
