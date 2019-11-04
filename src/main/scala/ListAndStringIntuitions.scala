object ListAndStringIntuitions extends App {
  {
    val ab   = List("a", "b")
    val cd   = List("c", "d")
    val abcd = ab.appendedAll(cd)
    assert(abcd == List("a", "b", "c", "d"))

    assert(ab.size == 2)
    assert(cd.size == 2)
    assert(abcd.size == 4)
    println(abcd)
  }

  {
    val ab   = "ab"
    val cd   = "cd"
    val abcd = ab.concat(cd)
    assert(abcd == "abcd")

    assert(ab.length == 2)
    assert(cd.length == 2)
    assert(abcd.length == 4)
    println(abcd)
  }

  {
    val abcd = List("a", "b", "c", "d")
    val bc   = abcd.slice(1, 3)
    assert(bc == List("b", "c"))

    println(bc)
  }

  {
    val abcd = "abcd"
    val bc   = abcd.substring(1, 3)
    assert(bc == "bc")

    println(bc)
  }
}
