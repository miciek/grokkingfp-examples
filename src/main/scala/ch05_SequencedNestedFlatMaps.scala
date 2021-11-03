object ch05_SequencedNestedFlatMaps extends App {
  val s = List(1, 2, 3)
    .flatMap(a => List(a * 2))
    .flatMap(b => List(b, b + 10))
  check(s).expect(List(2, 12, 4, 14, 6, 16))

  val n = List(1, 2, 3)
    .flatMap(a =>
      List(a * 2).flatMap(b => List(b, b + 10))
    )
  check(s).expect(n)
}
