import React, { useState, useEffect } from 'react';
import LoginScreen from './components/LoginScreen';
import HomeScreen from "./components/HomeScreen";
import { API_BASE_URL } from './constants';

function App() {
  const [user, setUser] = useState(null);

  const handleLoginSuccess = (userData) => {
    setUser(userData);
  };

  const handleLogout = () => {
    setUser(null);
  };

  useEffect(() => {
    if (!user || !user.sessionToken) return;

    const interval = setInterval(async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/auth/validate-session`, {
            headers: {
                'Authorization': `Bearer ${user.sessionToken}`
            }
        });

        if (!response.ok) {
          alert("You have been logged out because this account was signed in on another device.");
          setUser(null);
        }
      } catch (error) {
        console.error("Session validation check failed.");
      }
    }, 10000);

    return () => clearInterval(interval);
  }, [user]);

  return (
      <div style={{ backgroundColor: '#0a0512', minHeight: '100vh' }}>
        {user ? (
            <HomeScreen user={user} onLogout={handleLogout} />
        ) : (
            <LoginScreen onLoginSuccess={handleLoginSuccess} />
        )}
      </div>
  );
}
export default App;