package ionsap.ds

import scala.reflect._

object ConcBuffer {

  import ConcTree._

  class Chunk[T](val data: Array[T], val size: Int) extends Conc[T] {
    def level = 0

    def left: Conc[T] = Empty

    def right: Conc[T] = Empty
  }

  def appendLeaf[T](t1: Conc[T], t2: Chunk[T]): Conc[T] = t1 match {
    case Empty => t2
    case t: Chunk[T] => new <>(t, t2)
    case <>(_, _) => new Append(t1, t2)
    case t: Append[T] => append(t, t2)
  }

  class ConcBuffer[T: ClassTag](val size: Int, private var conc: Conc[T]) {
    private var chunk: Array[T] = new Array[T](size)
    private var currSize = 0

    def +=(elem: T): ConcBuffer[T] = {
      if (currSize >= size) appendConcTree()

      chunk(currSize) = elem
      currSize += 1
      this
    }

    private def eliminateAppendNode(t1: Conc[T], t2: Conc[T]): Conc[T] = t1 match {
      case Append(left, right) => eliminateAppendNode(left, right <> t2)
      case _ => t1 <> t2
    }

    def result: Conc[T] = {
      conc = eliminateAppendNode(conc, new Chunk(chunk, currSize))
      conc
    }

    def combine(that: ConcBuffer[T]): ConcBuffer[T] = {
      val combinedConc =
        if (this.result.size >= that.result.size) this.result <> that.result
        else that.result <> this.result
      new ConcBuffer[T](size, combinedConc)
    }

    private def appendConcTree(): Unit = {
      conc += new Chunk(chunk, currSize)
      chunk = new Array(size)
      currSize = 0
    }
  }

}


object ExampleMain {

  import ConcBuffer._

  def main(args: Array[String]): Unit = {
    val xs = for (i <- 1 to 10000000) yield i

    xs.par.aggregate(new ConcBuffer[Int](100, ConcTree.Empty))(_ += _, _ combine _)
  }

}