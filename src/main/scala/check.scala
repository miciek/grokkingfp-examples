object check {
  def apply[A](result: A)(expect: A): A = {
    println(result)
    assert(result == expect)
    result
  }
}
