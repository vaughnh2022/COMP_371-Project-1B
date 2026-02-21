package topwords

import scala.collection.mutable

class Processor(
  cloudSize: Int,
  minLength: Int,
  windowSize: Int,
  observer: WordCloudObserver
):
  private val window = mutable.Queue[String]()
  private val frequencyMap = mutable.Map[String, Int]()

  def process(word: String): Unit =
    if word.length < minLength then return

    window.enqueue(word)
    frequencyMap(word) = frequencyMap.getOrElse(word, 0) + 1

    if window.size > windowSize then
      val oldestWord = window.dequeue()
      frequencyMap(oldestWord) -= 1
      if frequencyMap(oldestWord) == 0 then
        frequencyMap.remove(oldestWord): Unit

    if window.size == windowSize then
      val topWords = frequencyMap
        .toList
        .sortBy(-_._2)
        .take(cloudSize)
      observer.update(topWords)