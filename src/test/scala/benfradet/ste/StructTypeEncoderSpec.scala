package benfradet.ste

import org.apache.spark.sql.types._
import org.scalatest.{FlatSpec, Matchers}
import shapeless.test.illTyped

class StructTypeEncoderSpec extends FlatSpec with Matchers {
  import StructTypeEncoder._

  "A StructTypeEncoder" should "deal with the supported primitive types" in {
    case class Foo(a: Array[Byte], b: Boolean, c: Byte, d: java.sql.Date, e: BigDecimal, f: Double, 
      g: Float, h: Int, i: Long, j: Short, k: String, l: java.sql.Timestamp)
    StructTypeEncoder[Foo].encode shouldBe StructType(
      StructField("a", BinaryType) ::
      StructField("b", BooleanType) ::
      StructField("c", ByteType) ::
      StructField("d", DateType) ::
      StructField("e", DecimalType.SYSTEM_DEFAULT) ::
      StructField("f", DoubleType) ::
      StructField("g", FloatType) ::
      StructField("h", IntegerType) ::
      StructField("i", LongType) ::
      StructField("j", ShortType) ::
      StructField("k", StringType) ::
      StructField("l", TimestampType) :: Nil
    )
  }

  it should "work with Unit" ignore {
    case class A(a: Unit)
    StructTypeEncoder[A].encode shouldBe StructType(StructField("a", NullType) :: Nil)
  }

  it should "deal with the supported combinators" in {
    case class Foo(a: Seq[Int], b: List[Int], c: Set[Int], d: Vector[Int], e: Array[Int])
    StructTypeEncoder[Foo].encode shouldBe StructType(
      StructField("a", ArrayType(IntegerType)) ::
      StructField("b", ArrayType(IntegerType)) ::
      StructField("c", ArrayType(IntegerType)) ::
      StructField("d", ArrayType(IntegerType)) ::
      StructField("e", ArrayType(IntegerType)) :: Nil
    )
    case class Bar(a: Map[Int, String])
    StructTypeEncoder[Bar].encode shouldBe
      StructType(StructField("a", MapType(IntegerType, StringType)) :: Nil)
  }

  it should "deal with nested products" in {
    case class Foo(a: Int)
    case class Bar(f: Foo, b: Int)
    StructTypeEncoder[Bar].encode shouldBe StructType(
      StructField("f", StructType(StructField("a", IntegerType) :: Nil)) ::
      StructField("b", IntegerType) :: Nil
    )
  }

  it should "deal with tuples" in {
    case class Foo(a: (String, Int))
    StructTypeEncoder[Foo].encode shouldBe StructType(
      StructField("a", StructType(
        StructField("_1", StringType) ::
        StructField("_2", IntegerType) :: Nil
      )) :: Nil
    )
    StructTypeEncoder[(String, Int)].encode shouldBe StructType(
      StructField("_1", StringType) ::
      StructField("_2", IntegerType) :: Nil
    )
  }

  it should "not compile with something that is not a product" in {
    class Foo(a: Int)
    illTyped { """StructTypeEncoder[Foo].encode""" }
  }
}