import React from 'react';
import { FiMusic } from "react-icons/fi";
import './HomeOverview.css'; // Explicitly import the CSS file

const HomeOverview = ({ randomPlaylists, followingRecentListens, handleNavigation, setActiveTab }) => {
    return (
        <div className="home-split-layout">
            <section className="home-window">
                <h3 className="window-title">Your Mixes</h3>
                <div className="window-content">
                    <div className="mixes-grid">
                        {randomPlaylists.length > 0 ? randomPlaylists.map(p => (
                            <div key={p.id} className="mix-card" onClick={() => handleNavigation('PlaylistView', p.id, p.name)}>
                                <div className="mix-card-icon"><FiMusic /></div>
                                <div className="mix-card-name">{p.name}</div>
                            </div>
                        )) : <p style={{ color: 'var(--text-secondary)', fontSize: '14px', textAlign: 'center', marginTop: '40px' }}>No playlists yet.</p>}
                    </div>
                </div>
            </section>
            
            <section className="home-window">
                <h3 className="window-title">Friend Activity</h3>
                <div className="window-content">
                    {followingRecentListens.length > 0 ? (
                        <div className="activity-stack">
                            {followingRecentListens.map(listen => (
                                <div key={listen.id} className="activity-row">
                                    <div className="mix-card-icon" style={{ width: '40px', height: '40px', fontSize: '18px' }}>
                                        <FiMusic />
                                    </div>
                                    <div className="activity-meta">
                                        <h4 className="activity-title">{listen.songTitle}</h4>
                                        <span className="activity-sub">{listen.listenerUsername} • {listen.artistName}</span>
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', textAlign: 'center', gap: '16px', padding: '40px' }}>
                            <p style={{ color: 'var(--text-secondary)' }}>Follow your friends to see what they are listening to!</p>
                            <button onClick={() => setActiveTab('Social')} style={{ backgroundColor: 'var(--accent-color)', border: 'none', color: '#ffffff', padding: '10px 20px', borderRadius: '6px', cursor: 'pointer' }}>Find Friends</button>
                        </div>
                    )}
                </div>
            </section>
        </div>
    );
};

export default HomeOverview;