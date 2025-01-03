/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Utils;

import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author ssydoruk
 */
public class StringUtilsTest {

    static class Person {

        public Person(String name, int age) {
            this.name = name;
            this.age = age;
        }
        String name;
        int age;

    };

    static class Student extends Person {

        public Student(String course, String name, int age) {
            super(name, age);
            this.course = course;
        }
        String course;
    }

    public StringUtilsTest() {
    }

    static StringUtilsTest me;
    private Student steve;

    public void setSteve(Student steve) {
        this.steve = steve;
    }

    @BeforeAll
    public static void setUpClass() {
        me = new StringUtilsTest();
        me.setSteve(new Student("math", "Steve", 21));
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of matching method, of class StringUtils.
     */
    @org.junit.jupiter.api.Test
    public void testMatching() {
        System.out.println("matching");
        Pattern ptSection = Pattern.compile("^aa$");
        assertEquals(true, StringUtils.matching(ptSection, "aa"));
        assertEquals(false, StringUtils.matching(ptSection, "aaa"));
        assertEquals(false, StringUtils.matching(ptSection, null));
        assertEquals(false, StringUtils.matching(null, null));
    }

    /**
     * Test of toJson method, of class StringUtils.
     */
    @org.junit.jupiter.api.Test
    public void testToJson_3args() {
        System.out.println("toJson");
        Object obj = null;
        boolean prettyPrint = false;
        boolean disableInnerClassSerialization = false;
        String expResult = "";
        String result = StringUtils.toJson(obj, prettyPrint, disableInnerClassSerialization);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.

    }

    /**
     * Test of toJson method, of class StringUtils.
     */
    @org.junit.jupiter.api.Test
    public void testToJson_Object() {
        Object obj = new Student("math", "Steve", 21);
        String expResult = "";
        String result = StringUtils.toJson(obj);
        System.out.println(result);
        assertEquals("""
                     {
                       "course": "math",
                       "name": "Steve",
                       "age": 21
                     }""", result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of CharOccurences method, of class StringUtils.
     */
    @org.junit.jupiter.api.Test
    public void testCharOccurences() {
        System.out.println("CharOccurences");
        StringBuilder s = null;
        char c = ' ';
        int expResult = 0;
        int result = StringUtils.CharOccurences(s, c);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of CountStrings method, of class StringUtils.
     */
    @org.junit.jupiter.api.Test
    public void testCountStrings() {
        System.out.println("CountStrings");
        StringBuilder sipBuf = null;
        String search = "";
        int expResult = 0;
        int result = StringUtils.CountStrings(sipBuf, search);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of isNumeric method, of class StringUtils.
     */
    @org.junit.jupiter.api.Test
    public void testIsNumeric() {
        System.out.println("isNumeric");
        String s = "aa";
        boolean expResult = false;
        boolean result = StringUtils.isNumeric(s);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

}
