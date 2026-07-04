import React from 'react';
import { 
    FiHome, 
    FiSearch, 
    FiUsers, 
    FiMusic, 
    FiPlus, 
    FiSettings, 
    FiLogOut, 
    FiHeadphones
} from "react-icons/fi";

const Sidebar = ({ 
    activeTab, 
    setActiveTab, 
    setIsModalOpen, 
    playlistSearch, 
    setPlaylistSearch, 
    playlists, 
    playlistsError, 
    handleNavigation, 
    handleLogoutAction 
}) => {
    const primaryNav = [
        { id: 'Home', label: 'Home', icon: <FiHome size={18} /> },
        { id: 'Search', label: 'Search', icon: <FiSearch size={18} /> },
        { id: 'Social', label: 'Social Hub', icon: <FiUsers size={18} /> }
    ];

    return (
        <aside className="sidebar">
            <div className="branding">
                <div style={{ color: 'white' }}><FiHeadphones size={24} /></div>
                <h1 className="logo-text">ArchivePlayer</h1>
            </div>
            
            <div className="nav-group">
                {primaryNav.map(item => (
                    <button key={item.id} onClick={() => setActiveTab(item.id)} className={`nav-btn ${activeTab === item.id ? 'active' : ''}`}>
                        <span className="icon-wrapper">{item.icon}</span><span>{item.label}</span>
                    </button>
                ))}
            </div>
            
            <div className="divider" />
            
            <div className="playlists-section">
                <div className="playlists-header">
                    <button onClick={() => setActiveTab(activeTab === 'Playlists' ? 'Home' : 'Playlists')} className="playlists-toggle-btn">
                        <FiMusic size={14} /> Playlists
                    </button>
                    <button onClick={() => setIsModalOpen(true)} className="add-playlist-btn" title="Create new playlist">
                        <FiPlus size={18} />
                    </button>
                </div>

                <div className="playlist-filter-wrapper">
                    <FiSearch size={16} className="playlist-filter-icon" />
                    <input 
                        type="text" 
                        placeholder="Filter playlists..." 
                        value={playlistSearch} 
                        onChange={e => setPlaylistSearch(e.target.value)} 
                        className="playlist-filter-input" 
                    />
                </div>

                {(activeTab === 'Playlists' || activeTab === 'PlaylistView') && (
                    <div className="playlist-list">
                        {playlistsError ? (
                            <div style={{ color: '#7f8c8d', fontSize: '11px', textAlign: 'center', padding: '10px' }}>{playlistsError}</div>
                        ) : playlists.filter(p => p.name.toLowerCase().includes(playlistSearch.toLowerCase())).length > 0 ? (
                            playlists.filter(p => p.name.toLowerCase().includes(playlistSearch.toLowerCase())).map(p => (
                                <button 
                                    key={p.id} 
                                    className={`playlist-item-btn ${activeTab === 'PlaylistView' && p.id === p.id ? 'active' : ''}`}
                                    onClick={() => handleNavigation('PlaylistView', p.id, p.name)}
                                >
                                    <div className="playlist-dot"></div>
                                    <span className="playlist-name-sidebar">{p.name}</span>
                                </button>
                            ))
                        ) : (
                            <div style={{ color: '#7f8c8d', fontSize: '11px', textAlign: 'center', padding: '10px' }}>No matches found</div>
                        )}
                    </div>
                )}
            </div>

            <div style={{ flexGrow: 1 }} />
            
            <div className="nav-group">
                <button onClick={() => setActiveTab('Settings')} className={`nav-btn ${activeTab === 'Settings' ? 'active' : ''}`}>
                    <span className="icon-wrapper"><FiSettings size={18} /></span><span>Settings</span>
                </button>
                <button onClick={handleLogoutAction} className="nav-btn" style={{ color: '#e74c3c' }}>
                    <span className="icon-wrapper"><FiLogOut size={18} /></span><span>Logout</span>
                </button>
            </div>
        </aside>
    );
};

export default Sidebar;