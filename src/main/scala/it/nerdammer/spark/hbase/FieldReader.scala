package it.nerdammer.spark.hbase

import org.apache.hadoop.hbase.util.Bytes


/**
 * Created by Nicola Ferraro on 10/01/15.
 */
trait FieldReader[T] extends Serializable {
  def map(data: HBaseDataHolder): T
}

trait SingleColumnFieldReader[T] extends FieldReader[T] {

  def map(data: HBaseDataHolder): T =
    if(data.columns.size!=1) throw new IllegalArgumentException(s"Unexpected number of columns: expected 1, returned ${data.columns.size}")
    else columnMapWithOption(data.columns.head)

  def columnMapWithOption(cols: Option[Array[Byte]]): T
}

trait SingleColumnConcreteFieldReader[T] extends SingleColumnFieldReader[T] {

  def columnMapWithOption(cols: Option[Array[Byte]]) =
    if(cols.nonEmpty) columnMap(cols.get)
    else throw new IllegalArgumentException("Null value assigned to concrete class. Use Option[T] instead")

  def columnMap(cols: Array[Byte]): T
}

trait TupleFieldReader[T <: Product] extends FieldReader[T] {

  val n: Int

  def map(data: HBaseDataHolder): T =
    if(data.columns.size==n)
      tupleMap(data)
    else if(data.columns.size==n-1)
      tupleMap(new HBaseDataHolder(data.rowKey, Some(Bytes.toBytes(data.rowKey)) :: data.columns.toList))
    else
      throw new IllegalArgumentException(s"Unexpected number of columns: expected $n or ${n-1}, returned ${data.columns.size}")

  def tupleMap(data: HBaseDataHolder): T
}

trait FieldReaderConversions extends Serializable {

  // Simple types

  implicit def intReader: FieldReader[Int] = new SingleColumnConcreteFieldReader[Int] {
    def columnMap(cols: Array[Byte]): Int = Bytes.toInt(cols)
  }

  implicit def longReader: FieldReader[Long] = new SingleColumnConcreteFieldReader[Long] {
    def columnMap(cols: Array[Byte]): Long = Bytes.toLong(cols)
  }

  implicit def shortReader: FieldReader[Short] = new SingleColumnConcreteFieldReader[Short] {
    def columnMap(cols: Array[Byte]): Short = Bytes.toShort(cols)
  }

  implicit def doubleReader: FieldReader[Double] = new SingleColumnConcreteFieldReader[Double] {
    def columnMap(cols: Array[Byte]): Double = Bytes.toDouble(cols)
  }

  implicit def floatReader: FieldReader[Float] = new SingleColumnConcreteFieldReader[Float] {
    def columnMap(cols: Array[Byte]): Float = Bytes.toFloat(cols)
  }

  implicit def booleanReader: FieldReader[Boolean] = new SingleColumnConcreteFieldReader[Boolean] {
    def columnMap(cols: Array[Byte]): Boolean = Bytes.toBoolean(cols)
  }

  implicit def bigDecimalReader: FieldReader[BigDecimal] = new SingleColumnConcreteFieldReader[BigDecimal] {
    def columnMap(cols: Array[Byte]): BigDecimal = Bytes.toBigDecimal(cols)
  }

  implicit def stringReader: FieldReader[String] = new SingleColumnConcreteFieldReader[String] {
    def columnMap(cols: Array[Byte]): String = Bytes.toString(cols)
  }

  // Options

  implicit def optionReader[T](implicit c: FieldReader[T]): FieldReader[Option[T]] = new FieldReader[Option[T]] {
    def map(data: HBaseDataHolder): Option[T] =
      if(data.columns.size!=1) throw new IllegalArgumentException(s"Unexpected number of columns: expected 1, returned ${data.columns.size}")
      else {
        if(!classOf[SingleColumnConcreteFieldReader[T]].isAssignableFrom(c.getClass)) throw new IllegalArgumentException("Option[T] can be used only with primitive values")
        if(data.columns.head.nonEmpty) Some(c.map(data))
        else None
      }
  }

  // Tuples

