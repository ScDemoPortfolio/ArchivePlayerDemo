import { useState, useCallback } from 'react';
import { apiFetch } from '../services/api';

export const usePlayback = (user) => {
    const [currentSong, setCurrentSong] = useState(null);
    const [currentSongIndex, setCurrentSongIndex] = useState(-1);
    const [currentPlaylist, setCurrentPlaylist] = useState(null);
    const [isPlaying, setIsPlaying] = useState(false);
    const [progress, setProgress] = useState(0);

    const playSong = useCallback((song, index = -1, playlist = null) => {
        setCurrentSong(song);
        setCurrentSongIndex(index);
        if (playlist) {
            setCurrentPlaylist(playlist);
        }
        setProgress(0);
        setIsPlaying(true);
        apiFetch(`/listens/record?songId=${song.id}`, { method: 'POST' }, user.sessionToken);
    }, [user.sessionToken]);

    const playPlaylistOrSong = useCallback(async (playlistId, specificSong = null) => {
        if (specificSong && playlistId) {
            const response = await apiFetch(`/playlists/${playlistId}`, {}, user.sessionToken);
            if (response.ok) {
                const data = await response.json();
                const songList = Array.isArray(data.songs) ? data.songs : [];
                const songIndex = songList.findIndex(s => s.id === specificSong.id);
                playSong(specificSong, songIndex, data);
            }
            return;
        }

        const response = await apiFetch(`/playlists/${playlistId}`, {}, user.sessionToken);
        if (response.ok) {
            const data = await response.json();
            const songList = Array.isArray(data.songs) ? data.songs : [];
            if (songList.length > 0) {
                playSong(songList[0], 0, data);
            } else {
                alert("This playlist is empty.");
            }
        }
    }, [user.sessionToken, playSong]);

    const handleSongEnded = useCallback(() => {
        if (currentPlaylist && currentPlaylist.songs && currentSongIndex !== -1) {
            const songList = Array.isArray(currentPlaylist.songs) ? currentPlaylist.songs : [];
            const nextIndex = currentSongIndex + 1;
            
            if (nextIndex < songList.length) {
                playSong(songList[nextIndex], nextIndex);
            } else {
                setIsPlaying(false);
            }
        } else {
            setIsPlaying(false);
        }
    }, [currentPlaylist, currentSongIndex, playSong]);

    const closePlayer = useCallback(() => {
        setIsPlaying(false);
        setCurrentSong(null);
        setCurrentSongIndex(-1);
        setCurrentPlaylist(null);
        setProgress(0);
    }, []);

    return {
        currentSong,
        currentPlaylist,
        isPlaying,
        setIsPlaying,
        progress,
        setProgress,
        playSong,
        playPlaylistOrSong,
        handleSongEnded,
        closePlayer
    };
};