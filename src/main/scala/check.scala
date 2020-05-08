object check {
  def apply[A](result: A): Assert[A] = {
    println(result)
    new Assert(result)
  }

  class Assert[A](result: A) {
    def expect(expected: A): A = {
      assert(result == expected)
      result
    }

    def expect(checkResult: A => Boolean): A = {
      assert(checkResult(result))
      result
    }
  }
}
