object ShoppingCart {
  def getDiscountPercentage(items: List[String]): Int = {
    if (items.contains("Book")) {
      5
    } else {
      0
    }
  }
}

object ch02_ShoppingCartDiscountsScala extends App {
  assert(ShoppingCart.getDiscountPercentage(List.empty) == 0)

  val justApple = List("Apple")
  assert(ShoppingCart.getDiscountPercentage(justApple) == 0)

  val appleAndBook = List("Apple", "Book")
  assert(ShoppingCart.getDiscountPercentage(appleAndBook) == 5)
}
