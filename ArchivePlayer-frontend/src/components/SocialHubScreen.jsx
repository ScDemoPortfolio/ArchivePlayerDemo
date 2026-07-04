import React, { useState, useEffect, useCallback } from 'react';
import './SocialHubScreen.css';
import { API_BASE_URL } from '../constants';

const SocialHubScreen = ({ user, onNavigate }) => {
    const [searchQuery, setSearchQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [counts, setCounts] = useState({ followers: 0, following: 0 });

    const apiFetch = useCallback(async (endpoint, options = {}) => {
        return fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${user.sessionToken}`
            }
        });
    }, [user.sessionToken]);

    const fetchCounts = useCallback(async () => {
        if (!user?.id) return;
        try {
            const response = await apiFetch(`/social/account/${user.id}/counts`);
            if (response.ok) setCounts(await response.json());
        } catch (error) {
            console.error("Failed to fetch follow counts:", error);
        }
    }, [user.id, apiFetch]);

    useEffect(() => { fetchCounts(); }, [fetchCounts]);

    useEffect(() => {
        if (!searchQuery.trim()) {
            setSearchResults([]);
            return;
        }

        const delayDebounceFn = setTimeout(async () => {
            setIsLoading(true);
            try {
                const response = await apiFetch(`/social/search?query=${encodeURIComponent(searchQuery)}`);
                if (response.ok) setSearchResults(await response.json());
            } catch (error) {
                console.error("Account search failed:", error);
            } finally {
                setIsLoading(false);
            }
        }, 300);

        return () => clearTimeout(delayDebounceFn);
    }, [searchQuery, apiFetch]);

    return (
        <div className="social-hub-container">
            <div className="social-header">
                <h2 className="social-title">Social Hub</h2>
                <p className="social-subtitle">Connect with other listeners on ArchivePlayer.</p>
            </div>

            <div className="stats-grid">
                <div className="stat-box" onClick={() => onNavigate('FollowList', user.id, { type: 'followers', username: user.username })}>
                    <span className="stat-number">{counts.followers}</span>
                    <span className="stat-text">Followers</span>
                </div>
                <div className="stat-box" onClick={() => onNavigate('FollowList', user.id, { type: 'following', username: user.username })}>
                    <span className="stat-number">{counts.following}</span>
                    <span className="stat-text">Following</span>
                </div>
            </div>

            <div className="user-search-wrapper">
                <input
                    type="text"
                    placeholder="Search for people..."
                    value={searchQuery}
                    onChange={e => setSearchQuery(e.target.value)}
                    className="search-input"
                    autoFocus
                />
            </div>

            {isLoading && <p className="status-text" style={{ textAlign: 'center' }}>Searching community...</p>}

            <div className="people-list">
                {searchResults.length > 0 ? (
                    searchResults.map(account => (
                        <div key={account.id} className="person-card" onClick={() => onNavigate('UserView', account.id, account.username)}>
                            <div className="person-avatar">{account.username.charAt(0).toUpperCase()}</div>
                            <div className="person-meta">
                                <h4 className="person-name">{account.username}</h4>
                                <span className="person-sub">Listener</span>
                            </div>
                            <div className="view-profile-link">View Profile</div>
                        </div>
                    ))
                ) : !isLoading && searchQuery ? (
                    <p className="status-text" style={{ gridColumn: '1 / -1', textAlign: 'center' }}>No users found matching "{searchQuery}"</p>
                ) : null}
            </div>
        </div>
    );
};

export default SocialHubScreen;