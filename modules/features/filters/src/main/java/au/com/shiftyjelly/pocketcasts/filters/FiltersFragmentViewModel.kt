package au.com.shiftyjelly.pocketcasts.filters

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import au.com.shiftyjelly.pocketcasts.models.entity.Playlist
import au.com.shiftyjelly.pocketcasts.repositories.playback.PlaybackManager
import au.com.shiftyjelly.pocketcasts.repositories.podcast.EpisodeManager
import au.com.shiftyjelly.pocketcasts.repositories.podcast.PlaylistManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Collections
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class FiltersFragmentViewModel @Inject constructor(val playlistManager: PlaylistManager, episodeManager: EpisodeManager, playbackManager: PlaybackManager) : ViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    val filters: LiveData<List<Playlist>> = LiveDataReactiveStreams.fromPublisher(
        playlistManager.observeAll()
    )

    val countGenerator = { playlist: Playlist ->
        playlistManager.countEpisodesRx(playlist, episodeManager, playbackManager).onErrorReturn { 0 }
    }

    var adapterState: MutableList<Playlist> = mutableListOf()
    fun movePlaylist(fromPosition: Int, toPosition: Int): List<Playlist> {
        if (fromPosition < toPosition) {
            for (index in fromPosition until toPosition) {
                Collections.swap(adapterState, index, index + 1)
            }
        } else {
            for (index in fromPosition downTo toPosition + 1) {
                Collections.swap(adapterState, index, index - 1)
            }
        }
        return adapterState.toList()
    }

    fun commitMoves() {
        val playlists = adapterState

        playlists.forEachIndexed { index, playlist ->
            playlist.sortPosition = index
            playlist.syncStatus = Playlist.SYNC_STATUS_NOT_SYNCED
        }

        runBlocking(Dispatchers.Default) { playlistManager.updateAll(playlists) }
    }
}