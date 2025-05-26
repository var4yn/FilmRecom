import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/auth';

function Navbar() {
  const navigate = useNavigate();
  const isAuthenticated = authService.isAuthenticated();

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <nav className="navbar">
      <div className="container">
        <Link to="/">FilmRecom</Link>
        <div className="nav-links">
          <Link to="/movies">Фильмы</Link>
          {isAuthenticated ? (
            <>
              <Link to="/recommendations">Рекомендации</Link>
              <Link to="/profile">Профиль</Link>
              <button onClick={handleLogout} className="btn-link">Выйти</button>
            </>
          ) : (
            <>
              <Link to="/login">Войти</Link>
              <Link to="/register">Регистрация</Link>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}

export default Navbar; 