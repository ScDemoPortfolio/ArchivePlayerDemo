import React, {useEffect, useState, useCallback} from 'react';
import './UserPageView.css';
import { API_BASE_URL } from '../constants';

const UserPageView = ({userId, user, onNavigate}) => {
    const [userDetails, setUserDetails] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [isFollowing, setIsFollowing] = useState(false);
    const [isBlockedByMe, setIsBlockedByMe] = useState(false);
    const [blockedByThem, setBlockedByThem] = useState(false);
    const [counts, setCounts] = useState({followers: 0, following: 0});

    const apiFetch = useCallback(async (endpoint, options = {}) => {
        return fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers: {
                ...options.headers,
                'Authorization': `Bearer ${user.sessionToken}`
            }
        });
    }, [user.sessionToken]);

    const fetchUserDetails = useCallback(async () => {
        setIsLoading(true);
        setBlockedByThem(false);
        try {
            const response = await apiFetch(`/social/account/${userId}`);
            if (response.status === 403) {
                setBlockedByThem(true);
                setIsLoading(false);
                return;
            }
            if (response.ok) {
                const data = await response.json();
                setUserDetails(data);
                setIsFollowing(data.isFollowedByMe);
                setIsBlockedByMe(data.isBlockedByMe);
                
                const countsResponse = await apiFetch(`/social/account/${userId}/counts`);
                if (countsResponse.ok) setCounts(await countsResponse.json());
            }
        } catch (error) {
            console.error("Error fetching user details:", error);
        } finally {
            setIsLoading(false);
        }
    }, [userId, apiFetch]);

    useEffect(() => {
        if (userId && user.id) fetchUserDetails();
    }, [userId, user.id, fetchUserDetails]);

    const handleFollowToggle = async () => {
        const endpoint = isFollowing ? 'unfollow' : 'follow';
        try {
            const response = await apiFetch(`/social/${endpoint}?accountTo${endpoint === 'follow' ? 'Follow' : 'Unfollow'}Id=${userId}`, {method: 'POST'});
            if (response.ok) {
                setIsFollowing(!isFollowing);
                fetchUserDetails();
            }
        } catch (error) {
            console.error(`Failed to ${endpoint}:`, error);
        }
    };

    const handleBlockToggle = async () => {
        const endpoint = isBlockedByMe ? 'unblock' : 'block';
        try {
            const response = await apiFetch(`/social/${endpoint}?targetId=${userId}`, {method: 'POST'});
            if (response.ok) {
                setIsBlockedByMe(!isBlockedByMe);
                if (endpoint === 'block') setIsFollowing(false);
                fetchUserDetails();
            }
        } catch (error) {
            console.error(`Failed to ${endpoint}:`, error);
        }
    };

    if (isLoading) return <div className="status-container"><p className="status-text">Loading profile...</p></div>;
    
    if (blockedByThem) return (
        <div className="user-page-container">
            <div className="profile-card error-card">
                <div className="profile-avatar alert-avatar">!</div>
                <h2 className="profile-name">Access Restricted</h2>
                <p className="status-text">This user has blocked you or the account is private.</p>
                <button onClick={() => onNavigate('Social')} className="back-btn">Go Back</button>
            </div>
        </div>
    );

    if (!userDetails) return <div className="status-container"><p className="status-text">User not found.</p></div>;

    return (
        <div className="user-page-container">
            <div className="profile-card">
                <div className="profile-avatar">{userDetails.username.charAt(0).toUpperCase()}</div>
                <h2 className="profile-name">{userDetails.username}</h2>
                
                {!isBlockedByMe && (
                    <div className="profile-stats">
                        <div className="stat-item" onClick={() => onNavigate('FollowList', userId, { type: 'followers', username: userDetails.username })}>
                            <span className="stat-value">{counts.followers}</span>
                            <span className="stat-label">Followers</span>
                        </div>
                        <div className="stat-item" onClick={() => onNavigate('FollowList', userId, { type: 'following', username: userDetails.username })}>
                            <span className="stat-value">{counts.following}</span>
                            <span className="stat-label">Following</span>
                        </div>
                    </div>
                )}

                {user.id !== userId && (
                    <div className="profile-actions">
                        {isBlockedByMe ? (
                            <button onClick={handleBlockToggle} className="action-btn danger-btn">Unblock User</button>
                        ) : (
                            <>
                                <button 
                                    onClick={handleFollowToggle} 
                                    className={`action-btn ${isFollowing ? 'secondary-btn' : 'primary-btn'}`}
                                >
                                    {isFollowing ? 'Unfollow' : 'Follow'}
                                </button>
                                <button onClick={handleBlockToggle} className="action-btn danger-btn">Block</button>
                            </>
                        )}
                    </div>
                )}
            </div>

            {!isBlockedByMe && (
                <div className="profile-content">
                    <h3 className="section-title">Listener Activity</h3>
                    <p className="status-text">Recently active on ArchivePlayer.</p>
                </div>
            )}
        </div>
    );
};

export default UserPageView;