import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../services/auth';
import api from '../services/api';

function Profile() {
  const [user, setUser] = useState(null);
  const [ratedMovies, setRatedMovies] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const currentUser = authService.getCurrentUser();
    if (!currentUser) {
      navigate('/login');
      return;
    }

    setUser(currentUser);
    fetchUserData();
  }, [navigate]);

  const fetchUserData = async () => {
    try {
      const user = await authService.getCurrentUser();
      const ratings = await api.getRatedMovies(user.id);
      setRatedMovies(ratings);
    } catch (error) {
      console.error('Ошибка при загрузке данных пользователя:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Загрузка...</div>;
  if (!user) return null;

  return (
    <div className="profile">
      <h1>Профиль пользователя</h1>
      <div className="profile-info">
        <h2>{user.username}</h2>
        <p>Email: {user.email}</p>
      </div>

      <div className="rated-movies">
        <h2>Оцененные фильмы</h2>
        {ratedMovies.length > 0 ? (
          <div className="movie-grid">
            {ratedMovies.map((rating) => (
              <Link 
                key={rating.movie.tmdbId} 
                to={`/movies/${rating.movie.tmdbId}`}
                className="movie-card"
              >
                <img 
                  src={rating.movie.posterUrl} 
                  alt={rating.movie.title} 
                  className="movie-poster"
                />
                <div className="movie-card-content">
                  <h3 className="movie-title">{rating.movie.title}</h3>
                  <p className="movie-rating">
                    Ваша оценка: {rating.score.toFixed(1)} ★
                  </p>
                  {rating.review && (
                    <p className="movie-review">
                      Ваш отзыв: {rating.review}
                    </p>
                  )}
                  <p className="movie-year">
                    Год: {rating.movie.releaseYear || (rating.movie.releaseDate ? new Date(rating.movie.releaseDate).getFullYear() : 'Н/Д')}
                  </p>
                  <p className="movie-genre">
                    {rating.movie.genres?.map(g => g.name).join(', ')}
                  </p>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <p>У вас пока нет оцененных фильмов</p>
        )}
      </div>
    </div>
  );
}

export default Profile; 