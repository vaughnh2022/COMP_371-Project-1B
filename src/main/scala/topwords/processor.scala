package topwords

import scala.collection.mutable

class Processor(
  cloudSize: Int,
  minLength: Int,
  windowSize: Int,
  observer: WordCloudObserver
) {
  private val window = mutable.Queue[String]()
  private val frequencyMap = mutable.Map[String, Int]()

  def process(word: String): Unit = {
    // Ignore if length < minLength
    if (word.length < minLength) {
      return
    }

    // Add word to window
    window.enqueue(word)

    // Increment frequency
    frequencyMap(word) = frequencyMap.getOrElse(word, 0) + 1

    // If window exceeds windowSize
    if (window.size > windowSize) {
      val oldestWord = window.dequeue()
      
      // Decrement its frequency
      frequencyMap(oldestWord) -= 1
      
      // Remove from map if count becomes 0
      if (frequencyMap(oldestWord) == 0) {
        frequencyMap.remove(oldestWord): Unit
      }
    }

    // If window is full
    if (window.size == windowSize) {
      // Compute top cloudSize
      val topWords = frequencyMap
        .toList
        .sortBy(-_._2)  // Sort descending by frequency
        .take(cloudSize)
      
      // Call observer.update with list of tuples
      observer.update(topWords)
    }
  }
}
