package topwords

import scala.io.Source
import scala.util.Using
import java.io.IOException

object Main:
  case class WindowState(window: List[String], frequencyMap: Map[String, Int])

  def parseArgs(args: Array[String]): (Int, Int, Int) =
    def parseRec(index: Int, cloudSize: Int, minLength: Int, windowSize: Int): (Int, Int, Int) =
      if index >= args.length then
        (cloudSize, minLength, windowSize)
      else
        args(index) match
          case "--cloudSize" | "-c" =>
            if index + 1 < args.length then
              parseRec(index + 2, args(index + 1).toInt, minLength, windowSize)
            else
              parseRec(index + 1, cloudSize, minLength, windowSize)
          case "--minLength" | "-m" =>
            if index + 1 < args.length then
              parseRec(index + 2, cloudSize, args(index + 1).toInt, windowSize)
            else
              parseRec(index + 1, cloudSize, minLength, windowSize)
          case "--windowSize" | "-w" =>
            if index + 1 < args.length then
              parseRec(index + 2, cloudSize, minLength, args(index + 1).toInt)
            else
              parseRec(index + 1, cloudSize, minLength, windowSize)
          case _ =>
            parseRec(index + 1, cloudSize, minLength, windowSize)
    parseRec(0, 10, 1, 100)

  def updateWindow(state: WindowState, word: String, windowSize: Int): WindowState =
    val newWindow = state.window :+ word
    val updatedFreq = state.frequencyMap.updated(word, state.frequencyMap.getOrElse(word, 0) + 1)

    if newWindow.size > windowSize then
      val oldWord = newWindow.head
      val oldCount = updatedFreq(oldWord) - 1
      val finalFreq = if oldCount <= 0 then updatedFreq - oldWord else updatedFreq.updated(oldWord, oldCount)
      WindowState(newWindow.tail, finalFreq)
    else
      WindowState(newWindow, updatedFreq)

  def getTopWords(state: WindowState, cloudSize: Int): List[(String, Int)] =
    state.frequencyMap
      .toList
      .sortBy(-_._2)
      .take(cloudSize)

  def main(args: Array[String]): Unit =
    val (cloudSize, minLength, windowSize) = parseArgs(args)

    System.err.println(s"[main] DEBUG topwords.Main - cloudSize=$cloudSize minLength=$minLength windowSize=$windowSize")

    try
      Using(Source.stdin) { source =>
        val words = source.getLines()
          .flatMap(line => line.split("\\s+").filter(_.nonEmpty))
          .filter(_.length >= minLength)

        words.scanLeft(WindowState(List(), Map[String, Int]())) { case (state, word) =>
          updateWindow(state, word, windowSize)
        }
          .drop(1)
          .filter(_.window.size == windowSize)
          .foreach { state =>
            val topWords = getTopWords(state, cloudSize)
            val formatted = topWords
              .map { case (word, freq) => s"$word: $freq" }
              .mkString(" ")
            System.out.println(formatted)
            System.out.flush()
          }
      }: Unit
    catch
      case _: IOException =>
        // Handle SIGPIPE and other IO errors gracefully