import React, { useState, useEffect, useCallback } from 'react';
import Sidebar from './Sidebar';
import Header from './Header';
import MusicPlayer from './MusicPlayer';
import HomeOverview from './HomeOverview';
import PlaylistView from './PlaylistView';
import SocialHubScreen from './SocialHubScreen';
import UserPageView from './UserPageView'; 
import FollowListView from './FollowListView';
import SearchScreen from './SearchScreen'; 
import SettingsScreen from './SettingsScreen'; 
import PlaylistModal from './PlaylistModal';
import TrackListView from './TrackListView';
import { usePlaylists } from '../hooks/usePlaylists';
import { usePlayback } from '../hooks/usePlayback';
import { apiFetch } from '../services/api';
import { ROUTES } from '../constants';
import './HomeScreen.css';

const HomeScreen = ({ user, onLogout }) => {
    const [activeTab, setActiveTab] = useState(ROUTES.HOME);
    const [playlistSearch, setPlaylistSearch] = useState('');
    const [selectedTargetId, setSelectedTargetId] = useState(null);
    const [selectedTargetName, setSelectedTargetName] = useState('');
    const [navigationData, setNavigationData] = useState(null); 
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [newPlaylistName, setNewPlaylistName] = useState('');
    const [modalError, setModalError] = useState('');
    
    const [randomPlaylists, setRandomPlaylists] = useState([]);
    const [followingRecentListens, setFollowingRecentListens] = useState([]);

    const { playlists, playlistsError, fetchPlaylists, createPlaylist } = usePlaylists(user);
    const playback = usePlayback(user);

    const fetchHomeData = useCallback(async (force = false) => {
        if (!user?.id || (!force && activeTab !== ROUTES.HOME)) return;
        try {
            const randomRes = await apiFetch(`/playlists/account/${user.id}/random?limit=6`, {}, user.sessionToken);
            if (randomRes.ok) {
                const data = await randomRes.json();
                setRandomPlaylists(Array.isArray(data) ? data : []);
            } else {
                setRandomPlaylists([]);
            }

            const socialRes = await apiFetch(`/social/following/${user.id}/recent-listens`, {}, user.sessionToken);
            if (socialRes.ok) {
                const data = await socialRes.json();
                setFollowingRecentListens(Array.isArray(data) ? data : []);
            } else {
                setFollowingRecentListens([]);
            }
        } catch (error) {
            console.error("Home data failed:", error);
            setRandomPlaylists([]);
            setFollowingRecentListens([]);
        }
    }, [user, activeTab]);

    useEffect(() => { fetchPlaylists(); }, [fetchPlaylists]);
    useEffect(() => { fetchHomeData(); }, [fetchHomeData]);

    const handleCreatePlaylist = async (e) => {
        e.preventDefault();
        const result = await createPlaylist(newPlaylistName);
        if (result.success) {
            await fetchHomeData(true); 
            setIsModalOpen(false);
            setNewPlaylistName('');
            setActiveTab(ROUTES.HOME);
        } else {
            setModalError(result.error);
        }
    };

    const handleLogoutAction = async () => {
        try {
            await apiFetch(`/auth/logout`, { method: 'POST' }, user.sessionToken);
        } finally {
            if (onLogout) onLogout();
        }
    };

    const handleNavigation = (viewName, targetId, data = null) => {
        setActiveTab(viewName);
        setSelectedTargetId(targetId);
        if (typeof data === 'string') {
            setSelectedTargetName(data);
            setNavigationData(null);
        } else {
            setNavigationData(data);
            setSelectedTargetName(data?.username || data?.name || '');
        }
    };

    const handlePlaylistDeletionSuccess = useCallback(async () => {
        await fetchPlaylists();
        setActiveTab(ROUTES.HOME);
    }, [fetchPlaylists]);

    const renderContent = () => {
        switch (activeTab) {
            case ROUTES.SEARCH: return <SearchScreen user={user} onNavigate={handleNavigation} playlists={playlists} onPlaySong={playback.playSong} />;
            case ROUTES.PLAYLIST_VIEW: return <PlaylistView user={user} playlistId={selectedTargetId} playlistName={selectedTargetName} onPlaylistDeleted={handlePlaylistDeletionSuccess} onPlayPlaylist={playback.playPlaylistOrSong} />;
            case ROUTES.ARTIST_VIEW: return <TrackListView user={user} type="artists" id={selectedTargetId} title={selectedTargetName} onPlaySong={playback.playSong} playlists={playlists} />;
            case ROUTES.ALBUM_VIEW: return <TrackListView user={user} type="albums" id={selectedTargetId} title={selectedTargetName} onPlaySong={playback.playSong} playlists={playlists} />;
            case ROUTES.SOCIAL: return <SocialHubScreen user={user} onNavigate={handleNavigation} />;
            case ROUTES.USER_VIEW: return <UserPageView userId={selectedTargetId} user={user} onNavigate={handleNavigation} />;
            case ROUTES.FOLLOW_LIST: return <FollowListView accountId={selectedTargetId} type={navigationData.type} username={navigationData.username} onNavigate={handleNavigation} user={user} />;
            case ROUTES.SETTINGS: return <SettingsScreen user={user} />;
            case ROUTES.HOME:
            default: return (
                <HomeOverview 
                    randomPlaylists={randomPlaylists} 
                    followingRecentListens={followingRecentListens} 
                    handleNavigation={handleNavigation} 
                    setActiveTab={setActiveTab} 
                />
            );
        }
    };

    return (
        <div className="home-wrapper">
            <Sidebar 
                activeTab={activeTab} 
                setActiveTab={setActiveTab} 
                setIsModalOpen={setIsModalOpen} 
                playlistSearch={playlistSearch} 
                setPlaylistSearch={setPlaylistSearch} 
                playlists={playlists} 
                playlistsError={playlistsError} 
                handleNavigation={handleNavigation} 
                handleLogoutAction={handleLogoutAction} 
            />
            
            <div className="main-content">
                <Header username={user?.username} />
                <main className="content-area" style={{ paddingBottom: playback.currentSong ? '120px' : '40px' }}>
                    {renderContent()}
                </main>
                
                <MusicPlayer 
                    currentSong={playback.currentSong}
                    currentPlaylist={playback.currentPlaylist}
                    isPlaying={playback.isPlaying}
                    setIsPlaying={playback.setIsPlaying}
                    progress={playback.progress}
                    setProgress={playback.setProgress}
                    closePlayer={playback.closePlayer}
                    onSongEnded={playback.handleSongEnded}
                />
            </div>

            <PlaylistModal 
                isModalOpen={isModalOpen}
                setIsModalOpen={setIsModalOpen}
                newPlaylistName={newPlaylistName}
                handleCreatePlaylist={handleCreatePlaylist}
                modalError={typeof modalError === 'object' ? 'An error occurred' : modalError}
            />
        </div>
    );
};

export default HomeScreen;