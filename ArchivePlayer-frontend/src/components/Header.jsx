import React from 'react';

const Header = ({ username }) => {
    return (
        <header className="top-header">
            <div className="user-badge">
                <div className="user-avatar">{username?.charAt(0).toUpperCase()}</div>
                <span className="user-name">{username}</span>
            </div>
        </header>
    );
};

export default Header;