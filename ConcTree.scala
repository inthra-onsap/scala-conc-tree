package ionsap.ds

import scala.annotation._


object ConcTree {

  sealed trait Conc[+T] {
    def level: Int

    def size: Int

    def left: Conc[T]

    def right: Conc[T]

    def <>[S >: T](that: Conc[S]): Conc[S] = {
      if (this == Empty) that
      else if (that == Empty) this
      else concat(this, that)
    }

    def +=[S >: T](elem: S): Conc[S] = {
      this <> Single(elem)
    }
  }

  case object Empty extends Conc[Nothing] {
    def level = 0

    def size = 0

    def left = throw new Error("ConcTree.Empty")

    def right = throw new Error("ConcTree.Empty")
  }

  case class Single[T](val x: T) extends Conc[T] {
    def level = 0

    def size = 1

    def left: Conc[T] = Empty

    def right: Conc[T] = Empty
  }

  case class <>[T](left: Conc[T], right: Conc[T]) extends Conc[T] {
    val level = 1 + math.max(left.level, right.level)
    val size = left.size + right.size
  }

  case class Append[+T](left: Conc[T], right: Conc[T]) extends Conc[T] {
    val level = 1 + math.max(left.level, right.level)
    val size = left.size + right.size
  }

  def concat[T](t1: Conc[T], t2: Conc[T]): Conc[T] = {
    val diff = t2.level - t1.level

    /**
      * 1) If the height of 2 trees are different within range of 1 (T2.level - T1.level)
      * then create new <> node to concatenate T1 and T2 together.
      */
    if (diff >= -1 && diff <= 1) {
      new <>(t1, t2)
    } else {
      /**
        * If the height of 2 trees are different greater than 1.
        */
      if (t1.left.level >= t1.right.level) {
        /**
          * case 1) T1's left sub-tree has height greater than or equal to T1's right sub-tree
          * then recursive concatenate the T1's right sub-tree with T2 to get T3. Finally concatenate T1.left and T3 together.
          */
        val t3 = concat(t1.right, t2)
        new <>(t1.left, t3)
      } else {
        /**
          * case 2) T1's left sub-tree has height lower than T1's right sub-tree then split T1 into 3 sub-trees as following.
          * a) T1.left
          * b) T1.right.left
          * c) T1.right.right
          */
        /**
          * Recursive concatenate T1.right.right and T2 together to get T3.
          */
        val t3 = concat(t1.right.right, t2)
        val t3Diff = t3.level - t1.level

        if (t3Diff >= -1 && t3Diff <= 1) {
          /**
            * If T1 and T3(T1.level - T3.level) has height difference between 0 and 1
            * then concatenate T1.left and T1.right.left together to get T4. Finally concatenate T4 and T3.
            */
          val t4 = new <>(t1.left, t1.right.left)
          new <>(t4, t3)
        } else {
          /**
            * Otherwise concatenate T1.right.left and T3 together to get T4. Finally concatenate T1.left and T4.
            */
          val t4 = new <>(t1.right.left, t3)
          new <>(t1.left, t4)
        }
      }
    }
  }

  @tailrec
  private def append[T](t1: Append[T], t2: Conc[T]): Conc[T] = {
    if (t1.right.level > t2.level) new Append(t1, t2)
    else {
      val t3 = new <>(t1.right, t2)
      t1.left match {
        case left: Append[T] => append(left, t3)
        case left if left.level <= t3.level => left <> t3
        case left => new Append(left, t3)
      }
    }
  }

  def appendLeaf[T](t1: Conc[T], t2: Single[T]): Conc[T] = t1 match {
    case Empty => t2
    case t: Single[T] => new <>(t, t2)
    case <>(_, _) => new Append(t1, t2)
    case t: Append[T] => append(t, t2)
  }
}

