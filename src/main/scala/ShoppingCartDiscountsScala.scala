object ShoppingCartScala {
  def getDiscountPercentage(items: List[String]): Int = {
    if (items.contains("Book")) {
      5
    } else {
      0
    }
  }
}

object ShoppingCartDiscountsScala extends App {
  assert(ShoppingCartScala.getDiscountPercentage(List.empty) == 0)

  val justApple = List("Apple")
  assert(ShoppingCartScala.getDiscountPercentage(justApple) == 0)

  val appleAndBook = List("Apple", "Book")
  assert(ShoppingCartScala.getDiscountPercentage(appleAndBook) == 5)
}
