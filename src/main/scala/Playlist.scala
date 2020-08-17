case class User(name: String)   extends AnyVal
case class Artist(name: String) extends AnyVal

case class Song(artist: Artist, title: String)

sealed trait MusicGenre
case object House  extends MusicGenre
case object Funk   extends MusicGenre
case object HipHop extends MusicGenre

sealed trait PlaylistKind
case class CuratedByUser(user: User)              extends PlaylistKind
case class BasedOnArtist(artist: Artist)          extends PlaylistKind
case class BasedOnGenres(genres: Set[MusicGenre]) extends PlaylistKind

case class Playlist(name: String, kind: PlaylistKind, songs: List[Song])

object Playlist extends App {
  val fooFighters = Artist("Foo Fighters")
  val playlist1 = Playlist(
    "This is Foo Fighters",
    BasedOnArtist(fooFighters),
    List(Song(fooFighters, "Breakout"), Song(fooFighters, "Learn To Fly"))
  )

  val playlist2 = Playlist(
    "Deep Focus",
    BasedOnGenres(Set(House, Funk)),
    List(Song(Artist("Daft Punk"), "One More Time"), Song(Artist("The Chemical Brothers"), "Hey Boy Hey Girl"))
  )

  val playlist3 = Playlist(
    "Michał's Playlist",
    CuratedByUser(User("Michał Płachta")),
    List(Song(fooFighters, "My Hero"), Song(Artist("Iron Maiden"), "The Trooper"))
  )

  def gatherSongs(playlists: List[Playlist], searchedArtist: Artist, searchedGenre: MusicGenre): List[Song] = {
    playlists.foldLeft(List.empty[Song])((songs, playlist) => {
      val matchingSongs = playlist.kind match {
        case CuratedByUser(user)   => playlist.songs.filter(_.artist == searchedArtist)
        case BasedOnArtist(artist) => if (artist == searchedArtist) playlist.songs else List.empty
        case BasedOnGenres(genres) => if (genres.contains(searchedGenre)) playlist.songs else List.empty
      }
      songs.appendedAll(matchingSongs)
    })
  }

  check {
    gatherSongs(List(playlist1, playlist2, playlist3), fooFighters, Funk)
  }.expect(playlist1.songs.appendedAll(playlist2.songs).appended(Song(fooFighters, "My Hero")))
}
