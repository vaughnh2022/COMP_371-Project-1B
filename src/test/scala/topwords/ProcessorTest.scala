package topwords

import org.scalatest.funsuite.AnyFunSuite
import scala.collection.mutable

class ProcessorTest extends AnyFunSuite {
  
  class MockObserver extends WordCloudObserver {
    var lastStats: List[(String, Int)] = List()
    var updateCallCount: Int = 0
    
    def update(stats: List[(String, Int)]): Unit = {
      lastStats = stats
      updateCallCount += 1
    }
  }
  
  test("Ignore words shorter than minLength") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 5, minLength = 3, windowSize = 10, observer)
    
    processor.process("a")
    processor.process("ab")
    processor.process("abc")
    
    assert(observer.updateCallCount == 0, "Observer should not be called for short words")
  }
  
  test("Add word to window and increment frequency") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 5, minLength = 1, windowSize = 5, observer)
    
    processor.process("hello")
    processor.process("hello")
    processor.process("world")
    processor.process("hello")
    processor.process("world")
    
    assert(observer.updateCallCount == 1, "Observer should be called when window becomes full")
    assert(observer.lastStats.length == 2, "Should have 2 unique words")
    assert(observer.lastStats(0) == ("hello", 3), "hello should have frequency 3")
    assert(observer.lastStats(1) == ("world", 2), "world should have frequency 2")
  }
  
  test("Sliding window removes oldest word") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 10, minLength = 1, windowSize = 3, observer)
    
    processor.process("one")
    processor.process("two")
    processor.process("three")
    // Window is now full: [one, two, three]
    
    val firstUpdate = observer.lastStats.toMap
    assert(firstUpdate("one") == 1)
    assert(firstUpdate("two") == 1)
    assert(firstUpdate("three") == 1)
    
    processor.process("four")
    // Window should now be: [two, three, four], one should be removed
    
    val secondUpdate = observer.lastStats.toMap
    assert(!secondUpdate.contains("one"), "one should be removed from map")
    assert(secondUpdate("two") == 1)
    assert(secondUpdate("three") == 1)
    assert(secondUpdate("four") == 1)
  }
  
  test("Remove word from frequency map when count becomes 0") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 10, minLength = 1, windowSize = 2, observer)
    
    processor.process("a")
    processor.process("b")
    // Window: [a, b]
    
    processor.process("c")
    // Window: [b, c], a removed
    
    val stats = observer.lastStats.toMap
    assert(!stats.contains("a"), "a should be completely removed from map")
    assert(stats.contains("b"))
    assert(stats.contains("c"))
  }
  
  test("Sort words descending by frequency") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 10, minLength = 1, windowSize = 6, observer)
    
    processor.process("apple")
    processor.process("apple")
    processor.process("banana")
    processor.process("cherry")
    processor.process("cherry")
    processor.process("cherry")
    // Window full with: apple(2), banana(1), cherry(3)
    
    // Check that results are sorted by frequency descending
    assert(observer.lastStats(0)._1 == "cherry" && observer.lastStats(0)._2 == 3)
    assert(observer.lastStats(1)._1 == "apple" && observer.lastStats(1)._2 == 2)
    assert(observer.lastStats(2)._1 == "banana" && observer.lastStats(2)._2 == 1)
  }
  
  test("Only return top cloudSize words") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 2, minLength = 1, windowSize = 5, observer)
    
    processor.process("a")
    processor.process("b")
    processor.process("c")
    processor.process("a")
    processor.process("b")
    // Window full with a(2), b(2), c(1)
    
    assert(observer.lastStats.length == 2, "Should only return top 2 words")
    assert(observer.lastStats.forall(_._2 >= 1), "All returned words should have frequency >= 1")
  }
  
  test("Observer not called until window is full") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 5, minLength = 1, windowSize = 5, observer)
    
    processor.process("word1")
    assert(observer.updateCallCount == 0)
    
    processor.process("word2")
    assert(observer.updateCallCount == 0)
    
    processor.process("word3")
    assert(observer.updateCallCount == 0)
    
    processor.process("word4")
    assert(observer.updateCallCount == 0)
    
    processor.process("word5")
    assert(observer.updateCallCount == 1, "Observer should be called when window becomes full")
  }
  
  test("Duplicate words increase frequency") {
    val observer = new MockObserver()
    val processor = new Processor(cloudSize = 10, minLength = 1, windowSize = 3, observer)
    
    processor.process("test")
    processor.process("test")
    processor.process("test")
    // Window full with test(3)
    
    assert(observer.lastStats.length == 1)
    assert(observer.lastStats(0) == ("test", 3))
  }
}
