package module2

import module2.type_classes.JsValue.{JsNull, JsNumber, JsString}


object type_classes {

  sealed trait JsValue
  object JsValue {
    final case class JsObject(get: Map[String, JsValue]) extends JsValue
    final case class JsString(get: String) extends JsValue
    final case class JsNumber(get: Double) extends JsValue
    final case object JsNull extends JsValue
  }

  // 1
  trait JsonWriter[T]{
    def write(v: T): JsValue
  }

  object JsonWriter {
    def apply[T](implicit ev: JsonWriter[T]): JsonWriter[T] = ev

    def from[T](f: T => JsValue): JsonWriter[T] = ???

    // 2
    implicit val strJsonWriter = from[String](JsString)

    implicit val intJsonWriter = from[Int](JsNumber(_))

    implicit def optToJsValue[A](implicit ev: JsonWriter[A]): JsonWriter[Option[A]] =
      from[Option[A]] {
        case Some(value) => ev.write(value)
        case None => JsNull
      }

  }

  // 3
  def toJson[T: JsonWriter](v: T): JsValue = {
    JsonWriter[T].write(v)
  }

  implicit class JsonSyntax[T](v: T){
    def toJson(implicit ev: JsonWriter[T]): JsValue = ev.write(v)
  }

  toJson("foo")
  toJson(12)
  toJson(Option(10))
  toJson(Option("bar"))

  "foo".toJson
  12.toJson
  Option("bar").toJson
  user.toJson


  // 1
  trait Ordering[T]{
    def less(a: T, b: T): Boolean
  }

  object Ordering{

    def from[A](f: (A, A) => Boolean): Ordering[A] = new Ordering[A] {
      override def less(a: A, b: A): Boolean = f(a, b)
    }

    // 2
    implicit val intOrdering = from[Int]((a, b) => a < b)

    implicit val strOrdering = from[String]((a, b) => a < b)


  }


  // 3
  def mx[T](a: T, b: T)(implicit ev: Ordering[T]): T =
    if(ev.less(a, b)) b else a

  mx(5, 10)
  mx("ab", "foo")

  // 1

  trait Eq[T]{
    def ===(a: T, b: T): Boolean
  }

  object Eq{
    def apply[T](implicit ev: Eq[T]): Eq[T] = ev

    implicit val eqStr: Eq[String] = new Eq[String] {
      override def ===(a: String, b: String): Boolean = a == b
    }
  }

  implicit class EqSyntax[T](a: T){
    def ===(b: T)(implicit eq: Eq[T]): Boolean = eq.===(a, b)
  }


  val result = List("a", "b", "c").filter(str => str === "a")

}
