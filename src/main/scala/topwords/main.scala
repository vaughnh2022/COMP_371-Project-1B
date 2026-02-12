package topwords

import scala.io.Source
import scala.util.Using
import java.io.IOException

object Main {
  def main(args: Array[String]): Unit = {
    // Parse CLI arguments
    var cloudSize = 10
    var minLength = 1
    var windowSize = 100
    
    var i = 0
    while (i < args.length) {
      args(i) match {
        case "--cloudSize" | "-c" =>
          if (i + 1 < args.length) {
            cloudSize = args(i + 1).toInt
            i += 2
          } else {
            i += 1
          }
        case "--minLength" | "-m" =>
          if (i + 1 < args.length) {
            minLength = args(i + 1).toInt
            i += 2
          } else {
            i += 1
          }
        case "--windowSize" | "-w" =>
          if (i + 1 < args.length) {
            windowSize = args(i + 1).toInt
            i += 2
          } else {
            i += 1
          }
        case _ =>
          i += 1
      }
    }
    
    val observer = new ConsoleObserver()
    val processor = new Processor(cloudSize, minLength, windowSize, observer)
    
    System.err.println(s"[main] DEBUG topwords.Main - cloudSize=$cloudSize minLength=$minLength windowSize=$windowSize")
    
    try {
      Using(Source.stdin) { source =>
        for (line <- source.getLines()) {
          val words = line.split("\\s+").filter(_.nonEmpty)
          for (word <- words) {
            processor.process(word)
          }
        }
      }
    } catch {
      case _: IOException =>
        // Handle SIGPIPE and other IO errors gracefully
    }
  }
}