  implicit def tuple2Reader[T1, T2](implicit m1: FieldReader[T1], m2: FieldReader[T2]): FieldReader[(T1, T2)] = new TupleFieldReader[(T1, T2)] {

    val n = 2

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      (m1.map(h1), m2.map(h2))
    }
  }

  implicit def tuple3Reader[T1, T2, T3](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3]): FieldReader[(T1, T2, T3)] = new TupleFieldReader[(T1, T2, T3)] {

    val n = 3

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      (m1.map(h1), m2.map(h2), m3.map(h3))
    }
  }

  implicit def tuple4Reader[T1, T2, T3, T4](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4]): FieldReader[(T1, T2, T3, T4)] = new TupleFieldReader[(T1, T2, T3, T4)] {

    val n = 4

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4))
    }
  }

  implicit def tuple5Reader[T1, T2, T3, T4, T5](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4], m5: FieldReader[T5]): FieldReader[(T1, T2, T3, T4, T5)] = new TupleFieldReader[(T1, T2, T3, T4, T5)] {

    val n = 5

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      val h5 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(4).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4), m5.map(h5))
    }
  }

  implicit def tuple6Reader[T1, T2, T3, T4, T5, T6](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4], m5: FieldReader[T5], m6: FieldReader[T6]): FieldReader[(T1, T2, T3, T4, T5, T6)] = new TupleFieldReader[(T1, T2, T3, T4, T5, T6)] {

    val n = 6

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      val h5 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(4).head))
      val h6 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(5).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4), m5.map(h5), m6.map(h6))
    }
  }

  implicit def tuple7Reader[T1, T2, T3, T4, T5, T6, T7](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4], m5: FieldReader[T5], m6: FieldReader[T6], m7: FieldReader[T7]): FieldReader[(T1, T2, T3, T4, T5, T6, T7)] = new TupleFieldReader[(T1, T2, T3, T4, T5, T6, T7)] {

    val n = 7

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      val h5 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(4).head))
      val h6 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(5).head))
      val h7 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(6).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4), m5.map(h5), m6.map(h6), m7.map(h7))
    }
  }

  implicit def tuple8Reader[T1, T2, T3, T4, T5, T6, T7, T8](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4], m5: FieldReader[T5], m6: FieldReader[T6], m7: FieldReader[T7], m8: FieldReader[T8]): FieldReader[(T1, T2, T3, T4, T5, T6, T7, T8)] = new TupleFieldReader[(T1, T2, T3, T4, T5, T6, T7, T8)] {

    val n = 8

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      val h5 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(4).head))
      val h6 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(5).head))
      val h7 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(6).head))
      val h8 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(7).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4), m5.map(h5), m6.map(h6), m7.map(h7), m8.map(h8))
    }
  }

  implicit def tuple9Reader[T1, T2, T3, T4, T5, T6, T7, T8, T9](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4], m5: FieldReader[T5], m6: FieldReader[T6], m7: FieldReader[T7], m8: FieldReader[T8], m9: FieldReader[T9]): FieldReader[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] = new TupleFieldReader[(T1, T2, T3, T4, T5, T6, T7, T8, T9)] {

    val n = 9

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      val h5 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(4).head))
      val h6 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(5).head))
      val h7 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(6).head))
      val h8 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(7).head))
      val h9 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(8).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4), m5.map(h5), m6.map(h6), m7.map(h7), m8.map(h8), m9.map(h9))
    }
  }

  implicit def tuple10Reader[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](implicit m1: FieldReader[T1], m2: FieldReader[T2], m3: FieldReader[T3], m4: FieldReader[T4], m5: FieldReader[T5], m6: FieldReader[T6], m7: FieldReader[T7], m8: FieldReader[T8], m9: FieldReader[T9], m10: FieldReader[T10]): FieldReader[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] = new TupleFieldReader[(T1, T2, T3, T4, T5, T6, T7, T8, T9, T10)] {

    val n = 10

    def tupleMap(data: HBaseDataHolder) = {
      val h1 = new HBaseDataHolder(data.rowKey, Seq(data.columns.head))
      val h2 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(1).head))
      val h3 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(2).head))
      val h4 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(3).head))
      val h5 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(4).head))
      val h6 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(5).head))
      val h7 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(6).head))
      val h8 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(7).head))
      val h9 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(8).head))
      val h10 = new HBaseDataHolder(data.rowKey, Seq(data.columns.drop(9).head))
      (m1.map(h1), m2.map(h2), m3.map(h3), m4.map(h4), m5.map(h5), m6.map(h6), m7.map(h7), m8.map(h8), m9.map(h9), m10.map(h10))
    }
  }

}


