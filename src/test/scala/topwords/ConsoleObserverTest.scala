package topwords

import org.scalatest.funsuite.AnyFunSuite
import java.io.{ByteArrayOutputStream, PrintStream}

class ConsoleObserverTest extends AnyFunSuite {
  
  test("Format single word-frequency pair") {
    val observer = new ConsoleObserver()
    
    // Capture stdout
    val outCapture = new ByteArrayOutputStream()
    val oldOut = System.out
    System.setOut(new PrintStream(outCapture))
    
    try {
      observer.update(List(("hello", 5)))
      System.out.flush()
      val output = outCapture.toString.trim
      assert(output == "hello: 5")
    } finally {
      System.setOut(oldOut)
    }
  }
  
  test("Format multiple word-frequency pairs with spaces") {
    val observer = new ConsoleObserver()
    
    val outCapture = new ByteArrayOutputStream()
    val oldOut = System.out
    System.setOut(new PrintStream(outCapture))
    
    try {
      observer.update(List(
        ("hello", 5),
        ("world", 3),
        ("test", 1)
      ))
      System.out.flush()
      val output = outCapture.toString.trim
      assert(output == "hello: 5 world: 3 test: 1")
    } finally {
      System.setOut(oldOut)
    }
  }
  
  test("Handle empty list") {
    val observer = new ConsoleObserver()
    
    val outCapture = new ByteArrayOutputStream()
    val oldOut = System.out
    System.setOut(new PrintStream(outCapture))
    
    try {
      observer.update(List())
      System.out.flush()
      val output = outCapture.toString.trim
      assert(output == "")
    } finally {
      System.setOut(oldOut)
    }
  }
  
  test("Preserve order of input list") {
    val observer = new ConsoleObserver()
    
    val outCapture = new ByteArrayOutputStream()
    val oldOut = System.out
    System.setOut(new PrintStream(outCapture))
    
    try {
      observer.update(List(
        ("zebra", 10),
        ("apple", 8),
        ("monkey", 5)
      ))
      System.out.flush()
      val output = outCapture.toString.trim
      assert(output == "zebra: 10 apple: 8 monkey: 5")
    } finally {
      System.setOut(oldOut)
    }
  }
  
  test("Handle high frequency numbers") {
    val observer = new ConsoleObserver()
    
    val outCapture = new ByteArrayOutputStream()
    val oldOut = System.out
    System.setOut(new PrintStream(outCapture))
    
    try {
      observer.update(List(
        ("common", 9999),
        ("rare", 1)
      ))
      System.out.flush()
      val output = outCapture.toString.trim
      assert(output == "common: 9999 rare: 1")
    } finally {
      System.setOut(oldOut)
    }
  }
  
  test("Print exactly one line") {
    val observer = new ConsoleObserver()
    
    val outCapture = new ByteArrayOutputStream()
    val oldOut = System.out
    System.setOut(new PrintStream(outCapture))
    
    try {
      observer.update(List(
        ("word1", 1),
        ("word2", 2),
        ("word3", 3)
      ))
      System.out.flush()
      val output = outCapture.toString
      val lineCount = output.split("\n").length
      assert(lineCount == 1 || (lineCount == 2 && output.endsWith("\n")))
    } finally {
      System.setOut(oldOut)
    }
  }
}
