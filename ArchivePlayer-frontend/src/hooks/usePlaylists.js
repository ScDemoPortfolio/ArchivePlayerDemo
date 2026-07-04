import { useState, useCallback } from 'react';
import { apiFetch } from '../services/api';

export const usePlaylists = (user) => {
    const [playlists, setPlaylists] = useState([]);
    const [playlistsError, setPlaylistsError] = useState(null);

    const fetchPlaylists = useCallback(async () => {
        if (!user?.id) return;
        try {
            const response = await apiFetch(`/playlists/account/${user.id}`, {}, user.sessionToken);
            if (response.ok) {
                const data = await response.json();
                // Ensure data is an array before setting state
                setPlaylists(Array.isArray(data) ? data : []);
                setPlaylistsError(null);
            } else {
                setPlaylistsError('Failed to load collections.');
            }
        } catch (error) {
            setPlaylistsError('Playlist server unreachable.');
        }
    }, [user]);

    const createPlaylist = useCallback(async (name) => {
        const response = await apiFetch(`/playlists`, {
            method: 'POST',
            body: JSON.stringify({ name }),
        }, user.sessionToken);

        if (response.ok) {
            await fetchPlaylists();
            return { success: true };
        } else {
            return { success: false, error: 'Could not save playlist.' };
        }
    }, [user, fetchPlaylists]);

    return { playlists, playlistsError, fetchPlaylists, createPlaylist };
};